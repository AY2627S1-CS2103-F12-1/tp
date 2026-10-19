package seedu.address.ui;

import java.time.LocalDate;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.student.Student;

/**
 * Displays the six fields of a student in the roster and the number of homework records due today or later.
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

    /**
     * Creates a card for the student at the specified one-based displayed index, counting the homework due on or
     * after {@code today}.
     */
    public StudentCard(Student student, int displayedIndex, LocalDate today) {
        super(FXML);
        index.setText(displayedIndex + ".");
        name.setText(student.getName().toString());
        academicLevel.setText(student.getAcademicLevel().name());
        subjects.setText(student.getSubjects().toString());
        studentPhone.setText("Student: " + student.getPhone());
        guardianName.setText("Guardian: " + student.getGuardianName());
        guardianPhone.setText("Guardian phone: " + student.getGuardianPhone());
        upcomingHomeworkCount.setText("Upcoming homework: " + student.getUpcomingHomeworkCount(today));
    }
}
