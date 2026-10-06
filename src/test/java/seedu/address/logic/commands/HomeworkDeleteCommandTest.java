package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.assertStudentCommandFailure;
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
import seedu.address.model.homework.Homework;
import seedu.address.model.student.Student;
import seedu.address.testutil.HomeworkBuilder;
import seedu.address.testutil.StudentBuilder;

public class HomeworkDeleteCommandTest {
    private final Model model = getModelWithTypicalStudents();

    @Test
    public void constructor_nullStudentIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkDeleteCommand(null, Index.fromOneBased(1)));
    }

    @Test
    public void constructor_nullHomeworkIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkDeleteCommand(Index.fromOneBased(1), null));
    }

    @Test
    public void execute_firstHomework_deletesAndKeepsOrder() {
        HomeworkDeleteCommand command = new HomeworkDeleteCommand(Index.fromOneBased(1), Index.fromOneBased(1));
        String expectedMessage = "Deleted homework:\n"
                + "Alice Tan; Title: Complete algebra worksheet; Subject: MATH; Due: 2026-10-15";
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(MECHANICS, ATOMIC_STRUCTURE)), BENSON, CARL));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_middleHomework_deletesAndKeepsOrder() {
        HomeworkDeleteCommand command = new HomeworkDeleteCommand(Index.fromOneBased(1), Index.fromOneBased(2));
        String expectedMessage = "Deleted homework:\n"
                + "Alice Tan; Title: Attempt mechanics questions 1-5; Subject: PHYSICS; Due: 2026-10-20";
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(ALGEBRA, ATOMIC_STRUCTURE)), BENSON, CARL));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_lastHomework_deletesAndKeepsOrder() {
        HomeworkDeleteCommand command = new HomeworkDeleteCommand(Index.fromOneBased(1), Index.fromOneBased(3));
        String expectedMessage = "Deleted homework:\n"
                + "Alice Tan; Title: Revise atomic structure; Subject: CHEMISTRY; Due: 2026-10-18";
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(ALGEBRA, MECHANICS)), BENSON, CARL));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_lastStudentOnlyHomework_leavesEmptyList() {
        HomeworkDeleteCommand command = new HomeworkDeleteCommand(Index.fromOneBased(3), Index.fromOneBased(1));
        String expectedMessage = "Deleted homework:\n"
                + "Carl Ng; Title: Attempt mechanics questions 1-5; Subject: PHYSICS; Due: 2026-10-20";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON, CARL.withHomeworks(List.of())));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_similarHomework_deletesOnlySelected() {
        Homework laterAlgebra = new HomeworkBuilder(ALGEBRA).withDueDate("2026-10-16").build();
        Student student = new StudentBuilder().withHomeworks(ALGEBRA, laterAlgebra).build();
        Model actualModel = getModelWithStudents(List.of(student));
        HomeworkDeleteCommand command = new HomeworkDeleteCommand(Index.fromOneBased(1), Index.fromOneBased(1));
        String expectedMessage = "Deleted homework:\n"
                + "John Tan; Title: Complete algebra worksheet; Subject: MATH; Due: 2026-10-15";
        Model expectedModel = getModelWithStudents(List.of(student.withHomeworks(List.of(laterAlgebra))));

        assertCommandSuccess(command, actualModel, expectedMessage, expectedModel);
    }

    @Test
    public void execute_studentIndexAboveListSize_failure() {
        HomeworkDeleteCommand command = new HomeworkDeleteCommand(Index.fromOneBased(4), Index.fromOneBased(1));

        assertStudentCommandFailure(command, model, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_bothIndicesOutOfRange_reportsStudentIndex() {
        HomeworkDeleteCommand command = new HomeworkDeleteCommand(Index.fromOneBased(4), Index.fromOneBased(9));

        assertStudentCommandFailure(command, model, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_studentWithoutHomework_failure() {
        HomeworkDeleteCommand command = new HomeworkDeleteCommand(Index.fromOneBased(2), Index.fromOneBased(1));

        assertStudentCommandFailure(command, model, HomeworkDeleteCommand.MESSAGE_NO_HOMEWORK);
    }

    @Test
    public void execute_homeworkIndexAboveListSize_failure() {
        HomeworkDeleteCommand command = new HomeworkDeleteCommand(Index.fromOneBased(1), Index.fromOneBased(4));

        assertStudentCommandFailure(command, model, Messages.MESSAGE_INVALID_HOMEWORK_DISPLAYED_INDEX);
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

        assertStudentCommandFailure(new HomeworkDeleteCommand(Index.fromOneBased(1), Index.fromOneBased(1)),
                failingModel, HomeworkDeleteCommand.MESSAGE_INTERNAL_ERROR);
    }

    @Test
    public void equals() {
        Index firstIndex = Index.fromOneBased(1);
        Index secondIndex = Index.fromOneBased(2);
        HomeworkDeleteCommand deleteCommand = new HomeworkDeleteCommand(firstIndex, secondIndex);

        // same object -> returns true
        assertTrue(deleteCommand.equals(deleteCommand));

        // same values -> returns true
        assertTrue(deleteCommand.equals(new HomeworkDeleteCommand(Index.fromOneBased(1), Index.fromOneBased(2))));

        // null -> returns false
        assertFalse(deleteCommand.equals(null));

        // different types -> returns false
        assertFalse(deleteCommand.equals(1));

        // different student index -> returns false
        assertFalse(deleteCommand.equals(new HomeworkDeleteCommand(secondIndex, secondIndex)));

        // different homework index -> returns false
        assertFalse(deleteCommand.equals(new HomeworkDeleteCommand(firstIndex, firstIndex)));
    }

    @Test
    public void toStringMethod() {
        Index studentIndex = Index.fromOneBased(1);
        Index homeworkIndex = Index.fromOneBased(2);
        String expected = HomeworkDeleteCommand.class.getCanonicalName() + "{studentIndex=" + studentIndex
                + ", homeworkIndex=" + homeworkIndex + "}";
        assertEquals(expected, new HomeworkDeleteCommand(studentIndex, homeworkIndex).toString());
    }
}
