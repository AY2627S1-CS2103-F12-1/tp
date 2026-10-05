package seedu.address.model.student;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

/** Represents one of the academic levels supported by TutorFlow. */
public enum AcademicLevel {
    S1, S2, S3, S4, S5, J1, J2;

    public static final String MESSAGE_CONSTRAINTS = "Academic level must be one of: S1, S2, S3, S4, S5, J1, J2.";

    /**
     * Returns the academic level represented by the input, ignoring surrounding whitespace and case.
     *
     * @throws IllegalArgumentException If the input is not one of the supported levels.
     */
    public static AcademicLevel parse(String rawLevel) {
        requireNonNull(rawLevel);
        try {
            return valueOf(rawLevel.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS, exception);
        }
    }
}
