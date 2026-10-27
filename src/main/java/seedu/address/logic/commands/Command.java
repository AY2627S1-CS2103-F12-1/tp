package seedu.address.logic.commands;

import seedu.address.logic.Messages;
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
     * @param shownName Name of the data file as the user knows it, e.g. {@code data/tutorflow.json}.
     * @param cause Plain description of why saving failed, e.g. {@code the data folder is read-only}.
     * @return The message to show to the user.
     */
    public String getSaveFailureMessage(String shownName, String cause) {
        return String.format(Messages.MESSAGE_SAVE_FAILURE, shownName, cause);
    }

}
