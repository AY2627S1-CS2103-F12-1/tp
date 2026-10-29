package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.student.Student;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.StudentBuilder;

public class StudentListPanelTest {
    @BeforeAll
    public static void startJavaFx() {
        FxTestUtil.startJavaFx();
    }

    @Test
    public void updateItem_renderingFailure_logsOnceAndPreservesStudents() throws Exception {
        runOnFxThread(() -> {
            Student student = new StudentBuilder().build();
            ObservableList<Student> students = FXCollections.observableArrayList(student, student);
            AtomicInteger notifications = new AtomicInteger();
            RuntimeException failure = new UncheckedIOException(new IOException("Broken card FXML"));
            StudentListPanel panel = new StudentListPanel(students, notifications::incrementAndGet, (value, index) -> {
                throw failure;
            });
            List<LogRecord> records = new ArrayList<>();
            Handler handler = createRecordingHandler(records);
            Logger logger = LogsCenter.getLogger(StudentListPanel.class);
            logger.addHandler(handler);
            try {
                ListCell<Student> cell = createCell(panel);
                cell.updateIndex(0);
                cell.updateIndex(1);

                assertNull(cell.getGraphic());
                assertTrue(panel.hasRenderingError());
                assertEquals(List.of(student, student), students);
                assertEquals(1, notifications.get());
                assertEquals(1, records.size());
                assertEquals(Level.WARNING, records.getFirst().getLevel());
                assertSame(failure, records.getFirst().getThrown());

                panel.clearRenderingError();
                cell.updateIndex(0);
                assertEquals(2, notifications.get());
            } finally {
                logger.removeHandler(handler);
            }
        });
    }

    @Test
    public void updateItem_assertionFailure_doesNotSuppressProgrammingError() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Student> students = FXCollections.observableArrayList(new StudentBuilder().build());
            AssertionError failure = new AssertionError("Invalid internal invariant");
            StudentListPanel panel = new StudentListPanel(students, () -> { }, (student, index) -> {
                throw failure;
            });
            ListCell<Student> cell = createCell(panel);

            assertSame(failure, assertThrows(AssertionError.class, () -> cell.updateIndex(0)));
        });
    }

    @Test
    public void canRenderStudent_checksEachCellPrecondition() {
        Student student = new StudentBuilder().build();

        assertFalse(StudentListPanel.canRenderStudent(true, student, 0));
        assertFalse(StudentListPanel.canRenderStudent(false, null, 0));
        assertFalse(StudentListPanel.canRenderStudent(false, student, -1));
        assertTrue(StudentListPanel.canRenderStudent(false, student, 0));
    }

    @Test
    public void renderCard_equalReplacement_rebuildsCardByIdentity() throws Exception {
        runOnFxThread(() -> {
            Student original = new StudentBuilder().build();
            Student replacement = new StudentBuilder().build();
            ObservableList<Student> students = FXCollections.observableArrayList(original);
            List<Student> renderedStudents = new ArrayList<>();
            StudentListPanel panel = new StudentListPanel(students, (student, index) -> {
                renderedStudents.add(student);
                return new Region();
            });
            panel.beginSort();
            ListCell<Student> cell = createCell(panel);
            cell.updateIndex(0);
            Region firstCard = (Region) cell.getGraphic();
            cell.updateIndex(0);
            assertSame(firstCard, cell.getGraphic());
            assertEquals(1, renderedStudents.size());

            students.set(0, replacement);
            cell.updateIndex(0);

            assertEquals(original, replacement);
            assertEquals(2, renderedStudents.size());
            assertSame(replacement, renderedStudents.getLast());
            panel.endSort();
        });
    }

    @Test
    public void rendering_multipleCellFailures_preservesFirstUntilSortEnds() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Student> students = FXCollections.observableArrayList(
                    new StudentBuilder().withName("First Student").build(),
                    new StudentBuilder().withName("Second Student").build());
            RuntimeException firstFailure = new IllegalStateException("First failure");
            RuntimeException secondFailure = new IllegalStateException("Second failure");
            AtomicBoolean shouldFail = new AtomicBoolean(true);
            StudentListPanel panel = new StudentListPanel(students, (student, index) -> {
                if (shouldFail.get()) {
                    throw index == 1 ? firstFailure : secondFailure;
                }
                return new Region();
            });
            StudentListPanel.ViewState state = panel.beginSort();
            ListCell<Student> cell = createCell(panel);
            cell.updateIndex(0);
            cell.updateIndex(1);
            shouldFail.set(false);

            assertSame(firstFailure, assertThrows(RuntimeException.class, panel::verifySortRendering));
            assertSame(firstFailure, assertThrows(RuntimeException.class, () -> panel.restoreViewState(state)));

            panel.endSort();
            panel.verifySortRendering();
            panel.restoreViewState(state);
            assertEquals(2, students.size());
        });
    }

    private static ListCell<Student> createCell(StudentListPanel panel) {
        @SuppressWarnings("unchecked")
        ListView<Student> listView = (ListView<Student>) panel.getRoot().lookup("#studentListView");
        ListCell<Student> cell = listView.getCellFactory().call(listView);
        cell.updateListView(listView);
        return cell;
    }

    private static Handler createRecordingHandler(List<LogRecord> records) {
        return new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
    }
}
