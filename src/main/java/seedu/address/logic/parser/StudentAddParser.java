package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public static final String MESSAGE_REPEATED_PREFIX = StrictArgumentTokenizer.MESSAGE_REPEATED_PREFIX;

    private static final List<String> REQUIRED_PREFIXES = List.of("n/", "l/", "s/", "p/", "gn/", "gp/");

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
        Map<String, String> fields = StrictArgumentTokenizer.tokenize(arguments, REQUIRED_PREFIXES,
                MESSAGE_INVALID_FORMAT, StudentAddParser::isPartOfName).values();

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

    /**
     * Returns true if a prefix-shaped token belongs to a name rather than the command structure.
     * Names may contain slashes, and the standalone name token {@code s/o} overlaps the subjects prefix.
     */
    private static boolean isPartOfName(String arguments, int prefixEnd, String previousPrefix, String prefix) {
        if (!"n/".equals(previousPrefix) && !"gn/".equals(previousPrefix)) {
            return false;
        }
        if (!REQUIRED_PREFIXES.contains(prefix)) {
            return true;
        }
        if (!"s/".equals(prefix) || prefixEnd >= arguments.length()
                || Character.toLowerCase(arguments.charAt(prefixEnd)) != 'o') {
            return false;
        }
        int tokenEnd = prefixEnd + 1;
        return tokenEnd == arguments.length() || Character.isWhitespace(arguments.charAt(tokenEnd));
    }
}
