package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalRegularLessons.MONDAY_MATH;
import static seedu.address.testutil.TypicalRegularLessons.TUESDAY_PHYSICS;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.student.Student;
import seedu.address.model.student.TuitionSubject;
import seedu.address.testutil.StudentBuilder;

public class LessonScheduleTest {
    @Test
    public void findClashes_excludesOnlySelectedEntryAndAllowsAdjacentAndOtherDays() {
        RegularLesson early = new RegularLesson(TuitionSubject.MATH, DayOfWeek.MONDAY,
                LocalTime.of(14, 0), LocalTime.of(16, 0));
        Student first = new StudentBuilder().withSubjects("MATH, PHYSICS")
                .withLessons(MONDAY_MATH, TUESDAY_PHYSICS, early).build();
        Student second = new StudentBuilder().withName("Amy Lim").withPhone("92345678")
                .withLessons(new RegularLesson(TuitionSubject.MATH, DayOfWeek.MONDAY,
                        LocalTime.of(19, 0), LocalTime.of(20, 0))).build();
        List<Student> students = List.of(first, second);
        RegularLesson candidate = new RegularLesson(TuitionSubject.MATH, DayOfWeek.MONDAY,
                LocalTime.of(16, 0), LocalTime.of(19, 30));
        LessonSchedule.Entry original = new LessonSchedule.Entry(first, MONDAY_MATH);
        LessonSchedule.Entry clash = new LessonSchedule.Entry(second, second.getLessons().getFirst());
        assertEquals(List.of(original, clash), LessonSchedule.findClashes(students, candidate));
        assertEquals(List.of(clash), LessonSchedule.findClashes(students, candidate, original));
        assertEquals(List.of(), LessonSchedule.findClashes(students, MONDAY_MATH, original));
        assertThrows(UnsupportedOperationException.class, () ->
                LessonSchedule.findClashes(students, candidate).clear());
    }

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
