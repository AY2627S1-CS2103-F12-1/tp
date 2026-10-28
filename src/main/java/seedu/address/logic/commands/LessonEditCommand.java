package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.LessonSchedule;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.Student;
import seedu.address.model.student.TuitionSubject;

/**
 * Edits a lesson at its weekly schedule index while preserving its owner and insertion position.
 */
public class LessonEditCommand extends Command {
    public static final String MESSAGE_USAGE = "lesson edit LESSON_INDEX [s/SUBJECT] [d/DAY]"
            + " [st/START_TIME] [et/END_TIME]";
    public static final String MESSAGE_SUCCESS = "Edited regular lesson: %s; %s";
    public static final String MESSAGE_UNCHANGED = "The lesson is unchanged.";
    public static final String MESSAGE_NO_LESSONS = "There are no regular lessons to edit.";
    public static final String MESSAGE_MULTIPLE_CLASHES = "This lesson clashes with the following existing lessons:\n";
    public static final String MESSAGE_INTERNAL = "TutorFlow could not edit the lesson due to an internal error.";
    public static final String MESSAGE_SAVE_FAILURE = "The lesson could not be edited because TutorFlow could not "
            + "save %s: %s. No lesson data was changed.";
    private static final Logger logger = LogsCenter.getLogger(LessonEditCommand.class);

    private final Index lessonIndex;
    private final EditLessonDescriptor descriptor;

    /**
     * Creates a command to edit the lesson numbered by {@code lesson list}.
     */
    public LessonEditCommand(Index lessonIndex, EditLessonDescriptor descriptor) {
        requireAllNonNull(lessonIndex, descriptor);
        this.lessonIndex = lessonIndex;
        this.descriptor = descriptor;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            return editLesson(model);
        } catch (RuntimeException exception) {
            logger.log(Level.SEVERE, "Unexpected error while editing a lesson", exception);
            throw new CommandException(MESSAGE_INTERNAL, exception);
        }
    }

    /**
     * Resolves the target in the full weekly schedule and validates a replacement before updating its owner.
     * The original owner/lesson pair is excluded from clash checks, even when the owner is hidden in the student view.
     */
    private CommandResult editLesson(Model model) throws CommandException {
        List<Student> students = model.getAddressBook().getStudentList();
        List<LessonSchedule.Entry> schedule = LessonSchedule.buildSchedule(students);
        if (schedule.isEmpty()) {
            throw new CommandException(MESSAGE_NO_LESSONS);
        }
        if (lessonIndex.getZeroBased() >= schedule.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
        }
        LessonSchedule.Entry original = schedule.get(lessonIndex.getZeroBased());
        Student owner = original.student();
        RegularLesson replacement;
        try {
            replacement = descriptor.applyTo(original.lesson());
        } catch (IllegalArgumentException exception) {
            throw new CommandException(exception.getMessage(), exception);
        }
        if (!owner.getSubjects().getSubjects().contains(replacement.getSubject())) {
            throw new CommandException(LessonAddCommand.MESSAGE_SUBJECT);
        }
        if (replacement.equals(original.lesson())) {
            throw new CommandException(MESSAGE_UNCHANGED);
        }
        if (owner.getLessons().contains(replacement)) {
            throw new CommandException(LessonAddCommand.MESSAGE_DUPLICATE);
        }
        List<String> clashes = LessonSchedule.findClashes(students, replacement, original).stream()
                .map(entry -> Messages.formatLessonOwner(entry.student()) + "; " + entry.lesson()).toList();
        if (!clashes.isEmpty()) {
            throw new CommandException(clashes.size() == 1 ? LessonAddCommand.MESSAGE_CLASH + clashes.get(0)
                    : MESSAGE_MULTIPLE_CLASHES + String.join("\n", clashes));
        }
        List<RegularLesson> updated = new ArrayList<>(owner.getLessons());
        updated.set(updated.indexOf(original.lesson()), replacement);
        model.setStudent(owner, owner.withLessons(updated));
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.formatLessonOwner(owner), replacement));
    }

    @Override
    protected String getSaveFailureFormat() {
        return MESSAGE_SAVE_FAILURE;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof LessonEditCommand command
                && lessonIndex.equals(command.lessonIndex) && descriptor.equals(command.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lessonIndex.getZeroBased(), descriptor);
    }

    /**
     * Immutable partial edit; null fields retain the corresponding original value.
     */
    public record EditLessonDescriptor(TuitionSubject subject, DayOfWeek day, LocalTime start, LocalTime end) {
        /**
         * Rejects an edit without any supplied fields.
         */
        public EditLessonDescriptor {
            if (subject == null && day == null && start == null && end == null) {
                throw new IllegalArgumentException("Specify at least one lesson field to edit.");
            }
        }

        /**
         * Builds and validates the complete proposed lesson using original values for omitted fields.
         */
        public RegularLesson applyTo(RegularLesson original) {
            requireNonNull(original);
            return new RegularLesson(subject == null ? original.getSubject() : subject,
                    day == null ? original.getDayOfWeek() : day,
                    start == null ? original.getStartTime() : start,
                    end == null ? original.getEndTime() : end);
        }
    }
}
