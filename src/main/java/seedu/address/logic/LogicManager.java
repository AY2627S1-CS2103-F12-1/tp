package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.student.Student;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
    }

    /**
     * {@inheritDoc}
     * The data file is written only if the command changed the data. If writing fails, the change is undone, so the
     * model again matches the data file, and the command's save failure message is reported.
     */
    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        Command command = addressBookParser.parseCommand(commandText);
        ReadOnlyAddressBook dataBeforeCommand = new AddressBook(model.getAddressBook());
        CommandResult commandResult = command.execute(model);

        // Read-only commands such as homework list never write the data file
        if (model.getAddressBook().equals(dataBeforeCommand)) {
            return commandResult;
        }

        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (IOException ioe) {
            logger.warning("Could not save data, so the command's changes are undone: " + ioe);
            model.setAddressBook(dataBeforeCommand);
            throw new CommandException(command.getSaveFailureMessage(getSaveErrorMessage(ioe)), ioe);
        }

        return commandResult;
    }

    /**
     * Returns the message describing why the data could not be saved.
     */
    private static String getSaveErrorMessage(IOException exception) {
        if (exception instanceof AccessDeniedException) {
            return String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, exception.getMessage());
        }
        return String.format(FILE_OPS_ERROR_FORMAT, exception.getMessage());
    }

    @Override
    public ObservableList<Student> getStudentList() {
        return model.getStudentList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
