package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalRegularLessons.MONDAY_MATH;
import static seedu.address.testutil.TypicalRegularLessons.TUESDAY_PHYSICS;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.student.Student;
import seedu.address.testutil.StudentBuilder;

public class LessonScheduleTest {
    @Test
    public void from_students_returnsLessonsInWeeklyOrder() {
        Student firstStudent = new StudentBuilder().withName("Zoe Tan")
                .withSubjects("MATH, PHYSICS").withLessons(TUESDAY_PHYSICS, MONDAY_MATH).build();
        Student secondStudent = new StudentBuilder().withName("Amy Lim")
                .withSubjects("PHYSICS").withLessons(TUESDAY_PHYSICS).build();

        List<LessonSchedule.Entry> entries = LessonSchedule.from(List.of(firstStudent, secondStudent));

        assertEquals(List.of(
                new LessonSchedule.Entry(firstStudent, MONDAY_MATH),
                new LessonSchedule.Entry(secondStudent, TUESDAY_PHYSICS),
                new LessonSchedule.Entry(firstStudent, TUESDAY_PHYSICS)), entries);
    }

    @Test
    public void entry_nullField_throwsNullPointerException() {
        Student student = new StudentBuilder().build();
        assertThrows(NullPointerException.class, () -> new LessonSchedule.Entry(null, MONDAY_MATH));
        assertThrows(NullPointerException.class, () -> new LessonSchedule.Entry(student, null));
    }
}
