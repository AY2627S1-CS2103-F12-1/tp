package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.homework.Homework;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentName;
import seedu.address.model.student.StudentPhone;
import seedu.address.model.student.TuitionSubject;
import seedu.address.model.student.TuitionSubjects;

/**
 * Jackson-friendly version of {@link Student}, including the student's homework.
 */
class JsonAdaptedStudent {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Student's %s field is missing!";
    public static final String MESSAGE_EMPTY_HOMEWORK_ENTRY = "Student's homeworks list contains an empty entry.";
    public static final String MESSAGE_SUBJECT_NOT_TAKEN = "Homework subject must be one of the student's subjects.";
    public static final String MESSAGE_DUPLICATE_HOMEWORK = "Student's homeworks list contains duplicate homework.";

    private final String name;
    private final String academicLevel;
    private final List<String> subjects;
    private final String phone;
    private final String guardianName;
    private final String guardianPhone;
    private final List<JsonAdaptedHomework> homeworks = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedStudent} with the given student details.
     *
     * @param homeworks Homework of the student in insertion order; null if the student has no homework.
     */
    @JsonCreator
    public JsonAdaptedStudent(@JsonProperty("name") String name, @JsonProperty("academicLevel") String academicLevel,
            @JsonProperty("subjects") List<String> subjects, @JsonProperty("phone") String phone,
            @JsonProperty("guardianName") String guardianName, @JsonProperty("guardianPhone") String guardianPhone,
            @JsonProperty("homeworks") List<JsonAdaptedHomework> homeworks) {
        this.name = name;
        this.academicLevel = academicLevel;
        this.subjects = subjects == null ? null : new ArrayList<>(subjects);
        this.phone = phone;
        this.guardianName = guardianName;
        this.guardianPhone = guardianPhone;
        if (homeworks != null) {
            this.homeworks.addAll(homeworks);
        }
    }

    /**
     * Converts a given {@code Student} into this class for Jackson use.
     */
    public JsonAdaptedStudent(Student source) {
        name = source.getName().toString();
        academicLevel = source.getAcademicLevel().name();
        subjects = source.getSubjects().getSubjects().stream().map(TuitionSubject::name).toList();
        phone = source.getPhone().toString();
        guardianName = source.getGuardianName().toString();
        guardianPhone = source.getGuardianPhone().toString();
        homeworks.addAll(source.getHomeworks().stream().map(JsonAdaptedHomework::new).toList());
    }

    /**
     * Converts this Jackson-friendly adapted student object into the model's {@code Student} object.
     * Each homework must be for a subject the student takes, and no two homework records may be the same by
     * {@link Homework#isSameHomework(Homework)}.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted student.
     */
    public Student toModelType() throws IllegalValueException {
        final StudentName modelName = toModelValue("name", name, StudentName::new, StudentName.MESSAGE_CONSTRAINTS);
        final AcademicLevel modelAcademicLevel = toModelValue("academicLevel", academicLevel, AcademicLevel::parse,
                AcademicLevel.MESSAGE_CONSTRAINTS);
        final TuitionSubjects modelSubjects = toModelSubjects();
        final StudentPhone modelPhone = toModelValue("phone", phone, StudentPhone::new,
                StudentPhone.MESSAGE_CONSTRAINTS);
        final StudentName modelGuardianName = toModelValue("guardianName", guardianName, StudentName::new,
                StudentName.MESSAGE_CONSTRAINTS);
        final StudentPhone modelGuardianPhone = toModelValue("guardianPhone", guardianPhone, StudentPhone::new,
                StudentPhone.MESSAGE_CONSTRAINTS);

        final List<Homework> modelHomeworks = new ArrayList<>();
        for (JsonAdaptedHomework jsonAdaptedHomework : homeworks) {
            if (jsonAdaptedHomework == null) {
                throw new IllegalValueException(MESSAGE_EMPTY_HOMEWORK_ENTRY);
            }
            Homework homework = jsonAdaptedHomework.toModelType();
            if (!modelSubjects.getSubjects().contains(homework.getSubject())) {
                throw new IllegalValueException(MESSAGE_SUBJECT_NOT_TAKEN);
            }
            if (modelHomeworks.stream().anyMatch(homework::isSameHomework)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_HOMEWORK);
            }
            modelHomeworks.add(homework);
        }

        return new Student(modelName, modelAcademicLevel, modelSubjects, modelPhone, modelGuardianName,
                modelGuardianPhone, modelHomeworks);
    }

    /**
     * Returns the tuition subjects in the {@code subjects} list, which must be nonempty and hold distinct
     * supported subjects.
     *
     * @throws IllegalValueException If the field is missing or its value is invalid.
     */
    private TuitionSubjects toModelSubjects() throws IllegalValueException {
        if (subjects == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "subjects"));
        }
        // A comma inside one entry would otherwise be read as two subjects
        if (subjects.stream().anyMatch(subject -> subject == null || subject.contains(","))) {
            throw new IllegalValueException(TuitionSubjects.MESSAGE_CONSTRAINTS);
        }
        return toModelValue("subjects", String.join(",", subjects), TuitionSubjects::new,
                TuitionSubjects.MESSAGE_CONSTRAINTS);
    }

    /**
     * Returns the model value parsed from the required field {@code value}.
     *
     * @param fieldName JSON name of the field, used in the message when the field is missing.
     * @param value Value of the field, or null if the field is missing.
     * @param parser Parser that throws an {@code IllegalArgumentException} if {@code value} is invalid.
     * @param messageConstraints Message used when {@code value} is invalid.
     * @throws IllegalValueException If the field is missing or its value is invalid.
     */
    private static <T> T toModelValue(String fieldName, String value, Function<String, T> parser,
            String messageConstraints) throws IllegalValueException {
        if (value == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, fieldName));
        }
        try {
            return parser.apply(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(messageConstraints);
        }
    }

}
