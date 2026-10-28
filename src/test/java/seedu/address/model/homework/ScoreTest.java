package seedu.address.model.homework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class ScoreTest {
    @Test
    public void constructor_invalidScore_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Score.MESSAGE_CONSTRAINTS, () -> new Score(11, 10));
        assertThrows(IllegalArgumentException.class, Score.MESSAGE_CONSTRAINTS, () -> new Score(5, 0));
        assertThrows(IllegalArgumentException.class, Score.MESSAGE_CONSTRAINTS, () -> new Score(-1, 10));
    }

    @Test
    public void constructor_negativeBareScore_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Score.MESSAGE_CONSTRAINTS, () -> new Score(-1));
        assertThrows(IllegalArgumentException.class, Score.MESSAGE_CONSTRAINTS, () -> new Score(Integer.MIN_VALUE));
    }

    @Test
    public void isValidScore() {
        // marks obtained at the boundaries -> returns true
        assertTrue(Score.isValidScore(0, 10));
        assertTrue(Score.isValidScore(10, 10));
        assertTrue(Score.isValidScore(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // smallest total -> returns true
        assertTrue(Score.isValidScore(1, 1));

        // marks obtained above the total -> returns false
        assertFalse(Score.isValidScore(11, 10));

        // negative marks obtained -> returns false
        assertFalse(Score.isValidScore(-1, 10));

        // zero or negative total -> returns false
        assertFalse(Score.isValidScore(0, 0));
        assertFalse(Score.isValidScore(0, -1));
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Score.parse(null));
    }

    @Test
    public void parse_validScore_returnsScore() {
        assertEquals(new Score(5, 10), Score.parse("5/10"));
        assertEquals(new Score(0, 10), Score.parse("0/10")); // no marks obtained
        assertEquals(new Score(10, 10), Score.parse("10/10")); // full marks
        assertEquals(new Score(35, 50), Score.parse("  35/50  ")); // surrounding whitespace ignored
        assertEquals(new Score(Integer.MAX_VALUE, Integer.MAX_VALUE), Score.parse("2147483647/2147483647"));
    }

    @Test
    public void parse_validBareScore_returnsScoreWithoutTotal() {
        assertEquals(new Score(0), Score.parse("0")); // smallest
        assertEquals(new Score(17), Score.parse("17"));
        assertEquals(new Score(17), Score.parse(" 17 ")); // surrounding whitespace ignored
        assertEquals(new Score(Integer.MAX_VALUE), Score.parse("2147483647")); // largest int
        assertEquals(new Score(1000), Score.parse("1000")); // no upper bound below the int range
    }

    @Test
    public void parse_validScore_keepsRatio() {
        Score score = Score.parse("5/10");

        assertEquals(5, score.getMarksObtained());
        assertEquals(Optional.of(10), score.getTotalMarks());
        assertFalse(score.equals(Score.parse("1/2")));
    }

    @Test
    public void parse_validBareScore_hasNoTotal() {
        Score score = Score.parse("17");

        assertEquals(17, score.getMarksObtained());
        assertEquals(Optional.empty(), score.getTotalMarks());
        assertFalse(score.equals(Score.parse("17/20")));
    }

    @Test
    public void parse_invalidScore_throwsIllegalArgumentException() {
        assertInvalidScore(""); // empty
        assertInvalidScore("5/"); // empty total
        assertInvalidScore("17/"); // empty total
        assertInvalidScore("/10"); // empty marks obtained
        assertInvalidScore("/20"); // empty marks obtained
        assertInvalidScore("17//20"); // double slash
        assertInvalidScore("17/20/30"); // three numbers
        assertInvalidScore("11/10"); // marks obtained above the total
        assertInvalidScore("5/0"); // zero total
        assertInvalidScore("0/0"); // zero total and marks
        assertInvalidScore("5.5/10"); // decimal marks obtained
        assertInvalidScore("5/10.0"); // decimal total
        assertInvalidScore("5 / 10"); // spaces inside
        assertInvalidScore("5/ 10"); // space after the slash
        assertInvalidScore("-1/10"); // negative marks obtained
        assertInvalidScore("+5/10"); // plus sign
        assertInvalidScore("5/-10"); // negative total
        assertInvalidScore("5/10/20"); // two slashes
        assertInvalidScore("five/ten"); // words
        assertInvalidScore("5:10"); // wrong separator
        assertInvalidScore("/"); // slash only
    }

    @Test
    public void parse_invalidBareScore_throwsIllegalArgumentException() {
        assertInvalidScore("+17"); // plus sign
        assertInvalidScore("-1"); // negative
        assertInvalidScore("17.5"); // decimal
        assertInvalidScore("17.0"); // decimal point
        assertInvalidScore("1 7"); // space inside
        assertInvalidScore("seventeen"); // word
        assertInvalidScore("17%"); // percentage
    }

    @Test
    public void parse_leadingZeroes_throwsIllegalArgumentException() {
        assertLeadingZeroes("017"); // bare score
        assertLeadingZeroes("07"); // bare single digit
        assertLeadingZeroes("00"); // zero written with two digits
        assertLeadingZeroes(" 07 "); // surrounding whitespace ignored
        assertLeadingZeroes("05/10"); // leading zero in marks obtained
        assertLeadingZeroes("5/010"); // leading zero in total
        assertLeadingZeroes("00/10"); // zero marks obtained written with two digits
        assertLeadingZeroes("05/010"); // leading zeroes in both numbers

        // leading zero reported before the other rules
        assertLeadingZeroes("011/10"); // marks obtained above the total
        assertLeadingZeroes("5/00"); // zero total
        assertLeadingZeroes("02147483648"); // larger than the largest int
        assertLeadingZeroes("5/02147483648"); // total larger than the largest int
    }

    @Test
    public void parse_leadingZeroesAndOtherFormatError_throwsIllegalArgumentException() {
        assertInvalidScore("-05"); // sign
        assertInvalidScore("05.5/10"); // decimal
        assertInvalidScore("05/"); // empty total
        assertInvalidScore("05 / 10"); // spaces inside
    }

    @Test
    public void parse_zeroWithoutLeadingZeroes_returnsScore() {
        assertEquals(new Score(0), Score.parse("0"));
        assertEquals(new Score(0, 5), Score.parse("0/5"));
        assertEquals(new Score(10, 100), Score.parse("10/100")); // zeroes inside a number are not leading
    }

    @Test
    public void parse_marksTooLarge_throwsIllegalArgumentException() {
        // bare score one above the largest int
        assertThrows(IllegalArgumentException.class, Score.MESSAGE_TOO_LARGE, () -> Score.parse("2147483648"));

        // marks obtained one above the largest int
        assertThrows(IllegalArgumentException.class, Score.MESSAGE_TOO_LARGE, () -> Score.parse("2147483648/10"));

        // total one above the largest int
        assertThrows(IllegalArgumentException.class, Score.MESSAGE_TOO_LARGE, () -> Score.parse("5/2147483648"));

        // far beyond the long range
        assertThrows(IllegalArgumentException.class, Score.MESSAGE_TOO_LARGE, () ->
                Score.parse("99999999999999999999/99999999999999999999"));
    }

    @Test
    public void equals() {
        Score score = new Score(5, 10);

        // same object -> returns true
        assertTrue(score.equals(score));

        // same values -> returns true
        assertTrue(score.equals(new Score(5, 10)));
        assertEquals(score.hashCode(), new Score(5, 10).hashCode());

        // null -> returns false
        assertFalse(score.equals(null));

        // different type -> returns false
        assertFalse(score.equals("5/10"));

        // different marks obtained -> returns false
        assertFalse(score.equals(new Score(6, 10)));

        // different total -> returns false
        assertFalse(score.equals(new Score(5, 11)));

        // same marks without a total -> returns false
        assertFalse(score.equals(new Score(5)));
    }

    @Test
    public void equals_bareScore() {
        Score score = new Score(17);

        // same object -> returns true
        assertTrue(score.equals(score));

        // same value -> returns true
        assertTrue(score.equals(new Score(17)));
        assertEquals(score.hashCode(), new Score(17).hashCode());

        // null -> returns false
        assertFalse(score.equals(null));

        // different marks -> returns false
        assertFalse(score.equals(new Score(18)));

        // same marks with a total -> returns false
        assertFalse(score.equals(new Score(17, 20)));
        assertFalse(score.equals(new Score(17, 17)));
    }

    @Test
    public void toStringMethod() {
        assertEquals("5/10", new Score(5, 10).toString());
        assertEquals("0/10", new Score(0, 10).toString());
        assertEquals("17", new Score(17).toString());
        assertEquals("0", new Score(0).toString());
    }

    private static void assertInvalidScore(String rawScore) {
        assertThrows(IllegalArgumentException.class, Score.MESSAGE_CONSTRAINTS, () -> Score.parse(rawScore));
    }

    private static void assertLeadingZeroes(String rawScore) {
        assertThrows(IllegalArgumentException.class, Score.MESSAGE_LEADING_ZEROES, () -> Score.parse(rawScore));
    }
}
