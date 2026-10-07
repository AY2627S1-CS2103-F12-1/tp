package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Represents a command with hidden internal logic and the ability to be executed.
 */
public abstract class Command {

    /**
     * Executes the command and returns the result message.
     *
     * @param model {@code Model} which the command should operate on.
     * @return feedback message of the operation result for display
     * @throws CommandException If an error occurs during command execution.
     */
    public abstract CommandResult execute(Model model) throws CommandException;

    /**
     * Returns the message shown when the data changed by this command could not be saved, after the change has
     * been undone. Commands override this to describe the failure in their own words.
     *
     * @param defaultMessage Message describing the save error, shown if the command has no message of its own.
     * @return The message to show to the user.
     */
    public String getSaveFailureMessage(String defaultMessage) {
        return defaultMessage;
    }

}
