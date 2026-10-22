package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.logic.commands.LessonEditCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class LessonEditIntegrationTest {
    @TempDir
    Path temporaryFolder;

    private ModelManager model;
    private LogicManager logic;
    private RecordingStorage storage;
    private Path dataFile;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        dataFile = temporaryFolder.resolve("tutorflow.json");
        storage = new RecordingStorage(dataFile, temporaryFolder.resolve("preferences.json"));
        logic = new LogicManager(model, storage);
        logic.execute("add n/John Tan l/S3 s/MATH,PHYSICS p/91234567 gn/Mary Tan gp/98765432");
        logic.execute("add n/Mary Lim l/S4 s/MATH,PHYSICS p/91234568 gn/Amy Lim gp/98765433");
        logic.execute("hw add 1 t/Algebra s/MATH due/2026-12-01");
        logic.execute("lesson add 1 s/MATH d/MONDAY st/16:00 et/18:00");
    }

    @Test
    public void execute_overlappingEdits_preservesModelViewAndSavedBytesWithoutSaving() throws Exception {
        logic.execute("lesson add 2 s/PHYSICS d/MONDAY st/18:00 et/19:00");
        logic.execute("lesson add 2 s/MATH d/TUESDAY st/16:00 et/18:00");
        logic.execute("filter l/S3");
        logic.execute("sort by/level o/desc");
        AddressBook before = new AddressBook(model.getAddressBook());
        var displayedBefore = List.copyOf(model.getStudentList());
        byte[] savedBefore = Files.readAllBytes(dataFile);
        int savesBefore = storage.saveCalls;

        for (String command : List.of("lesson edit 1 et/18:01", "lesson edit 2 st/17:59",
                "lesson edit 1 st/18:15 et/18:45", "lesson edit 1 st/15:00 et/20:00",
                "lesson edit 1 d/TUESDAY", "lesson edit 1 s/PHYSICS et/18:30")) {
            CommandException exception = assertThrows(CommandException.class, () -> logic.execute(command), command);

            assertTrue(exception.getMessage().startsWith(LessonAddCommand.MESSAGE_CLASH), command);
            assertEquals(before, model.getAddressBook(), command);
            assertEquals(displayedBefore, model.getStudentList(), command);
            assertArrayEquals(savedBefore, Files.readAllBytes(dataFile), command);
            assertEquals(savesBefore, storage.saveCalls, command);
        }
    }

    @Test
    public void execute_overlapWithAnotherLessonOfSameOwner_rejectsBeforeSaving() throws Exception {
        logic.execute("lesson add 1 s/PHYSICS d/MONDAY st/18:00 et/19:00");
        AddressBook before = new AddressBook(model.getAddressBook());
        int savesBefore = storage.saveCalls;

        CommandException exception = assertThrows(CommandException.class, () ->
                logic.execute("lesson edit 1 et/18:30"));

        assertEquals(LessonAddCommand.MESSAGE_CLASH + "John Tan; PHYSICS; MONDAY; 18:00-19:00",
                exception.getMessage());
        assertEquals(before, model.getAddressBook());
        assertEquals(savesBefore, storage.saveCalls);
    }

    @Test
    public void execute_multipleClashes_listsSameOwnerAndHiddenStudentInWeeklyOrder() throws Exception {
        logic.execute("lesson add 2 s/MATH d/MONDAY st/20:00 et/21:00");
        logic.execute("lesson add 1 s/PHYSICS d/MONDAY st/18:00 et/19:00");
        logic.execute("search John");
        byte[] savedBefore = Files.readAllBytes(dataFile);
        int savesBefore = storage.saveCalls;

        CommandException exception = assertThrows(CommandException.class, () ->
                logic.execute("lesson edit 1 et/20:30"));

        assertEquals(LessonEditCommand.MESSAGE_MULTIPLE_CLASHES
                + "John Tan; PHYSICS; MONDAY; 18:00-19:00\nMary Lim; MATH; MONDAY; 20:00-21:00",
                exception.getMessage());
        assertArrayEquals(savedBefore, Files.readAllBytes(dataFile));
        assertEquals(savesBefore, storage.saveCalls);
    }

    @Test
    public void execute_adjacentAndSubjectOnlyEdits_persistsAndChecksNewScheduleAfterReload() throws Exception {
        logic.execute("lesson add 2 s/PHYSICS d/MONDAY st/18:00 et/19:00");
        var ownerBefore = model.getAddressBook().getStudentList().getFirst();

        logic.execute("lesson edit 1 s/PHYSICS");
        logic.execute("lesson edit 1 st/17:00");
        logic.execute("lesson edit 1 st/19:00 et/20:00");

        ReadOnlyAddressBook saved = storage.readAddressBook().orElseThrow();
        assertEquals(model.getAddressBook(), new AddressBook(saved));
        assertEquals(ownerBefore.getHomeworks(), saved.getStudentList().getFirst().getHomeworks());
        assertTrue(logic.execute("lesson list").getFeedbackToUser()
                .contains("2. John Tan; PHYSICS; MON; 19:00-20:00"));

        model = new ModelManager(saved, model.getUserPrefs());
        logic = new LogicManager(model, storage);
        byte[] savedBefore = Files.readAllBytes(dataFile);
        int savesBefore = storage.saveCalls;
        assertThrows(CommandException.class, () -> logic.execute("lesson edit 2 st/18:59"));
        assertThrows(CommandException.class, () ->
                logic.execute("lesson add 2 s/MATH d/MONDAY st/19:30 et/20:30"));
        assertArrayEquals(savedBefore, Files.readAllBytes(dataFile));
        assertEquals(savesBefore, storage.saveCalls);

        // Moving away releases the previous slot for another lesson.
        logic.execute("lesson edit 2 d/TUESDAY");
        logic.execute("lesson add 2 s/MATH d/MONDAY st/19:00 et/20:00");
        assertEquals(model.getAddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    private static class RecordingStorage extends StorageManager {
        private int saveCalls;

        RecordingStorage(Path dataFile, Path preferencesFile) {
            super(new JsonAddressBookStorage(dataFile), new JsonUserPrefsStorage(preferencesFile));
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saveCalls++;
            super.saveAddressBook(addressBook);
        }
    }
}
