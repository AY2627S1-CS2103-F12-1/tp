package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
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
 * A command may also reject an allowed prefix typed in another case (e.g. {@code Due/} for {@code due/}) instead of
 * keeping it in a value; other words that are not lowercase stay in the value. Such a command explains a repeated
 * prefix by its {@link RepeatedPrefixRules}, and gives the hint of those rules, instead of an example built from the
 * value, only for a prefix inside a free-text value.
 */
public final class StrictArgumentTokenizer {
    public static final String MESSAGE_REPEATED_PREFIX = "Prefix %s must be specified exactly once.";
    public static final String MESSAGE_PREFIX_GIVEN_ONCE = "Prefix %s can be given only once.";
    public static final String MESSAGE_NOT_PREFIX = "%s is not a prefix of this command.";
    public static final String MESSAGE_WRONG_CASE_PREFIX =
            "%s is not a prefix of this command; prefixes are lowercase (%s).";
    public static final String MESSAGE_QUOTE_HINT = " To use it inside a value, wrap the whole value in straight "
            + "double quotes.";
    public static final String MESSAGE_QUOTE_HINT_WITH_EXAMPLE = " To use it inside a value, wrap the whole value "
            + "in straight double quotes, e.g. %s\"%s\".";
    public static final String MESSAGE_CURLY_QUOTES = "Use straight quotes (\"), not curly quotes (\u201c \u201d).";

    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?<!\\S)([a-z]+/)");
    /** A prefix-shaped word in any case, to find allowed prefixes typed in another case. */
    private static final Pattern ANY_CASE_PREFIX_PATTERN = Pattern.compile("(?<!\\S)([A-Za-z]+/)");
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
                true, NO_VALUE_TOKENS, false, null);
    }

    /** Returns the preamble and fields, keeping prefix-shaped name tokens accepted by {@code valueTokenFilter}. */
    public static TokenizedArguments tokenizeWithPreamble(String arguments, List<String> allowedPrefixes,
            String invalidFormatMessage, ValueTokenFilter valueTokenFilter) throws ParseException {
        return tokenize(arguments, allowedPrefixes, invalidFormatMessage, MESSAGE_REPEATED_PREFIX,
                true, valueTokenFilter, false, null);
    }

    /**
     * Returns the preamble and the prefixed values in {@code arguments}, like
     * {@link #tokenizeWithPreamble(String, List, String)}, but rejects an allowed prefix typed in another case
     * (e.g. {@code Due/} for {@code due/}) instead of keeping it in the value before it. It may still appear inside
     * a quoted value. A repeated prefix, or more than one alias of a field, is explained as
     * {@code repeatedPrefixRules} says.
     *
     * @param arguments Arguments to tokenize.
     * @param allowedPrefixes Prefixes that may appear, in lowercase, e.g. {@code "due/"}.
     * @param invalidFormatMessage Message for an unknown or wrong-case prefix.
     * @param repeatedPrefixRules Which values are free text and which prefixes are aliases.
     * @return The tokenized arguments.
     * @throws ParseException If a prefix is unknown, repeated or in the wrong case, or aliases are given together.
     */
    public static TokenizedArguments tokenizeWithPreambleRejectingWrongCase(String arguments,
            List<String> allowedPrefixes, String invalidFormatMessage, RepeatedPrefixRules repeatedPrefixRules)
            throws ParseException {
        requireNonNull(repeatedPrefixRules);
        return tokenize(arguments, allowedPrefixes, invalidFormatMessage, MESSAGE_REPEATED_PREFIX,
                true, NO_VALUE_TOKENS, true, repeatedPrefixRules);
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
                false, NO_VALUE_TOKENS, false, null);
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
                false, valueTokenFilter, false, null);
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
     * @param isWrongCaseRejected Whether an allowed prefix typed in another case is rejected.
     * @param repeatedPrefixRules How to explain a repeated prefix, aliases given together and a prefix inside a
     *     free-text value, or null to report a repeated prefix with {@code repeatedPrefixMessage} alone, allow aliases
     *     together and build the hint for an unknown prefix from the value it is in.
     */
    private static TokenizedArguments tokenize(String arguments, List<String> allowedPrefixes,
            String invalidFormatMessage, String repeatedPrefixMessage, boolean isPreambleAllowed,
            ValueTokenFilter valueTokenFilter, boolean isWrongCaseRejected, RepeatedPrefixRules repeatedPrefixRules)
            throws ParseException {
        requireAllNonNull(arguments, allowedPrefixes, invalidFormatMessage, repeatedPrefixMessage, valueTokenFilter);
        Matcher matcher = (isWrongCaseRejected ? ANY_CASE_PREFIX_PATTERN : PREFIX_PATTERN).matcher(arguments);
        boolean hasNextPrefix = skipValueWords(matcher, matcher.find(), allowedPrefixes);
        String preamble = arguments.substring(0, hasNextPrefix ? matcher.start() : arguments.length()).strip();
        if (!isPreambleAllowed && !preamble.isEmpty()) {
            throw new ParseException(invalidFormatMessage);
        }

        Map<String, String> values = new LinkedHashMap<>();
        Set<String> prefixesInText = new HashSet<>();
        String aliasErrorMessage = null;
        int previousValueStart = -1;
        String previousPrefix = null;
        boolean isPreviousValueQuoted = false;
        while (hasNextPrefix) {
            String prefix = matcher.group(1);
            boolean isInText = repeatedPrefixRules != null && previousPrefix != null && !isPreviousValueQuoted
                    && repeatedPrefixRules.textPrefixes().contains(previousPrefix);
            String quoteHint = isInText ? repeatedPrefixRules.quoteHint().apply(prefix) : "";
            if (!isLowercase(prefix)) {
                // only commands with rules reject a wrong-case prefix
                String firstSentence = String.format(MESSAGE_WRONG_CASE_PREFIX, prefix,
                        prefix.toLowerCase(Locale.ROOT));
                throw new ParseException(buildErrorMessage(firstSentence, quoteHint, arguments,
                        invalidFormatMessage));
            }
            if (!isPreviousValueQuoted
                    && valueTokenFilter.isPartOfValue(arguments, matcher.end(), previousPrefix, prefix)) {
                hasNextPrefix = skipValueWords(matcher, matcher.find(), allowedPrefixes);
                continue;
            }
            if (previousPrefix != null) {
                values.put(previousPrefix, extractValue(arguments, previousValueStart, matcher.start(),
                        isPreviousValueQuoted));
            }

            if (!allowedPrefixes.contains(prefix)) {
                String firstSentence = String.format(MESSAGE_NOT_PREFIX, prefix);
                throw new ParseException(repeatedPrefixRules != null
                        ? buildErrorMessage(firstSentence, quoteHint, arguments, invalidFormatMessage)
                        : buildPrefixErrorMessage(firstSentence, arguments, matcher, previousPrefix,
                                previousValueStart, allowedPrefixes, valueTokenFilter, invalidFormatMessage));
            }
            if (repeatedPrefixRules == null && values.containsKey(prefix)) {
                throw new ParseException(String.format(repeatedPrefixMessage, prefix));
            }
            if (repeatedPrefixRules != null) {
                if (values.containsKey(prefix)) {
                    // either one may be in a free-text value, e.g. the first s/ in t/Read s/o notes s/MATH
                    String hint = prefixesInText.contains(prefix) ? repeatedPrefixRules.quoteHint().apply(prefix)
                            : quoteHint;
                    throw new ParseException(buildErrorMessage(String.format(MESSAGE_PREFIX_GIVEN_ONCE, prefix),
                            hint, arguments, invalidFormatMessage));
                }
                if (aliasErrorMessage == null && repeatedPrefixRules.isAliasOfAny(prefix, values.keySet())) {
                    // reported only if no prefix after it is unknown or repeated
                    aliasErrorMessage = buildErrorMessage(repeatedPrefixRules.aliasMessage(), quoteHint, arguments,
                            invalidFormatMessage);
                }
                if (isInText) {
                    prefixesInText.add(prefix);
                }
            }
            previousPrefix = prefix;
            previousValueStart = matcher.end();
            int quotedValueEnd = findQuotedValueEnd(arguments, previousValueStart, allowedPrefixes);
            isPreviousValueQuoted = quotedValueEnd != -1;
            hasNextPrefix = skipValueWords(matcher,
                    isPreviousValueQuoted ? matcher.find(quotedValueEnd) : matcher.find(), allowedPrefixes);
        }
        if (aliasErrorMessage != null) {
            throw new ParseException(aliasErrorMessage);
        }
        if (previousPrefix != null) {
            values.put(previousPrefix, extractValue(arguments, previousValueStart, arguments.length(),
                    isPreviousValueQuoted));
        }
        return new TokenizedArguments(preamble, values, prefixesInText);
    }

    /**
     * Returns true if {@code isFound}, the result of the last search of {@code matcher}, is true and the word found
     * is a lowercase prefix or an allowed prefix in another case, after moving {@code matcher} past any other word
     * that is not lowercase, which belongs to a value.
     */
    private static boolean skipValueWords(Matcher matcher, boolean isFound, List<String> allowedPrefixes) {
        boolean hasFoundPrefix = isFound;
        while (hasFoundPrefix && !isLowercase(matcher.group(1))
                && !allowedPrefixes.contains(matcher.group(1).toLowerCase(Locale.ROOT))) {
            hasFoundPrefix = matcher.find();
        }
        return hasFoundPrefix;
    }

    /**
     * Returns true if {@code word} has no uppercase letters.
     */
    private static boolean isLowercase(String word) {
        return word.equals(word.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns the error for the unknown or wrong-case prefix found by {@code matcher}: {@code firstSentence}, a hint
     * to quote the value it is in if there is one, a hint to use straight quotes if {@code arguments} has curly
     * quotes, and the usage text of {@code invalidFormatMessage}.
     *
     * @param previousPrefix Prefix of the value the prefix found is in, or null if there is none.
     * @param valueStart Start of the value of {@code previousPrefix} in {@code arguments}.
     */
    private static String buildPrefixErrorMessage(String firstSentence, String arguments, Matcher matcher,
            String previousPrefix, int valueStart, List<String> allowedPrefixes, ValueTokenFilter valueTokenFilter,
            String invalidFormatMessage) {
        String quoteHint = previousPrefix == null ? ""
                : buildQuoteHint(arguments, matcher.start(), matcher.end(), previousPrefix, valueStart,
                        allowedPrefixes, valueTokenFilter);
        return buildErrorMessage(firstSentence, quoteHint, arguments, invalidFormatMessage);
    }

    /**
     * Returns {@code firstSentence} followed by {@code quoteHint}, a hint to use straight quotes if {@code arguments}
     * has curly quotes, and the usage text of {@code invalidFormatMessage}.
     */
    private static String buildErrorMessage(String firstSentence, String quoteHint, String arguments,
            String invalidFormatMessage) {
        StringBuilder message = new StringBuilder(firstSentence).append(quoteHint);
        if (hasCurlyQuotes(arguments)) {
            message.append('\n').append(MESSAGE_CURLY_QUOTES);
        }
        return message.append('\n').append(getUsageText(invalidFormatMessage)).toString();
    }

    /**
     * Returns the hint to quote the value of {@code previousPrefix} that the prefix from {@code prefixStart} to
     * {@code prefixEnd} is in, with an example built from that value unless the text before the prefix is only
     * digits and date or time punctuation.
     */
    private static String buildQuoteHint(String arguments, int prefixStart, int prefixEnd, String previousPrefix,
            int valueStart, List<String> allowedPrefixes, ValueTokenFilter valueTokenFilter) {
        String textBeforePrefix = arguments.substring(valueStart, prefixStart).strip();
        if (NUMERIC_TEXT_PATTERN.matcher(textBeforePrefix).matches()) {
            return MESSAGE_QUOTE_HINT;
        }
        int valueEnd = findFieldEnd(arguments, prefixEnd, previousPrefix, allowedPrefixes, valueTokenFilter);
        String valueText = stripQuotes(arguments.substring(valueStart, valueEnd));
        return String.format(MESSAGE_QUOTE_HINT_WITH_EXAMPLE, previousPrefix, valueText);
    }

    /**
     * Returns true if {@code arguments} contains a curly quote.
     */
    private static boolean hasCurlyQuotes(String arguments) {
        return arguments.chars().anyMatch(character -> CURLY_QUOTE_CHARACTERS.indexOf(character) != -1);
    }

    /**
     * Returns the part of {@code invalidFormatMessage} from {@code Usage:} on, or the whole message if it has none.
     */
    private static String getUsageText(String invalidFormatMessage) {
        int usageStart = invalidFormatMessage.indexOf(USAGE_START);
        return usageStart == -1 ? invalidFormatMessage : invalidFormatMessage.substring(usageStart);
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
        while (start < end && isQuoteOrWhitespace(text.charAt(start))) {
            start++;
        }
        while (end > start && isQuoteOrWhitespace(text.charAt(end - 1))) {
            end--;
        }
        return text.substring(start, end);
    }

    /**
     * Returns true if {@code character} is whitespace or a straight or curly double quote.
     */
    private static boolean isQuoteOrWhitespace(char character) {
        return Character.isWhitespace(character) || QUOTE_CHARACTERS.indexOf(character) != -1;
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
     * Describes how a command explains a repeated prefix, and a prefix-shaped word that may be part of a free-text
     * value. Such a word directly follows an unquoted value of one of {@code textPrefixes}, and its error gets the
     * hint {@code quoteHint} gives for it; an error about any other word gets no hint.
     *
     * @param textPrefixes Prefixes whose values are free text, which may contain prefix-shaped words when quoted.
     * @param aliasPrefixes Prefixes that name the same field, at most one of which may be given.
     * @param aliasMessage First sentence of the error for giving more than one of {@code aliasPrefixes}.
     * @param quoteHint Returns the hint, starting with a space, to quote the free-text value that holds the given
     *     prefix-shaped word.
     */
    public record RepeatedPrefixRules(List<String> textPrefixes, List<String> aliasPrefixes, String aliasMessage,
            UnaryOperator<String> quoteHint) {
        /** Rules for a command whose values are not free text and whose prefixes have no aliases. */
        public static final RepeatedPrefixRules NO_TEXT_OR_ALIAS_PREFIXES =
                new RepeatedPrefixRules(List.of(), List.of(), "", prefix -> "");

        /**
         * Creates rules holding unmodifiable copies of {@code textPrefixes} and {@code aliasPrefixes}.
         */
        public RepeatedPrefixRules {
            requireAllNonNull(textPrefixes, aliasPrefixes, aliasMessage, quoteHint);
            textPrefixes = List.copyOf(textPrefixes);
            aliasPrefixes = List.copyOf(aliasPrefixes);
        }

        /**
         * Returns true if {@code prefix} is an alias of a field that one of {@code givenPrefixes} already names.
         */
        boolean isAliasOfAny(String prefix, Set<String> givenPrefixes) {
            return aliasPrefixes.contains(prefix) && givenPrefixes.stream().anyMatch(aliasPrefixes::contains);
        }
    }

    /**
     * Holds the result of tokenizing: the stripped preamble, each prefix's stripped value in the order the
     * prefixes appear, and the prefixes that directly follow an unquoted free-text value, so may be part of it.
     * A prefix that is present with no value maps to an empty string. Only a command with
     * {@link RepeatedPrefixRules} has free-text values.
     */
    public record TokenizedArguments(String preamble, Map<String, String> values, Set<String> prefixesInText) {
        /**
         * Creates a result holding an unmodifiable copy of {@code values} that keeps its iteration order, and an
         * unmodifiable copy of {@code prefixesInText}.
         */
        public TokenizedArguments {
            requireAllNonNull(preamble, values, prefixesInText);
            values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
            prefixesInText = Set.copyOf(prefixesInText);
        }
    }
}
