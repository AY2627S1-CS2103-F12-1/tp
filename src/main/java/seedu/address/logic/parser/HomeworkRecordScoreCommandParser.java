package seedu.address.logic.parser;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.HomeworkRecordScoreCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Placeholder parser for a command that will record the score a student received for a homework record.
 * TODO: Implement this parser; it is not yet wired into {@link HomeworkCommandParser}.
 */
public class HomeworkRecordScoreCommandParser implements Parser<HomeworkRecordScoreCommand> {
    @Override
    public HomeworkRecordScoreCommand parse(String args) throws ParseException {
        throw new ParseException(Messages.MESSAGE_NOT_IMPLEMENTED);
    }
}
