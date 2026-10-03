package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentName;
import seedu.address.model.student.StudentPhone;
import seedu.address.model.student.TuitionSubjects;

/**
 * Parses the six required fields of an Add Student command into a student profile.
 * Parsing does not change the roster or saved data.
 */
public final class StudentAddParser {
    public static final String USAGE = "add n/STUDENT_NAME l/ACADEMIC_LEVEL s/SUBJECTS p/STUDENT_PHONE "
            + "gn/GUARDIAN_NAME gp/GUARDIAN_PHONE";
    public static final String MESSAGE_INVALID_FORMAT = "Invalid command format. Usage: " + USAGE;
    public static final String MESSAGE_MISSING_PREFIXES = "Missing required prefix(es): %s Usage: " + USAGE;
    public static final String MESSAGE_REPEATED_PREFIX = "Prefix %s must be specified exactly once.";

    private static final List<String> REQUIRED_PREFIXES = List.of("n/", "l/", "s/", "p/", "gn/", "gp/");
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?<!\\S)([a-z]+/)");

    /** Creates a parser for Add Student arguments. */
    public StudentAddParser() {
    }

    /**
     * Returns a student parsed from the arguments following {@code add}.
     * All six prefixes are required exactly once and may appear in any order. A present but empty value receives
     * its field's validation error, while a missing or repeated prefix receives a structural error.
     *
     * @throws ParseException If the command structure or any field value is invalid.
     */
    public Student parse(String arguments) throws ParseException {
        requireNonNull(arguments);
        Matcher matcher = PREFIX_PATTERN.matcher(arguments);
        Map<String, String> fields = new HashMap<>();
        int previousValueStart = -1;
        String previousPrefix = null;

        while (matcher.find()) {
            if (previousPrefix == null && !arguments.substring(0, matcher.start()).isBlank()) {
                throw new ParseException(MESSAGE_INVALID_FORMAT);
            }
            if (previousPrefix != null) {
                fields.put(previousPrefix, arguments.substring(previousValueStart, matcher.start()).strip());
            }

            String prefix = matcher.group(1);
            if (!REQUIRED_PREFIXES.contains(prefix)) {
                throw new ParseException(MESSAGE_INVALID_FORMAT);
            }
            if (fields.containsKey(prefix) || prefix.equals(previousPrefix)) {
                throw new ParseException(String.format(MESSAGE_REPEATED_PREFIX, prefix));
            }
            previousPrefix = prefix;
            previousValueStart = matcher.end();
        }

        if (previousPrefix == null) {
            if (!arguments.isBlank()) {
                throw new ParseException(MESSAGE_INVALID_FORMAT);
            }
        } else {
            fields.put(previousPrefix, arguments.substring(previousValueStart).strip());
        }

        List<String> missingPrefixes = new ArrayList<>();
        for (String prefix : REQUIRED_PREFIXES) {
            if (!fields.containsKey(prefix)) {
                missingPrefixes.add(prefix);
            }
        }
        if (!missingPrefixes.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_MISSING_PREFIXES, String.join(", ", missingPrefixes)));
        }

        try {
            return new Student(new StudentName(fields.get("n/")), AcademicLevel.parse(fields.get("l/")),
                    new TuitionSubjects(fields.get("s/")), new StudentPhone(fields.get("p/")),
                    new StudentName(fields.get("gn/")), new StudentPhone(fields.get("gp/")));
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }
}
