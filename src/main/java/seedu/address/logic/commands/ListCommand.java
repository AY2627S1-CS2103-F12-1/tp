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
    public static final String MESSAGE_SUCCESS_SINGULAR = "Listed 1 student.";
    public static final String MESSAGE_SUCCESS = "Listed %d students.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredStudentList(Model.PREDICATE_SHOW_ALL_STUDENTS);
        int count = model.getStudentList().size();
        String feedback;
        if (count == 0) {
            feedback = MESSAGE_EMPTY;
        } else if (count == 1) {
            feedback = MESSAGE_SUCCESS_SINGULAR;
        } else {
            feedback = String.format(MESSAGE_SUCCESS, count);
        }
        return new CommandResult(feedback, false, false, true);
    }
}
