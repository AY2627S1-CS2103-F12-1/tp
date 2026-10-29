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
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.homework.Score;
import seedu.address.model.homework.Title;
import seedu.address.model.student.TuitionSubject;

/**
 * Jackson-friendly version of {@link Homework}.
 */
class JsonAdaptedHomework {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Homework's %s field is missing!";
    public static final String INVALID_NUMBER_SCORE_WARNING_FORMAT =
            "Ignored invalid homework score %s for '%s'";

    private static final Logger logger = LogsCenter.getLogger(JsonAdaptedHomework.class);

    private final String title;
    private final String subject;
    private final String dueDate;
    private final JsonNode score;
    private final String status;

    /**
     * Constructs a {@code JsonAdaptedHomework} with the given homework details, as read from the JSON file.
     *
     * @param title Title of the homework, or null if the field is missing.
     * @param subject Subject of the homework, such as {@code MATH}, or null if the field is missing.
     * @param dueDate Due date in {@code YYYY-MM-DD} format, or null if the field is missing.
     * @param score Score as text in {@code MARKS} or {@code MARKS_OBTAINED/TOTAL_MARKS} format, a number as saved
     *         before scores were text, or null if none is recorded.
     */
    public JsonAdaptedHomework(String title, String subject, String dueDate, JsonNode score) {
        this(title, subject, dueDate, score, null);
    }

    /**
     * Reads explicit completion state, defaulting missing or null legacy status to ASSIGNED.
     * Scores never determine the default status.
     */
    @JsonCreator
    public JsonAdaptedHomework(@JsonProperty("title") String title, @JsonProperty("subject") String subject,
            @JsonProperty("dueDate") String dueDate, @JsonProperty("score") JsonNode score,
            @JsonProperty("status") String status) {
        this.title = title;
        this.subject = subject;
        this.dueDate = dueDate;
        this.score = score;
        this.status = status;
    }

    /**
     * Converts a given {@code Homework} into this class for Jackson use.
     */
    public JsonAdaptedHomework(Homework source) {
        title = source.getTitle().toString();
        subject = source.getSubject().name();
        dueDate = source.getDueDate().toString();
        score = source.getScore().map(modelScore -> TextNode.valueOf(modelScore.toString())).orElse(null);
        status = source.getStatus().name();
    }

    /** Returns the title of the homework, as written in the data file. */
    String getTitle() {
        return title;
    }

    /**
     * Converts this Jackson-friendly adapted homework object into the model's {@code Homework} object.
     * The due date must be a full {@code YYYY-MM-DD} date; its year is never inferred.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted homework.
     */
    public Homework toModelType() throws IllegalValueException {
        final Title modelTitle = toModelTitle();
        final TuitionSubject modelSubject = toModelValue("subject", subject, TuitionSubject::parse,
                TuitionSubject.MESSAGE_CONSTRAINTS);
        final DueDate modelDueDate = toModelDueDate();
        final Score modelScore = toModelScore(modelTitle);
        final HomeworkStatus modelStatus = status == null ? HomeworkStatus.ASSIGNED
                : toModelValue("status", status, HomeworkStatus::parse, HomeworkStatus.MESSAGE_CONSTRAINTS);
        return new Homework(modelTitle, modelSubject, modelDueDate, modelScore, modelStatus);
    }

    /**
     * Returns the model title parsed from the required field {@code title}. An invalid title keeps the message of
     * the rule it breaks, e.g. its length, as the homework commands give it. An empty title gets
     * {@link Title#MESSAGE_CONSTRAINTS}.
     *
     * @throws IllegalValueException If the field is missing or its value is invalid.
     */
    private Title toModelTitle() throws IllegalValueException {
        if (title == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "title"));
        }
        try {
            return new Title(title);
        } catch (IllegalArgumentException exception) {
            // "Enter 1 to 100 characters" in Title.MESSAGE_EMPTY is advice for the commands, not for the file
            String message = exception.getMessage().equals(Title.MESSAGE_EMPTY)
                    ? Title.MESSAGE_CONSTRAINTS
                    : exception.getMessage();
            throw new IllegalValueException(message);
        }
    }

    /**
     * Returns the model due date parsed from the required field {@code dueDate}. Unlike the other fields, an invalid
     * value keeps the parser's message, which says whether the format, the calendar date or the year is wrong.
     *
     * @throws IllegalValueException If the field is missing or its value is invalid.
     */
    private DueDate toModelDueDate() throws IllegalValueException {
        if (dueDate == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "dueDate"));
        }
        try {
            return DueDate.parseFullDate(dueDate);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage());
        }
    }

    /**
     * Returns the model score for the optional field {@code score}, or null if the field is missing or null.
     * A JSON number, as saved before scores were text, is read like the same digits written as text, e.g. {@code 85}
     * as the score {@code 85}. A number that is not a valid score, such as {@code 85.5}, is ignored with a warning
     * instead of making the whole data file invalid.
     *
     * @param modelTitle Title of the homework, used in the warning.
     * @throws IllegalValueException If {@code score} is neither a number nor a valid score in {@code MARKS} or
     *         {@code MARKS_OBTAINED/TOTAL_MARKS} format. A value that is neither text nor a number, e.g.
     *         {@code true}, gets {@link Score#MESSAGE_INVALID_FORMAT}, as text in neither format does.
     */
    private Score toModelScore(Title modelTitle) throws IllegalValueException {
        if (score == null || score.isNull()) {
            return null;
        }
        if (score.isNumber()) {
            try {
                return Score.parse(score.asText());
            } catch (IllegalArgumentException exception) {
                logger.warning(String.format(INVALID_NUMBER_SCORE_WARNING_FORMAT, score, modelTitle));
                return null;
            }
        }
        if (!score.isTextual()) {
            throw new IllegalValueException(Score.MESSAGE_INVALID_FORMAT);
        }
        try {
            return Score.parse(score.textValue());
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage());
        }
    }

    /**
     * Returns the model value parsed from the required homework field {@code value}.
     *
     * @throws IllegalValueException If the field is missing or its value is invalid.
     */
    private static <T> T toModelValue(String fieldName, String value, Function<String, T> parser,
            String messageConstraints) throws IllegalValueException {
        return toModelValue(MISSING_FIELD_MESSAGE_FORMAT, fieldName, value, parser, messageConstraints);
    }

    /**
     * Returns the model value parsed from the required field {@code value}. Used by the homework and student
     * adapters, each with its own message for a missing field.
     *
     * @param missingFieldMessageFormat Message used when the field is missing, with {@code %s} for its name.
     * @param fieldName JSON name of the field, used in the message when the field is missing.
     * @param value Value of the field, or null if the field is missing.
     * @param parser Parser that throws an {@code IllegalArgumentException} if {@code value} is invalid.
     * @param messageConstraints Message used when {@code value} is invalid.
     * @throws IllegalValueException If the field is missing or its value is invalid.
     */
    static <T> T toModelValue(String missingFieldMessageFormat, String fieldName, String value,
            Function<String, T> parser, String messageConstraints) throws IllegalValueException {
        if (value == null) {
            throw new IllegalValueException(String.format(missingFieldMessageFormat, fieldName));
        }
        try {
            return parser.apply(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(messageConstraints);
        }
    }

}
