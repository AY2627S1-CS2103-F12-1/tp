package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.util.Locale;
import java.util.regex.Pattern;

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
    /** The subcommands and their aliases, named when the subcommand is missing or unknown. */
    public static final String MESSAGE_SUBCOMMANDS =
            "Use add, list|ls, upcoming|up, edit|e, marks|m, delete|del, filter or status.";
    public static final String MESSAGE_MISSING_SUBCOMMAND = "Missing homework subcommand. " + MESSAGE_SUBCOMMANDS;
    public static final String MESSAGE_UNKNOWN_SUBCOMMAND = "%s is not a homework subcommand. " + MESSAGE_SUBCOMMANDS;
    public static final String MESSAGE_COMMAND_WORD_NOT_LOWERCASE = "Command words are lowercase: type %s, not %s.";
    public static final String MESSAGE_SUBCOMMAND_NOT_LOWERCASE =
            "Homework subcommands are lowercase: type %s, not %s.\nUsage: %s";

    /** A Unicode space separator, such as a no-break space (U+00A0), which is read as an ordinary space. */
    private static final Pattern UNICODE_SPACE = Pattern.compile("\\p{Zs}");

    private final Clock clock;

    /** Creates a parser whose subcommands take today's date from {@code clock}. */
    public HomeworkCommandParser(Clock clock) {
        this.clock = requireNonNull(clock);
    }

    /**
     * Returns the command for the subcommand named by the first word of {@code args}.
     * In the arguments of the subcommands other than {@code filter} and {@code status}, a Unicode space separator is
     * read as an ordinary space.
     *
     * @throws ParseException If the subcommand is missing or unknown, or its arguments are invalid.
     */
    @Override
    public Command parse(String args) throws ParseException {
        requireNonNull(args);
        String[] subcommandAndArguments = args.strip().split("\\s+", 2);
        String subcommandArguments = subcommandAndArguments.length > 1 ? subcommandAndArguments[1] : "";
        String homeworkArguments = UNICODE_SPACE.matcher(subcommandArguments).replaceAll(" ");
        return switch (subcommandAndArguments[0]) {
            case SUBCOMMAND_STATUS -> new HomeworkStatusCommandParser().parse(subcommandArguments);
            case SUBCOMMAND_FILTER -> new HomeworkFilterCommandParser(clock).parse(subcommandArguments);
            case SUBCOMMAND_ADD -> new HomeworkAddCommandParser(clock).parse(homeworkArguments);
            case SUBCOMMAND_LIST, SUBCOMMAND_LIST_ALIAS ->
                new HomeworkListCommandParser(clock).parse(homeworkArguments);
            case SUBCOMMAND_UPCOMING, SUBCOMMAND_UPCOMING_ALIAS ->
                new HomeworkUpcomingCommandParser(clock).parse(homeworkArguments);
            case SUBCOMMAND_EDIT, SUBCOMMAND_EDIT_ALIAS ->
                new HomeworkEditCommandParser(clock).parse(homeworkArguments);
            case SUBCOMMAND_MARKS, SUBCOMMAND_MARKS_ALIAS ->
                new HomeworkMarksCommandParser().parse(homeworkArguments);
            case SUBCOMMAND_DELETE, SUBCOMMAND_DELETE_ALIAS ->
                new HomeworkDeleteCommandParser(clock).parse(homeworkArguments);
            default -> throw new ParseException(buildUnknownSubcommandMessage(subcommandAndArguments[0]));
        };
    }

    /**
     * Checks that {@code commandWord}, which is not a command word, is not {@code homework} or {@code hw} typed in
     * another case.
     *
     * @throws ParseException If it is, with a hint to type it in lowercase.
     */
    public static void checkCommandWordCase(String commandWord) throws ParseException {
        if (commandWord.equalsIgnoreCase(COMMAND_WORD) || commandWord.equalsIgnoreCase(COMMAND_ALIAS)) {
            throw new ParseException(String.format(MESSAGE_COMMAND_WORD_NOT_LOWERCASE,
                    commandWord.toLowerCase(Locale.ROOT), commandWord));
        }
    }

    /**
     * Returns the error for {@code subcommand}, which is not a subcommand: if it is empty, that a subcommand is
     * missing; if it is one in another case, a hint to type it in lowercase and the usage of that subcommand;
     * otherwise that it is not a subcommand. A missing or unknown subcommand error names every subcommand.
     */
    private static String buildUnknownSubcommandMessage(String subcommand) {
        if (subcommand.isEmpty()) {
            return MESSAGE_MISSING_SUBCOMMAND;
        }
        String lowercaseSubcommand = subcommand.toLowerCase(Locale.ROOT);
        String usage = switch (lowercaseSubcommand) {
            case SUBCOMMAND_STATUS -> HomeworkStatusCommand.MESSAGE_USAGE;
            case SUBCOMMAND_FILTER -> HomeworkFilterCommand.MESSAGE_USAGE;
            case SUBCOMMAND_ADD -> HomeworkAddCommand.MESSAGE_USAGE;
            case SUBCOMMAND_LIST, SUBCOMMAND_LIST_ALIAS -> HomeworkListCommand.MESSAGE_USAGE;
            case SUBCOMMAND_UPCOMING, SUBCOMMAND_UPCOMING_ALIAS -> HomeworkUpcomingCommand.MESSAGE_USAGE;
            case SUBCOMMAND_EDIT, SUBCOMMAND_EDIT_ALIAS -> HomeworkEditCommand.MESSAGE_USAGE;
            case SUBCOMMAND_MARKS, SUBCOMMAND_MARKS_ALIAS -> HomeworkMarksCommand.MESSAGE_USAGE;
            case SUBCOMMAND_DELETE, SUBCOMMAND_DELETE_ALIAS -> HomeworkDeleteCommand.MESSAGE_USAGE;
            default -> null;
        };
        return usage == null ? String.format(MESSAGE_UNKNOWN_SUBCOMMAND, subcommand)
                : String.format(MESSAGE_SUBCOMMAND_NOT_LOWERCASE, lowercaseSubcommand, subcommand, usage);
    }
}
