package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.HomeworkAddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.homework.DueDate;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.Title;
import seedu.address.model.student.TuitionSubject;

/**
 * Parses the arguments of {@code homework add} into a {@link HomeworkAddCommand}.
 * Errors are checked in this order, and only the first one found is reported:
 * unknown or repeated prefix (in the order the prefixes appear), both {@code title/} and {@code t/} given,
 * a student index that is missing or not a single word, missing prefixes, student index syntax, title, subject,
 * and due date.
 * The {@code title/} and {@code t/} conflict is checked only after all prefixes are tokenized, so an unknown or
 * repeated prefix anywhere in the input is reported first (e.g. {@code t/A title/B x/C} reports {@code x/}).
 */
public class HomeworkAddCommandParser implements Parser<HomeworkAddCommand> {
    public static final String MESSAGE_MISSING_PREFIXES = "Missing required prefix(es): %s\nUsage: "
            + HomeworkAddCommand.MESSAGE_USAGE;
    public static final String PREFIX_TITLE = "title/";
    public static final String PREFIX_TITLE_ALIAS = "t/";
    public static final String PREFIX_SUBJECT = "s/";
    public static final String PREFIX_DUE_DATE = "due/";

    private static final String MESSAGE_INVALID_FORMAT = String.format(Messages.MESSAGE_INVALID_FORMAT_WITH_USAGE,
            HomeworkAddCommand.MESSAGE_USAGE);
    private static final List<String> ALLOWED_PREFIXES = List.of(PREFIX_TITLE, PREFIX_TITLE_ALIAS, PREFIX_SUBJECT,
            PREFIX_DUE_DATE);

    private final Clock clock;

    /** Creates a parser that infers the year of short-form due dates from the system clock. */
    public HomeworkAddCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Creates a parser that infers the year of short-form due dates from {@code clock}. */
    public HomeworkAddCommandParser(Clock clock) {
        this.clock = requireNonNull(clock);
    }

    /**
     * Returns a command that adds the homework described by {@code args} to the selected student.
     *
     * @throws ParseException If the command structure or any value is invalid.
     */
    @Override
    public HomeworkAddCommand parse(String args) throws ParseException {
        requireNonNull(args);
        StrictArgumentTokenizer.TokenizedArguments tokenizedArguments =
                StrictArgumentTokenizer.tokenizeWithPreamble(args, ALLOWED_PREFIXES, MESSAGE_INVALID_FORMAT);
        Map<String, String> values = tokenizedArguments.values();
        if (values.containsKey(PREFIX_TITLE) && values.containsKey(PREFIX_TITLE_ALIAS)) {
            throw new ParseException(String.format(StrictArgumentTokenizer.MESSAGE_REPEATED_PREFIX, PREFIX_TITLE));
        }

        String preamble = tokenizedArguments.preamble();
        if (preamble.isEmpty() || preamble.split("\\s+").length != 1) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }

        String rawTitle = values.getOrDefault(PREFIX_TITLE, values.get(PREFIX_TITLE_ALIAS));
        List<String> missingPrefixes = new ArrayList<>();
        if (rawTitle == null) {
            missingPrefixes.add(PREFIX_TITLE);
        }
        if (!values.containsKey(PREFIX_SUBJECT)) {
            missingPrefixes.add(PREFIX_SUBJECT);
        }
        if (!values.containsKey(PREFIX_DUE_DATE)) {
            missingPrefixes.add(PREFIX_DUE_DATE);
        }
        if (!missingPrefixes.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_MISSING_PREFIXES, String.join(", ", missingPrefixes)));
        }

        Index studentIndex = ParserUtil.parseStrictIndex(preamble, Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);
        String rawDueDate = values.get(PREFIX_DUE_DATE);
        try {
            Homework homework = new Homework(new Title(rawTitle), TuitionSubject.parse(values.get(PREFIX_SUBJECT)),
                    DueDate.parse(rawDueDate, LocalDate.now(clock)));
            return new HomeworkAddCommand(studentIndex, homework, DueDate.isShortForm(rawDueDate));
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }
}
