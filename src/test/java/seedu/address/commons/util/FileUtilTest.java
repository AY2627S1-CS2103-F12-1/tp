package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
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
    public void writeToFile_newFile_onlyOwnerCanReadAndWrite() throws IOException {
        assumeTrue(isPosix(), "file permissions are POSIX only");
        Path file = testFolder.resolve("tutorflow.json");

        FileUtil.writeToFile(file, CONTENT);

        assertEquals(PosixFilePermissions.fromString("rw-------"), Files.getPosixFilePermissions(file));
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
