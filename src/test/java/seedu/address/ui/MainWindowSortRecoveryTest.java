package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.student.Student;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.LogRecorder;
import seedu.address.testutil.StudentBuilder;

public class MainWindowSortRecoveryTest {
    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void startJavaFx() {
        FxTestUtil.startJavaFx();
    }

    @Test
    public void sort_rejectedCommandAndFailedRecovery_retainsBothCauses() throws Exception {
        runOnFxThread(() -> {
            RuntimeException recoveryFailure = new IllegalStateException("Recovery failed");
            try (SortFixture fixture = new SortFixture(recoveryFailure);
                    LogRecorder recorder = new LogRecorder(MainWindow.class)) {
                fixture.isRecoveryFailureEnabled = true;
                fixture.enterCommand("sort by/day by/level");

                LogRecord diagnostic = fixture.assertInternalFailure(recorder, Level.SEVERE);
                assertInstanceOf(ParseException.class, diagnostic.getThrown());
                assertSame(recoveryFailure, diagnostic.getThrown().getSuppressed()[0]);
                assertEquals("Student sort view recovery failed after command rejection", diagnostic.getMessage());
                fixture.assertStoredRosterUnchanged();
                fixture.isRecoveryFailureEnabled = false;
                fixture.enterCommand("list");
                assertEquals("Listed 2 students.", fixture.feedback());
            }
        });
    }

    @Test
    public void sort_renderingAndRecoveryFail_reportsFailedRecovery() throws Exception {
        runOnFxThread(() -> {
            RuntimeException recoveryFailure = new IllegalStateException("Recovery failed");
            try (SortFixture fixture = new SortFixture(recoveryFailure);
                    LogRecorder recorder = new LogRecorder(MainWindow.class)) {
                fixture.isRenderingFailureEnabled = true;
                fixture.isRecoveryFailureEnabled = true;
                fixture.enterCommand("sort by/level");

                LogRecord diagnostic = fixture.assertInternalFailure(recorder, Level.SEVERE);
                assertSame(fixture.failure, diagnostic.getThrown());
                assertSame(recoveryFailure, fixture.failure.getSuppressed()[0]);
                assertTrue(diagnostic.getMessage().contains("recovery=failed, context=Sorted 2 student(s)"));
                fixture.assertStoredRosterUnchanged();
            }
        });
    }

    @Test
    public void sort_repeatedFailureInstance_doesNotSuppressItself() throws Exception {
        runOnFxThread(() -> {
            RuntimeException failure = new IllegalStateException("Repeated failure");
            try (SortFixture fixture = new SortFixture(failure);
                    LogRecorder recorder = new LogRecorder(MainWindow.class)) {
                fixture.failure = failure;
                fixture.isRenderingFailureEnabled = true;
                fixture.isRecoveryFailureEnabled = true;
                fixture.enterCommand("sort by/level");

                assertSame(failure, fixture.assertInternalFailure(recorder, Level.SEVERE).getThrown());
                assertEquals(0, failure.getSuppressed().length);
                fixture.assertStoredRosterUnchanged();
            }
        });
    }

    @Test
    public void sort_executionFailure_reportsExecutionContextAndEndsRendering() throws Exception {
        runOnFxThread(() -> {
            try (SortFixture fixture = new SortFixture(new IllegalStateException("Unused recovery failure"));
                    LogRecorder recorder = new LogRecorder(MainWindow.class)) {
                fixture.isExecutionFailureEnabled = true;
                fixture.enterCommand("sort by/level");

                LogRecord diagnostic = fixture.assertInternalFailure(recorder, Level.WARNING);
                assertSame(fixture.failure, diagnostic.getThrown());
                assertEquals("Student sort failed: phase=rendering, recovery=restored, context=execution",
                        diagnostic.getMessage());
                fixture.assertStoredRosterUnchanged();
                fixture.isExecutionFailureEnabled = false;
                fixture.enterCommand("sort by/level");
                assertEquals("Sorted 2 student(s) by academic level (ascending).", fixture.feedback());
            }
        });
    }

    private final class SortFixture implements AutoCloseable {
        private final ModelManager model = new ModelManager();
        private final Stage stage = new Stage();
        private final RuntimeException recoveryFailure;
        private final List<Student> storedStudents;
        private final Parent root;
        private RuntimeException failure = new IllegalStateException("Sort failed");
        private boolean isRenderingFailureEnabled;
        private boolean isRecoveryFailureEnabled;
        private boolean isExecutionFailureEnabled;

        private SortFixture(RuntimeException recoveryFailure) {
            this.recoveryFailure = recoveryFailure;
            model.addStudent(new StudentBuilder().withName("Junior Student").withAcademicLevel("J1").build());
            model.addStudent(new StudentBuilder().withName("Secondary Student").withAcademicLevel("S1").build());
            storedStudents = List.copyOf(model.getAddressBook().getStudentList());
            Logic logic = new LogicManager(model, new StorageManager(
                    new JsonAddressBookStorage(temporaryFolder.resolve("students.json")),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")))) {
                @Override
                public CommandResult execute(String commandText) throws CommandException, ParseException {
                    if (isExecutionFailureEnabled) {
                        throw failure;
                    }
                    return super.execute(commandText);
                }
            };
            MainWindow window = new MainWindow(stage, logic, temporaryFolder.resolve("students.json")) {
                @Override
                StudentListPanel createStudentListPanel(ObservableList<Student> students,
                        Runnable renderingErrorHandler) {
                    return new StudentListPanel(students, renderingErrorHandler) {
                        @Override
                        public void verifySortRendering() {
                            if (isRenderingFailureEnabled) {
                                throw failure;
                            }
                            super.verifySortRendering();
                        }

                        @Override
                        public void restoreViewState(ViewState state) {
                            if (isRecoveryFailureEnabled) {
                                throw SortFixture.this.recoveryFailure;
                            }
                            super.restoreViewState(state);
                        }
                    };
                }
            };
            window.fillInnerParts();
            root = stage.getScene().getRoot();
            root.applyCss();
            root.layout();
        }

        private void enterCommand(String command) {
            TextField commandBox = (TextField) root.lookup("#commandTextField");
            commandBox.setText(command);
            commandBox.getOnAction().handle(new ActionEvent());
        }

        private String feedback() {
            return ((TextArea) root.lookup("#resultDisplay")).getText();
        }

        private LogRecord assertInternalFailure(LogRecorder recorder, Level expectedLevel) {
            assertEquals(SortCommand.MESSAGE_INTERNAL_ERROR, feedback());
            assertEquals(1, recorder.getFailures().size());
            LogRecord diagnostic = recorder.getFailures().getFirst();
            assertEquals(expectedLevel, diagnostic.getLevel());
            return diagnostic;
        }

        private void assertStoredRosterUnchanged() {
            assertEquals(storedStudents, model.getAddressBook().getStudentList());
            assertFalse(Files.exists(temporaryFolder.resolve("students.json")));
        }

        @Override
        public void close() {
            stage.close();
        }
    }
}
