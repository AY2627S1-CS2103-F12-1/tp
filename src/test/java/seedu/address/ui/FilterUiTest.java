package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.student.Student;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.StudentBuilder;

public class FilterUiTest {
    @TempDir
    Path temporaryFolder;

    @BeforeAll
    public static void startJavaFx() {
        FxTestUtil.startJavaFx();
    }

    @Test
    public void filter_success_displaysConsecutiveIndicesAndCorrectCount() throws Exception {
        ModelManager model = createModel();
        List<Student> stored = List.copyOf(model.getAddressBook().getStudentList());
        runOnFxThread(() -> {
            Stage stage = new Stage();
            new MainWindow(stage, createLogic(model), temporaryFolder.resolve("tutorflow.json")).fillInnerParts();
            stage.getScene().getRoot().applyCss();

            enterCommand(stage, "filter l/S4");

            ListView<Student> listView = getStudentListView(stage);
            assertEquals("Found 20 students.", getFeedback(stage));
            assertEquals("20 students", ((Label) stage.getScene().getRoot().lookup("#studentCount")).getText());
            ListCell<Student> cell = createCell(listView);
            cell.updateIndex(0);
            assertEquals("1.", ((Label) cell.getGraphic().lookup("#index")).getText());
            cell.updateIndex(19);
            assertEquals("20.", ((Label) cell.getGraphic().lookup("#index")).getText());
            assertEquals(stored.get(38), listView.getItems().get(19));
            assertEquals(stored, model.getAddressBook().getStudentList());
        });
        assertFalse(Files.exists(temporaryFolder.resolve("tutorflow.json")));
    }

    @Test
    public void filter_invalidInput_preservesSelectionScrollAndDisplayedStudents() throws Exception {
        ModelManager model = createModel();
        Path dataFile = temporaryFolder.resolve("tutorflow.json");
        new JsonAddressBookStorage(dataFile).saveAddressBook(model.getAddressBook());
        byte[] savedBefore = Files.readAllBytes(dataFile);
        runOnFxThread(() -> {
            Stage stage = new Stage();
            new MainWindow(stage, createLogic(model), dataFile).fillInnerParts();
            stage.getScene().getRoot().applyCss();
            enterCommand(stage, "filter l/S4");
            ListView<Student> listView = prepareScrolledStudentList(stage);
            ScrollBar scrollBar = findVerticalScrollBar(listView);
            assertTrue(scrollBar.getValue() > 0, "the test must start with a scrolled roster");
            VirtualFlow<?> flow = (VirtualFlow<?>) listView.lookup(".virtual-flow");
            int firstVisibleIndex = flow.getFirstVisibleCell().getIndex();
            double firstVisiblePosition = flow.getFirstVisibleCell().getBoundsInParent().getMinY();
            Student selectedBefore = listView.getSelectionModel().getSelectedItem();
            List<Student> displayedBefore = List.copyOf(listView.getItems());

            for (String command : List.of("filter", "filter x/value", "filter l/S4 l/S4", "filter l/",
                    "filter s/bad")) {
                enterCommand(stage, command);
                stage.getScene().getRoot().applyCss();
                stage.getScene().getRoot().layout();

                assertEquals(displayedBefore, listView.getItems());
                assertEquals(selectedBefore, listView.getSelectionModel().getSelectedItem());
                assertEquals(12, listView.getSelectionModel().getSelectedIndex());
                assertEquals(firstVisibleIndex, flow.getFirstVisibleCell().getIndex());
                assertEquals(firstVisiblePosition, flow.getFirstVisibleCell().getBoundsInParent().getMinY(), 0.0001);
            }
        });
        assertArrayEquals(savedBefore, Files.readAllBytes(dataFile));
    }

    @Test
    public void filter_synchronousRenderingFailure_doesNotOverwriteErrorWithSuccess() throws Exception {
        ModelManager model = createModel();
        AddressBook before = new AddressBook(model.getAddressBook());
        runOnFxThread(() -> {
            Stage stage = new Stage();
            AtomicBoolean shouldFail = new AtomicBoolean();
            createWindowWithFailingCards(stage, model, shouldFail).fillInnerParts();
            stage.getScene().getRoot().applyCss();
            ListCell<Student> cell = createCell(getStudentListView(stage));
            cell.updateIndex(0);
            shouldFail.set(true);

            enterCommand(stage, "filter s/CHEMISTRY");

            assertEquals("TutorFlow could not display the filtered results due to an internal error",
                    getFeedback(stage));
            assertEquals(before, model.getAddressBook());
        });
        assertFalse(Files.exists(temporaryFolder.resolve("tutorflow.json")));
    }

