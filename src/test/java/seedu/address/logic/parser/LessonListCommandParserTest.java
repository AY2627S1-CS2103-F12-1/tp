package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.LessonListCommand;

public class LessonListCommandParserTest {
    private static final String MESSAGE_INVALID_FORMAT = "Invalid command format.\nUsage: lesson list";

    private final LessonListCommandParser parser = new LessonListCommandParser();

    @Test
    public void parse_noArguments_success() {
        assertParseSuccess(parser, "   ", new LessonListCommand());
    }

    @Test
    public void parse_anyArgument_failure() {
        assertParseFailure(parser, "all", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "d/MON", MESSAGE_INVALID_FORMAT);
    }
}
