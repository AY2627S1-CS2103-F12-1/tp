package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.LessonDeleteCommand;

public class LessonDeleteCommandParserTest {
    private static final String MESSAGE_INVALID_FORMAT = "Invalid command format.\nUsage: lesson delete LESSON_INDEX";

    private final LessonDeleteCommandParser parser = new LessonDeleteCommandParser();

    @Test
    public void parse_validIndex_success() {
        assertParseSuccess(parser, "2", new LessonDeleteCommand(Index.fromOneBased(2)));
        assertParseSuccess(parser, "  13  ", new LessonDeleteCommand(Index.fromOneBased(13)));
    }

    @Test
    public void parse_invalidArguments_failure() {
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 2", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "d/1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidIndex_failure() {
        for (String invalidIndex : new String[]{"0", "01", "-1", "+1", "1.0", "one", "2147483648"}) {
            assertParseFailure(parser, invalidIndex, Messages.MESSAGE_INVALID_LESSON_INDEX_SYNTAX);
        }
    }
}
