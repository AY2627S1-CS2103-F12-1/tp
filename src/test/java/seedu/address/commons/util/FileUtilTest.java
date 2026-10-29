package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestUtil.createSymbolicLinkOrSkip;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileUtilTest {
    private static final String CONTENT = "{ \"students\" : [ ] }";
    private static final String OLD_CONTENT = "old content";

    @TempDir
    public Path testFolder;

    @Test
    public void readFromFile_multiByteUtf8_decoded() throws IOException {
        Path file = testFolder.resolve("tutorflow.json");
        // U+FEFF byte order mark, é (2 bytes), → (3 bytes) and an emoji (4 bytes)
        Files.write(file, "\ufeffR\u00e9vision \u2192 \ud83d\ude00".getBytes(StandardCharsets.UTF_8));

        assertEquals("\ufeffR\u00e9vision \u2192 \ud83d\ude00", FileUtil.readFromFile(file));
    }

    @Test
    public void readFromFile_invalidUtf8_throwsIoException() throws IOException {
        Path file = testFolder.resolve("tutorflow.json");

        // Issue #156: Windows-1252 é (E9) followed by an ASCII letter, not a continuation byte
        Files.write(file, new byte[] {'R', (byte) 0xE9, 'v'});
        assertThrows(IOException.class, "The file is not valid UTF-8 text near line 1, column 2. Save the file as "
                + "UTF-8.", () -> FileUtil.readFromFile(file));

        // a sequence cut off at the end of the file
        Files.write(file, new byte[] {'a', 'b', (byte) 0xE2, (byte) 0x82});
        assertInvalidUtf8(file, 1, 3);

        // a continuation byte without a lead byte
        Files.write(file, new byte[] {(byte) 0x80});
        assertInvalidUtf8(file, 1, 1);

        // PE round 2: a file saved as UTF-16, which starts with the bytes FF FE
        Files.write(file, new byte[] {(byte) 0xFF, (byte) 0xFE, '{', 0, '}', 0});
        assertInvalidUtf8(file, 1, 1);

        // a later line, after a character of two bytes
        Files.write(file, new byte[] {'{', '\n', ' ', (byte) 0xC3, (byte) 0xA9, (byte) 0xE9});
        assertInvalidUtf8(file, 2, 3);

        // Windows line endings, whose carriage return is not counted in the column
        Files.write(file, new byte[] {'{', '\r', '\n', '\r', '\n', 'a', (byte) 0xE9});
        assertInvalidUtf8(file, 3, 2);
    }

    private static void assertInvalidUtf8(Path file, int line, int column) {
        assertThrows(IOException.class, String.format(FileUtil.MESSAGE_INVALID_UTF8, line, column), () ->
                FileUtil.readFromFile(file));
    }

    @Test
    public void writeToFile_symbolicLink_writesTargetAndKeepsLink() throws IOException {
        // PE round 2: the temporary file replaced the link, so the file it points to silently kept the old data
        Path target = Files.createDirectories(testFolder.resolve("real")).resolve("tutorflow.json");
        Files.writeString(target, OLD_CONTENT);
        Path link = Files.createDirectories(testFolder.resolve("data")).resolve("tutorflow.json");
        createSymbolicLinkOrSkip(link, Path.of("..", "real", "tutorflow.json"));

        FileUtil.writeToFile(link, CONTENT);

        assertTrue(Files.isSymbolicLink(link));
        assertEquals(CONTENT, Files.readString(target));
        assertEquals(List.of(link), listFiles(link.getParent()));
        assertEquals(List.of(target), listFiles(target.getParent()));
    }

    @Test
    public void writeToFile_symbolicLinkToMissingFile_createsTargetAndKeepsLink() throws IOException {
        // PE round 3: a link whose file was missing or deleted was replaced by a regular file
        Path target = Files.createDirectories(testFolder.resolve("real")).resolve("tutorflow.json");
        Path link = Files.createDirectories(testFolder.resolve("data")).resolve("tutorflow.json");
        createSymbolicLinkOrSkip(link, Path.of("..", "real", "tutorflow.json"));

        FileUtil.writeToFile(link, CONTENT);

        assertTrue(Files.isSymbolicLink(link));
        assertEquals(CONTENT, Files.readString(target));
        assertEquals(List.of(link), listFiles(link.getParent()));
        assertEquals(List.of(target), listFiles(target.getParent()));
    }

    @Test
    public void writeToFile_chainOfSymbolicLinksToMissingFile_createsTargetAndKeepsLinks() throws IOException {
        Path target = Files.createDirectories(testFolder.resolve("real")).resolve("tutorflow.json");
        // each link is relative to its own folder
        Path middleLink = Files.createDirectories(testFolder.resolve("middle")).resolve("link.json");
        createSymbolicLinkOrSkip(middleLink, Path.of("..", "real", "tutorflow.json"));
        Path link = Files.createDirectories(testFolder.resolve("data")).resolve("tutorflow.json");
        createSymbolicLinkOrSkip(link, Path.of("..", "middle", "link.json"));

        FileUtil.writeToFile(link, CONTENT);

        assertTrue(Files.isSymbolicLink(link));
        assertTrue(Files.isSymbolicLink(middleLink));
        assertEquals(CONTENT, Files.readString(target));
    }

    @Test
    public void writeToFile_symbolicLinkIntoMissingFolder_throwsAndKeepsLink() throws IOException {
        Path link = Files.createDirectories(testFolder.resolve("data")).resolve("tutorflow.json");
        createSymbolicLinkOrSkip(link, Path.of("..", "gone", "tutorflow.json"));

        assertThrows(IOException.class, () -> FileUtil.writeToFile(link, CONTENT));

        assertTrue(Files.isSymbolicLink(link));
        assertFalse(Files.exists(testFolder.resolve("gone")));
        assertEquals(List.of(link), listFiles(link.getParent()));
    }

    @Test
    public void writeToFile_symbolicLinkToItself_throwsAndKeepsLink() throws IOException {
        Path link = testFolder.resolve("tutorflow.json");
        createSymbolicLinkOrSkip(link, Path.of("tutorflow.json"));

        assertThrows(IOException.class, () -> FileUtil.writeToFile(link, CONTENT));

        assertTrue(Files.isSymbolicLink(link));
        assertEquals(List.of(link), listFiles(testFolder));
    }

    @Test
    public void writeToFile_missingFileAndFolder_createsFile() throws IOException {
        Path file = testFolder.resolve("data").resolve("tutorflow.json");

        FileUtil.writeToFile(file, CONTENT);

        assertEquals(CONTENT, FileUtil.readFromFile(file));
        assertEquals(List.of(file), listFiles(file.getParent()));
    }

    @Test
    public void writeToFile_existingFile_replacesContent() throws IOException {
        Path file = testFolder.resolve("tutorflow.json");
        Files.writeString(file, OLD_CONTENT + OLD_CONTENT);

        FileUtil.writeToFile(file, CONTENT);

        assertEquals(CONTENT, FileUtil.readFromFile(file));
        assertEquals(List.of(file), listFiles(testFolder)); // no temporary file left behind
    }

    @Test
    public void writeToFile_readOnlyFile_throwsAccessDeniedException() throws IOException {
        Path file = testFolder.resolve("tutorflow.json");
        Files.writeString(file, OLD_CONTENT);
        assertTrue(file.toFile().setWritable(false));

        try {
            assertThrows(AccessDeniedException.class, () -> FileUtil.writeToFile(file, CONTENT));
            assertEquals(OLD_CONTENT, FileUtil.readFromFile(file));
            assertFalse(Files.isWritable(file));
            assertEquals(List.of(file), listFiles(testFolder));
        } finally {
            file.toFile().setWritable(true);
        }
    }

    @Test
    public void writeToFile_existingTempFileName_keepsThatFile() throws IOException {
        Path file = testFolder.resolve("tutorflow.json");
        Path otherFile = testFolder.resolve("tutorflow.json.tmp");
        Files.writeString(otherFile, OLD_CONTENT);

        FileUtil.writeToFile(file, CONTENT);

        assertEquals(CONTENT, FileUtil.readFromFile(file));
        assertEquals(OLD_CONTENT, FileUtil.readFromFile(otherFile));
        assertEquals(Set.of(file, otherFile), Set.copyOf(listFiles(testFolder)));
    }

    @Test
    public void writeToFile_replaceFails_deletesOnlyTemporaryFile() throws IOException {
        // A folder that holds a file cannot be replaced by the data file
        Path blockingFolder = Files.createDirectory(testFolder.resolve("tutorflow.json"));
        Files.writeString(blockingFolder.resolve("keep.txt"), OLD_CONTENT);
        Path emptyFolder = Files.createDirectory(testFolder.resolve("tutorflow.json.tmp"));

        assertThrows(IOException.class, () -> FileUtil.writeToFile(blockingFolder, CONTENT));

        assertEquals(OLD_CONTENT, FileUtil.readFromFile(blockingFolder.resolve("keep.txt")));
        assertEquals(Set.of(blockingFolder, emptyFolder), Set.copyOf(listFiles(testFolder)));
    }

    @Test
    public void writeToFile_readOnlyFolder_throwsAccessDeniedExceptionForFile() throws IOException {
        assumeTrue(isPosix(), "folder permissions only block writes on POSIX file systems");
        Path folder = Files.createDirectory(testFolder.resolve("data"));
        Path file = folder.resolve("tutorflow.json");
        Files.setPosixFilePermissions(folder, PosixFilePermissions.fromString("r-xr-xr-x"));

        try {
            // The message names the user's file, not the temporary file
            assertThrows(AccessDeniedException.class, file.toString(), () -> FileUtil.writeToFile(file, CONTENT));
            assertFalse(Files.exists(file));
        } finally {
            Files.setPosixFilePermissions(folder, PosixFilePermissions.fromString("rwxr-xr-x"));
        }
    }

    @Test
    public void writeToFile_existingFileWithCustomPermissions_keepsPermissions() throws IOException {
        assumeTrue(isPosix(), "file permissions are POSIX only");
        Path file = testFolder.resolve("tutorflow.json");
        Files.writeString(file, OLD_CONTENT);

        for (String permissions : List.of("rw-------", "rw-rw-r--")) {
            Set<PosixFilePermission> expectedPermissions = PosixFilePermissions.fromString(permissions);
            Files.setPosixFilePermissions(file, expectedPermissions);

            FileUtil.writeToFile(file, CONTENT);

            assertEquals(CONTENT, FileUtil.readFromFile(file));
            assertEquals(expectedPermissions, Files.getPosixFilePermissions(file));
        }
    }

    @Test
    public void writeToFile_newFile_getsPermissionsOfAnyNewFile() throws IOException {
        // PE round 3: a new data file could be read and written only by its owner (600), unlike other new files
        assumeTrue(isPosix(), "file permissions are POSIX only");
        Path file = testFolder.resolve("tutorflow.json");
        Path otherNewFile = Files.createFile(testFolder.resolve("other.json"));

        FileUtil.writeToFile(file, CONTENT);

        assertEquals(Files.getPosixFilePermissions(otherNewFile), Files.getPosixFilePermissions(file));
    }

    @Test
    public void writeToFile_existingFileWithOwnerOnlyAcl_keepsAcl() throws IOException {
        assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("acl"),
                "access control lists are supported on Windows only");
        Path file = testFolder.resolve("tutorflow.json");
        Files.writeString(file, OLD_CONTENT);
        AclEntry ownerOnly = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(Files.getOwner(file))
                .setPermissions(AclEntryPermission.values())
                .build();
        AclFileAttributeView aclView = Files.getFileAttributeView(file, AclFileAttributeView.class);
        aclView.setAcl(List.of(ownerOnly));
        List<AclEntry> expectedAcl = aclView.getAcl();

        FileUtil.writeToFile(file, CONTENT);

        assertEquals(CONTENT, FileUtil.readFromFile(file));
        assertEquals(expectedAcl, Files.getFileAttributeView(file, AclFileAttributeView.class).getAcl());
    }

    private static boolean isPosix() {
        return FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
    }

    private static List<Path> listFiles(Path folder) throws IOException {
        try (Stream<Path> files = Files.list(folder)) {
            return files.toList();
        }
    }
}
