package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.StrictArgumentTokenizer.TokenizedArguments;
import seedu.address.logic.parser.StrictArgumentTokenizer.ValueTokenFilter;
import seedu.address.logic.parser.exceptions.ParseException;

public class StrictArgumentTokenizerTest {
    private static final List<String> PREFIXES = List.of("n/", "t/", "due/");
    private static final String MESSAGE_INVALID_FORMAT = "Invalid format.";
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
    public void tokenize_unknownPrefix_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_FORMAT, () ->
                StrictArgumentTokenizer.tokenize(" n/John x/value", PREFIXES, MESSAGE_INVALID_FORMAT));
        assertThrows(ParseException.class, MESSAGE_INVALID_FORMAT, () ->
                StrictArgumentTokenizer.tokenizeWithPreamble("1 s/MATH", PREFIXES, MESSAGE_INVALID_FORMAT));
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
        assertThrows(ParseException.class, MESSAGE_INVALID_FORMAT, () -> StrictArgumentTokenizer.tokenize(
                " n/A x/b y/c", PREFIXES, MESSAGE_INVALID_FORMAT, ACCEPT_X_PREFIX));
        // repeated prefix
        String repeatedMessage = String.format(StrictArgumentTokenizer.MESSAGE_REPEATED_PREFIX, "n/");
        assertThrows(ParseException.class, repeatedMessage, () -> StrictArgumentTokenizer.tokenize(
                " n/A x/b n/c", PREFIXES, MESSAGE_INVALID_FORMAT, ACCEPT_X_PREFIX));
        // text before the first prefix
        assertThrows(ParseException.class, MESSAGE_INVALID_FORMAT, () -> StrictArgumentTokenizer.tokenize(
                "1 n/A", PREFIXES, MESSAGE_INVALID_FORMAT, ACCEPT_X_PREFIX));
    }

    @Test
    public void tokenizedArguments_valuesUnmodifiable() throws ParseException {
        TokenizedArguments result = StrictArgumentTokenizer.tokenize(" n/John", PREFIXES, MESSAGE_INVALID_FORMAT);

        assertThrows(UnsupportedOperationException.class, () -> result.values().put("t/", "x"));
    }
}
