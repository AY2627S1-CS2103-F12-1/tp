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

        assertStudentCommandFailure(command, model, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyRoster_failure() {
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, false);

        assertStudentCommandFailure(command, getModelWithStudents(List.of()),
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_subjectNotTaken_failure() {
        // CARL takes PHYSICS only
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(3), ALGEBRA, false);

        assertStudentCommandFailure(command, model, HomeworkAddCommand.MESSAGE_SUBJECT_NOT_TAKEN);
    }

    @Test
    public void execute_duplicateHomework_failure() {
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, false);

        assertStudentCommandFailure(command, model, HomeworkAddCommand.MESSAGE_DUPLICATE_HOMEWORK);
    }

    @Test
    public void execute_duplicateHomeworkDifferentTitleCase_failure() {
        Homework shoutedAlgebra = new HomeworkBuilder(ALGEBRA).withTitle("COMPLETE   algebra WORKSHEET").build();
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(1), shoutedAlgebra, false);

        assertStudentCommandFailure(command, model, HomeworkAddCommand.MESSAGE_DUPLICATE_HOMEWORK);
    }

    @Test
    public void execute_duplicateCompletedHomework_failure() {
        // status and score do not affect identity
        Homework assignedAtomicStructure = new HomeworkBuilder().withTitle("Revise atomic structure")
                .withSubject("CHEMISTRY").withDueDate("2026-10-18").build();
        HomeworkAddCommand command = new HomeworkAddCommand(Index.fromOneBased(1), assignedAtomicStructure, false);

        assertStudentCommandFailure(command, model, HomeworkAddCommand.MESSAGE_DUPLICATE_HOMEWORK);
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

        assertStudentCommandFailure(new HomeworkAddCommand(Index.fromOneBased(2), ALGEBRA, false), failingModel,
                HomeworkAddCommand.MESSAGE_INTERNAL_ERROR);
    }

    @Test
    public void equals() {
        Index firstIndex = Index.fromOneBased(1);
        HomeworkAddCommand addAlgebraCommand = new HomeworkAddCommand(firstIndex, ALGEBRA, false);

        // same object -> returns true
        assertTrue(addAlgebraCommand.equals(addAlgebraCommand));

        // same values -> returns true
        assertTrue(addAlgebraCommand.equals(new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, false)));

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
