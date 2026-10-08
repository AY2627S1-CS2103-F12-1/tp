package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.TuitionSubject;

/** Stores the subject, weekday and minute-precision times of a weekly lesson as readable JSON. */
class JsonAdaptedLesson {
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

    /** Returns the validated lesson, rejecting missing fields or invalid values. */
    public RegularLesson toModelType() throws IllegalValueException {
        if (subject == null || dayOfWeek == null || startTime == null || endTime == null) {
            throw new IllegalValueException("Lesson subject, dayOfWeek, startTime and endTime are required.");
        }
        try {
            return new RegularLesson(TuitionSubject.parse(subject), RegularLesson.parseDay(dayOfWeek),
                    RegularLesson.parseTime(startTime, RegularLesson.MESSAGE_START),
                    RegularLesson.parseTime(endTime, RegularLesson.MESSAGE_END));
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage());
        }
    }
}
