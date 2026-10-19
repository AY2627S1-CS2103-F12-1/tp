package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

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
import seedu.address.model.lesson.LessonSchedule;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.Student;

/**
 * Deletes a regular lesson identified by its one-based index in the lesson schedule.
 */
public class LessonDeleteCommand extends Command {
    public static final String MESSAGE_USAGE = "lesson delete LESSON_INDEX";
    public static final String MESSAGE_SUCCESS = "Deleted regular lesson:\n%s";
    public static final String MESSAGE_NO_LESSONS = "There are no regular lessons to delete.";
    public static final String MESSAGE_INTERNAL_ERROR =
            "TutorFlow could not delete the lesson due to an internal error.";
    public static final String MESSAGE_SAVE_FAILURE = "The lesson could not be deleted because TutorFlow could not "
            + "save the updated data. No lesson data was changed.";

    private static final Logger logger = LogsCenter.getLogger(LessonDeleteCommand.class);

    private final Index displayedLessonIndex;

    /**
     * Creates a command that deletes the lesson at {@code displayedLessonIndex} in the displayed schedule.
     */
    public LessonDeleteCommand(Index displayedLessonIndex) {
        this.displayedLessonIndex = requireNonNull(displayedLessonIndex);
    }

    /**
     * Deletes the selected lesson, logging unexpected errors without exposing implementation details.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            return deleteSelectedLesson(model);
        } catch (RuntimeException exception) {
            logger.log(Level.SEVERE, "Unexpected error while deleting a lesson", exception);
            throw new CommandException(MESSAGE_INTERNAL_ERROR, exception);
        }
    }

    /**
     * Resolves the displayed index from the same sorted schedule used by {@link LessonListCommand}.
     */
    private CommandResult deleteSelectedLesson(Model model) throws CommandException {
        List<LessonSchedule.Entry> orderedLessonSchedule = LessonSchedule.from(model.getAddressBook().getStudentList());
        if (orderedLessonSchedule.isEmpty()) {
            throw new CommandException(MESSAGE_NO_LESSONS);
        }
        if (displayedLessonIndex.getZeroBased() >= orderedLessonSchedule.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
        }

        LessonSchedule.Entry selectedLessonEntry = orderedLessonSchedule.get(displayedLessonIndex.getZeroBased());
        Student lessonOwner = selectedLessonEntry.student();
        RegularLesson selectedLesson = selectedLessonEntry.lesson();
        List<RegularLesson> updatedLessons = new ArrayList<>(lessonOwner.getLessons());
        updatedLessons.remove(selectedLesson);

        model.setStudent(lessonOwner, lessonOwner.withLessons(updatedLessons));
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                Messages.formatDeletedLesson(lessonOwner, selectedLesson)));
    }

    @Override
    public String getSaveFailureMessage(String defaultMessage) {
        return MESSAGE_SAVE_FAILURE;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof LessonDeleteCommand command
                && displayedLessonIndex.equals(command.displayedLessonIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("displayedLessonIndex", displayedLessonIndex).toString();
    }
}
