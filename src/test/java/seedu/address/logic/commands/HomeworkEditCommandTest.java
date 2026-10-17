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

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.HomeworkEditCommand.EditHomeworkDescriptor;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.homework.DueDate;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.Title;
import seedu.address.model.student.Student;
import seedu.address.model.student.TuitionSubject;
import seedu.address.testutil.HomeworkBuilder;

public class HomeworkEditCommandTest {
    private static final Index FIRST = Index.fromOneBased(1);
    private static final Index SECOND = Index.fromOneBased(2);
    private static final Index THIRD = Index.fromOneBased(3);
    private static final EditHomeworkDescriptor NEW_TITLE =
            new EditHomeworkDescriptor(new Title("Complete algebra questions 1-10"), null, null);

    private final Model model = getModelWithTypicalStudents();

    @Test
    public void constructor_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkEditCommand(null, FIRST, NEW_TITLE, false));
        assertThrows(NullPointerException.class, () -> new HomeworkEditCommand(FIRST, null, NEW_TITLE, false));
        assertThrows(NullPointerException.class, () -> new HomeworkEditCommand(FIRST, FIRST, null, false));
    }

    @Test
    public void execute_titleOnly_editsTitleAndShowsList() {
        Homework editedAlgebra = new HomeworkBuilder(ALGEBRA).withTitle("Complete algebra questions 1-10").build();
        String expectedMessage = "Edited homework:\n"
                + "Alice Tan; Title: Complete algebra questions 1-10; Subject: MATH; Due: 2026-10-15\n"
                + "Listed 3 homework records for Alice Tan.\n"
                + "Homework for Alice Tan:\n"
                + "1. Complete algebra questions 1-10 (MATH) - due 2026-10-15\n"
                + "2. Attempt mechanics questions 1-5 (PHYSICS) - due 2026-10-20\n"
                + "3. Revise atomic structure (CHEMISTRY) - due 2026-10-18";
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(editedAlgebra, MECHANICS, ATOMIC_STRUCTURE)), BENSON, CARL));

        assertCommandSuccess(new HomeworkEditCommand(FIRST, FIRST, NEW_TITLE, false), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_allFields_editsAllFieldsAndKeepsPosition() {
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(new Title("Revise mechanics"),
                TuitionSubject.CHEMISTRY, DueDate.parseFullDate("2026-11-10"));
        Homework editedMechanics = new HomeworkBuilder().withTitle("Revise mechanics").withSubject("CHEMISTRY")
                .withDueDate("2026-11-10").build();
        String expectedMessage = "Edited homework:\n"
                + "Alice Tan; Title: Revise mechanics; Subject: CHEMISTRY; Due: 2026-11-10\n"
                + "Listed 3 homework records for Alice Tan.\n"
                + "Homework for Alice Tan:\n"
                + "1. Complete algebra worksheet (MATH) - due 2026-10-15\n"
                + "2. Revise mechanics (CHEMISTRY) - due 2026-11-10\n"
                + "3. Revise atomic structure (CHEMISTRY) - due 2026-10-18";
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(ALGEBRA, editedMechanics, ATOMIC_STRUCTURE)), BENSON, CARL));

        assertCommandSuccess(new HomeworkEditCommand(FIRST, SECOND, descriptor, false), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_dueDateOfScoredHomework_keepsScore() {
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(null, null,
                DueDate.parseFullDate("2026-11-05"));
        Homework editedAtomicStructure = new HomeworkBuilder(ATOMIC_STRUCTURE).withDueDate("2026-11-05").build();
        String expectedMessage = "Edited homework:\n"
                + "Alice Tan; Title: Revise atomic structure; Subject: CHEMISTRY; Due: 2026-11-05\n"
                + "Listed 3 homework records for Alice Tan.\n"
                + "Homework for Alice Tan:\n"
                + "1. Complete algebra worksheet (MATH) - due 2026-10-15\n"
                + "2. Attempt mechanics questions 1-5 (PHYSICS) - due 2026-10-20\n"
                + "3. Revise atomic structure (CHEMISTRY) - due 2026-11-05";
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(ALGEBRA, MECHANICS, editedAtomicStructure)), BENSON, CARL));

        assertCommandSuccess(new HomeworkEditCommand(FIRST, THIRD, descriptor, false), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_yearInferred_showsInferredYear() {
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(null, null,
                DueDate.parseFullDate("2027-01-05"));
        Homework editedMechanics = new HomeworkBuilder(MECHANICS).withDueDate("2027-01-05").build();
        String expectedMessage = "Edited homework:\n"
                + "Carl Ng; Title: Attempt mechanics questions 1-5; Subject: PHYSICS; Due: 2027-01-05\n"
                + "Inferred year: 2027\n"
                + "Listed 1 homework record for Carl Ng.\n"
                + "Homework for Carl Ng:\n"
                + "1. Attempt mechanics questions 1-5 (PHYSICS) - due 2027-01-05";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON,
                CARL.withHomeworks(List.of(editedMechanics))));

        assertCommandSuccess(new HomeworkEditCommand(THIRD, FIRST, descriptor, true), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_titleCaseOnly_editsTitle() {
        // a title differing only in case is a change, and the homework is not a duplicate of itself
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(new Title("COMPLETE algebra worksheet"),
                null, null);
        Homework editedAlgebra = new HomeworkBuilder(ALGEBRA).withTitle("COMPLETE algebra worksheet").build();
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(editedAlgebra, MECHANICS, ATOMIC_STRUCTURE)), BENSON, CARL));
        String expectedMessage = "Edited homework:\n"
                + "Alice Tan; Title: COMPLETE algebra worksheet; Subject: MATH; Due: 2026-10-15\n"
                + "Listed 3 homework records for Alice Tan.\n"
                + "Homework for Alice Tan:\n"
                + "1. COMPLETE algebra worksheet (MATH) - due 2026-10-15\n"
                + "2. Attempt mechanics questions 1-5 (PHYSICS) - due 2026-10-20\n"
                + "3. Revise atomic structure (CHEMISTRY) - due 2026-10-18";

        assertCommandSuccess(new HomeworkEditCommand(FIRST, FIRST, descriptor, false), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_studentIndexAboveListSize_failure() {
        assertCommandFailure(new HomeworkEditCommand(Index.fromOneBased(4), FIRST, NEW_TITLE, false), model,
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyRoster_failure() {
        assertCommandFailure(new HomeworkEditCommand(FIRST, FIRST, NEW_TITLE, false),
                getModelWithStudents(List.of()), Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_bothIndicesOutOfRange_reportsStudentIndex() {
        assertCommandFailure(new HomeworkEditCommand(Index.fromOneBased(4), Index.fromOneBased(9), NEW_TITLE,
                false), model, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_studentWithoutHomework_failure() {
        assertCommandFailure(new HomeworkEditCommand(SECOND, FIRST, NEW_TITLE, false), model,
                HomeworkEditCommand.MESSAGE_NO_HOMEWORK);
    }

    @Test
    public void execute_homeworkIndexAboveListSize_failure() {
        assertCommandFailure(new HomeworkEditCommand(FIRST, Index.fromOneBased(4), NEW_TITLE, false), model,
                Messages.MESSAGE_INVALID_HOMEWORK_DISPLAYED_INDEX);
    }

    @Test
    public void execute_subjectNotTaken_failure() {
        // CARL takes PHYSICS only
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(null, TuitionSubject.MATH, null);

        assertCommandFailure(new HomeworkEditCommand(THIRD, FIRST, descriptor, false), model,
                HomeworkAddCommand.MESSAGE_SUBJECT_NOT_TAKEN);
    }

    @Test
    public void execute_identicalValues_failure() {
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(new Title("Complete algebra worksheet"),
                TuitionSubject.MATH, DueDate.parseFullDate("2026-10-15"));

        assertCommandFailure(new HomeworkEditCommand(FIRST, FIRST, descriptor, false), model,
                HomeworkEditCommand.MESSAGE_NO_CHANGES);
    }

    @Test
    public void execute_duplicateOfOtherHomework_failure() {
        // MECHANICS would become ALGEBRA, apart from the title case
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(new Title("COMPLETE ALGEBRA WORKSHEET"),
                TuitionSubject.MATH, DueDate.parseFullDate("2026-10-15"));

        assertCommandFailure(new HomeworkEditCommand(FIRST, SECOND, descriptor, false), model,
                HomeworkEditCommand.MESSAGE_DUPLICATE_HOMEWORK);
    }

    @Test
    public void execute_duplicateWithDifferentScore_failure() {
        // the score is not part of identity: ALGEBRA would become ATOMIC_STRUCTURE, which has a score
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(new Title("Revise atomic structure"),
                TuitionSubject.CHEMISTRY, DueDate.parseFullDate("2026-10-18"));

        assertCommandFailure(new HomeworkEditCommand(FIRST, FIRST, descriptor, false), model,
                HomeworkEditCommand.MESSAGE_DUPLICATE_HOMEWORK);
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

        assertCommandFailure(new HomeworkEditCommand(FIRST, FIRST, NEW_TITLE, false), failingModel,
                HomeworkEditCommand.MESSAGE_INTERNAL_ERROR);
    }

    @Test
    public void getSaveFailureMessage_anyDefaultMessage_returnsHomeworkMessage() {
        String expectedMessage = "The homework could not be edited because TutorFlow could not save the updated "
                + "data. No homework data was changed.";
        assertEquals(expectedMessage, new HomeworkEditCommand(FIRST, FIRST, NEW_TITLE, false)
                .getSaveFailureMessage("Could not save data"));
    }

    @Test
    public void applyTo_someFields_replacesOnlyThoseFields() {
        // no fields -> same homework
        assertEquals(ATOMIC_STRUCTURE, new EditHomeworkDescriptor(null, null, null).applyTo(ATOMIC_STRUCTURE));

        // subject only -> title, due date and score kept
        assertEquals(new HomeworkBuilder(ATOMIC_STRUCTURE).withSubject("PHYSICS").build(),
                new EditHomeworkDescriptor(null, TuitionSubject.PHYSICS, null).applyTo(ATOMIC_STRUCTURE));
    }

    @Test
    public void equals() {
        HomeworkEditCommand editCommand = new HomeworkEditCommand(FIRST, SECOND, NEW_TITLE, false);

        // same object -> returns true
        assertTrue(editCommand.equals(editCommand));

        // same values -> returns true
        assertTrue(editCommand.equals(new HomeworkEditCommand(Index.fromOneBased(1), Index.fromOneBased(2),
                new EditHomeworkDescriptor(new Title("Complete algebra questions 1-10"), null, null), false)));

        // null -> returns false
        assertFalse(editCommand.equals(null));

        // different types -> returns false
        assertFalse(editCommand.equals(1));

        // different student index -> returns false
        assertFalse(editCommand.equals(new HomeworkEditCommand(SECOND, SECOND, NEW_TITLE, false)));

        // different homework index -> returns false
        assertFalse(editCommand.equals(new HomeworkEditCommand(FIRST, FIRST, NEW_TITLE, false)));

        // different descriptor -> returns false
        assertFalse(editCommand.equals(new HomeworkEditCommand(FIRST, SECOND,
                new EditHomeworkDescriptor(null, TuitionSubject.MATH, null), false)));

        // different year inference -> returns false
        assertFalse(editCommand.equals(new HomeworkEditCommand(FIRST, SECOND, NEW_TITLE, true)));
    }

    @Test
    public void toStringMethod() {
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(null, null,
                DueDate.parse("11-05", LocalDate.of(2026, 10, 5)));
        String expected = HomeworkEditCommand.class.getCanonicalName() + "{studentIndex=" + FIRST
                + ", homeworkIndex=" + SECOND + ", descriptor=" + descriptor + ", isYearInferred=true}";
        assertEquals(expected, new HomeworkEditCommand(FIRST, SECOND, descriptor, true).toString());
    }
}
