package seedu.address.model.homework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalHomeworks.ATOMIC_STRUCTURE;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.student.TuitionSubject;
import seedu.address.testutil.HomeworkBuilder;

public class HomeworkTest {
    private static final Title TITLE = new Title("Complete algebra worksheet");
    private static final DueDate DUE_DATE = DueDate.parse("2026-10-15", LocalDate.of(2026, 10, 5));

    @Test
    public void constructor_nullField_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Homework(null, TuitionSubject.MATH, DUE_DATE));
        assertThrows(NullPointerException.class, () -> new Homework(TITLE, null, DUE_DATE));
        assertThrows(NullPointerException.class, () -> new Homework(TITLE, TuitionSubject.MATH, null));
        assertThrows(NullPointerException.class, () ->
                new Homework(TITLE, TuitionSubject.MATH, DUE_DATE, null, null));
    }

    @Test
    public void constructor_withoutStatusAndScore_assignedWithNoScore() {
        Homework homework = new Homework(TITLE, TuitionSubject.MATH, DUE_DATE);

        assertEquals(TITLE, homework.getTitle());
        assertEquals(TuitionSubject.MATH, homework.getSubject());
        assertEquals(DUE_DATE, homework.getDueDate());
        assertEquals(HomeworkStatus.ASSIGNED, homework.getStatus());
        assertEquals(Optional.empty(), homework.getScore());
    }

    @Test
    public void constructor_allFields_success() {
        Homework homework = new Homework(TITLE, TuitionSubject.MATH, DUE_DATE, HomeworkStatus.COMPLETED,
                new Score(90));

        assertEquals(HomeworkStatus.COMPLETED, homework.getStatus());
        assertEquals(Optional.of(new Score(90)), homework.getScore());
    }

    @Test
    public void isAssigned() {
        assertTrue(ALGEBRA.isAssigned());
        assertFalse(ATOMIC_STRUCTURE.isAssigned());
    }

    @Test
    public void isSameHomework() {
        // same object -> returns true
        assertTrue(ALGEBRA.isSameHomework(ALGEBRA));

        // null -> returns false
        assertFalse(ALGEBRA.isSameHomework(null));

        // title differs only in case and spacing -> returns true
        assertTrue(ALGEBRA.isSameHomework(new HomeworkBuilder(ALGEBRA)
                .withTitle(" COMPLETE  algebra Worksheet ").build()));

        // different status and score -> returns true
        assertTrue(ALGEBRA.isSameHomework(new HomeworkBuilder(ALGEBRA).withStatus(HomeworkStatus.COMPLETED)
                .withScore(70).build()));

        // different title -> returns false
        assertFalse(ALGEBRA.isSameHomework(new HomeworkBuilder(ALGEBRA).withTitle("Revise algebra").build()));

        // different subject -> returns false
        assertFalse(ALGEBRA.isSameHomework(new HomeworkBuilder(ALGEBRA).withSubject("PHYSICS").build()));

        // different due date -> returns false
        assertFalse(ALGEBRA.isSameHomework(new HomeworkBuilder(ALGEBRA).withDueDate("2026-10-16").build()));
    }

    @Test
    public void equals() {
        // same object -> returns true
        assertTrue(ALGEBRA.equals(ALGEBRA));

        // same values -> returns true
        Homework algebraCopy = new HomeworkBuilder(ALGEBRA).build();
        assertTrue(ALGEBRA.equals(algebraCopy));
        assertEquals(ALGEBRA.hashCode(), algebraCopy.hashCode());

        // null -> returns false
        assertFalse(ALGEBRA.equals(null));

        // different type -> returns false
        assertFalse(ALGEBRA.equals(5));

        // different title case -> returns false
        assertFalse(ALGEBRA.equals(new HomeworkBuilder(ALGEBRA).withTitle("complete algebra worksheet").build()));

        // different subject -> returns false
        assertFalse(ALGEBRA.equals(new HomeworkBuilder(ALGEBRA).withSubject("PHYSICS").build()));

        // different due date -> returns false
        assertFalse(ALGEBRA.equals(new HomeworkBuilder(ALGEBRA).withDueDate("2026-10-16").build()));

        // different status -> returns false
        assertFalse(ALGEBRA.equals(new HomeworkBuilder(ALGEBRA).withStatus(HomeworkStatus.COMPLETED).build()));

        // different score -> returns false
        assertFalse(ALGEBRA.equals(new HomeworkBuilder(ALGEBRA).withScore(70).build()));
    }

    @Test
    public void toStringMethod() {
        String expected = Homework.class.getCanonicalName() + "{title=Revise atomic structure, subject=CHEMISTRY, "
                + "dueDate=2026-10-18, status=COMPLETED, score=85}";
        assertEquals(expected, ATOMIC_STRUCTURE.toString());

        String expectedWithoutScore = Homework.class.getCanonicalName() + "{title=Complete algebra worksheet, "
                + "subject=MATH, dueDate=2026-10-15, status=ASSIGNED, score=null}";
        assertEquals(expectedWithoutScore, ALGEBRA.toString());
    }
}
