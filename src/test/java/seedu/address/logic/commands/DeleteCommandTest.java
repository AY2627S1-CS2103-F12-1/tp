package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.student.Student;
import seedu.address.testutil.StudentBuilder;

/** Tests deletion from the in-memory student roster. */
public class DeleteCommandTest {

    @Test
    public void execute_validIndex_deletesStudentAndUpdatesDisplayedIndexes() throws CommandException {
        ModelManager model = new ModelManager();
        Student first = new StudentBuilder().build();
        Student second = new StudentBuilder().withName("Jane Tan").build();
        model.addStudent(first);
        model.addStudent(second);

        CommandResult result = new DeleteCommand(INDEX_FIRST_PERSON).execute(model);

        assertEquals(String.format(DeleteCommand.MESSAGE_DELETE_STUDENT_SUCCESS, Messages.format(first)),
                result.getFeedbackToUser());
        assertEquals(List.of(second), model.getStudentList());

        // The remaining student is now shown at index 1.
        new DeleteCommand(INDEX_FIRST_PERSON).execute(model);
        assertTrue(model.getStudentList().isEmpty());
    }

    @Test
    public void execute_secondIndex_deletesOnlySecondStudent() throws CommandException {
        ModelManager model = new ModelManager();
        Student first = new StudentBuilder().build();
        Student second = new StudentBuilder().withName("Jane Tan").build();
        model.addStudent(first);
        model.addStudent(second);

        new DeleteCommand(INDEX_SECOND_PERSON).execute(model);

        assertEquals(List.of(first), model.getStudentList());
    }

    @Test
    public void execute_emptyRoster_throwsCommandException() {
        ModelManager model = new ModelManager();

        CommandException exception = assertThrows(CommandException.class, () ->
                new DeleteCommand(INDEX_FIRST_PERSON).execute(model));

        assertEquals(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX, exception.getMessage());
        assertTrue(model.getStudentList().isEmpty());
    }

    @Test
    public void execute_outOfRangeIndex_keepsRosterUnchanged() {
        ModelManager model = new ModelManager();
        Student student = new StudentBuilder().build();
        model.addStudent(student);

        CommandException exception = assertThrows(CommandException.class, () ->
                new DeleteCommand(INDEX_SECOND_PERSON).execute(model));

        assertEquals(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX, exception.getMessage());
        assertEquals(List.of(student), model.getStudentList());
    }

    @Test
    public void execute_validIndex_doesNotDeleteAddressBookPerson() throws CommandException {
        ModelManager model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Student student = new StudentBuilder().build();
        model.addStudent(student);
        int personCount = model.getFilteredPersonList().size();

        new DeleteCommand(INDEX_FIRST_PERSON).execute(model);

        assertTrue(model.getStudentList().isEmpty());
        assertEquals(personCount, model.getFilteredPersonList().size());
    }

    @Test
    public void equals() {
        DeleteCommand deleteFirstCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        DeleteCommand deleteSecondCommand = new DeleteCommand(INDEX_SECOND_PERSON);

        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));
        assertTrue(deleteFirstCommand.equals(new DeleteCommand(INDEX_FIRST_PERSON)));
        assertFalse(deleteFirstCommand.equals(1));
        assertFalse(deleteFirstCommand.equals(null));
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));
    }

    @Test
    public void toStringMethod() {
        Index targetIndex = Index.fromOneBased(1);
        DeleteCommand deleteCommand = new DeleteCommand(targetIndex);
        String expected = DeleteCommand.class.getCanonicalName() + "{targetIndex=" + targetIndex + "}";
        assertEquals(expected, deleteCommand.toString());
    }
}
