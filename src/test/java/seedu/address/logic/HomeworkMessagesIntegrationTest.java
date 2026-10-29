package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CommandParserTestUtil.givenOnce;
import static seedu.address.logic.parser.CommandParserTestUtil.givenOnceWithQuoteHint;
import static seedu.address.logic.parser.CommandParserTestUtil.titleAndAliasWithQuoteHint;
import static seedu.address.logic.parser.CommandParserTestUtil.unexpectedWords;
import static seedu.address.logic.parser.CommandParserTestUtil.wrongCasePrefix;
import static seedu.address.logic.parser.CommandParserTestUtil.wrongCasePrefixWithQuoteHint;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestUtil.ARABIC_EGYPT;
import static seedu.address.testutil.TestUtil.runWithFormatLocale;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.HomeworkAddCommand;
import seedu.address.logic.commands.HomeworkDeleteCommand;
import seedu.address.logic.commands.HomeworkEditCommand;
import seedu.address.logic.commands.HomeworkListCommand;
import seedu.address.logic.commands.HomeworkMarksCommand;
import seedu.address.logic.commands.HomeworkUpcomingCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.homework.Score;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Runs homework commands found in PE testing through {@code LogicManager#execute} with their exact input, and checks
 * the message shown and that rejected commands change and save nothing.
 */
public class HomeworkMessagesIntegrationTest {
    /** Fixed clock at 2026-10-29, the day the PE findings were triaged. */
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-29T00:00:00Z"), ZoneOffset.UTC);

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private Path dataFilePath;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager(new AddressBook(), new UserPrefs());
        dataFilePath = temporaryFolder.resolve("tutorflow.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFilePath),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        logic = new LogicManager(model, storage, CLOCK, temporaryFolder);
        logic.execute("add n/Tan Ah Kow Jr. l/S3 s/MATH, PHYSICS p/91234567 gn/Tan Mei gp/98765432");
        logic.execute("hw add 1 t/Algebra s/MATH due/2026-11-02");
        logic.execute("hw add 1 t/Optics s/PHYSICS due/2026-11-05");
    }

    @Test
    public void execute_indexTooLarge_namesUpperBound() throws Exception {
        assertParseFailure("hw del 1 9999999999", "HOMEWORK_INDEX must be no greater than 2147483647.");
        assertParseFailure("hw e 2147483648 1 t/X", "STUDENT_INDEX must be no greater than 2147483647.");
    }

    @Test
    public void execute_wrongCasePrefix_rejectedInsteadOfSavedInValue() throws Exception {
        assertParseFailure("hw e 1 2 t/X Due/2026-11-11", wrongCasePrefixWithQuoteHint("Due/", "due/",
                "t/\"X Due/2026-11-11\"") + "\nUsage: " + HomeworkEditCommand.MESSAGE_USAGE);
        assertParseFailure("hw add 1 t/A S/MATH due/2026-11-01", wrongCasePrefixWithQuoteHint("S/", "s/",
                "t/\"A S/MATH\"") + "\nUsage: " + HomeworkAddCommand.MESSAGE_USAGE);
        assertParseFailure("hw m 1 2 Score/5", wrongCasePrefix("Score/", "score/") + "\nUsage: "
                + HomeworkMarksCommand.MESSAGE_USAGE);
        assertParseFailure("hw up Due/3", wrongCasePrefix("Due/", "due/") + "\nUsage: "
                + HomeworkUpcomingCommand.MESSAGE_USAGE);
    }

    @Test
    public void execute_repeatedPrefix_saysOnceWithUsage() throws Exception {
        String addUsage = "\nUsage: " + HomeworkAddCommand.MESSAGE_USAGE;
        assertParseFailure("hw add 1 t/Revise t/f questions s/MATH due/2026-12-01",
                givenOnceWithQuoteHint("t/", "t/\"Revise t/f questions\"") + addUsage);
        assertParseFailure("hw add 1 t/Read s/o notes s/MATH due/2026-12-01",
                givenOnceWithQuoteHint("s/", "t/\"Read s/o notes\"") + addUsage);
        assertParseFailure("hw add 1 t/Read title/abstract s/MATH due/2026-12-01",
                titleAndAliasWithQuoteHint("t/\"Read title/abstract\"") + addUsage);
        assertParseFailure("hw add 1 t/a t/b s/MATH due/2026-12-01", givenOnceWithQuoteHint("t/", "t/\"a t/b\"")
                + addUsage);
        assertParseFailure("hw e 1 2 t/X t/Y", givenOnceWithQuoteHint("t/", "t/\"X t/Y\"") + "\nUsage: "
                + HomeworkEditCommand.MESSAGE_USAGE);
        assertParseFailure("hw m 1 2 score/1 score/2", givenOnce("score/") + "\nUsage: "
                + HomeworkMarksCommand.MESSAGE_USAGE);
        assertParseFailure("hw up due/3 due/7", givenOnce("due/") + "\nUsage: "
                + HomeworkUpcomingCommand.MESSAGE_USAGE);
    }

    @Test
    public void execute_unknownOrMissingSubcommand_listsSubcommands() throws Exception {
        String subcommands = "Use add, list|ls, upcoming|up, edit|e, marks|m, delete|del, filter or status.";
        assertParseFailure("hw rm 1 1", "rm is not a homework subcommand. " + subcommands);
        assertParseFailure("hw upcomming", "upcomming is not a homework subcommand. " + subcommands);
        assertParseFailure("hw ls3", "ls3 is not a homework subcommand. " + subcommands);
        assertParseFailure("hw", "Missing homework subcommand. " + subcommands);
        assertParseFailure("homework", "Missing homework subcommand. " + subcommands);
    }

    @Test
    public void execute_wrongWordsOrCase_specificMessage() throws Exception {
        assertParseFailure("hw e 1 2", "Specify at least one field to edit: title/ (or t/), s/ or due/.\nUsage: "
                + HomeworkEditCommand.MESSAGE_USAGE);
        assertParseFailure("hw del 1 1 1", unexpectedWords("STUDENT_INDEX HOMEWORK_INDEX", "3 words (1 1 1)",
                HomeworkDeleteCommand.MESSAGE_USAGE));
        assertParseFailure("hw e 1 2 extra t/X", unexpectedWords("STUDENT_INDEX HOMEWORK_INDEX before the first "
                + "prefix", "3 words (1 2 extra)", HomeworkEditCommand.MESSAGE_USAGE));
        assertParseFailure("hw m 1 score/5", unexpectedWords("STUDENT_INDEX HOMEWORK_INDEX before the first prefix",
                "1 word (1)", HomeworkMarksCommand.MESSAGE_USAGE));
        assertParseFailure("hw ls 1 2", unexpectedWords("STUDENT_INDEX, optionally followed by all",
                "2 words (1 2)", HomeworkListCommand.MESSAGE_USAGE));
        assertParseFailure("hw add 1 extra t/A s/MATH due/2026-11-01", unexpectedWords("STUDENT_INDEX before the "
                + "first prefix", "2 words (1 extra)", HomeworkAddCommand.MESSAGE_USAGE));
        assertParseFailure("hw add t/Essay s/MATH due/2026-11-20 1", unexpectedWords("STUDENT_INDEX before the "
                + "first prefix", "none", HomeworkAddCommand.MESSAGE_USAGE));
        assertParseFailure("hw up 3", unexpectedWords("no words other than due/DAYS", "1 word (3)",
                HomeworkUpcomingCommand.MESSAGE_USAGE));
        assertParseFailure("hw E 1 2 t/X", "Homework subcommands are lowercase: type e, not E.\nUsage: "
                + HomeworkEditCommand.MESSAGE_USAGE);
        assertParseFailure("hw ls 1 ALL", "The word all is lowercase: type all, not ALL.\nUsage: "
                + HomeworkListCommand.MESSAGE_USAGE);
    }

    @Test
    public void execute_invalidScore_namesFailedRule() throws Exception {
        assertParseFailure("hw m 1 2 score/11/10", "Marks obtained (11) must not be more than the total marks (10).");
        assertParseFailure("hw m 1 2 score/5/0", Score.MESSAGE_TOTAL_TOO_SMALL);
        assertParseFailure("hw m 1 2 score/0/0", Score.MESSAGE_TOTAL_TOO_SMALL);
        for (String invalidFormat : new String[] {"-1", "abc", "50.5", "1e2", "+5", "\uFF15", "5/10/20"}) {
            assertParseFailure("hw m 1 2 score/" + invalidFormat, Score.MESSAGE_INVALID_FORMAT);
        }
    }

    @Test
    public void execute_invalidTitle_namesLengthOrCharacter() throws Exception {
        assertParseFailure("hw add 1 t/Love \u2764\uFE0F s/MATH due/2026-11-20", "Homework title has an unsupported "
                + "character: U+FE0F (VARIATION SELECTOR-16). Titles may contain only letters, numbers, punctuation, "
                + "symbols and spaces.");
        assertParseFailure("hw add 1 t/" + "a".repeat(101) + " s/MATH due/2026-11-20",
                "Homework title must be at most 100 characters, but has 101.");
    }

    @Test
    public void execute_nameEndingInPeriod_noDoublePeriod() throws Exception {
        assertEquals("Listed 2 homework records for Tan Ah Kow Jr. (91234567).\n"
                + "Homework for Tan Ah Kow Jr. (91234567):\n"
                + "1. Algebra (MATH) - due 2026-11-02 - score Not recorded - status ASSIGNED\n"
                + "2. Optics (PHYSICS) - due 2026-11-05 - score Not recorded - status ASSIGNED", execute("hw "
                + "ls 1 all"));
        assertEquals("Edited homework:\n"
                + "Tan Ah Kow Jr. (91234567); Title: Algebra drill; Subject: MATH; Due: 2026-11-02\n"
                + "Listed 2 upcoming homework records for Tan Ah Kow Jr. (91234567).\n"
                + "Homework for Tan Ah Kow Jr. (91234567):\n"
                + "1. Algebra drill (MATH) - due 2026-11-02 - score Not recorded - status ASSIGNED\n"
                + "2. Optics (PHYSICS) - due 2026-11-05 - score Not recorded - status ASSIGNED",
                execute("hw e 1 1 t/Algebra drill"));
        execute("hw del 1 1");
        assertEquals("Deleted homework:\n"
                + "1. Optics (PHYSICS) - due 2026-11-05 - score Not recorded - status ASSIGNED\n"
                + "No homework found for Tan Ah Kow Jr. (91234567).", execute("hw del 1 1"));
    }

    @Test
    public void execute_sameNameStudents_toldApartByPhone() throws Exception {
        execute("add n/Ethan Lim l/S1 s/MATH p/81234567 gn/Lim Mei gp/81234568");
        execute("add n/Ethan Lim l/S1 s/MATH p/81234569 gn/Lim Mei gp/81234568");

        assertEquals("New homework added for Ethan Lim (81234569):\n"
                + "1. Algebra drill (MATH) - due 2026-11-20 - score Not recorded - status ASSIGNED",
                execute("hw add 3 t/Algebra drill s/MATH due/2026-11-20"));
        assertEquals("No homework found for Ethan Lim (81234567).", execute("hw ls 2"));
        assertEquals("Recorded homework marks:\n"
                + "Ethan Lim (81234569); Title: Algebra drill; Subject: MATH; Due: 2026-11-20; Score: 5/10",
                execute("hw m 3 1 score/5/10"));
        assertEquals("Found 3 homework records due from 2026-10-29 to 2026-11-28.\n"
                + "Student 1: Tan Ah Kow Jr. (91234567); Algebra; MATH; Due: 2026-11-02; Score: Not recorded; "
                + "Status: ASSIGNED\n"
                + "Student 1: Tan Ah Kow Jr. (91234567); Optics; PHYSICS; Due: 2026-11-05; Score: Not recorded; "
                + "Status: ASSIGNED\n"
                + "Student 3: Ethan Lim (81234569); Algebra drill; MATH; Due: 2026-11-20; Score: 5/10; "
                + "Status: ASSIGNED", execute("hw up due/30"));
    }

    @Test
    public void execute_addDueBeforeToday_notesHowToListIt() throws Exception {
        execute("add n/Ethan Lim l/S1 s/MATH p/81234567 gn/Lim Mei gp/81234568");

        assertEquals("New homework added for Ethan Lim (81234567):\n"
                + "1. d10 (MATH) - due 2020-01-01 - score Not recorded - status ASSIGNED\n"
                + "Note: this homework is due before today, so homework list 2 shows it only with all.",
                execute("hw add 2 t/d10 s/MATH due/2020-01-01"));
        assertEquals("No upcoming homework found for Ethan Lim (81234567) (1 record due before today not shown; "
                + "use homework list 2 all to see it).", execute("hw ls 2"));
        assertEquals("Listed 1 homework record for Ethan Lim (81234567).\n"
                + "Homework for Ethan Lim (81234567):\n"
                + "1. d10 (MATH) - due 2020-01-01 - score Not recorded - status ASSIGNED", execute("hw ls 2 all"));
    }

    @Test
    public void execute_nearDuplicateTitles_bothAdded() throws Exception {
        // titles that differ by a space or a period are different homework
        assertEquals("New homework added for Tan Ah Kow Jr. (91234567):\n"
                + "3. Worksheet1 (MATH) - due 2026-11-20 - score Not recorded - status ASSIGNED",
                execute("hw add 1 t/Worksheet1 s/MATH due/2026-11-20"));
        assertEquals("New homework added for Tan Ah Kow Jr. (91234567):\n"
                + "4. Worksheet 1 (MATH) - due 2026-11-20 - score Not recorded - status ASSIGNED",
                execute("hw add 1 t/Worksheet 1 s/MATH due/2026-11-20"));
    }

    @Test
    public void execute_prefixGluedToTitle_keptInTitle() throws Exception {
        assertEquals("Edited homework:\n"
                + "Tan Ah Kow Jr. (91234567); Title: Xs/MATH; Subject: PHYSICS; Due: 2026-11-05\n"
                + "Listed 2 upcoming homework records for Tan Ah Kow Jr. (91234567).\n"
                + "Homework for Tan Ah Kow Jr. (91234567):\n"
                + "1. Algebra (MATH) - due 2026-11-02 - score Not recorded - status ASSIGNED\n"
                + "2. Xs/MATH (PHYSICS) - due 2026-11-05 - score Not recorded - status ASSIGNED",
                execute("hw e 1 2 t/Xs/MATH"));
    }

    @Test
    public void execute_semicolonInTitle_keptInUpcomingLine() throws Exception {
        assertEquals("New homework added for Tan Ah Kow Jr. (91234567):\n"
                + "3. Read ch 1; ch 2 (MATH) - due 2026-11-03 - score Not recorded - status ASSIGNED",
                execute("hw add 1 t/Read ch 1; ch 2 s/MATH due/2026-11-03"));
        assertEquals("Found 3 homework records due from 2026-10-29 to 2026-11-05.\n"
                + "Student 1: Tan Ah Kow Jr. (91234567); Algebra; MATH; Due: 2026-11-02; Score: Not recorded; "
                + "Status: ASSIGNED\n"
                + "Student 1: Tan Ah Kow Jr. (91234567); Read ch 1; ch 2; MATH; Due: 2026-11-03; "
                + "Score: Not recorded; Status: ASSIGNED\n"
                + "Student 1: Tan Ah Kow Jr. (91234567); Optics; PHYSICS; Due: 2026-11-05; Score: Not recorded; "
                + "Status: ASSIGNED", execute("hw up"));
    }

    @Test
    public void execute_arabicDigitsLocale_upcomingListCountInAsciiDigits() throws Exception {
        execute("hw add 1 t/Old s/MATH due/2026-01-01");

        runWithFormatLocale(ARABIC_EGYPT, () -> assertEquals("Listed 2 upcoming homework records for "
                + "Tan Ah Kow Jr. (91234567) (1 record due before today not shown; use homework list 1 all to see "
                + "it).\n"
                + "Homework for Tan Ah Kow Jr. (91234567):\n"
                + "1. Algebra (MATH) - due 2026-11-02 - score Not recorded - status ASSIGNED\n"
                + "2. Optics (PHYSICS) - due 2026-11-05 - score Not recorded - status ASSIGNED",
                assertDoesNotThrow(() -> execute("hw ls 1"))));
    }

    @Test
    public void execute_shortDateYesterday_infersNextYear() throws Exception {
        // today is 2026-10-29
        assertEquals("New homework added for Tan Ah Kow Jr. (91234567):\n"
                + "3. X (MATH) - due 2027-10-28 - score Not recorded - status ASSIGNED\n"
                + "Inferred year: 2027", execute("hw add 1 t/X s/MATH due/10-28"));
    }

    @Test
    public void execute_wrongCasePrefixInStudentName_keptInName() throws Exception {
        logic.execute("add n/Ravi S/O Kumar l/S3 s/MATH p/91234568 gn/Kumar Devi gp/98765433");

        assertEquals("Ravi S/O Kumar", model.getStudentList().get(1).getName().toString());
        assertTrue(Files.readString(dataFilePath).contains("Ravi S/O Kumar"));
    }

    /**
     * Returns the message shown after executing {@code commandText}.
     */
    private String execute(String commandText) throws Exception {
        return logic.execute(commandText).getFeedbackToUser();
    }

    /**
     * Executes {@code commandText} and checks that it fails to parse with {@code expectedMessage}, leaving the model
     * and the data file unchanged.
     */
    private void assertParseFailure(String commandText, String expectedMessage) throws Exception {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        byte[] savedData = Files.readAllBytes(dataFilePath);
        assertThrows(ParseException.class, expectedMessage, () -> logic.execute(commandText));
        assertEquals(expectedModel, model);
        assertArrayEquals(savedData, Files.readAllBytes(dataFilePath));
    }
}
