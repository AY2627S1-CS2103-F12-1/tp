package seedu.address.model.util;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.homework.DueDate;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.homework.Title;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentName;
import seedu.address.model.student.StudentPhone;
import seedu.address.model.student.TuitionSubject;
import seedu.address.model.student.TuitionSubjects;

/**
 * Contains utility methods for populating {@code AddressBook} with sample students, homework and regular lessons.
 */
public class SampleDataUtil {
    /**
     * Returns sample students in roster order. Each homework subject is one the student takes, and no student has
     * two homework records that are the same by {@link Homework#isSameHomework(Homework)}.
     */
    public static Student[] getSampleStudents() {
        return new Student[] {
            new Student(new StudentName("Ethan Lim"), AcademicLevel.S3, new TuitionSubjects("MATH, PHYSICS"),
                new StudentPhone("91234567"), new StudentName("Grace Lim"), new StudentPhone("98765432"),
                List.of(getHomework("Quadratic equations worksheet", TuitionSubject.MATH, "2026-10-23"),
                    getHomework("Kinematics practice set", TuitionSubject.PHYSICS, "2026-10-28")),
                List.of(getRegularLesson(TuitionSubject.MATH, DayOfWeek.MONDAY, "17:00", "18:30"))),
            new Student(new StudentName("Priya Nair"), AcademicLevel.J1, new TuitionSubjects("MATH, CHEMISTRY"),
                new StudentPhone("82345671"), new StudentName("Ravi Nair"), new StudentPhone("96543218"),
                List.of(getHomework("Integration by parts exercise", TuitionSubject.MATH, "2026-10-26",
                        HomeworkStatus.COMPLETED),
                    getHomework("Organic reaction pathways map", TuitionSubject.CHEMISTRY, "2026-11-02"),
                    getHomework("Chemical equilibrium past paper", TuitionSubject.CHEMISTRY, "2026-11-09")),
                List.of(getRegularLesson(TuitionSubject.CHEMISTRY, DayOfWeek.SATURDAY, "15:00", "16:30"))),
            new Student(new StudentName("Marcus Tan"), AcademicLevel.S1, new TuitionSubjects("MATH"),
                new StudentPhone("87654329"), new StudentName("Tan Mei Ling"), new StudentPhone("91827364"),
                List.of(getHomework("Algebraic expansion worksheet", TuitionSubject.MATH, "2026-10-30"))),
            new Student(new StudentName("Nurul Aisyah"), AcademicLevel.S4, new TuitionSubjects("PHYSICS, CHEMISTRY"),
                new StudentPhone("93456782"), new StudentName("Rahman bin Ismail"), new StudentPhone("81234569"),
                List.of(getHomework("Electromagnetism practice questions", TuitionSubject.PHYSICS, "2026-11-04"),
                    getHomework("Mole concept worksheet", TuitionSubject.CHEMISTRY, "2026-11-11"))),
            new Student(new StudentName("Daniel Wong"), AcademicLevel.J2,
                new TuitionSubjects("MATH, PHYSICS, CHEMISTRY"), new StudentPhone("96781234"),
                new StudentName("Wong Siew Lan"), new StudentPhone("62345678")),
            new Student(new StudentName("Chloe Chua"), AcademicLevel.S2, new TuitionSubjects("MATH"),
                new StudentPhone("88812345"), new StudentName("Chua Boon Huat"), new StudentPhone("97771234"),
                List.of(getHomework("Linear graphs practice", TuitionSubject.MATH, "2026-11-16")))
        };
    }

    /**
     * Returns an address book holding the sample students in roster order.
     */
    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Student sampleStudent : getSampleStudents()) {
            sampleAb.addStudent(sampleStudent);
        }
        return sampleAb;
    }

    /**
     * Returns assigned homework without a score, due on {@code dueDate} given in {@code YYYY-MM-DD} format.
     */
    private static Homework getHomework(String title, TuitionSubject subject, String dueDate) {
        return getHomework(title, subject, dueDate, HomeworkStatus.ASSIGNED);
    }

    /**
     * Returns homework with {@code status} and without a score, due on {@code dueDate} given in {@code YYYY-MM-DD}
     * format.
     */
    private static Homework getHomework(String title, TuitionSubject subject, String dueDate,
            HomeworkStatus status) {
        return new Homework(new Title(title), subject, DueDate.parseFullDate(dueDate), status, null);
    }

    /**
     * Returns a regular lesson whose start and end times use {@code HH:mm} format.
     */
    private static RegularLesson getRegularLesson(TuitionSubject subject, DayOfWeek dayOfWeek, String startTime,
            String endTime) {
        return new RegularLesson(subject, dayOfWeek, LocalTime.parse(startTime), LocalTime.parse(endTime));
    }

}
