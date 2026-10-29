package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static seedu.address.logic.parser.CommandParserTestUtil.MESSAGE_CURLY_QUOTES;
import static seedu.address.logic.parser.CommandParserTestUtil.givenOnce;
import static seedu.address.logic.parser.CommandParserTestUtil.notPrefix;
import static seedu.address.logic.parser.CommandParserTestUtil.notPrefixWithQuoteHint;
import static seedu.address.logic.parser.CommandParserTestUtil.wrongCasePrefix;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.StrictArgumentTokenizer.RepeatedPrefixRules;
import seedu.address.logic.parser.StrictArgumentTokenizer.TokenizedArguments;
import seedu.address.logic.parser.StrictArgumentTokenizer.ValueTokenFilter;
import seedu.address.logic.parser.exceptions.ParseException;

public class StrictArgumentTokenizerTest {
    private static final List<String> PREFIXES = List.of("n/", "t/", "due/");
    private static final String USAGE_LINE = "\nUsage: cmd n/N t/T due/D";
    private static final String MESSAGE_INVALID_FORMAT = "Invalid format." + USAGE_LINE;
    private static final String MESSAGE_N_AND_T = "Use either n/ or t/, not both.";
    /**
     * Rules where n/ and t/ take free text and name the same field, and a due/ value starting with 2026 is a valid
     * value of its own.
     */
    private static final RepeatedPrefixRules TEXT_RULES = new RepeatedPrefixRules(List.of("n/", "t/"),
            List.of("n/", "t/"), MESSAGE_N_AND_T, StrictArgumentTokenizerTest::textHint, (prefix, text) ->
                    "due/".equals(prefix) && text.startsWith("2026"));
    private static final ValueTokenFilter ACCEPT_X_PREFIX = (arguments, prefixEnd, previousPrefix, prefix) ->
            "x/".equals(prefix);

    @Test
    public void tokenizeWithPreamble_preambleAndValues_returnsBothInOrder() throws ParseException {
        TokenizedArguments result = StrictArgumentTokenizer.tokenizeWithPreamble(
                "  12  due/ 2026-10-15  t/Complete  worksheet n/", PREFIXES, MESSAGE_INVALID_FORMAT);

        assertEquals("12", result.preamble());
        assertEquals(Map.of("due/", "2026-10-15", "t/", "Complete  worksheet", "n/", ""), result.values());
        assertEquals(List.of("due/", "t/", "n/"), List.copyOf(result.values().keySet()));
    }

    @Test
    public void tokenizeWithPreamble_noPrefixes_returnsWholeInputAsPreamble() throws ParseException {
        TokenizedArguments result = StrictArgumentTokenizer.tokenizeWithPreamble(" 1 2 ", PREFIXES,
                MESSAGE_INVALID_FORMAT);

        assertEquals("1 2", result.preamble());
        assertEquals(Map.of(), result.values());
    }

    @Test
    public void tokenize_blankArguments_returnsNothing() throws ParseException {
        TokenizedArguments result = StrictArgumentTokenizer.tokenize("   ", PREFIXES, MESSAGE_INVALID_FORMAT);

        assertEquals("", result.preamble());
        assertEquals(Map.of(), result.values());
    }

    @Test
    public void tokenize_slashInsideValue_staysOneValue() throws ParseException {
        // lowercase word with slash not preceded by whitespace
        assertEquals(Map.of("n/", "s/o rahman"),
                StrictArgumentTokenizer.tokenize("n/s/o rahman", PREFIXES, MESSAGE_INVALID_FORMAT).values());
        // uppercase word with slash after whitespace
        assertEquals(Map.of("n/", "John S/O Tan"),
                StrictArgumentTokenizer.tokenize(" n/John S/O Tan", PREFIXES, MESSAGE_INVALID_FORMAT).values());
    }

    @Test
    public void tokenize_unknownPrefixInValue_namesWordAndQuotesWholeValue() {
        // the example quotes the field's text up to the next allowed prefix
        assertThrows(ParseException.class, notPrefixWithQuoteHint("km/", "t/\"Speed in km/h\"") + USAGE_LINE, () ->
                tokenizeValues(" n/A t/Speed in km/h due/3"));
    }

