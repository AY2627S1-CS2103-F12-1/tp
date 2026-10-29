package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.Region;
import seedu.address.model.student.Student;
import seedu.address.testutil.StudentBuilder;

public class StudentSortRenderingTest {
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
    public void verifySortRendering_cellFailure_restoresOrderSelectionAndScroll() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Student> students = FXCollections.observableArrayList();
            for (int i = 0; i < 20; i++) {
                students.add(new StudentBuilder().withName("Student " + (char) ('A' + i)).build());
            }
            AtomicBoolean shouldFail = new AtomicBoolean();
            RuntimeException failure = new IllegalStateException("Injected cell rendering failure");
            StudentListPanel panel = new StudentListPanel(students, (student, index) -> {
                if (shouldFail.getAndSet(false)) {
                    throw failure;
                }
                return new StudentCard(student, index, LocalDate.of(2026, 10, 20)).getRoot();
            });
            Region root = panel.getRoot();
            new Scene(root, 500, 250);
            root.applyCss();
            root.layout();
            ListView<Student> listView = getListView(root);
            listView.getSelectionModel().select(12);
            ScrollBar scrollBar = (ScrollBar) listView.lookupAll(".scroll-bar").stream()
                    .filter(node -> ((ScrollBar) node).getOrientation() == Orientation.VERTICAL).findFirst()
                    .orElseThrow();
            scrollBar.setValue(0.6);
            root.layout();
            double previousScroll = scrollBar.getValue();
            List<Student> previousStudents = List.copyOf(students);
            StudentListPanel.ViewState state = panel.beginSort();
            shouldFail.set(true);
            students.setAll(previousStudents.reversed());

            assertSame(failure, assertThrows(RuntimeException.class, panel::verifySortRendering));
            panel.prepareRestore(state);
            students.setAll(previousStudents);
            panel.restoreViewState(state);
            panel.endSort();

            assertEquals(previousStudents, listView.getItems());
            assertEquals(12, listView.getSelectionModel().getSelectedIndex());
            assertEquals(previousScroll, scrollBar.getValue());
        });
    }

    @Test
    public void verifySortRendering_success_refreshesConsecutiveCardIndices() throws Exception {
        runOnFxThread(() -> {
            Student first = new StudentBuilder().withName("First Student").build();
            Student second = new StudentBuilder().withName("Second Student").build();
            ObservableList<Student> students = FXCollections.observableArrayList(first, second);
            StudentListPanel panel = new StudentListPanel(students);
            Region root = panel.getRoot();
            new Scene(root, 500, 400);
            root.applyCss();
            root.layout();
            panel.beginSort();
            students.setAll(second, first);
            panel.verifySortRendering();
            panel.endSort();
            ListView<Student> listView = getListView(root);
            ListCell<Student> cell = listView.getCellFactory().call(listView);
            cell.updateListView(listView);
            cell.updateIndex(0);
            Parent card = (Parent) cell.getGraphic();
            assertEquals("1.", ((Label) card.lookup("#index")).getText());
            assertEquals("Second Student", ((Label) card.lookup("#name")).getText());
            cell.updateIndex(1);
            assertEquals("2.", ((Label) cell.getGraphic().lookup("#index")).getText());
        });
    }

    @Test
    public void restoreViewState_newScrollbar_preservesItsUnsavedPosition() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Student> students = FXCollections.observableArrayList();
            for (int i = 0; i < 20; i++) {
                students.add(new StudentBuilder().withName("Student " + (char) ('A' + i)).build());
            }
            StudentListPanel panel = new StudentListPanel(students);
            StudentListPanel.ViewState state = panel.captureViewState();
            Region root = panel.getRoot();
            new Scene(root, 500, 250);
            root.applyCss();
            root.layout();
            ScrollBar scrollBar = (ScrollBar) getListView(root).lookupAll(".scroll-bar").stream()
                    .filter(node -> ((ScrollBar) node).getOrientation() == Orientation.VERTICAL).findFirst()
                    .orElseThrow();
            scrollBar.setValue(0.4);
            double position = scrollBar.getValue();

            panel.restoreViewState(state);

            assertEquals(position, scrollBar.getValue(), 1e-9);
            assertEquals(-1, getListView(root).getSelectionModel().getSelectedIndex());
        });
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
