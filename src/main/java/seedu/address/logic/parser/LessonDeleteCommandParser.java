package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.LessonDeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the arguments of {@code lesson delete} into a {@link LessonDeleteCommand}.
 */
public class LessonDeleteCommandParser implements Parser<LessonDeleteCommand> {
    private static final String MESSAGE_INVALID_FORMAT = String.format(Messages.MESSAGE_INVALID_FORMAT_WITH_USAGE,
            LessonDeleteCommand.MESSAGE_USAGE);

    /**
     * Parses exactly one non-prefix lesson index.
     */
    @Override
    public LessonDeleteCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String lessonIndexText = StrictArgumentTokenizer.tokenizeWithPreamble(args, List.of(), MESSAGE_INVALID_FORMAT)
                .preamble();
        String[] indexArguments = lessonIndexText.split("\\s+");
        if (lessonIndexText.isBlank() || indexArguments.length != 1) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }
        Index displayedLessonIndex = ParserUtil.parseStrictIndex(indexArguments[0],
                Messages.MESSAGE_INVALID_LESSON_INDEX_SYNTAX);
        return new LessonDeleteCommand(displayedLessonIndex);
    }
}
