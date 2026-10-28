package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.testutil.FxTestUtil;

public class CommandBoxTest {
    private Stage stage;
    private TextField commandTextField;
    private List<String> submittedCommands;

    @BeforeAll
    public static void startJavaFx() {
        FxTestUtil.startJavaFx();
    }

    @BeforeEach
    public void setUp() throws Exception {
        runOnFxThread(() -> {
            submittedCommands = new ArrayList<>();
            CommandBox commandBox = new CommandBox(commandText -> {
                submittedCommands.add(commandText);
                if (commandText.equals("invalid")) {
                    throw new CommandException("Invalid command");
                }
                return null;
            });
            stage = new Stage();
            stage.setScene(new Scene(commandBox.getRoot()));
            stage.show();
            stage.getScene().getRoot().applyCss();
            commandTextField = (TextField) stage.getScene().getRoot().lookup("#commandTextField");
        });
    }

    @AfterEach
    public void tearDown() throws Exception {
        runOnFxThread(() -> stage.hide());
    }

    @Test
    public void plainArrowKeys_navigateHistoryAndRestoreEmptyInput() throws Exception {
        runOnFxThread(() -> {
            enter("list");
            enter("search Alice");
            enter("filter l/S4");

            pressKey(KeyCode.UP);
            assertRecalled("filter l/S4");
            pressKey(KeyCode.UP);
            assertRecalled("search Alice");
            pressKey(KeyCode.UP);
            assertRecalled("list");
            pressKey(KeyCode.UP);
            assertRecalled("list");
            pressKey(KeyCode.DOWN);
            assertRecalled("search Alice");
            pressKey(KeyCode.DOWN);
            assertRecalled("filter l/S4");
            pressKey(KeyCode.DOWN);
            assertRecalled("");
            pressKey(KeyCode.DOWN);
            assertRecalled("");
        });
    }

    @Test
    public void rejectedCommand_isRecordedAndSkipped_whenRecalled()
            throws Exception {
        runOnFxThread(() -> {
            enter("list");
            enter("invalid");
            assertEquals("invalid", commandTextField.getText());
            assertTrue(commandTextField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));

            pressKey(KeyCode.UP);
            assertRecalled("list");
            assertFalse(commandTextField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            pressKey(KeyCode.DOWN);
            assertRecalled("invalid");
            assertFalse(commandTextField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            assertEquals(List.of("list", "invalid"), submittedCommands);
        });
    }

    @Test
    public void rejectedOnlyCommand_recallClearsErrorStyleEvenWhenTextIsUnchanged() throws Exception {
        runOnFxThread(() -> {
            enter("invalid");
            assertTrue(commandTextField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));

            pressKey(KeyCode.UP);

            assertRecalled("invalid");
            assertFalse(commandTextField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        });
    }

    @Test
    public void duplicateSubmissionAndEmptyEnter_resetHistoryPosition() throws Exception {
        runOnFxThread(() -> {
            enter("list");
            enter("search Alice");
            enter("search Alice");
            pressKey(KeyCode.UP);
            assertRecalled("search Alice");
            pressKey(KeyCode.UP);
            assertRecalled("list");

            enter("");
            pressKey(KeyCode.UP);
            assertRecalled("search Alice");
            assertEquals(List.of("list", "search Alice", "search Alice"), submittedCommands);
        });
    }

    @Test
    public void modifiedArrowKeys_doNotRecallCommands() throws Exception {
        runOnFxThread(() -> {
            enter("list");
            enter("search Alice");
            commandTextField.setText("draft text");
            commandTextField.positionCaret(5);

            pressKey(KeyCode.UP, false, true, false, false);
            assertEquals("draft text", commandTextField.getText());
            pressKey(KeyCode.DOWN, false, false, true, false);
            assertEquals("draft text", commandTextField.getText());
            pressKey(KeyCode.UP, false, false, false, true);
            assertEquals("draft text", commandTextField.getText());
            pressKey(KeyCode.UP, true, false, false, false);
            assertEquals("draft text", commandTextField.getText());

            pressKey(KeyCode.UP);
            assertRecalled("search Alice");
        });
    }

    @Test
    public void arrowsWithNoHistory_leaveInputUnchanged() throws Exception {
        runOnFxThread(() -> {
            commandTextField.setText("typed draft");
            pressKey(KeyCode.UP);
            assertEquals("typed draft", commandTextField.getText());
            pressKey(KeyCode.DOWN);
            assertEquals("typed draft", commandTextField.getText());
        });
    }

    private void enter(String commandText) {
        commandTextField.setText(commandText);
        commandTextField.getOnAction().handle(new ActionEvent());
    }

    private void pressKey(KeyCode keyCode) {
        pressKey(keyCode, false, false, false, false);
    }

    private void pressKey(KeyCode keyCode, boolean shiftDown, boolean controlDown, boolean altDown, boolean metaDown) {
        KeyEvent event = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode,
                shiftDown, controlDown, altDown, metaDown);
        commandTextField.fireEvent(event);
    }

    private void assertRecalled(String expectedCommand) {
        assertEquals(expectedCommand, commandTextField.getText());
        assertEquals(expectedCommand.length(), commandTextField.getCaretPosition());
    }
}
