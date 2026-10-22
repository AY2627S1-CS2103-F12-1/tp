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
        assertThrows(NullPointerException.class, () -> new Homework(null, TuitionSubject.MATH, DUE_DATE, null));
    }

    @Test
    public void constructor_withoutScore_noScore() {
        Homework homework = new Homework(TITLE, TuitionSubject.MATH, DUE_DATE);

        assertEquals(TITLE, homework.getTitle());
        assertEquals(TuitionSubject.MATH, homework.getSubject());
        assertEquals(DUE_DATE, homework.getDueDate());
        assertEquals(Optional.empty(), homework.getScore());
    }

    @Test
    public void constructor_allFields_success() {
        Homework homework = new Homework(TITLE, TuitionSubject.MATH, DUE_DATE, new Score(9, 10));

        assertEquals(Optional.of(new Score(9, 10)), homework.getScore());
    }

    @Test
    public void isUpcoming() {
        // ALGEBRA is due on 2026-10-15

        // due the day after today -> returns true
        assertTrue(ALGEBRA.isUpcoming(LocalDate.of(2026, 10, 14)));

        // due today -> returns true
        assertTrue(ALGEBRA.isUpcoming(LocalDate.of(2026, 10, 15)));

        // due the day before today -> returns false
        assertFalse(ALGEBRA.isUpcoming(LocalDate.of(2026, 10, 16)));
    }

    @Test
    public void isOverdue() {
        assertFalse(ALGEBRA.isOverdue(LocalDate.of(2026, 10, 14)));
        assertFalse(ALGEBRA.isOverdue(LocalDate.of(2026, 10, 15)));
        assertTrue(ALGEBRA.isOverdue(LocalDate.of(2026, 10, 16)));

        Homework scoredHomework = new HomeworkBuilder(ALGEBRA).withScore("0/10").build();
        assertFalse(scoredHomework.isOverdue(LocalDate.of(2026, 10, 14)));
        assertFalse(scoredHomework.isOverdue(LocalDate.of(2026, 10, 15)));
        assertTrue(scoredHomework.isOverdue(LocalDate.of(2026, 10, 16)));

        Homework completedHomework = new HomeworkBuilder(ALGEBRA).withStatus("COMPLETED").build();
        assertFalse(completedHomework.isOverdue(LocalDate.of(2026, 10, 16)));
        assertFalse(new HomeworkBuilder(completedHomework).withScore("0/10").build()
                .isOverdue(LocalDate.of(2026, 10, 16)));
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

        // different score -> returns true
        assertTrue(ALGEBRA.isSameHomework(new HomeworkBuilder(ALGEBRA).withScore("7/10").build()));

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

        // different score -> returns false
        assertFalse(ALGEBRA.equals(new HomeworkBuilder(ALGEBRA).withScore("7/10").build()));
    }

    @Test
    public void toStringMethod() {
        String expected = Homework.class.getCanonicalName() + "{title=Revise atomic structure, subject=CHEMISTRY, "
                + "dueDate=2026-10-18, score=17/20, status=ASSIGNED}";
        assertEquals(expected, ATOMIC_STRUCTURE.toString());

        String expectedWithoutScore = Homework.class.getCanonicalName() + "{title=Complete algebra worksheet, "
                + "subject=MATH, dueDate=2026-10-15, score=null, status=ASSIGNED}";
        assertEquals(expectedWithoutScore, ALGEBRA.toString());
    }
}