    @Test
    public void tokenize_unknownPrefixInLastValue_quotesTextToEnd() {
        assertThrows(ParseException.class, notPrefixWithQuoteHint("km/", "t/\"Speed in km/h\"") + USAGE_LINE, () ->
                tokenizeValues(" n/A t/Speed in km/h"));
        assertThrows(ParseException.class, notPrefixWithQuoteHint("x/", "n/\"John x/value\"") + USAGE_LINE, () ->
                tokenizeValues(" n/John x/value"));
    }

    @Test
    public void tokenize_twoUnknownWordsInValue_namesFirstAndQuotesWholeValue() {
        assertThrows(ParseException.class, notPrefixWithQuoteHint("km/", "t/\"A km/h and/or B\"") + USAGE_LINE, () ->
                tokenizeValues(" t/A km/h and/or B n/C"));
    }

    @Test
    public void tokenize_unknownPrefixAfterDigitsOnlyValue_omitsExample() {
        // the value's text before the word is only digits and date punctuation
        assertThrows(ParseException.class, notPrefixWithQuoteHint("x/") + USAGE_LINE, () ->
                tokenizeValues(" n/A due/2026-11-01 x/y"));
    }

    @Test
    public void tokenize_unknownPrefixFirst_omitsExample() {
        assertThrows(ParseException.class, notPrefix("x/") + USAGE_LINE, () -> tokenizeValues(" x/y n/A"));
    }

    @Test
    public void tokenizeWithPreamble_unknownPrefixAfterPreamble_omitsExample() {
        assertThrows(ParseException.class, notPrefix("x/") + USAGE_LINE, () ->
                StrictArgumentTokenizer.tokenizeWithPreamble("1 x/y t/A", PREFIXES, MESSAGE_INVALID_FORMAT));
        assertThrows(ParseException.class, notPrefix("s/") + USAGE_LINE, () ->
                StrictArgumentTokenizer.tokenizeWithPreamble("1 s/MATH", PREFIXES, MESSAGE_INVALID_FORMAT));
    }

    @Test
    public void tokenize_curlyQuotes_addsStraightQuoteHint() {
        // curly quotes are not value quotes, so km/ is an unknown prefix; they are stripped from the example
        assertThrows(ParseException.class, notPrefixWithQuoteHint("km/", "t/\"Speed in km/h\"") + "\n"
                + MESSAGE_CURLY_QUOTES + USAGE_LINE, () -> tokenizeValues(" t/“Speed in km/h” n/A"));
    }

    @Test
    public void tokenize_noSpaceAfterClosingQuote_namesWord() {
        // the quote is not followed by whitespace, so it does not close the value
        assertThrows(ParseException.class, notPrefixWithQuoteHint("km/", "t/\"Speed in km/h\"") + USAGE_LINE, () ->
                tokenizeValues(" t/\"Speed in km/h\"n/A"));
    }

    @Test
    public void tokenize_repeatedPrefix_throwsParseException() {
        String expectedMessage = String.format(StrictArgumentTokenizer.MESSAGE_REPEATED_PREFIX, "t/");

        // adjacent repetition
        assertThrows(ParseException.class, expectedMessage, () ->
                StrictArgumentTokenizer.tokenize(" t/a t/b", PREFIXES, MESSAGE_INVALID_FORMAT));
        // repetition separated by another prefix
        assertThrows(ParseException.class, expectedMessage, () ->
                StrictArgumentTokenizer.tokenizeWithPreamble("1 t/a n/b t/c", PREFIXES, MESSAGE_INVALID_FORMAT));
    }

    @Test
    public void tokenize_customRepeatedMessage_usesSuppliedMessage() {
        String repeatedMessage = "Each filter may only be specified once";
        for (String arguments : List.of(" t/a t/b", " t/a n/b t/c", " t/\"a\" t/b")) {
            assertThrows(ParseException.class, repeatedMessage, () ->
                    StrictArgumentTokenizer.tokenize(arguments, PREFIXES, MESSAGE_INVALID_FORMAT, repeatedMessage));
        }
    }

