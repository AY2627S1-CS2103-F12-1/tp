package seedu.address.logic.commands;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Placeholder for a command that will edit the title, subject or due date of a student's homework record.
 * TODO: Implement this command; it is not yet wired into any parser.
 */
public class HomeworkEditCommand extends Command {
    @Override
    public CommandResult execute(Model model) throws CommandException {
        throw new CommandException(Messages.MESSAGE_NOT_IMPLEMENTED);
    }
}
