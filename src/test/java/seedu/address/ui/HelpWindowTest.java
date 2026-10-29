package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HelpWindowTest {

    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException exception) {
            // Another UI test class in this JVM has already started the toolkit
        }
        Platform.setImplicitExit(false);
    }

    @Test
    public void constructor_showsGroupedCommandsWithDetailsCollapsed() throws Exception {
        List<String> expectedCommandWords = HelpContent.getSections().stream()
                .flatMap(section -> section.entries().stream()).map(HelpContent.Entry::commandWord).toList();
        runOnFxThread(() -> {
            Parent root = new HelpWindow().getRoot().getScene().getRoot();
            root.applyCss();
            List<TitledPane> entries = getEntryPanes(root);
            Label formatNote = (Label) root.lookup("#formatNoteLabel");
            Label historyNote = (Label) root.lookup("#historyNoteLabel");
            TextField searchField = (TextField) root.lookup("#searchTextField");

            assertEquals(expectedCommandWords, entries.stream().map(HelpWindowTest::commandWord).toList());
            assertTrue(entries.stream().noneMatch(TitledPane::isExpanded));
            assertEquals(HelpContent.FORMAT_NOTE, formatNote.getText());
            assertEquals(HelpContent.HISTORY_NOTE, historyNote.getText());
            assertEquals("Search commands or descriptions...", searchField.getPromptText());
            assertTrue(root.lookupAll(".button").stream().noneMatch(node -> node instanceof Button));
        });
    }

    @Test
    public void search_filtersByCommandAndDescription_caseInsensitively() throws Exception {
        runOnFxThread(() -> {
            Parent root = new HelpWindow().getRoot().getScene().getRoot();
            root.applyCss();
            TextField searchField = (TextField) root.lookup("#searchTextField");
            searchField.setText("HOMEWORK MARKS");
            assertEquals(List.of("homework marks"), getEntryPanes(root).stream()
                    .map(HelpWindowTest::commandWord).toList());

            searchField.setText("weekly chronological");
            assertEquals(List.of("lesson list"), getEntryPanes(root).stream()
                    .map(HelpWindowTest::commandWord).toList());

            searchField.setText("not a command");
            assertTrue(getEntryPanes(root).isEmpty());
            assertTrue(root.lookup("#noResultsLabel").isVisible());

            searchField.clear();
            assertEquals(HelpContent.getSections().stream().mapToInt(section -> section.entries().size()).sum(),
                    getEntryPanes(root).size());
            assertFalse(root.lookup("#noResultsLabel").isVisible());
        });
    }

    @Test
    public void search_formatOnlyMatch_showsMatchingCommand() throws Exception {
        runOnFxThread(() -> {
            Parent root = new HelpWindow().getRoot().getScene().getRoot();
            root.applyCss();
            TextField searchField = (TextField) root.lookup("#searchTextField");

            searchField.setText("HOMEWORK|HW EDIT|E");

            assertEquals(List.of("homework edit"), getEntryPanes(root).stream()
                    .map(HelpWindowTest::commandWord).toList());
        });
    }

    @Test
    public void selectingCommand_expandsSyntaxDescriptionAndExample() throws Exception {
        runOnFxThread(() -> {
            Parent root = new HelpWindow().getRoot().getScene().getRoot();
            root.applyCss();
            TitledPane entryPane = getEntryPanes(root).stream()
                    .filter(pane -> pane.getAccessibleText().equals("homework add"))
                    .findFirst().orElseThrow();
            HelpContent.Entry entry = HelpContent.getSections().stream()
                    .flatMap(section -> section.entries().stream())
                    .filter(candidate -> candidate.commandWord().equals("homework add"))
                    .findFirst().orElseThrow();

            entryPane.setExpanded(true);
            List<String> details = ((VBox) entryPane.getContent()).getChildren().stream()
                    .map(Label.class::cast).map(Label::getText).toList();

            assertTrue(details.contains(entry.format()));
            assertTrue(details.contains(entry.description()));
            assertTrue(details.contains(HelpWindow.EXAMPLE_PREFIX + entry.example()));
        });
    }

    @Test
    public void show_focusesSearchField_keysScrollAndHide() throws Exception {
        runOnFxThread(() -> {
            HelpWindow helpWindow = new HelpWindow();
            Stage stage = helpWindow.getRoot();
            ScrollPane scrollPane = (ScrollPane) stage.getScene().lookup("#helpScrollPane");
            TextField searchField = (TextField) stage.getScene().lookup("#searchTextField");

            helpWindow.show();
            try {
                assertTrue(helpWindow.isShowing());
                assertSame(searchField, stage.getScene().getFocusOwner());

                // Page Down scrolls by one viewport height, not by ScrollPane's small default step
                double viewportHeight = scrollPane.getViewportBounds().getHeight();
                double scrollableHeight = ((Region) scrollPane.getContent()).getHeight() - viewportHeight;
                pressKey(scrollPane, KeyCode.PAGE_DOWN);
                assertEquals(Math.min(1, viewportHeight / scrollableHeight), scrollPane.getVvalue(), 1e-9);
                pressKey(scrollPane, KeyCode.PAGE_UP);
                assertEquals(0, scrollPane.getVvalue());
                pressKey(searchField, KeyCode.DOWN);
                assertTrue(scrollPane.getVvalue() > 0);
                pressKey(searchField, KeyCode.UP);
                assertEquals(0, scrollPane.getVvalue());

                pressKey(scrollPane, KeyCode.ESCAPE);
                assertFalse(helpWindow.isShowing());
            } finally {
                helpWindow.hide();
            }
        });
    }

    @Test
    public void show_otherKey_neitherScrollsNorHides() throws Exception {
        runOnFxThread(() -> {
            HelpWindow helpWindow = new HelpWindow();
            ScrollPane scrollPane = (ScrollPane) helpWindow.getRoot().getScene().lookup("#helpScrollPane");

            helpWindow.show();
            try {
                pressKey(scrollPane, KeyCode.A);
                assertTrue(helpWindow.isShowing());
                assertEquals(0, scrollPane.getVvalue());
            } finally {
                helpWindow.hide();
            }
        });
    }

    private static void pressKey(Node target, KeyCode code) {
        Event.fireEvent(target, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
    }

    private static List<TitledPane> getEntryPanes(Parent root) {
        VBox content = (VBox) root.lookup("#helpContentBox");
        return content.getChildren().stream()
                .filter(VBox.class::isInstance)
                .map(VBox.class::cast)
                .flatMap(section -> section.getChildren().stream())
                .map(TitledPane.class::cast)
                .toList();
    }

    private static String commandWord(TitledPane pane) {
        HBox title = (HBox) pane.getGraphic();
        return ((Label) title.getChildren().getFirst()).getText();
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
