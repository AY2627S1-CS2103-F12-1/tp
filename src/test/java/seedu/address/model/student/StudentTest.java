package seedu.address.model.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalHomeworks.ATOMIC_STRUCTURE;
import static seedu.address.testutil.TypicalHomeworks.MECHANICS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.homework.Homework;
import seedu.address.testutil.HomeworkBuilder;
import seedu.address.testutil.StudentBuilder;

public class StudentTest {
    @Test
    public void constructor_withoutHomework_hasEmptyHomeworkList() {
        Student student = new Student(new StudentName("John Tan"), AcademicLevel.S3, new TuitionSubjects("MATH"),
                new StudentPhone("91234567"), new StudentName("Mary Tan"), new StudentPhone("98765432"));

        assertEquals(List.of(), student.getHomeworks());
    }

    @Test
    public void constructor_nullHomework_throwsNullPointerException() {
        StudentName name = new StudentName("John Tan");
        TuitionSubjects subjects = new TuitionSubjects("MATH");
        StudentPhone phone = new StudentPhone("91234567");
        StudentName guardianName = new StudentName("Mary Tan");
        StudentPhone guardianPhone = new StudentPhone("98765432");

        // null list
        assertThrows(NullPointerException.class, () -> new Student(name, AcademicLevel.S3, subjects, phone,
                guardianName, guardianPhone, null));
        // list containing null
        assertThrows(NullPointerException.class, () -> new Student(name, AcademicLevel.S3, subjects, phone,
                guardianName, guardianPhone, Arrays.asList(ALGEBRA, null)));
    }

    @Test
    public void withHomeworks_nullHomework_throwsNullPointerException() {
        Student student = new StudentBuilder().build();

        // null list
        assertThrows(NullPointerException.class, () -> student.withHomeworks(null));
        // list containing null
        assertThrows(NullPointerException.class, () -> student.withHomeworks(Arrays.asList(ALGEBRA, null)));
    }

    @Test
    public void constructor_sourceListChangedLater_homeworkUnchanged() {
        List<Homework> homeworks = new ArrayList<>(List.of(ALGEBRA));
        Student student = new StudentBuilder().build().withHomeworks(homeworks);

        homeworks.add(MECHANICS);

        assertEquals(List.of(ALGEBRA), student.getHomeworks());
    }

    @Test
    public void getHomeworks_insertionOrderAndUnmodifiable() {
        Student student = new StudentBuilder().withHomeworks(MECHANICS, ALGEBRA).build();

        assertEquals(List.of(MECHANICS, ALGEBRA), student.getHomeworks());
        assertThrows(UnsupportedOperationException.class, () -> student.getHomeworks().add(ATOMIC_STRUCTURE));
    }

    @Test
    public void getAssignedHomeworkCount() {
        // no homework
        assertEquals(0, new StudentBuilder().build().getAssignedHomeworkCount());

        // completed homework only
        assertEquals(0, new StudentBuilder().withHomeworks(ATOMIC_STRUCTURE).build().getAssignedHomeworkCount());

        // mixed statuses -> counts assigned only
        assertEquals(2, new StudentBuilder().withHomeworks(ALGEBRA, ATOMIC_STRUCTURE, MECHANICS).build()
                .getAssignedHomeworkCount());
    }

    @Test
    public void hasHomework() {
        Student student = new StudentBuilder().withHomeworks(ALGEBRA).build();

        // no homework -> returns false
        assertFalse(new StudentBuilder().build().hasHomework(ALGEBRA));

        // same identity with different title case and status -> returns true
        assertTrue(student.hasHomework(new HomeworkBuilder(ALGEBRA).withTitle("COMPLETE ALGEBRA WORKSHEET")
                .withScore(70).build()));

        // different homework -> returns false
        assertFalse(student.hasHomework(MECHANICS));
    }

    @Test
    public void withHomeworks_returnsNewStudentWithSameProfile() {
        Student original = new StudentBuilder().withName("Jane Lim").withSubjects("MATH, PHYSICS").build();

        Student updated = original.withHomeworks(List.of(ALGEBRA, MECHANICS));

        assertEquals(new StudentBuilder().withName("Jane Lim").withSubjects("MATH, PHYSICS")
                .withHomeworks(ALGEBRA, MECHANICS).build(), updated);
        assertTrue(updated.isSameStudent(original));
        assertEquals(List.of(), original.getHomeworks());
    }

    @Test
    public void equals_homeworkCompared() {
        Student student = new StudentBuilder().withHomeworks(ALGEBRA, MECHANICS).build();

        // same homework -> returns true
        Student copy = new StudentBuilder().withHomeworks(ALGEBRA, MECHANICS).build();
        assertTrue(student.equals(copy));
        assertEquals(student.hashCode(), copy.hashCode());

        // different homework -> returns false
        assertFalse(student.equals(new StudentBuilder().withHomeworks(ALGEBRA).build()));

        // same homework in different order -> returns false
        assertFalse(student.equals(new StudentBuilder().withHomeworks(MECHANICS, ALGEBRA).build()));
    }
}
