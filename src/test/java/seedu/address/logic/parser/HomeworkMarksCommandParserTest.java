package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.CommandParserTestUtil.notPrefix;
import static seedu.address.logic.parser.CommandParserTestUtil.notPrefixWithQuoteHint;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.HomeworkMarksCommand;
import seedu.address.model.homework.Score;

public class HomeworkMarksCommandParserTest {
    private static final String MESSAGE_INVALID_FORMAT = "Invalid command format.\n"
            + "Usage: homework|hw marks|m STUDENT_INDEX HOMEWORK_INDEX score/[SCORE]";
    private static final String MESSAGE_MISSING_SCORE = "Missing required prefix(es): score/\n"
            + "Usage: homework|hw marks|m STUDENT_INDEX HOMEWORK_INDEX score/[SCORE]";

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
    public void parse_bareScore_success() {
        assertParseSuccess(parser, "1 2 score/17",
                new HomeworkMarksCommand(Index.fromOneBased(1), Index.fromOneBased(2), new Score(17)));

        // no marks obtained
        assertParseSuccess(parser, "1 2 score/0",
                new HomeworkMarksCommand(Index.fromOneBased(1), Index.fromOneBased(2), new Score(0)));

        // largest number
        assertParseSuccess(parser, "1 2 score/2147483647",
                new HomeworkMarksCommand(Index.fromOneBased(1), Index.fromOneBased(2), new Score(Integer.MAX_VALUE)));

        // surrounding whitespace
        assertParseSuccess(parser, "1 2 score/  17  ",
                new HomeworkMarksCommand(Index.fromOneBased(1), Index.fromOneBased(2), new Score(17)));
    }

    @Test
    public void parse_quotedScore_success() {
        assertParseSuccess(parser, "1 2 score/\"17\"",
                new HomeworkMarksCommand(Index.fromOneBased(1), Index.fromOneBased(2), new Score(17)));
        assertParseSuccess(parser, "1 2 score/\"17/20\"",
                new HomeworkMarksCommand(Index.fromOneBased(1), Index.fromOneBased(2), new Score(17, 20)));

        // spaces at both ends inside the quotes are removed
        assertParseSuccess(parser, "1 2 score/\" 17 \"",
                new HomeworkMarksCommand(Index.fromOneBased(1), Index.fromOneBased(2), new Score(17)));

        // empty quotes clear the marks, like an empty unquoted value
        assertParseSuccess(parser, "1 2 score/\"\"",
                new HomeworkMarksCommand(Index.fromOneBased(1), Index.fromOneBased(2)));
    }

    @Test
    public void parse_invalidQuotedScore_failure() {
        // spaces between digits are kept
        assertParseFailure(parser, "1 2 score/\"1 7\"", Score.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_emptyScore_returnsClearCommand() {
        HomeworkMarksCommand expectedCommand = new HomeworkMarksCommand(Index.fromOneBased(1), Index.fromOneBased(2));

        // no value
        assertParseSuccess(parser, "1 2 score/", expectedCommand);

        // whitespace only
        assertParseSuccess(parser, " 1  2  score/   ", expectedCommand);
    }

    @Test
    public void parse_emptyScoreInvalidIndex_failure() {
        assertParseFailure(parser, "0 2 score/", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);
        assertParseFailure(parser, "1 01 score/", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);
        assertParseFailure(parser, "1 score/", MESSAGE_INVALID_FORMAT);
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
        String usageLine = "\nUsage: " + HomeworkMarksCommand.MESSAGE_USAGE;
        // the score before x/ is only digits and a slash, so no example is given
        assertParseFailure(parser, "1 2 score/5/10 x/extra", notPrefixWithQuoteHint("x/") + usageLine);
        assertParseFailure(parser, "1 2 s/5/10", notPrefix("s/") + usageLine);
    }

    @Test
    public void parse_uppercasePrefix_failure() {
        assertParseFailure(parser, "1 2 SCORE/5/10", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_repeatedScorePrefix_failure() {
        assertParseFailure(parser, "1 2 score/5/10 score/6/10",
                String.format(StrictArgumentTokenizer.MESSAGE_REPEATED_PREFIX, "score/"));
        assertParseFailure(parser, "1 2 score/ score/",
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
        assertParseFailure(parser, "2147483648 1 score/5/10", Messages.MESSAGE_STUDENT_INDEX_TOO_LARGE);
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
        assertParseFailure(parser, "1 2147483648 score/5/10", Messages.MESSAGE_HOMEWORK_INDEX_TOO_LARGE);
        assertParseFailure(parser, "1 9999999999 score/5/10", Messages.MESSAGE_HOMEWORK_INDEX_TOO_LARGE);
    }

    @Test
    public void parse_bothIndicesInvalid_reportsStudentIndex() {
        assertParseFailure(parser, "0 0 score/11/10", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);
    }

    @Test
    public void parse_invalidScore_failure() {
        // marks obtained above the total
        assertParseFailure(parser, "1 2 score/11/10", Score.MESSAGE_CONSTRAINTS);

        // zero total
        assertParseFailure(parser, "1 2 score/5/0", Score.MESSAGE_CONSTRAINTS);

        // decimal
        assertParseFailure(parser, "1 2 score/5.5/10", Score.MESSAGE_CONSTRAINTS);

        // spaces inside
        assertParseFailure(parser, "1 2 score/5 / 10", Score.MESSAGE_CONSTRAINTS);

        // sign
        assertParseFailure(parser, "1 2 score/-1/10", Score.MESSAGE_CONSTRAINTS);

        // extra word after the score
        assertParseFailure(parser, "1 2 score/5/10 extra", Score.MESSAGE_CONSTRAINTS);

        // empty total
        assertParseFailure(parser, "1 2 score/17/", Score.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidBareScore_failure() {
        // plus sign
        assertParseFailure(parser, "1 2 score/+17", Score.MESSAGE_CONSTRAINTS);

        // negative
        assertParseFailure(parser, "1 2 score/-1", Score.MESSAGE_CONSTRAINTS);

        // decimal
        assertParseFailure(parser, "1 2 score/17.5", Score.MESSAGE_CONSTRAINTS);

        // space inside
        assertParseFailure(parser, "1 2 score/1 7", Score.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_leadingZeroes_failure() {
        // bare score
        assertParseFailure(parser, "1 2 score/07", Score.MESSAGE_LEADING_ZEROES);

        // zero written with two digits
        assertParseFailure(parser, "1 2 score/00", Score.MESSAGE_LEADING_ZEROES);

        // marks obtained
        assertParseFailure(parser, "3 1 score/05/10", Score.MESSAGE_LEADING_ZEROES);

        // total
        assertParseFailure(parser, "1 2 score/5/010", Score.MESSAGE_LEADING_ZEROES);

        // reported before the number being too large
        assertParseFailure(parser, "1 2 score/02147483648", Score.MESSAGE_LEADING_ZEROES);

        // other format error as well
        assertParseFailure(parser, "1 2 score/-05", Score.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_marksTooLarge_failure() {
        assertParseFailure(parser, "1 2 score/2147483648/2147483648", Score.MESSAGE_TOO_LARGE);
        assertParseFailure(parser, "1 2 score/5/2147483648", Score.MESSAGE_TOO_LARGE);
        assertParseFailure(parser, "1 2 score/2147483648", Score.MESSAGE_TOO_LARGE);
    }
}
