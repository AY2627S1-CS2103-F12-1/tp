package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.model.homework.DueDateTest.fullDateFormatMessage;
import static seedu.address.storage.JsonAdaptedStudent.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.BENSON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.TextNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.homework.Homework;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentName;
import seedu.address.model.student.StudentPhone;
import seedu.address.model.student.TuitionSubjects;
import seedu.address.testutil.HomeworkBuilder;
import seedu.address.testutil.StudentBuilder;

public class JsonAdaptedStudentTest {
    private static final String VALID_NAME = "John Tan";
    private static final String VALID_ACADEMIC_LEVEL = "S3";
    private static final List<String> VALID_SUBJECTS = List.of("MATH");
    private static final String VALID_PHONE = "91234567";
    private static final String VALID_GUARDIAN_NAME = "Mary Tan";
    private static final String VALID_GUARDIAN_PHONE = "98765432";
    private static final List<JsonAdaptedHomework> VALID_HOMEWORKS = List.of(new JsonAdaptedHomework(ALGEBRA));

    private static final String INVALID_NAME = "J@hn";
    private static final String INVALID_PHONE = "1234";

    @Test
    public void toModelType_studentWithHomework_returnsStudent() throws Exception {
        assertEquals(ALICE, new JsonAdaptedStudent(ALICE).toModelType());
    }

    @Test
    public void toModelType_studentWithoutHomework_returnsStudent() throws Exception {
        assertEquals(BENSON, new JsonAdaptedStudent(BENSON).toModelType());
    }

    @Test
    public void toModelType_validDetails_returnsStudent() throws Exception {
        Student expectedStudent = new StudentBuilder().withHomeworks(ALGEBRA).build();
        JsonAdaptedStudent student = new JsonAdaptedStudent(VALID_NAME, VALID_ACADEMIC_LEVEL, VALID_SUBJECTS,
                VALID_PHONE, VALID_GUARDIAN_NAME, VALID_GUARDIAN_PHONE, VALID_HOMEWORKS);
        assertEquals(expectedStudent, student.toModelType());
    }

    @Test
    public void toModelType_nullHomeworks_returnsStudentWithoutHomework() throws Exception {
        JsonAdaptedStudent student = new JsonAdaptedStudent(VALID_NAME, VALID_ACADEMIC_LEVEL, VALID_SUBJECTS,
                VALID_PHONE, VALID_GUARDIAN_NAME, VALID_GUARDIAN_PHONE, null);
        assertEquals(new StudentBuilder().build(), student.toModelType());
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedStudent student = new JsonAdaptedStudent(null, VALID_ACADEMIC_LEVEL, VALID_SUBJECTS,
                VALID_PHONE, VALID_GUARDIAN_NAME, VALID_GUARDIAN_PHONE, VALID_HOMEWORKS);
        assertMissingField(student, "name");
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedStudent student = new JsonAdaptedStudent(INVALID_NAME, VALID_ACADEMIC_LEVEL, VALID_SUBJECTS,
                VALID_PHONE, VALID_GUARDIAN_NAME, VALID_GUARDIAN_PHONE, VALID_HOMEWORKS);
        assertThrows(IllegalValueException.class, StudentName.MESSAGE_CONSTRAINTS, student::toModelType);
    }

    @Test
    public void toModelType_nullAcademicLevel_throwsIllegalValueException() {
        JsonAdaptedStudent student = new JsonAdaptedStudent(VALID_NAME, null, VALID_SUBJECTS,
                VALID_PHONE, VALID_GUARDIAN_NAME, VALID_GUARDIAN_PHONE, VALID_HOMEWORKS);
        assertMissingField(student, "academicLevel");
    }

    @Test
    public void toModelType_invalidAcademicLevel_throwsIllegalValueException() {
        JsonAdaptedStudent student = new JsonAdaptedStudent(VALID_NAME, "P6", VALID_SUBJECTS,
                VALID_PHONE, VALID_GUARDIAN_NAME, VALID_GUARDIAN_PHONE, VALID_HOMEWORKS);
        assertThrows(IllegalValueException.class, AcademicLevel.MESSAGE_CONSTRAINTS, student::toModelType);
    }

    @Test
    public void toModelType_nullSubjects_throwsIllegalValueException() {
        JsonAdaptedStudent student = new JsonAdaptedStudent(VALID_NAME, VALID_ACADEMIC_LEVEL, null,
                VALID_PHONE, VALID_GUARDIAN_NAME, VALID_GUARDIAN_PHONE, VALID_HOMEWORKS);
        assertMissingField(student, "subjects");
    }

