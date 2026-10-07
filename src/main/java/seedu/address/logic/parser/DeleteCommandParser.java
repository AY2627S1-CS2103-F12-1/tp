package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the displayed student index for the Delete Student command. */
public class DeleteCommandParser implements Parser<DeleteCommand> {

    @Override
    public DeleteCommand parse(String args) throws ParseException {
        String invalidFormatMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE);
        Index index = ParserUtil.parseStrictIndex(args, invalidFormatMessage);
        return new DeleteCommand(index);
    }
}
