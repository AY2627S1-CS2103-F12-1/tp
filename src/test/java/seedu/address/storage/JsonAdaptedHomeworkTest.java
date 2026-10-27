package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.storage.JsonAdaptedHomework.INVALID_NUMBER_SCORE_WARNING_FORMAT;
import static seedu.address.storage.JsonAdaptedHomework.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalHomeworks.ATOMIC_STRUCTURE;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.TextNode;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.homework.DueDate;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.homework.Score;
import seedu.address.model.homework.Title;
import seedu.address.model.student.TuitionSubject;
import seedu.address.testutil.HomeworkBuilder;

public class JsonAdaptedHomeworkTest {
    private static final String VALID_TITLE = "Complete algebra worksheet";
    private static final String VALID_SUBJECT = "MATH";
    private static final String VALID_DUE_DATE = "2026-10-15";

    @Test
    public void toModelType_missingOrNullStatus_defaultsAssignedEvenWithScore() throws Exception {
        for (String statusField : List.of("", ",\"status\":null")) {
            Homework homework = JsonUtil.fromJsonString("{\"title\":\"Algebra\",\"subject\":\"MATH\","
                    + "\"dueDate\":\"2026-10-15\",\"score\":\"17/20\"" + statusField + "}",
                    JsonAdaptedHomework.class).toModelType();
            assertEquals(HomeworkStatus.ASSIGNED, homework.getStatus());
            assertEquals(new Score(17, 20), homework.getScore().orElseThrow());
        }
    }

    @Test
    public void toModelType_explicitStatusRoundTrip_preservesIndependentScore() throws Exception {
        Homework completed = new HomeworkBuilder().withStatus("COMPLETED").build();
        assertEquals(completed, new JsonAdaptedHomework(completed).toModelType());
        Homework assignedScored = new HomeworkBuilder().withScore("17/20").build();
        assertEquals(assignedScored, new JsonAdaptedHomework(assignedScored).toModelType());
        assertEquals(completed, new JsonAdaptedHomework(VALID_TITLE, VALID_SUBJECT, VALID_DUE_DATE,
                null, " completed ").toModelType());
    }

    @Test
    public void toModelType_invalidExplicitStatus_rejected() {
        for (String status : List.of("", "done", "true", "COMPLETED extra")) {
            JsonAdaptedHomework adapted = new JsonAdaptedHomework(VALID_TITLE, VALID_SUBJECT, VALID_DUE_DATE,
                    null, status);
            assertThrows(IllegalValueException.class, HomeworkStatus.MESSAGE_CONSTRAINTS, adapted::toModelType);
        }
    }

    @Test
    public void toModelType_validHomeworkDetails_returnsHomework() throws Exception {
        assertEquals(ALGEBRA, new JsonAdaptedHomework(ALGEBRA).toModelType());
    }

    @Test
    public void toModelType_homeworkWithScore_returnsHomework() throws Exception {
        assertEquals(ATOMIC_STRUCTURE, new JsonAdaptedHomework(ATOMIC_STRUCTURE).toModelType());
    }

    @Test
    public void toModelType_lenientValues_returnsNormalizedHomework() throws Exception {
        // The subject ignores case, the title is normalized and the date is padded, as in commands
        JsonAdaptedHomework homework = new JsonAdaptedHomework("  Complete   algebra worksheet ", "math",
                "2026-10-15", null);
        assertEquals(ALGEBRA, homework.toModelType());

        Homework paddedDate = new HomeworkBuilder().withDueDate("2026-02-05").build();
        assertEquals(paddedDate, new JsonAdaptedHomework(VALID_TITLE, VALID_SUBJECT, "2026-2-5", null)
                .toModelType());
    }

    @Test
    public void toModelType_nullTitle_throwsIllegalValueException() {
        JsonAdaptedHomework homework = new JsonAdaptedHomework(null, VALID_SUBJECT, VALID_DUE_DATE, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "title");
        assertThrows(IllegalValueException.class, expectedMessage, homework::toModelType);
    }

    @Test
    public void toModelType_invalidTitle_throwsIllegalValueException() {
        // empty title
        JsonAdaptedHomework homework = new JsonAdaptedHomework("   ", VALID_SUBJECT, VALID_DUE_DATE, null);
        assertThrows(IllegalValueException.class, Title.MESSAGE_CONSTRAINTS, homework::toModelType);

        // line break in title
        JsonAdaptedHomework homeworkWithLineBreak = new JsonAdaptedHomework("Line\nbreak", VALID_SUBJECT,
                VALID_DUE_DATE, null);
        assertThrows(IllegalValueException.class, Title.MESSAGE_CONSTRAINTS, homeworkWithLineBreak::toModelType);
    }

