package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_FORMAT_WITH_USAGE;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.StudentFilterPredicate;
import seedu.address.model.student.TuitionSubject;

/**
 * Parses optional academic level, subject, and meeting day without changing the displayed roster.
 */
public final class FilterCommandParser implements Parser<FilterCommand> {
    public static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_FORMAT_WITH_USAGE, FilterCommand.MESSAGE_USAGE);
    public static final String MESSAGE_MISSING_FILTERS = MESSAGE_INVALID_FORMAT
            + "\n" + StudentFilterPredicate.MESSAGE_CONSTRAINTS;
    public static final String MESSAGE_REPEATED_FILTER = "Each filter may only be specified once";
    public static final String MESSAGE_EMPTY_LEVEL = "Academic level must be a non-empty value";
    public static final String MESSAGE_EMPTY_SUBJECT = "Subject must be a non-empty value";
    public static final String MESSAGE_EMPTY_DAY = "Day must be a non-empty value";
    public static final String MESSAGE_MULTIPLE_SUBJECTS = "Filter accepts only one subject. Use s/SUBJECT.";

    private static final String PREFIX_LEVEL = "l/";
    private static final String PREFIX_SUBJECT = "s/";
    private static final String PREFIX_DAY = "d/";
    private static final List<String> ALLOWED_PREFIXES = List.of(PREFIX_LEVEL, PREFIX_SUBJECT, PREFIX_DAY);

    @Override
    public FilterCommand parse(String arguments) throws ParseException {
        requireNonNull(arguments);
        Map<String, String> values = StrictArgumentTokenizer.tokenize(arguments, ALLOWED_PREFIXES,
                MESSAGE_INVALID_FORMAT, MESSAGE_REPEATED_FILTER).values();
        if (values.isEmpty()) {
            throw new ParseException(MESSAGE_MISSING_FILTERS);
        }

        Optional<AcademicLevel> academicLevel = Optional.empty();
        Optional<TuitionSubject> subject = Optional.empty();
        Optional<DayOfWeek> day = Optional.empty();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            switch (entry.getKey()) {
                case PREFIX_LEVEL -> academicLevel = Optional.of(parseAcademicLevel(entry.getValue()));
                case PREFIX_SUBJECT -> subject = Optional.of(parseSubject(entry.getValue()));
                case PREFIX_DAY -> day = Optional.of(parseDay(entry.getValue()));
                default -> throw new ParseException(MESSAGE_INVALID_FORMAT);
            }
        }
        return new FilterCommand(new StudentFilterPredicate(academicLevel, subject, day));
    }

    /**
     * Returns the normalized level, preserving the domain constraint message for unsupported values.
     */
    private AcademicLevel parseAcademicLevel(String value) throws ParseException {
        if (value.isBlank()) {
            throw new ParseException(MESSAGE_EMPTY_LEVEL);
        }
        try {
            return AcademicLevel.parse(value);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }

    /**
     * Returns one normalized subject, rejecting empty values and comma-separated lists.
     */
    private TuitionSubject parseSubject(String value) throws ParseException {
        if (value.isBlank()) {
            throw new ParseException(MESSAGE_EMPTY_SUBJECT);
        }
        if (value.contains(",")) {
            throw new ParseException(MESSAGE_MULTIPLE_SUBJECTS);
        }
        try {
            return TuitionSubject.parse(value);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }

    /**
     * Returns a full weekday, preserving the shared lesson constraint message for unsupported values.
     */
    private DayOfWeek parseDay(String value) throws ParseException {
        if (value.isBlank()) {
            throw new ParseException(MESSAGE_EMPTY_DAY);
        }
        try {
            return RegularLesson.parseDay(value);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }
}
