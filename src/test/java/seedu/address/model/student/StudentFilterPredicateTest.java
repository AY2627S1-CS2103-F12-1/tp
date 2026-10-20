package seedu.address.model.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
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
}
