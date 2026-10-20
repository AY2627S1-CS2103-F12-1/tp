package seedu.address.ui;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.student.Student;

/**
 * Displays the current student roster and its count.
 */
public class StudentListPanel extends UiPart<Region> {
    private static final String FXML = "StudentListPanel.fxml";
    private static final Logger logger = LogsCenter.getLogger(StudentListPanel.class);

    private final Runnable renderingErrorHandler;
    private final BiFunction<Student, Integer, Region> studentCardFactory;
    private boolean hasRenderingError;

    @FXML
    private Label studentCount;
    @FXML
    private ListView<Student> studentListView;

    /**
     * Creates a panel that updates when the given student list changes.
     */
    public StudentListPanel(ObservableList<Student> students) {
        this(students, () -> { });
    }

    /**
     * Creates a panel that runs {@code renderingErrorHandler} after logging an unexpected card-rendering failure.
     */
    public StudentListPanel(ObservableList<Student> students, Runnable renderingErrorHandler) {
        this(students, renderingErrorHandler, (student, index) ->
                new StudentCard(student, index, LocalDate.now()).getRoot());
    }

    /**
     * Creates a panel with an injectable card factory so rendering failures can be tested independently.
     */
    StudentListPanel(ObservableList<Student> students, Runnable renderingErrorHandler,
            BiFunction<Student, Integer, Region> studentCardFactory) {
        super(FXML);
        requireAllNonNull(students, renderingErrorHandler, studentCardFactory);
        this.renderingErrorHandler = renderingErrorHandler;
        this.studentCardFactory = studentCardFactory;
        // Register before ListView so a new result set starts a fresh rendering attempt.
        students.addListener((ListChangeListener<Student>) change -> {
            clearRenderingError();
            updateStudentCount(students.size());
        });
        studentListView.setItems(students);
        studentListView.setCellFactory(unused -> new StudentListViewCell());
        updateStudentCount(students.size());
    }

    private void updateStudentCount(int count) {
        studentCount.setText(count == 1 ? "1 student" : count + " students");
    }

    boolean hasRenderingError() {
        return hasRenderingError;
    }

    /**
     * Starts a new command's rendering attempt without changing the list, selection or scroll position.
     */
    void clearRenderingError() {
        hasRenderingError = false;
    }

    /**
     * Logs the technical cause and notifies the owner once per rendering attempt.
     */
    private void handleRenderingError(RuntimeException exception) {
        if (hasRenderingError) {
            return;
        }
        hasRenderingError = true;
        logger.log(Level.WARNING, "Unexpected error while rendering student results", exception);
        renderingErrorHandler.run();
    }

    /** Clears the current selection and returns to the first student. */
    public void showFromTop() {
        studentListView.getSelectionModel().clearSelection();
        studentListView.scrollTo(0);
    }

    /**
     * Displays a student card with its current one-based roster index.
     */
    private class StudentListViewCell extends ListCell<Student> {
        @Override
        protected void updateItem(Student student, boolean isEmpty) {
            super.updateItem(student, isEmpty);
            setText(null);
            if (isEmpty || student == null) {
                setGraphic(null);
                return;
            }
            try {
                setGraphic(studentCardFactory.apply(student, getIndex() + 1));
            } catch (RuntimeException exception) {
                setGraphic(null);
                handleRenderingError(exception);
            }
        }
    }
}
