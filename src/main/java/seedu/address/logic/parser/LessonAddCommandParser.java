package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.TuitionSubject;

/** Parses the required student index, subject, weekday and times for lesson add. */
public class LessonAddCommandParser implements Parser<LessonAddCommand> {
    public static final String MESSAGE_FORMAT = "Invalid command format.\nUsage: " + LessonAddCommand.MESSAGE_USAGE;
    private static final List<String> PREFIXES = List.of("s/", "d/", "st/", "et/");

    @Override
    public LessonAddCommand parse(String args) throws ParseException {
        requireNonNull(args);
        var tokenized = StrictArgumentTokenizer.tokenizeWithPreamble(args, PREFIXES, MESSAGE_FORMAT);
        Map<String, String> values = tokenized.values();
        if (tokenized.preamble().isEmpty() || tokenized.preamble().split("\\s+").length != 1
                || !values.keySet().containsAll(PREFIXES)
                || values.values().stream().anyMatch(value -> value.strip().split("\\s+").length > 1)) {
            throw new ParseException(MESSAGE_FORMAT);
        }
        Index index = ParserUtil.parseStrictIndex(tokenized.preamble(), Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);
        try {
            TuitionSubject subject = TuitionSubject.parse(values.get("s/"));
            DayOfWeek day = RegularLesson.parseDay(values.get("d/"));
            LocalTime start = RegularLesson.parseTime(values.get("st/"), RegularLesson.MESSAGE_START);
            LocalTime end = RegularLesson.parseTime(values.get("et/"), RegularLesson.MESSAGE_END);
            return new LessonAddCommand(index, new RegularLesson(subject, day, start, end));
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }

}
