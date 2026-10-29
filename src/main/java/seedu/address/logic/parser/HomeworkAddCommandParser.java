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
    public static final String PREFIX_TITLE = "title/";
    public static final String PREFIX_TITLE_ALIAS = "t/";
    public static final String PREFIX_SUBJECT = "s/";
    public static final String PREFIX_DUE_DATE = "due/";
    public static final String MESSAGE_TITLE_AND_ALIAS = "Use either title/ or t/, not both.";

    /** Prefixes accepted by both {@code homework add} and {@code homework edit}. */
    static final List<String> ALLOWED_PREFIXES = List.of(PREFIX_TITLE, PREFIX_TITLE_ALIAS, PREFIX_SUBJECT,
            PREFIX_DUE_DATE);

    private static final String MESSAGE_INVALID_FORMAT = String.format(Messages.MESSAGE_INVALID_FORMAT_WITH_USAGE,
            HomeworkAddCommand.MESSAGE_USAGE);

    private final Clock clock;

    /**
     * Creates a parser that infers the year of short-form due dates from {@code clock}.
     */
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
        validateTitlePrefixes(values);

        String preamble = tokenizedArguments.preamble();
        validateStudentIndexStructure(preamble);

        String rawTitle = values.getOrDefault(PREFIX_TITLE, values.get(PREFIX_TITLE_ALIAS));
        validateRequiredPrefixes(values, rawTitle);

        Index studentIndex = ParserUtil.parseStrictIndex(preamble, Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX,
                Messages.MESSAGE_STUDENT_INDEX_TOO_LARGE);
        String rawDueDate = values.get(PREFIX_DUE_DATE);
        try {
            Homework homework = new Homework(new Title(rawTitle), TuitionSubject.parse(values.get(PREFIX_SUBJECT)),
                    DueDate.parse(rawDueDate, LocalDate.now(clock)));
            return new HomeworkAddCommand(studentIndex, homework, DueDate.isShortForm(rawDueDate));
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }

    /**
     * Checks that the title prefix and its alias are not both present in {@code values}.
     * Also used by {@code homework edit}.
     *
     * @throws ParseException If both title prefixes are present.
     */
    static void validateTitlePrefixes(Map<String, String> values) throws ParseException {
        if (values.containsKey(PREFIX_TITLE) && values.containsKey(PREFIX_TITLE_ALIAS)) {
            throw new ParseException(MESSAGE_TITLE_AND_ALIAS);
        }
    }

    /**
     * Checks that {@code preamble} contains exactly one word for the student index.
     *
     * @throws ParseException If the index is missing or the preamble contains extra words.
     */
    private static void validateStudentIndexStructure(String preamble) throws ParseException {
        if (preamble.isEmpty() || preamble.split("\\s+").length != 1) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }
    }

    /**
     * Checks that {@code values} contains all required prefixes, accepting either title prefix.
     * A null {@code rawTitle} means neither title prefix is present; an empty title is validated later.
     *
     * @throws ParseException If prefixes are missing, listed in the order title, subject, and due date.
     */
    private static void validateRequiredPrefixes(Map<String, String> values, String rawTitle) throws ParseException {
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
            throw new ParseException(String.format(Messages.MESSAGE_MISSING_PREFIXES_WITH_USAGE,
                    String.join(", ", missingPrefixes), HomeworkAddCommand.MESSAGE_USAGE));
        }
    }
}
