package seedu.address.logic.commands;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Placeholder for a command that will record the score a student received for a homework record.
 * TODO: Implement this command; it is not yet wired into any parser.
 */
public class HomeworkRecordScoreCommand extends Command {
    @Override
    public CommandResult execute(Model model) throws CommandException {
        throw new CommandException(Messages.MESSAGE_NOT_IMPLEMENTED);
    }
}
