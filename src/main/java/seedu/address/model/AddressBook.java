package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentRoster;

/**
 * Wraps all data at the address-book level: the student roster, which holds each student's homework.
 * Duplicates are not allowed (by .isSameStudent comparison).
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final StudentRoster students = new StudentRoster();

    public AddressBook() {}

    /**
     * Creates an AddressBook using the Students in the {@code toBeCopied}
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the student list with {@code students}.
     * {@code students} must not contain duplicate students.
     */
    public void setStudents(List<Student> students) {
        this.students.setStudents(students);
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        setStudents(newData.getStudentList());
    }

    //// student-level operations

    /**
     * Returns true if a student with the same identity as {@code student} exists in the address book.
     */
    public boolean hasStudent(Student student) {
        requireNonNull(student);
        return students.hasStudent(student);
    }

    /**
     * Adds a student to the end of the student list.
     * The student must not already exist in the address book.
     */
    public void addStudent(Student student) {
        students.addStudent(student);
    }

    /** Removes {@code student} and its homework from the address book. The student must exist. */
    public void deleteStudent(Student student) {
        students.deleteStudent(student);
    }

    /**
     * Replaces the given student {@code target} in the list with {@code editedStudent}, keeping its position.
     * {@code target} must exist in the address book.
     * The identity of {@code editedStudent} must not be the same as another existing student in the address book.
     */
    public void setStudent(Student target, Student editedStudent) {
        students.setStudent(target, editedStudent);
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("students", getStudentList())
                .toString();
    }

    @Override
    public ObservableList<Student> getStudentList() {
        return students.getStudents();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBook otherAddressBook)) {
            return false;
        }

        return getStudentList().equals(otherAddressBook.getStudentList());
    }

    @Override
    public int hashCode() {
        return getStudentList().hashCode();
    }
}
