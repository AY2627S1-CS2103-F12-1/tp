package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.time.Clock;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.HomeworkAddCommand;
import seedu.address.logic.commands.HomeworkDeleteCommand;
import seedu.address.logic.commands.HomeworkEditCommand;
import seedu.address.logic.commands.HomeworkFilterCommand;
import seedu.address.logic.commands.HomeworkListCommand;
import seedu.address.logic.commands.HomeworkMarksCommand;
import seedu.address.logic.commands.HomeworkStatusCommand;
import seedu.address.logic.commands.HomeworkUpcomingCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the arguments of the {@code homework} command by passing the rest of the arguments to the parser of the
 * subcommand named by the first word.
 */
public class HomeworkCommandParser implements Parser<Command> {
    public static final String COMMAND_WORD = "homework";
    public static final String COMMAND_ALIAS = "hw";
    public static final String SUBCOMMAND_ADD = "add";
    public static final String SUBCOMMAND_LIST = "list";
    public static final String SUBCOMMAND_LIST_ALIAS = "ls";
    public static final String SUBCOMMAND_UPCOMING = "upcoming";
    public static final String SUBCOMMAND_UPCOMING_ALIAS = "up";
    public static final String SUBCOMMAND_EDIT = "edit";
    public static final String SUBCOMMAND_FILTER = "filter";
    public static final String SUBCOMMAND_STATUS = "status";
    public static final String SUBCOMMAND_EDIT_ALIAS = "e";
    public static final String SUBCOMMAND_MARKS = "marks";
    public static final String SUBCOMMAND_MARKS_ALIAS = "m";
    public static final String SUBCOMMAND_DELETE = "delete";
    public static final String SUBCOMMAND_DELETE_ALIAS = "del";
    public static final String MESSAGE_INVALID_FORMAT = String.format(Messages.MESSAGE_INVALID_FORMAT_WITH_USAGE,
            String.join("\n", HomeworkAddCommand.MESSAGE_USAGE, HomeworkListCommand.MESSAGE_USAGE,
                    HomeworkUpcomingCommand.MESSAGE_USAGE, HomeworkEditCommand.MESSAGE_USAGE,
                    HomeworkMarksCommand.MESSAGE_USAGE, HomeworkDeleteCommand.MESSAGE_USAGE,
                    HomeworkFilterCommand.MESSAGE_USAGE, HomeworkStatusCommand.MESSAGE_USAGE));

    private final Clock clock;

    /** Creates a parser whose subcommands take today's date from {@code clock}. */
    public HomeworkCommandParser(Clock clock) {
        this.clock = requireNonNull(clock);
    }

    /**
     * Returns the command for the subcommand named by the first word of {@code args}.
     *
     * @throws ParseException If the subcommand is missing or unknown, or its arguments are invalid.
     */
    @Override
    public Command parse(String args) throws ParseException {
        requireNonNull(args);
        String[] subcommandAndArguments = args.strip().split("\\s+", 2);
        String subcommandArguments = subcommandAndArguments.length > 1 ? subcommandAndArguments[1] : "";
        return switch (subcommandAndArguments[0]) {
            case SUBCOMMAND_STATUS -> new HomeworkStatusCommandParser().parse(subcommandArguments);
            case SUBCOMMAND_FILTER -> new HomeworkFilterCommandParser(clock).parse(subcommandArguments);
            case SUBCOMMAND_ADD -> new HomeworkAddCommandParser(clock).parse(subcommandArguments);
            case SUBCOMMAND_LIST, SUBCOMMAND_LIST_ALIAS ->
                new HomeworkListCommandParser(clock).parse(subcommandArguments);
            case SUBCOMMAND_UPCOMING, SUBCOMMAND_UPCOMING_ALIAS ->
                new HomeworkUpcomingCommandParser(clock).parse(subcommandArguments);
            case SUBCOMMAND_EDIT, SUBCOMMAND_EDIT_ALIAS ->
                new HomeworkEditCommandParser(clock).parse(subcommandArguments);
            case SUBCOMMAND_MARKS, SUBCOMMAND_MARKS_ALIAS ->
                new HomeworkMarksCommandParser().parse(subcommandArguments);
            case SUBCOMMAND_DELETE, SUBCOMMAND_DELETE_ALIAS ->
                new HomeworkDeleteCommandParser(clock).parse(subcommandArguments);
            default -> throw new ParseException(MESSAGE_INVALID_FORMAT);
        };
    }
}
