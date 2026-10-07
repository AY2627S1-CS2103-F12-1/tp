package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.student.Student;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.StudentBuilder;

public class MainWindowTest {
    private static final double MIN_WINDOW_WIDTH = 600;
    private static final double MIN_WINDOW_HEIGHT = 450;
    // Scene that native Windows JavaFX leaves inside a 600x450 window; the window frame takes the rest
    private static final double MIN_SCENE_WIDTH = 587;
    private static final double MIN_SCENE_HEIGHT = 414.5;

    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException e) {
            // Another UI test class has already started JavaFX in this test JVM
        }
        Platform.setImplicitExit(false);
    }

    @Test
    public void fillInnerParts_minimumWindowSize_statusBarFitsScene() throws Exception {
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        Logic logic = new LogicManager(new ModelManager(), storage);

        runOnFxThread(() -> {
            Stage stage = new Stage();
            new MainWindow(stage, logic, Path.of("data", "tutorflow.json")).fillInnerParts();
            assertEquals(MIN_WINDOW_WIDTH, stage.getMinWidth());
            assertEquals(MIN_WINDOW_HEIGHT, stage.getMinHeight());

            // Below its minimum height, the window's root pushes the status bar past the bottom of the scene
            Region root = (Region) stage.getScene().getRoot();
            root.applyCss();
            double minRootHeight = root.minHeight(MIN_SCENE_WIDTH);
            assertTrue(minRootHeight <= MIN_SCENE_HEIGHT, "window parts need a height of " + minRootHeight
                    + ", more than the scene height of " + MIN_SCENE_HEIGHT);
        });
    }

    @Test
    public void listCommand_clearsStudentSelectionAndShowsCount() throws Exception {
        ModelManager model = new ModelManager();
        model.addStudent(new StudentBuilder().build());
        model.addStudent(new StudentBuilder().withName("Jane Tan").build());
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("tutorflow.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        Logic logic = new LogicManager(model, storage);

        runOnFxThread(() -> {
            Stage stage = new Stage();
            new MainWindow(stage, logic, Path.of("data", "tutorflow.json")).fillInnerParts();
            @SuppressWarnings("unchecked")
            ListView<Student> listView = (ListView<Student>) stage.getScene().getRoot().lookup("#studentListView");
            TextField commandBox = (TextField) stage.getScene().getRoot().lookup("#commandTextField");
            TextArea resultDisplay = (TextArea) stage.getScene().getRoot().lookup("#resultDisplay");

            listView.getSelectionModel().select(1);
            commandBox.setText("list");
            commandBox.getOnAction().handle(new ActionEvent());

            assertEquals(-1, listView.getSelectionModel().getSelectedIndex());
            assertEquals("Listed 2 students.", resultDisplay.getText());
            assertEquals("", commandBox.getText());
        });
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
