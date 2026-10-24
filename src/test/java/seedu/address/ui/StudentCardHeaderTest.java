package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.StudentBuilder;

public class StudentCardHeaderTest {
    @BeforeAll
    public static void startJavaFx() {
        FxTestUtil.startJavaFx();
    }

    @Test
    public void threeSubjects_narrowCard_keepsNameAndIndexReadable() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Region card = new StudentCard(new StudentBuilder()
                    .withName("Alexander Christopher Emmanuel Fitzgerald Goh")
                    .withSubjects("MATH,PHYSICS,CHEMISTRY").build(), 1, LocalDate.of(2026, 10, 24)).getRoot();
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(new StackPane(card), 282, 400));
                stage.show();
                stage.getScene().getRoot().applyCss();
                stage.getScene().getRoot().layout();
                Node name = card.lookup("#name");
                Node index = card.lookup("#index");
                assertTrue(name.getBoundsInLocal().getWidth() >= 150,
                        "The name must not collapse into a single-character column");
                assertTrue(name.getBoundsInLocal().getHeight() < 200);
                assertTrue(index.localToScene(index.getBoundsInLocal()).getMaxY() < 100,
                        "The student index must remain near the top of the card");
                Node subjects = card.lookup("#subjects");
                assertTrue(subjects.localToScene(subjects.getBoundsInLocal()).getMaxX()
                        <= stage.getScene().getWidth(), "Subject badges must fit without horizontal scrolling");
            } finally {
                stage.hide();
            }
        });
    }
}
