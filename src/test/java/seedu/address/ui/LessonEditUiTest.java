package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.student.Student;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;

public class LessonEditUiTest {
    @TempDir
    Path temporaryFolder;

    @BeforeAll
    public static void startJavaFx() {
        FxTestUtil.startJavaFx();
    }

    @Test
    public void lessonEdit_clashThenCorrection_displaysErrorPreservesSelectionAndSavesCorrection() throws Exception {
        Path dataFile = temporaryFolder.resolve("tutorflow.json");
        JsonAddressBookStorage dataStorage = new JsonAddressBookStorage(dataFile);
        StorageManager storage = new StorageManager(dataStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        ModelManager model = new ModelManager();
        LogicManager logic = new LogicManager(model, storage);
        logic.execute("add n/John Tan l/S3 s/MATH p/91234567 gn/Mary Tan gp/98765432");
        logic.execute("add n/Mary Lim l/S4 s/MATH p/91234568 gn/Amy Lim gp/98765433");
        logic.execute("lesson add 1 s/MATH d/MONDAY st/16:00 et/18:00");
        logic.execute("lesson add 2 s/MATH d/MONDAY st/18:00 et/19:00");
        logic.execute("search John");
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Student> displayedBefore = List.copyOf(model.getStudentList());
        byte[] savedBefore = Files.readAllBytes(dataFile);

        FxTestUtil.runOnFxThread(() -> {
            Stage stage = new Stage();
            try {
                new MainWindow(stage, logic, dataFile).fillInnerParts();
                stage.show();
                stage.getScene().getRoot().applyCss();
                stage.getScene().getRoot().layout();
                @SuppressWarnings("unchecked")
                ListView<Student> students = (ListView<Student>) stage.getScene().lookup("#studentListView");
                TextField commandBox = (TextField) stage.getScene().lookup("#commandTextField");
                TextArea result = (TextArea) stage.getScene().lookup("#resultDisplay");
                students.getSelectionModel().select(0);

                commandBox.setText("lesson edit 1 et/18:30");
                commandBox.getOnAction().handle(new ActionEvent());

                assertEquals(LessonAddCommand.MESSAGE_CLASH + "Mary Lim (91234568); MATH; MONDAY; 18:00-19:00",
                        result.getText());
                assertEquals("lesson edit 1 et/18:30", commandBox.getText());
                assertTrue(commandBox.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertEquals(0, students.getSelectionModel().getSelectedIndex());
                assertEquals(displayedBefore, students.getItems());
                assertEquals(before, model.getAddressBook());
                assertArrayEquals(savedBefore, assertDoesNotThrow(() -> Files.readAllBytes(dataFile)));

                commandBox.setText("lesson edit 1 st/17:00");
                commandBox.getOnAction().handle(new ActionEvent());

                assertEquals("Edited regular lesson: John Tan (91234567); MATH; MONDAY; 17:00-18:00", result.getText());
                assertEquals("", commandBox.getText());
                assertFalse(commandBox.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            } finally {
                stage.hide();
            }
        });

        assertEquals(model.getAddressBook(), new AddressBook(dataStorage.readAddressBook().orElseThrow()));
    }
}
