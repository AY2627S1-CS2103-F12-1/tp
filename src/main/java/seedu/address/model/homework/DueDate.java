package seedu.address.model.homework;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the date on which a homework record is due.
 * Guarantees: immutable; the date exists in the calendar and its year is from 0001 to 9999.
 */
public final class DueDate {
    public static final String MESSAGE_CONSTRAINTS = "Due date must be a valid calendar date in YYYY-MM-DD or "
            + "MM-DD format, where the month and day may have 1 or 2 digits.";

    private static final int MAX_YEAR = 9999;
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
     * does not exist in the year of {@code today}. An inferred year after 9999 is rejected.
     * In both forms the month and day may have 1 or 2 digits, so {@code 2026-2-5} is 2026-02-05.
     *
     * @param rawDate Date in {@code YYYY-MM-DD} or {@code MM-DD} format.
     * @param today Date used to infer the year of a short-form date.
     * @return The parsed due date.
     * @throws IllegalArgumentException If the input is in neither format, does not name a real calendar date, or
     *         needs a year after 9999.
     */
    public static DueDate parse(String rawDate, LocalDate today) {
        requireAllNonNull(rawDate, today);
        String trimmedDate = rawDate.strip();
        Matcher fullFormMatcher = FULL_FORM.matcher(trimmedDate);
        if (fullFormMatcher.matches()) {
            return new DueDate(parseFullForm(Integer.parseInt(fullFormMatcher.group(1)),
                    Integer.parseInt(fullFormMatcher.group(2)), Integer.parseInt(fullFormMatcher.group(3))));
        }
        Matcher shortFormMatcher = SHORT_FORM.matcher(trimmedDate);
        if (shortFormMatcher.matches()) {
            return new DueDate(parseShortForm(Integer.parseInt(shortFormMatcher.group(1)),
                    Integer.parseInt(shortFormMatcher.group(2)), today));
        }
        throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
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
     * Returns the date with the given year, month and day, rejecting year 0000 and non-existent dates.
     */
    private static LocalDate parseFullForm(int year, int month, int day) {
        if (year < 1) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS, exception);
        }
    }

    /**
     * Returns the date with the given month and day in the year of {@code today}, or in the next year if that
     * date is before {@code today} or does not exist in the year of {@code today}, rejecting a next year after 9999.
     */
    private static LocalDate parseShortForm(int month, int day, LocalDate today) {
        try {
            LocalDate dateThisYear = LocalDate.of(today.getYear(), month, day);
            if (!dateThisYear.isBefore(today)) {
                return dateThisYear;
            }
        } catch (DateTimeException exception) {
            // The date does not exist this year, so try next year below.
        }
        int nextYear = today.getYear() + 1;
        if (nextYear > MAX_YEAR) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        try {
            return LocalDate.of(nextYear, month, day);
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS, exception);
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
