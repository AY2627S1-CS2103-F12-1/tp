package seedu.address.model;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.student.Student;

/**
 * The API of the Model component.
 */
public interface Model {
    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /** Returns true if a student with the same normalized name and student phone exists. */
    boolean hasStudent(Student student);

    /** Adds a student to the end of the student roster. The student must not already exist. */
    void addStudent(Student student);

    /** Deletes a student from the student roster. The student must be present. */
    void deleteStudent(Student student);

    /**
     * Replaces the given student {@code target} with {@code editedStudent}, keeping its position in the roster.
     * {@code target} must exist in the roster.
     * The identity of {@code editedStudent} must not be the same as another existing student in the roster.
     */
    void setStudent(Student target, Student editedStudent);

    /** Returns an unmodifiable observable view of the student roster. */
    ObservableList<Student> getStudentList();
}
