package seedu.address.model.student;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

/** Represents one of the tuition subjects offered by TutorFlow. */
public enum TuitionSubject {
    MATH, PHYSICS, CHEMISTRY;

    public static final String MESSAGE_CONSTRAINTS = "Subject must be one of: MATH, PHYSICS, CHEMISTRY.";

    /**
     * Returns the subject represented by the input, ignoring surrounding whitespace and case.
     *
     * @throws IllegalArgumentException If the input is not one of the supported subjects.
     */
    public static TuitionSubject parse(String rawSubject) {
        requireNonNull(rawSubject);
        try {
            return valueOf(rawSubject.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS, exception);
        }
    }
}
