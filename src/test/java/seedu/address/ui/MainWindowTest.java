package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalRegularLessons.MONDAY_MATH;
import static seedu.address.testutil.TypicalRegularLessons.TUESDAY_PHYSICS;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.student.Student;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.StudentBuilder;

public class MainWindowTest {
    private static final double MIN_WINDOW_WIDTH = 600;
    private static final double MIN_WINDOW_HEIGHT = 450;

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

            // Measure this platform's window decorations rather than assuming Windows title-bar dimensions.
            stage.setWidth(MIN_WINDOW_WIDTH);
            stage.setHeight(MIN_WINDOW_HEIGHT);
            try {
                stage.show();
                Region root = (Region) stage.getScene().getRoot();
                root.applyCss();
                root.layout();
                double minRootHeight = root.minHeight(stage.getScene().getWidth());
                assertTrue(minRootHeight <= stage.getScene().getHeight(), "window parts need a height of "
                        + minRootHeight + ", more than the scene height of " + stage.getScene().getHeight());

                // Dragging the dividers toward the bottom must leave enough room for both sidebar lists.
                SplitPane verticalPane = (SplitPane) root.lookup("#verticalSplitPane");
                SplitPane contentPane = (SplitPane) root.lookup("#contentSplitPane");
                contentPane.setDividerPositions(1);
                verticalPane.setDividerPositions(0.2, 1);
                root.layout();
                Region sidebar = (Region) root.lookup("#upcomingPanelPlaceholder");
                assertTrue(contentPane.getHeight() + 1 >= sidebar.minHeight(sidebar.getWidth()),
                        "content height " + contentPane.getHeight() + " must accommodate sidebar minimum "
                        + sidebar.minHeight(sidebar.getWidth()) + " at width " + sidebar.getWidth()
                        + "; bound minimum " + contentPane.getMinHeight());
                for (String id : new String[] {"lessonListView", "homeworkListView"}) {
                    Node list = root.lookup("#" + id);
                    double bottom = list.localToScene(list.getLayoutBounds()).getMaxY();
                    double sidebarBottom = sidebar.localToScene(sidebar.getLayoutBounds()).getMaxY();
                    assertTrue(bottom <= sidebarBottom + 1, id + " extends below the sidebar");
                }
            } finally {
                stage.close();
            }
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
            stage.getScene().getRoot().applyCss();
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

