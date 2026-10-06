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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
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
    public void constructor_showsEveryHelpEntry() throws Exception {
        runOnFxThread(() -> {
            Parent root = new HelpWindow().getRoot().getScene().getRoot();
            root.applyCss();
            List<String> labelTexts = root.lookupAll(".label").stream()
                    .map(node -> ((Label) node).getText()).toList();

            assertTrue(labelTexts.contains(HelpContent.FORMAT_NOTE));
            for (HelpContent.Section section : HelpContent.getSections()) {
                assertTrue(labelTexts.contains(section.title()), section.title());
                for (HelpContent.Entry entry : section.entries()) {
                    assertTrue(labelTexts.contains(entry.format()), entry.format());
                    assertTrue(labelTexts.contains(entry.description()), entry.description());
                    assertTrue(labelTexts.contains(HelpWindow.EXAMPLE_PREFIX + entry.example()), entry.example());
                }
            }
            assertTrue(root.lookupAll(".button").stream().noneMatch(node -> node instanceof Button));
        });
    }

    @Test
    public void show_focusesScrollPane_keysScrollAndHide() throws Exception {
        runOnFxThread(() -> {
            HelpWindow helpWindow = new HelpWindow();
            Stage stage = helpWindow.getRoot();
            ScrollPane scrollPane = (ScrollPane) stage.getScene().lookup("#helpScrollPane");

            helpWindow.show();
            try {
                assertTrue(helpWindow.isShowing());
                assertSame(scrollPane, stage.getScene().getFocusOwner());

                // Page Down scrolls by one viewport height, not by ScrollPane's small default step
                double viewportHeight = scrollPane.getViewportBounds().getHeight();
                double scrollableHeight = ((Region) scrollPane.getContent()).getHeight() - viewportHeight;
                pressKey(scrollPane, KeyCode.PAGE_DOWN);
                assertEquals(Math.min(1, viewportHeight / scrollableHeight), scrollPane.getVvalue(), 1e-9);
                pressKey(scrollPane, KeyCode.PAGE_UP);
                assertEquals(0, scrollPane.getVvalue());

                pressKey(scrollPane, KeyCode.ESCAPE);
                assertFalse(helpWindow.isShowing());
            } finally {
                helpWindow.hide();
            }
        });
    }

    private static void pressKey(Node target, KeyCode code) {
        Event.fireEvent(target, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
