package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.CommandParserTestUtil.notPrefix;
import static seedu.address.logic.parser.CommandParserTestUtil.notPrefixWithQuoteHint;
import static seedu.address.logic.parser.CommandParserTestUtil.unexpectedWords;
import static seedu.address.logic.parser.CommandParserTestUtil.wrongCasePrefix;
import static seedu.address.logic.parser.HomeworkUpcomingCommandParser.MESSAGE_INVALID_DAYS;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.HomeworkUpcomingCommand;

public class HomeworkUpcomingCommandParserTest {
    /** Fixed clock at 2026-10-17, so today never changes. */
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-17T00:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 17);
    private static final String USAGE = "homework|hw upcoming|up [due/DAYS]";
    private static final String EXPECTED_WORDS = "no words other than due/DAYS";

    private final HomeworkUpcomingCommandParser parser = new HomeworkUpcomingCommandParser(CLOCK);

    @Test
    public void parse_manyQuotes_rejectedQuickly() {
        // Issue #154: `hw up due/` followed by quotes; 1,000,000 rather than the issue's 200,000 so that a quadratic
        // scan exceeds the limit on fast machines too
        String arguments = " due/" + "\"".repeat(1_000_000);

        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> assertParseFailure(parser, arguments,
                MESSAGE_INVALID_DAYS));
    }

    @Test
    public void parse_noArguments_usesDefaultDays() {
        assertParseSuccess(parser, "", new HomeworkUpcomingCommand(TODAY, 7));
        assertParseSuccess(parser, "   ", new HomeworkUpcomingCommand(TODAY, 7));
    }

    @Test
    public void parse_validDays_success() {
        assertParseSuccess(parser, "due/3", new HomeworkUpcomingCommand(TODAY, 3));
        assertParseSuccess(parser, "due/7", new HomeworkUpcomingCommand(TODAY, 7));
        assertParseSuccess(parser, "due/42", new HomeworkUpcomingCommand(TODAY, 42));
    }

    @Test
    public void parse_boundaryDays_success() {
        // today only
        assertParseSuccess(parser, "due/0", new HomeworkUpcomingCommand(TODAY, 0));

        // largest number of days
        assertParseSuccess(parser, "due/365", new HomeworkUpcomingCommand(TODAY, 365));
    }

    @Test
    public void parse_extraWhitespace_success() {
        assertParseSuccess(parser, "   due/   3   ", new HomeworkUpcomingCommand(TODAY, 3));
    }

    @Test
    public void parse_quotedDays_success() {
        assertParseSuccess(parser, "due/\"3\"", new HomeworkUpcomingCommand(TODAY, 3));
        assertParseSuccess(parser, " due/  \"0\"  ", new HomeworkUpcomingCommand(TODAY, 0));

        // spaces at both ends inside the quotes are removed
        assertParseSuccess(parser, "due/\" 3 \"", new HomeworkUpcomingCommand(TODAY, 3));
    }

    @Test
    public void parse_invalidQuotedDays_failure() {
        // empty quotes, rejected like an empty unquoted value
        assertParseFailure(parser, "due/\"\"", MESSAGE_INVALID_DAYS);

        // only spaces inside the quotes
        assertParseFailure(parser, "due/\" \"", MESSAGE_INVALID_DAYS);

        // spaces between digits are kept
        assertParseFailure(parser, "due/\"1 0\"", MESSAGE_INVALID_DAYS);
    }

    @Test
    public void parse_invalidDays_failure() {
        // one above the largest number of days
        assertParseFailure(parser, "due/366", MESSAGE_INVALID_DAYS);
        assertParseFailure(parser, "due/400", MESSAGE_INVALID_DAYS);

        // larger than the largest int
        assertParseFailure(parser, "due/2147483648", MESSAGE_INVALID_DAYS);

        // leading zeroes
        assertParseFailure(parser, "due/007", MESSAGE_INVALID_DAYS);
        assertParseFailure(parser, "due/07", MESSAGE_INVALID_DAYS);
        assertParseFailure(parser, "due/00", MESSAGE_INVALID_DAYS);

        // negative
        assertParseFailure(parser, "due/-1", MESSAGE_INVALID_DAYS);

        // plus sign
        assertParseFailure(parser, "due/+1", MESSAGE_INVALID_DAYS);

        // decimal
        assertParseFailure(parser, "due/1.5", MESSAGE_INVALID_DAYS);

        // word
        assertParseFailure(parser, "due/seven", MESSAGE_INVALID_DAYS);

        // date instead of days
        assertParseFailure(parser, "due/2026-10-20", MESSAGE_INVALID_DAYS);
        assertParseFailure(parser, "due/2026-11-01", MESSAGE_INVALID_DAYS);

        // internal space
        assertParseFailure(parser, "due/1 0", MESSAGE_INVALID_DAYS);
    }

    @Test
    public void parse_emptyDays_failure() {
        assertParseFailure(parser, "due/", MESSAGE_INVALID_DAYS);
        assertParseFailure(parser, "due/   ", MESSAGE_INVALID_DAYS);
    }

    @Test
    public void parse_repeatedPrefix_failure() {
        assertParseFailure(parser, "due/3 due/7", "Prefix due/ must be specified exactly once.");
    }

    @Test
    public void parse_unknownPrefix_failure() {
        String usageLine = "\nUsage: " + HomeworkUpcomingCommand.MESSAGE_USAGE;
        assertParseFailure(parser, "d/3", notPrefix("d/") + usageLine);
        // the days before s/ are only digits, so no example is given
        assertParseFailure(parser, "due/3 s/MATH", notPrefixWithQuoteHint("s/") + usageLine);
    }

    @Test
    public void parse_uppercasePrefix_failure() {
        String usageLine = "\nUsage: " + HomeworkUpcomingCommand.MESSAGE_USAGE;
        assertParseFailure(parser, "DUE/3", wrongCasePrefix("DUE/", "due/") + usageLine);
        assertParseFailure(parser, "Due/3", wrongCasePrefix("Due/", "due/") + usageLine);
    }

    @Test
    public void parse_extraPreamble_failure() {
        // days without the prefix
        assertParseFailure(parser, "3", unexpectedWords(EXPECTED_WORDS, "1 word (3)", USAGE));

        // text before the prefix
        assertParseFailure(parser, "1 due/3", unexpectedWords(EXPECTED_WORDS, "1 word (1)", USAGE));
        assertParseFailure(parser, "all", unexpectedWords(EXPECTED_WORDS, "1 word (all)", USAGE));
        assertParseFailure(parser, "extra", unexpectedWords(EXPECTED_WORDS, "1 word (extra)", USAGE));
    }
}
