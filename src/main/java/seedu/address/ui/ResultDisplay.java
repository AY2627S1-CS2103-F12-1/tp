package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";

    @FXML
    private TextArea resultDisplay;

    /**
     * Creates a result display that scrolls without requiring keyboard focus.
     */
    public ResultDisplay() {
        super(FXML);
        enableScrollingFrom(getRoot());
    }

    /**
     * Routes scrolling over {@code section} to this display without changing keyboard focus.
     */
    void enableScrollingFrom(Node section) {
        ScrollSupport.enableScrolling(section, resultDisplay);
    }

    public void setFeedbackToUser(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        resultDisplay.setText(feedbackToUser);
    }

}
