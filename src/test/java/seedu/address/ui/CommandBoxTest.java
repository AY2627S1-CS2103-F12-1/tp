package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.testutil.FxTestUtil;

public class CommandBoxTest {
    private static final String FAILING_COMMAND = "hw ls 99";

    private final List<String> executedCommands = new ArrayList<>();
    private TextField commandTextField;

    @BeforeAll
    public static void startJavaFx() {
        FxTestUtil.startJavaFx();
    }

    @BeforeEach
    public void setUp() throws Exception {
        runOnFxThread(() -> {
            CommandBox commandBox = new CommandBox(commandText -> {
                executedCommands.add(commandText);
                if (commandText.equals(FAILING_COMMAND)) {
                    throw new ParseException("The student index provided is invalid.");
                }
                return new CommandResult("Done.");
            });
            // A scene gives the text field its skin, which handles arrow keys that are not consumed
            new Scene(commandBox.getRoot()).getRoot().applyCss();
            commandTextField = (TextField) commandBox.getRoot().lookup("#commandTextField");
        });
    }

    @Test
    public void pressUp_emptyHistory_keepsTypedText() throws Exception {
        runOnFxThread(() -> {
            commandTextField.setText("hw ls");
            commandTextField.end();

            pressKey(KeyCode.UP);
            assertRecalled("hw ls");
            pressKey(KeyCode.DOWN);
            assertRecalled("hw ls");
        });
    }

    @Test
    public void pressUpAndDown_afterCommands_recallsCommandsInOrder() throws Exception {
        runOnFxThread(() -> {
            enter("list");
            enter("hw ls 2");
            assertEquals("", commandTextField.getText());

            pressKey(KeyCode.UP);
            assertRecalled("hw ls 2");
            pressKey(KeyCode.UP);
            assertRecalled("list");
            pressKey(KeyCode.UP);
            assertRecalled("list");
            pressKey(KeyCode.DOWN);
            assertRecalled("hw ls 2");
            pressKey(KeyCode.DOWN);
            assertRecalled("");
        });
    }

    @Test
    public void pressUp_afterFailedCommand_recallsFailedCommand() throws Exception {
        runOnFxThread(() -> {
            enter("hw ls 2");
            enter(FAILING_COMMAND);
            assertEquals(FAILING_COMMAND, commandTextField.getText());
            assertTrue(commandTextField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));

            commandTextField.setText("hw ls 3");
            pressKey(KeyCode.UP);
            assertRecalled(FAILING_COMMAND);
            pressKey(KeyCode.UP);
            assertRecalled("hw ls 2");
            assertFalse(commandTextField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        });
    }

    @Test
    public void enter_recalledCommand_runsItAndResetsHistoryPosition() throws Exception {
        runOnFxThread(() -> {
            enter("list");
            enter("hw ls 2");
            pressKey(KeyCode.UP);
            pressKey(KeyCode.UP);

            commandTextField.getOnAction().handle(new ActionEvent());

            assertEquals(List.of("list", "hw ls 2", "list"), executedCommands);
            pressKey(KeyCode.DOWN);
            assertRecalled("");
            pressKey(KeyCode.UP);
            assertRecalled("list");
            pressKey(KeyCode.UP);
            assertRecalled("hw ls 2");
        });
    }

    @Test
    public void enter_emptyBox_notRecordedOrExecuted() throws Exception {
        runOnFxThread(() -> {
            enter("list");
            enter("");
            enter("hw ls 2");

            assertEquals(List.of("list", "hw ls 2"), executedCommands);
            pressKey(KeyCode.UP);
            assertRecalled("hw ls 2");
            // A recorded empty command would show here as an empty box
            pressKey(KeyCode.UP);
            assertRecalled("list");
        });
    }

    @Test
    public void enter_emptiedBoxWhileBrowsing_resetsHistoryPosition() throws Exception {
        runOnFxThread(() -> {
            enter("list");
            enter("hw ls 2");
            enter("hw ls 3");
            pressKey(KeyCode.UP);
            pressKey(KeyCode.UP);
            commandTextField.clear();

            commandTextField.getOnAction().handle(new ActionEvent());

            pressKey(KeyCode.UP);
            assertRecalled("hw ls 3");
        });
    }

