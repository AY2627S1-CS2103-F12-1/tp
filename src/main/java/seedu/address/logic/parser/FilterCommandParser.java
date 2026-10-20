package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.StudentFilterPredicate;
import seedu.address.model.student.TuitionSubject;

/**
 * Parses optional academic level and subject criteria without changing the displayed roster.
 */
public final class FilterCommandParser implements Parser<FilterCommand> {
    public static final String MESSAGE_INVALID_FORMAT =
            "Invalid command format\nUsage: " + FilterCommand.MESSAGE_USAGE;
    public static final String MESSAGE_MISSING_FILTERS = "Invalid Command Format\nUsage: " + FilterCommand.MESSAGE_USAGE
            + "\n" + StudentFilterPredicate.MESSAGE_CONSTRAINTS;
    public static final String MESSAGE_REPEATED_FILTER = "Each filter may only be specified once";
    public static final String MESSAGE_INVALID_LEVEL = "Academic level must be a non-empty value";
    public static final String MESSAGE_INVALID_SUBJECT = "Subject must be a non-empty value";

    private static final String PREFIX_LEVEL = "l/";
    private static final String PREFIX_SUBJECT = "s/";
    private static final List<String> ALLOWED_PREFIXES = List.of(PREFIX_LEVEL, PREFIX_SUBJECT);

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
        for (Map.Entry<String, String> entry : values.entrySet()) {
            switch (entry.getKey()) {
                case PREFIX_LEVEL -> academicLevel = Optional.of(parseAcademicLevel(entry.getValue()));
                case PREFIX_SUBJECT -> subject = Optional.of(parseSubject(entry.getValue()));
                default -> throw new ParseException(MESSAGE_INVALID_FORMAT);
            }
        }
        return new FilterCommand(new StudentFilterPredicate(academicLevel, subject));
    }

    /**
     * Returns the normalized level, translating invalid values to the filter-specific error.
     */
    private AcademicLevel parseAcademicLevel(String value) throws ParseException {
        try {
            return AcademicLevel.parse(value);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(MESSAGE_INVALID_LEVEL, exception);
        }
    }

    /**
     * Returns the normalized subject, translating invalid values to the filter-specific error.
     */
    private TuitionSubject parseSubject(String value) throws ParseException {
        try {
            return TuitionSubject.parse(value);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(MESSAGE_INVALID_SUBJECT, exception);
        }
    }
}
