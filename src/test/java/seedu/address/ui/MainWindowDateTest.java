package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalRegularLessons.MONDAY_MATH;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.StudentBuilder;

public class MainWindowDateTest {
    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException exception) {
            // Another UI test has already started the toolkit.
        }
        Platform.setImplicitExit(false);
    }

    @Test
    public void dateBoundary_withoutCommands_refreshesSidebarAndCachedCards() throws Exception {
        MutableClock clock = new MutableClock(Instant.parse("2026-10-15T15:59:59.900Z"));
        CompletableFuture<Void> refreshed = new CompletableFuture<>();
        Stage[] stages = new Stage[1];
        runOnFxThread(() -> {
            Stage stage = new Stage();
            stages[0] = stage;
            MainWindow window = createWindow(stage, clock);
            window.show();
            assertCounts(stage, false);
            ListView<?> homework = (ListView<?>) stage.getScene().getRoot().lookup("#homeworkListView");
            homework.getItems().addListener((ListChangeListener<Object>) change -> refreshed.complete(null));
            clock.instant = Instant.parse("2026-10-15T16:00:00Z");
        });
        try {
            refreshed.get(10, TimeUnit.SECONDS);
            runOnFxThread(() -> assertCounts(stages[0], true));
        } finally {
            runOnFxThread(() -> stages[0].close());
        }
    }

    @Test
    public void listCommand_afterDateChange_refreshesSidebarAndCachedCards() throws Exception {
        runOnFxThread(() -> {
            MutableClock clock = new MutableClock(Instant.parse("2026-10-15T04:00:00Z"));
            Stage stage = new Stage();
            createWindow(stage, clock);
            try {
                assertCounts(stage, false);
                clock.instant = Instant.parse("2026-10-16T04:00:00Z");
                TextField commandBox = (TextField) stage.getScene().getRoot().lookup("#commandTextField");
                commandBox.setText("list");
                commandBox.getOnAction().handle(new ActionEvent());
                assertCounts(stage, true);
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void refreshDate_dateChanges_preservesWeeklyLessonsAndSelection() throws Exception {
        runOnFxThread(() -> {
            MutableClock clock = new MutableClock(Instant.parse("2026-10-15T04:00:00Z"));
            Stage stage = new Stage();
            MainWindow window = createWindow(stage, clock);
            try {
                assertCounts(stage, false);
                ListView<?> lessons = (ListView<?>) stage.getScene().getRoot().lookup("#lessonListView");
                lessons.getSelectionModel().select(0);
                Object selectedLesson = lessons.getSelectionModel().getSelectedItem();
                AtomicInteger lessonChanges = new AtomicInteger();
                lessons.getItems().addListener((ListChangeListener<Object>) change -> lessonChanges.incrementAndGet());

                clock.instant = Instant.parse("2026-10-16T04:00:00Z");
                window.refreshDate();

                assertCounts(stage, true);
                assertEquals(0, lessonChanges.get());
                assertEquals(0, lessons.getSelectionModel().getSelectedIndex());
                assertSame(selectedLesson, lessons.getSelectionModel().getSelectedItem());
                assertEquals("1 lesson", ((Label) stage.getScene().getRoot().lookup("#lessonCount")).getText());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void show_afterDateChangesWhileHidden_refreshesBothDisplays() throws Exception {
        runOnFxThread(() -> {
            MutableClock clock = new MutableClock(Instant.parse("2026-10-15T04:00:00Z"));
            Stage stage = new Stage();
            MainWindow window = createWindow(stage, clock);
            try {
                window.show();
                assertCounts(stage, false);
                stage.hide();
                clock.instant = Instant.parse("2026-10-16T04:00:00Z");
                window.show();
                assertCounts(stage, true);

                // Clock corrections back to the due date must also rebuild cached counts and remove highlighting.
                clock.instant = Instant.parse("2026-10-15T04:00:00Z");
                window.refreshDate();
                assertCounts(stage, false);
            } finally {
                stage.close();
            }
        });
    }

    private MainWindow createWindow(Stage stage, Clock clock) {
        ModelManager model = new ModelManager();
        model.addStudent(new StudentBuilder().withHomeworks(ALGEBRA).withLessons(MONDAY_MATH).build());
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("tutorflow.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        MainWindow window = new MainWindow(stage, new LogicManager(model, storage),
                temporaryFolder.resolve("tutorflow.json"), clock);
        window.fillInnerParts();
        return window;
    }

    private static void assertCounts(Stage stage, boolean isOverdue) {
        Parent root = stage.getScene().getRoot();
        root.applyCss();
        root.layout();
        ListView<?> homework = (ListView<?>) root.lookup("#homeworkListView");
        assertEquals(isOverdue, homework.getItems().getFirst().toString().contains("(overdue)"));
        Label entry = (Label) homework.lookup(".upcoming-entry");
        assertEquals(isOverdue, entry.getStyleClass().contains("overdue-homework"));
        Label overdueCount = (Label) root.lookup("#overdueHomeworkCount");
        Label upcomingCount = (Label) root.lookup("#upcomingHomeworkCount");
        assertEquals("Overdue homework: " + (isOverdue ? 1 : 0), overdueCount.getText());
        assertEquals("Upcoming homework: " + (isOverdue ? 0 : 1), upcomingCount.getText());
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    private static class MutableClock extends Clock {
        private Instant instant;

        MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("Asia/Singapore");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
