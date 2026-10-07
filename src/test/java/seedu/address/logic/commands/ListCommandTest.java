package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;
import seedu.address.model.student.Student;
import seedu.address.testutil.StudentBuilder;

/**
 * Tests listing the in-memory student roster without modifying it.
 */
public class ListCommandTest {

    @Test
    public void execute_emptyRoster_showsEmptyMessage() {
        ModelManager model = new ModelManager();

        CommandResult result = new ListCommand().execute(model);

        assertEquals(ListCommand.MESSAGE_EMPTY, result.getFeedbackToUser());
        assertTrue(result.isShowStudentList());
        assertTrue(model.getStudentList().isEmpty());
    }

    @Test
    public void execute_studentsPresent_preservesRosterOrder() {
        ModelManager model = new ModelManager();
        Student first = new StudentBuilder().build();
        Student second = new StudentBuilder().withName("Jane Tan").build();
        model.addStudent(first);
        model.addStudent(second);

        CommandResult result = new ListCommand().execute(model);

        assertEquals("Listed 2 students.", result.getFeedbackToUser());
        assertEquals(first, model.getStudentList().get(0));
        assertEquals(second, model.getStudentList().get(1));
        assertTrue(result.isShowStudentList());
    }
}
