package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.student.SortKey;
import seedu.address.model.student.SortOrder;
import seedu.address.model.student.Student;
import seedu.address.testutil.StudentBuilder;

public class SortCommandTest {
    private final Student junior = new StudentBuilder().withName("Junior Student").withAcademicLevel("J1").build();
    private final Student secondary = new StudentBuilder().withName("Secondary Student").withAcademicLevel("S1")
            .build();
    private final SortCommand command = new SortCommand(SortKey.LEVEL, SortOrder.ASCENDING);

    @Test
    public void execute_sortPreservesFilterAndStoredRoster() throws Exception {
        ModelManager model = new ModelManager();
        model.addStudent(junior);
        model.addStudent(secondary);
        Student hidden = new StudentBuilder().withName("Hidden Student").build();
        model.addStudent(hidden);
        model.updateFilteredStudentList(student -> student != hidden);
        AddressBook before = new AddressBook(model.getAddressBook());

        SortCommandResult result = assertInstanceOf(SortCommandResult.class, command.execute(model));

        assertEquals("Sorted 2 student(s) by academic level (ascending).", result.getFeedbackToUser());
        assertEquals(List.of(secondary, junior), model.getStudentList());
        assertEquals(before, model.getAddressBook());
        assertFalse(result.isShowStudentList());
        result.restorePreviousOrder();
        assertEquals(List.of(junior, secondary), model.getStudentList());
    }

    @Test
    public void execute_emptyAndSingleStudent_reportsSpecifiedFeedback() throws Exception {
        ModelManager model = new ModelManager();
        assertEquals("No students to sort.", command.execute(model).getFeedbackToUser());
        model.addStudent(junior);
        assertEquals("Sorted 1 student(s) by academic level (ascending).",
                command.execute(model).getFeedbackToUser());
        assertEquals(List.of(junior), model.getStudentList());
    }

    @Test
    public void execute_publicationFailure_restoresOrderAndLogsCauseOnce() {
        RuntimeException failure = new IllegalStateException("Injected publication failure");
        ModelManager model = new ModelManager() {
            private boolean shouldFail = true;

            @Override
            public void setStudentListOrder(List<Student> students) {
                super.setStudentListOrder(students);
                if (shouldFail) {
                    shouldFail = false;
                    throw failure;
                }
            }
        };
        model.addStudent(junior);
        model.addStudent(secondary);
        List<LogRecord> records = new ArrayList<>();
        Logger logger = LogsCenter.getLogger(SortCommand.class);
        Handler handler = new RecordingHandler(records);
        Level previousLevel = logger.getLevel();
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        try {
            CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));
            assertEquals(SortCommand.MESSAGE_INTERNAL_ERROR, exception.getMessage());
            assertSame(failure, exception.getCause());
            assertEquals(List.of(junior, secondary), model.getStudentList());
            List<LogRecord> failures = records.stream().filter(record -> record.getThrown() != null).toList();
            assertEquals(1, failures.size());
            assertEquals(Level.WARNING, failures.getFirst().getLevel());
            assertSame(failure, failures.getFirst().getThrown());
        } finally {
            logger.removeHandler(handler);
            logger.setLevel(previousLevel);
        }
    }

    @Test
    public void equals_comparesKeyAndOrder() {
        assertEquals(command, new SortCommand(SortKey.LEVEL, SortOrder.ASCENDING));
        assertFalse(command.equals(new SortCommand(SortKey.DAY, SortOrder.ASCENDING)));
        assertFalse(command.equals(new SortCommand(SortKey.LEVEL, SortOrder.DESCENDING)));
    }

    /** Records diagnostic events without reading or modifying application log files. */
    private static class RecordingHandler extends Handler {
        private final List<LogRecord> records;

        RecordingHandler(List<LogRecord> records) {
            this.records = records;
        }

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
    }
}
