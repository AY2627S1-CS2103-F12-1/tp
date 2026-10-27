package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.CommandParserTestUtil.notPrefix;
import static seedu.address.logic.parser.CommandParserTestUtil.notPrefixWithQuoteHint;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SortCommand;
import seedu.address.model.student.SortKey;
import seedu.address.model.student.SortOrder;

public class SortCommandParserTest {
    private final SortCommandParser parser = new SortCommandParser();

    @Test
    public void parse_validOptions_acceptsEitherOrderCaseWhitespaceAndQuotes() throws Exception {
        SortCommand expected = new SortCommand(SortKey.LEVEL, SortOrder.DESCENDING);
        for (String input : List.of("by/level o/desc", " o/desc by/level ", "by/ LEVEL  o/ DeSc ",
                "by/\" level \" o/\"DESC\"", "o/desc\tby/level")) {
            assertParseSuccess(parser, input, expected);
        }
        assertParseSuccess(parser, "by/subject", new SortCommand(SortKey.SUBJECT, SortOrder.ASCENDING));
        assertParseSuccess(parser, "by/day o/asc", new SortCommand(SortKey.DAY, SortOrder.ASCENDING));
        assertEquals(expected, new AddressBookParser().parseCommand("sort by/level o/desc"));
    }

    @Test
    public void parse_missingKey_reportsExactMissingKeyMessage() {
        for (String input : List.of("", "  ", "o/asc", "o/desc")) {
            assertParseFailure(parser, input, SortCommand.MESSAGE_MISSING_KEY);
        }
    }

    @Test
    public void parse_repeatedPrefix_reportsCommandSpecificMessage() {
        for (String input : List.of("by/level by/day", "by/ by/level", "by/day o/asc o/desc",
                "o/asc by/day o/asc", "by/\"day\" by/level")) {
            assertParseFailure(parser, input, SortCommand.MESSAGE_REPEATED_PREFIX);
        }
    }

    @Test
    public void parse_unknownPrefix_namesPrefix() {
        // the usage constant already starts with "Usage: "
        String usageLine = "\n" + SortCommand.MESSAGE_USAGE;
        assertParseFailure(parser, "by/level x/value", notPrefixWithQuoteHint("x/", "by/\"level x/value\"")
                + usageLine);
        assertParseFailure(parser, "x/day by/level", notPrefix("x/") + usageLine);
        assertParseFailure(parser, "by/level extra/value", notPrefixWithQuoteHint("extra/",
                "by/\"level extra/value\"") + usageLine);
    }

    @Test
    public void parse_preambleOrUnprefixedWord_reportsInvalidFormat() {
        for (String input : List.of("extra by/level", "level", "by/level X/value", "by/level x1/value", "BY/level")) {
            assertParseFailure(parser, input, SortCommand.MESSAGE_INVALID_FORMAT);
        }
    }

    @Test
    public void parse_emptyOrInvalidKey_reportsExactKeyMessage() {
        for (String input : List.of("by/", "by/ o/asc", "by/name", "by/level day", "by/\" \"")) {
            assertParseFailure(parser, input, SortCommand.MESSAGE_INVALID_KEY);
        }
    }

    @Test
    public void parse_emptyOrInvalidOrder_reportsExactOrderMessage() {
        for (String input : List.of("by/day o/", "o/ by/day", "by/day o/ascending", "by/day o/\"\"")) {
            assertParseFailure(parser, input, SortCommand.MESSAGE_INVALID_ORDER);
        }
    }
}
