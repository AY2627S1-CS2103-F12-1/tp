package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalStudents.getModelWithStudents;
import static seedu.address.testutil.TypicalStudents.getModelWithTypicalStudents;
import static seedu.address.testutil.TypicalStudents.getTypicalStudents;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.student.Student;

public class HomeworkListCommandTest {
    private final Model model = getModelWithTypicalStudents();
    private final Model expectedModel = getModelWithTypicalStudents();

    @Test
    public void constructor_nullStudentIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkListCommand(null));
    }

    @Test
    public void execute_studentWithManyHomework_listsInInsertionOrder() {
        String expectedMessage = "Listed 3 homework records for Alice Tan.\n"
                + "Homework for Alice Tan:\n"
                + "1. Complete algebra worksheet (MATH) - due 2026-10-15\n"
                + "2. Attempt mechanics questions 1-5 (PHYSICS) - due 2026-10-20\n"
                + "3. Revise atomic structure (CHEMISTRY) - due 2026-10-18";

        assertCommandSuccess(new HomeworkListCommand(Index.fromOneBased(1)), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_lastStudentWithOneHomework_listsSingleRecord() {
        String expectedMessage = "Listed 1 homework record for Carl Ng.\n"
                + "Homework for Carl Ng:\n"
                + "1. Attempt mechanics questions 1-5 (PHYSICS) - due 2026-10-20";

        assertCommandSuccess(new HomeworkListCommand(Index.fromOneBased(3)), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_studentWithoutHomework_showsNoHomeworkFound() {
        assertCommandSuccess(new HomeworkListCommand(Index.fromOneBased(2)), model,
                "No homework found for Benson Lim.", expectedModel);
    }

    @Test
    public void execute_studentIndexAboveListSize_failure() {
        assertCommandFailure(new HomeworkListCommand(Index.fromOneBased(4)), model,
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyRoster_failure() {
        assertCommandFailure(new HomeworkListCommand(Index.fromOneBased(1)), getModelWithStudents(List.of()),
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
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
                -> new HomeworkListCommand(Index.fromOneBased(1)).execute(failingModel));
        // ModelManager#equals compares the stored rosters directly, not through getStudentList()
        assertEquals(expectedModel, failingModel);
    }

    @Test
    public void equals() {
        HomeworkListCommand listFirstCommand = new HomeworkListCommand(Index.fromOneBased(1));

        // same object -> returns true
        assertTrue(listFirstCommand.equals(listFirstCommand));

        // same values -> returns true
        assertTrue(listFirstCommand.equals(new HomeworkListCommand(Index.fromOneBased(1))));

        // null -> returns false
        assertFalse(listFirstCommand.equals(null));

        // different types -> returns false
        assertFalse(listFirstCommand.equals(1));

        // different student index -> returns false
        assertFalse(listFirstCommand.equals(new HomeworkListCommand(Index.fromOneBased(2))));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        String expected = HomeworkListCommand.class.getCanonicalName() + "{studentIndex=" + index + "}";
        assertEquals(expected, new HomeworkListCommand(index).toString());
    }
}
