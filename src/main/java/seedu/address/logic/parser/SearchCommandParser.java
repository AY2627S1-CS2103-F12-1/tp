package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.SearchCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses a non-blank search keyword. */
public class SearchCommandParser implements Parser<SearchCommand> {
    @Override
    public SearchCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (args.isBlank()) {
            throw new ParseException(SearchCommand.MESSAGE_INVALID_FORMAT);
        }
        return new SearchCommand(args);
    }
}
