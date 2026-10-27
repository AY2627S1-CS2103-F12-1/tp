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
        assertInvalidTitle("\u00A0\u3000"); // only Unicode spaces
    }

    @Test
    public void constructor_zeroWidthSpaceOnly_throwsIllegalArgumentException() {
        // Issue #139: a title that looks blank
        assertInvalidTitle("\u200B");
        assertInvalidTitle(" \u200B ");
    }

    @Test
    public void constructor_invisibleCharacter_throwsIllegalArgumentException() {
        assertInvalidTitle("Alge\u200Bbra drill"); // zero-width space inside a word
        assertInvalidTitle("\u200BAlgebra drill"); // zero-width space at the start
        assertInvalidTitle("\u2764\uFE0F"); // red heart followed by the emoji variation selector
        assertInvalidTitle("\uD83D\uDC68\u200D\uD83D\uDC69"); // man and woman joined by a zero-width joiner
    }

    @Test
    public void constructor_controlCharacter_throwsIllegalArgumentException() {
        assertInvalidTitle("Algebra\u0007worksheet"); // bell character
        assertInvalidTitle("Algebra\u0000worksheet"); // null character
        assertInvalidTitle("Algebra\u007Fworksheet"); // delete character
    }

    @Test
    public void constructor_lineBreakOrTab_collapsedToSpace() {
        assertEquals("Algebra worksheet", new Title("Algebra\nworksheet").toString()); // line feed
        assertEquals("Algebra worksheet", new Title("Algebra\r\nworksheet").toString()); // CR and LF
        assertEquals("Algebra worksheet", new Title("Algebra\u2028worksheet").toString()); // Unicode line separator
        assertEquals("Algebra worksheet", new Title("Algebra\tworksheet").toString()); // tab
        assertEquals("Algebra worksheet", new Title("Algebra worksheet\n").toString()); // trailing line break
    }

    @Test
    public void constructor_lettersNumbersPunctuationAndSymbols_success() {
        assertValidTitle("Caf\u00E9"); // accented letter
        assertValidTitle("Zo\u00EB's essay"); // accented letter and apostrophe
        assertValidTitle("x\u00B2 + \u03C0 \u2248 3.14"); // superscript, Greek letter and math symbols
        assertValidTitle("\uD83D\uDE00 quiz"); // emoji
        assertValidTitle("Ch 3 Q1-10 (pg. 45)");
        assertValidTitle("Q1-5: solve x = 0, then check #3!");
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
    public void constructor_lengthCountedAfterUnicodeNormalization() {
        // each e followed by a combining acute accent becomes one character
        assertEquals("\u00E9".repeat(100), new Title("e\u0301".repeat(100)).toString()); // 100 characters
        assertInvalidTitle("e\u0301".repeat(101)); // 101 characters
    }

    @Test
    public void constructor_decomposedAccent_normalizedToComposedForm() {
        assertEquals("Caf\u00E9", new Title("Cafe\u0301").toString());
    }

    @Test
    public void constructor_compatibilityCharacters_kept() {
        // NFC keeps characters that only NFKC would change, such as superscripts and ligatures
        assertEquals("x\u00B2 \uFB01nal", new Title("x\u00B2 \uFB01nal").toString());
    }

    @Test
    public void constructor_extraSpaces_normalizedAndCasePreserved() {
        assertEquals("Complete Algebra worksheet", new Title("  Complete   Algebra  worksheet ").toString());
    }

    @Test
    public void isSameTitleIgnoringCase() {
        Title title = new Title("Complete algebra worksheet");

        assertTrue(title.isSameTitleIgnoringCase(new Title("COMPLETE  algebra Worksheet")));
        assertFalse(title.isSameTitleIgnoringCase(new Title("Complete algebra worksheets")));
        assertFalse(title.isSameTitleIgnoringCase(null));
    }

    @Test
    public void isSameTitleIgnoringCase_differentUnicodeForms_returnsTrue() {
        // composed small e with acute accent, and capital E followed by a combining acute accent
        assertTrue(new Title("Caf\u00E9").isSameTitleIgnoringCase(new Title("CAFE\u0301")));
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

        // same text in a different Unicode form -> returns true
        assertTrue(new Title("Caf\u00E9").equals(new Title("Cafe\u0301")));
        assertEquals(new Title("Caf\u00E9").hashCode(), new Title("Cafe\u0301").hashCode());

        // different case -> returns false
        assertFalse(title.equals(new Title("complete algebra worksheet")));

        // different value -> returns false
        assertFalse(title.equals(new Title("Revise atomic structure")));
    }

    private static void assertValidTitle(String rawTitle) {
        assertEquals(rawTitle, new Title(rawTitle).toString());
    }

    private static void assertInvalidTitle(String rawTitle) {
        assertThrows(IllegalArgumentException.class, Title.MESSAGE_CONSTRAINTS, () -> new Title(rawTitle));
    }
}
