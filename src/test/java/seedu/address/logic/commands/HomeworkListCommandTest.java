package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestUtil.ARABIC_EGYPT;
import static seedu.address.testutil.TestUtil.runWithFormatLocale;
import static seedu.address.testutil.TypicalHomeworks.COMPLETED_MECHANICS;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.BENSON;
import static seedu.address.testutil.TypicalStudents.CARL;
import static seedu.address.testutil.TypicalStudents.getModelWithStudents;
import static seedu.address.testutil.TypicalStudents.getModelWithTypicalStudents;
import static seedu.address.testutil.TypicalStudents.getTypicalStudents;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.student.Student;
import seedu.address.testutil.HomeworkBuilder;
import seedu.address.testutil.StudentBuilder;

/**
 * Tests {@code HomeworkListCommand}. ALICE's homework is due on 2026-10-15 (1, no score), 2026-10-20 (2, no score)
 * and 2026-10-18 (3, score 17/20).
 */
public class HomeworkListCommandTest {
    private static final LocalDate BEFORE_ALL_DUE_DATES = LocalDate.of(2026, 10, 5);
    private static final LocalDate AFTER_ALL_DUE_DATES = LocalDate.of(2026, 10, 21);
    private static final Index FIRST = Index.fromOneBased(1);

    private final Model model = getModelWithTypicalStudents();
    private final Model expectedModel = getModelWithTypicalStudents();

