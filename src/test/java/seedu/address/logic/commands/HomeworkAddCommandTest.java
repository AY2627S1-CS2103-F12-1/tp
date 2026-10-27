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

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.testutil.HomeworkBuilder;

public class HomeworkAddCommandTest {
    private final Model model = getModelWithTypicalStudents();

    @Test
    public void constructor_nullStudentIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkAddCommand(null, ALGEBRA, false));
    }

    @Test
    public void constructor_nullHomework_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkAddCommand(Index.fromOneBased(1), null, false));
    }

    @Test
    public void execute_studentWithoutHomework_addsHomework() {
        // ALICE already has ALGEBRA; the same homework may be assigned to another student
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(2), ALGEBRA, false);
        String expectedMessage = "New homework added:\n"
                + "Benson Lim; Title: Complete algebra worksheet; Subject: MATH; Due: 2026-10-15";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON.withHomeworks(List.of(ALGEBRA)), CARL));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_studentWithHomework_appendsHomeworkAtEnd() {
        Homework trigonometry = new HomeworkBuilder().withTitle("Trigonometry practice").withDueDate("2026-11-01")
                .build();
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(1), trigonometry, false);
        String expectedMessage = "New homework added:\n"
                + "Alice Tan; Title: Trigonometry practice; Subject: MATH; Due: 2026-11-01";
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(ALGEBRA, MECHANICS, ATOMIC_STRUCTURE, trigonometry)), BENSON, CARL));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_lastStudentIndex_addsHomework() {
        Homework optics = new HomeworkBuilder().withTitle("Optics worksheet").withSubject("PHYSICS").build();
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(3), optics, false);
        String expectedMessage = "New homework added:\n"
                + "Carl Ng; Title: Optics worksheet; Subject: PHYSICS; Due: 2026-10-15";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON,
                CARL.withHomeworks(List.of(MECHANICS, optics))));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_yearInferred_showsInferredYear() {
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(2), ALGEBRA, true);
        String expectedMessage = "New homework added:\n"
                + "Benson Lim; Title: Complete algebra worksheet; Subject: MATH; Due: 2026-10-15\n"
                + "Inferred year: 2026";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON.withHomeworks(List.of(ALGEBRA)), CARL));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_yearInferredArabicDigitsLocale_showsAsciiYear() {
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(2), ALGEBRA, true);

        runWithFormatLocale(ARABIC_EGYPT, () -> {
            String feedback = assertDoesNotThrow(() -> command.execute(model)).getFeedbackToUser();
            assertTrue(feedback.endsWith("Due: 2026-10-15\nInferred year: 2026"), feedback);
        });
    }

    @Test
    public void execute_sameTitleDifferentDueDate_addsHomework() {
        Homework laterAlgebra = new HomeworkBuilder(ALGEBRA).withDueDate("2026-10-16").build();
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(1), laterAlgebra, false);
        String expectedMessage = "New homework added:\n"
                + "Alice Tan; Title: Complete algebra worksheet; Subject: MATH; Due: 2026-10-16";
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(ALGEBRA, MECHANICS, ATOMIC_STRUCTURE, laterAlgebra)), BENSON, CARL));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_sameTitleDifferentSubject_addsHomework() {
        Homework physicsAlgebra = new HomeworkBuilder(ALGEBRA).withSubject("PHYSICS").build();
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(1), physicsAlgebra, false);
        String expectedMessage = "New homework added:\n"
                + "Alice Tan; Title: Complete algebra worksheet; Subject: PHYSICS; Due: 2026-10-15";
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(ALGEBRA, MECHANICS, ATOMIC_STRUCTURE, physicsAlgebra)), BENSON, CARL));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_studentIndexAboveListSize_failure() {
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(4), ALGEBRA, false);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_filteredList_addsToDisplayedStudent() {
        // CARL is third in the roster but first in the displayed list
        model.updateFilteredStudentList(CARL::equals);
        Homework optics = new HomeworkBuilder().withTitle("Optics worksheet").withSubject("PHYSICS").build();
        String expectedMessage = "New homework added:\n"
                + "Carl Ng; Title: Optics worksheet; Subject: PHYSICS; Due: 2026-10-15";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON,
                CARL.withHomeworks(List.of(MECHANICS, optics))));

        assertCommandSuccess(new HomeworkAddCommand(Index.fromOneBased(1), optics, false), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_indexBeyondFilteredList_failure() {
        model.updateFilteredStudentList(CARL::equals);

        assertCommandFailure(new HomeworkAddCommand(Index.fromOneBased(2), ALGEBRA, false), model,
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyRoster_failure() {
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, false);

        assertCommandFailure(command, getModelWithStudents(List.of()),
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_subjectNotTaken_failure() {
        // CARL takes PHYSICS only
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(3), ALGEBRA, false);

        assertCommandFailure(command, model, HomeworkAddCommand.MESSAGE_SUBJECT_NOT_TAKEN);
    }

    @Test
    public void execute_duplicateHomework_failure() {
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, false);

        assertCommandFailure(command, model, HomeworkAddCommand.MESSAGE_DUPLICATE_HOMEWORK);
    }

    @Test
    public void execute_duplicateHomeworkDifferentTitleCase_failure() {
        Homework shoutedAlgebra = new HomeworkBuilder(ALGEBRA).withTitle("COMPLETE   algebra WORKSHEET").build();
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(1), shoutedAlgebra, false);

        assertCommandFailure(command, model, HomeworkAddCommand.MESSAGE_DUPLICATE_HOMEWORK);
    }

    @Test
    public void execute_duplicateHomeworkDifferentUnicodeForm_failure() {
        // the existing title has a composed e with acute accent, the new one e followed by a combining accent
        Homework composedCafe = new HomeworkBuilder().withTitle("Caf\u00E9").withDueDate("2026-11-20").build();
        Homework decomposedCafe = new HomeworkBuilder().withTitle("Cafe\u0301").withDueDate("2026-11-20").build();
        Model modelWithCafe = getModelWithStudents(List.of(ALICE, BENSON.withHomeworks(List.of(composedCafe)), CARL));
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(2), decomposedCafe, false);

        assertCommandFailure(command, modelWithCafe, HomeworkAddCommand.MESSAGE_DUPLICATE_HOMEWORK);
    }

    @Test
    public void execute_duplicateScoredHomework_failure() {
        // the score does not affect identity
        Homework unscoredAtomicStructure = new HomeworkBuilder().withTitle("Revise atomic structure")
                .withSubject("CHEMISTRY").withDueDate("2026-10-18").build();
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(1), unscoredAtomicStructure, false);

        assertCommandFailure(command, model, HomeworkAddCommand.MESSAGE_DUPLICATE_HOMEWORK);
    }

    @Test
    public void execute_unexpectedError_reportsInternalErrorWithoutChangingModel() {
        Model failingModel = getModelFailingOnSetStudent();

        assertCommandFailure(new HomeworkAddCommand(Index.fromOneBased(2), ALGEBRA, false), failingModel,
                HomeworkAddCommand.MESSAGE_INTERNAL_ERROR);
    }

    @Test
    public void getSaveFailureMessage_cause_namesFileAndCause() {
        String expectedMessage = "The homework could not be added because TutorFlow could not save "
                + SHOWN_DATA_FILE_NAME + ": the data folder is read-only. No homework data was changed.";
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, false);
        assertEquals(expectedMessage, command.getSaveFailureMessage(SHOWN_DATA_FILE_NAME, CAUSE_READ_ONLY_FOLDER));
    }

    @Test
    public void equals() {
        Index firstIndex = Index.fromOneBased(1);
        HomeworkAddCommand addAlgebraCommand = new HomeworkAddCommand(firstIndex, ALGEBRA, false);

        // same object -> returns true
        assertTrue(addAlgebraCommand.equals(addAlgebraCommand));

        // same values -> returns true
        HomeworkAddCommand addAlgebraCommandCopy = new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, false);
        assertTrue(addAlgebraCommand.equals(addAlgebraCommandCopy));
        assertEquals(addAlgebraCommand.hashCode(), addAlgebraCommandCopy.hashCode());

        // null -> returns false
        assertFalse(addAlgebraCommand.equals(null));

        // different types -> returns false
        assertFalse(addAlgebraCommand.equals(1));

        // different student index -> returns false
        assertFalse(addAlgebraCommand.equals(new HomeworkAddCommand(Index.fromOneBased(2), ALGEBRA, false)));

        // different homework -> returns false
        assertFalse(addAlgebraCommand.equals(new HomeworkAddCommand(firstIndex, MECHANICS, false)));

        // different year inference -> returns false
        assertFalse(addAlgebraCommand.equals(new HomeworkAddCommand(firstIndex, ALGEBRA, true)));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        HomeworkAddCommand command = new HomeworkAddCommand(index, ALGEBRA, true);
        String expected = HomeworkAddCommand.class.getCanonicalName() + "{studentIndex=" + index + ", homework="
                + ALGEBRA + ", isYearInferred=true}";
        assertEquals(expected, command.toString());
    }
}
