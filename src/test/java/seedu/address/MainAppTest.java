package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalStudents.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
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

    private Storage getStorage(Path dataFilePath) {
        return new StorageManager(new JsonAddressBookStorage(dataFilePath),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
    }
}
