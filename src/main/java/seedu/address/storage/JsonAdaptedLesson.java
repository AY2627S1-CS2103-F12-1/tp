package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.TuitionSubject;

/** Stores the subject, weekday and minute-precision times of a weekly lesson as readable JSON. */
class JsonAdaptedLesson {
    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Lesson's %s field is missing!";

    private final String subject;
    private final String dayOfWeek;
    private final String startTime;
    private final String endTime;

    /** Creates an adapter from JSON fields. */
    @JsonCreator
    public JsonAdaptedLesson(@JsonProperty("subject") String subject, @JsonProperty("dayOfWeek") String dayOfWeek,
            @JsonProperty("startTime") String startTime, @JsonProperty("endTime") String endTime) {
        this.subject = subject;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /** Creates an adapter for a validated lesson. */
    public JsonAdaptedLesson(RegularLesson lesson) {
        this(lesson.getSubject().name(), lesson.getDayOfWeek().name(), lesson.getStartTime().toString(),
                lesson.getEndTime().toString());
    }

    /**
     * Returns the weekday and times of the lesson, as written in the data file, e.g. "MONDAY 17:00-18:30", or null if
     * any of them is missing.
     */
    String getTimeSlot() {
        if (dayOfWeek == null || startTime == null || endTime == null) {
            return null;
        }
        return dayOfWeek.strip() + " " + startTime.strip() + "-" + endTime.strip();
    }

    /** Returns the validated lesson, rejecting missing fields or invalid values. */
    public RegularLesson toModelType() throws IllegalValueException {
        requireField("subject", subject);
        requireField("dayOfWeek", dayOfWeek);
        requireField("startTime", startTime);
        requireField("endTime", endTime);
        try {
            return new RegularLesson(TuitionSubject.parse(subject), RegularLesson.parseDay(dayOfWeek),
                    RegularLesson.parseTime(startTime, RegularLesson.MESSAGE_START),
                    RegularLesson.parseTime(endTime, RegularLesson.MESSAGE_END));
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage());
        }
    }

    /**
     * Checks that the field {@code fieldName} is in the data file.
     *
     * @throws IllegalValueException If {@code value} is null, naming the missing field.
     */
    private static void requireField(String fieldName, String value) throws IllegalValueException {
        if (value == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, fieldName));
        }
    }
}