    @Test
    public void tokenize_customRepeatedMessage_preservesOtherRules() throws ParseException {
        String repeatedMessage = "Repeated filter";
        assertEquals(Map.of("t/", "a x/b", "n/", "c"), StrictArgumentTokenizer.tokenize(
                " t/\"a x/b\" n/c", PREFIXES, MESSAGE_INVALID_FORMAT, repeatedMessage).values());
        assertThrows(ParseException.class, notPrefixWithQuoteHint("x/", "t/\"a x/b\"") + USAGE_LINE, () ->
                StrictArgumentTokenizer.tokenize(" t/a x/b", PREFIXES, MESSAGE_INVALID_FORMAT, repeatedMessage));
        assertThrows(ParseException.class, MESSAGE_INVALID_FORMAT, () -> StrictArgumentTokenizer.tokenize(
                " text t/a", PREFIXES, MESSAGE_INVALID_FORMAT, repeatedMessage));
    }

    @Test
    public void tokenize_textBeforeFirstPrefix_throwsParseException() {
        // text before a prefix
        assertThrows(ParseException.class, MESSAGE_INVALID_FORMAT, () ->
                StrictArgumentTokenizer.tokenize("1 n/John", PREFIXES, MESSAGE_INVALID_FORMAT));
        // text without any prefix
        assertThrows(ParseException.class, MESSAGE_INVALID_FORMAT, () ->
                StrictArgumentTokenizer.tokenize("unexpected text", PREFIXES, MESSAGE_INVALID_FORMAT));
        // text before a later repeated prefix is reported first
        assertThrows(ParseException.class, MESSAGE_INVALID_FORMAT, () ->
                StrictArgumentTokenizer.tokenize("1 t/a t/b", PREFIXES, MESSAGE_INVALID_FORMAT));
    }

    @Test
    public void tokenize_filterAcceptsToken_keepsTokenInValue() throws ParseException {
        // unknown prefix accepted by the filter
        assertEquals(Map.of("n/", "A x/b", "t/", "c"), StrictArgumentTokenizer.tokenize(" n/A x/b t/c", PREFIXES,
                MESSAGE_INVALID_FORMAT, ACCEPT_X_PREFIX).values());
        // allowed prefixes accepted by the filter, including a repeat
        ValueTokenFilter acceptAfterName = (arguments, prefixEnd, previousPrefix, prefix) ->
                "n/".equals(previousPrefix);
        assertEquals(Map.of("n/", "A t/b n/c"), StrictArgumentTokenizer.tokenize(" n/A t/b n/c", PREFIXES,
                MESSAGE_INVALID_FORMAT, acceptAfterName).values());
    }

    @Test
    public void tokenize_filter_consultedWithPreviousPrefixOfValue() throws ParseException {
        List<List<Object>> calls = new ArrayList<>();
        ValueTokenFilter recordingFilter = (arguments, prefixEnd, previousPrefix, prefix) -> {
            calls.add(Arrays.asList(prefixEnd, previousPrefix, prefix));
            return ACCEPT_X_PREFIX.isPartOfValue(arguments, prefixEnd, previousPrefix, prefix);
        };

        StrictArgumentTokenizer.tokenize("n/A x/b t/c", PREFIXES, MESSAGE_INVALID_FORMAT, recordingFilter);

        // null before the first prefix; an accepted token does not become the previous prefix
        assertEquals(List.of(Arrays.asList(2, null, "n/"), Arrays.asList(6, "n/", "x/"),
                Arrays.asList(10, "n/", "t/")), calls);
    }

    @Test
    public void tokenize_noFilter_splitsAtEveryPrefix() throws ParseException {
        ValueTokenFilter rejectAll = (arguments, prefixEnd, previousPrefix, prefix) -> false;
        String arguments = " n/A t/b due/c";

        TokenizedArguments result = StrictArgumentTokenizer.tokenize(arguments, PREFIXES, MESSAGE_INVALID_FORMAT);

        assertEquals(Map.of("n/", "A", "t/", "b", "due/", "c"), result.values());
        assertEquals(result,
                StrictArgumentTokenizer.tokenize(arguments, PREFIXES, MESSAGE_INVALID_FORMAT, rejectAll));
    }

    @Test
    public void tokenize_filterRejectsToken_throwsParseException() {
        // unknown prefix
        assertThrows(ParseException.class, notPrefixWithQuoteHint("y/", "n/\"A x/b y/c\"") + USAGE_LINE, () ->
                StrictArgumentTokenizer.tokenize(" n/A x/b y/c", PREFIXES, MESSAGE_INVALID_FORMAT, ACCEPT_X_PREFIX));
        // repeated prefix
        String repeatedMessage = String.format(StrictArgumentTokenizer.MESSAGE_REPEATED_PREFIX, "n/");
        assertThrows(ParseException.class, repeatedMessage, () -> StrictArgumentTokenizer.tokenize(
                " n/A x/b n/c", PREFIXES, MESSAGE_INVALID_FORMAT, ACCEPT_X_PREFIX));
        // text before the first prefix
        assertThrows(ParseException.class, MESSAGE_INVALID_FORMAT, () -> StrictArgumentTokenizer.tokenize(
                "1 n/A", PREFIXES, MESSAGE_INVALID_FORMAT, ACCEPT_X_PREFIX));
    }

