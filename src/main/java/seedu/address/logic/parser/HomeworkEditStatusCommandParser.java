package seedu.address.logic.parser;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.HomeworkEditStatusCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Placeholder parser for a command that will mark a student's homework record as assigned or completed.
 * TODO: Implement this parser; it is not yet wired into {@link HomeworkCommandParser}.
 */
public class HomeworkEditStatusCommandParser implements Parser<HomeworkEditStatusCommand> {
    @Override
    public HomeworkEditStatusCommand parse(String args) throws ParseException {
        throw new ParseException(Messages.MESSAGE_NOT_IMPLEMENTED);
    }
}
