package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.HomeworkDeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the arguments of {@code homework delete} into a {@link HomeworkDeleteCommand}.
 * The arguments must be exactly two words that are not prefixes; the student index syntax is checked before the
 * homework index syntax.
 */
public class HomeworkDeleteCommandParser implements Parser<HomeworkDeleteCommand> {
    private static final String MESSAGE_INVALID_FORMAT = String.format(Messages.MESSAGE_INVALID_FORMAT_WITH_USAGE,
            HomeworkDeleteCommand.MESSAGE_USAGE);

    /**
     * Returns a command that deletes the homework whose student index and homework index are given in {@code args}.
     *
     * @throws ParseException If {@code args} is not exactly a student index followed by a homework index.
     */
    @Override
    public HomeworkDeleteCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String preamble = StrictArgumentTokenizer.tokenizeWithPreamble(args, List.of(), MESSAGE_INVALID_FORMAT)
                .preamble();
        String[] indices = preamble.split("\\s+");
        if (indices.length != 2) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }

        Index studentIndex = ParserUtil.parseStrictIndex(indices[0], Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);
        Index homeworkIndex = ParserUtil.parseStrictIndex(indices[1], Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);
        return new HomeworkDeleteCommand(studentIndex, homeworkIndex);
    }
}
