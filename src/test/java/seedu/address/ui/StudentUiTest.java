package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalHomeworks.ATOMIC_STRUCTURE;
import static seedu.address.testutil.TypicalHomeworks.MECHANICS;

import java.nio.file.Path;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.Region;
import seedu.address.model.student.Student;
import seedu.address.testutil.StudentBuilder;

public class StudentUiTest {
    private static final String LONG_NAME = "Guardian With An Exceptionally Long Name That Fills Seventy Characters";

    @BeforeAll
    public static void startJavaFx() {
        Platform.startup(() -> { });
        Platform.setImplicitExit(false);
    }

    @Test
    public void studentCard_displaysAllStudentFields() throws Exception {
        runOnFxThread(() -> {
            Student student = new StudentBuilder().build();
            Parent card = (Parent) new StudentCard(student, 2).getRoot();

            assertEquals("2.", labelText(card, "index"));
            assertEquals("John Tan", labelText(card, "name"));
            assertEquals("S3", labelText(card, "academicLevel"));
            assertEquals("MATH", labelText(card, "subjects"));
            assertEquals("Student: 91234567", labelText(card, "studentPhone"));
            assertEquals("Guardian: Mary Tan", labelText(card, "guardianName"));
            assertEquals("Guardian phone: 98765432", labelText(card, "guardianPhone"));
            assertEquals("Assigned homework: 0", labelText(card, "assignedHomeworkCount"));
        });
    }

    @Test
    public void studentCard_countsAssignedHomeworkOnly() throws Exception {
        runOnFxThread(() -> {
            // ATOMIC_STRUCTURE is completed, so it is not counted
            Student student = new StudentBuilder().withHomeworks(ALGEBRA, MECHANICS, ATOMIC_STRUCTURE).build();
            Parent card = (Parent) new StudentCard(student, 1).getRoot();

            assertEquals("Assigned homework: 2", labelText(card, "assignedHomeworkCount"));
        });
    }

    @Test
    public void studentListPanel_updatesCountWhenRosterChanges() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Student> students = FXCollections.observableArrayList();
            Parent panel = (Parent) new StudentListPanel(students).getRoot();
            @SuppressWarnings("unchecked")
            ListView<Student> listView = (ListView<Student>) panel.lookup("#studentListView");

            assertSame(students, listView.getItems());
            assertEquals("0 students", labelText(panel, "studentCount"));

            students.add(new StudentBuilder().build());
            assertEquals("1 student", labelText(panel, "studentCount"));
            ListCell<Student> cell = listView.getCellFactory().call(listView);
            cell.updateListView(listView);
            cell.updateIndex(0);
            assertEquals("John Tan", labelText((Parent) cell.getGraphic(), "name"));

            students.add(new StudentBuilder().withName("Jane Tan").build());
            assertEquals("2 students", labelText(panel, "studentCount"));

            students.remove(0);
            assertEquals("1 student", labelText(panel, "studentCount"));
            cell.updateIndex(-1);
            assertNull(cell.getGraphic());
        });
    }

    @Test
    public void studentListPanel_longNames_cardFitsListWidth() throws Exception {
        runOnFxThread(() -> {
            Student student = new StudentBuilder().withName(LONG_NAME).withSubjects("MATH, PHYSICS, CHEMISTRY")
                    .withGuardianName(LONG_NAME).build();
            // Make the list narrower than the guardian detail on one line, so the card must fit itself to the list
            // whatever font this platform uses
            double listWidth = 0.75 * oneLineWidth(student, "guardianName");
            Region panel = new StudentListPanel(FXCollections.observableArrayList(student)).getRoot();
            new Scene(panel, listWidth, 400);
            panel.applyCss();
            panel.layout();
            // A second pass applies the detail widths bound to the first pass's sizes
            panel.layout();

            ListView<?> listView = (ListView<?>) panel.lookup("#studentListView");
            double viewportRight = rightEdge(listView.lookup(".clipped-container"));
            Parent card = (Parent) listView.lookup("#cardPane");
            assertEndsBefore(viewportRight, card, "cardPane");
            for (String id : new String[] {"academicLevel", "subjects", "guardianName", "guardianPhone"}) {
                assertEndsBefore(viewportRight, card.lookup("#" + id), id);
            }
            for (Node node : listView.lookupAll(".scroll-bar")) {
                ScrollBar scrollBar = (ScrollBar) node;
                assertTrue(scrollBar.getOrientation() == Orientation.VERTICAL || !scrollBar.isVisible(),
                        "horizontal scroll bar is visible");
            }
        });
    }

    @Test
    public void statusBarFooter_updatesPreviewStatus() throws Exception {
        runOnFxThread(() -> {
            StatusBarFooter footer = new StatusBarFooter(Path.of("students.json"));
            Parent root = (Parent) footer.getRoot();

            footer.setStatusText("Student roster is in memory only; changes are not saved.");

            assertEquals("Student roster is in memory only; changes are not saved.",
                    labelText(root, "saveLocationStatus"));
        });
    }

    private static String labelText(Parent root, String id) {
        return ((Label) root.lookup("#" + id)).getText();
    }

    private static double rightEdge(Node node) {
        return node.localToScene(node.getLayoutBounds()).getMaxX();
    }

    private static void assertEndsBefore(double limit, Node node, String id) {
        double right = rightEdge(node);
        assertTrue(right <= limit, id + " ends at x=" + right + ", past the visible list edge at x=" + limit);
    }

    /** Returns the width the given label of the student's card needs to show its text on one line. */
    private static double oneLineWidth(Student student, String id) {
        Parent card = (Parent) new StudentCard(student, 1).getRoot();
        new Scene(card);
        card.applyCss();
        return card.lookup("#" + id).prefWidth(-1);
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
