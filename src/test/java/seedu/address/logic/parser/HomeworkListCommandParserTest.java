package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.CommandParserTestUtil.notPrefix;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.HomeworkListCommand;

public class HomeworkListCommandParserTest {
    /** Fixed clock at 2026-10-05, so today never changes. */
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-05T00:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 5);
    private static final String USAGE_LINE = "\nUsage: " + HomeworkListCommand.MESSAGE_USAGE;
    private static final String MESSAGE_INVALID_FORMAT = "Invalid command format.\n"
            + "Usage: homework|hw list|ls STUDENT_INDEX [all]";

    private final HomeworkListCommandParser parser = new HomeworkListCommandParser(CLOCK);

    @Test
    public void parse_validIndex_listsUpcomingHomework() {
        assertParseSuccess(parser, "1", new HomeworkListCommand(Index.fromOneBased(1), TODAY, false));
    }

    @Test
    public void parse_multiDigitIndexWithWhitespace_success() {
        assertParseSuccess(parser, "  12  ", new HomeworkListCommand(Index.fromOneBased(12), TODAY, false));
    }

    @Test
    public void parse_indexAndAll_listsAllHomework() {
        assertParseSuccess(parser, "1 all", new HomeworkListCommand(Index.fromOneBased(1), TODAY, true));
        assertParseSuccess(parser, "  2   all  ", new HomeworkListCommand(Index.fromOneBased(2), TODAY, true));
    }

    @Test
    public void parse_missingIndex_failure() {
        assertParseFailure(parser, "  ", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_allWithoutIndex_failure() {
        // all is not a valid index
        assertParseFailure(parser, "all", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);
    }

    @Test
    public void parse_extraArgument_failure() {
        assertParseFailure(parser, "1 2", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 all all", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "all 1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_allInOtherCase_failure() {
        assertParseFailure(parser, "1 ALL", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 All", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_prefixedIndex_failure() {
        assertParseFailure(parser, "s/1", notPrefix("s/") + USAGE_LINE);
    }

    @Test
    public void parse_indexWithPrefix_failure() {
        assertParseFailure(parser, "1 s/MATH", notPrefix("s/") + USAGE_LINE);
        assertParseFailure(parser, "1 all/", notPrefix("all/") + USAGE_LINE);
        assertParseFailure(parser, "1 x/all", notPrefix("x/") + USAGE_LINE);
    }

    @Test
    public void parse_invalidIndex_failure() {
        // zero
        assertParseFailure(parser, "0", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // leading zero
        assertParseFailure(parser, "01", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // negative
        assertParseFailure(parser, "-1", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // plus sign
        assertParseFailure(parser, "+1", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // decimal
        assertParseFailure(parser, "1.0", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // word
        assertParseFailure(parser, "one", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // larger than the largest int
        assertParseFailure(parser, "2147483648", Messages.MESSAGE_STUDENT_INDEX_TOO_LARGE);
        assertParseFailure(parser, "9999999999", Messages.MESSAGE_STUDENT_INDEX_TOO_LARGE);

        // invalid index followed by all
        assertParseFailure(parser, "0 all", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);
    }

    @Test
    public void parse_largestIntIndex_success() {
        assertParseSuccess(parser, "2147483647",
                new HomeworkListCommand(Index.fromOneBased(Integer.MAX_VALUE), TODAY, false));
    }
}
