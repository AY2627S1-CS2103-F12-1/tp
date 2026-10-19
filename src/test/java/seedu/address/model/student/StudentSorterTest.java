package seedu.address.model.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.lesson.RegularLesson;
import seedu.address.testutil.StudentBuilder;

public class StudentSorterTest {
    @Test
    public void sort_levels_usesSchoolOrderInBothDirections() {
        Student s1 = studentAt("S1");
        Student s2 = studentAt("S2");
        Student s3 = studentAt("S3");
        Student s4 = studentAt("S4");
        Student s5 = studentAt("S5");
        Student j1 = studentAt("J1");
        Student j2 = studentAt("J2");
        List<Student> input = List.of(j1, s2, s1, j2, s5, s4, s3);

        assertEquals(List.of(s1, s2, s3, s4, s5, j1, j2),
                StudentSorter.sort(input, SortKey.LEVEL, SortOrder.ASCENDING));
        assertEquals(List.of(j2, j1, s5, s4, s3, s2, s1),
                StudentSorter.sort(input, SortKey.LEVEL, SortOrder.DESCENDING));
        assertEquals(List.of(j1, s2, s1, j2, s5, s4, s3), input);
    }

    @Test
    public void sort_subjects_usesAlphabeticalMinimumInBothDirections() {
        Student math = new StudentBuilder().withSubjects("PHYSICS, MATH").build();
        Student chemistry = new StudentBuilder().withSubjects("CHEMISTRY, PHYSICS").build();
        Student physics = new StudentBuilder().withSubjects("PHYSICS").build();
        List<Student> input = List.of(physics, math, chemistry);

        assertEquals(List.of(chemistry, math, physics),
                StudentSorter.sort(input, SortKey.SUBJECT, SortOrder.ASCENDING));
        assertEquals(List.of(physics, math, chemistry),
                StudentSorter.sort(input, SortKey.SUBJECT, SortOrder.DESCENDING));
    }

    @Test
    public void sort_days_ignoresTimeAndKeepsMissingDaysLast() {
        Student tuesday = new StudentBuilder().withLessons(lesson(DayOfWeek.FRIDAY, 9),
                lesson(DayOfWeek.TUESDAY, 17)).build();
        Student thursday = new StudentBuilder().withLessons(lesson(DayOfWeek.THURSDAY, 8)).build();
        Student missing = new StudentBuilder().withName("Missing One").build();
        Student alsoMissing = new StudentBuilder().withName("Missing Two").build();
        List<Student> input = List.of(missing, thursday, tuesday, alsoMissing);

        assertEquals(List.of(tuesday, thursday, missing, alsoMissing),
                StudentSorter.sort(input, SortKey.DAY, SortOrder.ASCENDING));
        assertEquals(List.of(thursday, tuesday, missing, alsoMissing),
                StudentSorter.sort(input, SortKey.DAY, SortOrder.DESCENDING));
    }

    @Test
    public void sort_repeatedSort_preservesImmediatelyPreviousTieOrder() {
        Student math = new StudentBuilder().withSubjects("MATH").build();
        Student chemistry = new StudentBuilder().withSubjects("CHEMISTRY").build();
        List<Student> bySubject = StudentSorter.sort(List.of(math, chemistry), SortKey.SUBJECT,
                SortOrder.ASCENDING);

        assertEquals(List.of(chemistry, math),
                StudentSorter.sort(bySubject, SortKey.LEVEL, SortOrder.DESCENDING));
    }

    @Test
    public void sort_days_includesWeekBoundariesAndIgnoresMeetingTimesForTies() {
        Student sunday = new StudentBuilder().withLessons(lesson(DayOfWeek.SUNDAY, 8)).build();
        Student mondayLate = new StudentBuilder().withLessons(lesson(DayOfWeek.MONDAY, 17)).build();
        Student mondayEarly = new StudentBuilder().withLessons(lesson(DayOfWeek.MONDAY, 8)).build();
        List<Student> input = List.of(mondayLate, sunday, mondayEarly);

        assertEquals(List.of(mondayLate, mondayEarly, sunday),
                StudentSorter.sort(input, SortKey.DAY, SortOrder.ASCENDING));
        assertEquals(List.of(sunday, mondayLate, mondayEarly),
                StudentSorter.sort(input, SortKey.DAY, SortOrder.DESCENDING));
    }

    @Test
    public void sort_duplicateOccurrences_preservesReferencesAndMultiplicity() {
        Student first = new StudentBuilder().build();
        Student equal = new StudentBuilder().build();
        List<Student> result = StudentSorter.sort(List.of(first, equal, first), SortKey.DAY,
                SortOrder.DESCENDING);

        assertEquals(3, result.size());
        assertSame(first, result.get(0));
        assertSame(equal, result.get(1));
        assertSame(first, result.get(2));
    }

    @Test
    public void sort_emptyAndSingleStudent_returnsUnchangedContents() {
        Student student = new StudentBuilder().build();
        for (SortKey key : SortKey.values()) {
            for (SortOrder order : SortOrder.values()) {
                assertEquals(List.of(), StudentSorter.sort(List.of(), key, order));
                assertEquals(List.of(student), StudentSorter.sort(List.of(student), key, order));
            }
        }
    }

    private static Student studentAt(String level) {
        return new StudentBuilder().withAcademicLevel(level).build();
    }

    private static RegularLesson lesson(DayOfWeek day, int startHour) {
        return new RegularLesson(TuitionSubject.MATH, day, LocalTime.of(startHour, 0),
                LocalTime.of(startHour + 1, 0));
    }
}
