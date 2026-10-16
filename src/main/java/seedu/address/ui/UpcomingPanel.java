package seedu.address.ui;

import java.util.Comparator;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.LessonSchedule;
import seedu.address.model.student.Student;

/**
 * Displays the weekly lesson schedule and outstanding homework for the current student roster.
 */
public class UpcomingPanel extends UiPart<Region> {
    private static final String FXML = "UpcomingPanel.fxml";
    private static final Comparator<HomeworkEntry> HOMEWORK_ORDER = Comparator
            .comparing((HomeworkEntry entry) -> entry.homework().getDueDate().getDate())
            .thenComparing(entry -> entry.student().getName().toString(), String.CASE_INSENSITIVE_ORDER)
            .thenComparing(entry -> entry.homework().getTitle().toString(), String.CASE_INSENSITIVE_ORDER);

    private final ObservableList<String> lessonEntries = FXCollections.observableArrayList();
    private final ObservableList<String> homeworkEntries = FXCollections.observableArrayList();

    @FXML
    private Label homeworkCount;
    @FXML
    private ListView<String> homeworkListView;
    @FXML
    private Label lessonCount;
    @FXML
    private ListView<String> lessonListView;

    /**
     * Creates a panel that updates whenever the given student roster changes.
     */
    public UpcomingPanel(ObservableList<Student> students) {
        super(FXML);
        lessonListView.setItems(lessonEntries);
        homeworkListView.setItems(homeworkEntries);
        lessonListView.setCellFactory(unused -> new WrappedTextListCell());
        homeworkListView.setCellFactory(unused -> new WrappedTextListCell());
        lessonListView.setPlaceholder(new Label("No regular lessons"));
        homeworkListView.setPlaceholder(new Label("No outstanding homework"));

        updateEntries(students);
        students.addListener((ListChangeListener<Student>) change -> updateEntries(students));
    }

    private void updateEntries(List<Student> students) {
        lessonEntries.setAll(LessonSchedule.from(students).stream()
                .map(entry -> entry.lesson().getDayOfWeek().name().substring(0, 3)
                        + " " + entry.lesson().getStartTime() + " - " + entry.lesson().getEndTime()
                        + "\n" + entry.student().getName() + " \u00b7 " + entry.lesson().getSubject())
                .toList());
        homeworkEntries.setAll(students.stream()
                .flatMap(student -> student.getHomeworks().stream()
                        .filter(Homework::isAssigned)
                        .map(homework -> new HomeworkEntry(student, homework)))
                .sorted(HOMEWORK_ORDER)
                .map(entry -> entry.homework().getTitle() + "\n" + entry.student().getName() + " \u00b7 "
                        + entry.homework().getSubject() + " \u00b7 Due " + entry.homework().getDueDate())
                .toList());
        lessonCount.setText(formatCount(lessonEntries.size(), "lesson"));
        homeworkCount.setText(formatCount(homeworkEntries.size(), "task"));
    }

    private String formatCount(int count, String singular) {
        return count == 1 ? "1 " + singular : count + " " + singular + "s";
    }

    /** Associates a homework record with the student it belongs to. */
    private record HomeworkEntry(Student student, Homework homework) {
    }

    /** Displays wrapped sidebar entries without truncating their second line. */
    private static class WrappedTextListCell extends ListCell<String> {
        private final Label entryLabel = new Label();

        WrappedTextListCell() {
            entryLabel.setWrapText(true);
            entryLabel.maxWidthProperty().bind(widthProperty().subtract(18));
            entryLabel.getStyleClass().add("upcoming-entry");
        }

        @Override
        protected void updateItem(String entry, boolean isEmpty) {
            super.updateItem(entry, isEmpty);
            setText(null);
            entryLabel.setText(isEmpty || entry == null ? null : entry);
            setGraphic(isEmpty ? null : entryLabel);
        }
    }
}