    @Test
    public void tokenize_quotedValueWithPrefixShapedWord_removesQuotes() throws ParseException {
        // unknown prefix-shaped word inside, value ends before an allowed prefix
        assertEquals(Map.of("t/", "Speed in km/h", "n/", "A"),
                tokenizeValues(" t/\"Speed in km/h\" n/A"));
        // allowed prefix inside, value is the last one
        assertEquals(Map.of("n/", "A", "t/", "Read t/2 and due/3"),
                tokenizeValues(" n/A t/\"Read t/2 and due/3\""));
    }

    @Test
    public void tokenize_quotesInsideValue_keptAsTyped() throws ParseException {
        // value not wrapped in quotes
        assertEquals(Map.of("t/", "Read \"Hamlet\" act 1", "n/", "A"),
                tokenizeValues(" t/Read \"Hamlet\" act 1 n/A"));
        // inner quotes not followed by an allowed prefix or the end do not close the value
        assertEquals(Map.of("t/", "Read \"Hamlet\" km/h", "n/", "A"),
                tokenizeValues(" t/\"Read \"Hamlet\" km/h\" n/A"));
        // quote followed by an unknown prefix-shaped word does not close the value
        assertEquals(Map.of("t/", "a\" km/h"), tokenizeValues(" t/\"a\" km/h\""));
    }

    @Test
    public void tokenize_quoteFollowedByAllowedPrefix_closesValue() throws ParseException {
        // the first quote followed by an allowed prefix closes the value, so the rest is the next value
        assertEquals(Map.of("t/", "a", "n/", "b\""), tokenizeValues(" t/\"a\" n/b\""));

        // a closed value followed by its own prefix is a repeat
        assertThrows(ParseException.class, String.format(StrictArgumentTokenizer.MESSAGE_REPEATED_PREFIX, "t/"), () ->
                tokenizeValues(" t/\"a\" t/b\""));
    }

    @Test
    public void tokenize_unclosedQuote_readAsUnquoted() throws ParseException {
        // prefix-shaped word inside is read as an unknown prefix; the opening quote is not doubled in the example
        assertThrows(ParseException.class, notPrefixWithQuoteHint("km/", "t/\"Speed in km/h\"") + USAGE_LINE, () ->
                tokenizeValues(" t/\"Speed in km/h n/A"));
        // opening quote kept as typed
        assertEquals(Map.of("t/", "\"Speed", "n/", "A"), tokenizeValues(" t/\"Speed n/A"));
        // a lone quote
        assertEquals(Map.of("t/", "\""), tokenizeValues(" t/\""));
    }

    @Test
    public void tokenize_quoteFollowedByText_keptAsTyped() throws ParseException {
        assertEquals(Map.of("t/", "\"Speed\" extra", "n/", "A"), tokenizeValues(" t/\"Speed\" extra n/A"));
    }

    @Test
    public void tokenize_emptyQuotes_returnsEmptyValue() throws ParseException {
        // last value
        assertEquals(Map.of("t/", ""), tokenizeValues(" t/\"\""));
        // before another prefix
        assertEquals(Map.of("t/", "", "n/", "A"), tokenizeValues(" t/\"\" n/A"));
        // only whitespace inside
        assertEquals(Map.of("t/", "", "n/", "A"), tokenizeValues(" t/\"   \" n/A"));
    }

    @Test
    public void tokenize_whitespaceAroundQuotes_stripsBothSides() throws ParseException {
        // whitespace outside and at both ends inside the quotes is removed, whitespace between words is kept
        assertEquals(Map.of("t/", "a  km/h", "n/", "A"), tokenizeValues(" t/   \"  a  km/h \t\"   n/A"));
        // trailing whitespace after the last quoted value
        assertEquals(Map.of("t/", "km/h"), tokenizeValues(" t/ \"km/h\"  \t"));
        // tab between the closing quote and the next prefix
        assertEquals(Map.of("t/", "km/h", "n/", "A"), tokenizeValues(" t/\"km/h\"\tn/A"));
    }

