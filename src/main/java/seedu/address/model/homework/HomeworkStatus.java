package seedu.address.model.homework;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

/**
 * Explicit completion state, independent of homework scores and due dates.
 */
public enum HomeworkStatus {
    ASSIGNED, COMPLETED;

    public static final String MESSAGE_CONSTRAINTS = "Homework status must be one of: ASSIGNED, COMPLETED.";

    /**
     * Parses a status, ignoring case and surrounding whitespace.
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
