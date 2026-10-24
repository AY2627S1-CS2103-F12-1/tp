package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalHomeworks.ATOMIC_STRUCTURE;
import static seedu.address.testutil.TypicalHomeworks.MECHANICS;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.student.Student;
import seedu.address.testutil.HomeworkBuilder;
import seedu.address.testutil.StudentBuilder;

public class HomeworkFilterCommandTest {
    private final AddressBookParser parser = new AddressBookParser(
            Clock.fixed(Instant.parse("2026-10-20T00:00:00Z"), ZoneOffset.UTC));

    @Test
    public void execute_statusIntersectsOtherCriteria_andNeverInfersCompletionFromScores() throws Exception {
        ModelManager model = new ModelManager();
        model.addStudent(new StudentBuilder().withHomeworks(
                new HomeworkBuilder().withTitle("Scored assigned").withScore("17/20").build(),
                new HomeworkBuilder().withTitle("Unscored completed").withStatus("COMPLETED").build(),
                new HomeworkBuilder().withTitle("Other date completed").withDueDate("2026-11-01")
                        .withStatus("COMPLETED").build()).build());
        String assigned = feedback(model, "hw filter status/ASSIGNED");
        assertTrue(assigned.startsWith("Found 1 matching homework record."));
        assertTrue(assigned.contains("Homework 1: Scored assigned"));
        String completed = feedback(model, "hw filter 1 s/MATH due/2026-10-15 status/COMPLETED");
        assertTrue(completed.startsWith("Found 1 matching homework record."));
        assertTrue(completed.contains("Homework 2: Unscored completed"));
        assertTrue(completed.contains("Score: Not recorded; Status: COMPLETED"));
        assertEquals(HomeworkFilterCommand.MESSAGE_NO_MATCHES,
                feedback(model, "hw filter status/ASSIGNED due/2026-11-01"));
    }

    @Test
    public void execute_unexpectedModelFailure_reportsInternalError() throws Exception {
        ModelManager failing = new ModelManager() {
            @Override
            public ObservableList<Student> getStudentList() {
                throw new IllegalStateException("test failure");
            }
        };
        var command = parser.parseCommand("hw filter s/MATH");
        assertEquals(HomeworkFilterCommand.MESSAGE_INTERNAL,
                assertThrows(CommandException.class, () -> command.execute(failing)).getMessage());
    }

    @Test
    public void execute_studentOnly_includesOverdueAndScoredRecordsWithOriginalIndices() throws Exception {
        ModelManager model = model();
        AddressBook before = new AddressBook(model.getAddressBook());
        assertEquals("Found 3 matching homework records."
                + "\nStudent 1: John Tan; Homework 1: Complete algebra worksheet; MATH; Due: 2026-10-15;"
                + " Score: Not recorded; Status: ASSIGNED"
                + "\nStudent 1: John Tan; Homework 2: Attempt mechanics questions 1-5; PHYSICS; Due: 2026-10-20;"
                + " Score: Not recorded; Status: ASSIGNED"
                + "\nStudent 1: John Tan; Homework 3: Revise atomic structure; CHEMISTRY; Due: 2026-10-18;"
                + " Score: 17/20; Status: ASSIGNED",
                feedback(model, "hw filter 1"));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_individualAndCombinedCriteria_matchExactly() throws Exception {
        ModelManager model = model();
        assertTrue(feedback(model, "hw filter s/MATH").startsWith("Found 2 matching homework records."));
        assertTrue(feedback(model, "hw filter due/2026-10-20").startsWith("Found 1 matching homework record."));
        String combined = feedback(model, "hw filter 1 s/CHEMISTRY due/2026-10-18");
        assertTrue(combined.startsWith("Found 1 matching homework record."));
        assertTrue(combined.contains("Homework 3:"));
        assertEquals(HomeworkFilterCommand.MESSAGE_NO_MATCHES, feedback(model, "hw filter 2 s/PHYSICS"));
        assertEquals(HomeworkFilterCommand.MESSAGE_NO_MATCHES,
                feedback(model, "hw filter 1 s/MATH due/2026-10-20"));
        assertEquals(feedback(model, "hw filter due/2026-10-20"), feedback(model, "hw filter due/10-20"));
    }

    @Test
    public void execute_searchAndSort_keepViewAndUseDisplayedStudentIndex() throws Exception {
        ModelManager model = model();
        parser.parseCommand("search Lim").execute(model);
        List<Student> view = List.copyOf(model.getStudentList());
        assertTrue(feedback(model, "hw filter 1").contains("Student 1: Mary Lim; Homework 1:"));
        assertEquals(HomeworkFilterCommand.MESSAGE_NO_MATCHES, feedback(model, "hw filter s/PHYSICS"));
        assertEquals(view, model.getStudentList());
        parser.parseCommand("list").execute(model);
        parser.parseCommand("sort by/level o/desc").execute(model);
        assertTrue(feedback(model, "hw filter 1").contains("Student 1: Mary Lim;"));
    }

    @Test
    public void execute_emptyResultsAndOutOfRangeIndex_doNotChangeModel() throws Exception {
        assertEquals(HomeworkFilterCommand.MESSAGE_NO_MATCHES, feedback(new ModelManager(), "hw filter s/MATH"));
        ModelManager model = model();
        AddressBook before = new AddressBook(model.getAddressBook());
        assertEquals(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX,
                assertThrows(CommandException.class, () -> feedback(model, "hw filter 3")).getMessage());
        assertEquals(before, model.getAddressBook());
    }

    private String feedback(ModelManager model, String command) throws Exception {
        return parser.parseCommand(command).execute(model).getFeedbackToUser();
    }

    private ModelManager model() {
        ModelManager model = new ModelManager();
        model.addStudent(new StudentBuilder().withSubjects("MATH, PHYSICS, CHEMISTRY")
                .withHomeworks(ALGEBRA, MECHANICS, ATOMIC_STRUCTURE).build());
        model.addStudent(new StudentBuilder().withName("Mary Lim").withPhone("91234568").withAcademicLevel("J1")
                .withHomeworks(ALGEBRA).build());
        return model;
    }
}
