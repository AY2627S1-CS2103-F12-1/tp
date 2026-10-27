package seedu.address.model.homework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class DueDateTest {
    /** Error for an empty due date. */
    public static final String MESSAGE_EMPTY = "The due date is empty. Enter it as YYYY-MM-DD or MM-DD, "
            + "e.g. 2026-10-30 or 10-30.";
    /** Error for a due date whose year is outside 2000 to 2099. */
    public static final String MESSAGE_YEAR_OUT_OF_RANGE = "The due date's year must be from 2000 to 2099.";

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
        assertNotRealDate("2026-0-5", TODAY); // month 0
        assertNotRealDate("2026-10-0", TODAY); // day 0
        assertNotRealDate("2026-2-30", TODAY); // February never has 30 days
        assertNotRealDate("2026-2-29", TODAY); // non-leap year
        assertWrongFormat("2026-010-05", TODAY); // three-digit month
        assertWrongFormat("2026-10-005", TODAY); // three-digit day
        assertWrongFormat("026-10-05", TODAY); // three-digit year
    }

    @Test
    public void parse_fullFormYearBoundaries() {
        assertYearOutOfRange("0000-01-01", TODAY); // year 0000
        assertYearOutOfRange("0001-01-01", TODAY); // smallest four-digit year
        assertYearOutOfRange("1999-12-31", TODAY); // day before the range
        assertParsed("2000-01-01", TODAY, LocalDate.of(2000, 1, 1)); // first day of the range
        assertParsed("2099-12-31", TODAY, LocalDate.of(2099, 12, 31)); // last day of the range
        assertYearOutOfRange("2100-01-01", TODAY); // day after the range
        assertYearOutOfRange("2206-10-30", TODAY); // typo year
        assertYearOutOfRange("9999-12-31", TODAY); // largest four-digit year
        assertWrongFormat("10000-01-01", TODAY); // five-digit year
    }

    @Test
    public void parse_fullFormMonthAndDayBoundaries() {
        assertNotRealDate("2026-00-10", TODAY); // month 0
        assertParsed("2026-01-31", TODAY, LocalDate.of(2026, 1, 31));
        assertParsed("2026-12-01", TODAY, LocalDate.of(2026, 12, 1));
        assertNotRealDate("2026-13-01", TODAY); // month 13
        assertNotRealDate("2026-01-00", TODAY); // day 0
        assertNotRealDate("2026-01-32", TODAY); // day 32
        assertParsed("2026-04-30", TODAY, LocalDate.of(2026, 4, 30));
        assertNotRealDate("2026-04-31", TODAY); // April has 30 days
        assertNotRealDate("2026-02-30", TODAY); // February never has 30 days
    }

    @Test
    public void parse_fullFormLeapDay() {
        assertParsed("2028-02-29", TODAY, LocalDate.of(2028, 2, 29)); // leap year
        assertParsed("2000-02-29", TODAY, LocalDate.of(2000, 2, 29)); // century divisible by 400
        assertNotRealDate("2026-02-29", TODAY); // non-leap year
        assertNotRealDate("2027-02-29", TODAY); // non-leap year

        // the year is checked before the calendar
        assertYearOutOfRange("1900-02-29", TODAY);
        assertYearOutOfRange("2100-02-29", TODAY);
    }

    @Test
    public void parse_emptyInput_throwsEmptyMessage() {
        assertThrows(IllegalArgumentException.class, MESSAGE_EMPTY, () -> DueDate.parse("", TODAY));
        assertThrows(IllegalArgumentException.class, MESSAGE_EMPTY, () -> DueDate.parse("   ", TODAY));
    }

    @Test
    public void parse_wrongFormat_throwsIllegalArgumentException() {
        assertWrongFormat("30-10-2026", TODAY); // DD-MM-YYYY
        assertWrongFormat(" 30-10-2026 ", TODAY); // surrounding whitespace is not shown
        assertWrongFormat("15/10/2026", TODAY); // DD/MM/YYYY
        assertWrongFormat("2026/10/15", TODAY); // wrong separator
        assertWrongFormat("26-10-05", TODAY); // two-digit year
        assertWrongFormat("2026-10", TODAY); // year and month only
        assertWrongFormat("10-5-", TODAY); // trailing separator
        assertWrongFormat("10--5", TODAY); // double separator
        assertWrongFormat("2026.2.5", TODAY); // wrong separator, single-digit month and day
        assertWrongFormat("20261015", TODAY); // no separators
        assertWrongFormat("+2026-10-15", TODAY); // signed year
        assertWrongFormat("2026-10-15T00:00", TODAY); // time included
        assertWrongFormat("2026- 10-15", TODAY); // internal space
        assertWrongFormat("2026-10 -15", TODAY); // internal space
        assertWrongFormat("tomorrow", TODAY); // words
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
        assertLeapDayNotWithinYear("02-29", TODAY, 2028);
        assertLeapDayNotWithinYear("2-29", LocalDate.of(2026, 10, 27), 2028);
        // 2027 is not a leap year, 2028 is
        assertParsed("02-29", LocalDate.of(2027, 3, 1), LocalDate.of(2028, 2, 29));
        // 2028-02-29 has not passed yet
        assertParsed("02-29", LocalDate.of(2028, 2, 1), LocalDate.of(2028, 2, 29));
        // 2028-02-29 is today
        assertParsed("2-29", LocalDate.of(2028, 2, 29), LocalDate.of(2028, 2, 29));
        // 2028-02-29 has passed and 2029 is not a leap year; the example is the next leap year after 2028
        assertLeapDayNotWithinYear("2-29", LocalDate.of(2028, 3, 1), 2032);
    }

    @Test
    public void parse_shortFormLeapDayAfterRange_throwsYearMessage() {
        // the next 29 February is in 2104 (2100 is not a leap year), after the last allowed year
        assertYearOutOfRange("2-29", LocalDate.of(2096, 3, 1));
        assertYearOutOfRange("2-29", LocalDate.of(2099, 3, 1));
    }

    @Test
    public void parse_shortFormInLastYear_rejectsYearAfter2099() {
        LocalDate lastDay = LocalDate.of(2099, 12, 31);

        assertParsed("12-31", lastDay, lastDay); // today -> year 2099
        assertYearOutOfRange("01-01", lastDay); // passed -> year 2100
        assertYearOutOfRange("2-28", LocalDate.of(2099, 3, 1)); // passed -> year 2100
    }

    @Test
    public void parse_shortFormInvalid_throwsIllegalArgumentException() {
        assertNotRealDate("13-01", TODAY); // month 13
        assertNotRealDate("00-10", TODAY); // month 0
        assertNotRealDate("10-32", TODAY); // day 32
        assertNotRealDate("02-30", TODAY); // February never has 30 days
        assertNotRealDate("2-30", TODAY); // single-digit month, February never has 30 days
        assertNotRealDate("13-1", TODAY); // month 13, single-digit day
        assertNotRealDate("0-5", TODAY); // single-digit month 0
        assertNotRealDate("10-0", TODAY); // single-digit day 0
        assertWrongFormat("010-05", TODAY); // three-digit month
        assertWrongFormat("10-005", TODAY); // three-digit day
        assertWrongFormat("10 -15", TODAY); // internal space
        assertWrongFormat("10- 5", TODAY); // internal space before single-digit day
    }

    @Test
    public void parseFullDate_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> DueDate.parseFullDate(null));
    }

    @Test
    public void parseFullDate_validFullForm_success() {
        assertEquals(LocalDate.of(2026, 10, 15), DueDate.parseFullDate("2026-10-15").getDate());
        assertEquals(LocalDate.of(2026, 2, 5), DueDate.parseFullDate(" 2026-2-5 ").getDate()); // padded, stripped
        assertEquals(LocalDate.of(2000, 1, 1), DueDate.parseFullDate("2000-01-01").getDate()); // smallest year
        assertEquals(LocalDate.of(2099, 12, 31), DueDate.parseFullDate("2099-12-31").getDate()); // largest year
        assertEquals(LocalDate.of(2028, 2, 29), DueDate.parseFullDate("2028-02-29").getDate()); // leap day
    }

    @Test
    public void parseFullDate_wrongFormatOrShortForm_throwsFullDateFormatMessage() {
        // the wording is not agreed in detail: it names the input and only the full YYYY-MM-DD format
        assertFullDateFormatError("10-15", IllegalArgumentException.class, () ->
                DueDate.parseFullDate("10-15")); // short form, whose year would be inferred
        assertFullDateFormatError("1-5", IllegalArgumentException.class, () ->
                DueDate.parseFullDate("1-5")); // single-digit short form
        assertFullDateFormatError("2026-010-05", IllegalArgumentException.class, () ->
                DueDate.parseFullDate("2026-010-05")); // three-digit month
        assertFullDateFormatError("15/10/2026", IllegalArgumentException.class, () ->
                DueDate.parseFullDate("15/10/2026")); // wrong separator
        assertThrows(IllegalArgumentException.class, () -> DueDate.parseFullDate("")); // empty
    }

    @Test
    public void parseFullDate_notRealDate_throwsNotRealDate() {
        assertThrows(IllegalArgumentException.class, notRealDateMessage("2026-02-30"), () ->
                DueDate.parseFullDate("2026-02-30"));
        assertThrows(IllegalArgumentException.class, notRealDateMessage("2027-02-29"), () ->
                DueDate.parseFullDate("2027-02-29")); // leap day in a non-leap year
        assertThrows(IllegalArgumentException.class, notRealDateMessage("2026-13-01"), () ->
                DueDate.parseFullDate("2026-13-01"));
    }

    @Test
    public void parseFullDate_yearOutOfRange_throwsYearMessage() {
        for (String rawDate : new String[] {"0000-01-01", "0001-01-01", "1999-12-31", "2100-01-01", "2206-10-30"}) {
            assertThrows(IllegalArgumentException.class, MESSAGE_YEAR_OUT_OF_RANGE, () ->
                    DueDate.parseFullDate(rawDate));
        }
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
        assertEquals("2000-01-01", DueDate.parse("2000-01-01", TODAY).toString());
        assertEquals("2026-10-05", DueDate.parse("10-5", TODAY).toString()); // padded month and day
        assertEquals("2026-02-05", DueDate.parse("2026-2-5", TODAY).toString());
    }

    /** Returns the error for {@code date} (stripped), which is in neither date format. */
    public static String wrongFormatMessage(String date) {
        return date + " is not in YYYY-MM-DD or MM-DD format, e.g. 2026-10-30 or 10-30.";
    }

    /** Returns the error for {@code date} (stripped), which has a date format but names no real calendar date. */
    public static String notRealDateMessage(String date) {
        return date + " is not a real calendar date.";
    }

    /** Returns the error for a short-form 29 February that does not occur in the next 12 months. */
    public static String leapDayMessage(int nextLeapYear) {
        return "29 February does not occur in the next 12 months. Enter the full date, e.g. " + nextLeapYear
                + "-02-29.";
    }

    /**
     * Asserts that {@code executable} throws {@code expectedType} for {@code rawDate}, a date in the data file that
     * is not in the full {@code YYYY-MM-DD} format. The exact wording is not agreed; the message must start with the
     * date and name only the full format.
     */
    public static void assertFullDateFormatError(String rawDate, Class<? extends Throwable> expectedType,
            Executable executable) {
        String message = Assertions.assertThrows(expectedType, executable).getMessage();
        assertTrue(message.startsWith(rawDate + " "), message);
        assertTrue(message.contains("YYYY-MM-DD"), message);
        assertFalse(message.contains("or MM-DD"), message);
    }

    private static void assertParsed(String rawDate, LocalDate today, LocalDate expectedDate) {
        assertEquals(expectedDate, DueDate.parse(rawDate, today).getDate());
    }

    private static void assertWrongFormat(String rawDate, LocalDate today) {
        assertThrows(IllegalArgumentException.class, wrongFormatMessage(rawDate.strip()), () ->
                DueDate.parse(rawDate, today));
    }

    private static void assertNotRealDate(String rawDate, LocalDate today) {
        assertThrows(IllegalArgumentException.class, notRealDateMessage(rawDate.strip()), () ->
                DueDate.parse(rawDate, today));
    }

    private static void assertYearOutOfRange(String rawDate, LocalDate today) {
        assertThrows(IllegalArgumentException.class, MESSAGE_YEAR_OUT_OF_RANGE, () -> DueDate.parse(rawDate, today));
    }

    private static void assertLeapDayNotWithinYear(String rawDate, LocalDate today, int nextLeapYear) {
        assertThrows(IllegalArgumentException.class, leapDayMessage(nextLeapYear), () ->
                DueDate.parse(rawDate, today));
    }
}