    @Test
    public void tokenizeWithPreamble_quotedValue_removesQuotes() throws ParseException {
        TokenizedArguments result = StrictArgumentTokenizer.tokenizeWithPreamble("1 \"2\" t/\"km/h\" due/3",
                PREFIXES, MESSAGE_INVALID_FORMAT);

        // quotes in the preamble are not special
        assertEquals("1 \"2\"", result.preamble());
        assertEquals(Map.of("t/", "km/h", "due/", "3"), result.values());
    }

    @Test
    public void tokenize_quotedValueWithFilter_filterSkipsQuotedText() throws ParseException {
        List<String> filteredPrefixes = new ArrayList<>();
        ValueTokenFilter recordingFilter = (arguments, prefixEnd, previousPrefix, prefix) -> {
            filteredPrefixes.add(prefix);
            return ACCEPT_X_PREFIX.isPartOfValue(arguments, prefixEnd, previousPrefix, prefix);
        };

        // x/ inside the quotes is never passed to the filter, nor is the prefix after the closing quote
        assertEquals(Map.of("n/", "A x/b", "t/", "c x/d"), StrictArgumentTokenizer.tokenize(" n/\"A x/b\" t/c x/d",
                PREFIXES, MESSAGE_INVALID_FORMAT, recordingFilter).values());
        assertEquals(List.of("n/", "x/"), filteredPrefixes);
    }

    @Test
    public void tokenize_tokenAfterQuotedValue_notPartOfValue() throws ParseException {
        // the filter would accept t/ after n/, but the quotes already ended the value
        ValueTokenFilter acceptAfterName = (arguments, prefixEnd, previousPrefix, prefix) ->
                "n/".equals(previousPrefix);
        assertEquals(Map.of("n/", "A", "t/", "b"), StrictArgumentTokenizer.tokenize(" n/\"A\" t/b", PREFIXES,
                MESSAGE_INVALID_FORMAT, acceptAfterName).values());
    }

