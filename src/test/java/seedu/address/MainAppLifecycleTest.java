package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalStudents.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.AddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.testutil.FxTestUtil;

public class MainAppLifecycleTest {
    @TempDir
    Path temporaryFolder;

    @BeforeAll
    public static void startJavaFx() {
        FxTestUtil.startJavaFx();
    }

    @Test
    public void constructor_default_doesNotLoadOrCreateRecords() {
        MainApp app = new MainApp();
        assertNull(app.model);
        assertNull(app.storage);
    }

    @Test
    public void invalidData_startShowsRecoveryAndRequestsShutdownWithoutOpeningRoster() throws Exception {
        Path data = temporaryFolder.resolve("tutorflow.json");
        Path preferences = temporaryFolder.resolve("preferences.json");
        Files.writeString(data, "{\"students\":[");
        byte[] originalData = Files.readAllBytes(data);
        AtomicBoolean shutdownRequested = new AtomicBoolean();
        MainApp app = new MainApp(preferences, data, () -> shutdownRequested.set(true));

        app.init();

        assertNull(app.model);
        assertNull(app.logic);
        assertNull(app.ui);
        byte[] preferencesAfterInit = Files.readAllBytes(preferences);
        FxTestUtil.runOnFxThread(() -> {
            Stage unusedPrimaryStage = new Stage();
            FutureTask<Void> inspectAndCloseDialog = new FutureTask<>(() -> {
                Stage dialog = Window.getWindows().stream().filter(window -> window instanceof Stage stage
                        && stage.getTitle().equals("TutorFlow could not load saved data"))
                        .map(window -> (Stage) window).findFirst().orElseThrow();
                try {
                    DialogPane pane = (DialogPane) dialog.getScene().getRoot();
                    assertEquals("Your data file has not been changed.", pane.getHeaderText());
                    assertTrue(pane.getContentText().contains(data.toAbsolutePath().normalize().toString()));
                    assertTrue(pane.getContentText().contains("restore a valid backup"));
                    assertTrue(pane.getContentText().contains("move the original file to a safe location"));
                    assertFalse(shutdownRequested.get());
                    assertFalse(unusedPrimaryStage.isShowing());
                } finally {
                    Button closeButton = (Button) dialog.getScene().lookup(".button");
                    closeButton.fire();
                }
                return null;
            });
            Platform.runLater(inspectAndCloseDialog);
            app.start(unusedPrimaryStage);
            try {
                inspectAndCloseDialog.get(1, TimeUnit.SECONDS);
            } catch (Exception exception) {
                throw new AssertionError(exception);
            }
            assertTrue(shutdownRequested.get());
            assertFalse(unusedPrimaryStage.isShowing());
        });
        app.stop();

        assertArrayEquals(originalData, Files.readAllBytes(data));
        assertArrayEquals(preferencesAfterInit, Files.readAllBytes(preferences));
    }

    @Test
    public void validData_startOpensSavedRosterAndStopSavesPreferences() throws Exception {
        Path data = temporaryFolder.resolve("tutorflow.json");
        Path preferences = temporaryFolder.resolve("preferences.json");
        JsonAddressBookStorage dataStorage = new JsonAddressBookStorage(data);
        dataStorage.saveAddressBook(getTypicalAddressBook());
        byte[] originalData = Files.readAllBytes(data);
        AtomicBoolean shutdownRequested = new AtomicBoolean();
        MainApp app = new MainApp(preferences, data, () -> shutdownRequested.set(true));

        app.init();

        assertEquals(getTypicalAddressBook(), new AddressBook(app.model.getAddressBook()));
        assertNotNull(app.logic);
        FxTestUtil.runOnFxThread(() -> {
            Stage stage = new Stage();
            try {
                app.start(stage);
                assertTrue(stage.isShowing());
                assertNotNull(stage.getScene().lookup("#commandTextField"));
                assertNotNull(stage.getScene().lookup("#studentListView"));
                assertFalse(shutdownRequested.get());
            } finally {
                stage.hide();
            }
        });
        GuiSettings settings = new GuiSettings(1024, 768, 100, 100);
        app.model.setGuiSettings(settings);
        app.stop();

        assertEquals(settings, new JsonUserPrefsStorage(preferences).readUserPrefs().orElseThrow().getGuiSettings());
        assertArrayEquals(originalData, Files.readAllBytes(data));
    }
}
