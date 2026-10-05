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
    public void writeToFile_writeFails_keepsOldContent() throws IOException {
        Path file = testFolder.resolve("tutorflow.json");
        Files.writeString(file, OLD_CONTENT);
        // A folder where the temporary file should be makes writing the new content fail
        Path blockingFolder = Files.createDirectory(testFolder.resolve("tutorflow.json.tmp"));
        Files.writeString(blockingFolder.resolve("keep.txt"), OLD_CONTENT);

        assertThrows(IOException.class, () -> FileUtil.writeToFile(file, CONTENT));

        assertEquals(OLD_CONTENT, FileUtil.readFromFile(file));
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

    private static boolean isPosix() {
        return FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
    }

    private static List<Path> listFiles(Path folder) throws IOException {
        try (Stream<Path> files = Files.list(folder)) {
            return files.toList();
        }
    }
}
