package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.CAUSE_READ_ONLY_FOLDER;
import static seedu.address.logic.commands.CommandTestUtil.SHOWN_DATA_FILE_NAME;
import static seedu.address.logic.commands.CommandTestUtil.formatHomeworkLine;
import static seedu.address.logic.parser.CommandParserTestUtil.MESSAGE_TITLE_AND_ALIAS;
import static seedu.address.logic.parser.CommandParserTestUtil.TITLE_EXAMPLE_OTHER_PREFIX;
import static seedu.address.logic.parser.CommandParserTestUtil.TITLE_EXAMPLE_TITLE_PREFIX;
import static seedu.address.logic.parser.CommandParserTestUtil.notPrefix;
import static seedu.address.logic.parser.CommandParserTestUtil.notPrefixWithQuoteHint;
import static seedu.address.logic.parser.CommandParserTestUtil.titleQuoteHint;
import static seedu.address.model.homework.DueDateTest.MESSAGE_EMPTY;
import static seedu.address.model.homework.DueDateTest.MESSAGE_YEAR_OUT_OF_RANGE;
import static seedu.address.model.homework.DueDateTest.leapDayMessage;
import static seedu.address.model.homework.DueDateTest.notRealDateMessage;
import static seedu.address.model.homework.DueDateTest.wrongFormatMessage;
import static seedu.address.model.util.SampleDataUtil.getSampleAddressBook;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestUtil.ARABIC_EGYPT;
import static seedu.address.testutil.TestUtil.createSymbolicLinkOrSkip;
import static seedu.address.testutil.TestUtil.runWithFormatLocale;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalHomeworks.MECHANICS;
import static seedu.address.testutil.TypicalRegularLessons.MONDAY_MATH;
import static seedu.address.testutil.TypicalRegularLessons.TUESDAY_PHYSICS;
import static seedu.address.testutil.TypicalStudents.getModelWithTypicalStudents;
import static seedu.address.testutil.TypicalStudents.getTypicalAddressBook;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
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

