package seedu.address.ui;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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
    private final Set<String> overdueHomeworkEntries = new HashSet<>();
    private final Clock clock;

    @FXML
    private Label homeworkCount;
    @FXML
    private VBox homeworkSection;
    @FXML
    private ListView<String> homeworkListView;
    @FXML
    private Label lessonCount;
    @FXML
    private VBox lessonSection;
    @FXML
    private ListView<String> lessonListView;

    /**
     * Creates a panel that updates whenever the given student roster changes.
     */
    public UpcomingPanel(ObservableList<Student> students) {
        this(students, Clock.systemDefaultZone());
    }

    /**
     * Creates a panel that evaluates overdue homework against the given clock.
     */
    UpcomingPanel(ObservableList<Student> students, Clock clock) {
        super(FXML);
        this.clock = clock;
        lessonListView.setItems(lessonEntries);
        homeworkListView.setItems(homeworkEntries);
        lessonListView.setCellFactory(unused -> new WrappedTextListCell(entry -> false));
        homeworkListView.setCellFactory(unused -> new WrappedTextListCell(overdueHomeworkEntries::contains));
        lessonListView.setPlaceholder(new Label("No regular lessons"));
        homeworkListView.setPlaceholder(new Label("No outstanding homework"));
        ScrollSupport.enableScrolling(lessonSection, lessonListView);
        ScrollSupport.enableScrolling(homeworkSection, homeworkListView);

        updateEntries(students);
        students.addListener((ListChangeListener<Student>) change -> updateEntries(students));
    }

    private void updateEntries(List<Student> students) {
        lessonEntries.setAll(LessonSchedule.from(students).stream()
                .map(entry -> entry.lesson().getDayOfWeek().name().substring(0, 3)
                        + " " + entry.lesson().getStartTime() + " - " + entry.lesson().getEndTime()
                        + "\n" + entry.student().getName() + " \u00b7 " + entry.lesson().getSubject())
                .toList());
        List<HomeworkEntry> outstandingHomework = students.stream()
                .flatMap(student -> student.getHomeworks().stream()
                        .filter(homework -> homework.getScore().isEmpty())
                        .map(homework -> new HomeworkEntry(student, homework)))
                .sorted(HOMEWORK_ORDER)
                .toList();
        overdueHomeworkEntries.clear();
        LocalDate today = LocalDate.now(clock);
        outstandingHomework.stream()
                .filter(entry -> entry.homework().isOverdue(today))
                .map(entry -> formatHomework(entry, true))
                .forEach(overdueHomeworkEntries::add);
        homeworkEntries.setAll(outstandingHomework.stream()
                .map(entry -> formatHomework(entry, entry.homework().isOverdue(today)))
                .toList());
        lessonCount.setText(formatCount(lessonEntries.size(), "lesson"));
        homeworkCount.setText(formatCount(homeworkEntries.size(), "task"));
    }

    private String formatHomework(HomeworkEntry entry, boolean isOverdue) {
        return entry.homework().getTitle() + (isOverdue ? " (overdue)" : "") + "\n" + entry.student().getName()
                + " \u00b7 " + entry.homework().getSubject() + " \u00b7 Due " + entry.homework().getDueDate();
    }

    private String formatCount(int count, String singular) {
        return count == 1 ? "1 " + singular : count + " " + singular + "s";
    }

    /**
     * Associates a homework record with the student it belongs to.
     */
    private record HomeworkEntry(Student student, Homework homework) {
    }

    /**
     * Displays wrapped sidebar entries without truncating their second line.
     */
    private static class WrappedTextListCell extends ListCell<String> {
        private final Label entryLabel = new Label();
        private final Predicate<String> isHighlighted;

        WrappedTextListCell(Predicate<String> isHighlighted) {
            this.isHighlighted = isHighlighted;
            setPrefWidth(0);
            entryLabel.setWrapText(true);
            entryLabel.setMinHeight(Region.USE_PREF_SIZE);
            entryLabel.maxWidthProperty().bind(Bindings.createDoubleBinding(() -> Math.max(0,
                    getWidth() - getInsets().getLeft() - getInsets().getRight()), widthProperty(), insetsProperty()));
            entryLabel.getStyleClass().add("upcoming-entry");
        }

        @Override
        protected void updateItem(String entry, boolean isEmpty) {
            super.updateItem(entry, isEmpty);
            setText(null);
            entryLabel.setText(isEmpty || entry == null ? null : entry);
            entryLabel.getStyleClass().remove("overdue-homework");
            if (!isEmpty && entry != null && isHighlighted.test(entry)) {
                entryLabel.getStyleClass().add("overdue-homework");
            }
            setGraphic(isEmpty ? null : entryLabel);
        }
    }
}
