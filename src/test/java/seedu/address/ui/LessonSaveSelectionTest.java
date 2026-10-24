package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalRegularLessons.MONDAY_MATH;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.LessonEditCommand;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.student.Student;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.StudentBuilder;

public class LessonSaveSelectionTest {
    @TempDir
    Path temporaryFolder;

    @BeforeAll
    public static void startJavaFx() {
        FxTestUtil.startJavaFx();
    }

    @Test
    public void navigationRecoveryFailure_preservesOriginalSaveErrorAndData() throws Exception {
        Path dataFile = temporaryFolder.resolve("recovery.json");
        ModelManager model = new ModelManager();
        model.addStudent(new StudentBuilder().withLessons(MONDAY_MATH).build());
        AddressBook before = new AddressBook(model.getAddressBook());
        LogicManager logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
                throw new IOException("Simulated save failure");
            }
        });

        FxTestUtil.runOnFxThread(() -> {
            Stage stage = new Stage();
            try {
                MainWindow window = new MainWindow(stage, logic, dataFile) {
                    @Override
                    StudentListPanel createStudentListPanel(ObservableList<Student> students, Runnable errorHandler) {
                        return new StudentListPanel(students, errorHandler) {
                            @Override
                            public void restoreViewState(ViewState state) {
                                throw new IllegalStateException("Simulated navigation recovery failure");
                            }
                        };
                    }
                };
                window.fillInnerParts();
                stage.show();
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                input.setText("lesson edit 1 st/16:00");
                input.fireEvent(new ActionEvent());

                TextArea result = (TextArea) stage.getScene().lookup("#resultDisplay");
                assertEquals(LessonEditCommand.MESSAGE_SAVE_FAILURE, result.getText());
                assertEquals("lesson edit 1 st/16:00", input.getText());
                assertEquals(before, model.getAddressBook());
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void lessonSaveFailures_restoreSelectionsFocusAndSortedRoster() throws Exception {
        Path dataFile = temporaryFolder.resolve("tutorflow.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
                if (Files.exists(temporaryFolder.resolve("fail-save"))) {
                    throw new IOException("Simulated save failure");
                }
                super.saveAddressBook(data);
            }
        };
        ModelManager model = new ModelManager();
        LogicManager logic = new LogicManager(model, storage);
        logic.execute("add n/Alpha l/S3 s/MATH p/91234567 gn/Guardian gp/98765432");
        logic.execute("add n/Beta l/S1 s/MATH p/91234568 gn/Guardian gp/98765432");
        logic.execute("lesson add 1 s/MATH d/MONDAY st/09:00 et/10:00");
        logic.execute("lesson add 2 s/MATH d/TUESDAY st/10:00 et/11:00");
        logic.execute("hw add 2 t/Practice s/MATH due/2026-12-01");
        logic.execute("sort by/level");
        AddressBook before = new AddressBook(model.getAddressBook());
        var displayedBefore = List.copyOf(model.getStudentList());
        byte[] savedBefore = Files.readAllBytes(dataFile);
        Files.createFile(temporaryFolder.resolve("fail-save"));

        FxTestUtil.runOnFxThread(() -> {
            Stage stage = new Stage();
            try {
                new MainWindow(stage, logic, dataFile).fillInnerParts();
                stage.show();
                stage.getScene().getRoot().applyCss();
                stage.getScene().getRoot().layout();
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                for (String command : List.of("lesson edit 1 st/08:00", "lesson delete 1",
                        "lesson add 1 s/MATH d/WEDNESDAY st/08:00 et/09:00")) {
                    select(stage, "studentListView", 0);
                    select(stage, "lessonListView", 1);
                    select(stage, "homeworkListView", 0);
                    input.setText(command);
                    input.fireEvent(new ActionEvent());

                    assertSelection(stage, "studentListView", 0);
                    assertSelection(stage, "lessonListView", 1);
                    assertSelection(stage, "homeworkListView", 0);
                    assertEquals(displayedBefore, model.getStudentList());
                    assertEquals(before, model.getAddressBook());
                    assertArrayEquals(savedBefore, assertDoesNotThrow(() -> Files.readAllBytes(dataFile)));
                    assertEquals(command, input.getText());
                    assertTrue(((TextArea) stage.getScene().lookup("#resultDisplay")).getText()
                            .contains("No lesson data was changed."));
                }
            } finally {
                stage.hide();
            }
        });
    }

    private static void select(Stage stage, String id, int index) {
        ListView<?> list = (ListView<?>) stage.getScene().lookup("#" + id);
        list.getSelectionModel().select(index);
        list.getFocusModel().focus(index);
    }

    private static void assertSelection(Stage stage, String id, int index) {
        ListView<?> list = (ListView<?>) stage.getScene().lookup("#" + id);
        assertEquals(index, list.getSelectionModel().getSelectedIndex(), id + " selection");
        assertEquals(index, list.getFocusModel().getFocusedIndex(), id + " focus");
    }
}
