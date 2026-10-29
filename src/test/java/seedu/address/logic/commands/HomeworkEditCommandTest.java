package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.CAUSE_READ_ONLY_FOLDER;
import static seedu.address.logic.commands.CommandTestUtil.SHOWN_DATA_FILE_NAME;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.model.util.SampleDataUtil.getSampleStudents;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestUtil.ARABIC_EGYPT;
import static seedu.address.testutil.TestUtil.runWithFormatLocale;
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
import seedu.address.logic.commands.HomeworkEditCommand.EditHomeworkDescriptor;
import seedu.address.model.Model;
import seedu.address.model.homework.DueDate;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.Title;
import seedu.address.model.student.Student;
import seedu.address.model.student.TuitionSubject;
import seedu.address.testutil.HomeworkBuilder;

public class HomeworkEditCommandTest {
    /** Date before every typical due date, so all typical homework is upcoming. */
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 5);
    private static final Index FIRST = Index.fromOneBased(1);
    private static final Index SECOND = Index.fromOneBased(2);
    private static final Index THIRD = Index.fromOneBased(3);
    private static final EditHomeworkDescriptor NEW_TITLE =
            new EditHomeworkDescriptor(new Title("Complete algebra questions 1-10"), null, null);

    private final Model model = getModelWithTypicalStudents();

    @Test
    public void constructor_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkEditCommand(null, FIRST, NEW_TITLE, false, TODAY));
        assertThrows(NullPointerException.class, () -> new HomeworkEditCommand(FIRST, null, NEW_TITLE, false, TODAY));
        assertThrows(NullPointerException.class, () -> new HomeworkEditCommand(FIRST, FIRST, null, false, TODAY));
        assertThrows(NullPointerException.class, () -> new HomeworkEditCommand(FIRST, FIRST, NEW_TITLE, false, null));
    }

    @Test
    public void execute_titleOnly_editsTitleAndShowsList() {
        Homework editedAlgebra = new HomeworkBuilder(ALGEBRA).withTitle("Complete algebra questions 1-10").build();
        String expectedMessage = "Edited homework:\n"
                + "Alice Tan (91111111); Title: Complete algebra questions 1-10; Subject: MATH; Due: 2026-10-15\n"
                + "Listed 3 upcoming homework records for Alice Tan (91111111).\n"
                + "Homework for Alice Tan (91111111):\n"
                + "1. Complete algebra questions 1-10 (MATH) - due 2026-10-15 - score Not recorded - status ASSIGNED\n"
                + "3. Revise atomic structure (CHEMISTRY) - due 2026-10-18 - score 17/20 - status ASSIGNED\n"
                + "2. Attempt mechanics questions 1-5 (PHYSICS) - due 2026-10-20 - score Not recorded"
                + " - status ASSIGNED";
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(editedAlgebra, MECHANICS, ATOMIC_STRUCTURE)), BENSON, CARL));

        assertCommandSuccess(new HomeworkEditCommand(FIRST, FIRST, NEW_TITLE, false, TODAY), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_allFields_editsAllFieldsAndKeepsPosition() {
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(new Title("Revise mechanics"),
                TuitionSubject.CHEMISTRY, DueDate.parseFullDate("2026-11-10"));
        Homework editedMechanics = new HomeworkBuilder().withTitle("Revise mechanics").withSubject("CHEMISTRY")
                .withDueDate("2026-11-10").build();
        String expectedMessage = "Edited homework:\n"
                + "Alice Tan (91111111); Title: Revise mechanics; Subject: CHEMISTRY; Due: 2026-11-10\n"
                + "Listed 3 upcoming homework records for Alice Tan (91111111).\n"
                + "Homework for Alice Tan (91111111):\n"
                + "1. Complete algebra worksheet (MATH) - due 2026-10-15 - score Not recorded - status ASSIGNED\n"
                + "3. Revise atomic structure (CHEMISTRY) - due 2026-10-18 - score 17/20 - status ASSIGNED\n"
                + "2. Revise mechanics (CHEMISTRY) - due 2026-11-10 - score Not recorded - status ASSIGNED";
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(ALGEBRA, editedMechanics, ATOMIC_STRUCTURE)), BENSON, CARL));

        assertCommandSuccess(new HomeworkEditCommand(FIRST, SECOND, descriptor, false, TODAY), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_dueDateOfScoredHomework_keepsScore() {
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(null, null,
                DueDate.parseFullDate("2026-11-05"));
        Homework editedAtomicStructure = new HomeworkBuilder(ATOMIC_STRUCTURE).withDueDate("2026-11-05").build();
        String expectedMessage = "Edited homework:\n"
                + "Alice Tan (91111111); Title: Revise atomic structure; Subject: CHEMISTRY; Due: 2026-11-05\n"
                + "Listed 3 upcoming homework records for Alice Tan (91111111).\n"
                + "Homework for Alice Tan (91111111):\n"
                + "1. Complete algebra worksheet (MATH) - due 2026-10-15 - score Not recorded - status ASSIGNED\n"
                + "2. Attempt mechanics questions 1-5 (PHYSICS) - due 2026-10-20 - score Not recorded"
                + " - status ASSIGNED\n"
                + "3. Revise atomic structure (CHEMISTRY) - due 2026-11-05 - score 17/20 - status ASSIGNED";
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(ALGEBRA, MECHANICS, editedAtomicStructure)), BENSON, CARL));

        assertCommandSuccess(new HomeworkEditCommand(FIRST, THIRD, descriptor, false, TODAY), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_yearInferred_showsInferredYear() {
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(null, null,
                DueDate.parseFullDate("2027-01-05"));
        Homework editedMechanics = new HomeworkBuilder(MECHANICS).withDueDate("2027-01-05").build();
        String expectedMessage = "Edited homework:\n"
                + "Carl Ng (93333333); Title: Attempt mechanics questions 1-5; Subject: PHYSICS; Due: 2027-01-05\n"
                + "Inferred year: 2027\n"
                + "Listed 1 upcoming homework record for Carl Ng (93333333).\n"
                + "Homework for Carl Ng (93333333):\n"
                + "1. Attempt mechanics questions 1-5 (PHYSICS) - due 2027-01-05 - score Not recorded"
                + " - status ASSIGNED";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON,
                CARL.withHomeworks(List.of(editedMechanics))));

        assertCommandSuccess(new HomeworkEditCommand(THIRD, FIRST, descriptor, true, TODAY), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_yearInferredArabicDigitsLocale_showsAsciiYearAndIndices() {
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(null, null,
                DueDate.parseFullDate("2027-01-05"));
        HomeworkEditCommand command = new HomeworkEditCommand(THIRD, FIRST, descriptor, true, TODAY);

        runWithFormatLocale(ARABIC_EGYPT, () -> {
            String feedback = assertDoesNotThrow(() -> command.execute(model)).getFeedbackToUser();
            assertTrue(feedback.contains("Due: 2027-01-05\nInferred year: 2027\n"), feedback);
            assertTrue(feedback.endsWith("\n1. Attempt mechanics questions 1-5 (PHYSICS) - due 2027-01-05"
                    + " - score Not recorded - status ASSIGNED"), feedback);
        });
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
                + "Alice Tan (91111111); Title: COMPLETE algebra worksheet; Subject: MATH; Due: 2026-10-15\n"
                + "Listed 3 upcoming homework records for Alice Tan (91111111).\n"
                + "Homework for Alice Tan (91111111):\n"
                + "1. COMPLETE algebra worksheet (MATH) - due 2026-10-15 - score Not recorded - status ASSIGNED\n"
                + "3. Revise atomic structure (CHEMISTRY) - due 2026-10-18 - score 17/20 - status ASSIGNED\n"
                + "2. Attempt mechanics questions 1-5 (PHYSICS) - due 2026-10-20 - score Not recorded"
                + " - status ASSIGNED";

        assertCommandSuccess(new HomeworkEditCommand(FIRST, FIRST, descriptor, false, TODAY), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_overdueHomework_listsUpcomingHomeworkOnly() {
        // on 2026-10-16 ALGEBRA (due 2026-10-15) is overdue, so the list after the edit leaves it out and says so
        Homework editedAlgebra = new HomeworkBuilder(ALGEBRA).withTitle("Complete algebra questions 1-10").build();
        String expectedMessage = "Edited homework:\n"
                + "Alice Tan (91111111); Title: Complete algebra questions 1-10; Subject: MATH; Due: 2026-10-15\n"
                + "Listed 2 upcoming homework records for Alice Tan (91111111)"
                + " (1 record due before today not shown; use homework list 1 all to see it).\n"
                + "Homework for Alice Tan (91111111):\n"
                + "3. Revise atomic structure (CHEMISTRY) - due 2026-10-18 - score 17/20 - status ASSIGNED\n"
                + "2. Attempt mechanics questions 1-5 (PHYSICS) - due 2026-10-20 - score Not recorded"
                + " - status ASSIGNED";
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(editedAlgebra, MECHANICS, ATOMIC_STRUCTURE)), BENSON, CARL));

        assertCommandSuccess(new HomeworkEditCommand(FIRST, FIRST, NEW_TITLE, false, LocalDate.of(2026, 10, 16)),
                model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_earlierSampleHomework_hintsHiddenRecord() {
        // hw e 2 1 t/Research Organic pathways on 2026-11-06: Priya Nair's homework 1 (due 2026-11-02) is left
        // out, so the list starts at 2 and the header says why
        Student priya = getSampleStudents()[1];
        List<Homework> homeworks = priya.getHomeworks();
        Homework editedHomework = new HomeworkBuilder(homeworks.get(0)).withTitle("Research Organic pathways").build();
        Model sampleModel = getModelWithStudents(List.of(getSampleStudents()));
        Model expectedModel = getModelWithStudents(List.of(getSampleStudents()));
        expectedModel.setStudent(priya, priya.withHomeworks(List.of(editedHomework, homeworks.get(1),
                homeworks.get(2))));
        String expectedMessage = "Edited homework:\n"
                + "Priya Nair (82345671); Title: Research Organic pathways; Subject: MATH; Due: 2026-11-02\n"
                + "Listed 2 upcoming homework records for Priya Nair (82345671)"
                + " (1 record due before today not shown; use homework list 2 all to see it).\n"
                + "Homework for Priya Nair (82345671):\n"
                + "2. Organic reaction pathways map (CHEMISTRY) - due 2026-11-09 - score Not recorded"
                + " - status ASSIGNED\n"
                + "3. Chemical equilibrium past paper (CHEMISTRY) - due 2026-11-23 - score Not recorded"
                + " - status ASSIGNED";
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(new Title("Research Organic pathways"),
                null, null);

        assertCommandSuccess(new HomeworkEditCommand(SECOND, FIRST, descriptor, false, LocalDate.of(2026, 11, 6)),
                sampleModel, expectedMessage, expectedModel);
    }

    @Test
    public void execute_dueDateMovedToPast_showsNoUpcomingHomework() {
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(null, null,
                DueDate.parseFullDate("2026-10-01"));
        Homework editedMechanics = new HomeworkBuilder(MECHANICS).withDueDate("2026-10-01").build();
        String expectedMessage = "Edited homework:\n"
                + "Carl Ng (93333333); Title: Attempt mechanics questions 1-5; Subject: PHYSICS; Due: 2026-10-01\n"
                + "No upcoming homework found for Carl Ng (93333333)"
                + " (1 record due before today not shown; use homework list 3 all to see it).";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON,
                CARL.withHomeworks(List.of(editedMechanics))));

        assertCommandSuccess(new HomeworkEditCommand(THIRD, FIRST, descriptor, false, TODAY), model,
                expectedMessage, expectedModel);
    }

    @Test
    public void execute_onlyEarlierDueDateMovedToFuture_removesHiddenNote() {
        // on 2026-10-16 only ALGEBRA (due 2026-10-15) is due before today, so after the edit nothing is left out
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(null, null,
                DueDate.parseFullDate("2026-10-30"));
        Homework editedAlgebra = new HomeworkBuilder(ALGEBRA).withDueDate("2026-10-30").build();
        String expectedMessage = "Edited homework:\n"
                + "Alice Tan (91111111); Title: Complete algebra worksheet; Subject: MATH; Due: 2026-10-30\n"
                + "Listed 3 upcoming homework records for Alice Tan (91111111).\n"
                + "Homework for Alice Tan (91111111):\n"
                + "3. Revise atomic structure (CHEMISTRY) - due 2026-10-18 - score 17/20 - status ASSIGNED\n"
                + "2. Attempt mechanics questions 1-5 (PHYSICS) - due 2026-10-20 - score Not recorded"
                + " - status ASSIGNED\n"
                + "1. Complete algebra worksheet (MATH) - due 2026-10-30 - score Not recorded - status ASSIGNED";
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(editedAlgebra, MECHANICS, ATOMIC_STRUCTURE)), BENSON, CARL));

        assertCommandSuccess(new HomeworkEditCommand(FIRST, FIRST, descriptor, false, LocalDate.of(2026, 10, 16)),
                model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_oneOfTwoEarlierDueDatesMovedToFuture_lowersHiddenCount() {
        // on 2026-10-19 ALGEBRA (due 2026-10-15) and ATOMIC_STRUCTURE (due 2026-10-18) are due before today
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(null, null,
                DueDate.parseFullDate("2026-10-30"));
        Homework editedAlgebra = new HomeworkBuilder(ALGEBRA).withDueDate("2026-10-30").build();
        String expectedMessage = "Edited homework:\n"
                + "Alice Tan (91111111); Title: Complete algebra worksheet; Subject: MATH; Due: 2026-10-30\n"
                + "Listed 2 upcoming homework records for Alice Tan (91111111)"
                + " (1 record due before today not shown; use homework list 1 all to see it).\n"
                + "Homework for Alice Tan (91111111):\n"
                + "2. Attempt mechanics questions 1-5 (PHYSICS) - due 2026-10-20 - score Not recorded"
                + " - status ASSIGNED\n"
                + "1. Complete algebra worksheet (MATH) - due 2026-10-30 - score Not recorded - status ASSIGNED";
        Model expectedModel = getModelWithStudents(List.of(
                ALICE.withHomeworks(List.of(editedAlgebra, MECHANICS, ATOMIC_STRUCTURE)), BENSON, CARL));

        assertCommandSuccess(new HomeworkEditCommand(FIRST, FIRST, descriptor, false, LocalDate.of(2026, 10, 19)),
                model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_studentIndexAboveListSize_failure() {
        assertCommandFailure(new HomeworkEditCommand(Index.fromOneBased(4), FIRST, NEW_TITLE, false, TODAY), model,
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_filteredList_editsDisplayedStudentHomework() {
        // CARL is third in the roster but first in the displayed list
        model.updateFilteredStudentList(CARL::equals);
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(new Title("Revise mechanics"), null, null);
        Homework editedMechanics = new HomeworkBuilder(MECHANICS).withTitle("Revise mechanics").build();
        String expectedMessage = "Edited homework:\n"
                + "Carl Ng (93333333); Title: Revise mechanics; Subject: PHYSICS; Due: 2026-10-20\n"
                + "Listed 1 upcoming homework record for Carl Ng (93333333).\n"
                + "Homework for Carl Ng (93333333):\n"
                + "1. Revise mechanics (PHYSICS) - due 2026-10-20 - score Not recorded - status ASSIGNED";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON,
                CARL.withHomeworks(List.of(editedMechanics))));

        assertCommandSuccess(new HomeworkEditCommand(FIRST, FIRST, descriptor, false, TODAY), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_earlierHiddenInFilteredList_hintsDisplayedIndex() {
        // CARL is third in the roster but first in the displayed list, and his only homework is due before 2026-10-21
        model.updateFilteredStudentList(CARL::equals);
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(new Title("Revise mechanics"), null, null);
        Homework editedMechanics = new HomeworkBuilder(MECHANICS).withTitle("Revise mechanics").build();
        String expectedMessage = "Edited homework:\n"
                + "Carl Ng (93333333); Title: Revise mechanics; Subject: PHYSICS; Due: 2026-10-20\n"
                + "No upcoming homework found for Carl Ng (93333333)"
                + " (1 record due before today not shown; use homework list 1 all to see it).";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON,
                CARL.withHomeworks(List.of(editedMechanics))));

        assertCommandSuccess(new HomeworkEditCommand(FIRST, FIRST, descriptor, false, LocalDate.of(2026, 10, 21)),
                model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_indexBeyondFilteredList_failure() {
        model.updateFilteredStudentList(CARL::equals);

        assertCommandFailure(new HomeworkEditCommand(SECOND, FIRST, NEW_TITLE, false, TODAY), model,
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_completedHomework_keepsStatus() {
        Model completedModel = getModelWithStudents(List.of(ALICE, BENSON,
                CARL.withHomeworks(List.of(COMPLETED_MECHANICS))));
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(new Title("Revise mechanics"), null, null);
        Homework editedMechanics = new HomeworkBuilder(COMPLETED_MECHANICS).withTitle("Revise mechanics").build();
        String expectedMessage = "Edited homework:\n"
                + "Carl Ng (93333333); Title: Revise mechanics; Subject: PHYSICS; Due: 2026-10-20\n"
                + "Listed 1 upcoming homework record for Carl Ng (93333333).\n"
                + "Homework for Carl Ng (93333333):\n"
                + "1. Revise mechanics (PHYSICS) - due 2026-10-20 - score Not recorded - status COMPLETED";
        Model expectedModel = getModelWithStudents(List.of(ALICE, BENSON,
                CARL.withHomeworks(List.of(editedMechanics))));

        assertCommandSuccess(new HomeworkEditCommand(THIRD, FIRST, descriptor, false, TODAY), completedModel,
                expectedMessage, expectedModel);
    }

    @Test
    public void execute_emptyRoster_failure() {
        assertCommandFailure(new HomeworkEditCommand(FIRST, FIRST, NEW_TITLE, false, TODAY),
                getModelWithStudents(List.of()), Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_bothIndicesOutOfRange_reportsStudentIndex() {
        assertCommandFailure(new HomeworkEditCommand(Index.fromOneBased(4), Index.fromOneBased(9), NEW_TITLE,
                false, TODAY), model, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_studentWithoutHomework_failure() {
        assertCommandFailure(new HomeworkEditCommand(SECOND, FIRST, NEW_TITLE, false, TODAY), model,
                HomeworkEditCommand.MESSAGE_NO_HOMEWORK);
    }

    @Test
    public void execute_homeworkIndexAboveListSize_failure() {
        assertCommandFailure(new HomeworkEditCommand(FIRST, Index.fromOneBased(4), NEW_TITLE, false, TODAY), model,
                Messages.MESSAGE_INVALID_HOMEWORK_DISPLAYED_INDEX);
    }

    @Test
    public void execute_subjectNotTaken_failure() {
        // CARL takes PHYSICS only
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(null, TuitionSubject.MATH, null);

        assertCommandFailure(new HomeworkEditCommand(THIRD, FIRST, descriptor, false, TODAY), model,
                HomeworkAddCommand.MESSAGE_SUBJECT_NOT_TAKEN);
    }

    @Test
    public void execute_identicalValues_failure() {
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(new Title("Complete algebra worksheet"),
                TuitionSubject.MATH, DueDate.parseFullDate("2026-10-15"));

        assertCommandFailure(new HomeworkEditCommand(FIRST, FIRST, descriptor, false, TODAY), model,
                HomeworkEditCommand.MESSAGE_NO_CHANGES);
    }

    @Test
    public void execute_duplicateOfOtherHomework_failure() {
        // MECHANICS would become ALGEBRA, apart from the title case
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(new Title("COMPLETE ALGEBRA WORKSHEET"),
                TuitionSubject.MATH, DueDate.parseFullDate("2026-10-15"));

        assertCommandFailure(new HomeworkEditCommand(FIRST, SECOND, descriptor, false, TODAY), model,
                HomeworkEditCommand.MESSAGE_DUPLICATE_HOMEWORK);
    }

    @Test
    public void execute_duplicateInDifferentUnicodeForm_failure() {
        // ALGEBRA would get the title of the first homework, with the accent written as a combining character
        Homework composedCafe = new HomeworkBuilder().withTitle("Caf\u00E9").withDueDate("2026-10-15").build();
        Model modelWithCafe = getModelWithStudents(List.of(ALICE.withHomeworks(List.of(composedCafe, ALGEBRA)),
                BENSON, CARL));
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(new Title("Cafe\u0301"), null, null);

        assertCommandFailure(new HomeworkEditCommand(FIRST, SECOND, descriptor, false, TODAY), modelWithCafe,
                HomeworkEditCommand.MESSAGE_DUPLICATE_HOMEWORK);
    }

    @Test
    public void execute_duplicateWithDifferentScore_failure() {
        // the score is not part of identity: ALGEBRA would become ATOMIC_STRUCTURE, which has a score
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(new Title("Revise atomic structure"),
                TuitionSubject.CHEMISTRY, DueDate.parseFullDate("2026-10-18"));

        assertCommandFailure(new HomeworkEditCommand(FIRST, FIRST, descriptor, false, TODAY), model,
                HomeworkEditCommand.MESSAGE_DUPLICATE_HOMEWORK);
    }

    @Test
    public void execute_unexpectedError_reportsInternalErrorWithoutChangingModel() {
        Model failingModel = getModelFailingOnSetStudent();

        assertCommandFailure(new HomeworkEditCommand(FIRST, FIRST, NEW_TITLE, false, TODAY), failingModel,
                HomeworkEditCommand.MESSAGE_INTERNAL_ERROR);
    }

    @Test
    public void getSaveFailureMessage_cause_namesFileAndCause() {
        String expectedMessage = "The homework could not be edited because TutorFlow could not save "
                + SHOWN_DATA_FILE_NAME + ": the data folder is read-only. No homework data was changed.";
        assertEquals(expectedMessage, new HomeworkEditCommand(FIRST, FIRST, NEW_TITLE, false,
                TODAY).getSaveFailureMessage(SHOWN_DATA_FILE_NAME, CAUSE_READ_ONLY_FOLDER));
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
        HomeworkEditCommand editCommand = new HomeworkEditCommand(FIRST, SECOND, NEW_TITLE, false, TODAY);

        // same object -> returns true
        assertTrue(editCommand.equals(editCommand));

        // same values -> returns true
        HomeworkEditCommand editCommandCopy = new HomeworkEditCommand(Index.fromOneBased(1), Index.fromOneBased(2),
                new EditHomeworkDescriptor(new Title("Complete algebra questions 1-10"), null, null), false, TODAY);
        assertTrue(editCommand.equals(editCommandCopy));
        assertEquals(editCommand.hashCode(), editCommandCopy.hashCode());

        // null -> returns false
        assertFalse(editCommand.equals(null));

        // different types -> returns false
        assertFalse(editCommand.equals(1));

        // different student index -> returns false
        assertFalse(editCommand.equals(new HomeworkEditCommand(SECOND, SECOND, NEW_TITLE, false, TODAY)));

        // different homework index -> returns false
        assertFalse(editCommand.equals(new HomeworkEditCommand(FIRST, FIRST, NEW_TITLE, false, TODAY)));

        // different descriptor -> returns false
        assertFalse(editCommand.equals(new HomeworkEditCommand(FIRST, SECOND,
                new EditHomeworkDescriptor(null, TuitionSubject.MATH, null), false, TODAY)));

        // different year inference -> returns false
        assertFalse(editCommand.equals(new HomeworkEditCommand(FIRST, SECOND, NEW_TITLE, true, TODAY)));

        // different date -> returns false
        assertFalse(editCommand.equals(new HomeworkEditCommand(FIRST, SECOND, NEW_TITLE, false,
                LocalDate.of(2026, 10, 6))));
    }

    @Test
    public void toStringMethod() {
        EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(null, null,
                DueDate.parse("11-05", TODAY));
        String expected = HomeworkEditCommand.class.getCanonicalName() + "{studentIndex=" + FIRST
                + ", homeworkIndex=" + SECOND + ", descriptor=" + descriptor + ", isYearInferred=true, today="
                + TODAY + "}";
        assertEquals(expected, new HomeworkEditCommand(FIRST, SECOND, descriptor, true, TODAY).toString());
    }
}
