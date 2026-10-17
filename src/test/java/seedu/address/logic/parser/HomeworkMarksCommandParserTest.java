package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.HomeworkMarksCommand;
import seedu.address.model.homework.Score;

public class HomeworkMarksCommandParserTest {
    private static final String MESSAGE_INVALID_FORMAT = "Invalid command format.\n"
            + "Usage: homework|hw marks|m STUDENT_INDEX HOMEWORK_INDEX score/SCORE";
    private static final String MESSAGE_MISSING_SCORE = "Missing required prefix(es): score/\n"
            + "Usage: homework|hw marks|m STUDENT_INDEX HOMEWORK_INDEX score/SCORE";

    private final HomeworkMarksCommandParser parser = new HomeworkMarksCommandParser();

    @Test
    public void parse_validArguments_success() {
        assertParseSuccess(parser, "1 2 score/5/10",
                new HomeworkMarksCommand(Index.fromOneBased(1), Index.fromOneBased(2), new Score(5, 10)));
    }

    @Test
    public void parse_extraWhitespace_success() {
        assertParseSuccess(parser, "  2   1   score/  35/50  ",
                new HomeworkMarksCommand(Index.fromOneBased(2), Index.fromOneBased(1), new Score(35, 50)));
    }

    @Test
    public void parse_boundaryScores_success() {
        // no marks obtained
        assertParseSuccess(parser, "3 2 score/0/10",
                new HomeworkMarksCommand(Index.fromOneBased(3), Index.fromOneBased(2), new Score(0, 10)));

        // full marks
        assertParseSuccess(parser, "3 2 score/10/10",
                new HomeworkMarksCommand(Index.fromOneBased(3), Index.fromOneBased(2), new Score(10, 10)));

        // largest numbers
        assertParseSuccess(parser, "3 2 score/2147483647/2147483647", new HomeworkMarksCommand(
                Index.fromOneBased(3), Index.fromOneBased(2), new Score(Integer.MAX_VALUE, Integer.MAX_VALUE)));
    }

    @Test
    public void parse_largestIntIndices_success() {
        assertParseSuccess(parser, "2147483647 2147483647 score/5/10", new HomeworkMarksCommand(
                Index.fromOneBased(Integer.MAX_VALUE), Index.fromOneBased(Integer.MAX_VALUE), new Score(5, 10)));
    }

    @Test
    public void parse_wrongNumberOfIndices_failure() {
        // no index
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "score/5/10", MESSAGE_INVALID_FORMAT);

        // one index
        assertParseFailure(parser, "1 score/5/10", MESSAGE_INVALID_FORMAT);

        // three indices
        assertParseFailure(parser, "1 2 3 score/5/10", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_missingScorePrefix_failure() {
        assertParseFailure(parser, "1 2", MESSAGE_MISSING_SCORE);
    }

    @Test
    public void parse_unknownPrefix_failure() {
        assertParseFailure(parser, "1 2 score/5/10 x/extra", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 2 s/5/10", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_uppercasePrefix_failure() {
        assertParseFailure(parser, "1 2 SCORE/5/10", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_repeatedScorePrefix_failure() {
        assertParseFailure(parser, "1 2 score/5/10 score/6/10",
                String.format(StrictArgumentTokenizer.MESSAGE_REPEATED_PREFIX, "score/"));
    }

    @Test
    public void parse_invalidStudentIndex_failure() {
        // zero
        assertParseFailure(parser, "0 1 score/5/10", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // leading zero
        assertParseFailure(parser, "01 1 score/5/10", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // negative
        assertParseFailure(parser, "-1 1 score/5/10", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // plus sign
        assertParseFailure(parser, "+1 1 score/5/10", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // decimal
        assertParseFailure(parser, "1.0 1 score/5/10", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // word
        assertParseFailure(parser, "one 1 score/5/10", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // larger than the largest int
        assertParseFailure(parser, "2147483648 1 score/5/10", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);
    }

    @Test
    public void parse_invalidHomeworkIndex_failure() {
        // zero
        assertParseFailure(parser, "1 0 score/5/10", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);

        // leading zero
        assertParseFailure(parser, "1 01 score/5/10", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);

        // negative
        assertParseFailure(parser, "1 -1 score/5/10", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);

        // plus sign
        assertParseFailure(parser, "1 +1 score/5/10", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);

        // decimal
        assertParseFailure(parser, "1 1.0 score/5/10", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);

        // word
        assertParseFailure(parser, "1 one score/5/10", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);

        // larger than the largest int
        assertParseFailure(parser, "1 2147483648 score/5/10", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);
    }

    @Test
    public void parse_bothIndicesInvalid_reportsStudentIndex() {
        assertParseFailure(parser, "0 0 score/11/10", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);
    }

    @Test
    public void parse_invalidScore_failure() {
        // empty
        assertParseFailure(parser, "1 2 score/", Score.MESSAGE_CONSTRAINTS);

        // marks obtained above the total
        assertParseFailure(parser, "1 2 score/11/10", Score.MESSAGE_CONSTRAINTS);

        // zero total
        assertParseFailure(parser, "1 2 score/5/0", Score.MESSAGE_CONSTRAINTS);

        // decimal
        assertParseFailure(parser, "1 2 score/5.5/10", Score.MESSAGE_CONSTRAINTS);

        // spaces inside
        assertParseFailure(parser, "1 2 score/5 / 10", Score.MESSAGE_CONSTRAINTS);

        // single number
        assertParseFailure(parser, "1 2 score/85", Score.MESSAGE_CONSTRAINTS);

        // leading zero
        assertParseFailure(parser, "1 2 score/05/10", Score.MESSAGE_CONSTRAINTS);

        // sign
        assertParseFailure(parser, "1 2 score/-1/10", Score.MESSAGE_CONSTRAINTS);

        // extra word after the score
        assertParseFailure(parser, "1 2 score/5/10 extra", Score.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_marksTooLarge_failure() {
        assertParseFailure(parser, "1 2 score/2147483648/2147483648", Score.MESSAGE_TOO_LARGE);
        assertParseFailure(parser, "1 2 score/5/2147483648", Score.MESSAGE_TOO_LARGE);
    }
}
