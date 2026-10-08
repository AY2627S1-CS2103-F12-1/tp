package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.student.Student;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 * It holds every student, and each student holds their homework.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_MISSING_STUDENTS = "The students field is missing!";
    public static final String MESSAGE_EMPTY_STUDENT_ENTRY = "Students list contains an empty entry.";
    public static final String MESSAGE_DUPLICATE_STUDENT = "Students list contains duplicate student(s).";

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
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        // A missing or misspelled key is reported as invalid data rather than read as having no students
        if (students == null) {
            throw new IllegalValueException(MESSAGE_MISSING_STUDENTS);
        }
        AddressBook addressBook = new AddressBook();
        for (JsonAdaptedStudent jsonAdaptedStudent : students) {
            if (jsonAdaptedStudent == null) {
                throw new IllegalValueException(MESSAGE_EMPTY_STUDENT_ENTRY);
            }
            Student student = jsonAdaptedStudent.toModelType();
            if (addressBook.hasStudent(student)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_STUDENT);
            }
            for (var lesson : student.getLessons()) {
                long overlaps = student.getLessons().stream().filter(lesson::clashesWith).count();
                boolean clashesWithOthers = addressBook.getStudentList().stream()
                        .flatMap(existing -> existing.getLessons().stream()).anyMatch(lesson::clashesWith);
                if (overlaps > 1 || clashesWithOthers) {
                    throw new IllegalValueException("The data file contains overlapping lessons.");
                }
            }
            addressBook.addStudent(student);
        }
        return addressBook;
    }

}
