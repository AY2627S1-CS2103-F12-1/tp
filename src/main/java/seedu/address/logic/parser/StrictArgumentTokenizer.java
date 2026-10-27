package seedu.address.logic.parser;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Tokenizes arguments of the form {@code preamble prefix/value prefix/value ...}, rejecting unknown and repeated
 * prefixes.
 * Any lowercase word followed by {@code /} at the start of the arguments or after whitespace is treated as a prefix,
 * so a {@code /} inside a value (e.g. {@code n/A/P Roe}) does not start a new prefix.
 * A value wrapped in double quotes may contain prefix-shaped words (e.g. {@code t/"Speed in km/h"}); the outer quotes
 * and the whitespace at both ends inside them are removed from the value. A quoted value ends at the first {@code "}
 * that is followed by the end of the arguments, or by whitespace and an allowed prefix, so quotes inside the value
 * are kept. A value without such a closing quote is read as if it were not quoted.
 * Errors are reported in the order the offending text appears. An unknown prefix is named in the error, with a hint
 * to quote the value it is in, followed by the usage text of the command.
 */
public final class StrictArgumentTokenizer {
    public static final String MESSAGE_REPEATED_PREFIX = "Prefix %s must be specified exactly once.";
    public static final String MESSAGE_NOT_PREFIX = "%s is not a prefix of this command.";
    public static final String MESSAGE_QUOTE_HINT = " To use it inside a value, wrap the whole value in straight "
            + "double quotes.";
    public static final String MESSAGE_QUOTE_HINT_WITH_EXAMPLE = " To use it inside a value, wrap the whole value "
            + "in straight double quotes, e.g. %s\"%s\".";
    public static final String MESSAGE_CURLY_QUOTES = "Use straight quotes (\"), not curly quotes (\u201c \u201d).";

    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?<!\\S)([a-z]+/)");
    private static final Pattern WHITESPACE_THEN_PREFIX_PATTERN = Pattern.compile("\\s+([a-z]+/)");
    /** A prefix after whitespace or directly after a straight or curly quote, which ends a field in an example. */
    private static final Pattern FIELD_END_PREFIX_PATTERN = Pattern.compile("(?<![^\\s\"\u201c\u201d])([a-z]+/)");
    /** Text made only of digits, date or time punctuation and spaces, which needs no quoting example. */
    private static final Pattern NUMERIC_TEXT_PATTERN = Pattern.compile("[0-9\\-/:.+ ]*");
    private static final String QUOTE_CHARACTERS = "\"\u201c\u201d";
    private static final String CURLY_QUOTE_CHARACTERS = "\u201c\u201d";
    private static final String USAGE_START = "Usage:";
    private static final char QUOTE = '"';
    private static final ValueTokenFilter NO_VALUE_TOKENS = (arguments, prefixEnd, previousPrefix, prefix) -> false;

    private StrictArgumentTokenizer() {
    }

    /**
     * Returns the preamble (the stripped text before the first prefix) and the prefixed values in {@code arguments}.
     *
     * @param arguments Arguments to tokenize.
     * @param allowedPrefixes Prefixes that may appear, e.g. {@code "t/"}.
     * @param invalidFormatMessage Message for an unknown prefix.
     * @return The tokenized arguments.
     * @throws ParseException If a prefix is unknown or repeated.
     */
    public static TokenizedArguments tokenizeWithPreamble(String arguments, List<String> allowedPrefixes,
            String invalidFormatMessage) throws ParseException {
        return tokenize(arguments, allowedPrefixes, invalidFormatMessage, MESSAGE_REPEATED_PREFIX,
                true, NO_VALUE_TOKENS);
    }

    /** Returns the preamble and fields, keeping prefix-shaped name tokens accepted by {@code valueTokenFilter}. */
    public static TokenizedArguments tokenizeWithPreamble(String arguments, List<String> allowedPrefixes,
            String invalidFormatMessage, ValueTokenFilter valueTokenFilter) throws ParseException {
        return tokenize(arguments, allowedPrefixes, invalidFormatMessage, MESSAGE_REPEATED_PREFIX,
                true, valueTokenFilter);
    }

    /**
     * Returns the prefixed values in {@code arguments}, which must not contain any text before the first prefix.
     *
     * @param arguments Arguments to tokenize.
     * @param allowedPrefixes Prefixes that may appear, e.g. {@code "n/"}.
     * @param invalidFormatMessage Message for text before the first prefix or an unknown prefix.
     * @return The tokenized arguments, with an empty preamble.
     * @throws ParseException If there is text before the first prefix, or a prefix is unknown or repeated.
     */
    public static TokenizedArguments tokenize(String arguments, List<String> allowedPrefixes,
            String invalidFormatMessage) throws ParseException {
        return tokenize(arguments, allowedPrefixes, invalidFormatMessage, NO_VALUE_TOKENS);
    }

    /**
     * Returns prefixed values using the supplied format and repeated-prefix error messages.
     * The repeated-prefix message may contain {@code %s} for the offending prefix.
     *
     * @param arguments Arguments to tokenize.
     * @param allowedPrefixes Prefixes that may appear.
     * @param invalidFormatMessage Message for a rejected preamble or an unknown prefix.
     * @param repeatedPrefixMessage Message template for a repeated prefix.
     * @return The tokenized arguments, with an empty preamble.
     * @throws ParseException If a preamble, unknown prefix, or repeated prefix is present.
     */
    public static TokenizedArguments tokenize(String arguments, List<String> allowedPrefixes,
            String invalidFormatMessage, String repeatedPrefixMessage) throws ParseException {
        return tokenize(arguments, allowedPrefixes, invalidFormatMessage, repeatedPrefixMessage,
                false, NO_VALUE_TOKENS);
    }

    /**
     * Returns the prefixed values in {@code arguments}, which must not contain any text before the first prefix.
     * A prefix-shaped token that {@code valueTokenFilter} accepts is kept as part of the preceding value.
     *
     * @param arguments Arguments to tokenize.
     * @param allowedPrefixes Prefixes that may appear, e.g. {@code "n/"}.
     * @param invalidFormatMessage Message for text before the first prefix or an unknown prefix.
     * @param valueTokenFilter Decides which prefix-shaped tokens belong to the preceding value.
     * @return The tokenized arguments, with an empty preamble.
     * @throws ParseException If there is text before the first prefix, or a prefix is unknown or repeated.
     */
    public static TokenizedArguments tokenize(String arguments, List<String> allowedPrefixes,
            String invalidFormatMessage, ValueTokenFilter valueTokenFilter) throws ParseException {
        return tokenize(arguments, allowedPrefixes, invalidFormatMessage, MESSAGE_REPEATED_PREFIX,
                false, valueTokenFilter);
    }

    /**
     * Returns the preamble and prefixed values in {@code arguments}, checking each prefix in the order it appears.
     * A non-blank preamble is rejected when {@code isPreambleAllowed} is false. Prefix-shaped words inside a quoted
     * value are skipped, and {@code valueTokenFilter} is not consulted for the prefix after a quoted value.
     *
     * @param arguments Arguments to tokenize.
     * @param allowedPrefixes Prefixes that may appear.
     * @param invalidFormatMessage Message for a rejected preamble or an unknown prefix.
     * @param repeatedPrefixMessage Message template for a repeated prefix.
     * @param isPreambleAllowed Whether text may appear before the first prefix.
     * @param valueTokenFilter Decides which prefix-shaped tokens belong to the preceding value.
     */
    private static TokenizedArguments tokenize(String arguments, List<String> allowedPrefixes,
            String invalidFormatMessage, String repeatedPrefixMessage, boolean isPreambleAllowed,
            ValueTokenFilter valueTokenFilter)
            throws ParseException {
        requireAllNonNull(arguments, allowedPrefixes, invalidFormatMessage, repeatedPrefixMessage, valueTokenFilter);
        Matcher matcher = PREFIX_PATTERN.matcher(arguments);
        boolean hasNextPrefix = matcher.find();
        String preamble = arguments.substring(0, hasNextPrefix ? matcher.start() : arguments.length()).strip();
        if (!isPreambleAllowed && !preamble.isEmpty()) {
            throw new ParseException(invalidFormatMessage);
        }

        Map<String, String> values = new LinkedHashMap<>();
        int previousValueStart = -1;
        String previousPrefix = null;
        boolean isPreviousValueQuoted = false;
        while (hasNextPrefix) {
            String prefix = matcher.group(1);
            if (!isPreviousValueQuoted
                    && valueTokenFilter.isPartOfValue(arguments, matcher.end(), previousPrefix, prefix)) {
                hasNextPrefix = matcher.find();
                continue;
            }
            if (previousPrefix != null) {
                values.put(previousPrefix, extractValue(arguments, previousValueStart, matcher.start(),
                        isPreviousValueQuoted));
            }

            if (!allowedPrefixes.contains(prefix)) {
                throw new ParseException(buildUnknownPrefixMessage(arguments, matcher, previousPrefix,
                        previousValueStart, allowedPrefixes, valueTokenFilter, invalidFormatMessage));
            }
            if (values.containsKey(prefix)) {
                throw new ParseException(String.format(repeatedPrefixMessage, prefix));
            }
            previousPrefix = prefix;
            previousValueStart = matcher.end();
            int quotedValueEnd = findQuotedValueEnd(arguments, previousValueStart, allowedPrefixes);
            isPreviousValueQuoted = quotedValueEnd != -1;
            hasNextPrefix = isPreviousValueQuoted ? matcher.find(quotedValueEnd) : matcher.find();
        }
        if (previousPrefix != null) {
            values.put(previousPrefix, extractValue(arguments, previousValueStart, arguments.length(),
                    isPreviousValueQuoted));
        }
        return new TokenizedArguments(preamble, values);
    }

    /**
     * Returns the error for the unknown prefix found by {@code matcher}: its name, a hint to quote the value it is in
     * (with an example built from that value unless the text before the prefix is only digits and date or time
     * punctuation), a hint to use straight quotes if {@code arguments} has curly quotes, and the usage text of
     * {@code invalidFormatMessage}. There is no quote hint if the prefix is not inside any value.
     *
     * @param previousPrefix Prefix of the value the unknown prefix is in, or null if there is none.
     * @param valueStart Start of the value of {@code previousPrefix} in {@code arguments}.
     */
    private static String buildUnknownPrefixMessage(String arguments, Matcher matcher, String previousPrefix,
            int valueStart, List<String> allowedPrefixes, ValueTokenFilter valueTokenFilter,
            String invalidFormatMessage) {
        String unknownPrefix = matcher.group(1);
        StringBuilder message = new StringBuilder(String.format(MESSAGE_NOT_PREFIX, unknownPrefix));
        if (previousPrefix != null) {
            String textBeforePrefix = arguments.substring(valueStart, matcher.start()).strip();
            if (NUMERIC_TEXT_PATTERN.matcher(textBeforePrefix).matches()) {
                message.append(MESSAGE_QUOTE_HINT);
            } else {
                int valueEnd = findFieldEnd(arguments, matcher.end(), previousPrefix, allowedPrefixes,
                        valueTokenFilter);
                String valueText = stripQuotes(arguments.substring(valueStart, valueEnd));
                message.append(String.format(MESSAGE_QUOTE_HINT_WITH_EXAMPLE, previousPrefix, valueText));
            }
        }
        if (arguments.chars().anyMatch(character -> CURLY_QUOTE_CHARACTERS.indexOf(character) != -1)) {
            message.append('\n').append(MESSAGE_CURLY_QUOTES);
        }
        int usageStart = invalidFormatMessage.indexOf(USAGE_START);
        message.append('\n').append(usageStart == -1 ? invalidFormatMessage
                : invalidFormatMessage.substring(usageStart));
        return message.toString();
    }

    /**
     * Returns the end of the value of {@code previousPrefix} for an example: the start of the next allowed prefix
     * from {@code searchStart} that starts a word or directly follows a quote, or the end of {@code arguments}.
     * Prefixes that {@code valueTokenFilter} keeps in the value are skipped.
     */
    private static int findFieldEnd(String arguments, int searchStart, String previousPrefix,
            List<String> allowedPrefixes, ValueTokenFilter valueTokenFilter) {
        Matcher fieldEndMatcher = FIELD_END_PREFIX_PATTERN.matcher(arguments);
        fieldEndMatcher.region(searchStart, arguments.length());
        fieldEndMatcher.useTransparentBounds(true);
        while (fieldEndMatcher.find()) {
            String prefix = fieldEndMatcher.group(1);
            if (allowedPrefixes.contains(prefix)
                    && !valueTokenFilter.isPartOfValue(arguments, fieldEndMatcher.end(), previousPrefix, prefix)) {
                return fieldEndMatcher.start();
            }
        }
        return arguments.length();
    }

    /**
     * Returns {@code text} stripped of whitespace and of straight or curly quotes at both ends, so that the
     * example does not double the quotes the user typed.
     */
    private static String stripQuotes(String text) {
        int start = 0;
        int end = text.length();
        while (start < end && (Character.isWhitespace(text.charAt(start))
                || QUOTE_CHARACTERS.indexOf(text.charAt(start)) != -1)) {
            start++;
        }
        while (end > start && (Character.isWhitespace(text.charAt(end - 1))
                || QUOTE_CHARACTERS.indexOf(text.charAt(end - 1)) != -1)) {
            end--;
        }
        return text.substring(start, end);
    }

    /**
     * Returns the stripped text of {@code arguments} from {@code start} to {@code end}. If {@code isQuoted}, the outer
     * quotes are removed and the text inside them is stripped too.
     */
    private static String extractValue(String arguments, int start, int end, boolean isQuoted) {
        String value = arguments.substring(start, end).strip();
        assert !isQuoted || (value.length() >= 2 && value.charAt(0) == QUOTE
                && value.charAt(value.length() - 1) == QUOTE) : "A quoted value must start and end with a quote";
        return isQuoted ? value.substring(1, value.length() - 1).strip() : value;
    }

    /**
     * Returns the index just after the closing quote of the quoted value starting at {@code valueStart}, or -1 if
     * the value is not quoted. The value is quoted if, after leading whitespace, it starts with {@code "} and a later
     * {@code "} is followed by the end of {@code arguments} (ignoring trailing whitespace), or by whitespace and one
     * of {@code allowedPrefixes}. The first such {@code "} closes the value.
     */
    private static int findQuotedValueEnd(String arguments, int valueStart, List<String> allowedPrefixes) {
        int openingQuote = valueStart;
        while (openingQuote < arguments.length() && Character.isWhitespace(arguments.charAt(openingQuote))) {
            openingQuote++;
        }
        if (openingQuote == arguments.length() || arguments.charAt(openingQuote) != QUOTE) {
            return -1;
        }

        // Only whitespace follows this index. Comparing with it, rather than checking the rest of the arguments at
        // every quote, keeps the scan linear when there are many quotes.
        int contentEnd = arguments.stripTrailing().length();
        Matcher prefixMatcher = WHITESPACE_THEN_PREFIX_PATTERN.matcher(arguments);
        int closingQuote = arguments.indexOf(QUOTE, openingQuote + 1);
        while (closingQuote != -1) {
            int afterQuote = closingQuote + 1;
            if (afterQuote >= contentEnd) {
                return afterQuote;
            }
            prefixMatcher.region(afterQuote, arguments.length());
            if (prefixMatcher.lookingAt() && allowedPrefixes.contains(prefixMatcher.group(1))) {
                return afterQuote;
            }
            closingQuote = arguments.indexOf(QUOTE, afterQuote);
        }
        return -1;
    }

    /**
     * Decides whether a prefix-shaped token belongs to the value of the prefix before it.
     */
    @FunctionalInterface
    public interface ValueTokenFilter {
        /**
         * Returns true if {@code prefix}, ending at {@code prefixEnd} in {@code arguments}, is part of the value of
         * {@code previousPrefix} (null before the first prefix).
         */
        boolean isPartOfValue(String arguments, int prefixEnd, String previousPrefix, String prefix);
    }

    /**
     * Holds the result of tokenizing: the stripped preamble and each prefix's stripped value in the order the
     * prefixes appear. A prefix that is present with no value maps to an empty string.
     */
    public record TokenizedArguments(String preamble, Map<String, String> values) {
        /**
         * Creates a result holding an unmodifiable copy of {@code values} that keeps its iteration order.
         */
        public TokenizedArguments {
            requireAllNonNull(preamble, values);
            values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
        }
    }
}
