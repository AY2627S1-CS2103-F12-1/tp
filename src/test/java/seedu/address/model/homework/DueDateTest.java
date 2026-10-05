package seedu.address.model.homework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class DueDateTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 5);

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> DueDate.parse(null, TODAY));
        assertThrows(NullPointerException.class, () -> DueDate.parse("2026-10-15", null));
    }

    @Test
    public void parse_fullForm_success() {
        assertParsed("2026-10-15", TODAY, LocalDate.of(2026, 10, 15));
        assertParsed("2020-01-01", TODAY, LocalDate.of(2020, 1, 1)); // past date
        assertParsed(" 2026-10-15 ", TODAY, LocalDate.of(2026, 10, 15)); // surrounding whitespace
    }

    @Test
    public void parse_fullFormSingleDigitMonthOrDay_padsMonthAndDay() {
        assertParsed("2026-2-5", TODAY, LocalDate.of(2026, 2, 5)); // single-digit month and day
        assertParsed("2026-1-1", TODAY, LocalDate.of(2026, 1, 1)); // smallest month and day
        assertParsed("2026-10-5", TODAY, LocalDate.of(2026, 10, 5)); // single-digit day
        assertParsed("2026-9-15", TODAY, LocalDate.of(2026, 9, 15)); // single-digit month
        assertParsed("2028-2-29", TODAY, LocalDate.of(2028, 2, 29)); // leap day
    }

    @Test
    public void parse_fullFormSingleDigitMonthOrDayInvalid_throwsIllegalArgumentException() {
        assertInvalidDate("2026-0-5", TODAY); // month 0
        assertInvalidDate("2026-10-0", TODAY); // day 0
        assertInvalidDate("2026-2-30", TODAY); // February never has 30 days
        assertInvalidDate("2026-2-29", TODAY); // non-leap year
        assertInvalidDate("2026-010-05", TODAY); // three-digit month
        assertInvalidDate("2026-10-005", TODAY); // three-digit day
        assertInvalidDate("026-10-05", TODAY); // three-digit year
    }

    @Test
    public void parse_fullFormYearBoundaries() {
        assertInvalidDate("0000-01-01", TODAY); // year 0000
        assertParsed("0001-01-01", TODAY, LocalDate.of(1, 1, 1));
        assertParsed("9999-12-31", TODAY, LocalDate.of(9999, 12, 31));
        assertInvalidDate("10000-01-01", TODAY); // five-digit year
    }

    @Test
    public void parse_fullFormMonthAndDayBoundaries() {
        assertInvalidDate("2026-00-10", TODAY); // month 0
        assertParsed("2026-01-31", TODAY, LocalDate.of(2026, 1, 31));
        assertParsed("2026-12-01", TODAY, LocalDate.of(2026, 12, 1));
        assertInvalidDate("2026-13-01", TODAY); // month 13
        assertInvalidDate("2026-01-00", TODAY); // day 0
        assertInvalidDate("2026-01-32", TODAY); // day 32
        assertParsed("2026-04-30", TODAY, LocalDate.of(2026, 4, 30));
        assertInvalidDate("2026-04-31", TODAY); // April has 30 days
        assertInvalidDate("2026-02-30", TODAY); // February never has 30 days
    }

    @Test
    public void parse_fullFormLeapDay() {
        assertParsed("2028-02-29", TODAY, LocalDate.of(2028, 2, 29)); // leap year
        assertParsed("2000-02-29", TODAY, LocalDate.of(2000, 2, 29)); // century divisible by 400
        assertInvalidDate("2026-02-29", TODAY); // non-leap year
        assertInvalidDate("1900-02-29", TODAY); // century not divisible by 400
    }

    @Test
    public void parse_wrongFormat_throwsIllegalArgumentException() {
        assertInvalidDate("", TODAY); // empty
        assertInvalidDate("   ", TODAY); // whitespace only
        assertInvalidDate("15/10/2026", TODAY); // DD/MM/YYYY
        assertInvalidDate("2026/10/15", TODAY); // wrong separator
        assertInvalidDate("26-10-05", TODAY); // two-digit year
        assertInvalidDate("2026-10", TODAY); // year and month only
        assertInvalidDate("10-5-", TODAY); // trailing separator
        assertInvalidDate("10--5", TODAY); // double separator
        assertInvalidDate("2026.2.5", TODAY); // wrong separator, single-digit month and day
        assertInvalidDate("20261015", TODAY); // no separators
        assertInvalidDate("+2026-10-15", TODAY); // signed year
        assertInvalidDate("2026-10-15T00:00", TODAY); // time included
        assertInvalidDate("2026- 10-15", TODAY); // internal space
        assertInvalidDate("2026-10 -15", TODAY); // internal space
        assertInvalidDate("tomorrow", TODAY); // words
    }

    @Test
    public void parse_shortFormAroundToday_infersYear() {
        assertParsed("10-04", TODAY, LocalDate.of(2027, 10, 4)); // day before today -> next year
        assertParsed("10-05", TODAY, LocalDate.of(2026, 10, 5)); // today -> this year
        assertParsed("10-06", TODAY, LocalDate.of(2026, 10, 6)); // day after today -> this year
        assertParsed("01-01", TODAY, LocalDate.of(2027, 1, 1));
        assertParsed("12-31", TODAY, LocalDate.of(2026, 12, 31));
        assertParsed(" 10-15 ", TODAY, LocalDate.of(2026, 10, 15)); // surrounding whitespace
    }

    @Test
    public void parse_shortFormSingleDigitMonthOrDay_padsAndInfersYear() {
        assertParsed("10-5", TODAY, LocalDate.of(2026, 10, 5)); // single-digit day, today -> this year
        assertParsed("10-4", TODAY, LocalDate.of(2027, 10, 4)); // single-digit day, before today -> next year
        assertParsed("10-6", TODAY, LocalDate.of(2026, 10, 6)); // single-digit day, after today -> this year
        assertParsed("9-9", TODAY, LocalDate.of(2027, 9, 9)); // single-digit month and day, passed -> next year
        assertParsed("1-15", TODAY, LocalDate.of(2027, 1, 15)); // single-digit month
        assertParsed("1-1", TODAY, LocalDate.of(2027, 1, 1)); // smallest month and day
        assertParsed(" 10-5 ", TODAY, LocalDate.of(2026, 10, 5)); // surrounding whitespace
        assertParsed("2-29", LocalDate.of(2027, 3, 1), LocalDate.of(2028, 2, 29)); // leap day next year
    }

    @Test
    public void parse_shortFormLeapDay() {
        // neither 2026 nor 2027 is a leap year
        assertInvalidDate("02-29", TODAY);
        // 2027 is not a leap year, 2028 is
        assertParsed("02-29", LocalDate.of(2027, 3, 1), LocalDate.of(2028, 2, 29));
        // 2028-02-29 has not passed yet
        assertParsed("02-29", LocalDate.of(2028, 2, 1), LocalDate.of(2028, 2, 29));
        // 2028-02-29 has passed and 2029 is not a leap year
        assertInvalidDate("02-29", LocalDate.of(2028, 3, 1));
    }

    @Test
    public void parse_shortFormInLastYear_rejectsYearAfter9999() {
        LocalDate lastDay = LocalDate.of(9999, 12, 31);

        assertParsed("12-31", lastDay, lastDay); // today -> year 9999
        assertInvalidDate("01-01", lastDay); // passed -> year 10000
    }

    @Test
    public void parse_shortFormInvalid_throwsIllegalArgumentException() {
        assertInvalidDate("13-01", TODAY); // month 13
        assertInvalidDate("00-10", TODAY); // month 0
        assertInvalidDate("10-32", TODAY); // day 32
        assertInvalidDate("02-30", TODAY); // February never has 30 days
        assertInvalidDate("2-30", TODAY); // single-digit month, February never has 30 days
        assertInvalidDate("13-1", TODAY); // month 13, single-digit day
        assertInvalidDate("0-5", TODAY); // single-digit month 0
        assertInvalidDate("10-0", TODAY); // single-digit day 0
        assertInvalidDate("010-05", TODAY); // three-digit month
        assertInvalidDate("10-005", TODAY); // three-digit day
        assertInvalidDate("10 -15", TODAY); // internal space
        assertInvalidDate("10- 5", TODAY); // internal space before single-digit day
    }

    @Test
    public void isShortForm() {
        assertTrue(DueDate.isShortForm("10-15"));
        assertTrue(DueDate.isShortForm(" 10-15 "));
        assertTrue(DueDate.isShortForm("10-5")); // single-digit day
        assertTrue(DueDate.isShortForm("1-5")); // single-digit month and day
        assertTrue(DueDate.isShortForm(" 9-15 ")); // single-digit month, surrounding whitespace
        assertFalse(DueDate.isShortForm("2026-10-15"));
        assertFalse(DueDate.isShortForm("2026-2-5")); // full form with single-digit month and day
        assertFalse(DueDate.isShortForm("010-05")); // three-digit month
        assertFalse(DueDate.isShortForm(""));
    }

    @Test
    public void messageConstraints_mentionsBothFormats() {
        assertEquals("Due date must be a valid calendar date in YYYY-MM-DD or MM-DD format, where the month and "
                + "day may have 1 or 2 digits.", DueDate.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void equals() {
        DueDate dueDate = DueDate.parse("2026-10-15", TODAY);

        // same object -> returns true
        assertTrue(dueDate.equals(dueDate));

        // same date, full and short form -> returns true
        assertTrue(dueDate.equals(DueDate.parse("10-15", TODAY)));
        assertEquals(dueDate.hashCode(), DueDate.parse("10-15", TODAY).hashCode());

        // same date, padded and single-digit forms -> returns true
        assertTrue(DueDate.parse("2026-02-05", TODAY).equals(DueDate.parse("2026-2-5", TODAY)));

        // null -> returns false
        assertFalse(dueDate.equals(null));

        // different type -> returns false
        assertFalse(dueDate.equals(LocalDate.of(2026, 10, 15)));

        // different date -> returns false
        assertFalse(dueDate.equals(DueDate.parse("2026-10-16", TODAY)));
    }

    @Test
    public void toStringMethod() {
        assertEquals("2026-10-15", DueDate.parse("10-15", TODAY).toString());
        assertEquals("0001-01-01", DueDate.parse("0001-01-01", TODAY).toString());
        assertEquals("2026-10-05", DueDate.parse("10-5", TODAY).toString()); // padded month and day
        assertEquals("2026-02-05", DueDate.parse("2026-2-5", TODAY).toString());
    }

    private static void assertParsed(String rawDate, LocalDate today, LocalDate expectedDate) {
        assertEquals(expectedDate, DueDate.parse(rawDate, today).getDate());
    }

    private static void assertInvalidDate(String rawDate, LocalDate today) {
        assertThrows(IllegalArgumentException.class, DueDate.MESSAGE_CONSTRAINTS, () -> DueDate.parse(rawDate, today));
    }
}
