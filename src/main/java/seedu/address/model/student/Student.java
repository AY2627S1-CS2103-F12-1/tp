package seedu.address.model.student;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Objects;

import seedu.address.model.homework.Homework;

/**
 * Represents a student profile with the six fields required by Add Student and the student's homework.
 * Guarantees: immutable; all fields are present and validated by their value types.
 */
public final class Student {
    private final StudentName name;
    private final AcademicLevel academicLevel;
    private final TuitionSubjects subjects;
    private final StudentPhone phone;
    private final StudentName guardianName;
    private final StudentPhone guardianPhone;
    private final List<Homework> homeworks;

    /**
     * Creates a student with all six required Add Student fields and no homework.
     *
     * @throws NullPointerException If any field is null.
     */
    public Student(StudentName name, AcademicLevel academicLevel, TuitionSubjects subjects, StudentPhone phone,
            StudentName guardianName, StudentPhone guardianPhone) {
        this(name, academicLevel, subjects, phone, guardianName, guardianPhone, List.of());
    }

    /**
     * Creates a student with all six required Add Student fields and the given homework in insertion order.
     *
     * @throws NullPointerException If any field, or any homework in {@code homeworks}, is null.
     */
    public Student(StudentName name, AcademicLevel academicLevel, TuitionSubjects subjects, StudentPhone phone,
            StudentName guardianName, StudentPhone guardianPhone, List<Homework> homeworks) {
        requireAllNonNull(name, academicLevel, subjects, phone, guardianName, guardianPhone, homeworks);
        this.name = name;
        this.academicLevel = academicLevel;
        this.subjects = subjects;
        this.phone = phone;
        this.guardianName = guardianName;
        this.guardianPhone = guardianPhone;
        this.homeworks = List.copyOf(homeworks);
    }

    public StudentName getName() {
        return name;
    }

    public AcademicLevel getAcademicLevel() {
        return academicLevel;
    }

    public TuitionSubjects getSubjects() {
        return subjects;
    }

    public StudentPhone getPhone() {
        return phone;
    }

    public StudentName getGuardianName() {
        return guardianName;
    }

    public StudentPhone getGuardianPhone() {
        return guardianPhone;
    }

    /** Returns an unmodifiable list of this student's homework in insertion order. */
    public List<Homework> getHomeworks() {
        return homeworks;
    }

    /** Returns the number of this student's homework records that are still assigned. */
    public int getAssignedHomeworkCount() {
        return (int) homeworks.stream().filter(Homework::isAssigned).count();
    }

    /**
     * Returns true if this student already has homework with the same identity as {@code homework}, as defined by
     * {@link Homework#isSameHomework(Homework)}.
     */
    public boolean hasHomework(Homework homework) {
        return homeworks.stream().anyMatch(existing -> existing.isSameHomework(homework));
    }

    /**
     * Returns a copy of this student with the same six profile fields and {@code homeworks} as the homework list.
     *
     * @throws NullPointerException If {@code homeworks} or any homework in it is null.
     */
    public Student withHomeworks(List<Homework> homeworks) {
        return new Student(name, academicLevel, subjects, phone, guardianName, guardianPhone, homeworks);
    }

    /**
     * Returns true if both students have the same whitespace-normalized name, ignoring case, and the same
     * student phone number exactly. Guardian details, academic level, and subjects do not affect identity.
     */
    public boolean isSameStudent(Student other) {
        return other != null && name.isSameNameIgnoringCase(other.name) && phone.equals(other.phone);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Student student)) {
            return false;
        }
        return name.equals(student.name)
                && academicLevel == student.academicLevel
                && subjects.equals(student.subjects)
                && phone.equals(student.phone)
                && guardianName.equals(student.guardianName)
                && guardianPhone.equals(student.guardianPhone)
                && homeworks.equals(student.homeworks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, academicLevel, subjects, phone, guardianName, guardianPhone, homeworks);
    }
}
