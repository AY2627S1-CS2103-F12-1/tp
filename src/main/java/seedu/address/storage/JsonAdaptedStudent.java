package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.RegularLesson;
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
    /** Message for a duplicate homework, e.g. "Homework 2 (Essay)", and the position of the earlier homework. */
    public static final String MESSAGE_DUPLICATE_HOMEWORK =
            "%s has the same title, subject and due date as homework %d.";
    /** Prefix naming the entry or key that an error is about, e.g. "Homework 2 (Essay): " or "guardianPhone: ". */
    public static final String ENTRY_PREFIX_FORMAT = "%s: ";

    private final String name;
    private final String academicLevel;
    private final List<String> subjects;
    private final String phone;
    private final String guardianName;
    private final String guardianPhone;
    private final List<JsonAdaptedHomework> homeworks = new ArrayList<>();
    private final List<JsonAdaptedLesson> lessons = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedStudent} with the given student details, as read from the JSON file.
     *
     * @param name Name of the student, or null if the field is missing.
     * @param academicLevel Academic level of the student, such as {@code S3}, or null if the field is missing.
     * @param subjects Subjects the student takes, one per entry, or null if the field is missing.
     * @param phone Phone number of the student, or null if the field is missing.
     * @param guardianName Name of the student's guardian, or null if the field is missing.
     * @param guardianPhone Phone number of the student's guardian, or null if the field is missing.
     * @param homeworks Homework of the student in insertion order; null if the student has no homework.
     */
    public JsonAdaptedStudent(String name, String academicLevel, List<String> subjects, String phone,
            String guardianName, String guardianPhone, List<JsonAdaptedHomework> homeworks) {
        this(name, academicLevel, subjects, phone, guardianName, guardianPhone, homeworks, null);
    }

    /** Creates a student adapter, treating an absent lessons field as an empty list for older data files. */
    @JsonCreator
    public JsonAdaptedStudent(@JsonProperty("name") String name, @JsonProperty("academicLevel") String academicLevel,
            @JsonProperty("subjects") List<String> subjects, @JsonProperty("phone") String phone,
            @JsonProperty("guardianName") String guardianName, @JsonProperty("guardianPhone") String guardianPhone,
            @JsonProperty("homeworks") List<JsonAdaptedHomework> homeworks,
            @JsonProperty("lessons") List<JsonAdaptedLesson> lessons) {
        this.name = name;
        this.academicLevel = academicLevel;
        this.subjects = subjects == null ? null : new ArrayList<>(subjects);
        this.phone = phone;
        this.guardianName = guardianName;
        this.guardianPhone = guardianPhone;
        if (homeworks != null) {
            this.homeworks.addAll(homeworks);
        }
        if (lessons != null) {
            this.lessons.addAll(lessons);
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
        lessons.addAll(source.getLessons().stream().map(JsonAdaptedLesson::new).toList());
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

        final List<Homework> modelHomeworks = toModelHomeworks(modelSubjects);

        List<RegularLesson> modelLessons = new ArrayList<>();
        for (JsonAdaptedLesson adapted : lessons) {
            if (adapted == null) {
                throw new IllegalValueException("Student's lessons list contains an empty entry.");
            }
            RegularLesson lesson = adapted.toModelType();
            if (!modelSubjects.getSubjects().contains(lesson.getSubject())) {
                throw new IllegalValueException("Lesson subject must be one of the student's subjects.");
            }
            if (modelLessons.contains(lesson)) {
                throw new IllegalValueException("Student's lessons list contains duplicate lessons.");
            }
            modelLessons.add(lesson);
        }
        return new Student(modelName, modelAcademicLevel, modelSubjects, modelPhone, modelGuardianName,
                modelGuardianPhone, modelHomeworks, modelLessons);
    }

    /**
     * Returns the homework in the {@code homeworks} list, in order. Each homework must be for one of
     * {@code modelSubjects}, and no two homework records may be the same by {@link Homework#isSameHomework(Homework)}.
     *
     * @throws IllegalValueException If an entry is empty or invalid, is for another subject, or is a duplicate. The
     *         message names the homework by its position and title, e.g. "Homework 2 (Essay)".
     */
    private List<Homework> toModelHomeworks(TuitionSubjects modelSubjects) throws IllegalValueException {
        List<Homework> modelHomeworks = new ArrayList<>();
        for (int i = 0; i < homeworks.size(); i++) {
            JsonAdaptedHomework jsonAdaptedHomework = homeworks.get(i);
            if (jsonAdaptedHomework == null) {
                throw new IllegalValueException(MESSAGE_EMPTY_HOMEWORK_ENTRY);
            }
            String shownHomework = describeEntry("Homework", i + 1, jsonAdaptedHomework.getTitle());
            Homework homework = toModelEntry(jsonAdaptedHomework::toModelType, shownHomework);
            if (!modelSubjects.getSubjects().contains(homework.getSubject())) {
                throw new IllegalValueException(String.format(ENTRY_PREFIX_FORMAT, shownHomework)
                        + MESSAGE_SUBJECT_NOT_TAKEN);
            }
            for (int j = 0; j < modelHomeworks.size(); j++) {
                if (modelHomeworks.get(j).isSameHomework(homework)) {
                    throw new IllegalValueException(String.format(MESSAGE_DUPLICATE_HOMEWORK, shownHomework, j + 1));
                }
            }
            modelHomeworks.add(homework);
        }
        return modelHomeworks;
    }

    /**
     * Returns the name of an entry of a list in the data file as the user can find it: its {@code kind}, its
     * {@code position} counting from 1, and its {@code name} as written in the file, if any, e.g.
     * "Student 2 (Priya Nair)".
     */
    static String describeEntry(String kind, int position, String name) {
        String entry = kind + " " + position;
        return name == null || name.isBlank() ? entry : entry + " (" + name.strip() + ")";
    }

    /**
     * Returns the model object that {@code converter} makes from an entry of a list in the data file, adding the
     * entry's {@code shownName} in front of the message of any error.
     *
     * @throws IllegalValueException If {@code converter} fails.
     */
    static <T> T toModelEntry(ModelConverter<T> converter, String shownName) throws IllegalValueException {
        try {
            return converter.convert();
        } catch (IllegalValueException exception) {
            throw new IllegalValueException(String.format(ENTRY_PREFIX_FORMAT, shownName) + exception.getMessage(),
                    exception);
        }
    }

    /** Returns the name of the student, as written in the data file. */
    String getName() {
        return name;
    }

    /**
     * Converts an entry of the data file into a model object.
     */
    @FunctionalInterface
    interface ModelConverter<T> {
        T convert() throws IllegalValueException;
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
            throw new IllegalValueException(String.format(ENTRY_PREFIX_FORMAT, "subjects")
                    + TuitionSubjects.MESSAGE_CONSTRAINTS);
        }
        return toModelValue("subjects", String.join(",", subjects), TuitionSubjects::new,
                TuitionSubjects.MESSAGE_CONSTRAINTS);
    }

    /**
     * Returns the model value parsed from the required student field {@code value}.
     *
     * @throws IllegalValueException If the field is missing or its value is invalid. The message for an invalid value
     *         starts with the field's JSON key, as fields such as {@code phone} and {@code guardianPhone} share a rule.
     */
    private static <T> T toModelValue(String fieldName, String value, Function<String, T> parser,
            String messageConstraints) throws IllegalValueException {
        return JsonAdaptedHomework.toModelValue(MISSING_FIELD_MESSAGE_FORMAT, fieldName, value, parser,
                String.format(ENTRY_PREFIX_FORMAT, fieldName) + messageConstraints);
    }

}
