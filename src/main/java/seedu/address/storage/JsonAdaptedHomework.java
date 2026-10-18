package seedu.address.storage;

import java.util.function.Function;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.homework.DueDate;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.Score;
import seedu.address.model.homework.Title;
import seedu.address.model.student.TuitionSubject;

/**
 * Jackson-friendly version of {@link Homework}.
 */
class JsonAdaptedHomework {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Homework's %s field is missing!";
    public static final String OLD_FORMAT_SCORE_WARNING_FORMAT = "Ignored old-format homework score %s for '%s'";

    private static final Logger logger = LogsCenter.getLogger(JsonAdaptedHomework.class);

    private final String title;
    private final String subject;
    private final String dueDate;
    private final JsonNode score;

    /**
     * Constructs a {@code JsonAdaptedHomework} with the given homework details, as read from the JSON file.
     *
     * @param title Title of the homework, or null if the field is missing.
     * @param subject Subject of the homework, such as {@code MATH}, or null if the field is missing.
     * @param dueDate Due date in {@code YYYY-MM-DD} format, or null if the field is missing.
     * @param score Score as text in {@code MARKS_OBTAINED/TOTAL_MARKS} format, a whole number saved before scores
     *         had a total, or null if none is recorded.
     */
    @JsonCreator
    public JsonAdaptedHomework(@JsonProperty("title") String title, @JsonProperty("subject") String subject,
            @JsonProperty("dueDate") String dueDate, @JsonProperty("score") JsonNode score) {
        this.title = title;
        this.subject = subject;
        this.dueDate = dueDate;
        this.score = score;
    }

    /**
     * Converts a given {@code Homework} into this class for Jackson use.
     */
    public JsonAdaptedHomework(Homework source) {
        title = source.getTitle().toString();
        subject = source.getSubject().name();
        dueDate = source.getDueDate().toString();
        score = source.getScore().map(modelScore -> TextNode.valueOf(modelScore.toString())).orElse(null);
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
        final Score modelScore = toModelScore(modelTitle);
        return new Homework(modelTitle, modelSubject, modelDueDate, modelScore);
    }

    /**
     * Returns the model score for the optional field {@code score}, or null if the field is missing or null.
     * A whole number, as saved before scores had a total, has no total to keep, so it is ignored with a warning
     * instead of making the whole data file invalid.
     *
     * @param modelTitle Title of the homework, used in the warning.
     * @throws IllegalValueException If {@code score} is neither a whole number nor a valid score in
     *         {@code MARKS_OBTAINED/TOTAL_MARKS} format.
     */
    private Score toModelScore(Title modelTitle) throws IllegalValueException {
        if (score == null || score.isNull()) {
            return null;
        }
        if (score.isIntegralNumber()) {
            logger.warning(String.format(OLD_FORMAT_SCORE_WARNING_FORMAT, score, modelTitle));
            return null;
        }
        if (!score.isTextual()) {
            throw new IllegalValueException(Score.MESSAGE_CONSTRAINTS);
        }
        try {
            return Score.parse(score.textValue());
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage());
        }
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
