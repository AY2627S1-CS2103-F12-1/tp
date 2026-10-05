package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedHomework.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalHomeworks.ATOMIC_STRUCTURE;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.homework.DueDate;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.homework.Title;
import seedu.address.model.student.TuitionSubject;
import seedu.address.testutil.HomeworkBuilder;

public class JsonAdaptedHomeworkTest {
    private static final String VALID_TITLE = "Complete algebra worksheet";
    private static final String VALID_SUBJECT = "MATH";
    private static final String VALID_DUE_DATE = "2026-10-15";
    private static final String VALID_STATUS = "ASSIGNED";

    @Test
    public void toModelType_validHomeworkDetails_returnsHomework() throws Exception {
        assertEquals(ALGEBRA, new JsonAdaptedHomework(ALGEBRA).toModelType());
    }

    @Test
    public void toModelType_completedHomeworkWithScore_returnsHomework() throws Exception {
        assertEquals(ATOMIC_STRUCTURE, new JsonAdaptedHomework(ATOMIC_STRUCTURE).toModelType());
    }

    @Test
    public void toModelType_lenientValues_returnsNormalizedHomework() throws Exception {
        // Subject and status ignore case, the title is normalized and the date is padded, as in commands
        JsonAdaptedHomework homework = new JsonAdaptedHomework("  Complete   algebra worksheet ", "math",
                "2026-10-15", "assigned", null);
        assertEquals(ALGEBRA, homework.toModelType());

        Homework paddedDate = new HomeworkBuilder().withDueDate("2026-02-05").build();
        assertEquals(paddedDate, new JsonAdaptedHomework(VALID_TITLE, VALID_SUBJECT, "2026-2-5", VALID_STATUS, null)
                .toModelType());
    }

    @Test
    public void toModelType_nullTitle_throwsIllegalValueException() {
        JsonAdaptedHomework homework = new JsonAdaptedHomework(null, VALID_SUBJECT, VALID_DUE_DATE, VALID_STATUS,
                null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "title");
        assertThrows(IllegalValueException.class, expectedMessage, homework::toModelType);
    }

    @Test
    public void toModelType_invalidTitle_throwsIllegalValueException() {
        // empty title
        JsonAdaptedHomework homework = new JsonAdaptedHomework("   ", VALID_SUBJECT, VALID_DUE_DATE, VALID_STATUS,
                null);
        assertThrows(IllegalValueException.class, Title.MESSAGE_CONSTRAINTS, homework::toModelType);

        // line break in title
        JsonAdaptedHomework homeworkWithLineBreak = new JsonAdaptedHomework("Line\nbreak", VALID_SUBJECT,
                VALID_DUE_DATE, VALID_STATUS, null);
        assertThrows(IllegalValueException.class, Title.MESSAGE_CONSTRAINTS, homeworkWithLineBreak::toModelType);
    }

    @Test
    public void toModelType_nullSubject_throwsIllegalValueException() {
        JsonAdaptedHomework homework = new JsonAdaptedHomework(VALID_TITLE, null, VALID_DUE_DATE, VALID_STATUS, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "subject");
        assertThrows(IllegalValueException.class, expectedMessage, homework::toModelType);
    }

    @Test
    public void toModelType_invalidSubject_throwsIllegalValueException() {
        JsonAdaptedHomework homework = new JsonAdaptedHomework(VALID_TITLE, "BIOLOGY", VALID_DUE_DATE, VALID_STATUS,
                null);
        assertThrows(IllegalValueException.class, TuitionSubject.MESSAGE_CONSTRAINTS, homework::toModelType);
    }

    @Test
    public void toModelType_nullDueDate_throwsIllegalValueException() {
        JsonAdaptedHomework homework = new JsonAdaptedHomework(VALID_TITLE, VALID_SUBJECT, null, VALID_STATUS, null);
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
    public void toModelType_nullStatus_throwsIllegalValueException() {
        JsonAdaptedHomework homework = new JsonAdaptedHomework(VALID_TITLE, VALID_SUBJECT, VALID_DUE_DATE, null,
                null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "status");
        assertThrows(IllegalValueException.class, expectedMessage, homework::toModelType);
    }

    @Test
    public void toModelType_invalidStatus_throwsIllegalValueException() {
        JsonAdaptedHomework homework = new JsonAdaptedHomework(VALID_TITLE, VALID_SUBJECT, VALID_DUE_DATE, "DONE",
                null);
        assertThrows(IllegalValueException.class, HomeworkStatus.MESSAGE_CONSTRAINTS, homework::toModelType);
    }

    @Test
    public void toModelType_nullScore_returnsHomeworkWithoutScore() throws Exception {
        JsonAdaptedHomework homework = new JsonAdaptedHomework(VALID_TITLE, VALID_SUBJECT, VALID_DUE_DATE,
                "COMPLETED", null);
        assertEquals(new HomeworkBuilder().withStatus(HomeworkStatus.COMPLETED).build(), homework.toModelType());
    }

    @Test
    public void toModelType_score_returnsHomeworkWithScore() throws Exception {
        JsonAdaptedHomework homework = new JsonAdaptedHomework(VALID_TITLE, VALID_SUBJECT, VALID_DUE_DATE,
                VALID_STATUS, 0);
        assertEquals(new HomeworkBuilder().withScore(0).build(), homework.toModelType());
    }

    private static void assertInvalidDueDate(String dueDate) {
        JsonAdaptedHomework homework = new JsonAdaptedHomework(VALID_TITLE, VALID_SUBJECT, dueDate, VALID_STATUS,
                null);
        assertThrows(IllegalValueException.class, DueDate.MESSAGE_FULL_DATE_CONSTRAINTS, homework::toModelType);
    }
}
