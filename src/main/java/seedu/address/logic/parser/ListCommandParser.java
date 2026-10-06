package seedu.address.logic.parser;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses arguments for the List Student command.
 */
public class ListCommandParser implements Parser<ListCommand> {

    @Override
    public ListCommand parse(String args) throws ParseException {
        if (!args.isBlank()) {
            throw new ParseException(ListCommand.MESSAGE_INVALID_FORMAT);
        }
        return new ListCommand();
    }
}
