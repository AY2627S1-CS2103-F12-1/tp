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

    private void enter(String commandText) {
        commandTextField.setText(commandText);
        commandTextField.getOnAction().handle(new ActionEvent());
    }

    private void pressKey(KeyCode keyCode) {
        Event.fireEvent(commandTextField, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode,
                false, false, false, false));
    }

    private void assertRecalled(String expectedText) {
        assertEquals(expectedText, commandTextField.getText());
        assertEquals(expectedText.length(), commandTextField.getCaretPosition());
    }
}
