package seedu.address.model.homework;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the date on which a homework record is due.
 * Guarantees: immutable; the date exists in the calendar and its year is from 2000 to 2099.
 */
public final class DueDate {
    public static final String MESSAGE_CONSTRAINTS = "Due date must be a valid calendar date in YYYY-MM-DD or "
            + "MM-DD format, where the month and day may have 1 or 2 digits.";
    public static final String MESSAGE_EMPTY = "The due date is empty. Enter it as YYYY-MM-DD or MM-DD, "
            + "e.g. 2026-10-30 or 10-30.";
    public static final String MESSAGE_WRONG_FORMAT = "%s is not in YYYY-MM-DD or MM-DD format, "
            + "e.g. 2026-10-30 or 10-30.";
    public static final String MESSAGE_FULL_DATE_EMPTY = "The due date is empty. Enter it as YYYY-MM-DD, "
            + "e.g. 2026-10-30.";
    public static final String MESSAGE_FULL_DATE_WRONG_FORMAT = "%s is not in YYYY-MM-DD format, e.g. 2026-10-30.";
    public static final String MESSAGE_NOT_REAL_DATE = "%s is not a real calendar date.";
    public static final String MESSAGE_LEAP_DAY_NOT_WITHIN_YEAR = "29 February does not occur in the next 12 "
            + "months. Enter the full date, e.g. %d-02-29.";
    // Concatenation rather than String.format, which would use the default locale's digits for the years. The
    // class name is needed to use the year limits, which are declared below with the other private constants.
    public static final String MESSAGE_YEAR_OUT_OF_RANGE = "The due date's year must be from " + DueDate.MIN_YEAR
            + " to " + DueDate.MAX_YEAR + ".";

    /** Earliest year a due date may have, which catches typo years such as {@code 0026}. */
    private static final int MIN_YEAR = 2000;
    /** Latest year a due date may have, which catches typo years such as {@code 2206}. */
    private static final int MAX_YEAR = 2099;
    private static final MonthDay LEAP_DAY = MonthDay.of(2, 29);
    private static final Pattern FULL_FORM = Pattern.compile("([0-9]{4})-([0-9]{1,2})-([0-9]{1,2})");
    private static final Pattern SHORT_FORM = Pattern.compile("([0-9]{1,2})-([0-9]{1,2})");

    private final LocalDate value;

    private DueDate(LocalDate value) {
        this.value = value;
    }

    /**
     * Returns the due date represented by the input, ignoring surrounding whitespace.
     * The input is either a full {@code YYYY-MM-DD} date, which may be in the past, or a short {@code MM-DD} date
     * whose year is inferred: the year of {@code today}, or the next year if that date is before {@code today} or
     * does not exist in the year of {@code today}. In both forms the month and day may have 1 or 2 digits, so
     * {@code 2026-2-5} is 2026-02-05. The year, typed or inferred, must be from 2000 to 2099.
     *
     * @param rawDate Date in {@code YYYY-MM-DD} or {@code MM-DD} format.
     * @param today Date used to infer the year of a short-form date.
     * @return The parsed due date.
     * @throws IllegalArgumentException If the input is empty, is in neither format, has a year outside 2000 to
     *         2099 or does not name a real calendar date, with a message saying which.
     */
    public static DueDate parse(String rawDate, LocalDate today) {
        requireAllNonNull(rawDate, today);
        String trimmedDate = rawDate.strip();
        if (trimmedDate.isEmpty()) {
            throw new IllegalArgumentException(MESSAGE_EMPTY);
        }
        Matcher fullFormMatcher = FULL_FORM.matcher(trimmedDate);
        if (fullFormMatcher.matches()) {
            return new DueDate(parseFullForm(trimmedDate, fullFormMatcher));
        }
        Matcher shortFormMatcher = SHORT_FORM.matcher(trimmedDate);
        if (shortFormMatcher.matches()) {
            return new DueDate(parseShortForm(trimmedDate, Integer.parseInt(shortFormMatcher.group(1)),
                    Integer.parseInt(shortFormMatcher.group(2)), today));
        }
        throw new IllegalArgumentException(String.format(MESSAGE_WRONG_FORMAT, trimmedDate));
    }

