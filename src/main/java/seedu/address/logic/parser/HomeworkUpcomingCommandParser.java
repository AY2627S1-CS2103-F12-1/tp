package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.HomeworkUpcomingCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the arguments of {@code homework upcoming} into a {@link HomeworkUpcomingCommand}.
 * The arguments must be empty or the single prefix {@code due/} followed by a number of days from 0 to 365.
 */
public class HomeworkUpcomingCommandParser implements Parser<HomeworkUpcomingCommand> {
    public static final String PREFIX_DAYS = "due/";
    public static final int DEFAULT_DAYS = 7;
    public static final int MAX_DAYS = 365;
    /** What the command expects, named when other words are found. */
    public static final String EXPECTED_WORDS = "no words other than " + PREFIX_DAYS + "DAYS";
    public static final String MESSAGE_INVALID_DAYS =
            "Days must be a whole number from 0 to " + MAX_DAYS + " without leading zeroes.";

    private static final String MESSAGE_INVALID_FORMAT = String.format(Messages.MESSAGE_INVALID_FORMAT_WITH_USAGE,
            HomeworkUpcomingCommand.MESSAGE_USAGE);
    /** At most three digits, so the value always fits in an int; leading zeroes only in 0 itself. */
    private static final String DAYS_VALIDATION_REGEX = "0|[1-9][0-9]{0,2}";

    private final Clock clock;

    /**
     * Creates a parser that counts the days from the date of {@code clock}.
     */
    public HomeworkUpcomingCommandParser(Clock clock) {
        this.clock = requireNonNull(clock);
    }

    /**
     * Returns a command that lists the homework due from today to the number of days given in {@code args}, or
     * {@link #DEFAULT_DAYS} days if none is given.
     *
     * @throws ParseException If {@code args} has text other than one {@code due/} prefix, or the number of days is
     *     invalid.
     */
    @Override
    public HomeworkUpcomingCommand parse(String args) throws ParseException {
        requireNonNull(args);
        StrictArgumentTokenizer.TokenizedArguments tokenizedArguments =
                StrictArgumentTokenizer.tokenizeWithPreambleRejectingWrongCase(args, List.of(PREFIX_DAYS),
                        MESSAGE_INVALID_FORMAT);
        if (!tokenizedArguments.preamble().isEmpty()) {
            throw new ParseException(ParserUtil.formatUnexpectedWords(EXPECTED_WORDS,
                    ParserUtil.splitWords(tokenizedArguments.preamble()), HomeworkUpcomingCommand.MESSAGE_USAGE));
        }
        Map<String, String> values = tokenizedArguments.values();
        int days = values.containsKey(PREFIX_DAYS) ? parseDays(values.get(PREFIX_DAYS)) : DEFAULT_DAYS;
        return new HomeworkUpcomingCommand(LocalDate.now(clock), days);
    }

    /**
     * Returns the number of days in {@code rawDays}.
     *
     * @throws ParseException If {@code rawDays} is not a whole number from 0 to {@link #MAX_DAYS} without leading
     *     zeroes.
     */
    private static int parseDays(String rawDays) throws ParseException {
        if (!rawDays.matches(DAYS_VALIDATION_REGEX) || Integer.parseInt(rawDays) > MAX_DAYS) {
            throw new ParseException(MESSAGE_INVALID_DAYS);
        }
        return Integer.parseInt(rawDays);
    }
}
