package seedu.address.storage;

import java.io.IOException;

/** Indicates that saving would conflict with another writer's data or active save. */
public class DataFileConflictException extends IOException {
    /** Message for a data file changed since this copy last read or wrote it, whose {@code %s} is its name. */
    public static final String MESSAGE_CHANGED = "Another program or copy of TutorFlow changed %s after this copy "
            + "last loaded or saved it. Restart TutorFlow before making changes. No data was changed.";
    /** Message for a data file that another copy is saving, whose {@code %s} is the data file's name. */
    public static final String MESSAGE_BUSY = "Another copy of TutorFlow is saving %s. Try again. No data was changed.";
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
