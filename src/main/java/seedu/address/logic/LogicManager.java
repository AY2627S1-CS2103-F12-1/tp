package seedu.address.logic;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.Clock;
import java.util.List;
import java.util.function.Predicate;
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
import seedu.address.storage.DataFileConflictException;
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
        this(model, storage, Clock.systemDefaultZone());
    }

    /**
     * Constructs a {@code LogicManager} whose {@code homework} commands take today's date from {@code clock}.
     */
    public LogicManager(Model model, Storage storage, Clock clock) {
        requireAllNonNull(model, storage, clock);
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser(clock);
    }

    /**
     * {@inheritDoc}
     * The data file is written only if the command changed the data. If writing fails, the change is undone, so the
     * model again matches the data file and the displayed students and their order are unchanged; the command's save
     * failure message is reported.
     */
    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        Command command = addressBookParser.parseCommand(commandText);
        ReadOnlyAddressBook dataBeforeCommand = new AddressBook(model.getAddressBook());
        Predicate<Student> predicateBeforeCommand = model.getFilteredStudentListPredicate();
        List<Student> displayedBeforeCommand = List.copyOf(model.getStudentList());
        CommandResult commandResult = command.execute(model);

        // Read-only commands such as homework list never write the data file
        if (model.getAddressBook().equals(dataBeforeCommand)) {
            return commandResult;
        }

        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (IOException ioe) {
            logger.warning("Could not save data, so the command's changes are undone: " + ioe);
            restoreModel(dataBeforeCommand, predicateBeforeCommand, displayedBeforeCommand);
            String message = ioe instanceof DataFileConflictException ? ioe.getMessage()
                    : command.getSaveFailureMessage(getSaveErrorMessage(ioe));
            throw new CommandException(message, ioe);
        }

        return commandResult;
    }

    /**
     * Restores the data, the displayed students and their order to what they were before a command.
     * Restoring the data alone can change which students are displayed and their order.
     *
     * @param data Data before the command.
     * @param predicate Predicate that selected the displayed students before the command.
     * @param displayedStudents Displayed students before the command, in their displayed order.
     */
    private void restoreModel(ReadOnlyAddressBook data, Predicate<Student> predicate,
            List<Student> displayedStudents) {
        model.setAddressBook(data);
        model.updateFilteredStudentList(predicate);
        model.setStudentListOrder(displayedStudents);
        assert model.getAddressBook().equals(data) : "Rollback must restore the data";
        assert model.getStudentList().equals(displayedStudents) : "Rollback must restore the displayed students";
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
    public ObservableList<Student> getAllStudentList() {
        return model.getAddressBook().getStudentList();
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
