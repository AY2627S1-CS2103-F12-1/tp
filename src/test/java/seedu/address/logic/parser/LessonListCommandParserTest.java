package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.LessonListCommand;

public class LessonListCommandParserTest {
    private static final String MESSAGE_INVALID_FORMAT = "Invalid command format.\nUsage: "
            + LessonListCommand.MESSAGE_USAGE;

    private final LessonListCommandParser parser = new LessonListCommandParser();

    @Test
    public void parse_noArguments_success() {
        assertParseSuccess(parser, "   ", new LessonListCommand());
    }

    @Test
    public void parse_studentIndex_success() {
        assertParseSuccess(parser, " 2 ", new LessonListCommand(Index.fromOneBased(2)));
    }

    @Test
    public void parse_invalidArgument_failure() {
        for (String value : new String[]{"all", "d/MON", "0", "01", "-1", "2147483648"}) {
            assertParseFailure(parser, value, Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);
        }
        assertParseFailure(parser, "1 2", MESSAGE_INVALID_FORMAT);
    }
}
