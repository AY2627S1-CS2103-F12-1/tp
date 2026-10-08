package seedu.address.model.lesson;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

import seedu.address.model.student.TuitionSubject;

/** Represents an immutable weekly lesson with a positive duration within one day. */
public final class RegularLesson {
    public static final String MESSAGE_DAY = "Day must be one of: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY,"
            + " SATURDAY, SUNDAY.";
    public static final String MESSAGE_START = "Start time must be a valid time in HH:mm 24-hour format.";
    public static final String MESSAGE_END = "End time must be a valid time in HH:mm 24-hour format.";
    public static final String MESSAGE_TIME_ORDER = "End time must be later than start time.";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final TuitionSubject subject;
    private final DayOfWeek dayOfWeek;
    private final LocalTime startTime;
    private final LocalTime endTime;

    /** Creates a weekly lesson, rejecting nonpositive durations and times with seconds. */
    public RegularLesson(TuitionSubject subject, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        requireAllNonNull(subject, dayOfWeek, startTime, endTime);
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(MESSAGE_TIME_ORDER);
        }
        if (startTime.getSecond() != 0 || startTime.getNano() != 0
                || endTime.getSecond() != 0 || endTime.getNano() != 0) {
            throw new IllegalArgumentException("Lesson times must have minute precision.");
        }
        this.subject = subject;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public TuitionSubject getSubject() {
        return subject;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    /** Returns true if the lessons overlap; adjacent time slots do not overlap. */
    public boolean clashesWith(RegularLesson other) {
        return dayOfWeek == other.dayOfWeek && startTime.isBefore(other.endTime) && endTime.isAfter(other.startTime);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RegularLesson lesson && subject == lesson.subject && dayOfWeek == lesson.dayOfWeek
                && startTime.equals(lesson.startTime) && endTime.equals(lesson.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, dayOfWeek, startTime, endTime);
    }

    @Override
    public String toString() {
        return subject + "; " + dayOfWeek + "; " + startTime.format(TIME_FORMAT) + "-" + endTime.format(TIME_FORMAT);
    }

    /** Returns a full weekday name, ignoring case and surrounding whitespace. */
    public static DayOfWeek parseDay(String value) {
        try {
            return DayOfWeek.valueOf(value.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(MESSAGE_DAY, exception);
        }
    }

    /** Returns a minute-precision 24-hour time or throws the supplied validation message. */
    public static LocalTime parseTime(String value, String message) {
        if (!value.strip().matches("(?:[01][0-9]|2[0-3]):[0-5][0-9]")) {
            throw new IllegalArgumentException(message);
        }
        return LocalTime.parse(value.strip());
    }
}
