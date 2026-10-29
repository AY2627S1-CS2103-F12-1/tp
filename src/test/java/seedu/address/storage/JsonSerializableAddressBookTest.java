package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.model.homework.DueDateTest.fullDateFormatMessage;
import static seedu.address.model.util.SampleDataUtil.getSampleAddressBook;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentName;
import seedu.address.model.student.StudentPhone;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.HomeworkBuilder;
import seedu.address.testutil.StudentBuilder;
import seedu.address.testutil.TypicalStudents;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_STUDENTS_FILE = TEST_DATA_FOLDER.resolve("typicalStudentsAddressBook.json");
    private static final Path INVALID_STUDENT_FILE = TEST_DATA_FOLDER.resolve("invalidStudentAddressBook.json");
    private static final Path INVALID_HOMEWORK_FILE = TEST_DATA_FOLDER.resolve("invalidHomeworkAddressBook.json");
    private static final Path SINGLE_NUMBER_SCORE_FILE =
            TEST_DATA_FOLDER.resolve("singleNumberScoreAddressBook.json");
    private static final Path DUPLICATE_STUDENT_FILE = TEST_DATA_FOLDER.resolve("duplicateStudentAddressBook.json");
    private static final Path MISSING_STUDENTS_FILE = TEST_DATA_FOLDER.resolve("missingStudentsAddressBook.json");

    @Test
    public void toModelType_typicalStudentsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_STUDENTS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalStudentsAddressBook = TypicalStudents.getTypicalAddressBook();
        assertEquals(typicalStudentsAddressBook, addressBookFromFile);
    }

    @Test
    public void toModelType_invalidStudentFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_STUDENT_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, "Student 1 (Alice Tan): phone: "
                + StudentPhone.MESSAGE_CONSTRAINTS, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_shortFormDueDate_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_HOMEWORK_FILE,
                JsonSerializableAddressBook.class).get();
        // the file's due date is "10-15"
        assertThrows(IllegalValueException.class, "Student 1 (Alice Tan): Homework 1 (Complete algebra worksheet): "
                + fullDateFormatMessage("10-15"), dataFromFile::toModelType);
    }

    @Test
    public void toModelType_singleNumberScore_loadsBareScore() throws Exception {
        // The number 85, as saved before scores were text, and the text "85" both load as the score 85; the
        // invalid number 85.5 is dropped, and the rest of the file still loads
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(SINGLE_NUMBER_SCORE_FILE,
                JsonSerializableAddressBook.class).get();
        Student expectedStudent = new StudentBuilder().withName("Alice Tan").withPhone("91111111")
                .withHomeworks(new HomeworkBuilder().withScore("85").build(),
                        new HomeworkBuilder().withTitle("Revise quadratic equations").withDueDate("2026-10-22")
                                .withScore("7/10").build(),
                        new HomeworkBuilder().withTitle("Attempt factorization questions").withDueDate("2026-10-29")
                                .withScore("85").build(),
                        new HomeworkBuilder().withTitle("Revise indices").withDueDate("2026-11-05").build())
                .build();
        assertEquals(new AddressBookBuilder().withStudent(expectedStudent).build(), dataFromFile.toModelType());
    }

    @Test
    public void toModelType_duplicateStudents_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_STUDENT_FILE,
                JsonSerializableAddressBook.class).get();
        // the second student's name differs only in case
        assertThrows(IllegalValueException.class,
                "Student 2 (ALICE TAN) has the same name and phone number as student 1.", dataFromFile::toModelType);
    }

    @Test
    public void toModelType_invalidPhoneInSampleData_namesStudent() throws Exception {
        // Priya Nair's phone changed to "abc" in the sample data, as found in PE testing
        String sampleData = JsonUtil.toJsonString(new JsonSerializableAddressBook(getSampleAddressBook()));
        String invalidData = sampleData.replace("\"phone\" : \"82345671\"", "\"phone\" : \"abc\"");
        assertNotEquals(sampleData, invalidData);
        JsonSerializableAddressBook data = JsonUtil.fromJsonString(invalidData, JsonSerializableAddressBook.class);

        assertThrows(IllegalValueException.class, "Student 2 (Priya Nair): phone: "
                + StudentPhone.MESSAGE_CONSTRAINTS, data::toModelType);
    }

    @Test
    public void toModelType_invalidGuardianFieldsInSampleData_namesKey() throws Exception {
        // PE round 2: Ethan Lim's guardian fields changed by hand, which gave the same reason as his own phone or name
        String sampleData = JsonUtil.toJsonString(new JsonSerializableAddressBook(getSampleAddressBook()));
        String invalidPhoneData = sampleData.replace("\"guardianPhone\" : \"98765432\"",
                "\"guardianPhone\" : \"12ab\"");
        assertNotEquals(sampleData, invalidPhoneData);
        JsonSerializableAddressBook invalidPhone = JsonUtil.fromJsonString(invalidPhoneData,
                JsonSerializableAddressBook.class);
        assertThrows(IllegalValueException.class, "Student 1 (Ethan Lim): guardianPhone: Phone numbers must be "
                + "an 8-digit Singapore number starting with 6, 8, or 9, or an international number in "
                + "+<country code><number> format.", invalidPhone::toModelType);

        String invalidNameData = sampleData.replace("\"guardianName\" : \"Grace Lim\"",
                "\"guardianName\" : \"Grace@Lim\"");
        assertNotEquals(sampleData, invalidNameData);
        JsonSerializableAddressBook invalidName = JsonUtil.fromJsonString(invalidNameData,
                JsonSerializableAddressBook.class);
        assertThrows(IllegalValueException.class, "Student 1 (Ethan Lim): guardianName: "
                + StudentName.MESSAGE_CONSTRAINTS, invalidName::toModelType);
    }

    @Test
    public void toModelType_duplicateHomework_namesStudentAndHomework() {
        List<JsonAdaptedHomework> homeworks = List.of(
                new JsonAdaptedHomework("Integration by parts exercise", "MATH", "2026-10-26", null),
                new JsonAdaptedHomework("Integration by parts exercise", "MATH", "2026-10-26", null));
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(
                new JsonAdaptedStudent(TypicalStudents.ALICE), new JsonAdaptedStudent("Priya Nair", "J1",
                        List.of("MATH", "CHEMISTRY"), "82345671", "Ravi Nair", "96543218", homeworks)));

        assertThrows(IllegalValueException.class, "Student 2 (Priya Nair): Homework 2 (Integration by parts"
                + " exercise) has the same title, subject and due date as homework 1.", data::toModelType);
    }

    @Test
    public void toModelType_lessonOverlapsOtherStudent_namesStudent() throws Exception {
        RegularLesson lesson = new JsonAdaptedLesson("MATH", "MONDAY", "16:00", "18:00").toModelType();
        RegularLesson overlapping = new JsonAdaptedLesson("MATH", "MONDAY", "17:00", "19:00").toModelType();
        Student john = new StudentBuilder().build().withLessons(List.of(lesson));
        Student mary = new StudentBuilder().withName("Mary Lim").build().withLessons(List.of(overlapping));
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(
                new JsonAdaptedStudent(john), new JsonAdaptedStudent(mary)));

        assertThrows(IllegalValueException.class, "Student 2 (Mary Lim): Lesson 1 (MONDAY 17:00-19:00) overlaps "
                + "lesson 1 (MONDAY 16:00-18:00) of student 1 (John Tan).", data::toModelType);
    }

    @Test
    public void toModelType_lessonOverlapsOwnLesson_namesBothLessons() throws Exception {
        RegularLesson lesson = new JsonAdaptedLesson("MATH", "MONDAY", "16:00", "18:00").toModelType();
        RegularLesson overlapping = new JsonAdaptedLesson("MATH", "MONDAY", "17:00", "19:00").toModelType();
        Student john = new StudentBuilder().build().withLessons(List.of(lesson, overlapping));
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(new JsonAdaptedStudent(john)));

        assertThrows(IllegalValueException.class, "Student 1 (John Tan): Lesson 1 (MONDAY 16:00-18:00) overlaps "
                + "lesson 2 (MONDAY 17:00-19:00).", data::toModelType);
    }

    @Test
    public void toModelType_invalidLessonInSampleData_namesLesson() throws Exception {
        // PE round 2: the reasons for a lesson named the student but not the lesson
        assertSampleDataReason("\"dayOfWeek\" : \"MONDAY\"", "\"dayOfWeek\" : \"MOONDAY\"",
                "Student 1 (Ethan Lim): Lesson 1 (MOONDAY 17:00-18:30): " + RegularLesson.MESSAGE_DAY);
        assertSampleDataReason("\"endTime\" : \"18:30\"", "\"finishTime\" : \"18:30\"",
                "Student 1 (Ethan Lim): Lesson 1: Lesson subject, dayOfWeek, startTime and endTime are required.");
        assertSampleDataReason("\"subject\" : \"CHEMISTRY\",\n      \"dayOfWeek\"",
                "\"subject\" : \"PHYSICS\",\n      \"dayOfWeek\"",
                "Student 2 (Priya Nair): Lesson 1 (SATURDAY 15:00-16:30): Lesson subject must be one of the "
                + "student's subjects.");
        // Priya Nair's Saturday lesson moved to overlap Ethan Lim's Monday lesson
        assertSampleDataReason("\"SATURDAY\",\n      \"startTime\" : \"15:00\",\n      \"endTime\" : \"16:30\"",
                "\"MONDAY\",\n      \"startTime\" : \"18:00\",\n      \"endTime\" : \"19:00\"",
                "Student 2 (Priya Nair): Lesson 1 (MONDAY 18:00-19:00) overlaps lesson 1 (MONDAY 17:00-18:30) of "
                + "student 1 (Ethan Lim).");
    }

    @Test
    public void toModelType_duplicateLesson_namesBothLessons() throws Exception {
        List<JsonAdaptedLesson> lessons = List.of(new JsonAdaptedLesson("MATH", "MONDAY", "16:00", "18:00"),
                new JsonAdaptedLesson("MATH", "MONDAY", "16:00", "18:00"));
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(new JsonAdaptedStudent("John Tan",
                "S3", List.of("MATH"), "91234567", "Mary Tan", "98765432", List.of(), lessons)));

        assertThrows(IllegalValueException.class, "Student 1 (John Tan): Lesson 2 (MONDAY 16:00-18:00) is the same "
                + "as lesson 1.", data::toModelType);
    }

    @Test
    public void toModelType_missingName_namesStudentByPosition() {
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(new JsonAdaptedStudent(
                null, "S3", List.of("MATH"), "91234567", "Mary Tan", "98765432", null)));

        assertThrows(IllegalValueException.class, "Student 1: Student's name field is missing!", data::toModelType);
    }

    @Test
    public void toModelType_missingStudentsField_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(MISSING_STUDENTS_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_MISSING_STUDENTS,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_emptyStudentEntry_throwsIllegalValueException() {
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(Arrays.asList((JsonAdaptedStudent) null));
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_EMPTY_STUDENT_ENTRY,
                data::toModelType);
    }

    @Test
    public void toModelType_noStudents_returnsEmptyAddressBook() throws Exception {
        assertEquals(new AddressBook(), new JsonSerializableAddressBook(Arrays.asList()).toModelType());
    }

    @Test
    public void toModelType_fromTypicalAddressBook_returnsEqualAddressBook() throws Exception {
        AddressBook typicalAddressBook = TypicalStudents.getTypicalAddressBook();
        assertEquals(typicalAddressBook, new JsonSerializableAddressBook(typicalAddressBook).toModelType());
    }

    /**
     * Asserts that the sample data, with its first {@code target} replaced by {@code replacement}, is rejected with
     * {@code expectedReason}.
     */
    private static void assertSampleDataReason(String target, String replacement, String expectedReason)
            throws Exception {
        String sampleData = JsonUtil.toJsonString(new JsonSerializableAddressBook(getSampleAddressBook()));
        String invalidData = sampleData.replaceFirst(Pattern.quote(target), Matcher.quoteReplacement(replacement));
        assertNotEquals(sampleData, invalidData);
        JsonSerializableAddressBook data = JsonUtil.fromJsonString(invalidData, JsonSerializableAddressBook.class);
        assertThrows(IllegalValueException.class, expectedReason, data::toModelType);
    }
}
