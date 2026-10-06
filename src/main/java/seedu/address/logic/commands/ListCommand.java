package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;

/**
 * Shows all students in the roster.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";
    public static final String SHORT_COMMAND_WORD = "ls";
    public static final String MESSAGE_USAGE = "Usage: list";
    public static final String MESSAGE_INVALID_FORMAT = "Invalid command format.\n" + MESSAGE_USAGE;
    public static final String MESSAGE_EMPTY = "No students found.";
    public static final String MESSAGE_SUCCESS = "Listed %d students.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        int count = model.getStudentList().size();
        String feedback = count == 0 ? MESSAGE_EMPTY : String.format(MESSAGE_SUCCESS, count);
        return new CommandResult(feedback, false, false, true);
    }
}
