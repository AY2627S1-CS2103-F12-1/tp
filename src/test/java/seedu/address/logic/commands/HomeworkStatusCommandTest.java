package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.CAUSE_READ_ONLY_FOLDER;
import static seedu.address.logic.commands.CommandTestUtil.SHOWN_DATA_FILE_NAME;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.homework.Score;
import seedu.address.model.student.Student;
import seedu.address.testutil.HomeworkBuilder;
import seedu.address.testutil.StudentBuilder;

public class HomeworkStatusCommandTest {
    private final AddressBookParser parser = new AddressBookParser(
            Clock.fixed(Instant.parse("2026-10-20T00:00:00Z"), ZoneOffset.UTC));

    @Test
    public void execute_completeAndReopen_preserveScoreAndOtherStudentData() throws Exception {
        List<Homework> records = List.of(new HomeworkBuilder().build(), new HomeworkBuilder().withScore("17").build());
        for (Homework homework : records) {
            ModelManager model = model(homework);
            Student before = model.getStudentList().getFirst();
            String result = execute(model, "hw status 1 1 status/COMPLETED");
            Homework completed = new Homework(homework.getTitle(), homework.getSubject(), homework.getDueDate(),
                    homework.getScore().orElse(null), HomeworkStatus.COMPLETED);
            assertEquals(before.withHomeworks(List.of(completed)), model.getStudentList().getFirst());
            assertTrue(result.endsWith("Status: COMPLETED"));
            assertTrue(execute(model, "hw filter 1 status/COMPLETED").contains("Homework 1:"));
            execute(model, "hw status 1 1 status/ASSIGNED");
            assertEquals(before, model.getStudentList().getFirst());
        }
    }

    @Test
    public void execute_marksAndDetailsEdits_preserveCompletionIndependently() throws Exception {
        ModelManager model = model(new HomeworkBuilder().withStatus("COMPLETED").build());
        execute(model, "hw marks 1 1 score/17/20");
        assertEquals(HomeworkStatus.COMPLETED, homework(model).getStatus());
        execute(model, "hw edit 1 1 t/Revised worksheet due/2026-11-01");
        assertEquals(HomeworkStatus.COMPLETED, homework(model).getStatus());
        assertEquals(Optional.of(new Score(17, 20)), homework(model).getScore());
        execute(model, "hw marks 1 1 score/");
        assertEquals(HomeworkStatus.COMPLETED, homework(model).getStatus());
        assertEquals(Optional.empty(), homework(model).getScore());
        execute(model, "hw status 1 1 status/ASSIGNED");
        execute(model, "hw marks 1 1 score/20/20");
        assertEquals(HomeworkStatus.ASSIGNED, homework(model).getStatus());
    }

    @Test
    public void execute_displayedStudentAndInsertionHomeworkIndices_preservedAfterSearchAndSort() throws Exception {
        ModelManager model = model(new HomeworkBuilder().build());
        Student first = model.getStudentList().getFirst();
        model.addStudent(new StudentBuilder().withName("Amy Lim").withPhone("92345678").withAcademicLevel("J1")
                .withHomeworks(new HomeworkBuilder().withTitle("Earlier").build(),
                        new HomeworkBuilder().withTitle("Later").build()).build());
        execute(model, "sort by/level o/desc");
        execute(model, "hw status 1 2 status/COMPLETED");
        assertEquals(first, model.getAddressBook().getStudentList().getFirst());
        assertEquals(HomeworkStatus.ASSIGNED, model.getStudentList().getFirst().getHomeworks().getFirst().getStatus());
        assertEquals(HomeworkStatus.COMPLETED, model.getStudentList().getFirst().getHomeworks().get(1).getStatus());
        execute(model, "search Lim");
        execute(model, "hw status 1 2 status/ASSIGNED");
        assertEquals(1, model.getStudentList().size());
        assertEquals(HomeworkStatus.ASSIGNED, model.getStudentList().getFirst().getHomeworks().get(1).getStatus());
    }

    @Test
    public void execute_invalidIndicesAndUnchangedStatus_rejectWithoutMutation() throws Exception {
        ModelManager model = model(new HomeworkBuilder().build());
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String command : List.of("hw status 1 1 status/ASSIGNED", "hw status 2 1 status/COMPLETED",
                "hw status 1 2 status/COMPLETED")) {
            assertThrows(CommandException.class, () -> execute(model, command));
            assertEquals(before, model.getAddressBook());
        }
        assertEquals(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX,
                assertThrows(CommandException.class, () -> execute(model, "hw status 2 1 status/COMPLETED"))
                        .getMessage());
        model.setStudent(model.getStudentList().getFirst(), model.getStudentList().getFirst().withHomeworks(List.of()));
        assertEquals(HomeworkStatusCommand.MESSAGE_NO_HOMEWORK,
                assertThrows(CommandException.class, () -> execute(model, "hw status 1 1 status/COMPLETED"))
                        .getMessage());
    }

    @Test
    public void execute_unexpectedModelFailure_reportsInternalError() throws Exception {
        ModelManager failing = new ModelManager() {
            @Override
            public ObservableList<Student> getStudentList() {
                throw new IllegalStateException("test failure");
            }
        };
        assertEquals(HomeworkStatusCommand.MESSAGE_INTERNAL,
                assertThrows(CommandException.class, () -> execute(failing, "hw status 1 1 status/COMPLETED"))
                        .getMessage());
    }

    @Test
    public void getSaveFailureMessage_cause_namesFileAndCause() throws Exception {
        String expectedMessage = "The homework status could not be updated because TutorFlow could not save "
                + SHOWN_DATA_FILE_NAME + ": the data folder is read-only. No homework data was changed.";
        assertEquals(expectedMessage, parser.parseCommand("hw status 1 1 status/COMPLETED")
                .getSaveFailureMessage(SHOWN_DATA_FILE_NAME, CAUSE_READ_ONLY_FOLDER));
    }

    private Homework homework(ModelManager model) {
        return model.getStudentList().getFirst().getHomeworks().getFirst();
    }

    private String execute(ModelManager model, String command) throws Exception {
        return parser.parseCommand(command).execute(model).getFeedbackToUser();
    }

    private ModelManager model(Homework homework) {
        ModelManager model = new ModelManager();
        model.addStudent(new StudentBuilder().withHomeworks(homework).build());
        return model;
    }
}
