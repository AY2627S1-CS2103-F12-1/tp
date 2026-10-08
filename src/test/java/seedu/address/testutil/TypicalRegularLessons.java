package seedu.address.testutil;

import java.time.DayOfWeek;
import java.time.LocalTime;

import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.TuitionSubject;

/**
 * Contains regular lessons used across tests.
 */
public final class TypicalRegularLessons {
    public static final RegularLesson MONDAY_MATH = new RegularLesson(TuitionSubject.MATH, DayOfWeek.MONDAY,
            LocalTime.of(17, 0), LocalTime.of(18, 30));
    public static final RegularLesson TUESDAY_PHYSICS = new RegularLesson(TuitionSubject.PHYSICS, DayOfWeek.TUESDAY,
            LocalTime.of(19, 0), LocalTime.of(20, 30));
    public static final RegularLesson SATURDAY_CHEMISTRY = new RegularLesson(TuitionSubject.CHEMISTRY,
            DayOfWeek.SATURDAY, LocalTime.of(15, 0), LocalTime.of(16, 30));

    private TypicalRegularLessons() {
    }
}