    @Test
    public void tokenize_manyQuotes_returnsQuickly() {
        // Issue #154: every quote is a candidate closing quote, so checking each must not copy the rest of the input.
        // The issue used 200,000 quotes; 1,000,000 makes a quadratic scan exceed the limit on fast machines too.
        String quotes = "\"".repeat(1_000_000);

        // the last quote is followed by the end (and trailing whitespace), so it closes the value
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> assertEquals(Map.of("due/", quotes.substring(2)),
                tokenizeValues("due/" + quotes + " \t ")));
        // no quote is followed by the end or an allowed prefix, so the value is read as typed
        String unclosedQuotes = "\" x".repeat(500_000);
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> assertEquals(Map.of("due/", unclosedQuotes),
                tokenizeValues("due/" + unclosedQuotes)));
    }

    @Test
    public void tokenizeWithPreamble_wrongCasePrefix_keptInValue() throws ParseException {
        // only the variant that rejects wrong-case prefixes treats Due/ as a mistyped due/
        assertEquals(Map.of("t/", "X Due/2026-11-11"), StrictArgumentTokenizer.tokenizeWithPreamble(
                "1 t/X Due/2026-11-11", PREFIXES, MESSAGE_INVALID_FORMAT).values());
    }

    @Test
    public void tokenizeWithPreambleRejectingWrongCase_wrongCasePrefixInValue_namesLowercasePrefix() {
        assertThrows(ParseException.class, wrongCasePrefix("Due/", "due/") + textHint("Due/") + USAGE_LINE, () ->
                tokenizeRejectingWrongCase("1 t/X Due/2026-11-11"));
        // all capitals and mixed case
        assertThrows(ParseException.class, wrongCasePrefix("DUE/", "due/") + textHint("DUE/") + USAGE_LINE, () ->
                tokenizeRejectingWrongCase("1 t/X DUE/11-11"));
        assertThrows(ParseException.class, wrongCasePrefix("dUE/", "due/") + textHint("dUE/") + USAGE_LINE, () ->
                tokenizeRejectingWrongCase("1 t/X dUE/11-11"));
    }

    @Test
    public void tokenizeWithPreambleRejectingWrongCase_wrongCasePrefixInOtherValue_noHint() {
        // due/ does not take free text
        assertThrows(ParseException.class, wrongCasePrefix("T/", "t/") + USAGE_LINE, () ->
                tokenizeRejectingWrongCase("1 due/3 T/A"));
    }

    @Test
    public void tokenizeWithPreambleRejectingWrongCase_wrongCasePrefixOutsideValue_namesLowercasePrefix() {
        // right after the preamble
        assertThrows(ParseException.class, wrongCasePrefix("T/", "t/") + USAGE_LINE, () ->
                tokenizeRejectingWrongCase("1 T/A due/3"));
        // at the start of the arguments
        assertThrows(ParseException.class, wrongCasePrefix("Due/", "due/") + USAGE_LINE, () ->
                tokenizeRejectingWrongCase("Due/3"));
    }

    @Test
    public void tokenizeWithPreambleRejectingWrongCase_earlierUnknownPrefix_reportedFirst() {
        assertThrows(ParseException.class, notPrefix("x/") + textHint("x/") + USAGE_LINE, () ->
                tokenizeRejectingWrongCase("1 t/A x/B Due/3"));
    }

    @Test
    public void tokenizeWithPreambleRejectingWrongCase_unknownPrefixOutsideTextValue_noHint() {
        // in the value of due/, which is not free text
        assertThrows(ParseException.class, notPrefix("x/") + USAGE_LINE, () ->
                tokenizeRejectingWrongCase("1 due/3 x/y"));
        // a command with no free-text values
        assertThrows(ParseException.class, notPrefix("x/") + USAGE_LINE, () ->
                StrictArgumentTokenizer.tokenizeWithPreambleRejectingWrongCase("1 t/A x/y", PREFIXES,
                        MESSAGE_INVALID_FORMAT, RepeatedPrefixRules.NO_TEXT_OR_ALIAS_PREFIXES));
    }

    @Test
    public void tokenizeWithPreambleRejectingWrongCase_prefixAfterTextValue_returnedAsInText() throws ParseException {
        assertEquals(Set.of("due/"), tokenizeRejectingWrongCase("1 t/Read due/3").prefixesInText());
        // after a quoted value, so not in it
        assertEquals(Set.of(), tokenizeRejectingWrongCase("1 t/\"Read\" due/3").prefixesInText());
        // starts with a valid value of its own, so not in it
        assertEquals(Set.of(), tokenizeRejectingWrongCase("1 t/Read due/2026").prefixesInText());
        // commands without rules have no free-text values
        assertEquals(Set.of(), StrictArgumentTokenizer.tokenizeWithPreamble("1 t/Read due/3", PREFIXES,
                MESSAGE_INVALID_FORMAT).prefixesInText());
    }

    @Test
    public void tokenizeWithPreambleRejectingWrongCase_quotedOrOtherWords_keptInValue() throws ParseException {
        // inside a quoted value
        assertEquals(Map.of("t/", "X Due/2026-11-11", "due/", "3"),
                tokenizeRejectingWrongCase("1 t/\"X Due/2026-11-11\" due/3").values());
        assertEquals(Map.of("t/", "X Due/2026-11-11"), tokenizeRejectingWrongCase("1 t/\"X Due/2026-11-11\"")
                .values());
        // a word that is not an allowed prefix in any case
        assertEquals(Map.of("n/", "John S/O Tan"), tokenizeRejectingWrongCase("1 n/John S/O Tan").values());
        // not after whitespace
        assertEquals(Map.of("t/", "XDue/3"), tokenizeRejectingWrongCase("1 t/XDue/3").values());
    }

    @Test
    public void tokenizeWithPreambleRejectingWrongCase_otherWordInPreamble_keptInPreamble() throws ParseException {
        TokenizedArguments result = tokenizeRejectingWrongCase("1 A/B t/X");

        assertEquals("1 A/B", result.preamble());
        assertEquals(Map.of("t/", "X"), result.values());
    }

    @Test
    public void tokenizeWithPreambleRejectingWrongCase_repeatedPrefixInTextValue_hintsToQuoteIt() {
        // the second t/ is in the value of the first
        assertThrows(ParseException.class, givenOnce("t/") + textHint("t/") + USAGE_LINE, () ->
                tokenizeRejectingWrongCase("1 t/Revise t/f questions due/3"));
        // the first due/ is in the value of t/, and the second follows it
        assertThrows(ParseException.class, givenOnce("due/") + textHint("due/") + USAGE_LINE, () ->
                tokenizeRejectingWrongCase("1 t/Check due/date due/3"));
        // the first due/ is in the value of t/, though the second does not follow it
        assertThrows(ParseException.class, givenOnce("due/") + textHint("due/") + USAGE_LINE, () ->
                tokenizeRejectingWrongCase("1 t/A due/3 n/\"B\" due/4"));
        // with curly quotes, which do not quote the value
        assertThrows(ParseException.class, givenOnce("t/") + textHint("t/") + "\n" + MESSAGE_CURLY_QUOTES
                + USAGE_LINE, () -> tokenizeRejectingWrongCase("1 t/\u201cA t/B\u201d"));
    }

    @Test
    public void tokenizeWithPreambleRejectingWrongCase_repeatedPrefixOutsideTextValue_noHint() {
        // in the value of due/, which is not free text
        assertThrows(ParseException.class, givenOnce("due/") + USAGE_LINE, () ->
                tokenizeRejectingWrongCase("1 due/3 due/4"));
        // after a quoted value, so not in it
        assertThrows(ParseException.class, givenOnce("t/") + USAGE_LINE, () ->
                tokenizeRejectingWrongCase("1 t/\"A\" t/B"));
        // the first due/ follows a text value, but starts with a valid value of its own
        assertThrows(ParseException.class, givenOnce("due/") + USAGE_LINE, () ->
                tokenizeRejectingWrongCase("1 t/A due/2026 due/4"));
        // the second due/ follows a text value, but starts with a valid value of its own
        assertThrows(ParseException.class, givenOnce("due/") + USAGE_LINE, () ->
                tokenizeRejectingWrongCase("1 due/4 t/A due/2026"));
    }

    @Test
    public void tokenizeWithPreambleRejectingWrongCase_aliasesGiven_hintsToQuoteValue() {
        assertThrows(ParseException.class, MESSAGE_N_AND_T + textHint("t/") + USAGE_LINE, () ->
                tokenizeRejectingWrongCase("1 n/Read t/abstract due/3"));
        assertThrows(ParseException.class, MESSAGE_N_AND_T + USAGE_LINE, () ->
                tokenizeRejectingWrongCase("1 n/Read due/3 t/abstract"));
    }

    @Test
    public void tokenizeWithPreambleRejectingWrongCase_aliasesAndLaterError_reportsLaterError() {
        assertThrows(ParseException.class, notPrefix("x/") + textHint("x/") + USAGE_LINE, () ->
                tokenizeRejectingWrongCase("1 n/A t/B x/C"));
        assertThrows(ParseException.class, givenOnce("due/") + USAGE_LINE, () ->
                tokenizeRejectingWrongCase("1 n/A t/\"B\" due/3 due/4"));
    }

    @Test
    public void tokenizeWithPreamble_repeatedPrefix_reportsExactlyOnce() {
        // commands that do not opt in keep the plain message
        assertThrows(ParseException.class, String.format(StrictArgumentTokenizer.MESSAGE_REPEATED_PREFIX, "t/"), () ->
                StrictArgumentTokenizer.tokenizeWithPreamble("1 t/Revise t/f", PREFIXES, MESSAGE_INVALID_FORMAT));
    }

    @Test
    public void tokenizedArguments_valuesUnmodifiable() throws ParseException {
        TokenizedArguments result = StrictArgumentTokenizer.tokenize(" n/John", PREFIXES, MESSAGE_INVALID_FORMAT);

        assertThrows(UnsupportedOperationException.class, () -> result.values().put("t/", "x"));
    }

    private static Map<String, String> tokenizeValues(String arguments) throws ParseException {
        return StrictArgumentTokenizer.tokenize(arguments, PREFIXES, MESSAGE_INVALID_FORMAT).values();
    }

    /** Returns the hint of {@code TEXT_RULES} for {@code word} inside a free-text value. */
    private static String textHint(String word) {
        return " Quote the text that holds " + word + ".";
    }

    private static TokenizedArguments tokenizeRejectingWrongCase(String arguments) throws ParseException {
        return StrictArgumentTokenizer.tokenizeWithPreambleRejectingWrongCase(arguments, PREFIXES,
                MESSAGE_INVALID_FORMAT, TEXT_RULES);
    }
}
