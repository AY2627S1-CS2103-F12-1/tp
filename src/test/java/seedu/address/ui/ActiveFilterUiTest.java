package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;
import static seedu.address.testutil.TypicalRegularLessons.MONDAY_MATH;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.StudentBuilder;

public class ActiveFilterUiTest {
    @TempDir
    Path temporaryFolder;

    @BeforeAll
    public static void startJavaFx() {
        FxTestUtil.startJavaFx();
    }

    @Test
    public void filter_sameResults_updatesVisibleCriteria() throws Exception {
        ModelManager model = createModel();
        runOnFxThread(() -> {
            Stage stage = createWindow(model, new AtomicBoolean());
            try {
                assertDescription(stage, "");
                enterCommand(stage, "filter l/s4");
                assertDescription(stage, "Filtered: level S4");
                assertCount(stage, "2 students");

                enterCommand(stage, "filter s/math");
                assertDescription(stage, "Filtered: subject MATH");
                assertCount(stage, "2 students");

                enterCommand(stage, "filter s/math l/s4");
                assertDescription(stage, "Filtered: level S4; subject MATH");
                assertCount(stage, "2 students");

                enterCommand(stage, "filter l/S5");
                assertDescription(stage, "Filtered: level S5");
                assertCount(stage, "0 students");
                enterCommand(stage, "filter l/J2");
                assertDescription(stage, "Filtered: level J2");
                assertCount(stage, "0 students");
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void filter_meetingDay_updatesCriteriaWhenResultsStayEmptyOrLessonMoves() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            var first = model.getStudentList().getFirst();
            model.setStudent(first, first.withLessons(List.of(MONDAY_MATH)));
            Stage stage = createWindow(model, new AtomicBoolean());
            try {
                enterCommand(stage, "filter d/MONDAY");
                assertDescription(stage, "Filtered: day MONDAY");
                assertCount(stage, "1 student");
                enterCommand(stage, "filter l/S4 s/MATH d/MONDAY");
                assertDescription(stage, "Filtered: level S4; subject MATH; day MONDAY");
                enterCommand(stage, "lesson edit 1 d/WEDNESDAY");
                assertDescription(stage, "Filtered: level S4; subject MATH; day MONDAY");
                assertCount(stage, "0 students");
                enterCommand(stage, "filter d/TUESDAY");
                assertDescription(stage, "Filtered: day TUESDAY");
                assertCount(stage, "0 students");
                enterCommand(stage, "filter d/bad");
                assertDescription(stage, "Filtered: day TUESDAY");
                enterCommand(stage, "list");
                assertDescription(stage, "");
                assertCount(stage, "2 students");
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void filter_unrelatedCommandsAndRosterEdits_preservesDescription() throws Exception {
        runOnFxThread(() -> {
            Stage stage = createWindow(createModel(), new AtomicBoolean());
            try {
                enterCommand(stage, "filter l/S4");
                for (String command : List.of("hw list 1", "lesson list", "sort by/level", "filter l/S6")) {
                    enterCommand(stage, command);
                    assertDescription(stage, "Filtered: level S4");
                    assertCount(stage, "2 students");
                }
                enterCommand(stage, "edit 1 l/J1");
                assertDescription(stage, "Filtered: level S4");
                assertCount(stage, "1 student");
                enterCommand(stage, "delete 1");
                assertDescription(stage, "Filtered: level S4");
                assertCount(stage, "0 students");

                enterCommand(stage, "filter l/S5");
                enterCommand(stage, "lesson list");
                assertDescription(stage, "Filtered: level S5");
                assertCount(stage, "0 students");
                enterCommand(stage, "clear");
                assertDescription(stage, "Filtered: level S5");
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void filter_viewResetCommands_hidesDescription() throws Exception {
        runOnFxThread(() -> {
            Stage stage = createWindow(createModel(), new AtomicBoolean());
            try {
                for (String command : List.of("list", "ls", "hw upcoming due/7", "search Alice")) {
                    enterCommand(stage, "filter l/S4");
                    assertDescription(stage, "Filtered: level S4");
                    enterCommand(stage, command);
                    assertDescription(stage, "");
                }
                enterCommand(stage, "filter l/S4");
                enterCommand(stage, "add n/Cara l/J1 s/MATH p/92345678 gn/Guardian gp/98765432");
                assertDescription(stage, "");
                assertCount(stage, "3 students");
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void filter_beforeWindowCreation_initializesDescription() throws Exception {
        ModelManager model = createModel();
        LogicManager logic = createLogic(model, new AtomicBoolean());
        logic.execute("filter l/S4 s/MATH");
        runOnFxThread(() -> {
            Stage stage = new Stage();
            try {
                new MainWindow(stage, logic, temporaryFolder.resolve("tutorflow.json")).fillInnerParts();
                stage.getScene().getRoot().applyCss();
                assertDescription(stage, "Filtered: level S4; subject MATH");
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void filter_failedAdd_restoresDescription() throws Exception {
        runOnFxThread(() -> {
            AtomicBoolean shouldFail = new AtomicBoolean(true);
            Stage stage = createWindow(createModel(), shouldFail);
            try {
                enterCommand(stage, "filter l/S4 s/MATH");
                enterCommand(stage, "add n/Cara l/J1 s/MATH p/92345678 gn/Guardian gp/98765432");

                assertTrue(((TextArea) stage.getScene().getRoot().lookup("#resultDisplay")).getText()
                        .contains("Nothing was changed."));
                assertDescription(stage, "Filtered: level S4; subject MATH");
                assertCount(stage, "2 students");
                shouldFail.set(false);
                enterCommand(stage, "list");
                assertDescription(stage, "");
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void filter_narrowPanel_wrapsDescriptionWithoutClipping() throws Exception {
        ModelManager model = createModel();
        createLogic(model, new AtomicBoolean()).execute("filter l/S4 s/CHEMISTRY d/WEDNESDAY");
        runOnFxThread(() -> {
            StudentListPanel panel = new StudentListPanel(model.getStudentList());
            panel.bindStudentListPredicate(model.studentListPredicateProperty());
            Region root = panel.getRoot();
            Scene scene = new Scene(root, 220, 350);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
            Label description = (Label) root.lookup("#filterDescription");
            for (String family : List.of("System", "Serif", "Monospaced")) {
                description.setStyle("-fx-font-family: '" + family + "'; -fx-font-size: 16px;");
                root.applyCss();
                root.resize(220, 350);
                root.layout();
                root.layout();

                assertEquals("Filtered: level S4; subject CHEMISTRY; day WEDNESDAY", description.getText());
                assertTrue(description.getHeight() >= description.prefHeight(description.getWidth()) - 1,
                        "label " + description.getWidth() + "x" + description.getHeight()
                                + " needs height " + description.prefHeight(description.getWidth()));
                Text singleLine = new Text(description.getText());
                singleLine.setFont(description.getFont());
                assertTrue(description.getHeight() > singleLine.getLayoutBounds().getHeight(),
                        "the description should wrap onto multiple lines");
                assertTrue(description.getBoundsInParent().getMaxX() <= root.getWidth() - root.getInsets().getRight(),
                        family + ": bounds " + description.getBoundsInParent() + "; label width "
                                + description.getWidth() + "; panel width " + root.getWidth()
                                + "; insets " + root.getInsets());
            }
        });
    }

    private ModelManager createModel() {
        ModelManager model = new ModelManager();
        model.addStudent(new StudentBuilder().withName("Alice Tan").withAcademicLevel("S4")
                .withSubjects("MATH").withPhone("91111111").build());
        model.addStudent(new StudentBuilder().withName("Ben Lim").withAcademicLevel("S4")
                .withSubjects("MATH").withPhone("92222222").build());
        return model;
    }

    private Stage createWindow(ModelManager model, AtomicBoolean shouldFail) {
        Stage stage = new Stage();
        new MainWindow(stage, createLogic(model, shouldFail), temporaryFolder.resolve("tutorflow.json"))
                .fillInnerParts();
        stage.getScene().getRoot().applyCss();
        return stage;
    }

    private LogicManager createLogic(ModelManager model, AtomicBoolean shouldFail) {
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("tutorflow.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                if (shouldFail.get()) {
                    throw new IOException("Simulated save failure");
                }
                super.saveAddressBook(addressBook);
            }
        };
        return new LogicManager(model, storage);
    }

    private static void enterCommand(Stage stage, String command) {
        TextField input = (TextField) stage.getScene().getRoot().lookup("#commandTextField");
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }

    private static void assertDescription(Stage stage, String expected) {
        Label description = (Label) stage.getScene().getRoot().lookup("#filterDescription");
        assertEquals(expected, description.getText());
        if (expected.isEmpty()) {
            assertFalse(description.isVisible());
            assertFalse(description.isManaged());
        } else {
            assertTrue(description.isVisible());
            assertTrue(description.isManaged());
        }
    }

    private static void assertCount(Stage stage, String expected) {
        assertEquals(expected, ((Label) stage.getScene().getRoot().lookup("#studentCount")).getText());
    }
}
