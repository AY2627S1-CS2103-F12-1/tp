package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.model.student.Student;

/**
 * Deletes one homework record, identified by its index in the homework list of a student identified by its
 * displayed index. The other homework keeps its relative order. The result shows the deleted record in the
 * {@code homework list} layout, followed by the student's upcoming homework with its new numbers.
 */
public class HomeworkDeleteCommand extends Command {
    public static final String MESSAGE_USAGE = "homework|hw delete|del STUDENT_INDEX HOMEWORK_INDEX";
    /** Deleted record (a line that starts with a line break), then the student's upcoming homework. */
    public static final String MESSAGE_SUCCESS = "Deleted homework:%s\n%s";
    public static final String MESSAGE_NO_HOMEWORK = "The selected student has no homework to delete.";
    public static final String MESSAGE_INTERNAL_ERROR =
            "TutorFlow could not delete the homework due to an internal error.";
    public static final String MESSAGE_SAVE_FAILURE = "The homework could not be deleted because TutorFlow could not "
            + "save the updated data. No homework data was changed.";

    private static final Logger logger = LogsCenter.getLogger(HomeworkDeleteCommand.class);

    private final Index studentIndex;
    private final Index homeworkIndex;
    private final LocalDate today;

    /**
     * Creates a command that deletes the homework at {@code homeworkIndex} of the student at {@code studentIndex}.
     *
     * @param studentIndex Index of the student in the displayed student list.
     * @param homeworkIndex Index of the homework in that student's homework list.
     * @param today Date from which homework counts as upcoming in the homework list shown on success.
     */
    public HomeworkDeleteCommand(Index studentIndex, Index homeworkIndex, LocalDate today) {
        requireAllNonNull(studentIndex, homeworkIndex, today);
        this.studentIndex = studentIndex;
        this.homeworkIndex = homeworkIndex;
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
            return deleteHomework(model);
        } catch (RuntimeException exception) {
            logger.log(Level.SEVERE, "Unexpected error while deleting homework", exception);
            throw new CommandException(MESSAGE_INTERNAL_ERROR, exception);
        }
    }

    /**
     * Returns the result of deleting the homework from the selected student. Changing the model is the last step,
     * so an exception thrown before it leaves the model unchanged.
     */
    private CommandResult deleteHomework(Model model) throws CommandException {
        Student student = HomeworkCommandUtil.getDisplayedStudent(model, studentIndex);
        Homework homeworkToDelete = HomeworkCommandUtil.getHomework(student, homeworkIndex, MESSAGE_NO_HOMEWORK);

        List<Homework> remainingHomeworks = new ArrayList<>(student.getHomeworks());
        remainingHomeworks.remove(homeworkIndex.getZeroBased());
        Student editedStudent = student.withHomeworks(remainingHomeworks);
        String message = String.format(MESSAGE_SUCCESS,
                HomeworkListCommand.formatHomeworkLine(homeworkIndex.getOneBased(), homeworkToDelete),
                HomeworkListCommand.formatHomeworkList(editedStudent, today, false));

        model.setStudent(student, editedStudent);
        return new CommandResult(message);
    }

    @Override
    public String getSaveFailureMessage(String defaultMessage) {
        return MESSAGE_SAVE_FAILURE;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof HomeworkDeleteCommand otherCommand)) {
            return false;
        }

        return studentIndex.equals(otherCommand.studentIndex)
                && homeworkIndex.equals(otherCommand.homeworkIndex)
                && today.equals(otherCommand.today);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex.getZeroBased(), homeworkIndex.getZeroBased(), today);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .add("homeworkIndex", homeworkIndex)
                .add("today", today)
                .toString();
    }
}
