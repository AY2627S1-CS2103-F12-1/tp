package seedu.address.model.homework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.Duration;

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
    public void constructor_noVisibleCharacter_throwsIllegalArgumentException() {
        assertInvalidTitle("\u200B"); // zero-width space
        assertInvalidTitle(" \u200B "); // zero-width space between spaces
        assertInvalidTitle("\u200B\u200C\u200D\u2060\uFEFF"); // only format characters
        assertInvalidTitle("\u200B \u00A0\u3000"); // zero-width space and Unicode spaces
    }

    @Test
    public void constructor_blankCharactersOnly_throwsIllegalArgumentException() {
        assertInvalidTitle("\u3164"); // Hangul filler
        assertInvalidTitle("\u115F\u1160\uFFA0"); // other Hangul fillers
        assertInvalidTitle("\u2800"); // braille blank
        assertInvalidTitle("\u17B4\u17B5"); // Khmer inherent vowels
        assertInvalidTitle("\uFE0F"); // variation selector
        assertInvalidTitle("\uFE00 \u034F"); // variation selector and combining grapheme joiner
        assertInvalidTitle("\uDB40\uDD00"); // variation selector U+E0100 from the supplement
        assertInvalidTitle("\uDB40\uDC67\uDB40\uDC7F"); // tag characters without a flag
    }

    @Test
    public void constructor_longInnerRunOfZeroWidthSpaces_rejectedQuickly() {
        // the run is not at either end, so trimming must not retry it from every position
        String rawTitle = "a" + "\u200B".repeat(50_000) + "b";

        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> assertInvalidTitle(rawTitle));
    }

    @Test
    public void constructor_surroundingBlankFillers_removed() {
        assertEquals("Algebra drill", new Title("\u3164Algebra drill").toString()); // leading Hangul filler
        assertEquals("Algebra drill", new Title("Algebra drill\u2800").toString()); // trailing braille blank
        assertEquals("Algebra drill", new Title("\u17B4 Algebra drill \u3164\u200B").toString()); // mixed
    }

    @Test
    public void constructor_variationSelector_keptInTitle() {
        // red heart followed by the emoji variation selector U+FE0F
        assertEquals("I \u2764\uFE0F maths", new Title("I \u2764\uFE0F maths").toString());
        assertEquals("Maths \u2764\uFE0F", new Title("Maths \u2764\uFE0F").toString()); // at the end
        assertEquals("\u2764\uFE0F", new Title("\u2764\uFE0F").toString()); // the emoji alone is visible
    }

    @Test
    public void constructor_shownFormatCharacter_keptInTitle() {
        // the Arabic number sign U+0600 is a format character that is shown before the digits
        assertEquals("\u0600123 practice", new Title("\u0600123 practice").toString());
        assertEquals("\u0600", new Title("\u0600").toString());
    }

    @Test
    public void constructor_surroundingFormatCharacters_removed() {
        assertEquals("Algebra drill", new Title("\u200BAlgebra drill").toString()); // leading zero-width space
        assertEquals("Algebra drill", new Title("Algebra drill\u200B").toString()); // trailing zero-width space
        assertEquals("Algebra drill", new Title("\uFEFFAlgebra drill").toString()); // leading byte order mark
        assertEquals("Algebra drill", new Title(" \u200B Algebra drill \u2060 ").toString()); // mixed with spaces
        assertEquals("Algebra", new Title(" \u200B Algebra \uFEFF ").toString()); // spaces left after a format char
    }

    @Test
    public void constructor_formatCharacterInsideTitle_keptInTitle() {
        assertEquals("Alge\u00ADbra", new Title("Alge\u00ADbra").toString()); // soft hyphen inside a word
        assertEquals("Ca\u200Bf\u00E9", new Title("Ca\u200Bf\u00E9").toString()); // zero-width space inside a word
        assertEquals("a \u200B b", new Title("a \u200B b").toString()); // zero-width space between spaces
    }

    @Test
    public void constructor_flagEmojiAtEnd_keptInTitle() {
        // black flag followed by the tag characters for "gbeng" and a cancel tag, which are format characters
        String englandFlag = "\uD83C\uDFF4"
                + "\uDB40\uDC67\uDB40\uDC62\uDB40\uDC65\uDB40\uDC6E\uDB40\uDC67\uDB40\uDC7F";

        assertEquals("Read about " + englandFlag, new Title("Read about " + englandFlag).toString());
        assertEquals("Read about " + englandFlag, new Title("Read about " + englandFlag + "\u200B").toString());
        assertEquals(englandFlag, new Title(englandFlag).toString()); // the flag alone is visible
    }

    @Test
    public void constructor_lengthCountedAfterRemovingFormatCharacters_success() {
        assertEquals("A".repeat(100), new Title("\u200B" + "A".repeat(100) + "\u200B").toString());
    }

    @Test
    public void constructor_emojiWithZeroWidthJoiners_keptInTitle() {
        // man, woman and girl joined into one family emoji by zero-width joiners (U+200D)
        String family = "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67";

        assertEquals("Read with " + family, new Title("Read with " + family).toString());
        assertEquals(family, new Title(family).toString()); // an emoji alone is visible
        assertEquals("Read with " + family, new Title("Read with " + family + "\u200D").toString()); // trailing joiner
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
    public void isSameTitleIgnoringCase_differentUnicodeForms_returnsTrue() {
        // composed small e with acute accent, and capital E followed by a combining acute accent
        assertTrue(new Title("Caf\u00E9").isSameTitleIgnoringCase(new Title("CAFE\u0301")));
    }

    @Test
    public void isSameTitleIgnoringCase_leadingZeroWidthSpace_returnsTrue() {
        assertTrue(new Title("\u200BCaf\u00E9").isSameTitleIgnoringCase(new Title("Caf\u00E9")));
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

        // same text apart from a surrounding zero-width space -> returns true
        assertTrue(new Title("Algebra drill").equals(new Title("\u200BAlgebra drill")));
        assertEquals(new Title("Algebra drill").hashCode(), new Title("\u200BAlgebra drill").hashCode());

        // same text in a different Unicode form -> returns true
        assertTrue(new Title("Caf\u00E9").equals(new Title("Cafe\u0301")));
        assertEquals(new Title("Caf\u00E9").hashCode(), new Title("Cafe\u0301").hashCode());

        // different case -> returns false
        assertFalse(title.equals(new Title("complete algebra worksheet")));

        // different value -> returns false
        assertFalse(title.equals(new Title("Revise atomic structure")));
    }

    private static void assertInvalidTitle(String rawTitle) {
        assertThrows(IllegalArgumentException.class, Title.MESSAGE_CONSTRAINTS, () -> new Title(rawTitle));
    }
}
