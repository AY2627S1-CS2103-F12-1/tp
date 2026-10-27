package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.CAUSE_READ_ONLY_FOLDER;
import static seedu.address.logic.commands.CommandTestUtil.SHOWN_DATA_FILE_NAME;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.formatHomeworkLine;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalHomeworks.ATOMIC_STRUCTURE;
import static seedu.address.testutil.TypicalHomeworks.COMPLETED_MECHANICS;
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
import seedu.address.model.student.Student;
import seedu.address.testutil.HomeworkBuilder;
import seedu.address.testutil.StudentBuilder;

public class HomeworkDeleteCommandTest {
    /** Today, before all typical homework is due. */
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 5);
    private static final String ALGEBRA_TITLE = "Complete algebra worksheet";
    private static final String MECHANICS_TITLE = "Attempt mechanics questions 1-5";
    private static final String ATOMIC_STRUCTURE_TITLE = "Revise atomic structure";

    private final Model model = getModelWithTypicalStudents();

    @Test
    public void constructor_nullStudentIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkDeleteCommand(null, Index.fromOneBased(1),
                TODAY));
    }

    @Test
    public void constructor_nullToday_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkDeleteCommand(Index.fromOneBased(1),
                Index.fromOneBased(1), null));
    }

    @Test
    public void constructor_nullHomeworkIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkDeleteCommand(Index.fromOneBased(1), null,
                TODAY));
    }

    @Test
    public void execute_firstHomework_showsDeletedLineAndRenumberedList() {
        String expectedMessage = "Deleted homework:\n"
                + formatHomeworkLine(1, ALGEBRA_TITLE, "MATH", "2026-10-15", "Not recorded", "ASSIGNED")
                + "\nListed 2 upcoming homework records for Alice Tan.\nHomework for Alice Tan:\n"
                + formatHomeworkLine(2, ATOMIC_STRUCTURE_TITLE, "CHEMISTRY", "2026-10-18", "17/20", "ASSIGNED") + "\n"
                + formatHomeworkLine(1, MECHANICS_TITLE, "PHYSICS", "2026-10-20", "Not recorded", "ASSIGNED");
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(MECHANICS, ATOMIC_STRUCTURE)), BENSON, CARL));

        assertCommandSuccess(deleteCommand(1, 1, TODAY), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_middleHomework_renumbersLaterHomework() {
        // atomic structure moves from 3 to 2
        String expectedMessage = "Deleted homework:\n"
                + formatHomeworkLine(2, MECHANICS_TITLE, "PHYSICS", "2026-10-20", "Not recorded", "ASSIGNED")
                + "\nListed 2 upcoming homework records for Alice Tan.\nHomework for Alice Tan:\n"
                + formatHomeworkLine(1, ALGEBRA_TITLE, "MATH", "2026-10-15", "Not recorded", "ASSIGNED") + "\n"
                + formatHomeworkLine(2, ATOMIC_STRUCTURE_TITLE, "CHEMISTRY", "2026-10-18", "17/20", "ASSIGNED");
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(ALGEBRA, ATOMIC_STRUCTURE)), BENSON, CARL));

        assertCommandSuccess(deleteCommand(1, 2, TODAY), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_scoredHomework_showsScoreAndStatus() {
        String expectedMessage = "Deleted homework:\n"
                + formatHomeworkLine(3, ATOMIC_STRUCTURE_TITLE, "CHEMISTRY", "2026-10-18", "17/20", "ASSIGNED")
                + "\nListed 2 upcoming homework records for Alice Tan.\nHomework for Alice Tan:\n"
                + formatHomeworkLine(1, ALGEBRA_TITLE, "MATH", "2026-10-15", "Not recorded", "ASSIGNED") + "\n"
                + formatHomeworkLine(2, MECHANICS_TITLE, "PHYSICS", "2026-10-20", "Not recorded", "ASSIGNED");
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(ALGEBRA, MECHANICS)), BENSON, CARL));

        assertCommandSuccess(deleteCommand(1, 3, TODAY), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_overdueHomeworkLeft_listsUpcomingOnly() {
        // on 2026-10-16 the algebra homework (due 2026-10-15) is overdue, so it is not listed
        String expectedMessage = "Deleted homework:\n"
                + formatHomeworkLine(2, MECHANICS_TITLE, "PHYSICS", "2026-10-20", "Not recorded", "ASSIGNED")
                + "\nListed 1 upcoming homework record for Alice Tan.\nHomework for Alice Tan:\n"
                + formatHomeworkLine(2, ATOMIC_STRUCTURE_TITLE, "CHEMISTRY", "2026-10-18", "17/20", "ASSIGNED");
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(ALGEBRA, ATOMIC_STRUCTURE)), BENSON, CARL));

        assertCommandSuccess(deleteCommand(1, 2, LocalDate.of(2026, 10, 16)), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_onlyOverdueHomeworkLeft_showsNoUpcomingHomework() {
        String expectedMessage = "Deleted homework:\n"
                + formatHomeworkLine(3, ATOMIC_STRUCTURE_TITLE, "CHEMISTRY", "2026-10-18", "17/20", "ASSIGNED")
                + "\nNo upcoming homework found for Alice Tan.";
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(ALGEBRA, MECHANICS)), BENSON, CARL));

        assertCommandSuccess(deleteCommand(1, 3, LocalDate.of(2026, 10, 21)), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_lastStudentOnlyHomework_showsNoHomeworkFound() {
        String expectedMessage = "Deleted homework:\n"
                + formatHomeworkLine(1, MECHANICS_TITLE, "PHYSICS", "2026-10-20", "Not recorded", "ASSIGNED")
                + "\nNo homework found for Carl Ng.";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON, CARL.withHomeworks(List.of())));

        assertCommandSuccess(deleteCommand(3, 1, TODAY), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_completedHomework_showsCompletedStatus() {
        Student student = new StudentBuilder().withHomeworks(COMPLETED_MECHANICS).build();
        Model actualModel = getModelWithStudents(List.of(student));
        String expectedMessage = "Deleted homework:\n"
                + formatHomeworkLine(1, MECHANICS_TITLE, "PHYSICS", "2026-10-20", "Not recorded", "COMPLETED")
                + "\nNo homework found for John Tan.";
        Model expectedModel = getModelWithStudents(List.of(student.withHomeworks(List.of())));

        assertCommandSuccess(deleteCommand(1, 1, TODAY), actualModel, expectedMessage, expectedModel);
    }

    @Test
    public void execute_similarHomework_deletesOnlySelected() {
        Homework laterAlgebra = new HomeworkBuilder(ALGEBRA).withDueDate("2026-10-16").build();
        Student student = new StudentBuilder().withHomeworks(ALGEBRA, laterAlgebra).build();
        Model actualModel = getModelWithStudents(List.of(student));
        String expectedMessage = "Deleted homework:\n"
                + formatHomeworkLine(1, ALGEBRA_TITLE, "MATH", "2026-10-15", "Not recorded", "ASSIGNED")
                + "\nListed 1 upcoming homework record for John Tan.\nHomework for John Tan:\n"
                + formatHomeworkLine(1, ALGEBRA_TITLE, "MATH", "2026-10-16", "Not recorded", "ASSIGNED");
        Model expectedModel = getModelWithStudents(List.of(student.withHomeworks(List.of(laterAlgebra))));

        assertCommandSuccess(deleteCommand(1, 1, TODAY), actualModel, expectedMessage, expectedModel);
    }

    @Test
    public void execute_studentIndexAboveListSize_failure() {
        HomeworkDeleteCommand command = deleteCommand(4, 1, TODAY);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_filteredList_deletesFromDisplayedStudent() {
        // CARL is third in the roster but first in the displayed list
        model.updateFilteredStudentList(CARL::equals);
        String expectedMessage = "Deleted homework:\n"
                + formatHomeworkLine(1, MECHANICS_TITLE, "PHYSICS", "2026-10-20", "Not recorded", "ASSIGNED")
                + "\nNo homework found for Carl Ng.";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON, CARL.withHomeworks(List.of())));

        assertCommandSuccess(deleteCommand(1, 1, TODAY), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_indexBeyondFilteredList_failure() {
        model.updateFilteredStudentList(CARL::equals);

        assertCommandFailure(deleteCommand(2, 1, TODAY), model,
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_bothIndicesOutOfRange_reportsStudentIndex() {
        HomeworkDeleteCommand command = deleteCommand(4, 9, TODAY);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_studentWithoutHomework_failure() {
        HomeworkDeleteCommand command = deleteCommand(2, 1, TODAY);

        assertCommandFailure(command, model, HomeworkDeleteCommand.MESSAGE_NO_HOMEWORK);
    }

    @Test
    public void execute_homeworkIndexAboveListSize_failure() {
        HomeworkDeleteCommand command = deleteCommand(1, 4, TODAY);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_HOMEWORK_DISPLAYED_INDEX);
    }

    @Test
    public void execute_unexpectedError_reportsInternalErrorWithoutChangingModel() {
        Model failingModel = getModelFailingOnSetStudent();

        assertCommandFailure(deleteCommand(1, 1, TODAY),
                failingModel, HomeworkDeleteCommand.MESSAGE_INTERNAL_ERROR);
    }

    @Test
    public void getSaveFailureMessage_cause_namesFileAndCause() {
        String expectedMessage = "The homework could not be deleted because TutorFlow could not save "
                + SHOWN_DATA_FILE_NAME + ": the data folder is read-only. No homework data was changed.";
        HomeworkDeleteCommand command = deleteCommand(1, 1, TODAY);
        assertEquals(expectedMessage, command.getSaveFailureMessage(SHOWN_DATA_FILE_NAME, CAUSE_READ_ONLY_FOLDER));
    }

    @Test
    public void equals() {
        Index firstIndex = Index.fromOneBased(1);
        Index secondIndex = Index.fromOneBased(2);
        HomeworkDeleteCommand deleteCommand = new HomeworkDeleteCommand(firstIndex, secondIndex, TODAY);

        // same object -> returns true
        assertTrue(deleteCommand.equals(deleteCommand));

        // same values -> returns true
        HomeworkDeleteCommand deleteCommandCopy = deleteCommand(1, 2, TODAY);
        assertTrue(deleteCommand.equals(deleteCommandCopy));
        assertEquals(deleteCommand.hashCode(), deleteCommandCopy.hashCode());

        // null -> returns false
        assertFalse(deleteCommand.equals(null));

        // different types -> returns false
        assertFalse(deleteCommand.equals(1));

        // different student index -> returns false
        assertFalse(deleteCommand.equals(new HomeworkDeleteCommand(secondIndex, secondIndex, TODAY)));

        // different homework index -> returns false
        assertFalse(deleteCommand.equals(new HomeworkDeleteCommand(firstIndex, firstIndex, TODAY)));

        // different today -> returns false
        assertFalse(deleteCommand(1, 2, TODAY).equals(deleteCommand(1, 2, TODAY.plusDays(1))));
    }

    @Test
    public void toStringMethod() {
        Index studentIndex = Index.fromOneBased(1);
        Index homeworkIndex = Index.fromOneBased(2);
        String result = new HomeworkDeleteCommand(studentIndex, homeworkIndex, TODAY).toString();

        assertTrue(result.startsWith(HomeworkDeleteCommand.class.getCanonicalName() + "{studentIndex="
                + studentIndex + ", homeworkIndex=" + homeworkIndex), result);
        assertTrue(result.contains("today=2026-10-05"), result);
    }

    /**
     * Returns a command that deletes the given homework of the given displayed student, both one-based, and lists
     * the student's homework upcoming on {@code today}.
     */
    private static HomeworkDeleteCommand deleteCommand(int studentIndex, int homeworkIndex, LocalDate today) {
        return new HomeworkDeleteCommand(Index.fromOneBased(studentIndex), Index.fromOneBased(homeworkIndex), today);
    }
}
