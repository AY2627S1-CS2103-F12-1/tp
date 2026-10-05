package seedu.address.model.homework;

import static java.util.Objects.requireNonNull;

import java.util.regex.Pattern;

/**
 * Represents the title of a homework record.
 * Guarantees: immutable; whitespace is normalized; length and characters satisfy the Add Homework specification.
 */
public final class Title {
    public static final String MESSAGE_CONSTRAINTS = "Homework title must be 1 to 100 characters and must not "
            + "contain line breaks or control characters.";

    private static final int MAX_LENGTH = 100;
    private static final Pattern WHITESPACE = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern LINE_BREAK_OR_CONTROL = Pattern.compile("[\\p{Cc}\\u2028\\u2029]");

    private final String value;

    /**
     * Creates a title after replacing consecutive spaces with one space and removing surrounding whitespace.
     * Line breaks and control characters are rejected before normalization. Capitalization is preserved for display.
     *
     * @throws IllegalArgumentException If the input contains a line break or control character, or the normalized
     *         title is empty or longer than 100 characters.
     */
    public Title(String rawTitle) {
        requireNonNull(rawTitle);
        if (LINE_BREAK_OR_CONTROL.matcher(rawTitle).find()) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        String normalizedTitle = WHITESPACE.matcher(rawTitle).replaceAll(" ").strip();
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
