package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.LessonEditCommand;
import seedu.address.logic.commands.LessonEditCommand.EditLessonDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.TuitionSubject;

/**
 * Parses one weekly lesson index and one or more optional replacement fields.
 */
public class LessonEditCommandParser implements Parser<LessonEditCommand> {
    private static final String MESSAGE_FORMAT = String.format(Messages.MESSAGE_INVALID_FORMAT_WITH_USAGE,
            LessonEditCommand.MESSAGE_USAGE);

    @Override
    public LessonEditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        var tokens = StrictArgumentTokenizer.tokenizeWithPreamble(args, List.of("s/", "d/", "st/", "et/"),
                MESSAGE_FORMAT);
        Map<String, String> values = tokens.values();
        if (tokens.preamble().isBlank() || tokens.preamble().split("\\s+").length != 1 || values.isEmpty()) {
            throw new ParseException(MESSAGE_FORMAT);
        }
        Index index = ParserUtil.parseStrictIndex(tokens.preamble(), Messages.MESSAGE_INVALID_LESSON_INDEX_SYNTAX);
        try {
            return new LessonEditCommand(index, new EditLessonDescriptor(
                    values.containsKey("s/") ? TuitionSubject.parse(values.get("s/")) : null,
                    values.containsKey("d/") ? RegularLesson.parseDay(values.get("d/")) : null,
                    values.containsKey("st/") ? RegularLesson.parseTime(values.get("st/"), RegularLesson.MESSAGE_START)
                            : null,
                    values.containsKey("et/") ? RegularLesson.parseTime(values.get("et/"), RegularLesson.MESSAGE_END)
                            : null));
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }
}
