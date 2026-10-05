package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.student.Student;
import seedu.address.testutil.StudentBuilder;

/** Tests Add Student command interaction with the in-memory model. */
public class AddCommandIntegrationTest {
    @Test
    public void execute_newStudent_appendsToRoster() throws CommandException {
        ModelManager model = new ModelManager();
        Student first = new StudentBuilder().build();
        Student second = new StudentBuilder().withName("Nur Aisyah").withPhone("91112222").build();

        new AddCommand(first).execute(model);
        new AddCommand(second).execute(model);

        assertEquals(2, model.getStudentList().size());
        assertEquals(first, model.getStudentList().get(0));
        assertEquals(second, model.getStudentList().get(1));
    }

    @Test
    public void execute_duplicateStudent_keepsRosterUnchanged() throws CommandException {
        ModelManager model = new ModelManager();
        Student original = new StudentBuilder().build();
        Student duplicate = new StudentBuilder().withName("JOHN TAN").build();
        new AddCommand(original).execute(model);

        assertThrows(CommandException.class, () -> new AddCommand(duplicate).execute(model));

        assertEquals(1, model.getStudentList().size());
        assertEquals(original, model.getStudentList().getFirst());
    }
}
