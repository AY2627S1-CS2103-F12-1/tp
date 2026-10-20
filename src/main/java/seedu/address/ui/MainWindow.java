package seedu.address.ui;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.SortCommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.student.Student;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";

    private final Logger logger = LogsCenter.getLogger(MainWindow.class);

    private Stage primaryStage;
    private Logic logic;
    private Path dataFilePath;

    // Independent Ui parts residing in this Ui container
    private StudentListPanel studentListPanel;
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;
    private CommandBox commandBox;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane studentListPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage}, {@code Logic},
     * and the data file path to show in the status bar.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());

        setAccelerators();

        helpWindow = new HelpWindow();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in a later version of the SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField and
         * ResultDisplay contains a TextArea, some accelerators (e.g., F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add the following event filter to capture such key events and open
         * the help window purposely so as to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        studentListPanel = createStudentListPanel(logic.getStudentList(), this::handleStudentRenderingError);
        studentListPanelPlaceholder.getChildren().add(studentListPanel.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(dataFilePath);
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
        helpWindow.getRoot().setOnHidden(unused -> focusCommandBox());
    }

    /**
     * Creates the panel for {@code students}, reporting rendering failures through {@code renderingErrorHandler}.
     */
    StudentListPanel createStudentListPanel(ObservableList<Student> students, Runnable renderingErrorHandler) {
        return new StudentListPanel(students, renderingErrorHandler);
    }

    /**
     * Shows a safe rendering error after the student panel has logged its technical cause.
     */
    private void handleStudentRenderingError() {
        resultDisplay.setFeedbackToUser(FilterCommand.MESSAGE_INTERNAL_ERROR);
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
        }
    }

    /**
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    /**
     * Moves keyboard focus back to the command box so that the next command typed after closing the help window
     * is not lost.
     */
    private void focusCommandBox() {
        primaryStage.requestFocus();
        commandBox.requestFocus();
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        studentListPanel.clearRenderingError();
        try {
            boolean isSortRequest = commandText.strip().split("\\s+", 2)[0].equals(SortCommand.COMMAND_WORD);
            CommandResult commandResult = isSortRequest ? executeStudentSort(commandText) : logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            if (!studentListPanel.hasRenderingError()) {
                resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());
            }

            if (commandResult.isShowStudentList()) {
                studentListPanel.showFromTop();
            }

            if (commandResult.isShowHelp()) {
                handleHelp();
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("An error occurred while executing command: " + commandText);
            resultDisplay.setFeedbackToUser(e.getMessage());
            throw e;
        }
    }

    /**
     * Keeps the previous view available until the sorted cards and visible layout have been verified.
     */
    private CommandResult executeStudentSort(String commandText) throws CommandException, ParseException {
        StudentListPanel.ViewState previousState = studentListPanel.beginSort();
        SortCommandResult completedSort = null;
        try {
            CommandResult result = logic.execute(commandText);
            if (result instanceof SortCommandResult sortedResult) {
                completedSort = sortedResult;
                studentListPanel.verifySortRendering();
            } else {
                studentListPanel.prepareRestore(previousState);
            }
            return result;
        } catch (CommandException | ParseException exception) {
            if (!restoreStudentSort(null, previousState, exception)) {
                logger.log(Level.SEVERE, "Student sort view recovery failed after command rejection", exception);
                throw new CommandException(SortCommand.MESSAGE_INTERNAL_ERROR, exception);
            }
            throw exception;
        } catch (RuntimeException exception) {
            boolean isRestored = restoreStudentSort(completedSort, previousState, exception);
            logger.log(isRestored ? Level.WARNING : Level.SEVERE,
                    "Student sort failed: phase=rendering, recovery=" + (isRestored ? "restored" : "failed")
                    + ", context=" + (completedSort == null ? "execution" : completedSort.getFeedbackToUser()),
                    exception);
            throw new CommandException(SortCommand.MESSAGE_INTERNAL_ERROR, exception);
        } finally {
            studentListPanel.endSort();
        }
    }

    /**
     * Restores cards before notifying observers, then restores selection and viewport without hiding failures.
     */
    private boolean restoreStudentSort(SortCommandResult completedSort, StudentListPanel.ViewState previousState,
            Exception failure) {
        try {
            studentListPanel.prepareRestore(previousState);
            if (completedSort != null) {
                completedSort.restorePreviousOrder();
            }
            studentListPanel.restoreViewState(previousState);
            return true;
        } catch (RuntimeException recoveryFailure) {
            if (recoveryFailure != failure) {
                failure.addSuppressed(recoveryFailure);
            }
            return false;
        }
    }
}
