package seedu.address.model.student;

import static java.util.Objects.requireNonNull;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Represents the in-memory student roster in insertion order.
 * Duplicate identity is defined by {@link Student#isSameStudent(Student)}.
 */
public final class StudentRoster {
    private final ObservableList<Student> students = FXCollections.observableArrayList();
    private final ObservableList<Student> unmodifiableStudents = FXCollections.unmodifiableObservableList(students);

    /** Returns true if the roster already contains a student with the same name and student phone. */
    public boolean hasStudent(Student student) {
        requireNonNull(student);
        return students.stream().anyMatch(existing -> existing.isSameStudent(student));
    }

    /**
     * Adds a new student at the end of the roster.
     *
     * @throws IllegalArgumentException If a student with the same identity is already present.
     */
    public void addStudent(Student student) {
        requireNonNull(student);
        if (hasStudent(student)) {
            throw new IllegalArgumentException("A student with this name and phone already exists.");
        }
        students.add(student);
    }

    /** Returns an unmodifiable observable view of the roster in insertion order. */
    public ObservableList<Student> getStudents() {
        return unmodifiableStudents;
    }
}
