package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentFilterPredicate;
import seedu.address.model.student.TuitionSubject;
import seedu.address.testutil.StudentBuilder;

public class FilterCommandTest {
    private final Student first = new StudentBuilder().withName("Alice Tan").withAcademicLevel("S4")
            .withSubjects("MATH,PHYSICS").build();
    private final Student second = new StudentBuilder().withName("Ben Lim").withAcademicLevel("S4")
            .withSubjects("CHEMISTRY").build();
    private final Student third = new StudentBuilder().withName("Cara Ng").withAcademicLevel("J1").build();

    @Test
    public void execute_levelFilter_preservesSourceAndReportsCount() throws Exception {
        ModelManager model = createModel();
        AddressBook before = new AddressBook(model.getAddressBook());

        CommandResult result = new FilterCommand(new StudentFilterPredicate(
                Optional.of(AcademicLevel.S4), Optional.empty())).execute(model);

        assertEquals(List.of(first, second), model.getStudentList());
        assertEquals("Found 2 students.", result.getFeedbackToUser());
        assertTrue(result.isShowStudentList());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_combinedFilter_reportsSingleMatch() throws Exception {
        ModelManager model = createModel();

        CommandResult result = new FilterCommand(new StudentFilterPredicate(
                Optional.of(AcademicLevel.S4), Optional.of(TuitionSubject.MATH))).execute(model);

        assertEquals(List.of(first), model.getStudentList());
        assertEquals("Found 1 student.", result.getFeedbackToUser());
    }

    @Test
    public void execute_noMatchesAndEmptyRoster_reportsEmptyResults() throws Exception {
        FilterCommand command = new FilterCommand(new StudentFilterPredicate(
                Optional.of(AcademicLevel.S5), Optional.empty()));
        for (ModelManager model : List.of(createModel(), new ModelManager())) {
            AddressBook before = new AddressBook(model.getAddressBook());

            CommandResult result = command.execute(model);

            assertEquals(List.of(), model.getStudentList());
            assertEquals("No students match the selected filters", result.getFeedbackToUser());
            assertEquals(before, model.getAddressBook());
        }
    }

    @Test
    public void execute_afterSearchAndFilter_alwaysUsesFullRoster() throws Exception {
        ModelManager model = createModel();
        new SearchCommand("Alice").execute(model);

        new FilterCommand(new StudentFilterPredicate(Optional.empty(), Optional.of(TuitionSubject.MATH)))
                .execute(model);
        assertEquals(List.of(first, third), model.getStudentList());

        new FilterCommand(new StudentFilterPredicate(Optional.empty(), Optional.of(TuitionSubject.CHEMISTRY)))
                .execute(model);
        assertEquals(List.of(second), model.getStudentList());

        new ListCommand().execute(model);
        assertEquals(List.of(first, second, third), model.getStudentList());
    }

    @Test
    public void execute_unexpectedFailure_logsCauseOnceWithoutExposingDetails() {
        RuntimeException failure = new IllegalStateException("Technical details");
        ModelManager model = new ModelManager() {
            @Override
            public void updateFilteredStudentList(Predicate<Student> predicate) {
                throw failure;
            }
        };
        FilterCommand command = new FilterCommand(new StudentFilterPredicate(
                Optional.of(AcademicLevel.S4), Optional.empty()));
        List<LogRecord> records = new ArrayList<>();
        Handler handler = createRecordingHandler(records);
        Logger logger = LogsCenter.getLogger(FilterCommand.class);
        logger.addHandler(handler);
        try {
            CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));

            assertEquals("TutorFlow could not display the filtered results due to an internal error",
                    exception.getMessage());
            assertSame(failure, exception.getCause());
            assertEquals(1, records.size());
            assertEquals(Level.WARNING, records.getFirst().getLevel());
            assertSame(failure, records.getFirst().getThrown());
            assertTrue(model.getAddressBook().getStudentList().isEmpty());
        } finally {
            logger.removeHandler(handler);
        }
    }

    private ModelManager createModel() {
        ModelManager model = new ModelManager();
        List.of(first, second, third).forEach(model::addStudent);
        return model;
    }

    private Handler createRecordingHandler(List<LogRecord> records) {
        return new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
    }
}
