package seedu.address.testutil;

import java.util.List;

import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentName;
import seedu.address.model.student.StudentPhone;
import seedu.address.model.student.TuitionSubjects;

/** Builds a valid student for tests, allowing individual fields to be changed. */
public class StudentBuilder {
    private String name = "John Tan";
    private String academicLevel = "S3";
    private String subjects = "MATH";
    private String phone = "91234567";
    private String guardianName = "Mary Tan";
    private String guardianPhone = "98765432";
    private List<Homework> homeworks = List.of();
    private List<RegularLesson> lessons = List.of();

    /** Returns this builder with a different student name. */
    public StudentBuilder withName(String name) {
        this.name = name;
        return this;
    }

    /** Returns this builder with a different student phone. */
    public StudentBuilder withPhone(String phone) {
        this.phone = phone;
        return this;
    }

    /** Returns this builder with different tuition subjects. */
    public StudentBuilder withSubjects(String subjects) {
        this.subjects = subjects;
        return this;
    }

    /** Returns this builder with a different guardian name. */
    public StudentBuilder withGuardianName(String guardianName) {
        this.guardianName = guardianName;
        return this;
    }

    /** Returns this builder with the given homework, in order. */
    public StudentBuilder withHomeworks(Homework... homeworks) {
        this.homeworks = List.of(homeworks);
        return this;
    }

    /** Returns this builder with the given regular lessons, in order. */
    public StudentBuilder withLessons(RegularLesson... lessons) {
        this.lessons = List.of(lessons);
        return this;
    }

    /** Returns a student using the configured fields. */
    public Student build() {
        return new Student(new StudentName(name), AcademicLevel.parse(academicLevel), new TuitionSubjects(subjects),
                new StudentPhone(phone), new StudentName(guardianName), new StudentPhone(guardianPhone), homeworks,
                lessons);
    }
}
