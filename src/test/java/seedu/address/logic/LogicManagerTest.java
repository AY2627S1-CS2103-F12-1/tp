package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.logic.Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalStudents.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.HomeworkAddCommand;
import seedu.address.logic.commands.HomeworkDeleteCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");
    private static final String ADD_STUDENT_COMMAND = "add n/John Tan l/S3 s/MATH p/91234567 gn/Mary Tan gp/98765432";

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;
    private Path dataFilePath;
    private JsonAddressBookStorage addressBookStorage;

    @BeforeEach
    public void setUp() {
        dataFilePath = temporaryFolder.resolve("tutorflow.json");
        addressBookStorage = new JsonAddressBookStorage(dataFilePath);
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String homeworkListCommand = "hw ls 9";
        assertCommandException(homeworkListCommand, MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String clearCommand = ClearCommand.COMMAND_WORD;
        assertCommandSuccess(clearCommand, ClearCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_addStudent_savesStudent() throws Exception {
        logic.execute(ADD_STUDENT_COMMAND);

        assertEquals(1, logic.getStudentList().size());
        assertEquals(model.getAddressBook(), new AddressBook(addressBookStorage.readAddressBook().get()));
    }

    @Test
    public void execute_homeworkAddAndDelete_savesHomework() throws Exception {
        logic.execute(ADD_STUDENT_COMMAND);

        logic.execute("hw add 1 t/Complete algebra worksheet s/MATH due/2026-10-15");
        ReadOnlyAddressBook savedData = addressBookStorage.readAddressBook().get();
        assertEquals(1, savedData.getStudentList().get(0).getHomeworks().size());
        assertEquals(model.getAddressBook(), new AddressBook(savedData));

        logic.execute("hw del 1 1");
        savedData = addressBookStorage.readAddressBook().get();
        assertEquals(0, savedData.getStudentList().get(0).getHomeworks().size());
        assertEquals(model.getAddressBook(), new AddressBook(savedData));
    }

    @Test
    public void execute_clear_savesNoStudents() throws Exception {
        setUpLogic(getTypicalAddressBook(), addressBookStorage);

        logic.execute(ClearCommand.COMMAND_WORD);

        assertEquals(new AddressBook(), new AddressBook(addressBookStorage.readAddressBook().get()));
    }

    @Test
    public void execute_commandsNotChangingData_doNotSave() throws Exception {
        // Saving fails, so a command that saved would throw instead of succeeding
        setUpLogic(getTypicalAddressBook(), getStorageThatFailsToSave(DUMMY_IO_EXCEPTION));

        logic.execute("hw ls 1");
        logic.execute("help");

        assertEquals(new ModelManager(getTypicalAddressBook(), new UserPrefs()), model);
        assertFalse(Files.exists(dataFilePath));
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_clearSaveFails_restoresStudents() {
        setUpLogic(getTypicalAddressBook(), getStorageThatFailsToSave(DUMMY_IO_EXCEPTION));
        String expectedMessage = String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage());

        assertCommandFailure(ClearCommand.COMMAND_WORD, CommandException.class, expectedMessage,
                new ModelManager(getTypicalAddressBook(), new UserPrefs()));
    }

    @Test
    public void execute_homeworkAddSaveFails_rollsBackAddition() {
        setUpLogic(getTypicalAddressBook(), getStorageThatFailsToSave(DUMMY_IO_EXCEPTION));

        assertCommandFailure("hw add 2 t/Trigonometry practice s/MATH due/2026-11-01", CommandException.class,
                HomeworkAddCommand.MESSAGE_SAVE_FAILURE, new ModelManager(getTypicalAddressBook(), new UserPrefs()));
    }

    @Test
    public void execute_homeworkDeleteSaveFails_rollsBackDeletion() {
        setUpLogic(getTypicalAddressBook(), getStorageThatFailsToSave(DUMMY_AD_EXCEPTION));

        assertCommandFailure("hw del 1 2", CommandException.class, HomeworkDeleteCommand.MESSAGE_SAVE_FAILURE,
                new ModelManager(getTypicalAddressBook(), new UserPrefs()));
    }

    @Test
    public void getStudentList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getStudentList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        setUpLogic(new AddressBook(), getStorageThatFailsToSave(e));

        // Adding a student changes the data, so it is saved; the failed save undoes the addition
        ModelManager expectedModel = new ModelManager();
        assertCommandFailure(ADD_STUDENT_COMMAND, CommandException.class, expectedMessage, expectedModel);
    }

    /**
     * Replaces {@code model} with one holding {@code data}, and {@code logic} with one using {@code storage}.
     */
    private void setUpLogic(ReadOnlyAddressBook data, JsonAddressBookStorage storage) {
        model = new ModelManager(data, new UserPrefs());
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        logic = new LogicManager(model, new StorageManager(storage, userPrefsStorage));
    }

    /**
     * Returns a storage for the data file that throws {@code e} whenever it saves.
     */
    private JsonAddressBookStorage getStorageThatFailsToSave(IOException e) {
        return new JsonAddressBookStorage(dataFilePath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };
    }
}
