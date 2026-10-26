package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
 * Records, replaces or clears the marks of one homework record, identified by its index in the homework list of a
 * student identified by its displayed index.
 */
public class HomeworkMarksCommand extends Command {
    public static final String MESSAGE_USAGE = "homework|hw marks|m STUDENT_INDEX HOMEWORK_INDEX score/[SCORE]";
    public static final String MESSAGE_SUCCESS = "Recorded homework marks:\n%s";
    public static final String MESSAGE_CLEAR_SUCCESS = "Cleared homework marks:\n%s";
    public static final String MESSAGE_NO_HOMEWORK = "The selected student has no homework to record marks for.";
    public static final String MESSAGE_NO_CHANGES = "No changes made; the homework already has this score.";
    public static final String MESSAGE_NO_MARKS_TO_CLEAR = "No changes made; the homework has no recorded marks.";
    public static final String MESSAGE_INTERNAL_ERROR =
            "TutorFlow could not record the homework marks due to an internal error.";
    public static final String MESSAGE_SAVE_FAILURE = "The homework marks could not be recorded because TutorFlow "
            + "could not save the updated data. No homework data was changed.";

    private static final Logger logger = LogsCenter.getLogger(HomeworkMarksCommand.class);

    private final Index studentIndex;
    private final Index homeworkIndex;
    /** Marks to record, or null to clear the recorded marks. */
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
     * Creates a command that clears the marks of the homework at {@code homeworkIndex} of the student at
     * {@code studentIndex}.
     *
     * @param studentIndex Index of the student in the displayed student list.
     * @param homeworkIndex Index of the homework in that student's homework list.
     */
    public HomeworkMarksCommand(Index studentIndex, Index homeworkIndex) {
        requireAllNonNull(studentIndex, homeworkIndex);
        this.studentIndex = studentIndex;
        this.homeworkIndex = homeworkIndex;
        this.score = null;
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
     * Returns the result of recording or clearing the marks for the homework of the selected student. Changing the
     * model is the last step, so an exception thrown before it leaves the model unchanged.
     */
    private CommandResult recordMarks(Model model) throws CommandException {
        Student student = HomeworkCommandUtil.getDisplayedStudent(model, studentIndex);
        Homework homework = HomeworkCommandUtil.getHomework(student, homeworkIndex, MESSAGE_NO_HOMEWORK);
        if (homework.getScore().equals(Optional.ofNullable(score))) {
            throw new CommandException(score == null ? MESSAGE_NO_MARKS_TO_CLEAR : MESSAGE_NO_CHANGES);
        }

        Homework scoredHomework = new Homework(homework.getTitle(), homework.getSubject(), homework.getDueDate(),
                score, homework.getStatus());
        List<Homework> updatedHomeworks = new ArrayList<>(student.getHomeworks());
        updatedHomeworks.set(homeworkIndex.getZeroBased(), scoredHomework);
        String message = String.format(score == null ? MESSAGE_CLEAR_SUCCESS : MESSAGE_SUCCESS,
                Messages.formatWithScore(student, scoredHomework));

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
                && Objects.equals(score, otherCommand.score);
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
