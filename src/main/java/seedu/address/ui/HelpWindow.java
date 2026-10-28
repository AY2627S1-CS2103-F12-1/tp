package seedu.address.ui;

import java.util.Locale;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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

    @FXML
    private TextField searchTextField;

    @FXML
    private Label formatNoteLabel;

    @FXML
    private Label noResultsLabel;

    /**
     * Creates a new HelpWindow.
     *
     * @param root Stage to use as the root of the HelpWindow.
     */
    public HelpWindow(Stage root) {
        super(FXML, root);
        fillHelpContent();
        searchTextField.textProperty().addListener((unused, previous, current) -> fillHelpContent(current));
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                hide();
                event.consume();
            } else if (event.getCode() == KeyCode.PAGE_DOWN || event.getCode() == KeyCode.PAGE_UP) {
                // ScrollPane scrolls only one small step on Page Up/Down, so scroll a whole page instead
                scrollByPage(event.getCode() == KeyCode.PAGE_DOWN ? 1 : -1);
                event.consume();
            } else if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
                scrollByLine(event.getCode() == KeyCode.DOWN ? 1 : -1);
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
     * Shows the command groups with a short summary and expandable command details.
     */
    private void fillHelpContent() {
        fillHelpContent("");
    }

    /**
     * Shows the commands that match {@code query}, preserving their group and display order.
     *
     * @param query the text used to filter command names, formats, and descriptions.
     */
    private void fillHelpContent(String query) {
        String normalizedQuery = query.strip().toLowerCase(Locale.ROOT);
        formatNoteLabel.setText(HelpContent.FORMAT_NOTE);
        helpContentBox.getChildren().clear();
        boolean hasResults = false;
        for (HelpContent.Section section : HelpContent.getSections()) {
            VBox sectionContent = new VBox(4);
            sectionContent.getStyleClass().add("help-section");
            for (HelpContent.Entry entry : section.entries()) {
                if (matches(entry, normalizedQuery)) {
                    sectionContent.getChildren().add(createEntryPane(entry));
                }
            }
            if (!sectionContent.getChildren().isEmpty()) {
                hasResults = true;
                helpContentBox.getChildren().add(createLabel(section.title(), "help-section-title"));
                helpContentBox.getChildren().add(sectionContent);
            }
        }
        noResultsLabel.setVisible(!hasResults);
        noResultsLabel.setManaged(!hasResults);
        helpScrollPane.setVvalue(0);
    }

    /**
     * Returns whether {@code entry} matches the normalized query against its command, format, or description.
     *
     * @param entry the command entry to inspect.
     * @param normalizedQuery the lower-case query.
     * @return true if the query is empty or matches the entry.
     */
    private static boolean matches(HelpContent.Entry entry, String normalizedQuery) {
        return normalizedQuery.isEmpty()
                || entry.commandWord().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                || entry.format().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                || entry.description().toLowerCase(Locale.ROOT).contains(normalizedQuery);
    }

    /**
     * Creates a collapsed command row that reveals the full description, format, and example when expanded.
     *
     * @param entry the command to display.
     * @return the expandable row for the command.
     */
    private static TitledPane createEntryPane(HelpContent.Entry entry) {
        Label command = createLabel(entry.commandWord(), "help-command-word");
        Label summary = createLabel(getShortDescription(entry.description()), "help-command-summary");
        summary.setWrapText(false);
        summary.setTextOverrun(OverrunStyle.ELLIPSIS);
        summary.setMaxWidth(Double.MAX_VALUE);
        HBox title = new HBox(12, command, summary);
        title.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(summary, Priority.ALWAYS);

        VBox details = new VBox(4,
                createLabel(entry.format(), "help-format"),
                createLabel(entry.description(), "help-description"),
                createLabel(EXAMPLE_PREFIX + entry.example(), "help-example"));
        TitledPane pane = new TitledPane("", details);
        pane.setGraphic(title);
        pane.setAccessibleText(entry.commandWord());
        pane.setExpanded(false);
        pane.getStyleClass().add("help-entry");
        return pane;
    }

    /**
     * Shortens a long description at a word boundary for the collapsed command row.
     *
     * @param description the full command description.
     * @return the description capped at 76 characters.
     */
    private static String getShortDescription(String description) {
        final int maximumLength = 76;
        if (description.length() <= maximumLength) {
            return description;
        }
        int lastSpace = description.lastIndexOf(' ', maximumLength - 1);
        int end = lastSpace > 0 ? lastSpace : maximumLength - 1;
        return description.substring(0, end) + "…";
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
     * Scrolls the command list a short distance for an Up or Down key press.
     *
     * @param direction positive to scroll down or negative to scroll up.
     */
    private void scrollByLine(int direction) {
        double viewportHeight = helpScrollPane.getViewportBounds().getHeight();
        double scrollableHeight = helpContentBox.getHeight() - viewportHeight;
        if (scrollableHeight <= 0) {
            return;
        }
        double newVvalue = helpScrollPane.getVvalue() + direction * 36 / scrollableHeight;
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
        searchTextField.requestFocus();
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
