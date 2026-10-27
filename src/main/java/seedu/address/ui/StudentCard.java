package seedu.address.ui;

import java.time.LocalDate;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import seedu.address.model.student.Student;

/**
 * Displays a student's roster details and the counts of upcoming and overdue homework.
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
    private Label upcomingHomeworkCount;
    @FXML
    private Label overdueHomeworkCount;

    /**
     * Creates a card for the student at the specified one-based displayed index, counting the homework due on or
     * after {@code today} and overdue assigned homework before it.
     */
    public StudentCard(Student student, int displayedIndex, LocalDate today) {
        super(FXML);
        index.setText(displayedIndex + ".");
        name.setText(student.getName().toString());
        academicLevel.setText(student.getAcademicLevel().name());
        subjects.setText(student.getSubjects().toString());
        studentPhone.setText("Student: " + student.getPhone());
        guardianName.setText("Guardian: " + student.getGuardianName());
        guardianName.setTooltip(new Tooltip(student.getGuardianName()));
        guardianPhone.setText("Guardian phone: " + student.getGuardianPhone());
        upcomingHomeworkCount.setText("Upcoming homework: " + student.getUpcomingHomeworkCount(today));
        overdueHomeworkCount.setText("Overdue homework: " + student.getOverdueHomeworkCount(today));
    }
}
