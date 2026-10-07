package seedu.address.logic.parser;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.HomeworkEditCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Placeholder parser for a command that will edit the title, subject or due date of a student's homework record.
 * TODO: Implement this parser; it is not yet wired into {@link HomeworkCommandParser}.
 */
public class HomeworkEditCommandParser implements Parser<HomeworkEditCommand> {
    @Override
    public HomeworkEditCommand parse(String args) throws ParseException {
        throw new ParseException(Messages.MESSAGE_NOT_IMPLEMENTED);
    }
}