    @Test
    public void constructor_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkListCommand(null, BEFORE_ALL_DUE_DATES, false));
        assertThrows(NullPointerException.class, () -> new HomeworkListCommand(FIRST, null, false));
    }

    @Test
    public void execute_allUpcoming_listsByDueDateWithInsertionIndices() {
        String expectedMessage = "Listed 3 upcoming homework records for Alice Tan.\n"
                + "Homework for Alice Tan:\n"
                + "1. Complete algebra worksheet (MATH) - due 2026-10-15 - score Not recorded - status ASSIGNED\n"
                + "3. Revise atomic structure (CHEMISTRY) - due 2026-10-18 - score 17/20 - status ASSIGNED\n"
                + "2. Attempt mechanics questions 1-5 (PHYSICS) - due 2026-10-20 - score Not recorded"
                + " - status ASSIGNED";

        assertCommandSuccess(new HomeworkListCommand(FIRST, BEFORE_ALL_DUE_DATES, false), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_someOverdue_listsHomeworkDueTodayOrLater() {
        // homework 1 is overdue, homework 3 is due today
        String expectedMessage = "Listed 2 upcoming homework records for Alice Tan.\n"
                + "Homework for Alice Tan:\n"
                + "3. Revise atomic structure (CHEMISTRY) - due 2026-10-18 - score 17/20 - status ASSIGNED\n"
                + "2. Attempt mechanics questions 1-5 (PHYSICS) - due 2026-10-20 - score Not recorded"
                + " - status ASSIGNED";

        assertCommandSuccess(new HomeworkListCommand(FIRST, LocalDate.of(2026, 10, 18), false), model,
                expectedMessage, expectedModel);
    }

    @Test
    public void execute_oneUpcoming_listsSingleRecord() {
        String expectedMessage = "Listed 1 upcoming homework record for Alice Tan.\n"
                + "Homework for Alice Tan:\n"
                + "2. Attempt mechanics questions 1-5 (PHYSICS) - due 2026-10-20 - score Not recorded"
                + " - status ASSIGNED";

        assertCommandSuccess(new HomeworkListCommand(FIRST, LocalDate.of(2026, 10, 19), false), model,
                expectedMessage, expectedModel);
    }

    @Test
    public void execute_allOverdue_showsNoUpcomingHomework() {
        assertCommandSuccess(new HomeworkListCommand(FIRST, AFTER_ALL_DUE_DATES, false), model,
                "No upcoming homework found for Alice Tan.", expectedModel);
    }

    @Test
    public void execute_allKeyword_listsOverdueHomeworkToo() {
        String expectedMessage = "Listed 3 homework records for Alice Tan.\n"
                + "Homework for Alice Tan:\n"
                + "1. Complete algebra worksheet (MATH) - due 2026-10-15 - score Not recorded - status ASSIGNED\n"
                + "3. Revise atomic structure (CHEMISTRY) - due 2026-10-18 - score 17/20 - status ASSIGNED\n"
                + "2. Attempt mechanics questions 1-5 (PHYSICS) - due 2026-10-20 - score Not recorded"
                + " - status ASSIGNED";

        assertCommandSuccess(new HomeworkListCommand(FIRST, AFTER_ALL_DUE_DATES, true), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_arabicDigitsLocale_listsAsciiIndices() {
        // Issue #151: under ar-EG, String.format prints Arabic-Indic digits, which the index parser rejects
        String expectedLines = "Homework for Alice Tan:\n"
                + "1. Complete algebra worksheet (MATH) - due 2026-10-15 - score Not recorded - status ASSIGNED\n"
                + "3. Revise atomic structure (CHEMISTRY) - due 2026-10-18 - score 17/20 - status ASSIGNED\n"
                + "2. Attempt mechanics questions 1-5 (PHYSICS) - due 2026-10-20 - score Not recorded"
                + " - status ASSIGNED";
        HomeworkListCommand command = new HomeworkListCommand(FIRST, AFTER_ALL_DUE_DATES, true);

        runWithFormatLocale(ARABIC_EGYPT, () -> {
            String feedback = assertDoesNotThrow(() -> command.execute(model)).getFeedbackToUser();
            assertTrue(feedback.endsWith(expectedLines), feedback);
        });
    }

    @Test
    public void execute_allKeywordOneRecord_listsSingleRecord() {
        String expectedMessage = "Listed 1 homework record for Carl Ng.\n"
                + "Homework for Carl Ng:\n"
                + "1. Attempt mechanics questions 1-5 (PHYSICS) - due 2026-10-20 - score Not recorded"
                + " - status ASSIGNED";

        assertCommandSuccess(new HomeworkListCommand(Index.fromOneBased(3), AFTER_ALL_DUE_DATES, true), model,
                expectedMessage, expectedModel);
    }

    @Test
    public void execute_sameDueDate_keepsInsertionOrder() {
        Student student = new StudentBuilder().withSubjects("MATH, PHYSICS").withHomeworks(
                new HomeworkBuilder().withTitle("Late A").withDueDate("2026-10-20").build(),
                new HomeworkBuilder().withTitle("Early").withDueDate("2026-10-15").withScore("5/10").build(),
                new HomeworkBuilder().withTitle("Late B").withSubject("PHYSICS").withDueDate("2026-10-20").build())
                .build();
        String expectedMessage = "Listed 3 upcoming homework records for John Tan.\n"
                + "Homework for John Tan:\n"
                + "2. Early (MATH) - due 2026-10-15 - score 5/10 - status ASSIGNED\n"
                + "1. Late A (MATH) - due 2026-10-20 - score Not recorded - status ASSIGNED\n"
                + "3. Late B (PHYSICS) - due 2026-10-20 - score Not recorded - status ASSIGNED";

        assertCommandSuccess(new HomeworkListCommand(FIRST, BEFORE_ALL_DUE_DATES, false),
                getModelWithStudents(List.of(student)), expectedMessage, getModelWithStudents(List.of(student)));
    }

    @Test
    public void execute_bareScore_showsScoreAsEntered() {
        Student student = new StudentBuilder().withHomeworks(
                new HomeworkBuilder().withTitle("Bare").withDueDate("2026-10-15").withScore("17").build(),
                new HomeworkBuilder().withTitle("Ratio").withDueDate("2026-10-16").withScore("17/20").build(),
                new HomeworkBuilder().withTitle("Zero").withDueDate("2026-10-17").withScore("0").build())
                .build();
        String expectedMessage = "Listed 3 upcoming homework records for John Tan.\n"
                + "Homework for John Tan:\n"
                + "1. Bare (MATH) - due 2026-10-15 - score 17 - status ASSIGNED\n"
                + "2. Ratio (MATH) - due 2026-10-16 - score 17/20 - status ASSIGNED\n"
                + "3. Zero (MATH) - due 2026-10-17 - score 0 - status ASSIGNED";

        assertCommandSuccess(new HomeworkListCommand(FIRST, BEFORE_ALL_DUE_DATES, false),
                getModelWithStudents(List.of(student)), expectedMessage, getModelWithStudents(List.of(student)));
    }

    @Test
    public void execute_studentWithoutHomework_showsNoHomeworkFound() {
        Index secondIndex = Index.fromOneBased(2);

        assertCommandSuccess(new HomeworkListCommand(secondIndex, BEFORE_ALL_DUE_DATES, false), model,
                "No homework found for Benson Lim.", expectedModel);
        assertCommandSuccess(new HomeworkListCommand(secondIndex, BEFORE_ALL_DUE_DATES, true), model,
                "No homework found for Benson Lim.", expectedModel);
    }

    @Test
    public void execute_studentIndexAboveListSize_failure() {
        assertCommandFailure(new HomeworkListCommand(Index.fromOneBased(4), BEFORE_ALL_DUE_DATES, false), model,
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_filteredList_listsDisplayedStudent() {
        // CARL is third in the roster but first in the displayed list
        model.updateFilteredStudentList(CARL::equals);
        String expectedMessage = "Listed 1 upcoming homework record for Carl Ng.\n"
                + "Homework for Carl Ng:\n"
                + "1. Attempt mechanics questions 1-5 (PHYSICS) - due 2026-10-20 - score Not recorded"
                + " - status ASSIGNED";

        assertCommandSuccess(new HomeworkListCommand(FIRST, BEFORE_ALL_DUE_DATES, false), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_indexBeyondFilteredList_failure() {
        model.updateFilteredStudentList(CARL::equals);

        assertCommandFailure(new HomeworkListCommand(Index.fromOneBased(2), BEFORE_ALL_DUE_DATES, false), model,
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_completedHomework_showsCompletedStatus() {
        Model completedModel = getModelWithStudents(List.of(ALICE, BENSON,
                CARL.withHomeworks(List.of(COMPLETED_MECHANICS))));
        Model expectedCompletedModel = getModelWithStudents(List.of(ALICE, BENSON,
                CARL.withHomeworks(List.of(COMPLETED_MECHANICS))));
        String expectedMessage = "Listed 1 upcoming homework record for Carl Ng.\n"
                + "Homework for Carl Ng:\n"
                + "1. Attempt mechanics questions 1-5 (PHYSICS) - due 2026-10-20 - score Not recorded"
                + " - status COMPLETED";

        assertCommandSuccess(new HomeworkListCommand(Index.fromOneBased(3), BEFORE_ALL_DUE_DATES, false),
                completedModel, expectedMessage, expectedCompletedModel);
    }

    @Test
    public void execute_emptyRoster_failure() {
        assertCommandFailure(new HomeworkListCommand(FIRST, BEFORE_ALL_DUE_DATES, true),
                getModelWithStudents(List.of()), Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_unexpectedError_reportsInternalErrorWithoutChangingModel() {
        Model failingModel = new ModelManager() {
            @Override
            public ObservableList<Student> getStudentList() {
                throw new IllegalStateException("dummy internal error");
            }
        };
        getTypicalStudents().forEach(failingModel::addStudent);

        assertThrows(CommandException.class, HomeworkListCommand.MESSAGE_INTERNAL_ERROR, ()
                -> new HomeworkListCommand(FIRST, BEFORE_ALL_DUE_DATES, false).execute(failingModel));
        // ModelManager#equals compares the stored rosters directly, not through getStudentList()
        assertEquals(expectedModel, failingModel);
    }

    @Test
    public void equals() {
        HomeworkListCommand listFirstCommand = new HomeworkListCommand(FIRST, BEFORE_ALL_DUE_DATES, false);

        // same object -> returns true
        assertTrue(listFirstCommand.equals(listFirstCommand));

        // same values -> returns true
        HomeworkListCommand listFirstCommandCopy = new HomeworkListCommand(Index.fromOneBased(1),
                LocalDate.of(2026, 10, 5), false);
        assertTrue(listFirstCommand.equals(listFirstCommandCopy));
        assertEquals(listFirstCommand.hashCode(), listFirstCommandCopy.hashCode());

        // null -> returns false
        assertFalse(listFirstCommand.equals(null));

        // different types -> returns false
        assertFalse(listFirstCommand.equals(1));

        // different student index -> returns false
        assertFalse(listFirstCommand.equals(new HomeworkListCommand(Index.fromOneBased(2), BEFORE_ALL_DUE_DATES,
                false)));

        // different date -> returns false
        assertFalse(listFirstCommand.equals(new HomeworkListCommand(FIRST, AFTER_ALL_DUE_DATES, false)));

        // all homework listed -> returns false
        assertFalse(listFirstCommand.equals(new HomeworkListCommand(FIRST, BEFORE_ALL_DUE_DATES, true)));
    }

    @Test
    public void toStringMethod() {
        String expected = HomeworkListCommand.class.getCanonicalName() + "{studentIndex=" + FIRST
                + ", today=2026-10-05, isAllShown=true}";
        assertEquals(expected, new HomeworkListCommand(FIRST, BEFORE_ALL_DUE_DATES, true).toString());
    }
}
