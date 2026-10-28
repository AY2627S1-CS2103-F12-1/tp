package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.CAUSE_READ_ONLY_FOLDER;
import static seedu.address.logic.commands.CommandTestUtil.SHOWN_DATA_FILE_NAME;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalRegularLessons.MONDAY_MATH;
import static seedu.address.testutil.TypicalRegularLessons.SATURDAY_CHEMISTRY;
import static seedu.address.testutil.TypicalRegularLessons.TUESDAY_PHYSICS;
import static seedu.address.testutil.TypicalStudents.getModelWithStudents;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.student.Student;
import seedu.address.testutil.StudentBuilder;

public class LessonDeleteCommandTest {
    @Test
    public void constructor_nullLessonIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LessonDeleteCommand(null));
    }

    @Test
    public void execute_scheduleIndex_deletesCorrectLesson() {
        Student amy = new StudentBuilder().withName("Amy Lim").withSubjects("PHYSICS, CHEMISTRY")
                .withLessons(TUESDAY_PHYSICS, SATURDAY_CHEMISTRY).build();
        Student ben = new StudentBuilder().withName("Ben Tan").withLessons(MONDAY_MATH).build();
        Model model = getModelWithStudents(List.of(amy, ben));
        LessonDeleteCommand deleteCommand = new LessonDeleteCommand(Index.fromOneBased(2));

        String expectedMessage = "Deleted regular lesson:\n"
                + "Amy Lim (91234567); PHYSICS; TUE; 19:00-20:30";
        Model expectedModel = getModelWithStudents(List.of(
                amy.withLessons(List.of(SATURDAY_CHEMISTRY)), ben));

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_lastLesson_leavesEmptyLessonList() {
        Student student = new StudentBuilder().withLessons(MONDAY_MATH).build();
        Model model = getModelWithStudents(List.of(student));

        assertCommandSuccess(new LessonDeleteCommand(Index.fromOneBased(1)), model,
                "Deleted regular lesson:\nJohn Tan (91234567); MATH; MON; 17:00-18:30",
                getModelWithStudents(List.of(student.withLessons(List.of()))));
    }

    @Test
    public void execute_filteredStudentList_deletesLessonAtFullScheduleIndex() {
        Student john = new StudentBuilder().withLessons(MONDAY_MATH).build();
        Student amy = new StudentBuilder().withName("Amy Lim").withPhone("91234568")
                .withSubjects("PHYSICS").withLessons(TUESDAY_PHYSICS).build();
        Model model = getModelWithStudents(List.of(john, amy));
        model.updateFilteredStudentList(student -> student.equals(amy));

        assertCommandSuccess(new LessonDeleteCommand(Index.fromOneBased(1)), model,
                "Deleted regular lesson:\nJohn Tan (91234567); MATH; MON; 17:00-18:30",
                getModelWithStudents(List.of(john.withLessons(List.of()), amy)));
    }

    @Test
    public void execute_noLessons_failure() {
        Model model = getModelWithStudents(List.of(new StudentBuilder().build()));

        assertCommandFailure(new LessonDeleteCommand(Index.fromOneBased(1)), model,
                LessonDeleteCommand.MESSAGE_NO_LESSONS);
    }

    @Test
    public void execute_indexAboveScheduleSize_failure() {
        Student student = new StudentBuilder().withLessons(MONDAY_MATH).build();
        Model model = getModelWithStudents(List.of(student));

        assertCommandFailure(new LessonDeleteCommand(Index.fromOneBased(2)), model,
                Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_setStudentFails_reportsInternalError() {
        Model failingModel = new ModelManager() {
            @Override
            public void setStudent(Student target, Student editedStudent) {
                throw new IllegalStateException("dummy internal error");
            }
        };
        failingModel.addStudent(new StudentBuilder().withLessons(MONDAY_MATH).build());

        assertCommandFailure(new LessonDeleteCommand(Index.fromOneBased(1)), failingModel,
                LessonDeleteCommand.MESSAGE_INTERNAL_ERROR);
    }

    @Test
    public void getSaveFailureMessage_cause_namesFileAndCause() {
        LessonDeleteCommand deleteCommand = new LessonDeleteCommand(Index.fromOneBased(1));
        String expectedMessage = "The lesson could not be deleted because TutorFlow could not save "
                + SHOWN_DATA_FILE_NAME + ": the data folder is read-only. No lesson data was changed.";

        assertEquals(expectedMessage, deleteCommand.getSaveFailureMessage(SHOWN_DATA_FILE_NAME,
                CAUSE_READ_ONLY_FOLDER));
    }

    @Test
    public void equals() {
        LessonDeleteCommand deleteCommand = new LessonDeleteCommand(Index.fromOneBased(1));

        assertTrue(deleteCommand.equals(deleteCommand));
        assertTrue(deleteCommand.equals(new LessonDeleteCommand(Index.fromOneBased(1))));
        assertFalse(deleteCommand.equals(null));
        assertFalse(deleteCommand.equals(1));
        assertFalse(deleteCommand.equals(new LessonDeleteCommand(Index.fromOneBased(2))));
    }

    @Test
    public void hashCode_equalCommands_deduplicatesInHashSet() {
        LessonDeleteCommand firstCommand = new LessonDeleteCommand(Index.fromOneBased(1));
        LessonDeleteCommand equalCommand = new LessonDeleteCommand(Index.fromZeroBased(0));

        assertEquals(firstCommand, equalCommand);
        assertEquals(firstCommand.hashCode(), equalCommand.hashCode());
        assertEquals(1, new HashSet<>(List.of(firstCommand, equalCommand)).size());
    }

    @Test
    public void toString_includesDisplayedLessonIndex() {
        Index displayedLessonIndex = Index.fromOneBased(2);
        assertEquals(LessonDeleteCommand.class.getCanonicalName() + "{displayedLessonIndex="
                        + displayedLessonIndex + "}",
                new LessonDeleteCommand(displayedLessonIndex).toString());
    }
}
