package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.TuitionSubject;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;

public class LessonCardLayoutTest {
    @TempDir
    Path temporaryFolder;

    @BeforeAll
    public static void startJavaFx() {
        FxTestUtil.startJavaFx();
    }

    @Test
    public void lessonMutations_wrappedStudentDetails_doNotOverlapHomeworkCounts() throws Exception {
        Path dataFile = temporaryFolder.resolve("tutorflow.json");
        LogicManager logic = new LogicManager(new ModelManager(), new StorageManager(
                new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        logic.execute("add n/Alpha l/S3 s/MATH p/91234567 gn/Guardian gp/98765432");
        logic.execute("add n/Beta l/S1 s/MATH p/91234568 gn/Guardian gp/98765432");
        logic.execute("lesson add 1 s/MATH d/MONDAY st/09:00 et/10:00");

        FxTestUtil.runOnFxThread(() -> {
            Stage stage = new Stage();
            try {
                new MainWindow(stage, logic, dataFile).fillInnerParts();
                stage.setWidth(740);
                stage.setHeight(600);
                stage.show();
                SplitPane content = (SplitPane) stage.getScene().lookup("#contentSplitPane");
                content.setDividerPositions(0.5);
                assertSeparatedRows(stage);

                RegularLesson editedLesson = new RegularLesson(TuitionSubject.MATH, DayOfWeek.MONDAY,
                        LocalTime.of(8, 0), LocalTime.of(10, 0));
                RegularLesson addedLesson = new RegularLesson(TuitionSubject.MATH, DayOfWeek.FRIDAY,
                        LocalTime.of(9, 0), LocalTime.of(10, 0));
                assertMutationLayout(stage, logic, "lesson edit 1 st/08:00", List.of(editedLesson));
                assertMutationLayout(stage, logic, "lesson add 1 s/MATH d/FRIDAY st/09:00 et/10:00",
                        List.of(editedLesson, addedLesson));
                assertMutationLayout(stage, logic, "lesson delete 2", List.of(editedLesson));
            } finally {
                stage.hide();
            }
        });
    }

    private static void assertMutationLayout(Stage stage, LogicManager logic, String command,
            List<RegularLesson> expectedLessons) {
        TextField input = (TextField) stage.getScene().lookup("#commandTextField");
        input.setText(command);
        input.fireEvent(new ActionEvent());
        assertEquals("", input.getText(), "Command must succeed before testing layout: " + command);
        assertEquals(expectedLessons, logic.getAllStudentList().getFirst().getLessons(), command);
        assertTrue(logic.getAllStudentList().get(1).getLessons().isEmpty(), "Beta must remain unchanged");
        assertSeparatedRows(stage);
    }

    private static void assertSeparatedRows(Stage stage) {
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        Node card = stage.getScene().lookup("#cardPane");
        Node guardianPhone = card.lookup("#guardianPhone");
        Node homeworkCount = card.lookup("#upcomingHomeworkCount");
        assertTrue(guardianPhone.localToScene(guardianPhone.getBoundsInLocal()).getMaxY()
                <= homeworkCount.localToScene(homeworkCount.getBoundsInLocal()).getMinY(),
                "Guardian details must finish above the homework counts: guardian="
                        + guardianPhone.localToScene(guardianPhone.getBoundsInLocal()) + ", counts="
                        + homeworkCount.localToScene(homeworkCount.getBoundsInLocal()) + ", details="
                        + card.lookup("#details").getBoundsInParent() + ", minHeight="
                        + ((Region) card.lookup("#details")).minHeight(-1));
    }
}
