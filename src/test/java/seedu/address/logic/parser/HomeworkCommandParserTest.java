package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.HomeworkAddCommand;

public class HomeworkCommandParserTest {
    /** Fixed clock at 2026-10-05, so short-form due dates always infer the same year. */
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-05T00:00:00Z"), ZoneOffset.UTC);
    private static final String MESSAGE_INVALID_FORMAT = "Invalid command format.\n"
            + "Usage: homework|hw add STUDENT_INDEX title/|t/TITLE s/SUBJECT due/YYYY-MM-DD|MM-DD";

    private final HomeworkCommandParser parser = new HomeworkCommandParser(CLOCK);

    @Test
    public void parse_add_returnsHomeworkAddCommand() {
        assertParseSuccess(parser, " add 1 title/Complete algebra worksheet s/MATH due/2026-10-15",
                new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, false));
    }

    @Test
    public void parse_addShortDueDate_usesGivenClock() {
        assertParseSuccess(parser, " add 1 t/Complete algebra worksheet s/MATH due/10-15",
                new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, true));
    }

    @Test
    public void parse_missingSubcommand_failure() {
        assertParseFailure(parser, "   ", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_unknownSubcommand_failure() {
        assertParseFailure(parser, " remove 1 2", MESSAGE_INVALID_FORMAT);
    }
}
