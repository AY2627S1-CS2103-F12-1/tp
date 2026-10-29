package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.UserPrefs;

public class JsonUserPrefsStorageTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonUserPrefsStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readUserPrefs_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readUserPrefs(null));
    }

    private Optional<UserPrefs> readUserPrefs(String userPrefsFileInTestDataFolder) throws DataLoadingException {
        Path prefsFilePath = addToTestDataPathIfNotNull(userPrefsFileInTestDataFolder);
        return new JsonUserPrefsStorage(prefsFilePath).readUserPrefs(prefsFilePath);
    }

    @Test
    public void readUserPrefs_missingFile_emptyResult() throws DataLoadingException {
        assertFalse(readUserPrefs("NonExistentFile.json").isPresent());
    }

    @Test
    public void readUserPrefs_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readUserPrefs("NotJsonFormatUserPrefs.json"));
    }

    @Test
    public void readUserPrefs_duplicateKey_exceptionThrown() throws IOException {
        Path prefsFilePath = testFolder.resolve("DuplicateKeyPrefs.json");
        Files.writeString(prefsFilePath, "{\"guiSettings\" : {\"windowWidth\" : 1000.0, \"windowWidth\" : 800.0}}");

        assertThrows(DataLoadingException.class, () -> new JsonUserPrefsStorage(prefsFilePath).readUserPrefs());
    }

    @Test
    public void readUserPrefs_trailingContent_exceptionThrown() throws IOException {
        Path prefsFilePath = testFolder.resolve("TrailingContentPrefs.json");
        Files.writeString(prefsFilePath, Files.readString(TEST_DATA_FOLDER.resolve("TypicalUserPref.json"))
                + "{\"guiSettings\" : {}}");

        assertThrows(DataLoadingException.class, () -> new JsonUserPrefsStorage(prefsFilePath).readUserPrefs());
    }

    @Test
    public void readUserPrefs_nullGuiSettings_exceptionThrown() throws IOException {
        // Issue #152: an explicit null bypasses the non-null check of UserPrefs#setGuiSettings
        Path prefsFilePath = testFolder.resolve("NullGuiSettingsPrefs.json");
        Files.writeString(prefsFilePath, "{\"guiSettings\":null}");

        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(prefsFilePath);
        String expectedMessage = new IllegalValueException(JsonUserPrefsStorage.MESSAGE_NULL_GUI_SETTINGS).toString();
        assertThrows(DataLoadingException.class, expectedMessage, () -> storage.readUserPrefs());
    }

    @Test
    public void readUserPrefs_nullWindowCoordinates_noCoordinatesUsed() throws Exception {
        // null is how the default preferences save "no window position", so it stays valid
        Path prefsFilePath = testFolder.resolve("NullCoordinatesPrefs.json");
        Files.writeString(prefsFilePath, "{\"guiSettings\":{\"windowWidth\":740.0,\"windowHeight\":600.0,"
                + "\"windowCoordinates\":null}}");

        assertEquals(new UserPrefs(), new JsonUserPrefsStorage(prefsFilePath).readUserPrefs().orElseThrow());
    }

    @Test
    public void readUserPrefs_nullWindowWidth_zeroWidthKeepsHeight() throws Exception {
        // the window's minimum width, not the preferences, keeps the window usable
        Path prefsFilePath = testFolder.resolve("NullWindowWidthPrefs.json");
        Files.writeString(prefsFilePath, "{\"guiSettings\":{\"windowWidth\":null,\"windowHeight\":700.0}}");

        GuiSettings guiSettings = new JsonUserPrefsStorage(prefsFilePath).readUserPrefs().orElseThrow()
                .getGuiSettings();
        assertEquals(0.0, guiSettings.getWindowWidth());
        assertEquals(700.0, guiSettings.getWindowHeight());
        assertNull(guiSettings.getWindowCoordinates());
    }

    @Test
    public void readUserPrefs_nullUnknownField_ignored() throws Exception {
        Path prefsFilePath = testFolder.resolve("NullUnknownFieldPrefs.json");
        Files.writeString(prefsFilePath, "{\"addressBookFilePath\":null}");

        assertEquals(new UserPrefs(), new JsonUserPrefsStorage(prefsFilePath).readUserPrefs().orElseThrow());
    }

    private Path addToTestDataPathIfNotNull(String userPrefsFileInTestDataFolder) {
        return userPrefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(userPrefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void readUserPrefs_fileInOrder_successfullyRead() throws DataLoadingException {
        UserPrefs expected = getTypicalUserPrefs();
        UserPrefs actual = readUserPrefs("TypicalUserPref.json").get();
        assertEquals(expected, actual);
    }

    @Test
    public void readUserPrefs_valuesMissingFromFile_defaultValuesUsed() throws DataLoadingException {
        UserPrefs actual = readUserPrefs("EmptyUserPrefs.json").get();
        assertEquals(new UserPrefs(), actual);
    }

    @Test
    public void readUserPrefs_extraValuesInFile_extraValuesIgnored() throws DataLoadingException {
        UserPrefs expected = getTypicalUserPrefs();
        UserPrefs actual = readUserPrefs("ExtraValuesUserPref.json").get();

        assertEquals(expected, actual);
    }

    private UserPrefs getTypicalUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1000, 500, 300, 100));
        return userPrefs;
    }

    @Test
    public void savePrefs_nullPrefs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveUserPrefs(null, "SomeFile.json"));
    }

    @Test
    public void saveUserPrefs_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveUserPrefs(new UserPrefs(), null));
    }

    /**
     * Saves {@code userPrefs} at the specified {@code prefsFileInTestDataFolder} filepath.
     */
    private void saveUserPrefs(UserPrefs userPrefs, String prefsFileInTestDataFolder) {
        try {
            new JsonUserPrefsStorage(addToTestDataPathIfNotNull(prefsFileInTestDataFolder))
                    .saveUserPrefs(userPrefs);
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file", ioe);
        }
    }

    @Test
    public void saveUserPrefs_allInOrder_success() throws DataLoadingException, IOException {

        UserPrefs original = new UserPrefs();
        original.setGuiSettings(new GuiSettings(1200, 200, 0, 2));

        Path pefsFilePath = testFolder.resolve("TempPrefs.json");
        JsonUserPrefsStorage jsonUserPrefsStorage = new JsonUserPrefsStorage(pefsFilePath);

        //Try writing when the file doesn't exist
        jsonUserPrefsStorage.saveUserPrefs(original);
        UserPrefs readBack = jsonUserPrefsStorage.readUserPrefs().get();
        assertEquals(original, readBack);

        //Try saving when the file exists
        original.setGuiSettings(new GuiSettings(5, 5, 5, 5));
        jsonUserPrefsStorage.saveUserPrefs(original);
        readBack = jsonUserPrefsStorage.readUserPrefs().get();
        assertEquals(original, readBack);
    }

}
