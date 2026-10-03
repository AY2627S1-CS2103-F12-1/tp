package seedu.address.model.student;

import static java.util.Objects.requireNonNull;

import java.util.regex.Pattern;

/**
 * Represents a student's or guardian's Singapore or international phone number.
 * Guarantees: immutable; the number is stored as text after surrounding whitespace is removed.
 */
public final class StudentPhone {
    public static final String MESSAGE_CONSTRAINTS = "Phone numbers must be an 8-digit Singapore number starting with "
            + "6, 8, or 9, or an international number in +<country code><number> format.";

    private static final Pattern VALID_PHONE = Pattern.compile("(?:[689][0-9]{7}|\\+[0-9]{8,15})");

    private final String value;

    /**
     * Creates a phone number after removing surrounding whitespace.
     *
     * @throws IllegalArgumentException If the number is neither an eight-digit Singapore number starting with
     *         6, 8, or 9 nor an international number with a plus sign and 8 to 15 digits.
     */
    public StudentPhone(String rawPhone) {
        requireNonNull(rawPhone);
        String trimmedPhone = rawPhone.strip();
        if (!VALID_PHONE.matcher(trimmedPhone).matches()) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        value = trimmedPhone;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof StudentPhone phone && value.equals(phone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
