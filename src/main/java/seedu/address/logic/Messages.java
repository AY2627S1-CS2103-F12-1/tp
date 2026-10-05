package seedu.address.logic;

import seedu.address.model.homework.Homework;
import seedu.address.model.student.Student;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_FORMAT_WITH_USAGE = "Invalid command format.\nUsage: %s";
    public static final String MESSAGE_INVALID_STUDENT_INDEX_SYNTAX =
            "STUDENT_INDEX must be a positive whole number without leading zeroes.";
    public static final String MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX =
            "HOMEWORK_INDEX must be a positive whole number without leading zeroes.";
    public static final String MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX = "The student index provided is invalid.";
    public static final String MESSAGE_INVALID_HOMEWORK_DISPLAYED_INDEX = "The homework index provided is invalid.";
    public static final String MESSAGE_NOT_IMPLEMENTED = "This command is not implemented yet.";

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
