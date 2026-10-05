package seedu.address.logic;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.homework.Homework;
import seedu.address.model.person.Person;
import seedu.address.model.student.Student;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = "The person index provided is invalid.";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d person(s) listed!";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";
    public static final String MESSAGE_INVALID_FORMAT_WITH_USAGE = "Invalid command format.\nUsage: %s";
    public static final String MESSAGE_INVALID_STUDENT_INDEX_SYNTAX =
            "STUDENT_INDEX must be a positive whole number without leading zeroes.";
    public static final String MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX =
            "HOMEWORK_INDEX must be a positive whole number without leading zeroes.";
    public static final String MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX = "The student index provided is invalid.";
    public static final String MESSAGE_INVALID_HOMEWORK_DISPLAYED_INDEX = "The homework index provided is invalid.";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName())
                .append("; Phone: ")
                .append(person.getPhone())
                .append("; Email: ")
                .append(person.getEmail())
                .append("; Address: ")
                .append(person.getAddress())
                .append("; Tags: ");
        person.getTags().forEach(builder::append);
        return builder.toString();
    }

    /** Returns all six student fields in the Add Student success-message format. */
    public static String format(Student student) {
        return student.getName()
                + "; Level: " + student.getAcademicLevel()
                + "; Subjects: " + student.getSubjects()
                + "; Phone: " + student.getPhone()
                + "; Guardian: " + student.getGuardianName()
                + "; Guardian phone: " + student.getGuardianPhone();
    }

    /** Returns the owner's name and the homework's title, subject and due date in the homework message format. */
    public static String format(Student student, Homework homework) {
        return student.getName()
                + "; Title: " + homework.getTitle()
                + "; Subject: " + homework.getSubject()
                + "; Due: " + homework.getDueDate();
    }

}