    @Test
    public void searchCommand_filtersRosterButNotUpcomingPanel() throws Exception {
        ModelManager model = new ModelManager();
        model.addStudent(new StudentBuilder().withLessons(MONDAY_MATH).withHomeworks(ALGEBRA).build());
        model.addStudent(new StudentBuilder().withName("Amy Lim").withPhone("91234568").withSubjects("PHYSICS")
                .withLessons(TUESDAY_PHYSICS).build());
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("tutorflow.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        Logic logic = new LogicManager(model, storage);

        runOnFxThread(() -> {
            Stage stage = new Stage();
            new MainWindow(stage, logic, Path.of("data", "tutorflow.json")).fillInnerParts();
            try {
                stage.getScene().getRoot().applyCss();
                TextField commandBox = (TextField) stage.getScene().getRoot().lookup("#commandTextField");
                ListView<?> studentListView = (ListView<?>) stage.getScene().getRoot().lookup("#studentListView");
                ListView<?> lessonListView = (ListView<?>) stage.getScene().getRoot().lookup("#lessonListView");
                ListView<?> homeworkListView = (ListView<?>) stage.getScene().getRoot().lookup("#homeworkListView");

                commandBox.setText("search Amy");
                commandBox.getOnAction().handle(new ActionEvent());

                assertEquals(1, studentListView.getItems().size());
                assertEquals(2, lessonListView.getItems().size());
                assertEquals(1, homeworkListView.getItems().size());

                commandBox.setText("search Nobody");
                commandBox.getOnAction().handle(new ActionEvent());

                assertEquals(0, studentListView.getItems().size());
                assertEquals(2, lessonListView.getItems().size());
                assertEquals(1, homeworkListView.getItems().size());

                commandBox.setText("clear");
                commandBox.getOnAction().handle(new ActionEvent());

                assertEquals(0, lessonListView.getItems().size());
                assertEquals(0, homeworkListView.getItems().size());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void filterAndSortCommands_changeRosterButNotUpcomingPanel() throws Exception {
        ModelManager model = new ModelManager();
        Student john = new StudentBuilder().withLessons(MONDAY_MATH).withHomeworks(ALGEBRA).build();
        Student amy = new StudentBuilder().withName("Amy Lim").withPhone("91234568").withSubjects("PHYSICS")
                .withLessons(TUESDAY_PHYSICS).build();
        model.addStudent(john);
        model.addStudent(amy);
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("tutorflow.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        Logic logic = new LogicManager(model, storage);

        runOnFxThread(() -> {
            Stage stage = new Stage();
            new MainWindow(stage, logic, Path.of("data", "tutorflow.json")).fillInnerParts();
            try {
                Region root = (Region) stage.getScene().getRoot();
                root.applyCss();
                root.layout();
                TextField commandBox = (TextField) root.lookup("#commandTextField");
                TextArea resultDisplay = (TextArea) root.lookup("#resultDisplay");
                ListView<?> studentListView = (ListView<?>) root.lookup("#studentListView");
                ListView<?> lessonListView = (ListView<?>) root.lookup("#lessonListView");
                ListView<?> homeworkListView = (ListView<?>) root.lookup("#homeworkListView");
                List<?> lessonsBefore = List.copyOf(lessonListView.getItems());
                List<?> homeworkBefore = List.copyOf(homeworkListView.getItems());

                commandBox.setText("filter s/PHYSICS");
                commandBox.getOnAction().handle(new ActionEvent());
                assertEquals(List.of(amy), studentListView.getItems());
                assertEquals(lessonsBefore, lessonListView.getItems());
                assertEquals(homeworkBefore, homeworkListView.getItems());

                commandBox.setText("sort by/day o/desc");
                commandBox.getOnAction().handle(new ActionEvent());
                assertEquals(List.of(amy), studentListView.getItems());

                commandBox.setText("list");
                commandBox.getOnAction().handle(new ActionEvent());
                commandBox.setText("sort by/day o/desc");
                commandBox.getOnAction().handle(new ActionEvent());
                assertEquals(List.of(amy, john), studentListView.getItems());
                assertEquals(lessonsBefore, lessonListView.getItems());
                assertEquals(homeworkBefore, homeworkListView.getItems());
                assertEquals("Sorted 2 student(s) by regular meeting day (descending).",
                        resultDisplay.getText());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void scrollOverSections_commandFocused_scrollsWithoutChangingFocus() throws Exception {
        ModelManager model = new ModelManager();
        for (int i = 0; i < 30; i++) {
            model.addStudent(new StudentBuilder().withName("Student " + i).withPhone("9" + (1000000 + i))
                    .withLessons(MONDAY_MATH).withHomeworks(ALGEBRA).build());
        }
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        Logic logic = new LogicManager(model, storage);

        Stage[] stages = new Stage[1];
        CompletableFuture<Void> layoutReady = new CompletableFuture<>();
        Runnable signalLayout = () -> layoutReady.complete(null);
        try {
            runOnFxThread(() -> {
                Stage stage = new Stage();
                stages[0] = stage;
                new MainWindow(stage, logic, Path.of("data", "tutorflow.json")).fillInnerParts();
                stage.show();
                Region root = (Region) stage.getScene().getRoot();
                root.applyCss();
                root.layout();
                TextField commandBox = (TextField) root.lookup("#commandTextField");
                TextArea resultDisplay = (TextArea) root.lookup("#resultDisplay");
                resultDisplay.setText("A command result with enough lines to scroll.\n".repeat(100));
                commandBox.setText("homework ");

                // TextArea's skin can defer its content size until a pulse, even after root.layout().
                stage.getScene().addPostLayoutPulseListener(signalLayout);
                Platform.requestNextPulse();
            });
            layoutReady.get(10, TimeUnit.SECONDS);
            runOnFxThread(() -> {
                Stage stage = stages[0];
                Region root = (Region) stage.getScene().getRoot();
                TextField commandBox = (TextField) root.lookup("#commandTextField");
                TextArea resultDisplay = (TextArea) root.lookup("#resultDisplay");
                ScrollPane resultScrollPane = (ScrollPane) resultDisplay.lookup(".scroll-pane");
                assertTrue(resultScrollPane.getContent().getLayoutBounds().getHeight()
                        > resultScrollPane.getViewportBounds().getHeight(),
                        "result text must overflow before scrolling");
                commandBox.requestFocus();

                String[][] sections = {
                    {"studentListView", ".student-roster-title"},
                    {"lessonListView", "#lessonCount"},
                    {"homeworkListView", "#homeworkCount"}
                };
                for (String[] section : sections) {
                    ListView<?> list = (ListView<?>) root.lookup("#" + section[0]);
                    VirtualFlow<?> flow = (VirtualFlow<?>) list.lookup(".virtual-flow");
                    assertEquals(0, flow.getPosition());
                    scroll(root.lookup(section[1]), -80);
                    assertTrue(flow.getPosition() > 0, section[0] + " did not scroll from its heading");

                    double position = flow.getPosition();
                    scroll(flow.getFirstVisibleCell(), -80, ScrollEvent.VerticalTextScrollUnits.LINES, -3);
                    assertTrue(flow.getPosition() > position, section[0] + " did not scroll over a cell");
                    assertSame(commandBox, stage.getScene().getFocusOwner());
                    assertEquals("homework ", commandBox.getText());
                    assertEquals(-1, list.getSelectionModel().getSelectedIndex());
                }

                resultDisplay.setScrollTop(0);
                scroll(resultDisplay.getParent(), -80);
                assertTrue(resultDisplay.getScrollTop() > 0, "results did not scroll from their surrounding panel");
                assertSame(commandBox, stage.getScene().getFocusOwner());
                assertEquals("homework ", commandBox.getText());

                model.setAddressBook(new AddressBook());
                root.layout();
                for (String[] section : sections) {
                    ListView<?> list = (ListView<?>) root.lookup("#" + section[0]);
                    assertTrue(list.getItems().isEmpty());
                    scroll(root.lookup(section[1]), -80, ScrollEvent.VerticalTextScrollUnits.LINES, -3);
                }
                assertSame(commandBox, stage.getScene().getFocusOwner());
            });
        } finally {
            runOnFxThread(() -> {
                if (stages[0] != null) {
                    if (stages[0].getScene() != null) {
                        stages[0].getScene().removePostLayoutPulseListener(signalLayout);
                    }
                    stages[0].close();
                }
            });
        }
    }

    @Test
    public void closeHelpWindow_focusElsewhere_focusesCommandBox() throws Exception {
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        Logic logic = new LogicManager(new ModelManager(), storage);

        runOnFxThread(() -> {
            Stage stage = new Stage();
            MainWindow mainWindow = new MainWindow(stage, logic, Path.of("data", "tutorflow.json"));
            mainWindow.fillInnerParts();
            Stage helpStage = null;
            try {
                stage.show();
                Region root = (Region) stage.getScene().getRoot();
                root.applyCss();
                TextField commandBox = (TextField) root.lookup("#commandTextField");
                TextArea resultDisplay = (TextArea) root.lookup("#resultDisplay");

                mainWindow.handleHelp();
                helpStage = Window.getWindows().stream()
                        .filter(window -> window instanceof Stage && window != stage && window.isShowing())
                        .map(window -> (Stage) window)
                        .findFirst()
                        .orElseThrow();
                // Without the focus handler, the main window would keep this focus owner after help closes
                resultDisplay.requestFocus();
                assertSame(resultDisplay, stage.getScene().getFocusOwner());

                Event.fireEvent(helpStage, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE,
                        false, false, false, false));

                assertFalse(helpStage.isShowing());
                assertSame(commandBox, stage.getScene().getFocusOwner());
            } finally {
                if (helpStage != null) {
                    helpStage.hide();
                }
                stage.close();
            }
        });
    }

    @Test
    public void fillInnerParts_sectionsCanBeResized() throws Exception {
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        Logic logic = new LogicManager(new ModelManager(), storage);

        runOnFxThread(() -> {
            Stage stage = new Stage();
            new MainWindow(stage, logic, Path.of("data", "tutorflow.json")).fillInnerParts();
            Region root = (Region) stage.getScene().getRoot();
            root.applyCss();

            SplitPane verticalPane = (SplitPane) root.lookup("#verticalSplitPane");
            SplitPane contentPane = (SplitPane) root.lookup("#contentSplitPane");

            assertEquals(Orientation.VERTICAL, verticalPane.getOrientation());
            assertEquals(3, verticalPane.getItems().size());
            assertEquals(2, contentPane.getItems().size());
        });
    }

    private static void scroll(Node target, double deltaY) {
        scroll(target, deltaY, ScrollEvent.VerticalTextScrollUnits.NONE, 0);
    }

    private static void scroll(Node target, double deltaY, ScrollEvent.VerticalTextScrollUnits units,
            double textDeltaY) {
        Bounds bounds = target.getLayoutBounds();
        double x = bounds.getCenterX();
        double y = bounds.getCenterY();
        Point2D scenePoint = target.localToScene(x, y);
        Point2D screenPoint = target.localToScreen(x, y);
        Event.fireEvent(target, new ScrollEvent(ScrollEvent.SCROLL,
                scenePoint.getX(), scenePoint.getY(), screenPoint.getX(), screenPoint.getY(),
                false, false, false, false, false, false, 0, deltaY, 0, deltaY,
                ScrollEvent.HorizontalTextScrollUnits.NONE, 0, units, textDeltaY, 0,
                new PickResult(target, scenePoint.getX(), scenePoint.getY())));
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
