package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class LessonDeleteFeedbackIntegrationTest {
    @TempDir
    Path temporaryFolder;

    @Test
    public void execute_invalidIndices_preservesDataAndFilteredView() throws Exception {
        Session session = createSession();
        session.logic().execute("search Ethan");
        AddressBook original = new AddressBook(session.model().getAddressBook());
        var displayed = List.copyOf(session.model().getStudentList());
        byte[] saved = Files.readAllBytes(session.data().getAddressBookFilePath());

        for (String index : List.of("3", "2147483647", "2147483648", "9".repeat(1000))) {
            Exception error = assertThrows(Exception.class, () -> session.logic().execute("lesson delete " + index));
            assertEquals(Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX, error.getMessage());
            assertEquals(original, session.model().getAddressBook());
            assertEquals(displayed, session.model().getStudentList());
            assertArrayEquals(saved, Files.readAllBytes(session.data().getAddressBookFilePath()));
        }
    }

    @Test
    public void execute_hiddenOwner_feedbackMatchesScheduleAndDeletionPersists() throws Exception {
        Session session = createSession();
        String schedule = session.logic().execute("lesson list").getFeedbackToUser();
        String deletedDetails = "Priya Nair (82345671); CHEMISTRY; SAT; 15:00-16:30";
        assertEquals("2. " + deletedDetails, schedule.lines().toList().get(2));
        session.logic().execute("search Ethan");
        var displayed = List.copyOf(session.model().getStudentList());

        assertEquals("Deleted regular lesson:\n" + deletedDetails,
                session.logic().execute("lesson delete 2").getFeedbackToUser());

        assertEquals(displayed, session.model().getStudentList());
        assertFalse(session.logic().execute("lesson list").getFeedbackToUser().contains("Priya"));
        assertEquals(1, session.model().getAddressBook().getStudentList().getFirst().getLessons().size());
        assertEquals(0, session.model().getAddressBook().getStudentList().get(1).getLessons().size());
        assertEquals(session.model().getAddressBook(), session.data().readAddressBook().orElseThrow());
    }

    private Session createSession() throws Exception {
        ModelManager model = new ModelManager(SampleDataUtil.getSampleAddressBook(), new UserPrefs());
        JsonAddressBookStorage data = new JsonAddressBookStorage(temporaryFolder.resolve("tutorflow.json"));
        data.saveAddressBook(model.getAddressBook());
        LogicManager logic = new LogicManager(model, new StorageManager(data,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        return new Session(model, logic, data);
    }

    private record Session(ModelManager model, LogicManager logic, JsonAddressBookStorage data) {
    }
}
