package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.student.Student;
import seedu.address.testutil.StudentBuilder;

public class JsonAdaptedLessonTest {
    @Test
    public void toModelType_roundTrip_preservesLesson() throws Exception {
        var lesson = new JsonAdaptedLesson("math", "monday", "16:00", "18:00").toModelType();
        assertEquals(lesson, new JsonAdaptedLesson(lesson).toModelType());
        Student student = new StudentBuilder().build().withLessons(List.of(lesson));
        assertEquals(student, new JsonAdaptedStudent(student).toModelType());
    }

    @Test
    public void toModelType_invalidFields_rejects() {
        for (JsonAdaptedLesson invalid : List.of(
                new JsonAdaptedLesson(null, "MONDAY", "16:00", "18:00"),
                new JsonAdaptedLesson("MATH", null, "16:00", "18:00"),
                new JsonAdaptedLesson("MATH", "MONDAY", null, "18:00"),
                new JsonAdaptedLesson("MATH", "MONDAY", "16:00", null),
                new JsonAdaptedLesson("BIOLOGY", "MONDAY", "16:00", "18:00"),
                new JsonAdaptedLesson("MATH", "MON", "16:00", "18:00"),
                new JsonAdaptedLesson("MATH", "MONDAY", "24:00", "18:00"),
                new JsonAdaptedLesson("MATH", "MONDAY", "16:00", "18:60"),
                new JsonAdaptedLesson("MATH", "MONDAY", "16:00", "16:00"))) {
            assertThrows(IllegalValueException.class, invalid::toModelType);
        }
    }

    @Test
    public void toModelType_studentConstraintsAndLegacyData() throws Exception {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson("MATH", "MONDAY", "16:00", "18:00");
        assertEquals(List.of(), adapted(null).toModelType().getLessons());
        assertThrows(IllegalValueException.class, () -> adapted(Arrays.asList((JsonAdaptedLesson) null)).toModelType());
        assertThrows(IllegalValueException.class, () -> adapted(List.of(lesson, lesson)).toModelType());
        assertThrows(IllegalValueException.class, () ->
                adapted(List.of(new JsonAdaptedLesson("PHYSICS", "MONDAY", "16:00", "18:00"))).toModelType());
    }

    @Test
    public void toModelType_scheduleClashes_rejectsWithinAndAcrossStudents() throws Exception {
        var lesson = new JsonAdaptedLesson("MATH", "MONDAY", "16:00", "18:00").toModelType();
        var overlapping = new JsonAdaptedLesson("MATH", "MONDAY", "17:00", "19:00").toModelType();
        Student john = new StudentBuilder().build().withLessons(List.of(lesson));
        Student mary = new StudentBuilder().withName("Mary Lim").build().withLessons(List.of(overlapping));
        var across = new JsonSerializableAddressBook(List.of(
                new JsonAdaptedStudent(john), new JsonAdaptedStudent(mary)));
        assertThrows(IllegalValueException.class, across::toModelType);
        var within = new JsonSerializableAddressBook(List.of(
                new JsonAdaptedStudent(john.withLessons(List.of(lesson, overlapping)))));
        assertThrows(IllegalValueException.class, within::toModelType);
        var adjacent = new JsonAdaptedLesson("MATH", "MONDAY", "18:00", "19:00").toModelType();
        var valid = new JsonSerializableAddressBook(List.of(
                new JsonAdaptedStudent(john.withLessons(List.of(lesson, adjacent)))));
        assertEquals(2, valid.toModelType().getStudentList().get(0).getLessons().size());
    }

    private JsonAdaptedStudent adapted(List<JsonAdaptedLesson> lessons) {
        return new JsonAdaptedStudent("John Tan", "S3", List.of("MATH"), "91234567", "Mary Tan", "98765432",
                List.of(), lessons);
    }
}
