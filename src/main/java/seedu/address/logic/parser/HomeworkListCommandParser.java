package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.HomeworkListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the arguments of {@code homework list} into a {@link HomeworkListCommand}.
 * The arguments must be one word that is not a prefix, optionally followed by the word {@code all}; the index
 * syntax is checked afterwards.
 */
public class HomeworkListCommandParser implements Parser<HomeworkListCommand> {
    public static final String KEYWORD_ALL = "all";

    private static final String MESSAGE_INVALID_FORMAT = String.format(Messages.MESSAGE_INVALID_FORMAT_WITH_USAGE,
            HomeworkListCommand.MESSAGE_USAGE);

    private final Clock clock;

    /**
     * Creates a parser that decides which homework is upcoming from the date of {@code clock}.
     */
    public HomeworkListCommandParser(Clock clock) {
        this.clock = requireNonNull(clock);
    }

    /**
     * Returns a command that lists the homework of the student whose index is given in {@code args}.
     *
     * @throws ParseException If {@code args} is not exactly one student index, optionally followed by {@code all}.
     */
    @Override
    public HomeworkListCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String preamble = StrictArgumentTokenizer.tokenizeWithPreamble(args, List.of(), MESSAGE_INVALID_FORMAT)
                .preamble();
        String[] words = preamble.split("\\s+");
        boolean isAllShown = words.length == 2 && words[1].equals(KEYWORD_ALL);
        if (preamble.isEmpty() || words.length > 2 || (words.length == 2 && !isAllShown)) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }

        Index studentIndex = ParserUtil.parseStrictIndex(words[0], Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX,
                Messages.MESSAGE_STUDENT_INDEX_TOO_LARGE);
        return new HomeworkListCommand(studentIndex, LocalDate.now(clock), isAllShown);
    }
}
