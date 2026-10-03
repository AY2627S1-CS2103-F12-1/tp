package seedu.address.ui;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.student.Student;

/**
 * Displays the current student roster and its count.
 */
public class StudentListPanel extends UiPart<Region> {
    private static final String FXML = "StudentListPanel.fxml";

    @FXML
    private Label studentCount;
    @FXML
    private ListView<Student> studentListView;

    /**
     * Creates a panel that updates when the given student list changes.
     */
    public StudentListPanel(ObservableList<Student> students) {
        super(FXML);
        studentListView.setItems(students);
        studentListView.setCellFactory(unused -> new StudentListViewCell());
        updateStudentCount(students.size());
        students.addListener((ListChangeListener<Student>) change -> updateStudentCount(students.size()));
    }

    private void updateStudentCount(int count) {
        studentCount.setText(count == 1 ? "1 student" : count + " students");
    }

    /** Displays a student card with its current one-based roster index. */
    private static class StudentListViewCell extends ListCell<Student> {
        @Override
        protected void updateItem(Student student, boolean isEmpty) {
            super.updateItem(student, isEmpty);
            setText(null);
            setGraphic(isEmpty || student == null ? null : new StudentCard(student, getIndex() + 1).getRoot());
        }
    }
}
