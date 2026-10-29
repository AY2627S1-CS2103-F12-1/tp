package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.HomeworkMarksCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.homework.Score;

/**
 * Parses the arguments of {@code homework marks} into a {@link HomeworkMarksCommand}.
 * Errors are checked in this order, and only the first one found is reported:
 * unknown or repeated prefix, indices that are missing or not exactly two words, a missing {@code score/} prefix,
 * student index syntax, homework index syntax, and the score.
 * An empty {@code score/} value clears the recorded marks.
 */
public class HomeworkMarksCommandParser implements Parser<HomeworkMarksCommand> {
    public static final String PREFIX_SCORE = "score/";

    private static final String MESSAGE_INVALID_FORMAT = String.format(Messages.MESSAGE_INVALID_FORMAT_WITH_USAGE,
            HomeworkMarksCommand.MESSAGE_USAGE);

    /**
     * Returns a command that records the score given in {@code args} for the homework identified by its indices, or
     * clears its marks if the score is empty.
     *
     * @throws ParseException If the command structure, either index or the score is invalid.
     */
    @Override
    public HomeworkMarksCommand parse(String args) throws ParseException {
        requireNonNull(args);
        StrictArgumentTokenizer.TokenizedArguments tokenizedArguments =
                StrictArgumentTokenizer.tokenizeWithPreambleRejectingWrongCase(args, List.of(PREFIX_SCORE),
                        MESSAGE_INVALID_FORMAT);
        String[] indices = tokenizedArguments.preamble().split("\\s+");
        if (indices.length != 2) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }

        Map<String, String> values = tokenizedArguments.values();
        if (!values.containsKey(PREFIX_SCORE)) {
            throw new ParseException(String.format(Messages.MESSAGE_MISSING_PREFIXES_WITH_USAGE, PREFIX_SCORE,
                    HomeworkMarksCommand.MESSAGE_USAGE));
        }

        Index studentIndex = ParserUtil.parseStrictIndex(indices[0], Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX,
                Messages.MESSAGE_STUDENT_INDEX_TOO_LARGE);
        Index homeworkIndex = ParserUtil.parseStrictIndex(indices[1], Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX,
                Messages.MESSAGE_HOMEWORK_INDEX_TOO_LARGE);
        if (values.get(PREFIX_SCORE).isBlank()) {
            return new HomeworkMarksCommand(studentIndex, homeworkIndex);
        }
        try {
            return new HomeworkMarksCommand(studentIndex, homeworkIndex, Score.parse(values.get(PREFIX_SCORE)));
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }
}