    @Test
    public void toModelType_nullSubject_throwsIllegalValueException() {
        JsonAdaptedHomework homework = new JsonAdaptedHomework(VALID_TITLE, null, VALID_DUE_DATE, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "subject");
        assertThrows(IllegalValueException.class, expectedMessage, homework::toModelType);
    }

    @Test
    public void toModelType_invalidSubject_throwsIllegalValueException() {
        JsonAdaptedHomework homework = new JsonAdaptedHomework(VALID_TITLE, "BIOLOGY", VALID_DUE_DATE, null);
        assertThrows(IllegalValueException.class, TuitionSubject.MESSAGE_CONSTRAINTS, homework::toModelType);
    }

    @Test
    public void toModelType_nullDueDate_throwsIllegalValueException() {
        JsonAdaptedHomework homework = new JsonAdaptedHomework(VALID_TITLE, VALID_SUBJECT, null, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "dueDate");
        assertThrows(IllegalValueException.class, expectedMessage, homework::toModelType);
    }

    @Test
    public void toModelType_invalidDueDate_throwsIllegalValueException() {
        // not a calendar date
        assertInvalidDueDate("2026-02-30");
        // short form, whose year is never inferred when loading
        assertInvalidDueDate("10-15");
        // wrong separator
        assertInvalidDueDate("15/10/2026");
    }

    @Test
    public void toModelType_nullScore_returnsHomeworkWithoutScore() throws Exception {
        JsonAdaptedHomework homework = new JsonAdaptedHomework(VALID_TITLE, VALID_SUBJECT, VALID_DUE_DATE, null);
        assertEquals(new HomeworkBuilder().build(), homework.toModelType());
    }

    @Test
    public void toModelType_oldStatusKeyInJson_preservesCompletion() throws Exception {
        assertEquals(new HomeworkBuilder().withStatus("COMPLETED").build(), JsonUtil.fromJsonString("{\"title\": \""
                + VALID_TITLE
                + "\", \"subject\": \"MATH\", \"dueDate\": \"2026-10-15\", \"status\": \"COMPLETED\"}",
                JsonAdaptedHomework.class).toModelType());
    }

    @Test
    public void toModelType_score_returnsHomeworkWithScore() throws Exception {
        JsonAdaptedHomework homework = new JsonAdaptedHomework(VALID_TITLE, VALID_SUBJECT, VALID_DUE_DATE,
                TextNode.valueOf("0/10"));
        assertEquals(new HomeworkBuilder().withScore("0/10").build(), homework.toModelType());
    }

    @Test
    public void toModelType_bareScore_returnsHomeworkWithBareScore() throws Exception {
        JsonAdaptedHomework homework = new JsonAdaptedHomework(VALID_TITLE, VALID_SUBJECT, VALID_DUE_DATE,
                TextNode.valueOf("17"));
        assertEquals(new HomeworkBuilder().withScore("17").build(), homework.toModelType());
    }

    @Test
    public void toJson_bareScore_savedAsText() throws Exception {
        String json = JsonUtil.toJsonString(new JsonAdaptedHomework(new HomeworkBuilder().withScore("85").build()));
        assertTrue(json.contains("\"score\" : \"85\""), json);
    }

    @Test
    public void toModelType_validScoreInJson_returnsHomeworkWithScore() throws Exception {
        assertEquals(new HomeworkBuilder().withScore("5/10").build(), fromJsonWithScore("\"5/10\"").toModelType());
        assertEquals(new HomeworkBuilder().withScore("17/20").build(), fromJsonWithScore("\"17/20\"").toModelType());
        assertEquals(new HomeworkBuilder().withScore("85").build(), fromJsonWithScore("\"85\"").toModelType());
        assertEquals(new HomeworkBuilder().withScore("0").build(), fromJsonWithScore("\"0\"").toModelType());
        assertEquals(new HomeworkBuilder().withScore("2147483647/2147483647").build(),
                fromJsonWithScore("\"2147483647/2147483647\"").toModelType());

        // surrounding whitespace is ignored, as in commands
        assertEquals(new HomeworkBuilder().withScore("5/10").build(),
                fromJsonWithScore("\" 5/10 \"").toModelType());
    }

    @Test
    public void toModelType_nullOrMissingScoreInJson_returnsHomeworkWithoutScore() throws Exception {
        assertEquals(new HomeworkBuilder().build(), fromJsonWithScore("null").toModelType());
        assertEquals(new HomeworkBuilder().build(),
                JsonUtil.fromJsonString(toJson(""), JsonAdaptedHomework.class).toModelType());
    }

