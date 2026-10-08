package seedu.address.model.student;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Objects;

import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.RegularLesson;

/**
 * Represents a student profile with six required fields, homework and regular weekly lessons.
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
    private final List<RegularLesson> lessons;

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
        this(name, academicLevel, subjects, phone, guardianName, guardianPhone, homeworks, List.of());
    }

    /** Creates a student with homework and weekly lessons in insertion order. */
    public Student(StudentName name, AcademicLevel academicLevel, TuitionSubjects subjects, StudentPhone phone,
            StudentName guardianName, StudentPhone guardianPhone, List<Homework> homeworks,
            List<RegularLesson> lessons) {
        requireAllNonNull(name, academicLevel, subjects, phone, guardianName, guardianPhone, homeworks);
        this.name = name;
        this.academicLevel = academicLevel;
        this.subjects = subjects;
        this.phone = phone;
        this.guardianName = guardianName;
        this.guardianPhone = guardianPhone;
        this.homeworks = List.copyOf(homeworks);
        this.lessons = List.copyOf(lessons);
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
        return new Student(name, academicLevel, subjects, phone, guardianName, guardianPhone, homeworks, lessons);
    }

    /** Returns this student's unmodifiable weekly lesson list. */
    public List<RegularLesson> getLessons() {
        return lessons;
    }

    /** Returns a copy with the given lessons, preserving all profile fields and homework. */
    public Student withLessons(List<RegularLesson> lessons) {
        return new Student(name, academicLevel, subjects, phone, guardianName, guardianPhone, homeworks, lessons);
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
                && homeworks.equals(student.homeworks)
                && lessons.equals(student.lessons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, academicLevel, subjects, phone, guardianName, guardianPhone, homeworks, lessons);
    }
}
