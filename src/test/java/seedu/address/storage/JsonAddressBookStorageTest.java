package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestUtil.ARABIC_EGYPT;
import static seedu.address.testutil.TestUtil.createSymbolicLinkOrSkip;
import static seedu.address.testutil.TestUtil.runWithFormatLocale;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalRegularLessons.MONDAY_MATH;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.BENSON;
import static seedu.address.testutil.TypicalStudents.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.testutil.StudentBuilder;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_utf8Bom_preservesRecordsAndOriginalBytes() throws Exception {
        Path file = testFolder.resolve("bom.json");
        AddressBook original = getTypicalAddressBook();
        original.setStudent(ALICE, ALICE.withLessons(List.of(MONDAY_MATH)));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(original);
        Files.writeString(file, "\ufeff" + Files.readString(file));
        byte[] saved = Files.readAllBytes(file);

        assertEquals(original, new AddressBook(storage.readAddressBook().orElseThrow()));
        assertArrayEquals(saved, Files.readAllBytes(file));
    }

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    /**
     * Returns the reason the user is shown when a data file holding {@code content} cannot be loaded: the message
     * of the {@code DataLoadingException}'s cause.
     */
    private String getLoadingReason(String content) throws IOException {
        Path file = Files.writeString(testFolder.resolve("data.json"), content);
        try {
            new JsonAddressBookStorage(file).readAddressBook();
        } catch (DataLoadingException e) {
            return e.getCause().getMessage();
        }
        throw new AssertionError("The data file was loaded: " + content);
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_emptyFile_reasonSaysEmpty() throws Exception {
        assertEquals("The file is empty.", getLoadingReason(""));
    }

    @Test
    public void readAddressBook_whitespaceOnlyFile_reasonSaysEmpty() throws Exception {
        assertEquals("The file is empty.", getLoadingReason(" \n\t\r\n"));
    }

    @Test
    public void readAddressBook_doubleCommaInStudent_reasonPointsAtSecondComma() throws Exception {
        // Jackson finds this while mapping the student, and its own position is the start of the value before
        assertEquals("The file is not valid JSON near line 2, column 18.",
                getLoadingReason("{\"students\":[{\n    \"name\" : \"A\",, }]}"));
    }

    @Test
    public void readAddressBook_textAfterStudentList_reasonPointsAtText() throws Exception {
        assertEquals("The file is not valid JSON near line 1, column 16.",
                getLoadingReason("{\"students\":[] x }"));
    }

    @Test
    public void readAddressBook_sampleDataMissingClosingBrace_reasonPointsAtLastCharacter() throws Exception {
        // the sample data without its final }, as in the developer guide's manual test
        String content = Files.readString(TEST_DATA_FOLDER.resolve("missingClosingBraceAddressBook.json"));
        assertEquals("The file is not valid JSON near line 120, column 5.", getLoadingReason(content));
    }

    @Test
    public void readAddressBook_studentsNotList_reasonSaysWrongType() throws Exception {
        assertEquals("The file has a value of the wrong type near line 1, column 14.",
                getLoadingReason("{\"students\": 5}"));
    }

    @Test
    public void readAddressBook_nameNotText_reasonSaysWrongType() throws Exception {
        assertEquals("The file has a value of the wrong type near line 1, column 22.",
                getLoadingReason("{\"students\":[{\"name\":[\"a\"]}]}"));
    }

    @Test
    public void readAddressBook_repeatedPhoneKeyInSampleData_reasonNamesKeyAndPosition() throws Exception {
        // the second student's "phone" key repeated on its line, as found in PE testing
        String content = Files.readString(TEST_DATA_FOLDER.resolve("duplicatePhoneKeyAddressBook.json"));
        assertEquals("The key \"phone\" is repeated near line 32, column 27.", getLoadingReason(content));
    }

    @Test
    public void readAddressBook_repeatedStudentsKeyInSampleData_reasonNamesKeyAndPosition() throws Exception {
        // a second, empty student list after the sample data, as in the developer guide's manual test
        String content = Files.readString(TEST_DATA_FOLDER.resolve("duplicateStudentsKeyAddressBook.json"));
        assertEquals("The key \"students\" is repeated near line 121, column 3.", getLoadingReason(content));
    }

    @Test
    public void readAddressBook_objectAfterSampleData_reasonPointsAtObject() throws Exception {
        // a second root object on the line after the sample data, as found in PE testing
        String content = Files.readString(TEST_DATA_FOLDER.resolve("missingClosingBraceAddressBook.json"))
                .stripTrailing() + "\n}\n{\"students\":[]}";
        assertEquals("The file has unexpected content after the JSON data near line 122, column 1.",
                getLoadingReason(content));
    }

    @Test
    public void readAddressBook_arabicDigitsLocale_reasonGivesPositionInAsciiDigits() {
        runWithFormatLocale(ARABIC_EGYPT, () -> {
            assertEquals("The file is not valid JSON near line 2, column 18.", assertDoesNotThrow(() ->
                    getLoadingReason("{\"students\":[{\n    \"name\" : \"A\",, }]}")));
            assertEquals("The file has a value of the wrong type near line 1, column 14.", assertDoesNotThrow(() ->
                    getLoadingReason("{\"students\": 5}")));
        });
    }

    @Test
    public void readAddressBook_dataFileIsFolder_reasonSaysFolder() throws Exception {
        Path file = Files.createDirectories(testFolder.resolve("data").resolve("tutorflow.json"));
        assertEquals("It is a folder, not a file.", getLoadingReasonOfFile(file));
    }

    @Test
    public void readAddressBook_unreadableDataFile_reasonSaysNoPermission() throws Exception {
        Path file = testFolder.resolve("data").resolve("tutorflow.json");
        new JsonAddressBookStorage(file).saveAddressBook(getTypicalAddressBook());
        try {
            removePermissions(file);
            assertEquals("The file cannot be read (no permission).", getLoadingReasonOfFile(file));
        } finally {
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"));
        }
    }

    @Test
    public void readAddressBook_unreadableDataFolder_reasonSaysNoPermission() throws Exception {
        Path file = testFolder.resolve("data").resolve("tutorflow.json");
        new JsonAddressBookStorage(file).saveAddressBook(getTypicalAddressBook());
        try {
            removePermissions(file.getParent());
            assertEquals("The data folder cannot be read (no permission).", getLoadingReasonOfFile(file));
        } finally {
            Files.setPosixFilePermissions(file.getParent(), PosixFilePermissions.fromString("rwx------"));
        }
    }

    /**
     * Returns the reason the user is shown when the data file {@code file} cannot be loaded.
     */
    private static String getLoadingReasonOfFile(Path file) {
        try {
            new JsonAddressBookStorage(file).readAddressBook();
        } catch (DataLoadingException e) {
            return e.getCause().getMessage();
        }
        throw new AssertionError("The data file was loaded: " + file);
    }

    /**
     * Removes all permissions from {@code path}, skipping the test where that does not stop reading it: on file
     * systems without POSIX permissions, or when running as root.
     */
    private static void removePermissions(Path path) throws IOException {
        assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"),
                "permissions only block reading on POSIX file systems");
        Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("---------"));
        assumeFalse(Files.isReadable(path), "the file is still readable, e.g. when running as root");
    }

    @Test
    public void readAddressBook_invalidStudentAddressBook_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidStudentAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidStudentAddressBook_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidStudentAddressBook.json"));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.setStudent(BENSON, BENSON.withHomeworks(List.of(ALGEBRA)));
        original.setStudents(List.of(original.getStudentList().get(1), original.getStudentList().get(2)));
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addStudent(new StudentBuilder().withName("Ida Mueller").withPhone("98482131").build());
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));
    }

    @Test
    public void saveAddressBook_fileDeletedAfterLoad_recreatesFile() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        new JsonAddressBookStorage(filePath).saveAddressBook(getTypicalAddressBook());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        AddressBook data = new AddressBook(storage.readAddressBook().orElseThrow());
        Files.delete(filePath);

        data.deleteStudent(ALICE);
        storage.saveAddressBook(data);

        assertEquals(data, new AddressBook(new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow()));
    }

    @Test
    public void saveAddressBook_fileDeletedAfterSave_recreatesFile() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        AddressBook data = getTypicalAddressBook();
        storage.saveAddressBook(data);
        Files.delete(filePath);

        data.deleteStudent(ALICE);
        storage.saveAddressBook(data);

        assertEquals(data, new AddressBook(new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow()));
    }

    @Test
    public void saveAddressBook_fileCreatedByAnotherCopyAfterMissingAtLoad_throwsCreatedConflict() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        assertTrue(storage.readAddressBook().isEmpty());
        new JsonAddressBookStorage(filePath).saveAddressBook(getTypicalAddressBook());
        byte[] fileBefore = Files.readAllBytes(filePath);

        assertThrows(DataFileConflictException.class, DataFileConflictException.MESSAGE_CREATED, () ->
                storage.saveAddressBook(new AddressBook()));
        assertArrayEquals(fileBefore, Files.readAllBytes(filePath));
    }

    @Test
    public void saveAddressBook_fileChangedByAnotherCopyAfterLoad_throwsChangedConflict() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        new JsonAddressBookStorage(filePath).saveAddressBook(getTypicalAddressBook());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.readAddressBook();
        new JsonAddressBookStorage(filePath).saveAddressBook(new AddressBook());
        byte[] fileBefore = Files.readAllBytes(filePath);

        assertThrows(DataFileConflictException.class, DataFileConflictException.MESSAGE_CHANGED, () ->
                storage.saveAddressBook(getTypicalAddressBook()));
        assertArrayEquals(fileBefore, Files.readAllBytes(filePath));
    }

    @Test
    public void saveAddressBook_dataFileIsSymbolicLink_savesThroughLink() throws Exception {
        // PE round 2: data/tutorflow.json as a link to a file elsewhere, e.g. in a synced folder
        Path target = Files.createDirectories(testFolder.resolve("synced")).resolve("tutorflow.json");
        new JsonAddressBookStorage(target).saveAddressBook(getTypicalAddressBook());
        Path link = Files.createDirectories(testFolder.resolve("data")).resolve("tutorflow.json");
        createSymbolicLinkOrSkip(link, target);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(link);
        assertEquals(getTypicalAddressBook(), storage.readAddressBook().orElseThrow());

        // saved twice, so the second save checks the data the first one wrote through the link
        AddressBook withoutBenson = new AddressBook(getTypicalAddressBook());
        withoutBenson.deleteStudent(BENSON);
        storage.saveAddressBook(withoutBenson);
        storage.saveAddressBook(new AddressBook());

        assertTrue(Files.isSymbolicLink(link));
        assertEquals(new AddressBook(), new JsonAddressBookStorage(target).readAddressBook().orElseThrow());

        // a change to the file the link points to is still found
        new JsonAddressBookStorage(target).saveAddressBook(getTypicalAddressBook());
        assertThrows(DataFileConflictException.class, () -> storage.saveAddressBook(new AddressBook()));
        assertEquals(getTypicalAddressBook(), new JsonAddressBookStorage(target).readAddressBook().orElseThrow());
    }

    @Test
    public void saveAddressBook_newFile_getsPermissionsOfLockFile() throws Exception {
        // PE round 3: the first save made data/tutorflow.json 600, while the lock file next to it got 644
        assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"),
                "file permissions are POSIX only");
        Path file = testFolder.resolve("data").resolve("tutorflow.json");

        new JsonAddressBookStorage(file).saveAddressBook(getTypicalAddressBook());

        assertEquals(Files.getPosixFilePermissions(file.resolveSibling("tutorflow.json.lock")),
                Files.getPosixFilePermissions(file));
    }

    @Test
    public void saveAddressBook_typicalAddressBook_writesHumanEditableJson() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        new JsonAddressBookStorage(filePath).saveAddressBook(getTypicalAddressBook());

        String savedJson = Files.readString(filePath);
        assertTrue(savedJson.contains("\"name\" : \"" + ALICE.getName() + "\""));
        assertTrue(savedJson.contains("\"subjects\" : [ \"MATH\", \"PHYSICS\", \"CHEMISTRY\" ]"));
        assertTrue(savedJson.contains("\"dueDate\" : \"2026-10-15\""));
        assertTrue(savedJson.contains("\"score\" : \"17/20\""));
    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
