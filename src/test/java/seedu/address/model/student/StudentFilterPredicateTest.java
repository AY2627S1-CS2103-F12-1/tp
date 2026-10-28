package seedu.address.model.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.testutil.StudentBuilder;

public class StudentFilterPredicateTest {
    private final Student both = new StudentBuilder().withAcademicLevel("S4").withSubjects("PHYSICS,MATH").build();
    private final Student levelOnly = new StudentBuilder().withAcademicLevel("S4").withSubjects("CHEMISTRY").build();
    private final Student subjectOnly = new StudentBuilder().withAcademicLevel("J1").withSubjects("MATH").build();
    private final Student neither = new StudentBuilder().withAcademicLevel("J1").withSubjects("PHYSICS").build();

    @Test
    public void test_levelOnly_matchesEveryStudentAtThatLevel() {
        StudentFilterPredicate predicate = new StudentFilterPredicate(Optional.of(AcademicLevel.S4), Optional.empty());

        assertTrue(predicate.test(both));
        assertTrue(predicate.test(levelOnly));
        assertFalse(predicate.test(subjectOnly));
        assertFalse(predicate.test(neither));
    }

    @Test
    public void test_subjectOnly_matchesSubjectAmongMultipleSubjects() {
        StudentFilterPredicate predicate = new StudentFilterPredicate(
                Optional.empty(), Optional.of(TuitionSubject.MATH));

        assertTrue(predicate.test(both));
        assertFalse(predicate.test(levelOnly));
        assertTrue(predicate.test(subjectOnly));
        assertFalse(predicate.test(neither));
    }

    @Test
    public void test_combinedCriteria_requiresBothMatches() {
        StudentFilterPredicate predicate = new StudentFilterPredicate(
                Optional.of(AcademicLevel.S4), Optional.of(TuitionSubject.MATH));

        assertTrue(predicate.test(both));
        assertFalse(predicate.test(levelOnly));
        assertFalse(predicate.test(subjectOnly));
        assertFalse(predicate.test(neither));
    }

    @Test
    public void test_filteredSource_preservesOrderAndDuplicateEntries() {
        StudentFilterPredicate predicate = new StudentFilterPredicate(
                Optional.empty(), Optional.of(TuitionSubject.MATH));
        List<Student> source = List.of(subjectOnly, neither, both, subjectOnly);

        FilteredList<Student> results = new FilteredList<>(FXCollections.observableArrayList(source), predicate);

        assertEquals(List.of(subjectOnly, both, subjectOnly), results);
        assertEquals(List.of(subjectOnly, neither, both, subjectOnly), source);
    }

    @Test
    public void constructor_noCriteria_rejectsUnrestrictedFilter() {
        assertThrows(IllegalArgumentException.class, () ->
                new StudentFilterPredicate(Optional.empty(), Optional.empty()));
    }

    @Test
    public void equals_sameCriteria_hasSameValueAndHashCode() {
        StudentFilterPredicate predicate = new StudentFilterPredicate(
                Optional.of(AcademicLevel.S4), Optional.of(TuitionSubject.MATH));
        StudentFilterPredicate same = new StudentFilterPredicate(
                Optional.of(AcademicLevel.S4), Optional.of(TuitionSubject.MATH));

        assertEquals(predicate, same);
        assertEquals(predicate.hashCode(), same.hashCode());
        assertNotEquals(predicate, new StudentFilterPredicate(Optional.of(AcademicLevel.S4), Optional.empty()));
        assertNotEquals(predicate, new StudentFilterPredicate(Optional.empty(), Optional.of(TuitionSubject.MATH)));
        assertNotEquals(predicate, null);
    }

    @Test
    public void test_dayOnly_matchesAnyLessonOnEveryWeekday() {
        for (DayOfWeek day : DayOfWeek.values()) {
            RegularLesson lesson = new RegularLesson(TuitionSubject.MATH, day, LocalTime.of(9, 0), LocalTime.of(10, 0));
            Student student = new StudentBuilder().withLessons(lesson).build();
            StudentFilterPredicate predicate = new StudentFilterPredicate(
                    Optional.empty(), Optional.empty(), Optional.of(day));

            assertTrue(predicate.test(student));
            assertFalse(predicate.test(both));
            StudentFilterPredicate differentDay = new StudentFilterPredicate(
                    Optional.empty(), Optional.empty(), Optional.of(day.plus(1)));
            assertFalse(differentDay.test(student));
        }
    }

