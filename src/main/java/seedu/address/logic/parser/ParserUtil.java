package seedu.address.logic.parser;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Locale;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    private static final Pattern STRICT_INDEX_PATTERN = Pattern.compile("[1-9][0-9]*");

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it, ignoring surrounding whitespace.
     * Signs and leading zeroes are rejected.
     *
     * @param oneBasedIndex Positive whole number matching {@code [1-9][0-9]*} and fitting in an {@code int}.
     * @param invalidSyntaxMessage Message of the exception thrown if {@code oneBasedIndex} is invalid.
     * @return The parsed index.
     * @throws ParseException If {@code oneBasedIndex} is not a positive whole number without leading zeroes, or is
     *         larger than {@link Integer#MAX_VALUE}.
     */
    public static Index parseStrictIndex(String oneBasedIndex, String invalidSyntaxMessage) throws ParseException {
        return parseStrictIndex(oneBasedIndex, invalidSyntaxMessage, invalidSyntaxMessage);
    }

    /**
     * Parses a strict index, using a separate error when a positive whole number exceeds the supported range.
     */
    public static Index parseStrictIndex(String oneBasedIndex, String invalidSyntaxMessage,
            String outOfRangeMessage) throws ParseException {
        requireAllNonNull(oneBasedIndex, invalidSyntaxMessage, outOfRangeMessage);
        String strippedIndex = oneBasedIndex.strip();
        if (!STRICT_INDEX_PATTERN.matcher(strippedIndex).matches()) {
            throw new ParseException(invalidSyntaxMessage);
        }
        try {
            return Index.fromOneBased(Integer.parseInt(strippedIndex));
        } catch (NumberFormatException exception) {
            throw new ParseException(outOfRangeMessage, exception);
        }
    }

    /**
     * Returns the words of {@code text}, separated by whitespace, or no words if it is blank.
     */
    public static String[] splitWords(String text) {
        return text.isBlank() ? new String[0] : text.strip().split("\\s+");
    }

    /**
     * Returns the error for a command given {@code words} where it expects {@code expected}, saying how many words
     * were found and what they are, followed by {@code usage}.
     *
     * @param expected What the command expects, e.g. {@code STUDENT_INDEX HOMEWORK_INDEX}.
     * @param words Words found instead.
     * @param usage Usage text of the command.
     * @return The error message.
     */
    public static String formatUnexpectedWords(String expected, String[] words, String usage) {
        String joinedWords = String.join(" ", words);
        // Locale.ROOT prints the count in ASCII digits in every locale
        String foundWords = switch (words.length) {
            case 0 -> Messages.MESSAGE_NO_WORDS;
            case 1 -> String.format(Messages.MESSAGE_ONE_WORD, joinedWords);
            default -> String.format(Locale.ROOT, Messages.MESSAGE_WORDS, words.length, joinedWords);
        };
        return String.format(Messages.MESSAGE_UNEXPECTED_WORDS_WITH_USAGE, expected, foundWords, usage);
    }
}
