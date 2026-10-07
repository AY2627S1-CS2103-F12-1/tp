package seedu.address.model;

import javafx.collections.ObservableList;
import seedu.address.model.student.Student;

/**
 * Unmodifiable view of the app data: the student roster and each student's homework.
 */
public interface ReadOnlyAddressBook {

    /**
     * Returns an unmodifiable view of the student list in roster order.
     * This list will not contain any duplicate students.
     */
    ObservableList<Student> getStudentList();

}
