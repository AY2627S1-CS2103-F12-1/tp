package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

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
 * Lists the homework of a student identified by its displayed index, in insertion order with one-based indices.
 * The model is not changed.
 */
public class HomeworkListCommand extends Command {
    public static final String MESSAGE_USAGE = "homework|hw list|ls STUDENT_INDEX";
    public static final String MESSAGE_SUCCESS = "Listed %d homework %s for %s.\nHomework for %s:";
    public static final String MESSAGE_HOMEWORK_LINE = "\n%d. %s (%s) - due %s";
    public static final String MESSAGE_NO_HOMEWORK = "No homework found for %s.";
    public static final String MESSAGE_RECORD_SINGULAR = "record";
    public static final String MESSAGE_RECORD_PLURAL = "records";
    public static final String MESSAGE_INTERNAL_ERROR =
            "TutorFlow could not display the student's homework due to an internal error.";

    private static final Logger logger = LogsCenter.getLogger(HomeworkListCommand.class);

    private final Index studentIndex;

    /**
     * Creates a command that lists the homework of the student at {@code studentIndex}.
     */
    public HomeworkListCommand(Index studentIndex) {
        this.studentIndex = requireNonNull(studentIndex);
    }

    /**
     * {@inheritDoc}
     * An unexpected error is logged and reported as an internal error.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            return listHomework(model);
        } catch (RuntimeException exception) {
            logger.log(Level.SEVERE, "Unexpected error while listing homework", exception);
            throw new CommandException(MESSAGE_INTERNAL_ERROR, exception);
        }
    }

    /**
     * Returns the result listing the homework of the selected student.
     */
    private CommandResult listHomework(Model model) throws CommandException {
        List<Student> students = model.getStudentList();
        if (studentIndex.getZeroBased() >= students.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        }

        Student student = students.get(studentIndex.getZeroBased());
        List<Homework> homeworks = student.getHomeworks();
        if (homeworks.isEmpty()) {
            return new CommandResult(String.format(MESSAGE_NO_HOMEWORK, student.getName()));
        }

        String recordWord = homeworks.size() == 1 ? MESSAGE_RECORD_SINGULAR : MESSAGE_RECORD_PLURAL;
        StringBuilder message = new StringBuilder(String.format(MESSAGE_SUCCESS, homeworks.size(), recordWord,
                student.getName(), student.getName()));
        for (int i = 0; i < homeworks.size(); i++) {
            Homework homework = homeworks.get(i);
            message.append(String.format(MESSAGE_HOMEWORK_LINE, i + 1, homework.getTitle(), homework.getSubject(),
                    homework.getDueDate()));
        }
        return new CommandResult(message.toString());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof HomeworkListCommand otherCommand)) {
            return false;
        }

        return studentIndex.equals(otherCommand.studentIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .toString();
    }
}
