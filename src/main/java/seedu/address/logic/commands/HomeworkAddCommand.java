package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.model.student.Student;

/**
 * Appends a homework record to the homework list of a student identified by its displayed index.
 */
public class HomeworkAddCommand extends Command {
    public static final String MESSAGE_USAGE =
            "homework|hw add STUDENT_INDEX title/|t/TITLE s/SUBJECT due/YYYY-MM-DD|MM-DD";
    /** Header naming the owner, then the new homework's line in the format of {@code homework list}. */
    public static final String MESSAGE_SUCCESS = "New homework added for %s:%s";
    public static final String MESSAGE_INFERRED_YEAR = "\nInferred year: %d";
    public static final String MESSAGE_DUE_BEFORE_TODAY =
            "\nNote: this homework is due before today, so homework list %d shows it only with all.";
    /** Names the student, the subject they do not take, and the subjects they take. */
    public static final String MESSAGE_SUBJECT_NOT_TAKEN = "%s does not take %s. They take %s.";
    /** Names the student, then the line of the homework that is the same in the format of {@code homework list}. */
    public static final String MESSAGE_DUPLICATE_HOMEWORK = "This homework already exists for %s:%s";
    public static final String MESSAGE_INTERNAL_ERROR =
            "TutorFlow could not add the homework due to an internal error.";
    public static final String MESSAGE_SAVE_FAILURE = "The homework could not be added because TutorFlow could not "
            + "save %s: %s. No homework data was changed.";

    private static final Logger logger = LogsCenter.getLogger(HomeworkAddCommand.class);

    private final Index studentIndex;
    private final Homework homework;
    private final boolean isYearInferred;
    private final LocalDate today;

    /**
     * Creates a command that adds {@code homework} to the student at {@code studentIndex}.
     *
     * @param studentIndex Displayed index of the student receiving the homework.
     * @param homework Homework to add.
     * @param isYearInferred Whether the year of the due date was inferred, so the success message reports it.
     * @param today Date from which homework counts as upcoming, so the success message notes a homework due before it.
     */
    public HomeworkAddCommand(Index studentIndex, Homework homework, boolean isYearInferred, LocalDate today) {
        requireAllNonNull(studentIndex, homework, today);
        this.studentIndex = studentIndex;
        this.homework = homework;
        this.isYearInferred = isYearInferred;
        this.today = today;
    }

    /**
     * {@inheritDoc}
     * An unexpected error is logged and reported as an internal error, leaving the model unchanged.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            return addHomework(model);
        } catch (RuntimeException exception) {
            logger.log(Level.SEVERE, "Unexpected error while adding homework", exception);
            throw new CommandException(MESSAGE_INTERNAL_ERROR, exception);
        }
    }

    /**
     * Returns the result of adding the homework to the selected student. Changing the model is the last step, so
     * an exception thrown before it leaves the model unchanged.
     */
    private CommandResult addHomework(Model model) throws CommandException {
        Student student = HomeworkCommandUtil.getDisplayedStudent(model, studentIndex);
        HomeworkCommandUtil.requireSubjectTaken(student, homework);
        HomeworkCommandUtil.requireNoSameHomework(student, homework, null, MESSAGE_DUPLICATE_HOMEWORK);

        List<Homework> updatedHomeworks = new ArrayList<>(student.getHomeworks());
        updatedHomeworks.add(homework);
        // the new homework is last in insertion order, so its number lets the user edit or delete it right away
        String message = String.format(Locale.ROOT, MESSAGE_SUCCESS, Messages.formatLessonOwner(student),
                HomeworkListCommand.formatHomeworkLine(updatedHomeworks.size(), homework));
        if (isYearInferred) {
            // Locale.ROOT prints the year in ASCII digits in every locale, like the due date before it
            message += String.format(Locale.ROOT, MESSAGE_INFERRED_YEAR, homework.getDueDate().getDate().getYear());
        }
        if (!homework.isUpcoming(today)) {
            // Locale.ROOT prints the student index in ASCII digits, which the index parsers accept, in every locale
            message += String.format(Locale.ROOT, MESSAGE_DUE_BEFORE_TODAY, studentIndex.getOneBased());
        }

        model.setStudent(student, student.withHomeworks(updatedHomeworks));
        return new CommandResult(message);
    }

    @Override
    protected String getSaveFailureFormat() {
        return MESSAGE_SAVE_FAILURE;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof HomeworkAddCommand otherCommand)) {
            return false;
        }

        return studentIndex.equals(otherCommand.studentIndex)
                && homework.equals(otherCommand.homework)
                && isYearInferred == otherCommand.isYearInferred
                && today.equals(otherCommand.today);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex.getZeroBased(), homework, isYearInferred, today);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .add("homework", homework)
                .add("isYearInferred", isYearInferred)
                .add("today", today)
                .toString();
    }
}
