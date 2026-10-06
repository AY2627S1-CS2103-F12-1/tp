package seedu.address.model.homework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TitleTest {
    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Title(null));
    }

    @Test
    public void constructor_emptyOrWhitespaceOnly_throwsIllegalArgumentException() {
        assertInvalidTitle(""); // 0 characters
        assertInvalidTitle("   "); // 0 characters after normalization
    }

    @Test
    public void constructor_lengthBoundaries() {
        assertEquals("A", new Title("A").toString()); // 1 character
        assertEquals("A".repeat(100), new Title("A".repeat(100)).toString()); // 100 characters
        assertInvalidTitle("A".repeat(101)); // 101 characters
    }

    @Test
    public void constructor_lengthCountedAfterNormalization_success() {
        String rawTitle = "  " + "A".repeat(50) + "     " + "B".repeat(49) + "  ";

        assertEquals("A".repeat(50) + " " + "B".repeat(49), new Title(rawTitle).toString());
    }

    @Test
    public void constructor_extraSpaces_normalizedAndCasePreserved() {
        assertEquals("Complete Algebra worksheet", new Title("  Complete   Algebra  worksheet ").toString());
    }

    @Test
    public void constructor_punctuationAndMathSymbols_success() {
        String rawTitle = "Q1-5: solve x² + 2x = 0 (π ≈ 3.14), then check #3!";

        assertEquals(rawTitle, new Title(rawTitle).toString());
    }

    @Test
    public void constructor_lineBreakOrControlCharacter_throwsIllegalArgumentException() {
        assertInvalidTitle("Algebra\nworksheet"); // line feed
        assertInvalidTitle("Algebra\r\nworksheet"); // carriage return and line feed
        assertInvalidTitle("Algebra\u2028worksheet"); // Unicode line separator
        assertInvalidTitle("Algebra\u2029worksheet"); // Unicode paragraph separator
        assertInvalidTitle("Algebra\u0085worksheet"); // next line
        assertInvalidTitle("Algebra\tworksheet"); // tab is rejected, not collapsed
        assertInvalidTitle("Algebra worksheet\n"); // trailing line break is rejected, not stripped
        assertInvalidTitle("Algebra\u0000worksheet"); // null character
        assertInvalidTitle("Algebra\u007Fworksheet"); // delete character
    }

    @Test
    public void isSameTitleIgnoringCase() {
        Title title = new Title("Complete algebra worksheet");

        assertTrue(title.isSameTitleIgnoringCase(new Title("COMPLETE  algebra Worksheet")));
        assertFalse(title.isSameTitleIgnoringCase(new Title("Complete algebra worksheets")));
        assertFalse(title.isSameTitleIgnoringCase(null));
    }

    @Test
    public void equals() {
        Title title = new Title("Complete algebra worksheet");

        // same object -> returns true
        assertTrue(title.equals(title));

        // same normalized value -> returns true
        assertTrue(title.equals(new Title(" Complete  algebra worksheet ")));
        assertEquals(title.hashCode(), new Title(" Complete  algebra worksheet ").hashCode());

        // null -> returns false
        assertFalse(title.equals(null));

        // different type -> returns false
        assertFalse(title.equals("Complete algebra worksheet"));

        // different case -> returns false
        assertFalse(title.equals(new Title("complete algebra worksheet")));

        // different value -> returns false
        assertFalse(title.equals(new Title("Revise atomic structure")));
    }

    private static void assertInvalidTitle(String rawTitle) {
        assertThrows(IllegalArgumentException.class, Title.MESSAGE_CONSTRAINTS, () -> new Title(rawTitle));
    }
}
