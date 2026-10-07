package seedu.address.ui;

import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;

/**
 * Controller for the help window, which lists the commands, what they do and an example of each.
 */
public class HelpWindow extends UiPart<Stage> {

    public static final String EXAMPLE_PREFIX = "Example: ";

    private static final Logger logger = LogsCenter.getLogger(HelpWindow.class);
    private static final String FXML = "HelpWindow.fxml";

    @FXML
    private ScrollPane helpScrollPane;

    @FXML
    private VBox helpContentBox;

    /**
     * Creates a new HelpWindow.
     *
     * @param root Stage to use as the root of the HelpWindow.
     */
    public HelpWindow(Stage root) {
        super(FXML, root);
        fillHelpContent();
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                hide();
                event.consume();
            } else if (event.getCode() == KeyCode.PAGE_DOWN || event.getCode() == KeyCode.PAGE_UP) {
                // ScrollPane scrolls only one small step on Page Up/Down, so scroll a whole page instead
                scrollByPage(event.getCode() == KeyCode.PAGE_DOWN ? 1 : -1);
                event.consume();
            }
        });
    }

    /**
     * Creates a new HelpWindow.
     */
    public HelpWindow() {
        this(new Stage());
    }

    /**
     * Adds the format note, then a heading for each section of {@link HelpContent} and, under it, the format,
     * description and example of each command.
     */
    private void fillHelpContent() {
        helpContentBox.getChildren().add(createLabel(HelpContent.FORMAT_NOTE, "help-note"));
        for (HelpContent.Section section : HelpContent.getSections()) {
            helpContentBox.getChildren().add(createLabel(section.title(), "help-section-title"));
            for (HelpContent.Entry entry : section.entries()) {
                helpContentBox.getChildren().addAll(createLabel(entry.format(), "help-format"),
                        createLabel(entry.description(), "help-description"),
                        createLabel(EXAMPLE_PREFIX + entry.example(), "help-example"));
            }
        }
    }

    /**
     * Scrolls the help content down by one viewport height if {@code direction} is positive, or up otherwise.
     */
    private void scrollByPage(int direction) {
        double viewportHeight = helpScrollPane.getViewportBounds().getHeight();
        double scrollableHeight = helpContentBox.getHeight() - viewportHeight;
        if (scrollableHeight <= 0) {
            return;
        }
        double newVvalue = helpScrollPane.getVvalue() + direction * viewportHeight / scrollableHeight;
        helpScrollPane.setVvalue(Math.clamp(newVvalue, helpScrollPane.getVmin(), helpScrollPane.getVmax()));
    }

    /**
     * Returns a wrapping label showing {@code text} with the given style class.
     */
    private static Label createLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().add(styleClass);
        return label;
    }

    /**
     * Shows the help window.
     * @throws IllegalStateException
     *     <ul>
     *         <li>
     *             if this method is called on a thread other than the JavaFX Application Thread.
     *         </li>
     *         <li>
     *             if this method is called during animation or layout processing.
     *         </li>
     *         <li>
     *             if this method is called on the primary stage.
     *         </li>
     *         <li>
     *             if {@code dialogStage} is already showing.
     *         </li>
     *     </ul>
     */
    public void show() {
        logger.fine("Showing help page about the application.");
        getRoot().show();
        getRoot().centerOnScreen();
        helpScrollPane.setVvalue(0);
        helpScrollPane.requestFocus();
    }

    /**
     * Returns true if the help window is currently being shown.
     */
    public boolean isShowing() {
        return getRoot().isShowing();
    }

    /**
     * Hides the help window.
     */
    public void hide() {
        getRoot().hide();
    }

    /**
     * Focuses on the help window.
     */
    public void focus() {
        getRoot().requestFocus();
    }
}