    @Test
    public void toModelType_invalidSubjects_throwsIllegalValueException() {
        assertInvalidSubjects(List.of()); // no subject
        assertInvalidSubjects(List.of("BIOLOGY")); // unsupported subject
        assertInvalidSubjects(List.of("MATH", "math")); // repeated subject
        assertInvalidSubjects(List.of("MATH, PHYSICS")); // two subjects in one entry
        assertInvalidSubjects(Arrays.asList("MATH", null)); // empty entry
    }

    @Test
    public void toModelType_subjectsInAnyCase_returnsStudentWithSubjectsInOrder() throws Exception {
        JsonAdaptedStudent student = new JsonAdaptedStudent(VALID_NAME, VALID_ACADEMIC_LEVEL,
                List.of(" chemistry ", "Math"), VALID_PHONE, VALID_GUARDIAN_NAME, VALID_GUARDIAN_PHONE, null);
        assertEquals(new TuitionSubjects("CHEMISTRY, MATH"), student.toModelType().getSubjects());
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedStudent student = new JsonAdaptedStudent(VALID_NAME, VALID_ACADEMIC_LEVEL, VALID_SUBJECTS,
                null, VALID_GUARDIAN_NAME, VALID_GUARDIAN_PHONE, VALID_HOMEWORKS);
        assertMissingField(student, "phone");
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedStudent student = new JsonAdaptedStudent(VALID_NAME, VALID_ACADEMIC_LEVEL, VALID_SUBJECTS,
                INVALID_PHONE, VALID_GUARDIAN_NAME, VALID_GUARDIAN_PHONE, VALID_HOMEWORKS);
        assertThrows(IllegalValueException.class, StudentPhone.MESSAGE_CONSTRAINTS, student::toModelType);
    }

    @Test
    public void toModelType_nullGuardianName_throwsIllegalValueException() {
        JsonAdaptedStudent student = new JsonAdaptedStudent(VALID_NAME, VALID_ACADEMIC_LEVEL, VALID_SUBJECTS,
                VALID_PHONE, null, VALID_GUARDIAN_PHONE, VALID_HOMEWORKS);
        assertMissingField(student, "guardianName");
    }

    @Test
    public void toModelType_invalidGuardianName_throwsIllegalValueException() {
        JsonAdaptedStudent student = new JsonAdaptedStudent(VALID_NAME, VALID_ACADEMIC_LEVEL, VALID_SUBJECTS,
                VALID_PHONE, INVALID_NAME, VALID_GUARDIAN_PHONE, VALID_HOMEWORKS);
        assertThrows(IllegalValueException.class, StudentName.MESSAGE_CONSTRAINTS, student::toModelType);
    }

    @Test
    public void toModelType_nullGuardianPhone_throwsIllegalValueException() {
        JsonAdaptedStudent student = new JsonAdaptedStudent(VALID_NAME, VALID_ACADEMIC_LEVEL, VALID_SUBJECTS,
                VALID_PHONE, VALID_GUARDIAN_NAME, null, VALID_HOMEWORKS);
        assertMissingField(student, "guardianPhone");
    }

    @Test
    public void toModelType_invalidGuardianPhone_throwsIllegalValueException() {
        JsonAdaptedStudent student = new JsonAdaptedStudent(VALID_NAME, VALID_ACADEMIC_LEVEL, VALID_SUBJECTS,
                VALID_PHONE, VALID_GUARDIAN_NAME, INVALID_PHONE, VALID_HOMEWORKS);
        assertThrows(IllegalValueException.class, StudentPhone.MESSAGE_CONSTRAINTS, student::toModelType);
    }

    @Test
    public void toModelType_invalidHomework_throwsIllegalValueException() {
        List<JsonAdaptedHomework> homeworks = List.of(new JsonAdaptedHomework("Algebra", "MATH", "10-15", null));
        JsonAdaptedStudent student = new JsonAdaptedStudent(VALID_NAME, VALID_ACADEMIC_LEVEL, VALID_SUBJECTS,
                VALID_PHONE, VALID_GUARDIAN_NAME, VALID_GUARDIAN_PHONE, homeworks);
        assertThrows(IllegalValueException.class, fullDateFormatMessage("10-15"), student::toModelType);
    }

