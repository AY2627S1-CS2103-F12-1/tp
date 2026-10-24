package seedu.address;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "V1.5";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Paths.get("preferences.json");
    private static final Path ADDRESS_BOOK_FILE_PATH = Paths.get("data", "tutorflow.json");

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;
    private boolean hasDataLoadingError;

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing AddressBook ]===========================");
        super.init();

        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(USER_PREFS_FILE_PATH);
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(ADDRESS_BOOK_FILE_PATH);
        storage = new StorageManager(addressBookStorage, userPrefsStorage);

        try {
            model = initModelManager(storage, userPrefs);
        } catch (DataLoadingException exception) {
            logger.warning("Cannot load saved data. Startup stopped to preserve the original file: "
                    + StringUtil.getDetails(exception));
            hasDataLoadingError = true;
            return;
        }

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic, storage.getAddressBookFilePath());
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s address book and {@code userPrefs}.
     *
     * @see #readInitialData(Storage)
     */
    private Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) throws DataLoadingException {
        return new ModelManager(readInitialData(storage), userPrefs);
    }

    /**
     * Returns the data in {@code storage}'s data file. <br>
     * The sample students will be used instead if the data file is not found.
     * Loading errors propagate so an empty replacement cannot overwrite an existing invalid file.
     */
    static ReadOnlyAddressBook readInitialData(Storage storage) throws DataLoadingException {
        logger.info("Using data file : " + storage.getAddressBookFilePath());

        Optional<ReadOnlyAddressBook> addressBookOptional;
        ReadOnlyAddressBook initialData;
        addressBookOptional = storage.readAddressBook();
        if (addressBookOptional.isEmpty()) {
            logger.info("Data file " + storage.getAddressBookFilePath() + " not found. Starting with the"
                    + " sample students; the file will be created at the first change to the data.");
        }
        initialData = addressBookOptional.orElseGet(SampleDataUtil::getSampleAddressBook);
        return initialData;
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        if (hasDataLoadingError) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("TutorFlow could not load saved data");
            alert.setHeaderText("Your data file has not been changed.");
            alert.setContentText("TutorFlow could not read valid data from:\n"
                    + ADDRESS_BOOK_FILE_PATH.toAbsolutePath().normalize()
                    + "\n\nClose TutorFlow and restore a valid backup or correct the file. "
                    + "To start with sample data instead, move the original file to a safe location before restarting. "
                    + "Do not delete it if you need to recover your records.");
            alert.showAndWait();
            Platform.exit();
            return;
        }
        logger.info("Starting AddressBook " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping AddressBook ] =============================");
        if (model == null) {
            return;
        }
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
