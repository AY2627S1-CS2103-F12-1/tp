package seedu.address.model.util;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.homework.DueDate;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.homework.Title;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentName;
import seedu.address.model.student.StudentPhone;
import seedu.address.model.student.TuitionSubject;
import seedu.address.model.student.TuitionSubjects;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods for populating {@code AddressBook} and the student roster with sample data.
 */
public class SampleDataUtil {
    /** Date passed to {@code DueDate#parse}; unused because every sample due date gives its year. */
    private static final LocalDate SAMPLE_TODAY = LocalDate.of(2026, 10, 1);

    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new Name("Alex Yeoh"), new Phone("87438807"), new Email("alexyeoh@example.com"),
                new Address("Blk 30 Geylang Street 29, #06-40"),
                getTagSet("friends")),
            new Person(new Name("Bernice Yu"), new Phone("99272758"), new Email("berniceyu@example.com"),
                new Address("Blk 30 Lorong 3 Serangoon Gardens, #07-18"),
                getTagSet("colleagues", "friends")),
            new Person(new Name("Charlotte Oliveiro"), new Phone("93210283"), new Email("charlotte@example.com"),
                new Address("Blk 11 Ang Mo Kio Street 74, #11-04"),
                getTagSet("neighbours")),
            new Person(new Name("David Li"), new Phone("91031282"), new Email("lidavid@example.com"),
                new Address("Blk 436 Serangoon Gardens Street 26, #16-43"),
                getTagSet("family")),
            new Person(new Name("Irfan Ibrahim"), new Phone("92492021"), new Email("irfan@example.com"),
                new Address("Blk 47 Tampines Street 20, #17-35"),
                getTagSet("classmates")),
            new Person(new Name("Roy Balakrishnan"), new Phone("92624417"), new Email("royb@example.com"),
                new Address("Blk 45 Aljunied Street 85, #11-31"),
                getTagSet("colleagues"))
        };
    }

    /**
     * Returns sample students in roster order. Each homework subject is one the student takes, and no student has
     * two homework records that are the same by {@link Homework#isSameHomework(Homework)}.
     */
    public static Student[] getSampleStudents() {
        return new Student[] {
            new Student(new StudentName("Ethan Lim"), AcademicLevel.S3, new TuitionSubjects("MATH, PHYSICS"),
                new StudentPhone("91234567"), new StudentName("Grace Lim"), new StudentPhone("98765432"),
                List.of(getHomework("Quadratic equations worksheet", TuitionSubject.MATH, "2026-10-23"),
                    getHomework("Kinematics practice set", TuitionSubject.PHYSICS, "2026-10-28"))),
            new Student(new StudentName("Priya Nair"), AcademicLevel.J1, new TuitionSubjects("MATH, CHEMISTRY"),
                new StudentPhone("82345671"), new StudentName("Ravi Nair"), new StudentPhone("96543218"),
                List.of(getHomework("Integration by parts exercise", TuitionSubject.MATH, "2026-10-26",
                        HomeworkStatus.COMPLETED),
                    getHomework("Organic reaction pathways map", TuitionSubject.CHEMISTRY, "2026-11-02"),
                    getHomework("Chemical equilibrium past paper", TuitionSubject.CHEMISTRY, "2026-11-09"))),
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

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Person samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }

    /**
     * Returns a tag set containing the list of strings given.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toSet());
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
        return new Homework(new Title(title), subject, DueDate.parse(dueDate, SAMPLE_TODAY), status, null);
    }

}
