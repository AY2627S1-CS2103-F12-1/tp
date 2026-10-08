package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.LessonListCommand;

public class LessonCommandParserTest {
    private final LessonCommandParser parser = new LessonCommandParser();

    @Test
    public void parse_list_returnsLessonListCommand() {
        assertParseSuccess(parser, " list", new LessonListCommand());
    }

    @Test
    public void parse_missingOrUnknownSubcommand_failure() {
        assertParseFailure(parser, "   ", LessonCommandParser.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " show", LessonCommandParser.MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidListArguments_reportsListUsage() {
        assertParseFailure(parser, " list all", "Invalid command format.\nUsage: lesson list");
    }
}
