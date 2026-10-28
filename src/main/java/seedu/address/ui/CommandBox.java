package seedu.address.ui;

import java.util.Optional;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The UI component that is responsible for receiving user command inputs.
 */
public class CommandBox extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "CommandBox.fxml";

    private final CommandExecutor commandExecutor;
    private final CommandHistory commandHistory = new CommandHistory();

    @FXML
    private TextField commandTextField;

    /**
     * Creates a {@code CommandBox} with the given {@code CommandExecutor}.
     */
    public CommandBox(CommandExecutor commandExecutor) {
        super(FXML);
        this.commandExecutor = commandExecutor;
        // calls #setStyleToDefault() whenever there is a change to the text of the command box.
        commandTextField.textProperty().addListener((unused1, unused2, unused3) -> setStyleToDefault());
        commandTextField.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }

    /**
     * Handles the Enter button pressed event.
     */
    @FXML
    private void handleCommandEntered() {
        String commandText = commandTextField.getText();
        commandHistory.add(commandText);
        if (commandText.equals("")) {
            return;
        }

        try {
            commandExecutor.execute(commandText);
            commandTextField.setText("");
        } catch (CommandException | ParseException e) {
            setStyleToIndicateCommandFailure();
        }
    }

    /**
     * Recalls commands for plain Up and Down presses while leaving modified arrow keys to the text field.
     * Consumes plain arrow-key events so they do not also move the caret.
     *
     * @param event the key event to handle
     */
    private void handleKeyPressed(KeyEvent event) {
        if (event.isShiftDown() || event.isControlDown() || event.isAltDown() || event.isMetaDown()) {
            return;
        }

        if (event.getCode() == KeyCode.UP) {
            showPreviousCommand();
            event.consume();
        } else if (event.getCode() == KeyCode.DOWN) {
            commandHistory.next().ifPresent(this::showRecalledCommand);
            event.consume();
        }
    }

    /**
     * Shows the previous command, skipping it when it is already in the command box.
     */
    private void showPreviousCommand() {
        Optional<String> previousCommand = commandHistory.previous();
        if (previousCommand.equals(Optional.of(commandTextField.getText()))) {
            previousCommand = commandHistory.previous();
        }
        previousCommand.ifPresent(this::showRecalledCommand);
    }

    /**
     * Replaces the command box text with {@code commandText}, puts the caret at its end and clears error styling.
     *
     * @param commandText the command to show
     */
    private void showRecalledCommand(String commandText) {
        commandTextField.setText(commandText);
        commandTextField.end();
        // Setting the same text again does not trigger the text listener, so reset the style here too.
        setStyleToDefault();
    }

    /**
     * Moves keyboard focus to the command text field.
     */
    public void requestFocus() {
        commandTextField.requestFocus();
    }

    /**
     * Sets the command box style to use the default style.
     */
    private void setStyleToDefault() {
        commandTextField.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    /**
     * Sets the command box style to indicate a failed command.
     */
    private void setStyleToIndicateCommandFailure() {
        ObservableList<String> styleClass = commandTextField.getStyleClass();

        if (styleClass.contains(ERROR_STYLE_CLASS)) {
            return;
        }

        styleClass.add(ERROR_STYLE_CLASS);
    }

    /**
     * Represents a function that can execute commands.
     */
    @FunctionalInterface
    public interface CommandExecutor {
        /**
         * Executes the command and returns the result.
         *
         * @see seedu.address.logic.Logic#execute(String)
         */
        CommandResult execute(String commandText) throws CommandException, ParseException;
    }

}
