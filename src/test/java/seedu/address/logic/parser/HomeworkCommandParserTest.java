package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.CommandParserTestUtil.unexpectedWords;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.HomeworkAddCommand;
import seedu.address.logic.commands.HomeworkDeleteCommand;
import seedu.address.logic.commands.HomeworkEditCommand;
import seedu.address.logic.commands.HomeworkEditCommand.EditHomeworkDescriptor;
import seedu.address.logic.commands.HomeworkListCommand;
import seedu.address.logic.commands.HomeworkMarksCommand;
import seedu.address.logic.commands.HomeworkUpcomingCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.homework.Score;
import seedu.address.model.student.TuitionSubject;

public class HomeworkCommandParserTest {
    /** Fixed clock at 2026-10-05, so short-form due dates always infer the same year and today never changes. */
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-05T00:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 5);
    private static final String SUBCOMMANDS =
            "Use add, list|ls, upcoming|up, edit|e, marks|m, delete|del, filter or status.";

    private final HomeworkCommandParser parser = new HomeworkCommandParser(CLOCK);

    @Test
    public void parse_add_returnsHomeworkAddCommand() {
        assertParseSuccess(parser, " add 1 title/Complete algebra worksheet s/MATH due/2026-10-15",
                new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, false, TODAY));
    }

    @Test
    public void parse_addShortDueDate_usesGivenClock() {
        assertParseSuccess(parser, " add 1 t/Complete algebra worksheet s/MATH due/10-15",
                new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, true, TODAY));
    }

    @Test
    public void parse_list_returnsHomeworkListCommand() {
        assertParseSuccess(parser, " list 1", new HomeworkListCommand(Index.fromOneBased(1), TODAY, false));
    }

    @Test
    public void parse_listAlias_returnsHomeworkListCommand() {
        assertParseSuccess(parser, " ls 5 all", new HomeworkListCommand(Index.fromOneBased(5), TODAY, true));
    }

    @Test
    public void parse_upcoming_returnsHomeworkUpcomingCommand() {
        assertParseSuccess(parser, " upcoming due/3", new HomeworkUpcomingCommand(TODAY, 3));
    }

    @Test
    public void parse_upcomingAlias_returnsHomeworkUpcomingCommand() {
        assertParseSuccess(parser, " up", new HomeworkUpcomingCommand(TODAY, 7));
    }

    @Test
    public void parse_edit_returnsHomeworkEditCommand() {
        assertParseSuccess(parser, " edit 1 2 s/MATH", new HomeworkEditCommand(Index.fromOneBased(1),
                Index.fromOneBased(2), new EditHomeworkDescriptor(null, TuitionSubject.MATH, null), false, TODAY));
    }

    @Test
    public void parse_editAliasShortDueDate_usesGivenClock() {
        assertParseSuccess(parser, " e 2 1 due/10-15", new HomeworkEditCommand(Index.fromOneBased(2),
                Index.fromOneBased(1), new EditHomeworkDescriptor(null, null, ALGEBRA.getDueDate()), true, TODAY));
    }

    @Test
    public void parse_marks_returnsHomeworkMarksCommand() {
        assertParseSuccess(parser, " marks 3 2 score/0/10",
                new HomeworkMarksCommand(Index.fromOneBased(3), Index.fromOneBased(2), new Score(0, 10)));
    }

    @Test
    public void parse_marksAlias_returnsHomeworkMarksCommand() {
        assertParseSuccess(parser, " m 1 2 score/5/10",
                new HomeworkMarksCommand(Index.fromOneBased(1), Index.fromOneBased(2), new Score(5, 10)));
    }

    @Test
    public void parse_delete_returnsHomeworkDeleteCommand() {
        assertParseSuccess(parser, " delete 1 2",
                new HomeworkDeleteCommand(Index.fromOneBased(1), Index.fromOneBased(2), TODAY));
    }

    @Test
    public void parse_deleteAlias_returnsHomeworkDeleteCommand() {
        assertParseSuccess(parser, " del 3 1",
                new HomeworkDeleteCommand(Index.fromOneBased(3), Index.fromOneBased(1), TODAY));
    }

    @Test
    public void parse_missingSubcommand_failure() {
        assertParseFailure(parser, "", "Missing homework subcommand. " + SUBCOMMANDS);
        assertParseFailure(parser, "   ", "Missing homework subcommand. " + SUBCOMMANDS);
    }

    @Test
    public void parse_unknownSubcommand_failure() {
        assertParseFailure(parser, " remove 1 2", "remove is not a homework subcommand. " + SUBCOMMANDS);
        assertParseFailure(parser, " rm 1 1", "rm is not a homework subcommand. " + SUBCOMMANDS);
        assertParseFailure(parser, " d 1 1", "d is not a homework subcommand. " + SUBCOMMANDS);
        assertParseFailure(parser, " upcomming", "upcomming is not a homework subcommand. " + SUBCOMMANDS);
        assertParseFailure(parser, " 1", "1 is not a homework subcommand. " + SUBCOMMANDS);
        // not a subcommand in any case
        assertParseFailure(parser, " Rm 1 1", "Rm is not a homework subcommand. " + SUBCOMMANDS);
    }

    @Test
    public void parse_uppercaseSubcommand_failure() {
        assertParseFailure(parser, " LIST 1", "Homework subcommands are lowercase: type list, not LIST.\n"
                + "Usage: " + HomeworkListCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " LS 1", "Homework subcommands are lowercase: type ls, not LS.\n"
                + "Usage: " + HomeworkListCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " E 1 2 t/X", "Homework subcommands are lowercase: type e, not E.\n"
                + "Usage: " + HomeworkEditCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " DEL 1 1", "Homework subcommands are lowercase: type del, not DEL.\n"
                + "Usage: " + HomeworkDeleteCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " Upcoming", "Homework subcommands are lowercase: type upcoming, not Upcoming.\n"
                + "Usage: " + HomeworkUpcomingCommand.MESSAGE_USAGE);
    }

    @Test
    public void checkCommandWordCase_homeworkWordInOtherCase_throwsParseException() {
        assertThrows(ParseException.class, "Command words are lowercase: type hw, not hW.", () ->
                HomeworkCommandParser.checkCommandWordCase("hW"));
        assertThrows(ParseException.class, "Command words are lowercase: type homework, not HomeWork.", () ->
                HomeworkCommandParser.checkCommandWordCase("HomeWork"));
    }

    @Test
    public void checkCommandWordCase_otherWord_doesNotThrow() {
        assertDoesNotThrow(() -> HomeworkCommandParser.checkCommandWordCase("LIST"));
        assertDoesNotThrow(() -> HomeworkCommandParser.checkCommandWordCase("homeworks"));
        assertDoesNotThrow(() -> HomeworkCommandParser.checkCommandWordCase("H"));
    }

    @Test
    public void parse_subcommandJoinedToArgument_failure() {
        assertParseFailure(parser, " list1", "list1 is not a homework subcommand. " + SUBCOMMANDS);
        assertParseFailure(parser, " ls3", "ls3 is not a homework subcommand. " + SUBCOMMANDS);
        assertParseFailure(parser, " del3 1", "del3 is not a homework subcommand. " + SUBCOMMANDS);
        assertParseFailure(parser, " add1 t/A s/MATH due/2026-12-01", "add1 is not a homework subcommand. "
                + SUBCOMMANDS);
        assertParseFailure(parser, " up7", "up7 is not a homework subcommand. " + SUBCOMMANDS);
    }

    @Test
    public void parse_invalidSubcommandArguments_reportsSubcommandError() {
        assertParseFailure(parser, " list", unexpectedWords("STUDENT_INDEX, optionally followed by all", "none",
                HomeworkListCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidUpcomingArguments_reportsSubcommandError() {
        assertParseFailure(parser, " up 3", unexpectedWords("no words other than due/DAYS", "1 word (3)",
                HomeworkUpcomingCommand.MESSAGE_USAGE));
        assertParseFailure(parser, " up extra", unexpectedWords("no words other than due/DAYS", "1 word (extra)",
                HomeworkUpcomingCommand.MESSAGE_USAGE));
    }
}
