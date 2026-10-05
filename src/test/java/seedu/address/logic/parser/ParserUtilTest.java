package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.parser.exceptions.ParseException;

public class ParserUtilTest {
    private static final String MESSAGE_INVALID_STRICT_INDEX = "Index must be a positive whole number.";

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
            -> ParserUtil.parseIndex(Long.toString(Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("  1  "));
    }

    @Test
    public void parseStrictIndex_validInput_success() throws Exception {
        assertEquals(Index.fromOneBased(1), ParserUtil.parseStrictIndex("1", MESSAGE_INVALID_STRICT_INDEX));
        assertEquals(Index.fromOneBased(12), ParserUtil.parseStrictIndex("12", MESSAGE_INVALID_STRICT_INDEX));
        assertEquals(Index.fromOneBased(1), ParserUtil.parseStrictIndex(" 1 ", MESSAGE_INVALID_STRICT_INDEX));
        assertEquals(Index.fromOneBased(Integer.MAX_VALUE),
                ParserUtil.parseStrictIndex("2147483647", MESSAGE_INVALID_STRICT_INDEX));
    }

    @Test
    public void parseStrictIndex_invalidInput_throwsParseException() {
        assertInvalidStrictIndex("0"); // zero
        assertInvalidStrictIndex("01"); // leading zero
        assertInvalidStrictIndex("-1"); // negative
        assertInvalidStrictIndex("+1"); // explicit sign
        assertInvalidStrictIndex("1.0"); // decimal
        assertInvalidStrictIndex("one"); // word
        assertInvalidStrictIndex("1 2"); // two numbers
        assertInvalidStrictIndex(""); // empty
        assertInvalidStrictIndex("2147483648"); // larger than Integer.MAX_VALUE
    }

    @Test
    public void parseStrictIndex_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseStrictIndex(null, MESSAGE_INVALID_STRICT_INDEX));
    }

    private static void assertInvalidStrictIndex(String oneBasedIndex) {
        assertThrows(ParseException.class, MESSAGE_INVALID_STRICT_INDEX, () ->
                ParserUtil.parseStrictIndex(oneBasedIndex, MESSAGE_INVALID_STRICT_INDEX));
    }
}
