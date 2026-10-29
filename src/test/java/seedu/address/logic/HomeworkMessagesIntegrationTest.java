package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Runs homework commands found in PE testing through {@code LogicManager#execute} with their exact input, and checks
 * the message shown and that rejected commands change and save nothing.
 */
public class HomeworkMessagesIntegrationTest {
    /** Fixed clock at 2026-10-29, the day the PE findings were triaged. */
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-29T00:00:00Z"), ZoneOffset.UTC);

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private Path dataFilePath;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager(new AddressBook(), new UserPrefs());
        dataFilePath = temporaryFolder.resolve("tutorflow.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFilePath),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        logic = new LogicManager(model, storage, CLOCK, temporaryFolder);
        logic.execute("add n/Tan Ah Kow Jr. l/S3 s/MATH, PHYSICS p/91234567 gn/Tan Mei gp/98765432");
        logic.execute("hw add 1 t/Algebra s/MATH due/2026-11-02");
        logic.execute("hw add 1 t/Optics s/PHYSICS due/2026-11-05");
    }

    @Test
    public void execute_indexTooLarge_namesUpperBound() throws Exception {
        assertParseFailure("hw del 1 9999999999", Messages.MESSAGE_HOMEWORK_INDEX_TOO_LARGE);
        assertParseFailure("hw e 2147483648 1 t/X", Messages.MESSAGE_STUDENT_INDEX_TOO_LARGE);
    }

    /**
     * Executes {@code commandText} and checks that it fails to parse with {@code expectedMessage}, leaving the model
     * and the data file unchanged.
     */
    private void assertParseFailure(String commandText, String expectedMessage) throws Exception {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        byte[] savedData = Files.readAllBytes(dataFilePath);
        assertThrows(ParseException.class, expectedMessage, () -> logic.execute(commandText));
        assertEquals(expectedModel, model);
        assertArrayEquals(savedData, Files.readAllBytes(dataFilePath));
    }
}