    @Test
    public void test_allCriteria_checksRegisteredSubjectIndependentlyOfLessonSubject() {
        RegularLesson mondayPhysics = new RegularLesson(TuitionSubject.PHYSICS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        RegularLesson tuesdayMath = new RegularLesson(TuitionSubject.MATH, DayOfWeek.TUESDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        Student student = new StudentBuilder().withAcademicLevel("S4").withSubjects("MATH,PHYSICS")
                .withLessons(tuesdayMath, mondayPhysics).build();
        StudentFilterPredicate predicate = new StudentFilterPredicate(
                Optional.of(AcademicLevel.S4), Optional.of(TuitionSubject.MATH), Optional.of(DayOfWeek.MONDAY));

        assertTrue(predicate.test(student));
        assertFalse(predicate.test(new StudentBuilder().withAcademicLevel("J1").withSubjects("MATH,PHYSICS")
                .withLessons(mondayPhysics).build()));
        assertFalse(predicate.test(new StudentBuilder().withAcademicLevel("S4").withSubjects("PHYSICS")
                .withLessons(mondayPhysics).build()));
        assertFalse(predicate.test(new StudentBuilder().withAcademicLevel("S4").withSubjects("MATH")
                .withLessons(tuesdayMath).build()));
    }

    @Test
    public void constructor_dayCriteria_rejectsAbsentOrNullCriteria() {
        assertThrows(IllegalArgumentException.class, () ->
                new StudentFilterPredicate(Optional.empty(), Optional.empty(), Optional.empty()));
        assertThrows(NullPointerException.class, () ->
                new StudentFilterPredicate(Optional.empty(), Optional.empty(), null));
        assertThrows(NullPointerException.class, () ->
                new StudentFilterPredicate(null, Optional.empty(), Optional.of(DayOfWeek.MONDAY)));
        assertThrows(NullPointerException.class, () ->
                new StudentFilterPredicate(Optional.empty(), null, Optional.of(DayOfWeek.MONDAY)));
    }

    @Test
    public void equals_dayCriteria_participatesInValueAndHashCode() {
        StudentFilterPredicate monday = new StudentFilterPredicate(
                Optional.empty(), Optional.empty(), Optional.of(DayOfWeek.MONDAY));
        StudentFilterPredicate same = new StudentFilterPredicate(
                Optional.empty(), Optional.empty(), Optional.of(DayOfWeek.MONDAY));
        StudentFilterPredicate tuesday = new StudentFilterPredicate(
                Optional.empty(), Optional.empty(), Optional.of(DayOfWeek.TUESDAY));

        assertTrue(monday.equals(monday));
        assertEquals(monday, same);
        assertEquals(monday.hashCode(), same.hashCode());
        assertNotEquals(monday, tuesday);
        assertEquals(Optional.of(DayOfWeek.MONDAY), monday.getDay());
    }

    @Test
    public void toString_dayCriteria_describesSelectedAndOmittedFilters() {
        StudentFilterPredicate combined = new StudentFilterPredicate(
                Optional.of(AcademicLevel.S4), Optional.of(TuitionSubject.PHYSICS), Optional.of(DayOfWeek.MONDAY));
        StudentFilterPredicate dayOnly = new StudentFilterPredicate(
                Optional.empty(), Optional.empty(), Optional.of(DayOfWeek.TUESDAY));

        String combinedDescription = combined.toString();
        assertTrue(combinedDescription.contains("academicLevel=Optional[S4]"));
        assertTrue(combinedDescription.contains("subject=Optional[PHYSICS]"));
        assertTrue(combinedDescription.contains("day=Optional[MONDAY]"));

        String dayOnlyDescription = dayOnly.toString();
        assertTrue(dayOnlyDescription.contains("academicLevel=Optional.empty"));
        assertTrue(dayOnlyDescription.contains("subject=Optional.empty"));
        assertTrue(dayOnlyDescription.contains("day=Optional[TUESDAY]"));
    }
}
