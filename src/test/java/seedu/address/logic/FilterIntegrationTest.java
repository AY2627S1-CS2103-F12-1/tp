package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalHomeworks.ATOMIC_STRUCTURE;
import static seedu.address.testutil.TypicalRegularLessons.MONDAY_MATH;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.homework.Score;
import seedu.address.model.student.Student;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.StudentBuilder;

public class FilterIntegrationTest {
    @TempDir
    Path temporaryFolder;

    private final Student first = new StudentBuilder().withName("Alice Tan").withAcademicLevel("S4")
            .withSubjects("MATH,PHYSICS").withLessons(MONDAY_MATH).build();
    private final Student second = new StudentBuilder().withName("Ben Lim").withAcademicLevel("S4")
            .withSubjects("CHEMISTRY").withHomeworks(ATOMIC_STRUCTURE).build();
    private final Student third = new StudentBuilder().withName("Cara Ng").withAcademicLevel("J1").build();

    private ModelManager model;
    private RecordingStorage storage;
    private LogicManager logic;
    private Path dataFile;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        List.of(first, second, third).forEach(model::addStudent);
        dataFile = temporaryFolder.resolve("tutorflow.json");
        storage = new RecordingStorage(dataFile, temporaryFolder.resolve("preferences.json"));
        Clock clock = Clock.fixed(Instant.parse("2026-10-19T00:00:00Z"), ZoneOffset.UTC);
        logic = new LogicManager(model, storage, clock);
    }

    @Test
    public void execute_filterSuccessAndNoMatches_neverSavesOrCreatesDataFile() throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());

        for (String command : List.of("filter l/S4", "filter s/Math", "filter l/S4 s/Math", "filter l/S5")) {
            logic.execute(command);

            assertEquals(0, storage.saveCalls);
            assertFalse(Files.exists(dataFile));
            assertEquals(before, model.getAddressBook());
        }
    }

    @Test
    public void execute_invalidFilters_preservesPreviousResultsAndSavedBytes() throws Exception {
        new JsonAddressBookStorage(dataFile).saveAddressBook(model.getAddressBook());
        byte[] savedBefore = Files.readAllBytes(dataFile);
        AddressBook rosterBefore = new AddressBook(model.getAddressBook());
        logic.execute("filter s/Math");
        List<Student> displayedBefore = List.copyOf(model.getStudentList());

        for (String command : List.of("filter", "filter x/value", "filter l/S4 l/S4", "filter l/", "filter s/bad",
                "filter l/S6", "filter l/Sec 4", "filter s/Math, Physics")) {
            assertThrows(ParseException.class, () -> logic.execute(command));

            assertEquals(displayedBefore, model.getStudentList());
            assertEquals(rosterBefore, model.getAddressBook());
            assertArrayEquals(savedBefore, Files.readAllBytes(dataFile));
            assertEquals(0, storage.saveCalls);
        }
    }

    @Test
    public void execute_successfulFilters_preservesSavedBytesWithoutWriting() throws Exception {
        new JsonAddressBookStorage(dataFile).saveAddressBook(model.getAddressBook());
        byte[] savedBefore = Files.readAllBytes(dataFile);

        logic.execute("filter s/CHEMISTRY l/S4");
        assertEquals(List.of(second), model.getStudentList());
        logic.execute("filter l/S5");

        assertArrayEquals(savedBefore, Files.readAllBytes(dataFile));
        assertEquals(0, storage.saveCalls);
    }

    @Test
    public void execute_homeworkCommandsAfterFilter_useDisplayedStudentIndex() throws Exception {
        logic.execute("filter s/CHEMISTRY");
        assertTrue(logic.execute("homework list 1 all").getFeedbackToUser().contains("Homework for Ben Lim:"));

        logic.execute("homework edit 1 1 t/Corrected task");
        assertEquals("Corrected task", model.getStudentList().getFirst().getHomeworks().getFirst()
                .getTitle().toString());
        logic.execute("homework marks 1 1 score/10/20");
        assertEquals(Optional.of(new Score(10, 20)), model.getStudentList().getFirst().getHomeworks().getFirst()
                .getScore());
        logic.execute("homework delete 1 1");
        assertTrue(model.getStudentList().getFirst().getHomeworks().isEmpty());
        logic.execute("homework add 1 t/New task s/CHEMISTRY due/2026-10-25");

        List<Student> stored = model.getAddressBook().getStudentList();
        assertEquals(first, stored.getFirst());
        assertEquals(third, stored.get(2));
        assertEquals("New task", stored.get(1).getHomeworks().getFirst().getTitle().toString());
        assertEquals(4, storage.saveCalls);
    }

    @Test
    public void execute_lessonAddAndDeleteStudentAfterFilter_useDisplayedStudentIndex() throws Exception {
        logic.execute("filter s/CHEMISTRY");
        logic.execute("lesson add 1 s/CHEMISTRY d/MONDAY st/10:00 et/11:00");

        assertEquals(1, model.getAddressBook().getStudentList().get(1).getLessons().size());
        assertEquals(first, model.getAddressBook().getStudentList().getFirst());
        logic.execute("delete 1");

        assertEquals(List.of(first, third), model.getAddressBook().getStudentList());
        assertTrue(model.getStudentList().isEmpty());
        assertEquals(2, storage.saveCalls);
        assertEquals(model.getAddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void execute_lessonCommandsAfterFilter_includeHiddenStudents() throws Exception {
        logic.execute("filter l/J1");

        CommandException exception = assertThrows(CommandException.class, () ->
                logic.execute("lesson add 1 s/MATH d/MONDAY st/17:30 et/19:00"));
        assertTrue(exception.getMessage().contains("Alice Tan"));
        assertTrue(logic.execute("lesson list").getFeedbackToUser().contains("Alice Tan"));
        assertEquals(0, storage.saveCalls);

        logic.execute("lesson delete 1");
        assertTrue(model.getAddressBook().getStudentList().getFirst().getLessons().isEmpty());
        assertEquals(List.of(third), model.getStudentList());
    }

    @Test
    public void execute_viewResetCommands_restoreFullRoster() throws Exception {
        for (String command : List.of("list", "ls", "homework upcoming due/7")) {
            logic.execute("filter s/CHEMISTRY");
            logic.execute(command);

            assertEquals(List.of(first, second, third), model.getStudentList());
        }
        logic.execute("filter s/CHEMISTRY");
        logic.execute("search Alice");
        assertEquals(List.of(first), model.getStudentList());

        logic.execute("filter s/CHEMISTRY");
        logic.execute("add n/Dan Lee l/S2 s/MATH p/92345678 gn/Mary Lee gp/98765432");
        assertEquals(4, model.getStudentList().size());
        assertEquals(1, storage.saveCalls);
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
