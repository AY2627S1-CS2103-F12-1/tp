package seedu.address.logic.parser;

import seedu.address.logic.commands.Command;
import seedu.address.logic.parser.exceptions.ParseException;

/** Routes lesson subcommands to their argument parsers. */
public class LessonCommandParser implements Parser<Command> {
    public static final String COMMAND_WORD = "lesson";

    @Override
    public Command parse(String args) throws ParseException {
        String[] parts = args.strip().split("\\s+", 2);
        if (!parts[0].equals("add")) {
            throw new ParseException(LessonAddCommandParser.MESSAGE_FORMAT);
        }
        return new LessonAddCommandParser().parse(parts.length == 2 ? parts[1] : "");
    }
}
