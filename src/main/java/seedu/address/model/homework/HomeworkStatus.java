package seedu.address.model.homework;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

/** Represents whether a homework record is still outstanding or has been completed. */
public enum HomeworkStatus {
    ASSIGNED, COMPLETED;

    public static final String MESSAGE_CONSTRAINTS = "Homework status must be one of: ASSIGNED, COMPLETED.";

    /**
     * Returns the status represented by the input, ignoring surrounding whitespace and case.
     *
     * @throws IllegalArgumentException If the input is not one of the supported statuses.
     */
    public static HomeworkStatus parse(String rawStatus) {
        requireNonNull(rawStatus);
        try {
            return valueOf(rawStatus.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS, exception);
        }
    }
}
