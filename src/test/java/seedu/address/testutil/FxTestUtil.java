package seedu.address.testutil;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/**
 * Starts JavaFX and runs assertions on its application thread for UI tests.
 */
public final class FxTestUtil {
    private FxTestUtil() {
    }

    /**
     * Starts JavaFX once for the test JVM and keeps it running between test classes.
     */
    public static void startJavaFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException exception) {
            // Another UI test has already started the toolkit in this JVM.
        }
        Platform.setImplicitExit(false);
    }

    /**
     * Runs {@code action} on the JavaFX application thread and propagates assertion failures to the test.
     */
    public static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