    @Test
    public void toModelType_emptyHomeworkEntry_throwsIllegalValueException() {
        List<JsonAdaptedHomework> homeworks = new ArrayList<>(VALID_HOMEWORKS);
        homeworks.add(null);
        JsonAdaptedStudent student = new JsonAdaptedStudent(VALID_NAME, VALID_ACADEMIC_LEVEL, VALID_SUBJECTS,
                VALID_PHONE, VALID_GUARDIAN_NAME, VALID_GUARDIAN_PHONE, homeworks);
        assertThrows(IllegalValueException.class, JsonAdaptedStudent.MESSAGE_EMPTY_HOMEWORK_ENTRY,
                student::toModelType);
    }

    @Test
    public void toModelType_homeworkSubjectNotTaken_throwsIllegalValueException() {
        List<JsonAdaptedHomework> homeworks = List.of(new JsonAdaptedHomework(
                new HomeworkBuilder().withSubject("PHYSICS").build()));
        JsonAdaptedStudent student = new JsonAdaptedStudent(VALID_NAME, VALID_ACADEMIC_LEVEL, VALID_SUBJECTS,
                VALID_PHONE, VALID_GUARDIAN_NAME, VALID_GUARDIAN_PHONE, homeworks);
        assertThrows(IllegalValueException.class, JsonAdaptedStudent.MESSAGE_SUBJECT_NOT_TAKEN,
                student::toModelType);
    }

    @Test
    public void toModelType_duplicateHomework_throwsIllegalValueException() {
        // Same title ignoring case, subject and due date; the score differs
        JsonAdaptedHomework duplicate = new JsonAdaptedHomework("COMPLETE ALGEBRA WORKSHEET", "MATH", "2026-10-15",
                TextNode.valueOf("9/10"));
        List<JsonAdaptedHomework> homeworks = List.of(new JsonAdaptedHomework(ALGEBRA), duplicate);
        JsonAdaptedStudent student = new JsonAdaptedStudent(VALID_NAME, VALID_ACADEMIC_LEVEL, VALID_SUBJECTS,
                VALID_PHONE, VALID_GUARDIAN_NAME, VALID_GUARDIAN_PHONE, homeworks);
        assertThrows(IllegalValueException.class, JsonAdaptedStudent.MESSAGE_DUPLICATE_HOMEWORK,
                student::toModelType);
    }

    @Test
    public void toModelType_duplicateHomeworkDifferentUnicodeForm_throwsIllegalValueException() {
        // composed e with acute accent, and e followed by a combining acute accent
        List<JsonAdaptedHomework> homeworks = List.of(
                new JsonAdaptedHomework("Caf\u00E9", "MATH", "2026-11-20", null),
                new JsonAdaptedHomework("Cafe\u0301", "MATH", "2026-11-20", null));
        JsonAdaptedStudent student = new JsonAdaptedStudent(VALID_NAME, VALID_ACADEMIC_LEVEL, VALID_SUBJECTS,
                VALID_PHONE, VALID_GUARDIAN_NAME, VALID_GUARDIAN_PHONE, homeworks);
        assertThrows(IllegalValueException.class, JsonAdaptedStudent.MESSAGE_DUPLICATE_HOMEWORK,
                student::toModelType);
    }

    @Test
    public void toModelType_sameTitleDifferentDueDate_returnsStudentWithBothHomework() throws Exception {
        JsonAdaptedHomework laterAlgebra = new JsonAdaptedHomework(ALGEBRA.getTitle().toString(), "MATH",
                "2026-10-22", null);
        List<JsonAdaptedHomework> homeworks = List.of(new JsonAdaptedHomework(ALGEBRA), laterAlgebra);
        JsonAdaptedStudent student = new JsonAdaptedStudent(VALID_NAME, VALID_ACADEMIC_LEVEL, VALID_SUBJECTS,
                VALID_PHONE, VALID_GUARDIAN_NAME, VALID_GUARDIAN_PHONE, homeworks);
        Homework expectedLaterAlgebra = new HomeworkBuilder(ALGEBRA).withDueDate("2026-10-22").build();
        assertEquals(List.of(ALGEBRA, expectedLaterAlgebra), student.toModelType().getHomeworks());
    }

    private static void assertMissingField(JsonAdaptedStudent student, String fieldName) {
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, fieldName);
        assertThrows(IllegalValueException.class, expectedMessage, student::toModelType);
    }

    private static void assertInvalidSubjects(List<String> subjects) {
        JsonAdaptedStudent student = new JsonAdaptedStudent(VALID_NAME, VALID_ACADEMIC_LEVEL, subjects,
                VALID_PHONE, VALID_GUARDIAN_NAME, VALID_GUARDIAN_PHONE, VALID_HOMEWORKS);
        assertThrows(IllegalValueException.class, TuitionSubjects.MESSAGE_CONSTRAINTS, student::toModelType);
    }
}
