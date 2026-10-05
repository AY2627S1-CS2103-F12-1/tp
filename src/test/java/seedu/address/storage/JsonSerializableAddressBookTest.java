package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.homework.DueDate;
import seedu.address.model.student.StudentPhone;
import seedu.address.testutil.TypicalStudents;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_STUDENTS_FILE = TEST_DATA_FOLDER.resolve("typicalStudentsAddressBook.json");
    private static final Path INVALID_STUDENT_FILE = TEST_DATA_FOLDER.resolve("invalidStudentAddressBook.json");
    private static final Path INVALID_HOMEWORK_FILE = TEST_DATA_FOLDER.resolve("invalidHomeworkAddressBook.json");
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
        assertThrows(IllegalValueException.class, StudentPhone.MESSAGE_CONSTRAINTS, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_shortFormDueDate_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_HOMEWORK_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, DueDate.MESSAGE_FULL_DATE_CONSTRAINTS, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicateStudents_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_STUDENT_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_STUDENT,
                dataFromFile::toModelType);
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

}
