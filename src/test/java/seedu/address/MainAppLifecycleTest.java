package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.homework.DueDateTest.MESSAGE_YEAR_OUT_OF_RANGE;
import static seedu.address.model.homework.DueDateTest.fullDateFormatMessage;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalStudents.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.exceptions.CommandException;
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
        String content = getRecoveryDialogContent("{\"students\":[");

        // the line and column are where the JSON parser stopped; the reason is taken from the cause, so the
        // exception class name is not shown
        assertEquals("The file is not valid JSON near line 1, column 13.", getReason(content));
        assertFalse(content.contains("Exception"), content);
    }

    @Test
    public void invalidData_shortFormDueDate_dialogShowsReason() throws Exception {
        String content = getRecoveryDialogContent(getTypicalDataReplacing("\"2026-10-15\"", "\"10-15\""));
        assertTrue(getReason(content).startsWith(fullDateFormatMessage("10-15")), content);
    }

    @Test
    public void invalidData_dueDateYearOutOfRange_dialogShowsReason() throws Exception {
        String content = getRecoveryDialogContent(getTypicalDataReplacing("\"2026-10-15\"", "\"2206-10-30\""));
        assertTrue(getReason(content).startsWith(MESSAGE_YEAR_OUT_OF_RANGE), content);
    }

    @Test
    public void invalidData_missingStudentField_dialogShowsReason() throws Exception {
        // unknown keys are ignored, so the renamed key reads as missing
        String content = getRecoveryDialogContent(getTypicalDataReplacing("\"guardianPhone\"",
                "\"unknownGuardianPhone\""));
        assertTrue(getReason(content).startsWith("Student's guardianPhone field is missing!"), content);
    }

    /**
     * Starts the app on a data file holding {@code dataFileContent}, which cannot be loaded, and returns the content
     * text of the recovery dialog. Also checks that the dialog names the file and the ways to recover, that the app
     * shuts down without opening the roster, and that neither the data file nor the preferences file is changed.
     */
    private String getRecoveryDialogContent(String dataFileContent) throws Exception {
        Path data = temporaryFolder.resolve("tutorflow.json");
        Path preferences = temporaryFolder.resolve("preferences.json");
        Files.writeString(data, dataFileContent);
        byte[] originalData = Files.readAllBytes(data);
        AtomicReference<String> content = new AtomicReference<>();
        AtomicBoolean shutdownRequested = new AtomicBoolean();
        MainApp app = new MainApp(temporaryFolder, preferences, data, () -> shutdownRequested.set(true));

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
                    content.set(pane.getContentText());
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
        return content.get();
    }

    /**
     * Returns the data file of the typical students with {@code target} replaced by {@code replacement}.
     */
    private String getTypicalDataReplacing(String target, String replacement) throws Exception {
        Path typicalData = temporaryFolder.resolve("typical.json");
        new JsonAddressBookStorage(typicalData).saveAddressBook(getTypicalAddressBook());
        String data = Files.readString(typicalData);
        assertTrue(data.contains(target), target);
        return data.replace(target, replacement);
    }

    /**
     * Returns the reason that {@code dialogContent} gives on its line starting with "Reason: ".
     */
    private static String getReason(String dialogContent) {
        String reasonPrefix = "Reason: ";
        return dialogContent.lines().filter(line -> line.startsWith(reasonPrefix)).findFirst()
                .map(line -> line.substring(reasonPrefix.length()))
                .orElseThrow(() -> new AssertionError("The dialog gives no reason:\n" + dialogContent));
    }

    @Test
    public void init_homeFolderNotWorkingFolder_saveFailureNamesFileFromHomeFolder() throws Exception {
        // the JAR is started from another folder, so its home folder is not the working folder
        Path home = Files.createDirectories(temporaryFolder.resolve("app"));
        Path data = home.resolve("data").resolve("tutorflow.json");
        MainApp app = new MainApp(home, home.resolve("preferences.json"), data, () -> { });
        app.init();
        Files.createDirectories(data);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_SAVE_FAILURE, Path.of("data", "tutorflow.json"),
                Messages.MESSAGE_SAVE_CAUSE_FOLDER);
        assertThrows(CommandException.class, expectedMessage, () -> app.logic.execute("delete 1"));
    }

    @Test
    public void validData_startOpensSavedRosterAndStopSavesPreferences() throws Exception {
        Path data = temporaryFolder.resolve("tutorflow.json");
        Path preferences = temporaryFolder.resolve("preferences.json");
        JsonAddressBookStorage dataStorage = new JsonAddressBookStorage(data);
        dataStorage.saveAddressBook(getTypicalAddressBook());
        byte[] originalData = Files.readAllBytes(data);
        AtomicBoolean shutdownRequested = new AtomicBoolean();
        MainApp app = new MainApp(temporaryFolder, preferences, data, () -> shutdownRequested.set(true));

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
