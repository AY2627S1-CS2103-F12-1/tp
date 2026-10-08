package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.logic.commands.LessonListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Routes lesson subcommands to their argument parsers. */
public class LessonCommandParser implements Parser<Command> {
    public static final String COMMAND_WORD = "lesson";
    public static final String SUBCOMMAND_ADD = "add";
    public static final String SUBCOMMAND_LIST = "list";
    public static final String MESSAGE_INVALID_FORMAT = String.format(Messages.MESSAGE_INVALID_FORMAT_WITH_USAGE,
            String.join("\n", LessonAddCommand.MESSAGE_USAGE, LessonListCommand.MESSAGE_USAGE));

    @Override
    public Command parse(String args) throws ParseException {
        requireNonNull(args);
        String[] parts = args.strip().split("\\s+", 2);
        String commandArgs = parts.length > 1 ? parts[1] : "";
        return switch (parts[0]) {
            case SUBCOMMAND_ADD -> new LessonAddCommandParser().parse(commandArgs);
            case SUBCOMMAND_LIST -> new LessonListCommandParser().parse(commandArgs);
            default -> throw new ParseException(MESSAGE_INVALID_FORMAT);
        };
    }
}
