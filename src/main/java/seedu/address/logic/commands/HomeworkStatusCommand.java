package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.student.Student;

/**
 * Marks homework completed or reopens it without changing its score or insertion index.
 */
public class HomeworkStatusCommand extends Command {
    public static final String MESSAGE_USAGE = "homework|hw status STUDENT_INDEX HOMEWORK_INDEX status/STATUS";
    public static final String MESSAGE_SUCCESS = "Updated homework status:\n%s; Status: %s";
    public static final String MESSAGE_NO_HOMEWORK = "The selected student has no homework to update status for.";
    public static final String MESSAGE_UNCHANGED = "No changes made; the homework already has this status.";
    public static final String MESSAGE_INTERNAL = "TutorFlow could not update homework status "
            + "due to an internal error.";
    public static final String MESSAGE_SAVE_FAILURE = "The homework status could not be updated because TutorFlow "
            + "could not save %s: %s. No homework data was changed.";
    private static final Logger logger = LogsCenter.getLogger(HomeworkStatusCommand.class);

    private final Index studentIndex;
    private final Index homeworkIndex;
    private final HomeworkStatus status;

    /**
     * Creates a command targeting the displayed student and original homework insertion index.
     */
    public HomeworkStatusCommand(Index studentIndex, Index homeworkIndex, HomeworkStatus status) {
        requireAllNonNull(studentIndex, homeworkIndex, status);
        this.studentIndex = studentIndex;
        this.homeworkIndex = homeworkIndex;
        this.status = status;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            return updateStatus(model);
        } catch (RuntimeException exception) {
            logger.log(Level.SEVERE, "Unexpected error while updating homework status", exception);
            throw new CommandException(MESSAGE_INTERNAL, exception);
        }
    }

    /**
     * Replaces one homework at its insertion index, preserving its score, details and the owner's other records.
     * Invalid targets and unchanged status are rejected before the model is modified.
     */
    private CommandResult updateStatus(Model model) throws CommandException {
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
        Homework homework = homeworks.get(homeworkIndex.getZeroBased());
        if (homework.getStatus() == status) {
            throw new CommandException(MESSAGE_UNCHANGED);
        }
        Homework updated = homework.withStatus(status);
        List<Homework> replacements = new ArrayList<>(homeworks);
        replacements.set(homeworkIndex.getZeroBased(), updated);
        CommandResult result = new CommandResult(String.format(MESSAGE_SUCCESS,
                Messages.formatWithScore(student, updated), status));
        model.setStudent(student, student.withHomeworks(replacements));
        return result;
    }

    @Override
    public String getSaveFailureMessage(String shownName, String cause) {
        return String.format(MESSAGE_SAVE_FAILURE, shownName, cause);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof HomeworkStatusCommand command
                && studentIndex.equals(command.studentIndex) && homeworkIndex.equals(command.homeworkIndex)
                && status == command.status;
    }
}
