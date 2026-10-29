package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CommandParserTestUtil.unexpectedWords;
import static seedu.address.logic.parser.CommandParserTestUtil.wrongCasePrefix;
import static seedu.address.logic.parser.CommandParserTestUtil.wrongCasePrefixWithQuoteHint;
import static seedu.address.testutil.Assert.assertThrows;

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
        assertParseFailure("hw del 1 9999999999", Messages.MESSAGE_HOMEWORK_INDEX_TOO_LARGE);
        assertParseFailure("hw e 2147483648 1 t/X", Messages.MESSAGE_STUDENT_INDEX_TOO_LARGE);
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
    public void execute_wrongCasePrefixInStudentName_keptInName() throws Exception {
        logic.execute("add n/Ravi S/O Kumar l/S3 s/MATH p/91234568 gn/Kumar Devi gp/98765433");

        assertEquals("Ravi S/O Kumar", model.getStudentList().get(1).getName().toString());
        assertTrue(Files.readString(dataFilePath).contains("Ravi S/O Kumar"));
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
