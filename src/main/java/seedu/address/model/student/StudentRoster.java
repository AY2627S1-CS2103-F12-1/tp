package seedu.address.model.student;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Represents the in-memory student roster in insertion order.
 * Duplicate identity is defined by {@link Student#isSameStudent(Student)}.
 */
public final class StudentRoster {
    private static final String MESSAGE_DUPLICATE_STUDENT = "A student with this name and phone already exists.";
    private static final String MESSAGE_STUDENT_NOT_FOUND = "The student to replace is not in the roster.";

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
            throw new IllegalArgumentException(MESSAGE_DUPLICATE_STUDENT);
        }
        students.add(student);
    }

    /**
     * Replaces {@code target} with {@code editedStudent} at the same position in the roster.
     *
     * @throws IllegalArgumentException If {@code target} is not in the roster, or {@code editedStudent} has the
     *         identity of a student other than {@code target}.
     */
    public void setStudent(Student target, Student editedStudent) {
        requireAllNonNull(target, editedStudent);
        int index = students.indexOf(target);
        if (index == -1) {
            throw new IllegalArgumentException(MESSAGE_STUDENT_NOT_FOUND);
        }
        if (!target.isSameStudent(editedStudent) && hasStudent(editedStudent)) {
            throw new IllegalArgumentException(MESSAGE_DUPLICATE_STUDENT);
        }
        students.set(index, editedStudent);
    }

    /** Returns an unmodifiable observable view of the roster in insertion order. */
    public ObservableList<Student> getStudents() {
        return unmodifiableStudents;
    }
}
