package seedu.address.testutil;

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

    /** Returns a student using the configured fields. */
    public Student build() {
        return new Student(new StudentName(name), AcademicLevel.parse(academicLevel), new TuitionSubjects(subjects),
                new StudentPhone(phone), new StudentName(guardianName), new StudentPhone(guardianPhone));
    }
}
