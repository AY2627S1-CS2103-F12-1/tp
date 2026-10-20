package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

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
    public void parse_unknownPrefixOrPreamble_reportsInvalidFormat() {
        for (String input : List.of("extra by/level", "level", "by/level x/value", "x/day by/level",
                "by/level extra/value", "by/level X/value", "by/level x1/value", "BY/level")) {
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
