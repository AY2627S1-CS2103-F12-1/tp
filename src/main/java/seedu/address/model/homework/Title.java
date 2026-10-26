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
    public static final String MESSAGE_CONSTRAINTS = "Homework title must be 1 to " + MAX_LENGTH + " characters and"
            + " must not contain line breaks or control characters.";

    private static final Pattern WHITESPACE = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern LINE_BREAK_OR_CONTROL = Pattern.compile("[\\p{Cc}\\u2028\\u2029]");

    private final String value;

    /**
     * Creates a title after converting it to Unicode NFC, replacing consecutive spaces with one space and removing
     * surrounding whitespace. NFC gives canonically equivalent text one encoding, e.g. {@code é} written as one
     * character or as {@code e} followed by a combining accent; look-alike letters of other scripts stay different.
     * Line breaks and control characters are rejected before normalization. Capitalization is preserved for display.
     *
     * @throws IllegalArgumentException If the input contains a line break or control character, or the normalized
     *         title is empty or longer than {@link #MAX_LENGTH} characters.
     */
    public Title(String rawTitle) {
        requireNonNull(rawTitle);
        if (LINE_BREAK_OR_CONTROL.matcher(rawTitle).find()) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        String nfcTitle = Normalizer.normalize(rawTitle, Normalizer.Form.NFC);
        String normalizedTitle = WHITESPACE.matcher(nfcTitle).replaceAll(" ").strip();
        int length = normalizedTitle.codePointCount(0, normalizedTitle.length());
        if (length < 1 || length > MAX_LENGTH) {
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
