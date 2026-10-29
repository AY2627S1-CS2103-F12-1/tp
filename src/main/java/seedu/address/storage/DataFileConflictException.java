package seedu.address.storage;

import java.io.IOException;

/** Indicates that saving would conflict with another writer's data or active save. */
public class DataFileConflictException extends IOException {
    public static final String MESSAGE_CHANGED = "The data file has changed since it was loaded. "
            + "Restart TutorFlow before making changes. No data was changed.";
    public static final String MESSAGE_BUSY = "Another TutorFlow instance is saving this data file. "
            + "Try again. No data was changed.";
    /** Message for a data file that did not exist at launch, whose {@code %s} is the data file's name. */
    public static final String MESSAGE_CREATED = "Another program or copy of TutorFlow created %s after this copy "
            + "started. Restart TutorFlow before making changes. No data was changed.";

    public DataFileConflictException(String message) {
        super(message);
    }

    /**
     * Returns the message for the user, naming the data file as {@code shownName} where the message names it.
     */
    public String getMessage(String shownName) {
        return String.format(getMessage(), shownName);
    }
}
