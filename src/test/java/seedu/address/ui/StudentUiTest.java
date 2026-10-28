package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalHomeworks.ATOMIC_STRUCTURE;
import static seedu.address.testutil.TypicalHomeworks.MECHANICS;
import static seedu.address.testutil.TypicalRegularLessons.MONDAY_MATH;
import static seedu.address.testutil.TypicalRegularLessons.TUESDAY_PHYSICS;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
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
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.student.Student;
import seedu.address.testutil.HomeworkBuilder;
import seedu.address.testutil.StudentBuilder;

public class StudentUiTest {
    /** Date the student cards count upcoming homework from, between the due dates of the typical homework. */
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 16);
    private static final String LONG_NAME = "Guardian With An Exceptionally Long Name That Fills Seventy Characters";

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
    public void studentCard_displaysAllStudentFields() throws Exception {
        runOnFxThread(() -> {
            Student student = new StudentBuilder().build();
            Parent card = (Parent) new StudentCard(student, 2, TODAY).getRoot();

            assertEquals("2.", labelText(card, "index"));
            assertEquals("John Tan", labelText(card, "name"));
            assertEquals("S3", labelText(card, "academicLevel"));
            assertEquals("MATH", labelText(card, "subjects"));
            assertEquals("Student: 91234567", labelText(card, "studentPhone"));
            assertEquals("Guardian: Mary Tan", labelText(card, "guardianName"));
            assertEquals("Guardian phone: 98765432", labelText(card, "guardianPhone"));
            assertEquals("Upcoming homework: 0", labelText(card, "upcomingHomeworkCount"));
            assertEquals("Overdue homework: 0", labelText(card, "overdueHomeworkCount"));
        });
    }

    @Test
    public void studentCard_longGuardianName_tooltipShowsFullName() throws Exception {
        runOnFxThread(() -> {
            Student student = new StudentBuilder().withGuardianName(LONG_NAME).build();
            Parent card = (Parent) new StudentCard(student, 1, TODAY).getRoot();
            Label guardianName = (Label) card.lookup("#guardianName");

            assertEquals(LONG_NAME, guardianName.getTooltip().getText());
        });
    }

    @Test
    public void studentCard_countsUpcomingHomeworkOnly() throws Exception {
        runOnFxThread(() -> {
            // ALGEBRA is overdue; MECHANICS and scored ATOMIC_STRUCTURE are due on or after TODAY.
            Student student = new StudentBuilder().withHomeworks(ALGEBRA, MECHANICS, ATOMIC_STRUCTURE).build();
            Parent card = (Parent) new StudentCard(student, 1, TODAY).getRoot();

            assertEquals("Upcoming homework: 2", labelText(card, "upcomingHomeworkCount"));
            assertEquals("Overdue homework: 1", labelText(card, "overdueHomeworkCount"));
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
    public void studentCard_narrowWidth_homeworkCountsRemainReadable() throws Exception {
        runOnFxThread(() -> {
            for (String family : List.of("System", "Serif", "Monospaced")) {
                Region card = new StudentCard(new StudentBuilder().build(), 1, TODAY).getRoot();
                Scene scene = new Scene(card, 260, 350);
                scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
                for (String id : List.of("upcomingHomeworkCount", "overdueHomeworkCount")) {
                    card.lookup("#" + id).setStyle("-fx-font-family: '" + family + "'; -fx-font-size: 16px;");
                }
                card.applyCss();
                card.layout();
                for (String id : List.of("upcomingHomeworkCount", "overdueHomeworkCount")) {
                    Label label = (Label) card.lookup("#" + id);
                    assertEndsBefore(card.getWidth(), label, id);
                    assertTrue(label.getWidth() >= label.prefWidth(-1), id + " is truncated with " + family);
                }
            }
        });
    }

    @Test
    public void upcomingPanel_narrowWidths_entriesAndHeadingsFit() throws Exception {
        runOnFxThread(() -> {
            Student student = new StudentBuilder().withName(LONG_NAME).withLessons(MONDAY_MATH)
                    .withHomeworks(new HomeworkBuilder().withTitle("A long homework title that must wrap on resize")
                            .build()).build();
            for (String family : List.of("System", "Serif", "Monospaced")) {
                Region panel = new UpcomingPanel(FXCollections.observableArrayList(student)).getRoot();
                Scene scene = new Scene(panel, 360, 600);
                scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
                panel.applyCss();
                panel.layout();
                for (Node node : panel.lookupAll(".label")) {
                    node.setStyle("-fx-font-family: '" + family + "'; -fx-font-size: 16px;");
                }
                // Different skins can add different cell insets. Leave room for them without a fixed pixel guess.
                for (Node node : panel.lookupAll(".list-cell")) {
                    node.setStyle("-fx-padding: 2 13 2 9;");
                }
                panel.applyCss();
                for (double width : new double[] {220, 360, 260}) {
                    panel.resize(width, 600);
                    panel.layout();
                    panel.layout();
                    for (Node node : panel.lookupAll(".upcoming-section-title")) {
                        Label heading = (Label) node;
                        assertTrue(heading.getWidth() >= heading.prefWidth(-1), "heading is truncated with " + family);
                    }
                    for (String id : List.of("lessonListView", "homeworkListView")) {
                        ListView<?> list = (ListView<?>) panel.lookup("#" + id);
                        Label entry = (Label) list.lookup(".upcoming-entry");
                        assertTrue(entry.getWidth() > 0);
                        assertEndsBefore(rightEdge(list.lookup(".clipped-container")), entry, id);
                        assertTrue(entry.getHeight() + 1 >= entry.prefHeight(entry.getWidth()),
                                id + " clips wrapped text with " + family + " at width " + width);
                        assertTrue(entry.getWidth() <= ((Region) entry.getParent()).getWidth() - 22 + 1,
                                id + " ignores the cell insets");
                        for (Node node : list.lookupAll(".scroll-bar")) {
                            ScrollBar bar = (ScrollBar) node;
                            assertTrue(bar.getOrientation() == Orientation.VERTICAL || !bar.isVisible(),
                                    id + " has a horizontal scroll bar");
                        }
                    }
                }
            }
        });
    }

    @Test
    public void upcomingPanel_displaysSortedLessonsAndOutstandingHomework() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Student> students = FXCollections.observableArrayList(
                    new StudentBuilder().withName("Zoe Tan").withLessons(TUESDAY_PHYSICS)
                            .withHomeworks(MECHANICS, ATOMIC_STRUCTURE.withStatus(
                                    HomeworkStatus.COMPLETED)).build(),
                    new StudentBuilder().withName("Amy Lim").withPhone("91234568").withLessons(MONDAY_MATH)
                            .withHomeworks(ALGEBRA).build());
            Parent panel = (Parent) new UpcomingPanel(students,
                    Clock.fixed(Instant.parse("2026-10-16T00:00:00Z"), ZoneOffset.UTC)).getRoot();
            ListView<?> lessonListView = (ListView<?>) panel.lookup("#lessonListView");
            ListView<?> homeworkListView = (ListView<?>) panel.lookup("#homeworkListView");

            assertEquals(List.of("MON 17:00 - 18:30\nAmy Lim (91234568) \u00b7 MATH",
                    "TUE 19:00 - 20:30\nZoe Tan (91234567) \u00b7 PHYSICS"), getEntryTexts(lessonListView));
            assertEquals(List.of("Complete algebra worksheet (overdue)\nAmy Lim (91234568)"
                    + " \u00b7 MATH \u00b7 Due 2026-10-15",
                    "Attempt mechanics questions 1-5\nZoe Tan (91234567) \u00b7 PHYSICS \u00b7 Due 2026-10-20"),
                    getEntryTexts(homeworkListView));
            assertEquals("2 lessons", labelText(panel, "lessonCount"));
            assertEquals("2 tasks", labelText(panel, "homeworkCount"));

            assertHomeworkHighlightOnReuse(homeworkListView);

            students.set(0, students.get(0).withLessons(List.of()).withHomeworks(List.of()));

            assertEquals(List.of("MON 17:00 - 18:30\nAmy Lim (91234568) \u00b7 MATH"), getEntryTexts(lessonListView));
            assertEquals(List.of("Complete algebra worksheet (overdue)\nAmy Lim (91234568)"
                    + " \u00b7 MATH \u00b7 Due 2026-10-15"),
                    getEntryTexts(homeworkListView));
        });
    }

    @Test
    public void upcomingPanel_sameNameDifferentPhones_distinguishesBothReports() throws Exception {
        runOnFxThread(() -> {
            Student first = new StudentBuilder().withName("Sam Tan").withPhone("91234567")
                    .withLessons(MONDAY_MATH).withHomeworks(ALGEBRA).build();
            Student second = new StudentBuilder().withName("Sam Tan").withPhone("91234568")
                    .withLessons(MONDAY_MATH).withHomeworks(ALGEBRA).build();
            Parent panel = (Parent) new UpcomingPanel(FXCollections.observableArrayList(second, first)).getRoot();
            ListView<?> lessons = (ListView<?>) panel.lookup("#lessonListView");
            ListView<?> homework = (ListView<?>) panel.lookup("#homeworkListView");

            for (ListView<?> list : List.of(lessons, homework)) {
                assertEquals(2, list.getItems().size());
                assertTrue(list.getItems().stream().anyMatch(entry -> entry.toString().contains("Sam Tan (91234567)")));
                assertTrue(list.getItems().stream().anyMatch(entry -> entry.toString().contains("Sam Tan (91234568)")));
            }
        });
    }

    @Test
    public void upcomingPanel_completionIndependentOfScore_updatesEntriesAndCardCounts() throws Exception {
        runOnFxThread(() -> {
            Homework scoredAssigned = new HomeworkBuilder(ALGEBRA).withScore("1/10").build();
            Homework unscoredCompleted = new HomeworkBuilder(MECHANICS).withStatus("COMPLETED").build();
            Student student = new StudentBuilder().withHomeworks(scoredAssigned, unscoredCompleted).build();
            ObservableList<Student> students = FXCollections.observableArrayList(student);
            Clock clock = Clock.fixed(Instant.parse("2026-10-21T00:00:00Z"), ZoneOffset.UTC);
            Parent panel = (Parent) new UpcomingPanel(students, clock).getRoot();
            ListView<?> homework = (ListView<?>) panel.lookup("#homeworkListView");

            assertEquals(1, homework.getItems().size());
            assertTrue(homework.getItems().getFirst().toString().contains("Complete algebra worksheet (overdue)"));
            Parent card = (Parent) new StudentCard(student, 1, LocalDate.now(clock)).getRoot();
            assertEquals("Overdue homework: 1", labelText(card, "overdueHomeworkCount"));

            students.set(0, student.withHomeworks(List.of(scoredAssigned.withStatus(HomeworkStatus.COMPLETED),
                    unscoredCompleted)));
            assertTrue(homework.getItems().isEmpty());
            assertEquals("0 tasks", labelText(panel, "homeworkCount"));
            card = (Parent) new StudentCard(students.getFirst(), 1, LocalDate.now(clock)).getRoot();
            assertEquals("Overdue homework: 0", labelText(card, "overdueHomeworkCount"));
        });
    }

    @Test
    public void statusBarFooter_showsDataFilePath() throws Exception {
        runOnFxThread(() -> {
            Path dataFilePath = Path.of("data", "tutorflow.json");
            Parent root = (Parent) new StatusBarFooter(dataFilePath).getRoot();

            assertEquals(Path.of(".").resolve(dataFilePath).toString(), labelText(root, "saveLocationStatus"));
        });
    }

    private static List<String> getEntryTexts(ListView<?> listView) {
        return listView.getItems().stream().map(Object::toString).toList();
    }

    private static <T> void assertHomeworkHighlightOnReuse(ListView<T> listView) {
        ListCell<T> cell = listView.getCellFactory().call(listView);
        cell.updateListView(listView);
        cell.updateIndex(0);
        assertTrue(cell.getGraphic().getStyleClass().contains("overdue-homework"));

        cell.updateIndex(1);
        assertFalse(cell.getGraphic().getStyleClass().contains("overdue-homework"));

        cell.updateIndex(-1);
        assertNull(cell.getGraphic());

        cell.updateIndex(0);
        assertTrue(cell.getGraphic().getStyleClass().contains("overdue-homework"));
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
        Parent card = (Parent) new StudentCard(student, 1, TODAY).getRoot();
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
