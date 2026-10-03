package seedu.address.model.student;

import static java.util.Objects.requireNonNull;

import java.util.regex.Pattern;

/**
 * Represents a student's or guardian's name.
 * Guarantees: immutable; whitespace is normalized; length and characters satisfy the Add Student specification.
 */
public final class StudentName {
    public static final String MESSAGE_CONSTRAINTS = "Names must be 1 to 70 characters and may contain "
            + "letters, numbers, spaces, apostrophes, hyphens, periods, parentheses, or '/'.";

    private static final int MAX_LENGTH = 70;
    private static final Pattern WHITESPACE = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern VALID_CHARACTERS = Pattern.compile("[\\p{L}\\p{N} '()./\\-]+");

    private final String value;

    /**
     * Creates a name after replacing consecutive whitespace with one space and removing surrounding whitespace.
     * Capitalization is preserved for display.
     *
     * @throws IllegalArgumentException If the normalized name is empty, longer than 70 characters, or has
     *         unsupported characters.
     */
    public StudentName(String rawName) {
        requireNonNull(rawName);
        String normalizedName = WHITESPACE.matcher(rawName).replaceAll(" ").strip();
        int length = normalizedName.codePointCount(0, normalizedName.length());
        if (length < 1 || length > MAX_LENGTH || !VALID_CHARACTERS.matcher(normalizedName).matches()) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        value = normalizedName;
    }

    /**
     * Returns true if both normalized names have the same letters regardless of case.
     */
    public boolean isSameNameIgnoringCase(StudentName other) {
        return other != null && value.equalsIgnoreCase(other.value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof StudentName name && value.equals(name.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
