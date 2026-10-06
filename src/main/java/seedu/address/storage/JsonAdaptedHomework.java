package seedu.address.storage;

import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.homework.DueDate;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.homework.Score;
import seedu.address.model.homework.Title;
import seedu.address.model.student.TuitionSubject;

/**
 * Jackson-friendly version of {@link Homework}.
 */
class JsonAdaptedHomework {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Homework's %s field is missing!";

    private final String title;
    private final String subject;
    private final String dueDate;
    private final String status;
    private final Object score;

    /**
     * Constructs a {@code JsonAdaptedHomework} with the given homework details, as read from the JSON file.
     *
     * @param title Title of the homework, or null if the field is missing.
     * @param subject Subject of the homework, such as {@code MATH}, or null if the field is missing.
     * @param dueDate Due date in {@code YYYY-MM-DD} format, or null if the field is missing.
     * @param status Status of the homework, {@code ASSIGNED} or {@code COMPLETED}, or null if the field is missing.
     * @param score Score received for the homework, or null if none is recorded. It is kept as read from the JSON
     *     file (e.g. a {@code Double} for {@code 85.5}) so that {@link #toModelType()} can reject a score that is
     *     not a whole number instead of truncating it.
     */
    @JsonCreator
    public JsonAdaptedHomework(@JsonProperty("title") String title, @JsonProperty("subject") String subject,
            @JsonProperty("dueDate") String dueDate, @JsonProperty("status") String status,
            @JsonProperty("score") Object score) {
        this.title = title;
        this.subject = subject;
        this.dueDate = dueDate;
        this.status = status;
        this.score = score;
    }

    /**
     * Converts a given {@code Homework} into this class for Jackson use.
     */
    public JsonAdaptedHomework(Homework source) {
        title = source.getTitle().toString();
        subject = source.getSubject().name();
        dueDate = source.getDueDate().toString();
        status = source.getStatus().name();
        score = source.getScore().map(Score::getValue).orElse(null);
    }

    /**
     * Converts this Jackson-friendly adapted homework object into the model's {@code Homework} object.
     * The due date must be a full {@code YYYY-MM-DD} date; its year is never inferred.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted homework.
     */
    public Homework toModelType() throws IllegalValueException {
        final Title modelTitle = toModelValue("title", title, Title::new, Title.MESSAGE_CONSTRAINTS);
        final TuitionSubject modelSubject = toModelValue("subject", subject, TuitionSubject::parse,
                TuitionSubject.MESSAGE_CONSTRAINTS);
        final DueDate modelDueDate = toModelValue("dueDate", dueDate, DueDate::parseFullDate,
                DueDate.MESSAGE_FULL_DATE_CONSTRAINTS);
        final HomeworkStatus modelStatus = toModelValue("status", status, HomeworkStatus::parse,
                HomeworkStatus.MESSAGE_CONSTRAINTS);
        final Score modelScore = toModelScore(score);
        return new Homework(modelTitle, modelSubject, modelDueDate, modelStatus, modelScore);
    }

    /**
     * Returns the model score for the optional field {@code score}, or null if the field is missing.
     *
     * @throws IllegalValueException If {@code score} is not a JSON whole number within the {@code int} range.
     */
    private static Score toModelScore(Object score) throws IllegalValueException {
        if (score == null) {
            return null;
        }
        // Jackson reads a whole number within the int range as an Integer, and 85.5 as a Double
        if (!(score instanceof Integer value)) {
            throw new IllegalValueException(Score.MESSAGE_CONSTRAINTS);
        }
        return new Score(value);
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
