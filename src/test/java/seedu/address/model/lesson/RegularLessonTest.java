package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import seedu.address.model.student.TuitionSubject;

public class RegularLessonTest {
    @Test
    public void constructor_invalidDurationOrPrecision_rejects() {
        for (String end : new String[]{"16:00", "15:00", "01:00"}) {
            assertThrows(IllegalArgumentException.class, () -> lesson("16:00", end));
        }
        assertThrows(IllegalArgumentException.class, () -> lesson("16:00:01", "18:00"));
        assertThrows(NullPointerException.class, () -> new RegularLesson(null, DayOfWeek.MONDAY,
                LocalTime.NOON, LocalTime.of(13, 0)));
    }

    @Test
    public void clashesWith_overlapContainmentAndAdjacency() {
        RegularLesson base = lesson("16:00", "18:00");
        for (RegularLesson overlapping : new RegularLesson[]{lesson("15:00", "17:00"),
            lesson("17:00", "19:00"), lesson("16:30", "17:00"), lesson("15:00", "19:00"), base}) {
            assertTrue(base.clashesWith(overlapping));
            assertTrue(overlapping.clashesWith(base));
        }
        assertFalse(base.clashesWith(lesson("18:00", "19:00")));
        assertFalse(base.clashesWith(lesson("15:00", "16:00")));
        assertFalse(base.clashesWith(new RegularLesson(TuitionSubject.MATH, DayOfWeek.TUESDAY,
                LocalTime.of(16, 0), LocalTime.of(18, 0))));
    }

    @Test
    public void equalsAndDisplay_normalizedValues() {
        RegularLesson base = lesson("16:00", "18:00");
        assertEquals(base, lesson("16:00", "18:00"));
        assertEquals(base.hashCode(), lesson("16:00", "18:00").hashCode());
        assertNotEquals(base, lesson("16:00", "19:00"));
        assertNotEquals(base, null);
        assertEquals("MATH; MONDAY; 16:00-18:00", base.toString());
        assertEquals(DayOfWeek.MONDAY, RegularLesson.parseDay(" monday "));
        assertEquals(LocalTime.MIDNIGHT, RegularLesson.parseTime("00:00", "invalid"));
    }

    private RegularLesson lesson(String start, String end) {
        return new RegularLesson(TuitionSubject.MATH, DayOfWeek.MONDAY, LocalTime.parse(start), LocalTime.parse(end));
    }
}
