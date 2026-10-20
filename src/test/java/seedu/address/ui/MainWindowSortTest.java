package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.SortCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.student.Student;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.LogRecorder;
import seedu.address.testutil.StudentBuilder;

public class MainWindowSortTest {
    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException exception) {
            // Another UI test has already initialized the JavaFX runtime.
        }
        Platform.setImplicitExit(false);
    }

    @Test
    public void sortCommand_success_reordersDisplayedIndices() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, createLogic(model), temporaryFolder.resolve("students.json"));
            window.fillInnerParts();
            Parent root = stage.getScene().getRoot();
            root.applyCss();
            root.layout();
            enterCommand(root, "sort by/level");

            assertEquals("Secondary Student", model.getStudentList().getFirst().getName().toString());
            assertEquals("Sorted 2 student(s) by academic level (ascending).", feedback(root));
            assertEquals("", ((TextField) root.lookup("#commandTextField")).getText());
        });
    }

    @Test
    public void sortCommand_invalidInput_preservesSelectionAndOrder() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, createLogic(model), temporaryFolder.resolve("students.json"));
            window.fillInnerParts();
            Parent root = stage.getScene().getRoot();
            root.applyCss();
            root.layout();
            ListView<Student> listView = getListView(root);
            listView.getSelectionModel().select(1);
            List<Student> before = List.copyOf(model.getStudentList());
            enterCommand(root, "sort by/day by/level");

            assertEquals(SortCommand.MESSAGE_REPEATED_PREFIX, feedback(root));
            assertEquals(before, model.getStudentList());
            assertEquals(1, listView.getSelectionModel().getSelectedIndex());
            assertEquals("sort by/day by/level", ((TextField) root.lookup("#commandTextField")).getText());
        });
    }

    @Test
    public void sortCommand_emptyFilterResults_succeedsWithoutChangingStoredRoster() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            List<Student> storedBefore = List.copyOf(model.getAddressBook().getStudentList());
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, createLogic(model), temporaryFolder.resolve("students.json"));
            window.fillInnerParts();
            Parent root = stage.getScene().getRoot();
            enterCommand(root, "filter l/S5");
            enterCommand(root, "sort by/level");

            assertEquals("No students to sort.", feedback(root));
            assertEquals(List.of(), model.getStudentList());
            assertEquals(storedBefore, model.getAddressBook().getStudentList());
            assertEquals("", ((TextField) root.lookup("#commandTextField")).getText());
            enterCommand(root, "list");
            assertEquals(storedBefore, model.getStudentList());
        });
    }

    @Test
    public void sortCommand_renderFailure_restoresPreviousViewAndReportsFriendlyError() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            model.addStudent(new StudentBuilder().withName("Hidden Student").withSubjects("CHEMISTRY").build());
            List<Student> storedBefore = List.copyOf(model.getAddressBook().getStudentList());
            AtomicBoolean shouldFail = new AtomicBoolean();
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, createLogic(model), temporaryFolder.resolve("students.json")) {
                @Override
                StudentListPanel createStudentListPanel(ObservableList<Student> students,
                        Runnable renderingErrorHandler) {
                    return new StudentListPanel(students, renderingErrorHandler, (student, index) -> {
                        if (shouldFail.getAndSet(false)) {
                            throw new IllegalStateException("Injected rendering failure");
                        }
                        return new StudentCard(student, index, LocalDate.of(2026, 10, 20)).getRoot();
                    });
                }
            };
            window.fillInnerParts();
            Parent root = stage.getScene().getRoot();
            root.applyCss();
            root.layout();
            enterCommand(root, "filter s/MATH");
            List<Student> before = List.copyOf(model.getStudentList());
            ListView<Student> listView = getListView(root);
            listView.getSelectionModel().select(1);
            shouldFail.set(true);
            try (LogRecorder recorder = new LogRecorder(MainWindow.class)) {
                enterCommand(root, "sort by/level");
                assertEquals(1, recorder.getFailures().size());
                assertEquals(Level.WARNING, recorder.getFailures().getFirst().getLevel());
                assertEquals("Injected rendering failure", recorder.getFailures().getFirst().getThrown().getMessage());
                assertFalse(recorder.getRecords().stream()
                        .anyMatch(record -> record.getMessage().startsWith("Result: Sorted")));
            }
            assertEquals(SortCommand.MESSAGE_INTERNAL_ERROR, feedback(root));
            assertEquals(before, model.getStudentList());
            assertEquals(storedBefore, model.getAddressBook().getStudentList());
            assertEquals(1, listView.getSelectionModel().getSelectedIndex());
            assertEquals("sort by/level", ((TextField) root.lookup("#commandTextField")).getText());
            assertFalse(temporaryFolder.resolve("students.json").toFile().exists());
        });
    }

    @Test
    public void filterAfterSort_renderFailure_logsOnceForTheWholeCommand() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            AtomicBoolean shouldFail = new AtomicBoolean();
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, createLogic(model), temporaryFolder.resolve("students.json")) {
                @Override
                StudentListPanel createStudentListPanel(ObservableList<Student> students,
                        Runnable renderingErrorHandler) {
                    return new StudentListPanel(students, renderingErrorHandler, (student, index) -> {
                        if (shouldFail.get()) {
                            throw new IllegalStateException("Injected filter rendering failure");
                        }
                        return new StudentCard(student, index, LocalDate.of(2026, 10, 20)).getRoot();
                    });
                }
            };
            window.fillInnerParts();
            Parent root = stage.getScene().getRoot();
            root.applyCss();
            root.layout();
            enterCommand(root, "sort by/level");
            shouldFail.set(true);

            try (LogRecorder panelLogs = new LogRecorder(StudentListPanel.class);
                    LogRecorder windowLogs = new LogRecorder(MainWindow.class)) {
                enterCommand(root, "filter s/MATH");
                assertEquals(1, panelLogs.getFailures().size());
                assertEquals(Level.WARNING, panelLogs.getFailures().getFirst().getLevel());
                assertEquals(0, windowLogs.getFailures().size());
            }
            assertEquals("TutorFlow could not display the filtered results due to an internal error", feedback(root));
            shouldFail.set(false);
            enterCommand(root, "list");
            assertEquals("Listed 2 students.", feedback(root));
        });
    }

    private ModelManager createModel() {
        ModelManager model = new ModelManager();
        model.addStudent(new StudentBuilder().withName("Junior Student").withAcademicLevel("J1").build());
        model.addStudent(new StudentBuilder().withName("Secondary Student").withAcademicLevel("S1").build());
        return model;
    }

    private Logic createLogic(ModelManager model) {
        return new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("students.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private static void enterCommand(Parent root, String command) {
        TextField commandBox = (TextField) root.lookup("#commandTextField");
        commandBox.setText(command);
        commandBox.getOnAction().handle(new ActionEvent());
    }

    private static String feedback(Parent root) {
        return ((TextArea) root.lookup("#resultDisplay")).getText();
    }

    @SuppressWarnings("unchecked")
    private static ListView<Student> getListView(Parent root) {
        return (ListView<Student>) root.lookup("#studentListView");
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