    @Test
    public void enter_whitespaceOnlyWhileBrowsing_resetsPositionWithoutRecording() throws Exception {
        runOnFxThread(() -> {
            enter("list");
            enter("hw ls 2");
            enter("hw ls 3");
            pressKey(KeyCode.UP);
            pressKey(KeyCode.UP);

            enter("   ");

            pressKey(KeyCode.UP);
            assertRecalled("hw ls 3");
            pressKey(KeyCode.UP);
            assertRecalled("hw ls 2");
        });
    }

    @Test
    public void pressShiftUp_typedText_selectsToStart() throws Exception {
        runOnFxThread(() -> {
            enterCommandsThenTypeFindAlex();

            pressKey(KeyCode.UP, true, false, false, false);

            assertEquals("find n/Alex", commandTextField.getText());
            assertEquals(new IndexRange(0, 6), commandTextField.getSelection());
        });
    }

    @Test
    public void pressShiftDown_typedText_selectsToEnd() throws Exception {
        runOnFxThread(() -> {
            enterCommandsThenTypeFindAlex();

            pressKey(KeyCode.DOWN, true, false, false, false);

            assertEquals("find n/Alex", commandTextField.getText());
            assertEquals(new IndexRange(6, 11), commandTextField.getSelection());
        });
    }

    @Test
    public void pressUp_ctrlAltOrMetaDown_keepsTypedText() throws Exception {
        runOnFxThread(() -> {
            enterCommandsThenTypeFindAlex();

            // Ctrl, and Meta (Cmd on macOS), also cover the platform's Shortcut key
            pressKey(KeyCode.UP, false, true, false, false);
            assertEquals("find n/Alex", commandTextField.getText());
            pressKey(KeyCode.UP, false, false, true, false);
            assertEquals("find n/Alex", commandTextField.getText());
            pressKey(KeyCode.UP, false, false, false, true);
            assertEquals("find n/Alex", commandTextField.getText());

            // The history position has not moved, so plain Up still starts from the newest command
            pressKey(KeyCode.UP);
            assertRecalled("hw ls 2");
        });
    }

    @Test
    public void pressDown_ctrlAltOrMetaDown_keepsTypedText() throws Exception {
        runOnFxThread(() -> {
            enterCommandsThenTypeFindAlex();
            pressKey(KeyCode.UP);
            pressKey(KeyCode.UP);
            commandTextField.setText("find n/Alex");

            pressKey(KeyCode.DOWN, false, true, false, false);
            assertEquals("find n/Alex", commandTextField.getText());
            pressKey(KeyCode.DOWN, false, false, true, false);
            assertEquals("find n/Alex", commandTextField.getText());
            pressKey(KeyCode.DOWN, false, false, false, true);
            assertEquals("find n/Alex", commandTextField.getText());

            // The history position has not moved, so plain Down shows the command after the oldest one
            pressKey(KeyCode.DOWN);
            assertRecalled("hw ls 2");
        });
    }

    /**
     * Enters {@code list} and {@code hw ls 2}, then types {@code find n/Alex} with the caret after {@code n}.
     */
    private void enterCommandsThenTypeFindAlex() {
        enter("list");
        enter("hw ls 2");
        commandTextField.setText("find n/Alex");
        commandTextField.positionCaret(6);
    }

    private void enter(String commandText) {
        commandTextField.setText(commandText);
        commandTextField.getOnAction().handle(new ActionEvent());
    }

    private void pressKey(KeyCode keyCode) {
        pressKey(keyCode, false, false, false, false);
    }

    private void pressKey(KeyCode keyCode, boolean isShiftDown, boolean isControlDown, boolean isAltDown,
            boolean isMetaDown) {
        Event.fireEvent(commandTextField, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode,
                isShiftDown, isControlDown, isAltDown, isMetaDown));
    }

    private void assertRecalled(String expectedText) {
        assertEquals(expectedText, commandTextField.getText());
        assertEquals(expectedText.length(), commandTextField.getCaretPosition());
    }
}
