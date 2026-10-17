package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.Score;
import seedu.address.model.student.Student;

/**
 * Records or replaces the marks of one homework record, identified by its index in the homework list of a student
 * identified by its displayed index.
 */
public class HomeworkMarksCommand extends Command {
    public static final String MESSAGE_USAGE = "homework|hw marks|m STUDENT_INDEX HOMEWORK_INDEX score/SCORE";
    public static final String MESSAGE_SUCCESS = "Recorded homework marks:\n%s";
    public static final String MESSAGE_NO_HOMEWORK = "The selected student has no homework to record marks for.";
    public static final String MESSAGE_NO_CHANGES = "No changes made; the homework already has this score.";
    public static final String MESSAGE_INTERNAL_ERROR =
            "TutorFlow could not record the homework marks due to an internal error.";
    public static final String MESSAGE_SAVE_FAILURE = "The homework marks could not be recorded because TutorFlow "
            + "could not save the updated data. No homework data was changed.";

    private static final Logger logger = LogsCenter.getLogger(HomeworkMarksCommand.class);

    private final Index studentIndex;
    private final Index homeworkIndex;
    private final Score score;

    /**
     * Creates a command that records {@code score} for the homework at {@code homeworkIndex} of the student at
     * {@code studentIndex}.
     *
     * @param studentIndex Index of the student in the displayed student list.
     * @param homeworkIndex Index of the homework in that student's homework list.
     * @param score Marks to record.
     */
    public HomeworkMarksCommand(Index studentIndex, Index homeworkIndex, Score score) {
        requireAllNonNull(studentIndex, homeworkIndex, score);
        this.studentIndex = studentIndex;
        this.homeworkIndex = homeworkIndex;
        this.score = score;
    }

    /**
     * {@inheritDoc}
     * An unexpected error is logged and reported as an internal error, leaving the model unchanged.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            return recordMarks(model);
        } catch (RuntimeException exception) {
            logger.log(Level.SEVERE, "Unexpected error while recording homework marks", exception);
            throw new CommandException(MESSAGE_INTERNAL_ERROR, exception);
        }
    }

    /**
     * Returns the result of recording the marks for the homework of the selected student. Changing the model is the
     * last step, so an exception thrown before it leaves the model unchanged.
     */
    private CommandResult recordMarks(Model model) throws CommandException {
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
        if (homework.getScore().equals(Optional.of(score))) {
            throw new CommandException(MESSAGE_NO_CHANGES);
        }

        Homework scoredHomework = new Homework(homework.getTitle(), homework.getSubject(), homework.getDueDate(),
                score);
        List<Homework> updatedHomeworks = new ArrayList<>(homeworks);
        updatedHomeworks.set(homeworkIndex.getZeroBased(), scoredHomework);
        String message = String.format(MESSAGE_SUCCESS, Messages.formatWithScore(student, scoredHomework));

        model.setStudent(student, student.withHomeworks(updatedHomeworks));
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
        if (!(other instanceof HomeworkMarksCommand otherCommand)) {
            return false;
        }

        return studentIndex.equals(otherCommand.studentIndex)
                && homeworkIndex.equals(otherCommand.homeworkIndex)
                && score.equals(otherCommand.score);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .add("homeworkIndex", homeworkIndex)
                .add("score", score)
                .toString();
    }
}
