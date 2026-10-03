package seedu.address.logic.parser;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses Add Student arguments into an {@link AddCommand}.
 */
public class AddCommandParser implements Parser<AddCommand> {
    private final StudentAddParser studentParser = new StudentAddParser();

    /**
     * Returns an Add Student command with all six required fields.
     *
     * @throws ParseException If the command structure or a field value is invalid.
     */
    @Override
    public AddCommand parse(String arguments) throws ParseException {
        return new AddCommand(studentParser.parse(arguments));
    }
}
