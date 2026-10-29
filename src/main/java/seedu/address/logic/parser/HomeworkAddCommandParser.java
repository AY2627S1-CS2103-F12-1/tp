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
import seedu.address.logic.parser.StrictArgumentTokenizer.RepeatedPrefixRules;
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
 * The {@code title/} and {@code t/} conflict is reported only after all prefixes are checked, so an unknown or
 * repeated prefix anywhere in the input is reported first (e.g. {@code t/A title/B x/C} reports {@code x/}).
 * An unknown, wrong-case or repeated prefix, or the conflict, in a title gets a hint to quote the title, with a fixed
 * example title that never contains the user's text.
 */
public class HomeworkAddCommandParser implements Parser<HomeworkAddCommand> {
    public static final String PREFIX_TITLE = "title/";
    public static final String PREFIX_TITLE_ALIAS = "t/";
    public static final String PREFIX_SUBJECT = "s/";
    public static final String PREFIX_DUE_DATE = "due/";
    public static final String MESSAGE_TITLE_AND_ALIAS = "Use either title/ or t/, not both.";
    /** Hint to quote a title that holds the given prefix-shaped word, with an example title holding such a word. */
    public static final String MESSAGE_TITLE_QUOTE_HINT = " If %s is part of the title, put the whole title in "
            + "straight double quotes, e.g. %s.";
    public static final String MESSAGE_SUBJECT_EMPTY = "The subject is empty. " + TuitionSubject.MESSAGE_CONSTRAINTS;
    public static final String MESSAGE_NOT_SUBJECT = "%s is not a subject. " + TuitionSubject.MESSAGE_CONSTRAINTS;
    /** What the command expects before its prefixes, named when other words are found. */
    public static final String EXPECTED_WORDS = "STUDENT_INDEX before the first prefix";

    /** Prefixes accepted by both {@code homework add} and {@code homework edit}. */
    static final List<String> ALLOWED_PREFIXES = List.of(PREFIX_TITLE, PREFIX_TITLE_ALIAS, PREFIX_SUBJECT,
            PREFIX_DUE_DATE);

    /**
     * How {@code homework add} and {@code homework edit} explain a repeated prefix: titles are free text, and
     * {@code title/} and {@code t/} name the same field.
     */
    static final RepeatedPrefixRules REPEATED_PREFIX_RULES = new RepeatedPrefixRules(
            List.of(PREFIX_TITLE, PREFIX_TITLE_ALIAS), List.of(PREFIX_TITLE, PREFIX_TITLE_ALIAS),
            MESSAGE_TITLE_AND_ALIAS, HomeworkAddCommandParser::buildTitleQuoteHint);

    private static final String MESSAGE_INVALID_FORMAT = String.format(Messages.MESSAGE_INVALID_FORMAT_WITH_USAGE,
            HomeworkAddCommand.MESSAGE_USAGE);
    // Example titles in the quoting hint, each typed as shown giving the title in its quotes
    private static final String EXAMPLE_TITLE_WITH_TITLE_PREFIX = "t/\"True t/f quiz\"";
    private static final String EXAMPLE_TITLE_WITH_SUBJECT_PREFIX = "t/\"Read s/o notes\"";
    private static final String EXAMPLE_TITLE_WITH_OTHER_PREFIX = "t/\"Pg 5 w/ answers\"";

    private final Clock clock;

    /**
     * Creates a parser that infers the year of short-form due dates, and finds today, from {@code clock}.
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
                StrictArgumentTokenizer.tokenizeWithPreambleRejectingWrongCase(args, ALLOWED_PREFIXES,
                        MESSAGE_INVALID_FORMAT, REPEATED_PREFIX_RULES);
        Map<String, String> values = tokenizedArguments.values();
        String preamble = tokenizedArguments.preamble();
        validateStudentIndexStructure(preamble);

        String rawTitle = values.getOrDefault(PREFIX_TITLE, values.get(PREFIX_TITLE_ALIAS));
        validateRequiredPrefixes(values, rawTitle);

        Index studentIndex = ParserUtil.parseStrictIndex(preamble, Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX,
                Messages.MESSAGE_STUDENT_INDEX_TOO_LARGE);
        String rawDueDate = values.get(PREFIX_DUE_DATE);
        boolean isSubjectInTitle = tokenizedArguments.prefixesInText().contains(PREFIX_SUBJECT);
        try {
            LocalDate today = LocalDate.now(clock);
            Homework homework = new Homework(new Title(rawTitle),
                    parseSubject(values.get(PREFIX_SUBJECT), isSubjectInTitle), DueDate.parse(rawDueDate, today));
            return new HomeworkAddCommand(studentIndex, homework, DueDate.isShortForm(rawDueDate), today);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }

    /**
     * Returns the hint to quote a title that holds {@code word}, with a fixed example title that holds {@code t/} if
     * {@code word} is a title prefix, {@code s/} if it is the subject prefix, or {@code w/} otherwise.
     */
    static String buildTitleQuoteHint(String word) {
        String example = switch (word) {
            case PREFIX_TITLE, PREFIX_TITLE_ALIAS -> EXAMPLE_TITLE_WITH_TITLE_PREFIX;
            case PREFIX_SUBJECT -> EXAMPLE_TITLE_WITH_SUBJECT_PREFIX;
            default -> EXAMPLE_TITLE_WITH_OTHER_PREFIX;
        };
        return String.format(MESSAGE_TITLE_QUOTE_HINT, word, example);
    }

    /**
     * Returns the subject in {@code rawSubject}, ignoring surrounding whitespace and case.
     *
     * @param isInTitle Whether {@code s/} directly follows an unquoted title, so may be part of it.
     * @throws ParseException If {@code rawSubject} is empty, or names no subject, naming it and, if
     *     {@code isInTitle}, with a hint to quote the title.
     */
    static TuitionSubject parseSubject(String rawSubject, boolean isInTitle) throws ParseException {
        String trimmedSubject = rawSubject.strip();
        if (trimmedSubject.isEmpty()) {
            // a title ending with s/ is unlikely, so there is no hint to quote it
            throw new ParseException(MESSAGE_SUBJECT_EMPTY);
        }
        try {
            return TuitionSubject.parse(trimmedSubject);
        } catch (IllegalArgumentException exception) {
            String quoteHint = isInTitle ? buildTitleQuoteHint(PREFIX_SUBJECT) : "";
            throw new ParseException(String.format(MESSAGE_NOT_SUBJECT, trimmedSubject) + quoteHint, exception);
        }
    }

    /**
     * Checks that {@code preamble} contains exactly one word for the student index.
     *
     * @throws ParseException If the index is missing or the preamble contains extra words, naming the words found.
     */
    private static void validateStudentIndexStructure(String preamble) throws ParseException {
        String[] words = ParserUtil.splitWords(preamble);
        if (words.length != 1) {
            throw new ParseException(ParserUtil.formatUnexpectedWords(EXPECTED_WORDS, words,
                    HomeworkAddCommand.MESSAGE_USAGE));
        }
    }

    /**
     * Checks that {@code values} contains all required prefixes, accepting either title prefix.
     * A null {@code rawTitle} means neither title prefix is present; an empty title is validated later.
     *
     * @throws ParseException If prefixes are missing, listed in the order title (naming its alias), subject, and due
     *     date.
     */
    private static void validateRequiredPrefixes(Map<String, String> values, String rawTitle) throws ParseException {
        List<String> missingPrefixes = new ArrayList<>();
        if (rawTitle == null) {
            missingPrefixes.add(PREFIX_TITLE + " (or " + PREFIX_TITLE_ALIAS + ")");
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
