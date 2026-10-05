package seedu.address.logic.parser;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";

    private static final Pattern STRICT_INDEX_PATTERN = Pattern.compile("[1-9][0-9]*");

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it, ignoring surrounding whitespace.
     * Unlike {@link #parseIndex(String)}, signs and leading zeroes are rejected.
     *
     * @param oneBasedIndex Positive whole number matching {@code [1-9][0-9]*} and fitting in an {@code int}.
     * @param invalidSyntaxMessage Message of the exception thrown if {@code oneBasedIndex} is invalid.
     * @return The parsed index.
     * @throws ParseException If {@code oneBasedIndex} is not a positive whole number without leading zeroes, or is
     *         larger than {@link Integer#MAX_VALUE}.
     */
    public static Index parseStrictIndex(String oneBasedIndex, String invalidSyntaxMessage) throws ParseException {
        requireAllNonNull(oneBasedIndex, invalidSyntaxMessage);
        String strippedIndex = oneBasedIndex.strip();
        if (!STRICT_INDEX_PATTERN.matcher(strippedIndex).matches()) {
            throw new ParseException(invalidSyntaxMessage);
        }
        try {
            return Index.fromOneBased(Integer.parseInt(strippedIndex));
        } catch (NumberFormatException exception) {
            throw new ParseException(invalidSyntaxMessage, exception);
        }
    }

}
