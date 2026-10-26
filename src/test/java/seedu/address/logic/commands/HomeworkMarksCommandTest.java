package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalHomeworks.ATOMIC_STRUCTURE;
import static seedu.address.testutil.TypicalHomeworks.MECHANICS;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.BENSON;
import static seedu.address.testutil.TypicalStudents.CARL;
import static seedu.address.testutil.TypicalStudents.getModelWithStudents;
import static seedu.address.testutil.TypicalStudents.getModelWithTypicalStudents;
import static seedu.address.testutil.TypicalStudents.getTypicalStudents;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.homework.Score;
import seedu.address.model.student.Student;
import seedu.address.testutil.HomeworkBuilder;

public class HomeworkMarksCommandTest {
    private static final Index FIRST = Index.fromOneBased(1);
    private static final Index SECOND = Index.fromOneBased(2);
    private static final Index THIRD = Index.fromOneBased(3);
    private static final Score FIVE_OUT_OF_TEN = new Score(5, 10);

    private final Model model = getModelWithTypicalStudents();

    @Test
    public void constructor_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkMarksCommand(null, FIRST, FIVE_OUT_OF_TEN));
        assertThrows(NullPointerException.class, () -> new HomeworkMarksCommand(FIRST, null, FIVE_OUT_OF_TEN));
        assertThrows(NullPointerException.class, () -> new HomeworkMarksCommand(FIRST, FIRST, null));
        assertThrows(NullPointerException.class, () -> new HomeworkMarksCommand(null, FIRST));
        assertThrows(NullPointerException.class, () -> new HomeworkMarksCommand(FIRST, null));
    }

    @Test
    public void execute_homeworkWithoutScore_recordsScore() {
        String expectedMessage = "Recorded homework marks:\n"
                + "Alice Tan; Title: Attempt mechanics questions 1-5; Subject: PHYSICS; Due: 2026-10-20; Score: 5/10";
        Model expectedModel = getModelWithStudents(List.of(ALICE.withHomeworks(List.of(ALGEBRA,
                new HomeworkBuilder(MECHANICS).withScore("5/10").build(), ATOMIC_STRUCTURE)), BENSON, CARL));

        assertCommandSuccess(new HomeworkMarksCommand(FIRST, SECOND, FIVE_OUT_OF_TEN), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_homeworkWithScore_replacesScore() {
        // ATOMIC_STRUCTURE has 17/20; the same ratio with another total is a different score
        String expectedMessage = "Recorded homework marks:\n"
                + "Alice Tan; Title: Revise atomic structure; Subject: CHEMISTRY; Due: 2026-10-18; Score: 34/40";
        Model expectedModel = getModelWithStudents(List.of(ALICE.withHomeworks(List.of(ALGEBRA, MECHANICS,
                new HomeworkBuilder(ATOMIC_STRUCTURE).withScore("34/40").build())), BENSON, CARL));

        assertCommandSuccess(new HomeworkMarksCommand(FIRST, THIRD, new Score(34, 40)), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_bareScore_recordsScoreWithoutTotal() {
        String expectedMessage = "Recorded homework marks:\n"
                + "Alice Tan; Title: Attempt mechanics questions 1-5; Subject: PHYSICS; Due: 2026-10-20; Score: 17";
        Model expectedModel = getModelWithStudents(List.of(ALICE.withHomeworks(List.of(ALGEBRA,
                new HomeworkBuilder(MECHANICS).withScore("17").build(), ATOMIC_STRUCTURE)), BENSON, CARL));

        assertCommandSuccess(new HomeworkMarksCommand(FIRST, SECOND, new Score(17)), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_bareScoreReplacingSameMarksWithTotal_replacesScore() {
        // ATOMIC_STRUCTURE has 17/20; 17 without a total is a different score
        String expectedMessage = "Recorded homework marks:\n"
                + "Alice Tan; Title: Revise atomic structure; Subject: CHEMISTRY; Due: 2026-10-18; Score: 17";
        Model expectedModel = getModelWithStudents(List.of(ALICE.withHomeworks(List.of(ALGEBRA, MECHANICS,
                new HomeworkBuilder(ATOMIC_STRUCTURE).withScore("17").build())), BENSON, CARL));

        assertCommandSuccess(new HomeworkMarksCommand(FIRST, THIRD, new Score(17)), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_scoreWithTotalReplacingBareScore_replacesScore() {
        Model modelWithBareScore = getModelWithStudents(List.of(ALICE.withHomeworks(List.of(ALGEBRA, MECHANICS,
                new HomeworkBuilder(ATOMIC_STRUCTURE).withScore("17").build())), BENSON, CARL));
        String expectedMessage = "Recorded homework marks:\n"
                + "Alice Tan; Title: Revise atomic structure; Subject: CHEMISTRY; Due: 2026-10-18; Score: 17/20";

        assertCommandSuccess(new HomeworkMarksCommand(FIRST, THIRD, new Score(17, 20)), modelWithBareScore,
                expectedMessage, getModelWithTypicalStudents());
    }

    @Test
    public void execute_zeroMarks_recordsZero() {
        String expectedMessage = "Recorded homework marks:\n"
                + "Carl Ng; Title: Attempt mechanics questions 1-5; Subject: PHYSICS; Due: 2026-10-20; Score: 0/10";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON,
                CARL.withHomeworks(List.of(new HomeworkBuilder(MECHANICS).withScore("0/10").build()))));

        assertCommandSuccess(new HomeworkMarksCommand(THIRD, FIRST, new Score(0, 10)), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_homeworkWithScore_clearsScore() {
        String expectedMessage = "Cleared homework marks:\n"
                + "Alice Tan; Title: Revise atomic structure; Subject: CHEMISTRY; Due: 2026-10-18; Score: Not recorded";
        Model expectedModel = getModelWithStudents(List.of(ALICE.withHomeworks(List.of(ALGEBRA, MECHANICS,
                new HomeworkBuilder(ATOMIC_STRUCTURE).withoutScore().build())), BENSON, CARL));

        assertCommandSuccess(new HomeworkMarksCommand(FIRST, THIRD), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_clearHomeworkWithoutScore_failure() {
        assertCommandFailure(new HomeworkMarksCommand(FIRST, SECOND), model,
                HomeworkMarksCommand.MESSAGE_NO_MARKS_TO_CLEAR);
    }

    @Test
    public void execute_clearIndicesOutOfRange_failure() {
        // student index
        assertCommandFailure(new HomeworkMarksCommand(Index.fromOneBased(4), FIRST), model,
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);

        // student without homework
        assertCommandFailure(new HomeworkMarksCommand(SECOND, FIRST), model,
                HomeworkMarksCommand.MESSAGE_NO_HOMEWORK);

        // homework index
        assertCommandFailure(new HomeworkMarksCommand(FIRST, Index.fromOneBased(4)), model,
                Messages.MESSAGE_INVALID_HOMEWORK_DISPLAYED_INDEX);
    }

    @Test
    public void execute_studentIndexAboveListSize_failure() {
        assertCommandFailure(new HomeworkMarksCommand(Index.fromOneBased(4), FIRST, FIVE_OUT_OF_TEN), model,
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyRoster_failure() {
        assertCommandFailure(new HomeworkMarksCommand(FIRST, FIRST, FIVE_OUT_OF_TEN),
                getModelWithStudents(List.of()), Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_bothIndicesOutOfRange_reportsStudentIndex() {
        assertCommandFailure(new HomeworkMarksCommand(Index.fromOneBased(4), Index.fromOneBased(9), FIVE_OUT_OF_TEN),
                model, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_studentWithoutHomework_failure() {
        assertCommandFailure(new HomeworkMarksCommand(SECOND, FIRST, FIVE_OUT_OF_TEN), model,
                HomeworkMarksCommand.MESSAGE_NO_HOMEWORK);
    }

    @Test
    public void execute_homeworkIndexAboveListSize_failure() {
        assertCommandFailure(new HomeworkMarksCommand(FIRST, Index.fromOneBased(4), FIVE_OUT_OF_TEN), model,
                Messages.MESSAGE_INVALID_HOMEWORK_DISPLAYED_INDEX);
    }

    @Test
    public void execute_sameScore_failure() {
        assertCommandFailure(new HomeworkMarksCommand(FIRST, THIRD, new Score(17, 20)), model,
                HomeworkMarksCommand.MESSAGE_NO_CHANGES);
    }

    @Test
    public void execute_sameBareScore_failure() {
        Model modelWithBareScore = getModelWithStudents(List.of(ALICE.withHomeworks(List.of(ALGEBRA, MECHANICS,
                new HomeworkBuilder(ATOMIC_STRUCTURE).withScore("17").build())), BENSON, CARL));

        assertCommandFailure(new HomeworkMarksCommand(FIRST, THIRD, new Score(17)), modelWithBareScore,
                HomeworkMarksCommand.MESSAGE_NO_CHANGES);
    }

    @Test
    public void execute_unexpectedError_reportsInternalErrorWithoutChangingModel() {
        Model failingModel = new ModelManager() {
            @Override
            public void setStudent(Student target, Student editedStudent) {
                throw new IllegalStateException("dummy internal error");
            }
        };
        getTypicalStudents().forEach(failingModel::addStudent);

        assertCommandFailure(new HomeworkMarksCommand(FIRST, FIRST, FIVE_OUT_OF_TEN), failingModel,
                HomeworkMarksCommand.MESSAGE_INTERNAL_ERROR);
    }

    @Test
    public void getSaveFailureMessage_anyDefaultMessage_returnsHomeworkMessage() {
        String expectedMessage = "The homework marks could not be recorded because TutorFlow could not save the "
                + "updated data. No homework data was changed.";
        assertEquals(expectedMessage, new HomeworkMarksCommand(FIRST, FIRST, FIVE_OUT_OF_TEN)
                .getSaveFailureMessage("Could not save data"));
    }

    @Test
    public void equals() {
        HomeworkMarksCommand marksCommand = new HomeworkMarksCommand(FIRST, SECOND, FIVE_OUT_OF_TEN);

        // same object -> returns true
        assertTrue(marksCommand.equals(marksCommand));

        // same values -> returns true
        HomeworkMarksCommand marksCommandCopy = new HomeworkMarksCommand(Index.fromOneBased(1),
                Index.fromOneBased(2), new Score(5, 10));
        assertTrue(marksCommand.equals(marksCommandCopy));
        assertEquals(marksCommand.hashCode(), marksCommandCopy.hashCode());

        // null -> returns false
        assertFalse(marksCommand.equals(null));

        // different types -> returns false
        assertFalse(marksCommand.equals(1));

        // different student index -> returns false
        assertFalse(marksCommand.equals(new HomeworkMarksCommand(SECOND, SECOND, FIVE_OUT_OF_TEN)));

        // different homework index -> returns false
        assertFalse(marksCommand.equals(new HomeworkMarksCommand(FIRST, FIRST, FIVE_OUT_OF_TEN)));

        // different score -> returns false
        assertFalse(marksCommand.equals(new HomeworkMarksCommand(FIRST, SECOND, new Score(1, 2))));

        // same marks without a total -> returns false
        assertFalse(marksCommand.equals(new HomeworkMarksCommand(FIRST, SECOND, new Score(5))));

        // clearing instead of recording -> returns false
        HomeworkMarksCommand clearCommand = new HomeworkMarksCommand(FIRST, SECOND);
        assertFalse(marksCommand.equals(clearCommand));
        assertFalse(clearCommand.equals(marksCommand));

        // both clearing the same homework -> returns true
        HomeworkMarksCommand clearCommandCopy = new HomeworkMarksCommand(Index.fromOneBased(1), Index.fromOneBased(2));
        assertTrue(clearCommand.equals(clearCommandCopy));
        assertEquals(clearCommand.hashCode(), clearCommandCopy.hashCode());
    }

    @Test
    public void toStringMethod() {
        String expected = HomeworkMarksCommand.class.getCanonicalName() + "{studentIndex=" + FIRST
                + ", homeworkIndex=" + SECOND + ", score=5/10}";
        assertEquals(expected, new HomeworkMarksCommand(FIRST, SECOND, FIVE_OUT_OF_TEN).toString());

        String expectedClear = HomeworkMarksCommand.class.getCanonicalName() + "{studentIndex=" + FIRST
                + ", homeworkIndex=" + SECOND + ", score=null}";
        assertEquals(expectedClear, new HomeworkMarksCommand(FIRST, SECOND).toString());
    }
}
