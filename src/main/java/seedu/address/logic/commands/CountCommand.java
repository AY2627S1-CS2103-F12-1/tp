package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;

/**
 * Reports the number of people in the currently displayed list.
 */
public class CountCommand extends Command {

    public static final String COMMAND_WORD = "count";
    public static final String MESSAGE_SUCCESS = "%d person(s) in the displayed list.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        return new CommandResult(String.format(MESSAGE_SUCCESS, model.getFilteredPersonList().size()));
    }
}
