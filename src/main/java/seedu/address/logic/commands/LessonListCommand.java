package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.LessonSchedule;
import seedu.address.model.student.Student;

/**
 * Lists regular lessons in weekly order, optionally for one displayed student.
 */
public class LessonListCommand extends Command {
    public static final String MESSAGE_USAGE = "lesson list [STUDENT_INDEX]";
    public static final String MESSAGE_SUCCESS = "Listed %d regular lessons.";
    public static final String MESSAGE_SUCCESS_SINGULAR = "Listed 1 regular lesson.";
    public static final String MESSAGE_EMPTY = "No regular lessons found.";
    public static final String MESSAGE_STUDENT_EMPTY = "No regular lessons found for %s.";
    public static final String MESSAGE_STUDENT_SUCCESS = "Listed %d regular %s for %s.";
    public static final String MESSAGE_LESSON_LINE = "\n%d. %s";
    public static final String MESSAGE_INTERNAL_ERROR =
            "TutorFlow could not display the regular lesson schedule due to an internal error.";

    private static final Logger logger = LogsCenter.getLogger(LessonListCommand.class);

    private final Optional<Index> studentIndex;

    /** Creates a command that lists the complete weekly schedule. */
    public LessonListCommand() {
        studentIndex = Optional.empty();
    }

    /** Creates a command that lists the lessons of a displayed student, retaining global lesson indices. */
    public LessonListCommand(Index studentIndex) {
        this.studentIndex = Optional.of(requireNonNull(studentIndex));
    }

    /**
     * Returns the result listing every regular lesson in weekly chronological order.
     * An unexpected error is logged and reported without modifying the model.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            return listLessons(model);
        } catch (RuntimeException exception) {
            logger.log(Level.SEVERE, "Unexpected error while listing regular lessons", exception);
            throw new CommandException(MESSAGE_INTERNAL_ERROR, exception);
        }
    }

    /**
     * Returns the command result containing the sorted regular lesson schedule.
     */
    private CommandResult listLessons(Model model) throws CommandException {
        Optional<Student> owner = Optional.empty();
        if (studentIndex.isPresent()) {
            int index = studentIndex.get().getZeroBased();
            if (index >= model.getStudentList().size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
            }
            owner = Optional.of(model.getStudentList().get(index));
        }
        List<LessonSchedule.Entry> lessons = LessonSchedule.from(model.getAddressBook().getStudentList());
        int matches = 0;
        StringBuilder lines = new StringBuilder();
        for (int i = 0; i < lessons.size(); i++) {
            LessonSchedule.Entry entry = lessons.get(i);
            if (owner.isPresent() && !owner.get().equals(entry.student())) {
                continue;
            }
            matches++;
            lines.append(String.format(Locale.ROOT, MESSAGE_LESSON_LINE, i + 1,
                    Messages.format(entry.student(), entry.lesson())));
        }
        if (matches == 0) {
            return new CommandResult(owner.isPresent()
                    ? String.format(Locale.ROOT, MESSAGE_STUDENT_EMPTY, Messages.formatLessonOwner(owner.get()))
                    : MESSAGE_EMPTY);
        }
        String summary = owner.isPresent()
                ? String.format(Locale.ROOT, MESSAGE_STUDENT_SUCCESS, matches, matches == 1 ? "lesson" : "lessons",
                        Messages.formatLessonOwner(owner.get()))
                : matches == 1 ? MESSAGE_SUCCESS_SINGULAR : String.format(Locale.ROOT, MESSAGE_SUCCESS, matches);
        return new CommandResult(summary + lines);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof LessonListCommand command && studentIndex.equals(command.studentIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(LessonListCommand.class, studentIndex.map(Index::getZeroBased));
    }
}