import javafx.collections.ObservableList;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.HomeworkAddCommand;
import seedu.address.logic.commands.HomeworkDeleteCommand;
import seedu.address.logic.commands.HomeworkEditCommand;
import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.homework.Score;
import seedu.address.model.student.Student;
import seedu.address.model.student.TuitionSubject;
import seedu.address.storage.DataFileConflictException;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.StudentBuilder;
import seedu.address.ui.HelpContent;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    /** The data file of {@link #setUp()} is {@code tutorflow.json} in the home folder, so only its name is shown. */
    private static final String SET_UP_DATA_FILE_NAME = "tutorflow.json";
    private static final String DUMMY_IO_ERROR_MESSAGE =
            "Could not save tutorflow.json: dummy IO exception. Nothing was changed.";
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");
    private static final String CAUSE_FOLDER = "it is a folder, not a file";
    private static final String CAUSE_READ_ONLY_FILE = "the file is read-only";
    private static final String CAUSE_BLOCKED_FOLDER = "a file named data is in the way of the data folder";
    private static final String CAUSE_READ_ONLY_HOME_FOLDER = "the home folder is read-only";
    private static final String ADD_SAMPLE_STUDENT_COMMAND =
            "add n/Aaron Koh l/S2 s/MATH p/91112222 gn/Koh Mei Hua gp/98887777";
    private static final String ADD_STUDENT_COMMAND = "add n/John Tan l/S3 s/MATH p/91234567 gn/Mary Tan gp/98765432";
    /** Fixed clock at 2026-11-06, a day on which each sample student with homework has some upcoming. */
    private static final Clock SAMPLE_CLOCK = Clock.fixed(Instant.parse("2026-11-06T00:00:00Z"), ZoneOffset.UTC);
    /** Fixed clock at 2026-11-07, the day after the sample clock; the sample homework lists the same. */
    private static final Clock HIDDEN_NOTE_CLOCK =
            Clock.fixed(Instant.parse("2026-11-07T00:00:00Z"), ZoneOffset.UTC);

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
        logic = new LogicManager(model, storage, Clock.systemDefaultZone(), temporaryFolder);
    }

    @Test
    public void constructor_nullArgument_throwsNullPointerException() {
        StorageManager storage = new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        Clock clock = Clock.systemDefaultZone();
        assertThrows(NullPointerException.class, () -> new LogicManager(null, storage, clock));
        assertThrows(NullPointerException.class, () -> new LogicManager(model, null, clock));
        assertThrows(NullPointerException.class, () -> new LogicManager(model, storage, null));
    }

    @Test
    public void execute_studentEditAfterHomeworkCompletion_preservesStatusAndLessonsAcrossReload() throws Exception {
        logic.execute(ADD_STUDENT_COMMAND);
        logic.execute("hw add 1 t/Algebra s/MATH due/2026-10-15");
        logic.execute("lesson add 1 s/MATH d/MONDAY st/16:00 et/18:00");
        logic.execute("hw status 1 1 status/COMPLETED");
        logic.execute("edit 1 n/John Lim");

        var savedStudent = addressBookStorage.readAddressBook().orElseThrow().getStudentList().getFirst();
        assertEquals("John Lim", savedStudent.getName().toString());
        assertEquals(HomeworkStatus.COMPLETED, savedStudent.getHomeworks().getFirst().getStatus());
        assertEquals(1, savedStudent.getLessons().size());
        assertEquals(model.getAddressBook(), new AddressBook(addressBookStorage.readAddressBook().orElseThrow()));
    }

    @Test
    public void execute_statusCompleteAndReopen_persistIndependentlyOfScore() throws Exception {
        logic.execute(ADD_STUDENT_COMMAND);
        logic.execute("hw add 1 t/Algebra s/MATH due/2026-10-15");
        logic.execute("hw status 1 1 status/COMPLETED");
        ReadOnlyAddressBook reloaded = addressBookStorage.readAddressBook().orElseThrow();
        assertEquals(HomeworkStatus.COMPLETED, reloaded.getStudentList().getFirst().getHomeworks().getFirst()
                .getStatus());
        assertEquals(Optional.empty(), reloaded.getStudentList().getFirst().getHomeworks().getFirst().getScore());
        setUpLogic(reloaded, addressBookStorage);
        logic.execute("hw marks 1 1 score/17");
        logic.execute("hw edit 1 1 t/Updated algebra");
        reloaded = addressBookStorage.readAddressBook().orElseThrow();
        assertEquals(HomeworkStatus.COMPLETED, reloaded.getStudentList().getFirst().getHomeworks().getFirst()
                .getStatus());
        logic.execute("hw status 1 1 status/ASSIGNED");
        reloaded = addressBookStorage.readAddressBook().orElseThrow();
        var savedHomework = reloaded.getStudentList().getFirst().getHomeworks().getFirst();
        assertEquals(HomeworkStatus.ASSIGNED, savedHomework.getStatus());
        assertEquals(Optional.of(new Score(17)), savedHomework.getScore());
        assertEquals(model.getAddressBook(), new AddressBook(reloaded));
    }

    @Test
    public void execute_statusSaveFailure_restoresOriginalStatusAndFile() throws Exception {
        logic.execute(ADD_STUDENT_COMMAND);
        logic.execute("hw add 1 t/Algebra s/MATH due/2026-10-15");
        for (String nextStatus : List.of("COMPLETED", "ASSIGNED")) {
            AddressBook before = new AddressBook(model.getAddressBook());
            String fileBefore = Files.readString(dataFilePath);
            setUpLogic(before, getStorageThatFailsToSave(DUMMY_IO_EXCEPTION));
            assertCommandException("hw status 1 1 status/" + nextStatus, getCommandSaveFailureMessage(
                    "homework status could not be updated", SET_UP_DATA_FILE_NAME, DUMMY_IO_EXCEPTION, "homework"));
            assertEquals(before, model.getAddressBook());
            assertEquals(fileBefore, Files.readString(dataFilePath));
            setUpLogic(before, addressBookStorage);
            logic.execute("hw status 1 1 status/" + nextStatus);
        }
    }

    @Test
    public void execute_completionFilter_neverSaves() throws Exception {
        setUpLogic(getTypicalAddressBook(), getStorageThatFailsToSave(DUMMY_IO_EXCEPTION));
        logic.execute("hw filter status/ASSIGNED");
        logic.execute("hw filter status/COMPLETED");
        assertFalse(Files.exists(dataFilePath));
    }

    @Test
    public void execute_lessonEdit_persistsReplacementAcrossReload() throws Exception {
        logic.execute(ADD_STUDENT_COMMAND);
        logic.execute("lesson add 1 s/MATH d/MONDAY st/16:00 et/18:00");
        logic.execute("lesson edit 1 st/17:00");
        ReadOnlyAddressBook saved = addressBookStorage.readAddressBook().get();
        assertEquals(model.getAddressBook(), new AddressBook(saved));
        assertEquals(java.time.LocalTime.of(17, 0), saved.getStudentList().getFirst().getLessons().getFirst()
                .getStartTime());
    }

    @Test
    public void execute_lessonEditSaveFails_restoresOriginalAndFile() throws Exception {
        logic.execute(ADD_STUDENT_COMMAND);
        logic.execute("lesson add 1 s/MATH d/MONDAY st/16:00 et/18:00");
        AddressBook before = new AddressBook(model.getAddressBook());
        String savedBefore = Files.readString(dataFilePath);
        setUpLogic(before, getStorageThatFailsToSave(DUMMY_IO_EXCEPTION));
        assertCommandException("lesson edit 1 st/17:00", getCommandSaveFailureMessage(
                "lesson could not be edited", SET_UP_DATA_FILE_NAME, DUMMY_IO_EXCEPTION, "lesson"));
        assertEquals(before, model.getAddressBook());
        assertEquals(savedBefore, Files.readString(dataFilePath));
    }

    @Test
    public void execute_homeworkFilter_neverSavesOrChangesDisplayedView() throws Exception {
        setUpLogic(getTypicalAddressBook(), getStorageThatFailsToSave(DUMMY_IO_EXCEPTION));
        logic.execute("search Alice");
        List<seedu.address.model.student.Student> displayed = List.copyOf(model.getStudentList());
        AddressBook before = new AddressBook(model.getAddressBook());
        logic.execute("hw filter 1");
        logic.execute("hw filter s/PHYSICS due/2026-10-20");
        logic.execute("hw filter s/MATH due/2026-01-01");
        assertEquals(before, model.getAddressBook());
        assertEquals(displayed, model.getStudentList());
        assertFalse(Files.exists(dataFilePath));
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
        assertCommandException("lesson add 1 s/MATH d/MONDAY st/16:00 et/18:00", getCommandSaveFailureMessage(
                "lesson could not be added", SET_UP_DATA_FILE_NAME, DUMMY_IO_EXCEPTION, "lesson"));
        assertEquals(before, model.getAddressBook());
        assertEquals(beforeFile, Files.readString(dataFilePath));
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_unknownPrefixInTitle_namesWordAndKeepsData() {
        setUpLogic(getSampleAddressBook(), addressBookStorage, SAMPLE_CLOCK);

        String usageLine = "\nUsage: " + HomeworkAddCommand.MESSAGE_USAGE;
        String quoteHint = titleQuoteHint("km/", TITLE_EXAMPLE_OTHER_PREFIX);
        assertParseException("hw add 1 t/Speed in km/h s/PHYSICS due/2026-11-01", notPrefix("km/") + quoteHint
                + usageLine);
        // the closing quote is not followed by a space, so the title is not quoted
        assertParseException("hw add 1 t/\"Speed in km/h\"s/PHYSICS due/2026-11-20", notPrefix("km/") + quoteHint
                + usageLine);
        // x/ follows the due date, not the title, so there is no hint to quote the title
        assertParseException("hw add 1 t/A s/PHYSICS due/2026-11-01 x/y", notPrefix("x/") + usageLine);
        assertFalse(Files.exists(dataFilePath));
    }

    @Test
    public void execute_quotedTitleWithSlashWord_addsAndSaves() throws Exception {
        setUpLogic(getSampleAddressBook(), addressBookStorage, SAMPLE_CLOCK);

        logic.execute("hw add 1 t/\"Speed in km/h\" s/PHYSICS due/2026-11-01");

        Student savedStudent = addressBookStorage.readAddressBook().orElseThrow().getStudentList().getFirst();
        assertEquals("Speed in km/h", savedStudent.getHomeworks().get(2).getTitle().toString());
    }

    @Test
    public void execute_titleAndAliasInEdit_reportsUseEither() {
        setUpLogic(getSampleAddressBook(), addressBookStorage, SAMPLE_CLOCK);

        assertParseException("hw e 1 2 t/A title/B", MESSAGE_TITLE_AND_ALIAS
                + titleQuoteHint("title/", TITLE_EXAMPLE_TITLE_PREFIX) + "\nUsage: "
                + HomeworkEditCommand.MESSAGE_USAGE);
        assertFalse(Files.exists(dataFilePath));
    }

    @Test
    public void execute_unknownPrefixInOtherCommands_namesPrefix() {
        setUpLogic(getSampleAddressBook(), addressBookStorage, SAMPLE_CLOCK);

        assertParseException("edit 1 l/S3 x/value", notPrefixWithQuoteHint("x/", "l/\"S3 x/value\"") + "\nUsage: "
                + EditCommand.MESSAGE_USAGE);
        // the end time before x/ is only digits and a colon, so no example is given
        assertParseException("lesson add 1 s/MATH d/FRIDAY st/10:00 et/11:00 x/y", notPrefixWithQuoteHint("x/")
                + "\nUsage: " + LessonAddCommand.MESSAGE_USAGE);
        // no value comes before the word, so there is no quote hint
        assertParseException("hw e 1 2 score/5/10", notPrefix("score/") + "\nUsage: "
                + HomeworkEditCommand.MESSAGE_USAGE);
        assertParseException("hw del 1 x/y", notPrefix("x/") + "\nUsage: " + HomeworkDeleteCommand.MESSAGE_USAGE);
        assertFalse(Files.exists(dataFilePath));
    }

    @Test
    public void execute_dueDateErrors_specificMessageAndNothingSaved() {
        setUpLogic(getSampleAddressBook(), addressBookStorage, SAMPLE_CLOCK);

        assertParseException("hw add 1 t/X s/MATH due/", MESSAGE_EMPTY);
        assertParseException("hw add 1 t/X s/MATH due/30-10-2026", wrongFormatMessage("30-10-2026"));
        assertParseException("hw add 1 t/X s/MATH due/2026-13-01", notRealDateMessage("2026-13-01"));
        assertParseException("hw add 1 t/X s/MATH due/2026-02-30", notRealDateMessage("2026-02-30"));
        // today is 2026-11-06, and neither 2026 nor 2027 is a leap year
        assertParseException("hw add 1 t/X s/MATH due/2-29", leapDayMessage(2028));
        assertParseException("hw add 1 t/Typo year s/MATH due/2206-10-30", MESSAGE_YEAR_OUT_OF_RANGE);
        assertFalse(Files.exists(dataFilePath));
    }

    @Test
    public void execute_leapDayArabicDigitsLocale_exampleInAsciiDigitsCanBeTypedBack() {
        setUpLogic(getSampleAddressBook(), addressBookStorage, SAMPLE_CLOCK);

        runWithFormatLocale(ARABIC_EGYPT, () -> {
            // today is 2026-11-06, so the example is 2028-02-29, which the user may type back in
            assertParseException("hw add 1 t/X s/MATH due/2-29", leapDayMessage(2028));
            String feedback = assertDoesNotThrow(() -> logic.execute("hw add 1 t/X s/MATH due/2028-02-29"))
                    .getFeedbackToUser();
            assertEquals("New homework added for Ethan Lim (91234567):\n"
                    + "3. X (MATH) - due 2028-02-29 - score Not recorded - status ASSIGNED", feedback);
        });
    }

    @Test
    public void execute_pastDueDateInRange_addsAndSaves() throws Exception {
        setUpLogic(getSampleAddressBook(), addressBookStorage, SAMPLE_CLOCK);

        assertEquals("New homework added for Ethan Lim (91234567):\n"
                + "3. X (MATH) - due 2001-01-01 - score Not recorded - status ASSIGNED\n"
                + "Note: this homework is due before today, so homework list 1 shows it only with all.",
                logic.execute("hw add 1 t/X s/MATH due/2001-01-01").getFeedbackToUser());

        Student savedStudent = addressBookStorage.readAddressBook().orElseThrow().getStudentList().getFirst();
        assertTrue(savedStudent.getHomeworks().stream().anyMatch(homework -> homework.getTitle().toString().equals("X")
                && homework.getDueDate().toString().equals("2001-01-01")));
    }

    @Test
    public void execute_helpEditExampleOnSampleData_editsPhysicsRecord() throws Exception {
        setUpLogic(getSampleAddressBook(), addressBookStorage, SAMPLE_CLOCK);
        String example = HelpContent.getSections().stream().flatMap(section -> section.entries().stream())
                .filter(entry -> entry.commandWord().equals("homework edit")).findFirst().orElseThrow().example();

        logic.execute(example);

        Homework savedHomework = addressBookStorage.readAddressBook().orElseThrow().getStudentList().getFirst()
                .getHomeworks().get(1);
        assertEquals("Kinematics practice set B", savedHomework.getTitle().toString());
        assertEquals(TuitionSubject.PHYSICS, savedHomework.getSubject());
        assertEquals("2026-11-11", savedHomework.getDueDate().toString());
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
    public void getAllStudentList_searchAndRosterChanges_returnsLiveUnmodifiableRoster() throws Exception {
        model.setAddressBook(getTypicalAddressBook());
        ObservableList<Student> allStudents = logic.getAllStudentList();
        List<Student> expectedStudents = List.copyOf(allStudents);

        logic.execute("search Alice");
        assertEquals(1, logic.getStudentList().size());
        assertEquals(expectedStudents, allStudents);
        assertThrows(UnsupportedOperationException.class, () -> allStudents.remove(0));

        logic.execute("clear");
        assertEquals(List.of(), allStudents);

        Student student = new StudentBuilder().build();
        model.addStudent(student);
        assertEquals(List.of(student), allStudents);
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
                LessonAddCommand.MESSAGE_CLASH + "John Tan (91234567); MATH; MONDAY; 14:00-16:00");

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
        Matcher carlLine = Pattern.compile("\nStudent (\\d+): Carl Ng \\(93333333\\);").matcher(upcomingFeedback);
        assertTrue(carlLine.find());
        assertEquals("Listed 1 upcoming homework record for Carl Ng (93333333).\nHomework for Carl Ng (93333333):\n"
                + "1. Attempt mechanics questions 1-5 (PHYSICS) - due 2026-10-20 - score Not recorded"
                + " - status ASSIGNED",
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
    public void execute_twoDeletesFromShownNumbers_deletesIntendedRecords() throws Exception {
        setUpLogic(getSampleAddressBook(), addressBookStorage, SAMPLE_CLOCK);
        logic.execute("hw add 2 t/Vectors revision s/MATH due/2026-11-30");

        // the overdue integration homework (due 2026-11-02) is not listed
        assertEquals("Deleted homework:\n"
                + formatHomeworkLine(2, "Organic reaction pathways map", "CHEMISTRY", "2026-11-09", "Not recorded",
                        "ASSIGNED")
                + "\nListed 2 upcoming homework records for Priya Nair (82345671)"
                + " (1 record due before today not shown; use homework list 2 all to see it).\n"
                + "Homework for Priya Nair (82345671):\n"
                + formatHomeworkLine(2, "Chemical equilibrium past paper", "CHEMISTRY", "2026-11-23", "Not recorded",
                        "ASSIGNED") + "\n"
                + formatHomeworkLine(3, "Vectors revision", "MATH", "2026-11-30", "Not recorded", "ASSIGNED"),
                logic.execute("hw del 2 2").getFeedbackToUser());

        // 2 is the number just shown for the chemical equilibrium homework
        logic.execute("hw del 2 2");
        Student savedStudent = addressBookStorage.readAddressBook().orElseThrow().getStudentList().get(1);
        assertEquals(List.of("Integration by parts exercise", "Vectors revision"), savedStudent.getHomeworks()
                .stream().map(homework -> homework.getTitle().toString()).toList());
    }

    @Test
    public void execute_editEarlierSampleHomework_hintsHiddenRecord() throws Exception {
        // Priya Nair's homework 1 is due 2026-11-02, so on 2026-11-07 the list after the edit starts at 2
        setUpLogic(getSampleAddressBook(), addressBookStorage, HIDDEN_NOTE_CLOCK);

        assertEquals("Edited homework:\n"
                + "Priya Nair (82345671); Title: Research Organic pathways; Subject: MATH; Due: 2026-11-02\n"
                + "Listed 2 upcoming homework records for Priya Nair (82345671)"
                + " (1 record due before today not shown; use homework list 2 all to see it).\n"
                + "Homework for Priya Nair (82345671):\n"
                + formatHomeworkLine(2, "Organic reaction pathways map", "CHEMISTRY", "2026-11-09", "Not recorded",
                        "ASSIGNED") + "\n"
                + formatHomeworkLine(3, "Chemical equilibrium past paper", "CHEMISTRY", "2026-11-23", "Not recorded",
                        "ASSIGNED"),
                logic.execute("hw e 2 1 t/Research Organic pathways").getFeedbackToUser());
    }

    @Test
    public void execute_hiddenNoteAfterSearch_hintedCommandListsHiddenRecord() throws Exception {
        // after the search Priya Nair is displayed student 1, so the hint names 1, not her roster position 2
        setUpLogic(getSampleAddressBook(), addressBookStorage, SAMPLE_CLOCK);
        logic.execute("search Priya");
        String hintedCommand = "homework list 1 all";

        assertTrue(logic.execute("hw ls 1").getFeedbackToUser().startsWith("Listed 2 upcoming homework records for"
                + " Priya Nair (82345671) (1 record due before today not shown; use " + hintedCommand
                + " to see it).\n"));
        assertTrue(logic.execute(hintedCommand).getFeedbackToUser().contains("\n"
                + formatHomeworkLine(1, "Integration by parts exercise", "MATH", "2026-11-02", "Not recorded",
                        "ASSIGNED")));
    }

    @Test
    public void execute_hiddenNoteAfterFilterAndEdit_hintedCommandListsHiddenRecord() throws Exception {
        // after the filter Priya Nair is displayed student 1, so the hint names 1, not her roster position 2
        setUpLogic(getSampleAddressBook(), addressBookStorage, HIDDEN_NOTE_CLOCK);
        logic.execute("filter s/CHEMISTRY");
        String hintedCommand = "homework list 1 all";

        assertTrue(logic.execute("hw e 1 2 t/Pathways map v2").getFeedbackToUser().startsWith("Edited homework:\n"
                + "Priya Nair (82345671); Title: Pathways map v2; Subject: CHEMISTRY; Due: 2026-11-09\n"
                + "Listed 2 upcoming homework records for Priya Nair (82345671) (1 record due before today not "
                + "shown; use "
                + hintedCommand + " to see it).\n"));
        assertTrue(logic.execute(hintedCommand).getFeedbackToUser().contains("\n"
                + formatHomeworkLine(1, "Integration by parts exercise", "MATH", "2026-11-02", "Not recorded",
                        "ASSIGNED")));
    }

    @Test
    public void execute_hiddenNoteAfterSortAndDelete_hintedCommandListsHiddenRecord() throws Exception {
        // after the sort Ethan Lim is displayed student 4, so the hint names 4, not his roster position 1
        setUpLogic(getSampleAddressBook(), addressBookStorage, HIDDEN_NOTE_CLOCK);
        logic.execute("sort by/level o/desc");
        String hintedCommand = "homework list 4 all";

        assertEquals("Deleted homework:\n"
                + formatHomeworkLine(2, "Kinematics practice set", "PHYSICS", "2026-11-11", "Not recorded",
                        "ASSIGNED")
                + "\nNo upcoming homework found for Ethan Lim (91234567) (1 record due before today not shown; use "
                + hintedCommand + " to see it).",
                logic.execute("hw del 4 2").getFeedbackToUser());
        assertTrue(logic.execute(hintedCommand).getFeedbackToUser().contains("\n"
                + formatHomeworkLine(1, "Quadratic equations worksheet", "MATH", "2026-11-03", "Not recorded",
                        "ASSIGNED")));
    }

    @Test
    public void execute_deleteOnlyEarlierSampleHomework_removesHiddenNote() throws Exception {
        // Priya Nair's homework 1 (due 2026-11-02) is her only record due before today, so after deleting it
        // none is hidden
        setUpLogic(getSampleAddressBook(), addressBookStorage, HIDDEN_NOTE_CLOCK);

        assertEquals("Deleted homework:\n"
                + formatHomeworkLine(1, "Integration by parts exercise", "MATH", "2026-11-02", "Not recorded",
                        "ASSIGNED")
                + "\nListed 2 upcoming homework records for Priya Nair (82345671).\nHomework for Priya Nair "
                + "(82345671):\n"
                + formatHomeworkLine(1, "Organic reaction pathways map", "CHEMISTRY", "2026-11-09", "Not recorded",
                        "ASSIGNED") + "\n"
                + formatHomeworkLine(2, "Chemical equilibrium past paper", "CHEMISTRY", "2026-11-23", "Not recorded",
                        "ASSIGNED"),
                logic.execute("hw del 2 1").getFeedbackToUser());
    }

    @Test
    public void execute_editEarlierSampleDueDateToFuture_removesHiddenNote() throws Exception {
        // Priya Nair's homework 1 (due 2026-11-02) is her only record due before today until the edit moves it
        // to December
        setUpLogic(getSampleAddressBook(), addressBookStorage, HIDDEN_NOTE_CLOCK);

        assertEquals("Edited homework:\n"
                + "Priya Nair (82345671); Title: Integration by parts exercise; Subject: MATH; Due: 2026-12-01\n"
                + "Listed 3 upcoming homework records for Priya Nair (82345671).\nHomework for Priya Nair (82345671):\n"
                + formatHomeworkLine(2, "Organic reaction pathways map", "CHEMISTRY", "2026-11-09", "Not recorded",
                        "ASSIGNED") + "\n"
                + formatHomeworkLine(3, "Chemical equilibrium past paper", "CHEMISTRY", "2026-11-23", "Not recorded",
                        "ASSIGNED") + "\n"
                + formatHomeworkLine(1, "Integration by parts exercise", "MATH", "2026-12-01", "Not recorded",
                        "ASSIGNED"),
                logic.execute("hw e 2 1 due/2026-12-01").getFeedbackToUser());
    }

    @Test
    public void execute_deleteScoredHomework_showsScoreAndStatus() throws Exception {
        setUpLogic(getSampleAddressBook(), addressBookStorage, SAMPLE_CLOCK);
        logic.execute("hw m 2 1 score/5/10");

        assertEquals("Deleted homework:\n"
                + formatHomeworkLine(1, "Integration by parts exercise", "MATH", "2026-11-02", "5/10", "ASSIGNED")
                + "\nListed 2 upcoming homework records for Priya Nair (82345671).\nHomework for Priya Nair "
                + "(82345671):\n"
                + formatHomeworkLine(1, "Organic reaction pathways map", "CHEMISTRY", "2026-11-09", "Not recorded",
                        "ASSIGNED") + "\n"
                + formatHomeworkLine(2, "Chemical equilibrium past paper", "CHEMISTRY", "2026-11-23", "Not recorded",
                        "ASSIGNED"),
                logic.execute("hw del 2 1").getFeedbackToUser());
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

        assertEquals(getModelWithTypicalStudents(), model);
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

        assertCommandFailure("delete 1", CommandException.class, getCommandSaveFailureMessage(
                "student could not be deleted", SET_UP_DATA_FILE_NAME, DUMMY_IO_EXCEPTION, "student"),
                new ModelManager(initial, new UserPrefs()));
    }

    @Test
    public void execute_deleteStudentSaveFails_preservesSortedDisplayedOrder() throws Exception {
        AddressBook initial = new AddressBook();
        initial.addStudent(new StudentBuilder().withName("Secondary Student").withAcademicLevel("S3").build());
        initial.addStudent(new StudentBuilder().withName("Junior Student").withPhone("92345678")
                .withAcademicLevel("J1").build());
        initial.addStudent(new StudentBuilder().withName("Primary Student").withPhone("93456789")
                .withAcademicLevel("S1").build());
        setUpLogic(initial, getStorageThatFailsToSave(DUMMY_IO_EXCEPTION));
        logic.execute("sort by/level");
        List<Student> sortedStudents = List.copyOf(logic.getStudentList());
        assertFalse(sortedStudents.equals(initial.getStudentList()));

        String expectedMessage = getCommandSaveFailureMessage(
                "student could not be deleted", SET_UP_DATA_FILE_NAME, DUMMY_IO_EXCEPTION, "student");
        assertCommandException("delete 1", expectedMessage);

        assertEquals(sortedStudents, logic.getStudentList());
        assertEquals(initial, model.getAddressBook());
    }

    @Test
    public void execute_addStudentSaveFailsWithFilteredView_restoresStoredRoster() throws Exception {
        AddressBook initial = new AddressBook();
        initial.addStudent(new StudentBuilder().build());
        initial.addStudent(new StudentBuilder().withName("Jane Tan").withPhone("92345678").build());
        setUpLogic(initial, getStorageThatFailsToSave(DUMMY_IO_EXCEPTION));
        logic.execute("search John");
        assertEquals(1, logic.getStudentList().size());

        assertCommandException("add n/Olivia Tan l/S2 s/MATH p/93456789 gn/May Tan gp/98765432",
                DUMMY_IO_ERROR_MESSAGE);

        assertEquals(initial, model.getAddressBook());
    }

    @Test
    public void execute_editStudent_savesProfileAndPreservesLinkedRecordsAcrossReload() throws Exception {
        AddressBook initial = new AddressBook();
        initial.addStudent(new StudentBuilder().withHomeworks(ALGEBRA).withLessons(MONDAY_MATH).build());
        setUpLogic(initial, addressBookStorage);

        logic.execute("edit 1 n/Jane Tan l/S4 s/MATH, PHYSICS p/92345678 gn/May Tan gp/95554444");

        var expected = new StudentBuilder().withName("Jane Tan").withAcademicLevel("S4")
                .withSubjects("MATH, PHYSICS").withPhone("92345678").withGuardianName("May Tan")
                .withGuardianPhone("95554444").withHomeworks(ALGEBRA).withLessons(MONDAY_MATH).build();
        assertEquals(List.of(expected), logic.getStudentList());
        assertEquals(List.of(expected), addressBookStorage.readAddressBook().get().getStudentList());
    }

    @Test
    public void execute_editStudentSubjectOrder_persistsNewOrder() throws Exception {
        AddressBook initial = new AddressBook();
        initial.addStudent(new StudentBuilder().withSubjects("MATH, PHYSICS").build());
        setUpLogic(initial, addressBookStorage);

        logic.execute("edit 1 s/PHYSICS, MATH");

        Student saved = addressBookStorage.readAddressBook().orElseThrow().getStudentList().getFirst();
        assertEquals("PHYSICS, MATH", saved.getSubjects().toString());
    }

    @Test
    public void execute_editStudentAfterSearch_updatesSelectedStudentAndFilteredView() throws Exception {
        Student first = new StudentBuilder().build();
        Student second = new StudentBuilder().withName("Jane Tan").withPhone("92345678").build();
        AddressBook initial = new AddressBook();
        initial.addStudent(first);
        initial.addStudent(second);
        setUpLogic(initial, addressBookStorage);

        logic.execute("search Jane");
        logic.execute("edit 1 n/Jenna Tan");

        Student edited = new StudentBuilder().withName("Jenna Tan").withPhone("92345678").build();
        assertTrue(logic.getStudentList().isEmpty());
        assertEquals(List.of(first, edited), addressBookStorage.readAddressBook().get().getStudentList());

        logic.execute("list");
        assertEquals(List.of(first, edited), logic.getStudentList());
    }

    @Test
    public void execute_editStudentSaveFails_restoresProfileAndPreservesFile() throws Exception {
        logic.execute(ADD_STUDENT_COMMAND);
        logic.execute("hw add 1 t/Algebra s/MATH due/2026-10-15");
        logic.execute("lesson add 1 s/MATH d/MONDAY st/16:00 et/18:00");
        String beforeFile = Files.readString(dataFilePath);
        AddressBook before = new AddressBook(model.getAddressBook());
        setUpLogic(before, getStorageThatFailsToSave(DUMMY_IO_EXCEPTION));

        assertCommandException("edit 1 n/Jane Tan", getCommandSaveFailureMessage(
                "student could not be edited", SET_UP_DATA_FILE_NAME, DUMMY_IO_EXCEPTION, "student"));

        assertEquals(before, model.getAddressBook());
        assertEquals(beforeFile, Files.readString(dataFilePath));
    }

    @Test
    public void execute_editStudentRejectedOrUnchanged_doesNotSave() throws Exception {
        AddressBook initial = new AddressBook();
        initial.addStudent(new StudentBuilder().withHomeworks(ALGEBRA).withLessons(MONDAY_MATH).build());
        setUpLogic(initial, getStorageThatFailsToSave(DUMMY_IO_EXCEPTION));

        assertCommandException("edit 1 gn/Mary Tan", EditCommand.MESSAGE_NO_CHANGES);
        assertCommandException("edit 1 s/PHYSICS", EditCommand.MESSAGE_SUBJECT_IN_USE);

        assertEquals(initial, model.getAddressBook());
        assertFalse(Files.exists(dataFilePath));
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        // no common cause applies, so the system's text is the cause
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, DUMMY_IO_ERROR_MESSAGE);
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        // the file and its folder are writable, so the system's text is the cause
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION,
                "Could not save tutorflow.json: dummy access denied exception. Nothing was changed.");
    }

    @Test
    public void execute_dataFileDeletedWhileOpen_recreatesFileWithAllData() throws Exception {
        setUpLogic(getSampleAddressBook(), addressBookStorage, SAMPLE_CLOCK);
        logic.execute("hw add 1 t/Precious s/MATH due/2026-12-01");
        logic.execute(ADD_SAMPLE_STUDENT_COMMAND);
        Files.delete(dataFilePath);

        logic.execute("hw add 1 t/Another s/MATH due/2026-12-02");

        ReadOnlyAddressBook saved = new JsonAddressBookStorage(dataFilePath).readAddressBook().orElseThrow();
        assertEquals(model.getAddressBook(), new AddressBook(saved));
        assertEquals(7, saved.getStudentList().size());
        assertEquals(List.of("Precious", "Another"), saved.getStudentList().getFirst().getHomeworks().stream()
                .map(homework -> homework.getTitle().toString())
                .filter(title -> title.equals("Precious") || title.equals("Another")).toList());
    }

    @Test
    public void execute_dataFileCreatedByAnotherCopy_namesFileAndKeepsData() throws Exception {
        // No data file at launch, so this copy starts with no data; another copy then creates the file
        assertTrue(addressBookStorage.readAddressBook().isEmpty());
        new JsonAddressBookStorage(dataFilePath).saveAddressBook(getTypicalAddressBook());
        byte[] fileBefore = Files.readAllBytes(dataFilePath);

        assertCommandException(ADD_STUDENT_COMMAND, "Another program or copy of TutorFlow created "
                + SET_UP_DATA_FILE_NAME + " after this copy started. Restart TutorFlow before making changes."
                + " No data was changed.");
        assertArrayEquals(fileBefore, Files.readAllBytes(dataFilePath));
    }

    @Test
    public void execute_dataFileConflict_keepsConflictMessage() {
        setUpLogic(getTypicalAddressBook(), getStorageThatFailsToSave(
                new DataFileConflictException(DataFileConflictException.MESSAGE_CHANGED)));

        assertCommandFailure("hw del 1 2", CommandException.class, String.format(
                DataFileConflictException.MESSAGE_CHANGED, SET_UP_DATA_FILE_NAME), getModelWithTypicalStudents());
    }

    @Test
    public void execute_dataFileChangedAfterOwnSave_namesFileAndKeepsData() throws Exception {
        setUpLogic(getSampleAddressBook(), addressBookStorage, SAMPLE_CLOCK);
        logic.execute(ADD_SAMPLE_STUDENT_COMMAND);
        // another program changes the file after this copy saved it
        new JsonAddressBookStorage(dataFilePath).saveAddressBook(getTypicalAddressBook());
        byte[] fileBefore = Files.readAllBytes(dataFilePath);

        // PE round 2: the message named no file and said "since it was loaded", though this copy saved it since
        assertSaveFailureRestores("hw del 1 1", "Another program or copy of TutorFlow changed "
                + SET_UP_DATA_FILE_NAME + " after this copy last loaded or saved it. Restart TutorFlow before making"
                + " changes. No data was changed.");
        assertArrayEquals(fileBefore, Files.readAllBytes(dataFilePath));
    }

    @Test
    public void execute_dataFileBeingSaved_namesFileAndCopy() throws Exception {
        setUpLogic(getSampleAddressBook(), addressBookStorage, SAMPLE_CLOCK);
        Path lockFile = temporaryFolder.resolve("tutorflow.json.lock");
        // another copy of TutorFlow holds the lock while it saves
        try (FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock lock = channel.lock()) {
            assertSaveFailureRestores(ADD_SAMPLE_STUDENT_COMMAND, "Another copy of TutorFlow is saving "
                    + SET_UP_DATA_FILE_NAME + ". Try again. No data was changed.");
        }
    }

    @Test
    public void execute_linkedDataFileBeingSaved_namesLinkAndCopy() throws Exception {
        // PR #194 review: a copy saving the linked file by its own path did not stop a save through the link
        Path target = Files.createDirectories(temporaryFolder.resolve("synced")).resolve("tutorflow.json");
        new JsonAddressBookStorage(target).saveAddressBook(getSampleAddressBook());
        Path link = Files.createDirectories(temporaryFolder.resolve("data")).resolve("tutorflow.json");
        createSymbolicLinkOrSkip(link, target);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(link);
        setUpLogic(storage.readAddressBook().orElseThrow(), storage, SAMPLE_CLOCK);
        byte[] fileBefore = Files.readAllBytes(target);
        Path lockFile = target.resolveSibling("tutorflow.json.lock");
        try (FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock lock = channel.lock()) {
            assertSaveFailureRestores(ADD_SAMPLE_STUDENT_COMMAND, "Another copy of TutorFlow is saving "
                    + SHOWN_DATA_FILE_NAME + ". Try again. No data was changed.");
        }
        assertArrayEquals(fileBefore, Files.readAllBytes(target));
    }

    @Test
    public void execute_readOnlyLockFileOfLinkedFile_reportsThatLockFile() throws Exception {
        Path target = Files.createDirectories(temporaryFolder.resolve("synced")).resolve("tutorflow.json");
        new JsonAddressBookStorage(target).saveAddressBook(getSampleAddressBook());
        Path link = Files.createDirectories(temporaryFolder.resolve("data")).resolve("tutorflow.json");
        createSymbolicLinkOrSkip(link, target);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(link);
        setUpLogic(storage.readAddressBook().orElseThrow(), storage, SAMPLE_CLOCK);
        Path lockFile = target.resolveSibling("tutorflow.json.lock");
        assertTrue(lockFile.toFile().setWritable(false));
        try {
            assumeFalse(Files.isWritable(lockFile), "the file is still writable, e.g. when running as root");
            assertSaveFailureRestores("delete 1", getCommandSaveFailureMessage("student could not be deleted",
                    "the lock file " + Path.of("synced", "tutorflow.json.lock") + " is read-only", "student"));
        } finally {
            lockFile.toFile().setWritable(true);
        }
    }

    @Test
    public void execute_linkedDataFileDeletedWhileOpen_savesThroughLink() throws Exception {
        // PE round 3: the file that data/tutorflow.json links to was deleted, and the next save replaced the link
        Path target = Files.createDirectories(temporaryFolder.resolve("synced")).resolve("tutorflow.json");
        new JsonAddressBookStorage(target).saveAddressBook(getSampleAddressBook());
        Path link = Files.createDirectories(temporaryFolder.resolve("data")).resolve("tutorflow.json");
        createSymbolicLinkOrSkip(link, target);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(link);
        setUpLogic(storage.readAddressBook().orElseThrow(), storage, SAMPLE_CLOCK);
        Files.delete(target);

        logic.execute(ADD_SAMPLE_STUDENT_COMMAND);

        assertTrue(Files.isSymbolicLink(link));
        assertEquals(model.getAddressBook(), new JsonAddressBookStorage(target).readAddressBook().orElseThrow());
    }

    @Test
    public void execute_linkedDataFileInReadOnlyFolder_namesThatFolder() throws Exception {
        // PE round 3: the cause was the bare path of the data file
        Path targetFolder = Files.createDirectories(temporaryFolder.resolve("synced"));
        Path target = targetFolder.resolve("tutorflow.json");
        new JsonAddressBookStorage(target).saveAddressBook(getSampleAddressBook());
        Path link = Files.createDirectories(temporaryFolder.resolve("data")).resolve("tutorflow.json");
        createSymbolicLinkOrSkip(link, target);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(link);
        setUpLogic(storage.readAddressBook().orElseThrow(), storage, SAMPLE_CLOCK);
        byte[] fileBefore = Files.readAllBytes(target);
        makeFolderReadOnly(targetFolder);
        try {
            assertSaveFailureRestores(ADD_SAMPLE_STUDENT_COMMAND,
                    getGeneralSaveFailureMessage(SHOWN_DATA_FILE_NAME, "the folder synced is read-only"));
            assertArrayEquals(fileBefore, Files.readAllBytes(target));
            assertTrue(Files.isSymbolicLink(link));
        } finally {
            makeFolderWritable(targetFolder);
        }
    }

    @Test
    public void execute_linkedDataFileInMissingFolder_namesThatFolder() throws Exception {
        Path link = Files.createDirectories(temporaryFolder.resolve("data")).resolve("tutorflow.json");
        createSymbolicLinkOrSkip(link, temporaryFolder.resolve("gone").resolve("tutorflow.json"));
        setUpLogic(getSampleAddressBook(), new JsonAddressBookStorage(link), SAMPLE_CLOCK);

        assertSaveFailureRestores(ADD_SAMPLE_STUDENT_COMMAND,
                getGeneralSaveFailureMessage(SHOWN_DATA_FILE_NAME, "the folder gone does not exist"));
        assertTrue(Files.isSymbolicLink(link));
        assertFalse(Files.exists(temporaryFolder.resolve("gone")));
    }

    @Test
    public void execute_dataFolderLinksToMissingFolder_reportsBrokenLink() throws Exception {
        // PE round 3: data as a link to a folder that was moved or deleted gave the bare path of data as the cause
        Path dataFolderLink = temporaryFolder.resolve("data");
        createSymbolicLinkOrSkip(dataFolderLink, temporaryFolder.resolve("gone"));
        setUpLogic(getSampleAddressBook(), new JsonAddressBookStorage(dataFolderLink.resolve("tutorflow.json")),
                SAMPLE_CLOCK);

        assertSaveFailureRestores(ADD_SAMPLE_STUDENT_COMMAND,
                getGeneralSaveFailureMessage(SHOWN_DATA_FILE_NAME, "data links to a folder that does not exist"));
        assertSaveFailureRestores("delete 1", getCommandSaveFailureMessage("student could not be deleted",
                "data links to a folder that does not exist", "student"));
        assertTrue(Files.isSymbolicLink(dataFolderLink));
    }

    @Test
    public void execute_dataFileIsFolder_reportsFolderAndRestores() throws Exception {
        Path dataFile = temporaryFolder.resolve("data").resolve("tutorflow.json");
        Files.createDirectories(dataFile);
        setUpLogic(getSampleAddressBook(), new JsonAddressBookStorage(dataFile), SAMPLE_CLOCK);
        logic.execute("search Priya");

        assertSaveFailureRestores("delete 1", getCommandSaveFailureMessage(
                "student could not be deleted", SHOWN_DATA_FILE_NAME, CAUSE_FOLDER, "student"));
        assertTrue(Files.isDirectory(dataFile));
    }

    @Test
    public void execute_readOnlyLockFile_reportsReadOnlyLockFile() throws Exception {
        Path dataFile = temporaryFolder.resolve("data").resolve("tutorflow.json");
        setUpLogic(getSampleAddressBook(), new JsonAddressBookStorage(dataFile), SAMPLE_CLOCK);
        logic.execute("edit 6 l/S5");
        byte[] fileBefore = Files.readAllBytes(dataFile);
        Path lockFile = dataFile.resolveSibling("tutorflow.json.lock");
        assertTrue(lockFile.toFile().setWritable(false));
        try {
            assumeFalse(Files.isWritable(lockFile), "the file is still writable, e.g. when running as root");
            assertSaveFailureRestores("hw add 1 t/Test s/MATH due/2026-12-01", getCommandSaveFailureMessage(
                    "homework could not be added", "the lock file " + SHOWN_DATA_FILE_NAME + ".lock is read-only",
                    "homework"));
            assertArrayEquals(fileBefore, Files.readAllBytes(dataFile));
        } finally {
            lockFile.toFile().setWritable(true);
        }
    }

    @Test
    public void execute_lockFileIsFolder_reportsFolderLockFile() throws Exception {
        Path dataFile = temporaryFolder.resolve("data").resolve("tutorflow.json");
        Files.createDirectories(dataFile.resolveSibling("tutorflow.json.lock"));
        setUpLogic(getSampleAddressBook(), new JsonAddressBookStorage(dataFile), SAMPLE_CLOCK);

        assertSaveFailureRestores("delete 1", getCommandSaveFailureMessage("student could not be deleted",
                "the lock file " + SHOWN_DATA_FILE_NAME + ".lock is a folder, not a file", "student"));
        assertFalse(Files.exists(dataFile));
    }

    @Test
    public void execute_readOnlyDataFolderNoLock_reportsReadOnlyFolder() throws Exception {
        Path dataFolder = Files.createDirectories(temporaryFolder.resolve("data"));
        setUpLogic(getSampleAddressBook(), new JsonAddressBookStorage(dataFolder.resolve("tutorflow.json")),
                SAMPLE_CLOCK);
        makeFolderReadOnly(dataFolder);
        try {
            // the failure is on creating the internal lock file, so the folder, not the lock file, is the cause
            assertSaveFailureRestores("delete 1", getCommandSaveFailureMessage(
                    "student could not be deleted", SHOWN_DATA_FILE_NAME, CAUSE_READ_ONLY_FOLDER, "student"));
            assertEquals(List.of(), listFileNames(dataFolder));
        } finally {
            makeFolderWritable(dataFolder);
        }
    }

    @Test
    public void execute_readOnlyDataFolderWithLock_reportsReadOnlyFolder() throws Exception {
        Path dataFile = temporaryFolder.resolve("data").resolve("tutorflow.json");
        setUpLogic(getSampleAddressBook(), new JsonAddressBookStorage(dataFile), SAMPLE_CLOCK);
        logic.execute("edit 6 l/S5");
        byte[] fileBefore = Files.readAllBytes(dataFile);
        makeFolderReadOnly(dataFile.getParent());
        try {
            logic.execute("sort by/level");
            assertSaveFailureRestores("delete 1", getCommandSaveFailureMessage(
                    "student could not be deleted", SHOWN_DATA_FILE_NAME, CAUSE_READ_ONLY_FOLDER, "student"));
            assertArrayEquals(fileBefore, Files.readAllBytes(dataFile));
        } finally {
            makeFolderWritable(dataFile.getParent());
        }
    }

    @Test
    public void execute_readOnlyHomeFolderNoDataFolder_reportsReadOnlyHomeFolder() throws Exception {
        Path dataFile = temporaryFolder.resolve("data").resolve("tutorflow.json");
        setUpLogic(getSampleAddressBook(), new JsonAddressBookStorage(dataFile), SAMPLE_CLOCK);
        makeFolderReadOnly(temporaryFolder);
        try {
            assertSaveFailureRestores(ADD_SAMPLE_STUDENT_COMMAND,
                    getGeneralSaveFailureMessage(SHOWN_DATA_FILE_NAME, CAUSE_READ_ONLY_HOME_FOLDER));
            assertSaveFailureRestores("delete 1", getCommandSaveFailureMessage(
                    "student could not be deleted", CAUSE_READ_ONLY_HOME_FOLDER, "student"));
            assertFalse(Files.exists(dataFile.getParent()));
        } finally {
            makeFolderWritable(temporaryFolder);
        }
    }

    @Test
    public void execute_readOnlyFolderAboveDataFolder_namesThatFolder() throws Exception {
        Path readOnlyFolder = Files.createDirectories(temporaryFolder.resolve("store"));
        Path dataFile = readOnlyFolder.resolve("data").resolve("tutorflow.json");
        setUpLogic(getSampleAddressBook(), new JsonAddressBookStorage(dataFile), SAMPLE_CLOCK);
        makeFolderReadOnly(readOnlyFolder);
        try {
            assertSaveFailureRestores(ADD_SAMPLE_STUDENT_COMMAND, getGeneralSaveFailureMessage(
                    Path.of("store", "data", "tutorflow.json").toString(), "the folder store is read-only"));
        } finally {
            makeFolderWritable(readOnlyFolder);
        }
    }

    @Test
    public void execute_fileNamedData_reportsBlockedFolder() throws Exception {
        Path blockingFile = Files.writeString(temporaryFolder.resolve("data"), "x");
        setUpLogic(getSampleAddressBook(), new JsonAddressBookStorage(blockingFile.resolve("tutorflow.json")),
                SAMPLE_CLOCK);

        assertSaveFailureRestores(ADD_SAMPLE_STUDENT_COMMAND,
                getGeneralSaveFailureMessage(SHOWN_DATA_FILE_NAME, CAUSE_BLOCKED_FOLDER));
        assertEquals(6, model.getAddressBook().getStudentList().size());
        assertEquals("x", Files.readString(blockingFile));
    }

    @Test
    public void execute_fileNamedLikeOtherDataFolder_namesThatFolder() throws Exception {
        Path blockingFile = Files.writeString(temporaryFolder.resolve("store"), "x");
        setUpLogic(getSampleAddressBook(), new JsonAddressBookStorage(blockingFile.resolve("tutorflow.json")),
                SAMPLE_CLOCK);

        assertSaveFailureRestores(ADD_SAMPLE_STUDENT_COMMAND, getGeneralSaveFailureMessage(
                Path.of("store", "tutorflow.json").toString(), "a file named store is in the way of the data folder"));
    }

    @Test
    public void execute_dataFileOutsideHome_namesAbsolutePath() throws Exception {
        Path dataFile = temporaryFolder.resolve("elsewhere").resolve("tutorflow.json");
        Files.createDirectories(dataFile);
        setUpLogic(getSampleAddressBook(), new JsonAddressBookStorage(dataFile), SAMPLE_CLOCK,
                Files.createDirectories(temporaryFolder.resolve("app")));

        assertSaveFailureRestores("delete 1", getCommandSaveFailureMessage("student could not be deleted",
                dataFile.toAbsolutePath().normalize().toString(), CAUSE_FOLDER, "student"));
    }

    @Test
    public void execute_readOnlyDataFile_homeworkCommandsReportReadOnlyFile() throws Exception {
        Path dataFile = temporaryFolder.resolve("data").resolve("tutorflow.json");
        setUpLogic(getSampleAddressBook(), new JsonAddressBookStorage(dataFile), SAMPLE_CLOCK);
        logic.execute("edit 6 l/S5");
        Homework homeworkBefore = model.getAddressBook().getStudentList().getFirst().getHomeworks().get(1);
        assertTrue(dataFile.toFile().setWritable(false));
        try {
            assumeFalse(Files.isWritable(dataFile), "the file is still writable, e.g. when running as root");
            assertSaveFailureRestores("hw e 1 2 t/X", getCommandSaveFailureMessage(
                    "homework could not be edited", CAUSE_READ_ONLY_FILE, "homework"));
            assertSaveFailureRestores("hw m 1 2 score/5/10", getCommandSaveFailureMessage(
                    "homework marks could not be recorded", CAUSE_READ_ONLY_FILE, "homework"));
            assertSaveFailureRestores("hw del 1 2", getCommandSaveFailureMessage(
                    "homework could not be deleted", CAUSE_READ_ONLY_FILE, "homework"));
            assertEquals(homeworkBefore, model.getAddressBook().getStudentList().getFirst().getHomeworks().get(1));
        } finally {
            dataFile.toFile().setWritable(true);
        }
    }

    @Test
    public void execute_readOnlyDataFile_studentDeleteReportsReadOnlyFile() throws Exception {
        Path dataFile = temporaryFolder.resolve("data").resolve("tutorflow.json");
        setUpLogic(getSampleAddressBook(), new JsonAddressBookStorage(dataFile), SAMPLE_CLOCK);
        logic.execute("edit 6 l/S5");
        byte[] fileBefore = Files.readAllBytes(dataFile);
        assertTrue(dataFile.toFile().setWritable(false));
        try {
            assumeFalse(Files.isWritable(dataFile), "the file is still writable, e.g. when running as root");
            assertSaveFailureRestores("delete 1", getCommandSaveFailureMessage(
                    "student could not be deleted", CAUSE_READ_ONLY_FILE, "student"));
            assertArrayEquals(fileBefore, Files.readAllBytes(dataFile));
        } finally {
            dataFile.toFile().setWritable(true);
        }
    }

    @Test
    public void execute_unreadableDataFile_reportsUnreadableFile() throws Exception {
        assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"),
                "file permissions only block reads on POSIX file systems");
        Path dataFile = temporaryFolder.resolve("data").resolve("tutorflow.json");
        setUpLogic(getSampleAddressBook(), new JsonAddressBookStorage(dataFile), SAMPLE_CLOCK);
        logic.execute("edit 6 l/S5");
        // PE round 2: chmod 200 data/tutorflow.json with the app open; the cause was only the absolute path
        Files.setPosixFilePermissions(dataFile, PosixFilePermissions.fromString("-w-------"));
        try {
            assumeFalse(Files.isReadable(dataFile), "the file is still readable, e.g. when running as root");
            assertSaveFailureRestores("hw m 1 2 score/5/10", getCommandSaveFailureMessage(
                    "homework marks could not be recorded", "the file cannot be read (no permission)", "homework"));
        } finally {
            Files.setPosixFilePermissions(dataFile, PosixFilePermissions.fromString("rw-------"));
        }
    }

    @Test
    public void execute_readOnlyDataFolder_otherCommandsReportReadOnlyFolder() throws Exception {
        Path dataFile = temporaryFolder.resolve("data").resolve("tutorflow.json");
        setUpLogic(getSampleAddressBook(), new JsonAddressBookStorage(dataFile), SAMPLE_CLOCK);
        logic.execute("edit 6 l/S5");
        byte[] fileBefore = Files.readAllBytes(dataFile);
        makeFolderReadOnly(dataFile.getParent());
        try {
            assertSaveFailureRestores("hw add 1 t/Test s/MATH due/2026-12-01", getCommandSaveFailureMessage(
                    "homework could not be added", CAUSE_READ_ONLY_FOLDER, "homework"));
            assertSaveFailureRestores("hw status 1 2 status/COMPLETED", getCommandSaveFailureMessage(
                    "homework status could not be updated", CAUSE_READ_ONLY_FOLDER, "homework"));
            assertSaveFailureRestores("lesson add 1 s/MATH d/FRIDAY st/10:00 et/11:00", getCommandSaveFailureMessage(
                    "lesson could not be added", CAUSE_READ_ONLY_FOLDER, "lesson"));
            assertSaveFailureRestores("lesson edit 1 st/16:00", getCommandSaveFailureMessage(
                    "lesson could not be edited", CAUSE_READ_ONLY_FOLDER, "lesson"));
            assertSaveFailureRestores("lesson delete 1", getCommandSaveFailureMessage(
                    "lesson could not be deleted", CAUSE_READ_ONLY_FOLDER, "lesson"));
            assertSaveFailureRestores("edit 1 p/91234568", getCommandSaveFailureMessage(
                    "student could not be edited", CAUSE_READ_ONLY_FOLDER, "student"));
            assertArrayEquals(fileBefore, Files.readAllBytes(dataFile));
        } finally {
            makeFolderWritable(dataFile.getParent());
        }
    }

    @Test
    public void execute_clearSaveFails_restoresStudents() {
        setUpLogic(getTypicalAddressBook(), getStorageThatFailsToSave(DUMMY_IO_EXCEPTION));

        assertCommandFailure(ClearCommand.COMMAND_WORD, CommandException.class, DUMMY_IO_ERROR_MESSAGE,
                getModelWithTypicalStudents());
    }

    @Test
    public void execute_deleteSaveFailsAfterSort_keepsDisplayedOrder() throws Exception {
        setUpLogic(getSampleAddressBook(), getStorageThatFailsToSave(DUMMY_IO_EXCEPTION));
        logic.execute("sort by/level o/desc");
        List<Student> displayedBefore = List.copyOf(logic.getStudentList());

        assertCommandException("delete 1", getCommandSaveFailureMessage(
                "student could not be deleted", SET_UP_DATA_FILE_NAME, DUMMY_IO_EXCEPTION, "student"));

        assertEquals(displayedBefore, logic.getStudentList());
    }

    @Test
    public void execute_clearSaveFailsAfterSort_keepsDisplayedOrder() throws Exception {
        setUpLogic(getSampleAddressBook(), getStorageThatFailsToSave(DUMMY_IO_EXCEPTION));
        logic.execute("sort by/level o/desc");
        List<Student> displayedBefore = List.copyOf(logic.getStudentList());

        assertCommandException(ClearCommand.COMMAND_WORD, DUMMY_IO_ERROR_MESSAGE);

        assertEquals(displayedBefore, logic.getStudentList());
    }

    @Test
    public void execute_addSaveFailsAfterFilter_keepsDisplayedStudents() throws Exception {
        setUpLogic(getSampleAddressBook(), getStorageThatFailsToSave(DUMMY_IO_EXCEPTION));
        logic.execute("filter s/MATH");
        logic.execute("sort by/level o/desc");
        List<Student> displayedBefore = List.copyOf(logic.getStudentList());

        // Adding a student shows all students, so a failed save must bring back the filter
        assertCommandException(ADD_STUDENT_COMMAND, DUMMY_IO_ERROR_MESSAGE);

        assertEquals(displayedBefore, logic.getStudentList());
    }

    @Test
    public void execute_homeworkAddSaveFails_rollsBackAddition() {
        setUpLogic(getTypicalAddressBook(), getStorageThatFailsToSave(DUMMY_IO_EXCEPTION));

        assertCommandFailure("hw add 2 t/Trigonometry practice s/MATH due/2026-11-01", CommandException.class,
                getCommandSaveFailureMessage("homework could not be added", SET_UP_DATA_FILE_NAME,
                        DUMMY_IO_EXCEPTION, "homework"), getModelWithTypicalStudents());
    }

    @Test
    public void execute_homeworkDeleteSaveFails_rollsBackDeletion() {
        setUpLogic(getTypicalAddressBook(), getStorageThatFailsToSave(DUMMY_AD_EXCEPTION));

        assertCommandFailure("hw del 1 2", CommandException.class, getCommandSaveFailureMessage(
                "homework could not be deleted", SET_UP_DATA_FILE_NAME, DUMMY_AD_EXCEPTION, "homework"),
                getModelWithTypicalStudents());
    }

    @Test
    public void execute_homeworkEditSaveFails_rollsBackEdit() {
        setUpLogic(getTypicalAddressBook(), getStorageThatFailsToSave(DUMMY_IO_EXCEPTION));

        assertCommandFailure("hw edit 1 2 t/Revise mechanics s/PHYSICS due/2026-11-10", CommandException.class,
                getCommandSaveFailureMessage("homework could not be edited", SET_UP_DATA_FILE_NAME,
                        DUMMY_IO_EXCEPTION, "homework"), getModelWithTypicalStudents());
    }

    @Test
    public void execute_homeworkMarksSaveFails_rollsBackScore() {
        setUpLogic(getTypicalAddressBook(), getStorageThatFailsToSave(DUMMY_AD_EXCEPTION));

        assertCommandFailure("hw marks 1 3 score/0/10", CommandException.class,
                getCommandSaveFailureMessage("homework marks could not be recorded", SET_UP_DATA_FILE_NAME,
                        DUMMY_AD_EXCEPTION, "homework"), getModelWithTypicalStudents());
    }

    @Test
    public void execute_homeworkMarksClearSaveFails_keepsScore() {
        setUpLogic(getTypicalAddressBook(), getStorageThatFailsToSave(DUMMY_AD_EXCEPTION));

        assertCommandFailure("hw m 1 3 score/", CommandException.class,
                getCommandSaveFailureMessage("homework marks could not be cleared", SET_UP_DATA_FILE_NAME,
                        DUMMY_AD_EXCEPTION, "homework"), getModelWithTypicalStudents());
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
        setUpLogic(data, storage, clock, temporaryFolder);
    }

    /**
     * Replaces {@code model} with one holding {@code data}, and {@code logic} with one using {@code storage},
     * taking today's date from {@code clock} and naming the data file relative to {@code homeFolder}.
     */
    private void setUpLogic(ReadOnlyAddressBook data, JsonAddressBookStorage storage, Clock clock,
            Path homeFolder) {
        model = new ModelManager(data, new UserPrefs());
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        logic = new LogicManager(model, new StorageManager(storage, userPrefsStorage), clock, homeFolder);
    }

    /**
     * Executes {@code inputCommand}, whose save fails, and confirms that the message is {@code expectedMessage}
     * and that the data, the displayed students and their order are what they were before.
     */
    private void assertSaveFailureRestores(String inputCommand, String expectedMessage) {
        AddressBook dataBefore = new AddressBook(model.getAddressBook());
        List<Student> displayedBefore = List.copyOf(model.getStudentList());

        assertThrows(CommandException.class, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(dataBefore, model.getAddressBook());
        assertEquals(displayedBefore, model.getStudentList());
    }

    /**
     * Returns the message of a command without its own save-failure message, for a data file shown as
     * {@code shownName} that could not be saved because of {@code cause}.
     */
    private static String getGeneralSaveFailureMessage(String shownName, String cause) {
        return "Could not save " + shownName + ": " + cause + ". Nothing was changed.";
    }

    /**
     * Returns a command's own save-failure message, e.g. "The {@code failure} because TutorFlow could not save
     * data/tutorflow.json: {@code cause}. No {@code dataKind} data was changed."
     */
    private static String getCommandSaveFailureMessage(String failure, String cause, String dataKind) {
        return getCommandSaveFailureMessage(failure, SHOWN_DATA_FILE_NAME, cause, dataKind);
    }

    /**
     * Returns a command's own save-failure message for a save that failed with {@code exception}, which matches no
     * common cause, so its text is the cause.
     */
    private static String getCommandSaveFailureMessage(String failure, String shownName, IOException exception,
            String dataKind) {
        return getCommandSaveFailureMessage(failure, shownName, exception.getMessage(), dataKind);
    }

    private static String getCommandSaveFailureMessage(String failure, String shownName, String cause,
            String dataKind) {
        return "The " + failure + " because TutorFlow could not save " + shownName + ": " + cause + ". No "
                + dataKind + " data was changed.";
    }

    /**
     * Removes write permission from {@code folder}, skipping the test where that does not stop writes into it:
     * on file systems without POSIX permissions, or when running as root.
     */
    private static void makeFolderReadOnly(Path folder) throws IOException {
        assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"),
                "folder permissions only block writes on POSIX file systems");
        Files.setPosixFilePermissions(folder, PosixFilePermissions.fromString("r-xr-xr-x"));
        if (Files.isWritable(folder)) {
            makeFolderWritable(folder);
            assumeFalse(true, "the folder is still writable, e.g. when running as root");
        }
    }

    /**
     * Gives the owner write permission on {@code folder} again, so the temporary folder can be deleted.
     */
    private static void makeFolderWritable(Path folder) throws IOException {
        Files.setPosixFilePermissions(folder, PosixFilePermissions.fromString("rwxr-xr-x"));
    }

    private static List<String> listFileNames(Path folder) throws IOException {
        try (var files = Files.list(folder)) {
            return files.map(file -> file.getFileName().toString()).toList();
        }
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
