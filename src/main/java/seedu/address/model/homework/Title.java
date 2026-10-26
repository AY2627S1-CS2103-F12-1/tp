package seedu.address.model.homework;

import static java.util.Objects.requireNonNull;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Represents the title of a homework record.
 * Guarantees: immutable; whitespace and Unicode (to NFC) are normalized; length and characters satisfy the Add
 * Homework specification.
 */
public final class Title {
    public static final int MAX_LENGTH = 100;
    public static final String MESSAGE_CONSTRAINTS = "Homework title must be 1 to " + MAX_LENGTH + " characters with"
            + " at least one visible character and must not contain line breaks or control characters.";

    private static final Pattern WHITESPACE = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern LINE_BREAK_OR_CONTROL = Pattern.compile("[\\p{Cc}\\u2028\\u2029]");
    /** Characters that are not whitespace or format characters but show as blank, such as the Hangul filler. */
    private static final String BLANK_FILLERS = "\\u115F\\u1160\\u3164\\uFFA0\\u2800\\u17B4\\u17B5";
    /** Format characters that are shown, the prepended concatenation marks such as the Arabic number sign. */
    private static final String SHOWN_FORMAT_CHARACTERS =
            "\\u0600-\\u0605\\u06DD\\u070F\\u0890\\u0891\\u08E2\\x{110BD}\\x{110CD}";
    /** Variation selectors, which only change the character before them, such as U+FE0F at the end of an emoji. */
    private static final String VARIATION_SELECTORS = "\\uFE00-\\uFE0F\\x{E0100}-\\x{E01EF}";
    /** Format characters that end emoji flags, such as England's. */
    private static final String TAG_CHARACTERS = "\\x{E0020}-\\x{E007F}";
    /** Whitespace, format characters and blank fillers, which are removed from both ends of a title. */
    private static final String TRIMMED_CHARACTER =
            "[[\\s\\p{Cf}" + BLANK_FILLERS + "]&&[^" + SHOWN_FORMAT_CHARACTERS + TAG_CHARACTERS + "]]";
    /**
     * Characters that do not count as visible: the trimmed ones, tag characters, the combining grapheme joiner and
     * variation selectors, which only change the character before them.
     */
    private static final String INVISIBLE_CHARACTER =
            "[[\\s\\p{Cf}" + BLANK_FILLERS + "\\u034F" + VARIATION_SELECTORS + "]&&[^" + SHOWN_FORMAT_CHARACTERS + "]]";
    /** The lookbehind lets a run inside the title be tried only once, so long runs take linear time. */
    private static final Pattern SURROUNDING_TRIMMED_CHARACTERS = Pattern.compile(
            "^" + TRIMMED_CHARACTER + "+|(?<!" + TRIMMED_CHARACTER + ")" + TRIMMED_CHARACTER + "+$",
            Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern INVISIBLE_CHARACTERS_ONLY =
            Pattern.compile(INVISIBLE_CHARACTER + "*", Pattern.UNICODE_CHARACTER_CLASS);

    private final String value;

    /**
     * Creates a title after converting it to Unicode NFC, replacing consecutive spaces with one space and removing
     * surrounding whitespace, format characters, such as a zero-width space, and blank fillers, such as the Hangul
     * filler. NFC gives canonically equivalent text one encoding, e.g. {@code é} written as one character or as
     * {@code e} followed by a combining accent; look-alike letters of other scripts stay different. Line breaks and
     * control characters are rejected before normalization. Capitalization is preserved for display. Format
     * characters inside the title, such as the zero-width joiner in an emoji sequence, are kept, and so are the tag
     * characters at the end of an emoji flag and the variation selector at the end of an emoji such as a red heart.
     *
     * @throws IllegalArgumentException If the input contains a line break or control character, or the normalized
     *         title is empty, longer than {@link #MAX_LENGTH} characters, or has no visible character.
     */
    public Title(String rawTitle) {
        requireNonNull(rawTitle);
        if (LINE_BREAK_OR_CONTROL.matcher(rawTitle).find()) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        String nfcTitle = Normalizer.normalize(rawTitle, Normalizer.Form.NFC);
        String spacedTitle = WHITESPACE.matcher(nfcTitle).replaceAll(" ");
        String normalizedTitle = SURROUNDING_TRIMMED_CHARACTERS.matcher(spacedTitle).replaceAll("");
        int length = normalizedTitle.codePointCount(0, normalizedTitle.length());
        if (length < 1 || length > MAX_LENGTH || INVISIBLE_CHARACTERS_ONLY.matcher(normalizedTitle).matches()) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        value = normalizedTitle;
    }

    /**
     * Returns true if both normalized titles have the same characters regardless of case.
     */
    public boolean isSameTitleIgnoringCase(Title other) {
        return other != null && value.equalsIgnoreCase(other.value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Title title && value.equals(title.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
