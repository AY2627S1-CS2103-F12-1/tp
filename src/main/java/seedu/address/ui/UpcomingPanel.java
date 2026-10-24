package seedu.address.ui;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

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
import seedu.address.model.homework.HomeworkStatus;
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
            .thenComparing(entry -> entry.homework().getTitle().toString(), String.CASE_INSENSITIVE_ORDER)
            .thenComparing(entry -> entry.student().getPhone().toString());

    private final ObservableList<ReportEntry> lessonEntries = FXCollections.observableArrayList();
    private final ObservableList<ReportEntry> homeworkEntries = FXCollections.observableArrayList();
    private final ObservableList<Student> students;

    @FXML
    private Label homeworkCount;
    @FXML
    private VBox homeworkSection;
    @FXML
    private ListView<ReportEntry> homeworkListView;
    @FXML
    private Label lessonCount;
    @FXML
    private VBox lessonSection;
    @FXML
    private ListView<ReportEntry> lessonListView;

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
        this.students = students;
        lessonListView.setItems(lessonEntries);
        homeworkListView.setItems(homeworkEntries);
        lessonListView.setCellFactory(unused -> new WrappedTextListCell());
        homeworkListView.setCellFactory(unused -> new WrappedTextListCell());
        lessonListView.setPlaceholder(new Label("No regular lessons"));
        homeworkListView.setPlaceholder(new Label("No outstanding homework"));
        ScrollSupport.enableScrolling(lessonSection, lessonListView);
        ScrollSupport.enableScrolling(homeworkSection, homeworkListView);

        updateEntries(students, LocalDate.now(clock));
        students.addListener((ListChangeListener<Student>) change -> updateEntries(students, LocalDate.now(clock)));
    }

    /**
     * Refreshes date-dependent entries using the same local date as the student cards.
     */
    void refreshDate(LocalDate today) {
        updateHomeworkEntries(students, today);
    }

    /**
     * Rebuilds both reports and their counts from the full roster, highlighting homework overdue on the given date.
     */
    private void updateEntries(List<Student> students, LocalDate today) {
        updateLessonEntries(students);
        updateHomeworkEntries(students, today);
    }

    /**
     * Rebuilds the weekly lesson report and its count from the full roster.
     */
    private void updateLessonEntries(List<Student> students) {
        lessonEntries.setAll(LessonSchedule.buildSchedule(students).stream()
                .map(entry -> new ReportEntry(entry.lesson().getDayOfWeek().name().substring(0, 3)
                        + " " + entry.lesson().getStartTime() + " - " + entry.lesson().getEndTime()
                        + "\n" + formatStudent(entry.student()) + " \u00b7 " + entry.lesson().getSubject(), false))
                .toList());
        lessonCount.setText(formatCount(lessonEntries.size(), "lesson"));
    }

    /**
     * Rebuilds outstanding homework and its count, highlighting entries overdue on the given date.
     */
    private void updateHomeworkEntries(List<Student> students, LocalDate today) {
        List<HomeworkEntry> outstandingHomework = students.stream()
                .flatMap(student -> student.getHomeworks().stream()
                        .filter(homework -> homework.getStatus() == HomeworkStatus.ASSIGNED)
                        .map(homework -> new HomeworkEntry(student, homework)))
                .sorted(HOMEWORK_ORDER)
                .toList();
        homeworkEntries.setAll(outstandingHomework.stream()
                .map(entry -> createHomeworkReportEntry(entry, today))
                .toList());
        homeworkCount.setText(formatCount(homeworkEntries.size(), "task"));
    }

    private ReportEntry createHomeworkReportEntry(HomeworkEntry entry, LocalDate today) {
        boolean isOverdue = entry.homework().isOverdue(today);
        String text = entry.homework().getTitle() + (isOverdue ? " (overdue)" : "")
                + "\n" + formatStudent(entry.student())
                + " \u00b7 " + entry.homework().getSubject() + " \u00b7 Due " + entry.homework().getDueDate();
        return new ReportEntry(text, isOverdue);
    }

    private String formatStudent(Student student) {
        return student.getName() + " (" + student.getPhone() + ")";
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
     * Keeps an entry's display text and overdue state together.
     */
    private record ReportEntry(String text, boolean isOverdue) {
        @Override
        public String toString() {
            return text;
        }
    }

    /**
     * Displays wrapped sidebar entries without truncating their second line.
     */
    private static class WrappedTextListCell extends ListCell<ReportEntry> {
        private final Label entryLabel = new Label();

        WrappedTextListCell() {
            setPrefWidth(0);
            entryLabel.setWrapText(true);
            entryLabel.setMinHeight(Region.USE_PREF_SIZE);
            entryLabel.maxWidthProperty().bind(Bindings.createDoubleBinding(() -> Math.max(0,
                    getWidth() - getInsets().getLeft() - getInsets().getRight()), widthProperty(), insetsProperty()));
            entryLabel.getStyleClass().add("upcoming-entry");
        }

        @Override
        protected void updateItem(ReportEntry entry, boolean isEmpty) {
            super.updateItem(entry, isEmpty);
            setText(null);
            entryLabel.setText(isEmpty || entry == null ? null : entry.text());
            entryLabel.getStyleClass().remove("overdue-homework");
            if (!isEmpty && entry != null && entry.isOverdue()) {
                entryLabel.getStyleClass().add("overdue-homework");
            }
            setGraphic(isEmpty ? null : entryLabel);
        }
    }
}
