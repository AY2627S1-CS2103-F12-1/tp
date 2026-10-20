package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.Student;
import seedu.address.model.student.TuitionSubject;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.StudentBuilder;

public class SortIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private Logic logic;
    private CountingStorage storage;
    private Path dataPath;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        model.addStudent(new StudentBuilder().withName("Target Junior").withAcademicLevel("J1").build());
        model.addStudent(new StudentBuilder().withName("Hidden Student").withAcademicLevel("S3").build());
        model.addStudent(new StudentBuilder().withName("Target Secondary").withAcademicLevel("S1").build());
        dataPath = temporaryFolder.resolve("students.json");
        storage = new CountingStorage(dataPath);
        storage.saveAddressBook(model.getAddressBook());
        storage.saveCount = 0;
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))),
                Clock.fixed(Instant.parse("2026-10-20T00:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    public void execute_sortAndValidationFailures_neverWriteStoredData() throws Exception {
        String before = Files.readString(dataPath);
        logic.execute("search Target");
        logic.execute("sort by/level");
        logic.execute("sort by/subject o/desc");
        logic.execute("sort by/day");
        List<String> displayedBeforeFailure = displayedNames();
        assertThrows(ParseException.class, () -> logic.execute("sort by/name"));

        assertEquals(displayedBeforeFailure, displayedNames());
        assertEquals(List.of("Target Secondary", "Target Junior"), displayedNames());
        assertEquals(0, storage.saveCount);
        assertEquals(before, Files.readString(dataPath));
        assertEquals(List.of("Target Junior", "Hidden Student", "Target Secondary"), storedNames());
    }

    @Test
    public void execute_listSearchAddAndUpcoming_discardPreviousSort() throws Exception {
        logic.execute("sort by/level");
        logic.execute("list");
        assertEquals(storedNames(), displayedNames());
        logic.execute("sort by/level");
        logic.execute("search Target");
        assertEquals(List.of("Target Junior", "Target Secondary"), displayedNames());
        logic.execute("sort by/level");
        logic.execute("hw up");
        assertEquals(storedNames(), displayedNames());
        logic.execute("sort by/level");
        logic.execute("add n/New Student l/S2 s/MATH p/92345678 gn/New Guardian gp/98765432");
        assertEquals(List.of("Target Junior", "Hidden Student", "Target Secondary", "New Student"), displayedNames());
    }

    @Test
    public void execute_indexedCommandsAfterSort_targetDisplayedStudentAndKeepStoredOrder() throws Exception {
        logic.execute("search Target");
        logic.execute("sort by/level");
        logic.execute("lesson add 1 s/MATH d/TUESDAY st/14:00 et/15:00");
        logic.execute("hw add 1 t/Original title s/MATH due/2026-10-21");
        logic.execute("hw edit 1 1 t/Updated title");
        logic.execute("hw marks 1 1 score/5");
        assertTrue(logic.execute("hw ls 1").getFeedbackToUser().contains("Updated title"));
        assertEquals("Target Secondary", model.getStudentList().getFirst().getName().toString());
        assertEquals(1, model.getAddressBook().getStudentList().get(2).getLessons().size());
        assertEquals(1, model.getAddressBook().getStudentList().get(2).getHomeworks().size());
        assertEquals(0, model.getAddressBook().getStudentList().getFirst().getHomeworks().size());
        assertEquals(List.of("Target Junior", "Hidden Student", "Target Secondary"), storedNames());
        logic.execute("hw delete 1 1");
        logic.execute("delete 1");
        assertEquals(List.of("Target Junior", "Hidden Student"), storedNames());
        assertEquals(List.of("Target Junior"), displayedNames());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_failedSaveAfterSort_restoresStudentAndDisplayOrder() throws Exception {
        logic.execute("sort by/level");
        List<String> before = displayedNames();
        String savedBefore = Files.readString(dataPath);
        storage.shouldFail = true;
        String addLesson = "lesson add 1 s/MATH d/TUESDAY st/14:00 et/15:00";
        assertThrows(CommandException.class, () -> logic.execute(addLesson));
        assertEquals(before, displayedNames());
        assertTrue(model.getStudentList().getFirst().getLessons().isEmpty());
        assertEquals(savedBefore, Files.readString(dataPath));
    }

    @Test
    public void execute_internalSortFailure_neverWritesStoredData() throws Exception {
        ModelManager failingModel = new ModelManager(model.getAddressBook(), new UserPrefs()) {
            private boolean shouldFail = true;

            @Override
            public void setStudentListOrder(List<Student> students) {
                super.setStudentListOrder(students);
                if (shouldFail) {
                    shouldFail = false;
                    throw new IllegalStateException("Injected sort publication failure");
                }
            }
        };
        Logic failingLogic = new LogicManager(failingModel, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        String before = Files.readString(dataPath);

        assertThrows(CommandException.class, () -> failingLogic.execute("sort by/level"));

        assertEquals(model.getStudentList(), failingModel.getStudentList());
        assertEquals(before, Files.readString(dataPath));
        assertEquals(0, storage.saveCount);
    }

    @Test
    public void execute_sortAfterFilter_preservesMatchesAndNeverWritesData() throws Exception {
        setUpSubjectFilter();
        String savedBefore = Files.readString(dataPath);
        logic.execute("filter s/MATH");
        logic.execute("sort by/level");

        assertEquals(List.of("Target Secondary", "Target Junior"), displayedNames());
        assertEquals(List.of("Target Junior", "Hidden Student", "Target Secondary"), storedNames());
        assertEquals(savedBefore, Files.readString(dataPath));
        assertEquals(0, storage.saveCount);
    }

    @Test
    public void execute_filterAfterSort_resetsOrderAndExaminesFullRoster() throws Exception {
        setUpSubjectFilter();
        logic.execute("filter s/MATH");
        logic.execute("sort by/level");
        logic.execute("filter s/MATH");
        assertEquals(List.of("Target Junior", "Target Secondary"), displayedNames());

        logic.execute("search Hidden");
        logic.execute("sort by/day");
        logic.execute("filter s/MATH");
        assertEquals(List.of("Target Junior", "Target Secondary"), displayedNames());
        assertEquals(0, storage.saveCount);
    }

    @Test
    public void execute_indexedCommandsAfterFilteredSort_targetCorrectStudent() throws Exception {
        setUpSubjectFilter();
        logic.execute("filter s/MATH");
        logic.execute("sort by/level");
        logic.execute("hw add 1 t/Filtered homework s/MATH due/2026-10-21");

        assertEquals("Filtered homework", model.getAddressBook().getStudentList().get(2).getHomeworks()
                .getFirst().getTitle().toString());
        assertEquals(List.of("Target Secondary", "Target Junior"), displayedNames());
        logic.execute("delete 1");
        assertEquals(List.of("Target Junior"), displayedNames());
        assertEquals(List.of("Target Junior", "Hidden Student"), storedNames());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_levelChangeAfterSort_keepsPositionUntilExplicitSort() throws Exception {
        Student original = new StudentBuilder().withName("Changed Student").withAcademicLevel("S1").build();
        Student edited = new StudentBuilder().withName("Changed Student").withAcademicLevel("J2").build();
        Student other = new StudentBuilder().withName("Other Student").withAcademicLevel("S3").build();

        assertReplacementKeepsPositionUntilSort("level", original, edited, other);
    }

    @Test
    public void execute_subjectChangeAfterSort_keepsPositionUntilExplicitSort() throws Exception {
        Student original = new StudentBuilder().withName("Changed Student").withSubjects("CHEMISTRY").build();
        Student edited = new StudentBuilder().withName("Changed Student").withSubjects("PHYSICS").build();
        Student other = new StudentBuilder().withName("Other Student").withSubjects("MATH").build();

        assertReplacementKeepsPositionUntilSort("subject", original, edited, other);
    }

    @Test
    public void execute_dayChangeAfterSort_keepsPositionUntilExplicitSort() throws Exception {
        Student original = new StudentBuilder().withName("Changed Student")
                .withLessons(lessonOn(DayOfWeek.MONDAY)).build();
        Student edited = original.withLessons(List.of(lessonOn(DayOfWeek.FRIDAY)));
        Student other = new StudentBuilder().withName("Other Student")
                .withLessons(lessonOn(DayOfWeek.WEDNESDAY)).build();

        assertReplacementKeepsPositionUntilSort("day", original, edited, other);
    }

    @Test
    public void execute_replacementNoLongerMatchesFilter_removesStudentWithoutExposingOtherStudents() throws Exception {
        setUpSubjectFilter();
        logic.execute("filter s/MATH");
        logic.execute("sort by/level");
        Student original = model.getStudentList().getFirst();
        Student edited = new StudentBuilder().withName("Target Secondary").withAcademicLevel("S1")
                .withSubjects("CHEMISTRY").build();

        model.setStudent(original, edited);

        assertEquals(List.of("Target Junior"), displayedNames());
        assertEquals(edited, model.getAddressBook().getStudentList().get(2));
        logic.execute("sort by/subject");
        assertEquals(List.of("Target Junior"), displayedNames());
        assertEquals(List.of("Target Junior", "Hidden Student", "Target Secondary"), storedNames());
    }

    /**
     * Checks the model boundary for future student edits without introducing an edit command or resetting the view.
     */
    private void assertReplacementKeepsPositionUntilSort(String key, Student original, Student edited, Student other)
            throws Exception {
        Student hidden = new StudentBuilder().withName("Hidden Person").build();
        model.setAddressBook(new AddressBookBuilder().withStudent(other).withStudent(hidden)
                .withStudent(original).build());
        logic.execute("search Student");
        logic.execute("sort by/" + key);
        assertEquals(List.of(original, other), model.getStudentList());

        model.setStudent(original, edited);

        assertEquals(List.of(edited, other), model.getStudentList());
        assertEquals(List.of(other, hidden, edited), model.getAddressBook().getStudentList());
        logic.execute("sort by/" + key);
        assertEquals(List.of(other, edited), model.getStudentList());
        assertEquals(List.of(other, hidden, edited), model.getAddressBook().getStudentList());
        assertEquals(0, storage.saveCount);
    }

    private static RegularLesson lessonOn(DayOfWeek day) {
        return new RegularLesson(TuitionSubject.MATH, day, LocalTime.of(14, 0), LocalTime.of(15, 0));
    }

    /**
     * Persists a fixture where the hidden student does not match the other students' subject.
     */
    private void setUpSubjectFilter() throws IOException {
        Student hidden = model.getAddressBook().getStudentList().get(1);
        model.setStudent(hidden, new StudentBuilder().withName("Hidden Student").withSubjects("CHEMISTRY").build());
        storage.saveAddressBook(model.getAddressBook());
        storage.saveCount = 0;
    }

    private List<String> displayedNames() {
        return model.getStudentList().stream().map(student -> student.getName().toString()).toList();
    }

    private List<String> storedNames() {
        return model.getAddressBook().getStudentList().stream().map(student -> student.getName().toString()).toList();
    }

    /** Counts actual persistence calls while retaining real JSON serialization for interaction tests. */
    private static class CountingStorage extends JsonAddressBookStorage {
        private int saveCount;
        private boolean shouldFail;

        CountingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saveCount++;
            if (shouldFail) {
                throw new IOException("Injected save failure");
            }
            super.saveAddressBook(addressBook);
        }
    }
}
