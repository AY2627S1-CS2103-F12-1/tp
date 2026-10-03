package seedu.address.model.student;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

/**
 * Represents a student profile with the six fields required by Add Student.
 * Guarantees: immutable; all fields are present and validated by their value types.
 */
public final class Student {
    private final StudentName name;
    private final AcademicLevel academicLevel;
    private final TuitionSubjects subjects;
    private final StudentPhone phone;
    private final StudentName guardianName;
    private final StudentPhone guardianPhone;

    /**
     * Creates a student with all six required Add Student fields.
     *
     * @throws NullPointerException If any field is null.
     */
    public Student(StudentName name, AcademicLevel academicLevel, TuitionSubjects subjects, StudentPhone phone,
            StudentName guardianName, StudentPhone guardianPhone) {
        requireAllNonNull(name, academicLevel, subjects, phone, guardianName, guardianPhone);
        this.name = name;
        this.academicLevel = academicLevel;
        this.subjects = subjects;
        this.phone = phone;
        this.guardianName = guardianName;
        this.guardianPhone = guardianPhone;
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
                && guardianPhone.equals(student.guardianPhone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, academicLevel, subjects, phone, guardianName, guardianPhone);
    }
}
