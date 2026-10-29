package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.CAUSE_READ_ONLY_FOLDER;
import static seedu.address.logic.commands.CommandTestUtil.SHOWN_DATA_FILE_NAME;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestUtil.ARABIC_EGYPT;
import static seedu.address.testutil.TestUtil.runWithFormatLocale;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalHomeworks.ATOMIC_STRUCTURE;
import static seedu.address.testutil.TypicalHomeworks.MECHANICS;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.BENSON;
import static seedu.address.testutil.TypicalStudents.CARL;
import static seedu.address.testutil.TypicalStudents.getModelFailingOnSetStudent;
import static seedu.address.testutil.TypicalStudents.getModelWithStudents;
import static seedu.address.testutil.TypicalStudents.getModelWithTypicalStudents;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.testutil.HomeworkBuilder;
import seedu.address.testutil.StudentBuilder;

public class HomeworkAddCommandTest {
    /** A day before the due dates of the typical homework, so adding it shows no note about past due dates. */
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 5);

    private final Model model = getModelWithTypicalStudents();

    @Test
    public void constructor_nullStudentIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkAddCommand(null, ALGEBRA, false, TODAY));
    }

    @Test
    public void constructor_nullHomework_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkAddCommand(Index.fromOneBased(1), null, false,
                TODAY));
    }

    @Test
    public void constructor_nullToday_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, false,
                null));
    }

    @Test
    public void execute_studentWithoutHomework_addsHomework() {
        // ALICE already has ALGEBRA; the same homework may be assigned to another student
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(2), ALGEBRA, false, TODAY);
        String expectedMessage = "New homework added for Benson Lim (92222222):\n"
                + "1. Complete algebra worksheet (MATH) - due 2026-10-15 - score Not recorded - status ASSIGNED";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON.withHomeworks(List.of(ALGEBRA)), CARL));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_studentWithHomework_appendsHomeworkAtEnd() {
        Homework trigonometry = new HomeworkBuilder().withTitle("Trigonometry practice").withDueDate("2026-11-01")
                .build();
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(1), trigonometry, false, TODAY);
        String expectedMessage = "New homework added for Alice Tan (91111111):\n"
                + "4. Trigonometry practice (MATH) - due 2026-11-01 - score Not recorded - status ASSIGNED";
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(ALGEBRA, MECHANICS, ATOMIC_STRUCTURE, trigonometry)), BENSON, CARL));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_lastStudentIndex_addsHomework() {
        Homework optics = new HomeworkBuilder().withTitle("Optics worksheet").withSubject("PHYSICS").build();
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(3), optics, false, TODAY);
        String expectedMessage = "New homework added for Carl Ng (93333333):\n"
                + "2. Optics worksheet (PHYSICS) - due 2026-10-15 - score Not recorded - status ASSIGNED";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON,
                CARL.withHomeworks(List.of(MECHANICS, optics))));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_yearInferred_showsInferredYear() {
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(2), ALGEBRA, true, TODAY);
        String expectedMessage = "New homework added for Benson Lim (92222222):\n"
                + "1. Complete algebra worksheet (MATH) - due 2026-10-15 - score Not recorded - status ASSIGNED\n"
                + "Inferred year: 2026";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON.withHomeworks(List.of(ALGEBRA)), CARL));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_yearInferredArabicDigitsLocale_showsAsciiDigits() {
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(2), ALGEBRA, true, TODAY);

        runWithFormatLocale(ARABIC_EGYPT, () -> {
            String feedback = assertDoesNotThrow(() -> command.execute(model)).getFeedbackToUser();
            assertEquals("New homework added for Benson Lim (92222222):\n"
                    + "1. Complete algebra worksheet (MATH) - due 2026-10-15 - score Not recorded - status ASSIGNED\n"
                    + "Inferred year: 2026", feedback);
        });
    }

    @Test
    public void execute_dueBeforeToday_addsHomeworkWithNote() {
        // ALGEBRA is due 2026-10-15, the day before today
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(2), ALGEBRA, false,
                LocalDate.of(2026, 10, 16));
        String expectedMessage = "New homework added for Benson Lim (92222222):\n"
                + "1. Complete algebra worksheet (MATH) - due 2026-10-15 - score Not recorded - status ASSIGNED\n"
                + "Note: this homework is due before today, so homework list 2 shows it only with all.";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON.withHomeworks(List.of(ALGEBRA)), CARL));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_dueToday_addsHomeworkWithoutNote() {
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(2), ALGEBRA, false,
                LocalDate.of(2026, 10, 15));
        String expectedMessage = "New homework added for Benson Lim (92222222):\n"
                + "1. Complete algebra worksheet (MATH) - due 2026-10-15 - score Not recorded - status ASSIGNED";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON.withHomeworks(List.of(ALGEBRA)), CARL));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_dueBeforeTodayYearInferred_showsYearThenNote() {
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(2), ALGEBRA, true,
                LocalDate.of(2026, 10, 16));
        String expectedMessage = "New homework added for Benson Lim (92222222):\n"
                + "1. Complete algebra worksheet (MATH) - due 2026-10-15 - score Not recorded - status ASSIGNED\n"
                + "Inferred year: 2026\n"
                + "Note: this homework is due before today, so homework list 2 shows it only with all.";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON.withHomeworks(List.of(ALGEBRA)), CARL));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_dueBeforeTodayArabicDigitsLocale_showsAsciiStudentIndex() {
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(2), ALGEBRA, false,
                LocalDate.of(2026, 10, 16));

        runWithFormatLocale(ARABIC_EGYPT, () -> {
            String feedback = assertDoesNotThrow(() -> command.execute(model)).getFeedbackToUser();
            assertTrue(feedback.endsWith("\nNote: this homework is due before today, so homework list 2 shows it "
                    + "only with all."), feedback);
        });
    }

    @Test
    public void execute_sameTitleDifferentDueDate_addsHomework() {
        Homework laterAlgebra = new HomeworkBuilder(ALGEBRA).withDueDate("2026-10-16").build();
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(1), laterAlgebra, false, TODAY);
        String expectedMessage = "New homework added for Alice Tan (91111111):\n"
                + "4. Complete algebra worksheet (MATH) - due 2026-10-16 - score Not recorded - status ASSIGNED";
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(ALGEBRA, MECHANICS, ATOMIC_STRUCTURE, laterAlgebra)), BENSON, CARL));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_sameTitleDifferentSubject_addsHomework() {
        Homework physicsAlgebra = new HomeworkBuilder(ALGEBRA).withSubject("PHYSICS").build();
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(1), physicsAlgebra, false, TODAY);
        String expectedMessage = "New homework added for Alice Tan (91111111):\n"
                + "4. Complete algebra worksheet (PHYSICS) - due 2026-10-15 - score Not recorded - status ASSIGNED";
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(ALGEBRA, MECHANICS, ATOMIC_STRUCTURE, physicsAlgebra)), BENSON, CARL));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_studentIndexAboveListSize_failure() {
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(4), ALGEBRA, false, TODAY);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_filteredList_addsToDisplayedStudent() {
        // CARL is third in the roster but first in the displayed list
        model.updateFilteredStudentList(CARL::equals);
        Homework optics = new HomeworkBuilder().withTitle("Optics worksheet").withSubject("PHYSICS").build();
        String expectedMessage = "New homework added for Carl Ng (93333333):\n"
                + "2. Optics worksheet (PHYSICS) - due 2026-10-15 - score Not recorded - status ASSIGNED";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON,
                CARL.withHomeworks(List.of(MECHANICS, optics))));

        assertCommandSuccess(new HomeworkAddCommand(Index.fromOneBased(1), optics, false, TODAY), model,
                expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredListDueBeforeToday_noteNamesDisplayedIndex() {
        // CARL is third in the roster but first in the displayed list
        model.updateFilteredStudentList(CARL::equals);
        Homework optics = new HomeworkBuilder().withTitle("Optics worksheet").withSubject("PHYSICS").build();
        String expectedMessage = "New homework added for Carl Ng (93333333):\n"
                + "2. Optics worksheet (PHYSICS) - due 2026-10-15 - score Not recorded - status ASSIGNED\n"
                + "Note: this homework is due before today, so homework list 1 shows it only with all.";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON,
                CARL.withHomeworks(List.of(MECHANICS, optics))));

        assertCommandSuccess(new HomeworkAddCommand(Index.fromOneBased(1), optics, false, LocalDate.of(2026, 10, 16)),
                model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_indexBeyondFilteredList_failure() {
        model.updateFilteredStudentList(CARL::equals);

        assertCommandFailure(new HomeworkAddCommand(Index.fromOneBased(2), ALGEBRA, false, TODAY), model,
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyRoster_failure() {
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, false, TODAY);

        assertCommandFailure(command, getModelWithStudents(List.of()),
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_subjectNotTaken_failure() {
        // CARL takes PHYSICS only
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(3), ALGEBRA, false, TODAY);

        assertCommandFailure(command, model, "Carl Ng (93333333) does not take MATH. They take PHYSICS.");
    }

    @Test
    public void execute_subjectNotTakenBySeveralSubjectStudent_listsSubjects() {
        Model modelWithTwoSubjects = getModelWithStudents(List.of(new StudentBuilder().withName("Ethan Lim")
                .withPhone("91234567").withSubjects("MATH, PHYSICS").build()));
        Homework chemistry = new HomeworkBuilder().withTitle("S4").withSubject("CHEMISTRY").build();
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(1), chemistry, false, TODAY);

        assertCommandFailure(command, modelWithTwoSubjects,
                "Ethan Lim (91234567) does not take CHEMISTRY. They take MATH, PHYSICS.");
    }

    @Test
    public void execute_duplicateHomework_failure() {
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, false, TODAY);

        assertCommandFailure(command, model, "This homework already exists for Alice Tan (91111111):\n"
                + "1. Complete algebra worksheet (MATH) - due 2026-10-15 - score Not recorded - status ASSIGNED");
    }

    @Test
    public void execute_duplicateHomeworkDifferentTitleCase_failure() {
        Homework shoutedAlgebra = new HomeworkBuilder(ALGEBRA).withTitle("COMPLETE   algebra WORKSHEET").build();
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(1), shoutedAlgebra, false, TODAY);

        assertCommandFailure(command, model, "This homework already exists for Alice Tan (91111111):\n"
                + "1. Complete algebra worksheet (MATH) - due 2026-10-15 - score Not recorded - status ASSIGNED");
    }

    @Test
    public void execute_duplicateHomeworkDifferentUnicodeForm_failure() {
        // the existing title has a composed e with acute accent, the new one e followed by a combining accent
        Homework composedCafe = new HomeworkBuilder().withTitle("Caf\u00E9").withDueDate("2026-11-20").build();
        Homework decomposedCafe = new HomeworkBuilder().withTitle("Cafe\u0301").withDueDate("2026-11-20").build();
        Model modelWithCafe = getModelWithStudents(List.of(ALICE, BENSON.withHomeworks(List.of(composedCafe)), CARL));
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(2), decomposedCafe, false, TODAY);

        assertCommandFailure(command, modelWithCafe, "This homework already exists for Benson Lim (92222222):\n"
                + "1. Caf\u00E9 (MATH) - due 2026-11-20 - score Not recorded - status ASSIGNED");
    }

    @Test
    public void execute_duplicateScoredHomework_failure() {
        // the score does not affect identity
        Homework unscoredAtomicStructure = new HomeworkBuilder().withTitle("Revise atomic structure")
                .withSubject("CHEMISTRY").withDueDate("2026-10-18").build();
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(1), unscoredAtomicStructure, false,
                TODAY);

        assertCommandFailure(command, model, "This homework already exists for Alice Tan (91111111):\n"
                + "3. Revise atomic structure (CHEMISTRY) - due 2026-10-18 - score 17/20 - status ASSIGNED");
    }

    @Test
    public void execute_unexpectedError_reportsInternalErrorWithoutChangingModel() {
        Model failingModel = getModelFailingOnSetStudent();

        assertCommandFailure(new HomeworkAddCommand(Index.fromOneBased(2), ALGEBRA, false, TODAY), failingModel,
                HomeworkAddCommand.MESSAGE_INTERNAL_ERROR);
    }

    @Test
    public void getSaveFailureMessage_cause_namesFileAndCause() {
        String expectedMessage = "The homework could not be added because TutorFlow could not save "
                + SHOWN_DATA_FILE_NAME + ": the data folder is read-only. No homework data was changed.";
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, false, TODAY);
        assertEquals(expectedMessage, command.getSaveFailureMessage(SHOWN_DATA_FILE_NAME, CAUSE_READ_ONLY_FOLDER));
    }

    @Test
    public void equals() {
        Index firstIndex = Index.fromOneBased(1);
        HomeworkAddCommand addAlgebraCommand = new HomeworkAddCommand(firstIndex, ALGEBRA, false, TODAY);

        // same object -> returns true
        assertTrue(addAlgebraCommand.equals(addAlgebraCommand));

        // same values -> returns true
        HomeworkAddCommand addAlgebraCommandCopy = new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, false, TODAY);
        assertTrue(addAlgebraCommand.equals(addAlgebraCommandCopy));
        assertEquals(addAlgebraCommand.hashCode(), addAlgebraCommandCopy.hashCode());

        // null -> returns false
        assertFalse(addAlgebraCommand.equals(null));

        // different types -> returns false
        assertFalse(addAlgebraCommand.equals(1));

        // different student index -> returns false
        assertFalse(addAlgebraCommand.equals(new HomeworkAddCommand(Index.fromOneBased(2), ALGEBRA, false, TODAY)));

        // different homework -> returns false
        assertFalse(addAlgebraCommand.equals(new HomeworkAddCommand(firstIndex, MECHANICS, false, TODAY)));

        // different year inference -> returns false
        assertFalse(addAlgebraCommand.equals(new HomeworkAddCommand(firstIndex, ALGEBRA, true, TODAY)));

        // different today -> returns false
        assertFalse(addAlgebraCommand.equals(new HomeworkAddCommand(firstIndex, ALGEBRA, false,
                TODAY.plusDays(1))));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        HomeworkAddCommand command = new HomeworkAddCommand(index, ALGEBRA, true, TODAY);
        String expected = HomeworkAddCommand.class.getCanonicalName() + "{studentIndex=" + index + ", homework="
                + ALGEBRA + ", isYearInferred=true, today=" + TODAY + "}";
        assertEquals(expected, command.toString());
    }
}
