package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TestUtil.ARABIC_EGYPT;
import static seedu.address.testutil.TestUtil.runWithFormatLocale;
import static seedu.address.testutil.TypicalStudents.getModelWithStudents;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.Model;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.Student;
import seedu.address.model.student.TuitionSubject;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.StudentBuilder;

public class LessonIndexLocaleIntegrationTest {
    @TempDir
    Path temporaryFolder;

    @Test
    public void execute_arabicFormatLocale_globalIndicesRemainUsable() throws Exception {
        List<Student> students = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            LocalTime start = LocalTime.of(9, 0).plusMinutes(i * 30L);
            RegularLesson lesson = new RegularLesson(TuitionSubject.MATH, DayOfWeek.MONDAY,
                    start, start.plusMinutes(30));
            students.add(new StudentBuilder().withName("Student " + (i + 1))
                    .withPhone(Integer.toString(91230000 + i)).withLessons(lesson).build());
        }
        Model model = getModelWithStudents(students);
        JsonAddressBookStorage data = new JsonAddressBookStorage(temporaryFolder.resolve("tutorflow.json"));
        data.saveAddressBook(model.getAddressBook());
        LogicManager logic = new LogicManager(model, new StorageManager(data,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        logic.execute("search 91230000");
        var displayed = List.copyOf(model.getStudentList());
        byte[] saved = Files.readAllBytes(data.getAddressBookFilePath());
        String[] feedback = new String[1];

        runWithFormatLocale(ARABIC_EGYPT, () -> {
            try {
                feedback[0] = logic.execute("lesson list").getFeedbackToUser();
            } catch (Exception exception) {
                throw new AssertionError(exception);
            }
        });

        List<String> lines = feedback[0].lines().toList();
        assertEquals("Listed 12 regular lessons.", lines.getFirst());
        for (int i = 1; i <= 12; i++) {
            assertTrue(lines.get(i).startsWith(i + ". Student " + i + " ("), lines.get(i));
        }
        assertEquals(displayed, model.getStudentList());
        assertArrayEquals(saved, Files.readAllBytes(data.getAddressBookFilePath()));

        String finalIndex = lines.getLast().substring(0, lines.getLast().indexOf('.'));
        logic.execute("lesson delete " + finalIndex);

        assertEquals(displayed, model.getStudentList());
        assertTrue(model.getAddressBook().getStudentList().getLast().getLessons().isEmpty());
        assertEquals(1, model.getAddressBook().getStudentList().getFirst().getLessons().size());
        assertEquals(model.getAddressBook(), data.readAddressBook().orElseThrow());
    }
}
