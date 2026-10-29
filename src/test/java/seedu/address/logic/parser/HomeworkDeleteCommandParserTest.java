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
import seedu.address.logic.commands.HomeworkDeleteCommand;

public class HomeworkDeleteCommandParserTest {
    private static final String MESSAGE_INVALID_FORMAT = "Invalid command format.\n"
            + "Usage: homework|hw delete|del STUDENT_INDEX HOMEWORK_INDEX";

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-05T00:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 5);

    private final HomeworkDeleteCommandParser parser = new HomeworkDeleteCommandParser(CLOCK);

    @Test
    public void parse_validIndices_success() {
        assertParseSuccess(parser, "1 2",
                new HomeworkDeleteCommand(Index.fromOneBased(1), Index.fromOneBased(2), TODAY));
    }

    @Test
    public void parse_validIndicesWithWhitespace_success() {
        assertParseSuccess(parser, "  13   10  ",
                new HomeworkDeleteCommand(Index.fromOneBased(13), Index.fromOneBased(10), TODAY));
    }

    @Test
    public void parse_largestIndices_success() {
        Index largestIndex = Index.fromOneBased(Integer.MAX_VALUE);
        assertParseSuccess(parser, "2147483647 2147483647",
                new HomeworkDeleteCommand(largestIndex, largestIndex, TODAY));
    }

    @Test
    public void parse_noIndex_failure() {
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_oneIndex_failure() {
        assertParseFailure(parser, "1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_threeIndices_failure() {
        assertParseFailure(parser, "1 2 3", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_prefixSupplied_failure() {
        String usageLine = "\nUsage: " + HomeworkDeleteCommand.MESSAGE_USAGE;
        assertParseFailure(parser, "1 s/2", notPrefix("s/") + usageLine);
        assertParseFailure(parser, "1 x/2", notPrefix("x/") + usageLine);
    }

    @Test
    public void parse_invalidStudentIndex_failure() {
        // zero
        assertParseFailure(parser, "0 1", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // leading zero
        assertParseFailure(parser, "01 1", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // negative
        assertParseFailure(parser, "-1 1", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // plus sign
        assertParseFailure(parser, "+1 1", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // decimal
        assertParseFailure(parser, "1.0 1", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // word
        assertParseFailure(parser, "one 1", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // larger than the largest int
        assertParseFailure(parser, "2147483648 1", Messages.MESSAGE_STUDENT_INDEX_TOO_LARGE);
        assertParseFailure(parser, "9999999999 1", Messages.MESSAGE_STUDENT_INDEX_TOO_LARGE);
    }

    @Test
    public void parse_invalidHomeworkIndex_failure() {
        // zero
        assertParseFailure(parser, "1 0", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);

        // leading zero
        assertParseFailure(parser, "1 01", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);

        // negative
        assertParseFailure(parser, "1 -1", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);

        // plus sign
        assertParseFailure(parser, "1 +1", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);

        // decimal
        assertParseFailure(parser, "1 1.0", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);

        // word
        assertParseFailure(parser, "1 one", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);

        // larger than the largest int
        assertParseFailure(parser, "1 2147483648", Messages.MESSAGE_HOMEWORK_INDEX_TOO_LARGE);
        assertParseFailure(parser, "1 9999999999", Messages.MESSAGE_HOMEWORK_INDEX_TOO_LARGE);
    }

    @Test
    public void parse_bothIndicesInvalid_reportsStudentIndex() {
        assertParseFailure(parser, "0 0", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);
    }
}
