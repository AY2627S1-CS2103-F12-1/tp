package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.HomeworkListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the arguments of {@code homework list} into a {@link HomeworkListCommand}.
 * The arguments must be exactly one word that is not a prefix; its index syntax is checked afterwards.
 */
public class HomeworkListCommandParser implements Parser<HomeworkListCommand> {
    private static final String MESSAGE_INVALID_FORMAT = String.format(Messages.MESSAGE_INVALID_FORMAT_WITH_USAGE,
            HomeworkListCommand.MESSAGE_USAGE);

    /**
     * Returns a command that lists the homework of the student whose index is given in {@code args}.
     *
     * @throws ParseException If {@code args} is not exactly one student index.
     */
    @Override
    public HomeworkListCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String preamble = StrictArgumentTokenizer.tokenizeWithPreamble(args, List.of(), MESSAGE_INVALID_FORMAT)
                .preamble();
        if (preamble.isEmpty() || preamble.split("\\s+").length != 1) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }

        Index studentIndex = ParserUtil.parseStrictIndex(preamble, Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);
        return new HomeworkListCommand(studentIndex);
    }
}
