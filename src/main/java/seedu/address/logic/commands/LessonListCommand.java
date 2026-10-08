package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.LessonSchedule;

/**
 * Lists every regular lesson across all students in weekly chronological order.
 */
public class LessonListCommand extends Command {
    public static final String MESSAGE_USAGE = "lesson list";
    public static final String MESSAGE_SUCCESS = "Listed %d regular lessons.";
    public static final String MESSAGE_EMPTY = "No regular lessons found.";
    public static final String MESSAGE_LESSON_LINE = "\n%d. %s";
    public static final String MESSAGE_INTERNAL_ERROR =
            "TutorFlow could not display the regular lesson schedule due to an internal error.";

    private static final Logger logger = LogsCenter.getLogger(LessonListCommand.class);
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
    private CommandResult listLessons(Model model) {
        List<LessonSchedule.Entry> lessons = LessonSchedule.from(model.getStudentList());

        if (lessons.isEmpty()) {
            return new CommandResult(MESSAGE_EMPTY);
        }

        StringBuilder message = new StringBuilder(String.format(MESSAGE_SUCCESS, lessons.size()));
        for (int i = 0; i < lessons.size(); i++) {
            LessonSchedule.Entry entry = lessons.get(i);
            message.append(String.format(MESSAGE_LESSON_LINE, i + 1,
                    Messages.format(entry.student(), entry.lesson())));
        }
        return new CommandResult(message.toString());
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof LessonListCommand;
    }

    @Override
    public int hashCode() {
        return LessonListCommand.class.hashCode();
    }
}
