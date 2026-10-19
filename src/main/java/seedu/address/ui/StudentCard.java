package seedu.address.ui;

import java.time.Clock;
import java.time.LocalDate;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.homework.Homework;
import seedu.address.model.student.Student;

/**
 * Displays a student's roster details and the counts of assigned and overdue homework.
 */
public class StudentCard extends UiPart<Region> {
    private static final String FXML = "StudentListCard.fxml";

    @FXML
    private Label index;
    @FXML
    private Label name;
    @FXML
    private Label academicLevel;
    @FXML
    private Label subjects;
    @FXML
    private Label studentPhone;
    @FXML
    private Label guardianName;
    @FXML
    private Label guardianPhone;
    @FXML
    private Label assignedHomeworkCount;
    @FXML
    private Label overdueHomeworkCount;

    /**
     * Creates a card for the student at the specified one-based displayed index.
     */
    public StudentCard(Student student, int displayedIndex) {
        this(student, displayedIndex, Clock.systemDefaultZone());
    }

    /**
     * Creates a card that evaluates overdue homework against the given clock.
     */
    StudentCard(Student student, int displayedIndex, Clock clock) {
        super(FXML);
        index.setText(displayedIndex + ".");
        name.setText(student.getName().toString());
        academicLevel.setText(student.getAcademicLevel().name());
        subjects.setText(student.getSubjects().toString());
        studentPhone.setText("Student: " + student.getPhone());
        guardianName.setText("Guardian: " + student.getGuardianName());
        guardianPhone.setText("Guardian phone: " + student.getGuardianPhone());
        assignedHomeworkCount.setText("Assigned homework: " + student.getAssignedHomeworkCount());
        overdueHomeworkCount.setText("Overdue homework: " + getOverdueHomeworkCount(student, clock));
    }

    private int getOverdueHomeworkCount(Student student, Clock clock) {
        LocalDate today = LocalDate.now(clock);
        return (int) student.getHomeworks().stream()
                .filter(Homework::isAssigned)
                .filter(homework -> homework.getDueDate().getDate().isBefore(today))
                .count();
    }
}
