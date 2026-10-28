package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.LessonListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the arguments of {@code lesson list} into a {@link LessonListCommand}.
 */
public class LessonListCommandParser implements Parser<LessonListCommand> {
    private static final String MESSAGE_INVALID_FORMAT = String.format(Messages.MESSAGE_INVALID_FORMAT_WITH_USAGE,
            LessonListCommand.MESSAGE_USAGE);

    /**
     * Returns a command that lists all regular lessons, or those of one displayed student.
     *
     * @throws ParseException If {@code args} is not empty or a single strict student index.
     */
    @Override
    public LessonListCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (args.isBlank()) {
            return new LessonListCommand();
        }
        if (args.strip().split("\\s+").length != 1) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }
        return new LessonListCommand(ParserUtil.parseStrictIndex(args, Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX));
    }
}
