package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.model.homework.DueDateTest.MESSAGE_YEAR_OUT_OF_RANGE;
import static seedu.address.testutil.TypicalStudents.getTypicalAddressBook;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;

public class MainAppTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void readInitialData_missingDataFile_returnsSampleStudents() throws Exception {
        Storage storage = getStorage(temporaryFolder.resolve("tutorflow.json"));

        assertEquals(SampleDataUtil.getSampleAddressBook(), new AddressBook(MainApp.readInitialData(storage)));
    }

    @Test
    public void readInitialData_validDataFile_returnsSavedStudents() throws Exception {
        Path dataFilePath = temporaryFolder.resolve("tutorflow.json");
        Storage storage = getStorage(dataFilePath);
        storage.saveAddressBook(getTypicalAddressBook());

        assertEquals(getTypicalAddressBook(), new AddressBook(MainApp.readInitialData(storage)));
    }

    @Test
    public void readInitialData_savedEmptyAddressBook_returnsNoStudents() throws Exception {
        // A saved file with no students is used as is, without falling back to the sample students
        Storage storage = getStorage(temporaryFolder.resolve("tutorflow.json"));
        storage.saveAddressBook(new AddressBook());

        assertEquals(new AddressBook(), new AddressBook(MainApp.readInitialData(storage)));
    }

    @Test
    public void readInitialData_invalidDataFile_rejectsWithoutChangingFile() throws Exception {
        Path dataFilePath = temporaryFolder.resolve("tutorflow.json");
        Files.writeString(dataFilePath, "not json format!");

        assertThrows(DataLoadingException.class, () -> MainApp.readInitialData(getStorage(dataFilePath)));
        assertEquals("not json format!", Files.readString(dataFilePath));
    }

    @Test
    public void readInitialData_nullDataFile_rejectsWithoutChangingFile() throws Exception {
        Path dataFilePath = temporaryFolder.resolve("tutorflow.json");
        Files.writeString(dataFilePath, "null");

        assertThrows(DataLoadingException.class, () -> MainApp.readInitialData(getStorage(dataFilePath)));
        assertEquals("null", Files.readString(dataFilePath));
    }

    @Test
    public void readInitialData_invalidStudent_rejectsWithoutChangingFile() throws Exception {
        Path dataFilePath = temporaryFolder.resolve("tutorflow.json");
        Storage storage = getStorage(dataFilePath);
        storage.saveAddressBook(getTypicalAddressBook());
        String invalidData = Files.readString(dataFilePath)
                .replace("\"guardianPhone\"", "\"unknownGuardianPhone\"");
        Files.writeString(dataFilePath, invalidData);

        assertThrows(DataLoadingException.class, () -> MainApp.readInitialData(storage));
        assertEquals(invalidData, Files.readString(dataFilePath));
    }

    @Test
    public void readInitialData_dueDateYearOutOfRange_rejectsWithoutChangingFile() throws Exception {
        Path dataFilePath = temporaryFolder.resolve("tutorflow.json");
        Storage storage = getStorage(dataFilePath);
        storage.saveAddressBook(getTypicalAddressBook());
        // a typo year, which is a real calendar date
        String invalidData = Files.readString(dataFilePath).replace("\"2026-10-15\"", "\"2206-10-30\"");
        Files.writeString(dataFilePath, invalidData);

        DataLoadingException exception = assertThrows(DataLoadingException.class, () ->
                MainApp.readInitialData(storage));
        assertEquals("Student 1 (Alice Tan): Homework 1 (Complete algebra worksheet): " + MESSAGE_YEAR_OUT_OF_RANGE,
                exception.getCause().getMessage());
        assertEquals(invalidData, Files.readString(dataFilePath));
    }

    @Test
    public void readInitialData_duplicateKey_rejectsWithoutChangingFile() throws Exception {
        Path dataFilePath = temporaryFolder.resolve("tutorflow.json");
        Storage storage = getStorage(dataFilePath);
        storage.saveAddressBook(getTypicalAddressBook());
        // A second "students" key would replace the saved roster, and the next save would delete those students
        String savedData = Files.readString(dataFilePath).stripTrailing();
        String invalidData = savedData.substring(0, savedData.length() - 1) + ", \"students\" : [ ] }";
        Files.writeString(dataFilePath, invalidData);

        assertThrows(DataLoadingException.class, () -> MainApp.readInitialData(storage));
        assertEquals(invalidData, Files.readString(dataFilePath));
    }

    @Test
    public void readInitialData_duplicateNestedKey_rejectsWithoutChangingFile() throws Exception {
        Path dataFilePath = temporaryFolder.resolve("tutorflow.json");
        Storage storage = getStorage(dataFilePath);
        storage.saveAddressBook(getTypicalAddressBook());
        // A student with two "phone" keys would keep only the last one
        String invalidData = Files.readString(dataFilePath)
                .replaceFirst("\"phone\" : (\"[0-9]+\")", "\"phone\" : $1, \"phone\" : \"99999999\"");
        Files.writeString(dataFilePath, invalidData);

        assertThrows(DataLoadingException.class, () -> MainApp.readInitialData(storage));
        assertEquals(invalidData, Files.readString(dataFilePath));
    }

    @Test
    public void readInitialData_trailingJsonObject_rejectsWithoutChangingFile() throws Exception {
        Path dataFilePath = temporaryFolder.resolve("tutorflow.json");
        Storage storage = getStorage(dataFilePath);
        storage.saveAddressBook(getTypicalAddressBook());
        // A second root object after the saved one, as in Issue #149
        String invalidData = Files.readString(dataFilePath) + "{\"students\":[]}";
        Files.writeString(dataFilePath, invalidData);

        assertThrows(DataLoadingException.class, () -> MainApp.readInitialData(storage));
        assertEquals(invalidData, Files.readString(dataFilePath));
    }

    @Test
    public void initPrefs_nullGuiSettings_usesDefaultPrefs() throws Exception {
        Path prefsFilePath = temporaryFolder.resolve("preferences.json");
        Path dataFilePath = temporaryFolder.resolve("tutorflow.json");
        Files.writeString(prefsFilePath, "{\"guiSettings\":null}");
        MainApp app = new MainApp(temporaryFolder, prefsFilePath, dataFilePath, () -> { });

        UserPrefs prefs = app.initPrefs(new JsonUserPrefsStorage(prefsFilePath));

        assertEquals(new UserPrefs(), prefs);
        assertEquals(new UserPrefs(), new ModelManager(new AddressBook(), prefs).getUserPrefs());
        assertEquals(new UserPrefs(), new JsonUserPrefsStorage(prefsFilePath).readUserPrefs().orElseThrow());
        assertFalse(Files.exists(dataFilePath));
    }

    @Test
    public void readInitialData_invalidUtf8_rejectsWithoutChangingFile() throws Exception {
        Path dataFilePath = temporaryFolder.resolve("tutorflow.json");
        Storage storage = getStorage(dataFilePath);
        storage.saveAddressBook(getTypicalAddressBook());
        // Issue #156: a title saved by an editor in Windows-1252, so é is the single byte E9
        byte[] invalidData = Files.readString(dataFilePath).replace("Complete algebra", "Compl\u00e9te algebra")
                .getBytes(StandardCharsets.ISO_8859_1);
        Files.write(dataFilePath, invalidData);

        assertThrows(DataLoadingException.class, () -> MainApp.readInitialData(storage));
        assertArrayEquals(invalidData, Files.readAllBytes(dataFilePath));
    }

    @Test
    public void initPrefs_invalidUtf8_usesDefaultPrefs() throws Exception {
        Path prefsFilePath = temporaryFolder.resolve("preferences.json");
        // valid GUI settings, but the file also holds the Windows-1252 byte E9, which is not UTF-8
        String prefs = "{\"guiSettings\":{\"windowWidth\":1000.0,\"windowHeight\":500.0},\"note\":\"\u00e9\"}";
        Files.write(prefsFilePath, prefs.getBytes(StandardCharsets.ISO_8859_1));
        MainApp app = new MainApp(temporaryFolder, prefsFilePath, temporaryFolder.resolve("tutorflow.json"), () -> { });

        assertEquals(new UserPrefs(), app.initPrefs(new JsonUserPrefsStorage(prefsFilePath)));
    }

    private Storage getStorage(Path dataFilePath) {
        return new StorageManager(new JsonAddressBookStorage(dataFilePath),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
    }
}
