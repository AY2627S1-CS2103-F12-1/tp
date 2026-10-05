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
 * Errors are reported in the order the offending text appears.
 */
public final class StrictArgumentTokenizer {
    public static final String MESSAGE_REPEATED_PREFIX = "Prefix %s must be specified exactly once.";

    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?<!\\S)([a-z]+/)");
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
        return tokenize(arguments, allowedPrefixes, invalidFormatMessage, true, NO_VALUE_TOKENS);
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
        return tokenize(arguments, allowedPrefixes, invalidFormatMessage, false, valueTokenFilter);
    }

    /**
     * Returns the preamble and prefixed values in {@code arguments}, checking each prefix in the order it appears.
     * A non-blank preamble is rejected when {@code isPreambleAllowed} is false.
     */
    private static TokenizedArguments tokenize(String arguments, List<String> allowedPrefixes,
            String invalidFormatMessage, boolean isPreambleAllowed, ValueTokenFilter valueTokenFilter)
            throws ParseException {
        requireAllNonNull(arguments, allowedPrefixes, invalidFormatMessage, valueTokenFilter);
        Matcher matcher = PREFIX_PATTERN.matcher(arguments);
        boolean hasNextPrefix = matcher.find();
        String preamble = arguments.substring(0, hasNextPrefix ? matcher.start() : arguments.length()).strip();
        if (!isPreambleAllowed && !preamble.isEmpty()) {
            throw new ParseException(invalidFormatMessage);
        }

        Map<String, String> values = new LinkedHashMap<>();
        int previousValueStart = -1;
        String previousPrefix = null;
        while (hasNextPrefix) {
            String prefix = matcher.group(1);
            if (valueTokenFilter.isPartOfValue(arguments, matcher.end(), previousPrefix, prefix)) {
                hasNextPrefix = matcher.find();
                continue;
            }
            if (previousPrefix != null) {
                values.put(previousPrefix, arguments.substring(previousValueStart, matcher.start()).strip());
            }

            if (!allowedPrefixes.contains(prefix)) {
                throw new ParseException(invalidFormatMessage);
            }
            if (values.containsKey(prefix) || prefix.equals(previousPrefix)) {
                throw new ParseException(String.format(MESSAGE_REPEATED_PREFIX, prefix));
            }
            previousPrefix = prefix;
            previousValueStart = matcher.end();
            hasNextPrefix = matcher.find();
        }
        if (previousPrefix != null) {
            values.put(previousPrefix, arguments.substring(previousValueStart).strip());
        }
        return new TokenizedArguments(preamble, values);
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
