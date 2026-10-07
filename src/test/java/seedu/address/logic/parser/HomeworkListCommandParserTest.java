package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.HomeworkListCommand;

public class HomeworkListCommandParserTest {
    private static final String MESSAGE_INVALID_FORMAT = "Invalid command format.\n"
            + "Usage: homework|hw list|ls STUDENT_INDEX";

    private final HomeworkListCommandParser parser = new HomeworkListCommandParser();

    @Test
    public void parse_validIndex_success() {
        assertParseSuccess(parser, "1", new HomeworkListCommand(Index.fromOneBased(1)));
    }

    @Test
    public void parse_multiDigitIndexWithWhitespace_success() {
        assertParseSuccess(parser, "  12  ", new HomeworkListCommand(Index.fromOneBased(12)));
    }

    @Test
    public void parse_missingIndex_failure() {
        assertParseFailure(parser, "  ", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_extraArgument_failure() {
        assertParseFailure(parser, "1 2", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_prefixedIndex_failure() {
        assertParseFailure(parser, "s/1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_indexWithPrefix_failure() {
        assertParseFailure(parser, "1 s/MATH", MESSAGE_INVALID_FORMAT);
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
        assertParseFailure(parser, "2147483648", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);
    }
}
