package seedu.address.logic;

import seedu.address.model.homework.Homework;
import seedu.address.model.homework.Score;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.Student;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_FORMAT_WITH_USAGE = "Invalid command format.\nUsage: %s";
    public static final String MESSAGE_MISSING_PREFIXES_WITH_USAGE = "Missing required prefix(es): %s\nUsage: %s";
    public static final String MESSAGE_UNEXPECTED_WORDS_WITH_USAGE = "Expected %s, but found %s.\nUsage: %s";
    public static final String MESSAGE_NO_WORDS = "none";
    public static final String MESSAGE_ONE_WORD = "1 word (%s)";
    public static final String MESSAGE_WORDS = "%d words (%s)";
    public static final String MESSAGE_INVALID_STUDENT_INDEX_SYNTAX =
            "STUDENT_INDEX must be a positive whole number without leading zeroes.";
    public static final String MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX =
            "HOMEWORK_INDEX must be a positive whole number without leading zeroes.";
    public static final String MESSAGE_INVALID_LESSON_INDEX_SYNTAX =
            "LESSON_INDEX must be a positive whole number without leading zeroes.";
    public static final String MESSAGE_STUDENT_INDEX_TOO_LARGE =
            "STUDENT_INDEX must be no greater than " + Integer.MAX_VALUE + ".";
    public static final String MESSAGE_HOMEWORK_INDEX_TOO_LARGE =
            "HOMEWORK_INDEX must be no greater than " + Integer.MAX_VALUE + ".";
    public static final String MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX = "The student index provided is invalid.";
    public static final String MESSAGE_INVALID_HOMEWORK_DISPLAYED_INDEX = "The homework index provided is invalid.";
    public static final String MESSAGE_INVALID_LESSON_DISPLAYED_INDEX = "The lesson index provided is invalid.";
    public static final String MESSAGE_SCORE_NOT_RECORDED = "Not recorded";
    public static final String MESSAGE_SAVE_FAILURE = "Could not save %s: %s. Nothing was changed.";
    public static final String MESSAGE_SAVE_CAUSE_FOLDER = "it is a folder, not a file";
    public static final String MESSAGE_SAVE_CAUSE_READ_ONLY_FILE = "the file is read-only";
    public static final String MESSAGE_SAVE_CAUSE_UNREADABLE_FILE = "the file cannot be read (no permission)";
    public static final String MESSAGE_SAVE_CAUSE_READ_ONLY_FOLDER = "the data folder is read-only";
    public static final String MESSAGE_SAVE_CAUSE_READ_ONLY_HOME_FOLDER = "the home folder is read-only";
    public static final String MESSAGE_SAVE_CAUSE_READ_ONLY_OTHER_FOLDER = "the folder %s is read-only";
    public static final String MESSAGE_SAVE_CAUSE_BLOCKED_FOLDER = "a file named %s is in the way of the data folder";
    public static final String MESSAGE_SAVE_CAUSE_MISSING_FOLDER = "the folder %s does not exist";
    public static final String MESSAGE_SAVE_CAUSE_BROKEN_FOLDER_LINK = "%s links to a folder that does not exist";
    public static final String MESSAGE_SAVE_CAUSE_FOLDER_LOCK_FILE = "the lock file %s is a folder, not a file";
    public static final String MESSAGE_SAVE_CAUSE_READ_ONLY_LOCK_FILE = "the lock file %s is read-only";

    /** Returns all six student fields in the Add Student success-message format. */
    public static String format(Student student) {
        return student.getName()
                + "; Level: " + student.getAcademicLevel()
                + "; Subjects: " + student.getSubjects()
                + "; Phone: " + student.getPhone()
                + "; Guardian: " + student.getGuardianName()
                + "; Guardian phone: " + student.getGuardianPhone();
    }

    /**
     * Returns the owner's name and phone, and the homework's title, subject and due date in the homework message
     * format.
     */
    public static String format(Student student, Homework homework) {
        return formatLessonOwner(student)
                + "; Title: " + homework.getTitle()
                + "; Subject: " + homework.getSubject()
                + "; Due: " + homework.getDueDate();
    }

    /**
     * Returns the owner's name and phone, and the regular lesson's subject, weekday and time range.
     */
    public static String format(Student student, RegularLesson lesson) {
        return formatLessonOwner(student)
                + "; " + lesson.getSubject()
                + "; " + lesson.getDayOfWeek().name().substring(0, 3)
                + "; " + lesson.getStartTime() + "-" + lesson.getEndTime();
    }

    /**
     * Returns an unambiguous lesson or homework owner, including the phone that distinguishes same-name students.
     */
    public static String formatLessonOwner(Student student) {
        return student.getName() + " (" + student.getPhone() + ")";
    }

    /**
     * Returns the homework in the format of {@link #format(Student, Homework)}, followed by its score, or
     * {@link #MESSAGE_SCORE_NOT_RECORDED} if it has none.
     */
    public static String formatWithScore(Student student, Homework homework) {
        return format(student, homework) + "; Score: " + formatScore(homework);
    }

    /** Returns the score of {@code homework}, or {@link #MESSAGE_SCORE_NOT_RECORDED} if it has none. */
    public static String formatScore(Homework homework) {
        return homework.getScore().map(Score::toString).orElse(MESSAGE_SCORE_NOT_RECORDED);
    }

}
