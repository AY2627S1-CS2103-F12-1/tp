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
    private final Integer score;

    /**
     * Constructs a {@code JsonAdaptedHomework} with the given homework details.
     *
     * @param score Score received for the homework, or null if none is recorded.
     */
    @JsonCreator
    public JsonAdaptedHomework(@JsonProperty("title") String title, @JsonProperty("subject") String subject,
            @JsonProperty("dueDate") String dueDate, @JsonProperty("status") String status,
            @JsonProperty("score") Integer score) {
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
        final Score modelScore = score == null ? null : new Score(score);
        return new Homework(modelTitle, modelSubject, modelDueDate, modelStatus, modelScore);
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