    /**
     * Returns the due date represented by a full {@code YYYY-MM-DD} input, ignoring surrounding whitespace.
     * The month and day may have 1 or 2 digits. Unlike {@link #parse(String, LocalDate)}, the short {@code MM-DD}
     * form is rejected, so the year is never inferred.
     *
     * @throws IllegalArgumentException If the input is empty, is not in {@code YYYY-MM-DD} format, has a year
     *         outside 2000 to 2099 or does not name a real calendar date, with a message saying which.
     */
    public static DueDate parseFullDate(String rawDate) {
        requireNonNull(rawDate);
        String trimmedDate = rawDate.strip();
        if (trimmedDate.isEmpty()) {
            throw new IllegalArgumentException(MESSAGE_FULL_DATE_EMPTY);
        }
        Matcher fullFormMatcher = FULL_FORM.matcher(trimmedDate);
        if (!fullFormMatcher.matches()) {
            throw new IllegalArgumentException(String.format(MESSAGE_FULL_DATE_WRONG_FORMAT, trimmedDate));
        }
        return new DueDate(parseFullForm(trimmedDate, fullFormMatcher));
    }

    /**
     * Returns true if the input, ignoring surrounding whitespace, has the short {@code MM-DD} form whose year
     * {@link #parse(String, LocalDate)} infers.
     */
    public static boolean isShortForm(String rawDate) {
        requireNonNull(rawDate);
        return SHORT_FORM.matcher(rawDate.strip()).matches();
    }

    /**
     * Returns the date matched by {@code fullFormMatcher}, checking the year range before the calendar.
     *
     * @param trimmedDate Input as typed, without surrounding whitespace, shown in the error message.
     * @param fullFormMatcher Matcher of the full form that has matched {@code trimmedDate}.
     */
    private static LocalDate parseFullForm(String trimmedDate, Matcher fullFormMatcher) {
        int year = Integer.parseInt(fullFormMatcher.group(1));
        checkYearInRange(year);
        try {
            return LocalDate.of(year, Integer.parseInt(fullFormMatcher.group(2)),
                    Integer.parseInt(fullFormMatcher.group(3)));
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException(String.format(MESSAGE_NOT_REAL_DATE, trimmedDate), exception);
        }
    }

    /**
     * Returns the date with the given month and day in the year of {@code today}, or in the next year if that
     * date is before {@code today} or does not exist in the year of {@code today}.
     * A 29 February that exists in neither year names the next leap year in its message.
     *
     * @param trimmedDate Input as typed, without surrounding whitespace, shown in the error message.
     */
    private static LocalDate parseShortForm(String trimmedDate, int month, int day, LocalDate today) {
        MonthDay monthDay;
        try {
            monthDay = MonthDay.of(month, day);
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException(String.format(MESSAGE_NOT_REAL_DATE, trimmedDate), exception);
        }
        int thisYear = today.getYear();
        if (monthDay.isValidYear(thisYear) && !monthDay.atYear(thisYear).isBefore(today)) {
            checkYearInRange(thisYear);
            return monthDay.atYear(thisYear);
        }
        int nextYear = thisYear + 1;
        if (monthDay.isValidYear(nextYear)) {
            checkYearInRange(nextYear);
            return monthDay.atYear(nextYear);
        }
        assert monthDay.equals(LEAP_DAY);
        int nextLeapYear = nextYear + 1;
        while (!Year.isLeap(nextLeapYear)) {
            nextLeapYear++;
        }
        checkYearInRange(nextLeapYear);
        // Locale.ROOT prints the year in ASCII digits, so the example can be typed back in, in every locale
        throw new IllegalArgumentException(String.format(Locale.ROOT, MESSAGE_LEAP_DAY_NOT_WITHIN_YEAR, nextLeapYear));
    }

    /**
     * Throws an {@code IllegalArgumentException} if {@code year} is outside {@code MIN_YEAR} to {@code MAX_YEAR}.
     */
    private static void checkYearInRange(int year) {
        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new IllegalArgumentException(MESSAGE_YEAR_OUT_OF_RANGE);
        }
    }

    public LocalDate getDate() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DueDate dueDate && value.equals(dueDate.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
