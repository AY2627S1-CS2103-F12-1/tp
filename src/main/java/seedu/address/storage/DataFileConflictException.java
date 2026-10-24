package seedu.address.storage;

import java.io.IOException;

/** Indicates that saving would conflict with another writer's data or active save. */
public class DataFileConflictException extends IOException {
    public static final String MESSAGE_CHANGED = "The data file has changed since it was loaded. "
            + "Restart TutorFlow before making changes. No data was changed.";
    public static final String MESSAGE_BUSY = "Another TutorFlow instance is saving this data file. "
            + "Try again. No data was changed.";

    public DataFileConflictException(String message) {
        super(message);
    }
}
