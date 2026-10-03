package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.student.Student;
import seedu.address.testutil.StudentBuilder;

public class AddCommandTest {
    @Test
    public void constructor_nullStudent_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddCommand(null));
    }

    @Test
    public void execute_newStudent_addsOnceAndShowsFullSummary() throws CommandException {
        Student student = new StudentBuilder().build();
        ModelStub model = new ModelStub();

        CommandResult result = new AddCommand(student).execute(model);

        assertEquals("New student added: " + Messages.format(student), result.getFeedbackToUser());
        assertEquals(List.of(student), model.studentsAdded);
    }

    @Test
    public void execute_duplicateStudent_doesNotAdd() throws CommandException {
        Student original = new StudentBuilder().build();
        Student duplicate = new StudentBuilder().withName("john   tan").build();
        ModelStub model = new ModelStub();
        model.addStudent(original);

        CommandException exception = assertThrows(CommandException.class, () ->
                new AddCommand(duplicate).execute(model));

        assertEquals(AddCommand.MESSAGE_DUPLICATE_STUDENT, exception.getMessage());
        assertEquals(List.of(original), model.studentsAdded);
    }

    @Test
    public void equals_comparesStudentValues() {
        Student john = new StudentBuilder().build();
        Student mary = new StudentBuilder().withName("Mary Tan").build();
        AddCommand addJohn = new AddCommand(john);

        assertTrue(addJohn.equals(new AddCommand(john)));
        assertFalse(addJohn.equals(new AddCommand(mary)));
        assertFalse(addJohn.equals(null));
    }

    private static class ModelStub extends ModelManager {
        private final List<Student> studentsAdded = new ArrayList<>();

        @Override
        public boolean hasStudent(Student student) {
            return studentsAdded.stream().anyMatch(existing -> existing.isSameStudent(student));
        }

        @Override
        public void addStudent(Student student) {
            studentsAdded.add(student);
        }
    }
}
