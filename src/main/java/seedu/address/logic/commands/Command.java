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
     * been undone: the format of {@link #getSaveFailureFormat()} filled in with the file's name and the cause.
     *
     * @param shownName Name of the data file as the user knows it, e.g. {@code data/tutorflow.json}.
     * @param cause Plain description of why saving failed, e.g. {@code the data folder is read-only}.
     * @return The message to show to the user.
     */
    public final String getSaveFailureMessage(String shownName, String cause) {
        return String.format(getSaveFailureFormat(), shownName, cause);
    }

    /**
     * Returns the format of the save-failure message, whose two {@code %s} are the data file's name and the cause.
     * Commands override this to describe the failure in their own words.
     */
    protected String getSaveFailureFormat() {
        return Messages.MESSAGE_SAVE_FAILURE;
    }

}