    @Test
    public void filter_deferredRenderingFailure_reportsErrorAndAllowsRecovery() throws Exception {
        ModelManager model = createModel();
        AddressBook before = new AddressBook(model.getAddressBook());
        runOnFxThread(() -> {
            Stage stage = new Stage();
            AtomicBoolean shouldFail = new AtomicBoolean();
            createWindowWithFailingCards(stage, model, shouldFail).fillInnerParts();
            stage.getScene().getRoot().applyCss();
            enterCommand(stage, "filter s/CHEMISTRY");
            assertEquals("Found 20 students.", getFeedback(stage));
            ListCell<Student> cell = createCell(getStudentListView(stage));
            shouldFail.set(true);

            // Render an offscreen record whose card has not been cached by the initial layout.
            cell.updateIndex(19);

            assertEquals("TutorFlow could not display the filtered results due to an internal error",
                    getFeedback(stage));
            assertEquals(before, model.getAddressBook());
            shouldFail.set(false);
            enterCommand(stage, "list");
            assertEquals("Listed 40 students.", getFeedback(stage));
        });
    }

    private ModelManager createModel() {
        ModelManager model = new ModelManager();
        for (int i = 0; i < 40; i++) {
            model.addStudent(new StudentBuilder().withPhone(String.format("9%07d", i))
                    .withAcademicLevel(i % 2 == 0 ? "S4" : "J1")
                    .withSubjects(i % 2 == 0 ? "MATH,PHYSICS" : "CHEMISTRY").build());
        }
        return model;
    }

    private Logic createLogic(ModelManager model) {
        return new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("tutorflow.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private MainWindow createWindowWithFailingCards(Stage stage, ModelManager model, AtomicBoolean shouldFail) {
        return new MainWindow(stage, createLogic(model), temporaryFolder.resolve("tutorflow.json")) {
            @Override
            StudentListPanel createStudentListPanel(ObservableList<Student> students, Runnable renderingErrorHandler) {
                return new StudentListPanel(students, renderingErrorHandler, (student, index) -> {
                    if (shouldFail.get()) {
                        throw new UncheckedIOException(new IOException("Technical FXML failure"));
                    }
                    return new StackPane();
                });
            }
        };
    }

    private static void enterCommand(Stage stage, String command) {
        TextField commandBox = (TextField) stage.getScene().getRoot().lookup("#commandTextField");
        commandBox.setText(command);
        commandBox.getOnAction().handle(new ActionEvent());
    }

    private static String getFeedback(Stage stage) {
        return ((TextArea) stage.getScene().getRoot().lookup("#resultDisplay")).getText();
    }

    @SuppressWarnings("unchecked")
    private static ListView<Student> getStudentListView(Stage stage) {
        return (ListView<Student>) stage.getScene().getRoot().lookup("#studentListView");
    }

    private static ListCell<Student> createCell(ListView<Student> listView) {
        ListCell<Student> cell = listView.getCellFactory().call(listView);
        cell.updateListView(listView);
        return cell;
    }

    private static ScrollBar findVerticalScrollBar(ListView<Student> listView) {
        return listView.lookupAll(".scroll-bar").stream().map(node -> (ScrollBar) node)
                .filter(scrollBar -> scrollBar.getOrientation() == Orientation.VERTICAL).findFirst().orElseThrow();
    }

    private static ListView<Student> prepareScrolledStudentList(Stage stage) {
        Region root = (Region) stage.getScene().getRoot();
        root.resize(800, 500);
        root.applyCss();
        root.layout();
        ListView<Student> listView = getStudentListView(stage);
        listView.scrollTo(12);
        listView.getSelectionModel().select(12);
        // Realize selection borders before taking the viewport snapshot.
        root.applyCss();
        root.layout();
        listView.scrollTo(12);
        root.layout();
        return listView;
    }
}
