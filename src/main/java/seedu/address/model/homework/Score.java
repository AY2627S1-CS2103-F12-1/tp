package seedu.address.model.homework;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the marks a student obtained for a homework record out of its total marks, e.g. {@code 5/10}.
 * Guarantees: immutable; the total is positive and the marks obtained are from zero to the total. The ratio is kept
 * as given, so {@code 5/10} is not simplified to {@code 1/2}.
 */
public final class Score {
    public static final String MESSAGE_CONSTRAINTS = "Score must use MARKS_OBTAINED/TOTAL_MARKS format with whole "
            + "numbers, a positive total, and marks obtained between zero and the total.";
    public static final String MESSAGE_TOO_LARGE =
            "Marks are too large. Enter whole numbers no greater than 2147483647.";

    /** Whole numbers without signs, decimals or leading zeroes (except {@code 0}), separated by a single slash. */
    private static final Pattern SCORE_FORMAT = Pattern.compile("(0|[1-9][0-9]*)/(0|[1-9][0-9]*)");

    private final int marksObtained;
    private final int totalMarks;

    /**
     * Creates a score of {@code marksObtained} out of {@code totalMarks}.
     *
     * @throws IllegalArgumentException If the total is not positive or the marks obtained are not from zero to the
     *         total.
     */
    public Score(int marksObtained, int totalMarks) {
        if (!isValidScore(marksObtained, totalMarks)) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        this.marksObtained = marksObtained;
        this.totalMarks = totalMarks;
    }

    /**
     * Returns the score represented by the input in {@code MARKS_OBTAINED/TOTAL_MARKS} format, ignoring surrounding
     * whitespace.
     *
     * @throws IllegalArgumentException With {@link #MESSAGE_CONSTRAINTS} if the input is not in that format or
     *         breaks the rules of {@link #Score(int, int)}, or with {@link #MESSAGE_TOO_LARGE} if a number in the
     *         right format is larger than {@link Integer#MAX_VALUE}.
     */
    public static Score parse(String rawScore) {
        requireNonNull(rawScore);
        Matcher matcher = SCORE_FORMAT.matcher(rawScore.strip());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        return new Score(parseMarks(matcher.group(1)), parseMarks(matcher.group(2)));
    }

    /**
     * Returns true if {@code totalMarks} is positive and {@code marksObtained} is from zero to {@code totalMarks}.
     */
    public static boolean isValidScore(int marksObtained, int totalMarks) {
        return totalMarks > 0 && marksObtained >= 0 && marksObtained <= totalMarks;
    }

    /**
     * Returns the value of {@code digits}, a whole number without a sign.
     *
     * @throws IllegalArgumentException If the number is larger than {@link Integer#MAX_VALUE}.
     */
    private static int parseMarks(String digits) {
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(MESSAGE_TOO_LARGE, exception);
        }
    }

    public int getMarksObtained() {
        return marksObtained;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    @Override
    public String toString() {
        return marksObtained + "/" + totalMarks;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Score score
                && marksObtained == score.marksObtained
                && totalMarks == score.totalMarks;
    }

    @Override
    public int hashCode() {
        return Objects.hash(marksObtained, totalMarks);
    }
}
