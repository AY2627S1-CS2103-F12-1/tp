package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.Student;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 * It holds every student, and each student holds their homework.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_MISSING_STUDENTS = "The students field is missing!";
    /** Message for a duplicate student, e.g. "Student 3 (Alex Yeoh)", and the position of the earlier student. */
    public static final String MESSAGE_DUPLICATE_STUDENT = "%s has the same name and phone number as student %d.";
    /**
     * Message for a lesson, e.g. "Lesson 1 (MONDAY 17:00-18:30)", that overlaps another lesson, e.g.
     * "lesson 2 (MONDAY 18:00-19:00)" of the same student, or "lesson 1 (MONDAY 18:00-19:00) of student 1 (John Tan)".
     */
    public static final String MESSAGE_OVERLAPPING_LESSON = "%s overlaps %s.";

    private final List<JsonAdaptedStudent> students;

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given students.
     *
     * @param students Students in roster order, or null if the field is missing.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("students") List<JsonAdaptedStudent> students) {
        this.students = students == null ? null : new ArrayList<>(students);
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        students = new ArrayList<>(source.getStudentList().stream().map(JsonAdaptedStudent::new).toList());
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated. The message names the student by
     *         their position and name, e.g. "Student 2 (Priya Nair)".
     */
    public AddressBook toModelType() throws IllegalValueException {
        // A missing or misspelled key is reported as invalid data rather than read as having no students
        if (students == null) {
            throw new IllegalValueException(MESSAGE_MISSING_STUDENTS);
        }
        AddressBook addressBook = new AddressBook();
        for (int i = 0; i < students.size(); i++) {
            JsonAdaptedStudent jsonAdaptedStudent = students.get(i);
            if (jsonAdaptedStudent == null) {
                throw new IllegalValueException(String.format(JsonAdaptedStudent.MESSAGE_EMPTY_ENTRY,
                        JsonAdaptedStudent.describeEntry("Student", i + 1, null)));
            }
            String shownStudent = JsonAdaptedStudent.describeEntry("Student", i + 1, jsonAdaptedStudent.getName());
            Student student = JsonAdaptedStudent.toModelEntry(jsonAdaptedStudent::toModelType, shownStudent);
            // Every earlier student was added, so a student's place in the list is their place in the file
            List<Student> earlierStudents = addressBook.getStudentList();
            for (int j = 0; j < earlierStudents.size(); j++) {
                if (earlierStudents.get(j).isSameStudent(student)) {
                    throw new IllegalValueException(String.format(MESSAGE_DUPLICATE_STUDENT, shownStudent, j + 1));
                }
            }
            checkNoOverlappingLessons(student, jsonAdaptedStudent, shownStudent, earlierStudents);
            addressBook.addStudent(student);
        }
        return addressBook;
    }

    /**
     * Checks that no lesson of {@code student} overlaps another of their lessons or a lesson of
     * {@code earlierStudents}. A student's lessons are in the order of the data file.
     *
     * @param student The student to check, built from {@code adaptedStudent}.
     * @param adaptedStudent The student as read from the data file, whose lessons are shown as written there.
     * @param shownStudent Name of the student as the user can find them in the data file, e.g.
     *         "Student 2 (Priya Nair)".
     * @param earlierStudents Students before {@code student} in the data file, in the order of the file.
     * @throws IllegalValueException If two lessons overlap. The message names both lessons, and the other student.
     */
    private void checkNoOverlappingLessons(Student student, JsonAdaptedStudent adaptedStudent, String shownStudent,
            List<Student> earlierStudents) throws IllegalValueException {
        List<RegularLesson> lessons = student.getLessons();
        for (int i = 0; i < lessons.size(); i++) {
            RegularLesson lesson = lessons.get(i);
            String shownLesson = describeLesson("Lesson", i + 1, adaptedStudent);
            for (int j = 0; j < lessons.size(); j++) {
                if (j != i && lesson.clashesWith(lessons.get(j))) {
                    throw new IllegalValueException(String.format(JsonAdaptedStudent.ENTRY_PREFIX_FORMAT, shownStudent)
                            + String.format(MESSAGE_OVERLAPPING_LESSON, shownLesson,
                                    describeLesson("lesson", j + 1, adaptedStudent)));
                }
            }
            for (int j = 0; j < earlierStudents.size(); j++) {
                List<RegularLesson> otherLessons = earlierStudents.get(j).getLessons();
                for (int k = 0; k < otherLessons.size(); k++) {
                    if (lesson.clashesWith(otherLessons.get(k))) {
                        // Every earlier student was added, so their place in the list is their place in the file
                        String otherLesson = describeLesson("lesson", k + 1, students.get(j)) + " of "
                                + JsonAdaptedStudent.describeEntry("student", j + 1,
                                        earlierStudents.get(j).getName().toString());
                        throw new IllegalValueException(String.format(JsonAdaptedStudent.ENTRY_PREFIX_FORMAT,
                                shownStudent) + String.format(MESSAGE_OVERLAPPING_LESSON, shownLesson, otherLesson));
                    }
                }
            }
        }
    }

    /**
     * Returns the name of the lesson at {@code position} of {@code student}, counting from 1, as the user can find it
     * in the data file, with its day and times as written there, e.g. "Lesson 1 (MONDAY 17:00-18:30)".
     */
    private static String describeLesson(String kind, int position, JsonAdaptedStudent student) {
        return JsonAdaptedStudent.describeEntry(kind, position, student.getLessonTimeSlot(position - 1));
    }

}
