package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import seedu.address.logic.commands.Command;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Contains helper methods for testing command parsers.
 */
public class CommandParserTestUtil {
    /** Hint added when the arguments contain a curly quote, on its own line before the usage line. */
    public static final String MESSAGE_CURLY_QUOTES = "Use straight quotes (\"), not curly quotes (“ ”).";
    /**
     * First line of the error for a homework command given both the title prefix and its alias outside a title.
     * The usage line follows it on the next line.
     */
    public static final String MESSAGE_TITLE_AND_ALIAS = "Use either title/ or t/, not both.";

    /**
     * Returns the first line of the error for {@code word}, an unknown prefix that is not inside any value
     * (e.g. right after the indices). The usage line follows it on the next line.
     */
    public static String notPrefix(String word) {
        return word + " is not a prefix of this command.";
    }

    /**
     * Returns the first line of the error for {@code word}, an unknown prefix inside a value whose text before the
     * word is only digits and date or time punctuation, so no example is given.
     */
    public static String notPrefixWithQuoteHint(String word) {
        return notPrefix(word) + " To use it inside a value, wrap the whole value in straight double quotes.";
    }

    /**
     * Returns the first line of the error for {@code word}, an unknown prefix inside a value, with
     * {@code example} (the field's prefix and its whole text in quotes) showing how to quote it.
     */
    public static String notPrefixWithQuoteHint(String word, String example) {
        return notPrefix(word) + " To use it inside a value, wrap the whole value in straight double quotes, e.g. "
                + example + ".";
    }

    /**
     * Returns the first line of the error for {@code word}, an allowed prefix typed in another case (e.g.
     * {@code Due/} for {@code due/}) that is not inside any value. The usage line follows it on the next line.
     */
    public static String wrongCasePrefix(String word, String prefix) {
        return word + " is not a prefix of this command; prefixes are lowercase (" + prefix + ").";
    }

    /**
     * Returns the first line of the error for {@code word}, an allowed prefix typed in another case inside a value,
     * with {@code example} (the field's prefix and its whole text in quotes) showing how to quote it.
     */
    public static String wrongCasePrefixWithQuoteHint(String word, String prefix, String example) {
        return wrongCasePrefix(word, prefix) + " To use it inside a value, wrap the whole value in straight double "
                + "quotes, e.g. " + example + ".";
    }

    /**
     * Returns the first line of the error for {@code prefix}, given more than once where quoting cannot help.
     * The usage line follows it on the next line.
     */
    public static String givenOnce(String prefix) {
        return "Prefix " + prefix + " can be given only once.";
    }

    /**
     * Returns the first line of the error for {@code prefix}, given more than once where one of them is in a
     * free-text value, with {@code example} (the field's prefix and its whole text in quotes) showing how to quote it.
     */
    public static String givenOnceWithQuoteHint(String prefix, String example) {
        return givenOnce(prefix) + " To use it inside a value, wrap the whole value in straight double quotes, e.g. "
                + example + ".";
    }

    /**
     * Returns the first line of the error for a homework command given both the title prefix and its alias, where
     * the later one is in a title, with {@code example} showing how to quote that title.
     */
    public static String titleAndAliasWithQuoteHint(String example) {
        return MESSAGE_TITLE_AND_ALIAS + " To use it inside a value, wrap the whole value in straight double quotes, "
                + "e.g. " + example + ".";
    }

    /**
     * Returns the error for a homework command whose words before the first prefix are not the {@code expected}
     * ones, where {@code found} describes them (e.g. {@code 3 words (1 1 extra)} or {@code none}), followed by the
     * usage line of {@code usage}.
     */
    public static String unexpectedWords(String expected, String found, String usage) {
        return "Expected " + expected + ", but found " + found + ".\nUsage: " + usage;
    }

    /**
     * Asserts that the parsing of {@code userInput} by {@code parser} is successful and the command created
     * is equal to {@code expectedCommand}.
     */
    public static void assertParseSuccess(Parser<? extends Command> parser, String userInput,
            Command expectedCommand) {
        try {
            Command command = parser.parse(userInput);
            assertEquals(expectedCommand, command);
        } catch (ParseException pe) {
            throw new IllegalArgumentException("Invalid userInput.", pe);
        }
    }

    /**
     * Asserts that the parsing of {@code userInput} by {@code parser} is unsuccessful and the error message
     * is equal to {@code expectedMessage}.
     */
    public static void assertParseFailure(Parser<? extends Command> parser, String userInput, String expectedMessage) {
        try {
            parser.parse(userInput);
            throw new AssertionError("The expected ParseException was not thrown.");
        } catch (ParseException pe) {
            assertEquals(expectedMessage, pe.getMessage());
        }
    }
}