    @Test
    public void toModelType_numberScoreInJson_returnsHomeworkWithBareScore() throws Exception {
        // whole numbers, as saved before scores were text, load without a warning
        assertNumberScoreLoaded("85", new HomeworkBuilder().withScore("85").build());
        assertNumberScoreLoaded("0", new HomeworkBuilder().withScore("0").build());
        assertNumberScoreLoaded("2147483647", new HomeworkBuilder().withScore("2147483647").build());
    }

    @Test
    public void toModelType_invalidNumberScoreInJson_returnsHomeworkWithoutScore() throws Exception {
        assertInvalidNumberScoreIgnored("85.5"); // decimal
        assertInvalidNumberScoreIgnored("85.0"); // decimal point
        assertInvalidNumberScoreIgnored("-3"); // negative
        assertInvalidNumberScoreIgnored("2147483648"); // one above the largest int
        assertInvalidNumberScoreIgnored("3000000000"); // beyond the int range
        assertInvalidNumberScoreIgnored("99999999999999999999"); // beyond the long range
    }

    @Test
    public void toModelType_invalidScoreInJson_throwsIllegalValueException() throws Exception {
        assertInvalidScore("\"85.5\""); // decimal written as text
        assertInvalidScore("\"-3\""); // negative written as text
        assertInvalidScore("\"017\""); // leading zero
        assertInvalidScore("\"\""); // empty text
        assertInvalidScore("\"5.5/10\""); // decimal marks
        assertInvalidScore("\"11/10\""); // marks above the total
        assertInvalidScore("\"5/0\""); // zero total
        assertInvalidScore("\"5 / 10\""); // spaces inside
        assertInvalidScore("\"abc\""); // non-numeric text
        assertInvalidScore("true"); // boolean
        assertInvalidScore("[\"5/10\"]"); // list
        assertThrows(IllegalValueException.class, Score.MESSAGE_TOO_LARGE, fromJsonWithScore(
                "\"2147483648/2147483648\"")::toModelType); // larger than Integer.MAX_VALUE
        assertThrows(IllegalValueException.class, Score.MESSAGE_TOO_LARGE, fromJsonWithScore(
                "\"2147483648\"")::toModelType); // bare score larger than Integer.MAX_VALUE
    }

    private static JsonAdaptedHomework fromJsonWithScore(String score) throws Exception {
        return JsonUtil.fromJsonString(toJson(", \"score\": " + score), JsonAdaptedHomework.class);
    }

    /**
     * Returns the JSON of a homework with the valid title, subject and due date, followed by {@code extraFields}.
     */
    private static String toJson(String extraFields) {
        return "{\"title\": \"" + VALID_TITLE + "\", \"subject\": \"" + VALID_SUBJECT + "\", \"dueDate\": \""
                + VALID_DUE_DATE + "\"" + extraFields + "}";
    }

    /**
     * Asserts that the JSON {@code score} loads into {@code expectedHomework} without any warning.
     */
    private static void assertNumberScoreLoaded(String score, Homework expectedHomework) throws Exception {
        List<LogRecord> records = new ArrayList<>();
        assertEquals(expectedHomework, toModelTypeCapturingLogs(score, records));
        assertEquals(List.of(), records);
    }

    /**
     * Asserts that the JSON {@code score} is ignored when loading, with one warning naming the score and the title.
     */
    private static void assertInvalidNumberScoreIgnored(String score) throws Exception {
        List<LogRecord> records = new ArrayList<>();
        assertEquals(new HomeworkBuilder().build(), toModelTypeCapturingLogs(score, records));
        assertEquals(1, records.size());
        assertEquals(Level.WARNING, records.get(0).getLevel());
        assertEquals(String.format(INVALID_NUMBER_SCORE_WARNING_FORMAT, score, VALID_TITLE),
                records.get(0).getMessage());
    }

    /**
     * Returns the homework loaded from JSON with the given {@code score}, adding every message logged by
     * {@code JsonAdaptedHomework} meanwhile to {@code records}.
     */
    private static Homework toModelTypeCapturingLogs(String score, List<LogRecord> records) throws Exception {
        Logger logger = LogsCenter.getLogger(JsonAdaptedHomework.class);
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord logRecord) {
                records.add(logRecord);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        logger.addHandler(handler);
        try {
            return fromJsonWithScore(score).toModelType();
        } finally {
            logger.removeHandler(handler);
        }
    }

    private static void assertInvalidScore(String score) throws Exception {
        assertThrows(IllegalValueException.class, Score.MESSAGE_CONSTRAINTS, fromJsonWithScore(score)::toModelType);
    }

    private static void assertInvalidDueDate(String dueDate) {
        JsonAdaptedHomework homework = new JsonAdaptedHomework(VALID_TITLE, VALID_SUBJECT, dueDate, null);
        assertThrows(IllegalValueException.class, DueDate.MESSAGE_FULL_DATE_CONSTRAINTS, homework::toModelType);
    }
}
