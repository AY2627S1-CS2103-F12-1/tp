package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalHomeworks.MECHANICS;
import static seedu.address.testutil.TypicalRegularLessons.MONDAY_MATH;
import static seedu.address.testutil.TypicalRegularLessons.TUESDAY_PHYSICS;
import static seedu.address.testutil.TypicalStudents.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.HomeworkAddCommand;
import seedu.address.logic.commands.HomeworkDeleteCommand;
import seedu.address.logic.commands.HomeworkEditCommand;
import seedu.address.logic.commands.HomeworkMarksCommand;
import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.homework.Score;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.StudentBuilder;

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
    public void execute_lessonAdd_savesAndPreservesHomeworkAcrossReload() throws Exception {
        logic.execute(ADD_STUDENT_COMMAND);
        logic.execute("hw add 1 t/Algebra s/MATH due/2026-10-15");
        logic.execute("lesson add 1 s/MATH d/MONDAY st/16:00 et/18:00");
        ReadOnlyAddressBook saved = addressBookStorage.readAddressBook().get();
        assertEquals(model.getAddressBook(), new AddressBook(saved));
        assertEquals(1, saved.getStudentList().get(0).getLessons().size());
        logic.execute("hw del 1 1");
        assertEquals(1, addressBookStorage.readAddressBook().get().getStudentList().get(0).getLessons().size());
    }

    @Test
    public void execute_lessonSaveFails_rollsBackAndPreservesFile() throws Exception {
        logic.execute(ADD_STUDENT_COMMAND);
        String beforeFile = Files.readString(dataFilePath);
        AddressBook before = new AddressBook(model.getAddressBook());
        setUpLogic(before, getStorageThatFailsToSave(DUMMY_IO_EXCEPTION));
        assertCommandException("lesson add 1 s/MATH d/MONDAY st/16:00 et/18:00",
                LessonAddCommand.MESSAGE_SAVE_FAILURE);
        assertEquals(before, model.getAddressBook());
        assertEquals(beforeFile, Files.readString(dataFilePath));
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
        assertCommandException("delete 9", MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String clearCommand = ClearCommand.COMMAND_WORD;
        assertCommandSuccess(clearCommand, ClearCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_listCommand_doesNotSaveData() throws Exception {
        assertCommandSuccess(ListCommand.COMMAND_WORD, ListCommand.MESSAGE_EMPTY, model);
        assertFalse(Files.exists(dataFilePath));
    }

    @Test
    public void execute_searchFiltersDisplayWithoutSavingData() throws Exception {
        setUpLogic(getTypicalAddressBook(), getStorageThatFailsToSave(DUMMY_IO_EXCEPTION));
        AddressBook before = new AddressBook(model.getAddressBook());

        CommandResult result = logic.execute("search Alice");

        assertEquals("Found 1 student for \"Alice\".\nType list to show all students.",
                result.getFeedbackToUser());
        assertEquals(1, logic.getStudentList().size());
        assertEquals(before, model.getAddressBook());
        assertFalse(Files.exists(dataFilePath));
    }

    @Test
    public void execute_lessonClashHiddenBySearch_preservesSavedRoster() throws Exception {
        logic.execute(ADD_STUDENT_COMMAND);
        logic.execute("add n/Alex Lim l/S3 s/MATH p/92345678 gn/Jane Lim gp/98765433");
        logic.execute("lesson add 1 s/MATH d/MONDAY st/14:00 et/16:00");
        String savedBefore = Files.readString(dataFilePath);
        AddressBook rosterBefore = new AddressBook(model.getAddressBook());
        logic.execute("search Alex");

        assertCommandException("lesson add 1 s/MATH d/MONDAY st/15:00 et/17:00",
                LessonAddCommand.MESSAGE_CLASH + "John Tan; MATH; MONDAY; 14:00-16:00");

        assertEquals(1, logic.getStudentList().size());
        assertEquals(rosterBefore, model.getAddressBook());
        assertEquals(savedBefore, Files.readString(dataFilePath));
        assertEquals(rosterBefore, new AddressBook(addressBookStorage.readAddressBook().get()));
    }

    @Test
    public void execute_homeworkUpcomingAfterSearch_studentIndexMatchesHomeworkList() throws Exception {
        // today is 2026-10-19, so the mechanics homework of Alice Tan (1st) and Carl Ng (3rd) is upcoming
        setUpLogic(getTypicalAddressBook(), addressBookStorage,
                Clock.fixed(Instant.parse("2026-10-19T00:00:00Z"), ZoneOffset.UTC));
        logic.execute("search Carl");

        String upcomingFeedback = logic.execute("hw up").getFeedbackToUser();

        assertEquals(3, logic.getStudentList().size());
        Matcher carlLine = Pattern.compile("\nStudent (\\d+): Carl Ng;").matcher(upcomingFeedback);
        assertTrue(carlLine.find());
        assertEquals("Listed 1 upcoming homework record for Carl Ng.\nHomework for Carl Ng:\n"
                + "1. Attempt mechanics questions 1-5 (PHYSICS) - due 2026-10-20 - score Not recorded",
                logic.execute("hw ls " + carlLine.group(1)).getFeedbackToUser());
        assertFalse(Files.exists(dataFilePath));
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
    public void execute_homeworkEdit_savesEditedHomework() throws Exception {
        setUpLogic(getTypicalAddressBook(), addressBookStorage);

        logic.execute("hw e 1 1 t/Complete algebra questions 1-10");

        ReadOnlyAddressBook savedData = addressBookStorage.readAddressBook().get();
        assertEquals("Complete algebra questions 1-10",
                savedData.getStudentList().get(0).getHomeworks().get(0).getTitle().toString());
        assertEquals(model.getAddressBook(), new AddressBook(savedData));
    }

    @Test
    public void execute_homeworkMarks_savesScore() throws Exception {
        setUpLogic(getTypicalAddressBook(), addressBookStorage);

        logic.execute("hw m 1 2 score/5/10");

        ReadOnlyAddressBook savedData = addressBookStorage.readAddressBook().get();
        assertEquals(Optional.of(new Score(5, 10)), savedData.getStudentList().get(0).getHomeworks().get(1).getScore());
        assertEquals(model.getAddressBook(), new AddressBook(savedData));
    }

    @Test
    public void execute_homeworkMarksClear_savesClearedScore() throws Exception {
        setUpLogic(getTypicalAddressBook(), addressBookStorage);

        logic.execute("hw m 1 3 score/");

        ReadOnlyAddressBook savedData = addressBookStorage.readAddressBook().get();
        assertEquals(Optional.empty(), savedData.getStudentList().get(0).getHomeworks().get(2).getScore());
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
        logic.execute("hw up due/365");
        logic.execute("help");

        assertEquals(new ModelManager(getTypicalAddressBook(), new UserPrefs()), model);
        assertFalse(Files.exists(dataFilePath));
    }

    @Test
    public void execute_deleteStudent_savesRemainingStudents() throws Exception {
        setUpLogic(getTypicalAddressBook(), addressBookStorage);

        logic.execute("delete 1");

        assertEquals(getTypicalAddressBook().getStudentList().size() - 1, logic.getStudentList().size());
        assertEquals(model.getAddressBook(), new AddressBook(addressBookStorage.readAddressBook().get()));
    }

    @Test
    public void execute_deleteStudentWithHomeworkAndLesson_removesDataAcrossReload() throws Exception {
        AddressBook initial = new AddressBook();
        initial.addStudent(new StudentBuilder().withHomeworks(ALGEBRA).withLessons(MONDAY_MATH).build());
        initial.addStudent(new StudentBuilder().withName("Jane Tan").withPhone("92345678")
                .withSubjects("PHYSICS").withHomeworks(MECHANICS).withLessons(TUESDAY_PHYSICS).build());
        setUpLogic(initial, addressBookStorage);

        logic.execute("delete 1");

        ReadOnlyAddressBook reloaded = addressBookStorage.readAddressBook().get();
        assertEquals(List.of(initial.getStudentList().get(1)), reloaded.getStudentList());
        assertEquals(reloaded.getStudentList(), logic.getStudentList());
    }

    @Test
    public void execute_deleteStudentSaveFails_restoresStudentHomeworkAndLesson() {
        AddressBook initial = getTypicalAddressBook();
        initial.setStudent(initial.getStudentList().get(0),
                initial.getStudentList().get(0).withLessons(List.of(MONDAY_MATH)));
        setUpLogic(initial, getStorageThatFailsToSave(DUMMY_IO_EXCEPTION));
        String expectedMessage = String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage());

        assertCommandFailure("delete 1", CommandException.class, expectedMessage,
                new ModelManager(initial, new UserPrefs()));
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
    public void execute_homeworkEditSaveFails_rollsBackEdit() {
        setUpLogic(getTypicalAddressBook(), getStorageThatFailsToSave(DUMMY_IO_EXCEPTION));

        assertCommandFailure("hw edit 1 2 t/Revise mechanics s/PHYSICS due/2026-11-10", CommandException.class,
                HomeworkEditCommand.MESSAGE_SAVE_FAILURE, new ModelManager(getTypicalAddressBook(), new UserPrefs()));
        assertFalse(Files.exists(dataFilePath));
    }

    @Test
    public void execute_homeworkMarksSaveFails_rollsBackScore() {
        setUpLogic(getTypicalAddressBook(), getStorageThatFailsToSave(DUMMY_AD_EXCEPTION));

        assertCommandFailure("hw marks 1 3 score/0/10", CommandException.class,
                HomeworkMarksCommand.MESSAGE_SAVE_FAILURE, new ModelManager(getTypicalAddressBook(), new UserPrefs()));
        assertFalse(Files.exists(dataFilePath));
    }

    @Test
    public void execute_homeworkMarksClearSaveFails_keepsScore() {
        setUpLogic(getTypicalAddressBook(), getStorageThatFailsToSave(DUMMY_AD_EXCEPTION));

        assertCommandFailure("hw m 1 3 score/", CommandException.class,
                HomeworkMarksCommand.MESSAGE_SAVE_FAILURE, new ModelManager(getTypicalAddressBook(), new UserPrefs()));
        assertFalse(Files.exists(dataFilePath));
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
        setUpLogic(data, storage, Clock.systemDefaultZone());
    }

    /**
     * Replaces {@code model} with one holding {@code data}, and {@code logic} with one using {@code storage} and
     * taking today's date from {@code clock}.
     */
    private void setUpLogic(ReadOnlyAddressBook data, JsonAddressBookStorage storage, Clock clock) {
        model = new ModelManager(data, new UserPrefs());
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        logic = new LogicManager(model, new StorageManager(storage, userPrefsStorage), clock);
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
