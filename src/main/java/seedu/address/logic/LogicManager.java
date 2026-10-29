package seedu.address.logic;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyObjectProperty;
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
    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;
    private final Path homeFolder;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this(model, storage, Clock.systemDefaultZone());
    }

    /**
     * Constructs a {@code LogicManager} whose {@code homework} commands take today's date from {@code clock}.
     * Save-failure messages name the data file relative to the current working folder.
     */
    public LogicManager(Model model, Storage storage, Clock clock) {
        this(model, storage, clock, Path.of(""));
    }

    /**
     * Constructs a {@code LogicManager} whose {@code homework} commands take today's date from {@code clock} and
     * whose save-failure messages name the data file relative to {@code homeFolder}, if it is inside it.
     */
    public LogicManager(Model model, Storage storage, Clock clock, Path homeFolder) {
        requireAllNonNull(model, storage, clock, homeFolder);
        this.model = model;
        this.storage = storage;
        this.homeFolder = homeFolder.toAbsolutePath().normalize();
        addressBookParser = new AddressBookParser(clock);
    }

    /**
     * {@inheritDoc}
     * The data file is written only if the command changed the data. If writing fails, the change is undone, so the
     * model again matches the data file and the displayed students and their order are unchanged; the command's save
     * failure message, with the data file's name and a plain cause, is reported.
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
            Path dataFile = storage.getAddressBookFilePath().toAbsolutePath().normalize();
            String message = ioe instanceof DataFileConflictException conflict
                    ? conflict.getMessage(getShownName(dataFile))
                    : command.getSaveFailureMessage(getShownName(dataFile), describeSaveFailure(ioe, dataFile));
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
     * Returns the name of {@code path} as the user knows it: relative to the home folder, e.g.
     * {@code data/tutorflow.json} with the system's separator, or the absolute path if it is outside the home folder.
     */
    private String getShownName(Path path) {
        return path.startsWith(homeFolder) ? homeFolder.relativize(path).toString() : path.toString();
    }

    /**
     * Returns a plain description of why {@code dataFile} could not be saved, checking the common causes in this
     * order: the data file is a folder, the data file is read-only, a file is where its folder should be, or the
     * nearest existing folder above it is read-only. Otherwise, the system's text of {@code exception} is returned.
     * The causes are read from the file system, as the exception may be about a different file, such as the
     * internal lock file next to the data file.
     */
    private String describeSaveFailure(IOException exception, Path dataFile) {
        if (Files.isDirectory(dataFile)) {
            return Messages.MESSAGE_SAVE_CAUSE_FOLDER;
        }
        if (Files.exists(dataFile) && !Files.isWritable(dataFile)) {
            return Messages.MESSAGE_SAVE_CAUSE_READ_ONLY_FILE;
        }
        Path existingAncestor = findNearestExistingAncestor(dataFile);
        if (existingAncestor != null && !Files.isDirectory(existingAncestor)) {
            return String.format(Messages.MESSAGE_SAVE_CAUSE_BLOCKED_FOLDER, existingAncestor.getFileName());
        }
        if (existingAncestor != null && !Files.isWritable(existingAncestor)) {
            return describeReadOnlyFolder(existingAncestor, dataFile);
        }
        return getSystemText(exception);
    }

    /**
     * Returns the cause for a read-only {@code folder} that stops {@code dataFile} from being saved, naming the
     * folder as the user knows it: the data folder, the home folder, or any other folder by its shown name.
     */
    private String describeReadOnlyFolder(Path folder, Path dataFile) {
        if (folder.equals(dataFile.getParent())) {
            return Messages.MESSAGE_SAVE_CAUSE_READ_ONLY_FOLDER;
        }
        if (folder.equals(homeFolder)) {
            return Messages.MESSAGE_SAVE_CAUSE_READ_ONLY_HOME_FOLDER;
        }
        return String.format(Messages.MESSAGE_SAVE_CAUSE_READ_ONLY_OTHER_FOLDER, getShownName(folder));
    }

    /**
     * Returns the nearest folder above {@code path}, or file in its place, that exists, or null if none does.
     */
    private static Path findNearestExistingAncestor(Path path) {
        Path ancestor = path.getParent();
        while (ancestor != null && !Files.exists(ancestor)) {
            ancestor = ancestor.getParent();
        }
        return ancestor;
    }

    /**
     * Returns the system's text of {@code exception} without a final period, as the message adds its own, or the
     * exception's class name if it has no text.
     */
    private static String getSystemText(IOException exception) {
        String text = exception.getMessage() == null ? "" : exception.getMessage().strip();
        if (text.isEmpty()) {
            return exception.getClass().getSimpleName();
        }
        return text.endsWith(".") ? text.substring(0, text.length() - 1) : text;
    }

    @Override
    public ObservableList<Student> getStudentList() {
        return model.getStudentList();
    }

    @Override
    public ReadOnlyObjectProperty<Predicate<Student>> studentListPredicateProperty() {
        return model.studentListPredicateProperty();
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
