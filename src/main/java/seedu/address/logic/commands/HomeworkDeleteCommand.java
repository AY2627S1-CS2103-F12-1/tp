package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;
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
 * Deletes one homework record, identified by its index in the homework list of a student identified by its
 * displayed index. The other homework keeps its relative order.
 */
public class HomeworkDeleteCommand extends Command {
    public static final String MESSAGE_USAGE = "homework|hw delete|del STUDENT_INDEX HOMEWORK_INDEX";
    public static final String MESSAGE_SUCCESS = "Deleted homework:\n%s";
    public static final String MESSAGE_NO_HOMEWORK = "The selected student has no homework to delete.";
    public static final String MESSAGE_INTERNAL_ERROR =
            "TutorFlow could not delete the homework due to an internal error.";

    private static final Logger logger = LogsCenter.getLogger(HomeworkDeleteCommand.class);

    private final Index studentIndex;
    private final Index homeworkIndex;

    /**
     * Creates a command that deletes the homework at {@code homeworkIndex} of the student at {@code studentIndex}.
     *
     * @param studentIndex Index of the student in the displayed student list.
     * @param homeworkIndex Index of the homework in that student's homework list.
     */
    public HomeworkDeleteCommand(Index studentIndex, Index homeworkIndex) {
        requireAllNonNull(studentIndex, homeworkIndex);
        this.studentIndex = studentIndex;
        this.homeworkIndex = homeworkIndex;
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
        List<Student> students = model.getStudentList();
        if (studentIndex.getZeroBased() >= students.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        }

        Student student = students.get(studentIndex.getZeroBased());
        List<Homework> homeworks = student.getHomeworks();
        if (homeworks.isEmpty()) {
            throw new CommandException(MESSAGE_NO_HOMEWORK);
        }
        if (homeworkIndex.getZeroBased() >= homeworks.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_HOMEWORK_DISPLAYED_INDEX);
        }

        List<Homework> remainingHomeworks = new ArrayList<>(homeworks);
        Homework deletedHomework = remainingHomeworks.remove(homeworkIndex.getZeroBased());
        String message = String.format(MESSAGE_SUCCESS, Messages.format(student, deletedHomework));

        model.setStudent(student, student.withHomeworks(remainingHomeworks));
        return new CommandResult(message);
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
                && homeworkIndex.equals(otherCommand.homeworkIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .add("homeworkIndex", homeworkIndex)
                .toString();
    }
}
