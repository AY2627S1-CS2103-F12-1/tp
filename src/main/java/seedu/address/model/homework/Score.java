package seedu.address.model.homework;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the marks a student obtained for a homework record, either on their own, e.g. {@code 17}, or out of the
 * total marks, e.g. {@code 5/10}.
 * Guarantees: immutable; the marks obtained are not negative; a total, if any, is positive and the marks obtained are
 * at most the total. The score is kept as given, so {@code 5/10} is not simplified to {@code 1/2}, and {@code 17} is
 * a different score from {@code 17/20}.
 */
public final class Score {
    public static final String MESSAGE_CONSTRAINTS = "Score must be a whole number, or MARKS_OBTAINED/TOTAL_MARKS "
            + "with whole numbers, a positive total, and marks obtained between zero and the total.";
    public static final String MESSAGE_INVALID_FORMAT = "Score must be whole marks such as 17, or marks out of a "
            + "total such as 5/10, with no signs, decimals, spaces or other characters.";
    public static final String MESSAGE_MARKS_ABOVE_TOTAL =
            "Marks obtained (%d) must not be more than the total marks (%d).";
    public static final String MESSAGE_TOTAL_TOO_SMALL = "Total marks must be at least 1.";
    public static final String MESSAGE_TOO_LARGE =
            "Marks are too large. Enter whole numbers no greater than " + Integer.MAX_VALUE + ".";
    public static final String MESSAGE_LEADING_ZEROES =
            "Marks must not have leading zeroes. Enter whole numbers such as 5/10, not 05/10.";

    /**
     * A whole number without a sign, decimals or leading zeroes (except {@code 0}), optionally followed by a single
     * slash and a second such number.
     */
    private static final Pattern SCORE_FORMAT = Pattern.compile("(0|[1-9][0-9]*)(?:/(0|[1-9][0-9]*))?");

    /** The same format as {@link #SCORE_FORMAT}, but with leading zeroes allowed. */
    private static final Pattern SCORE_FORMAT_WITH_LEADING_ZEROES = Pattern.compile("[0-9]+(?:/[0-9]+)?");

    private final int marksObtained;
    private final Integer totalMarks;

    /**
     * Creates a score of {@code marksObtained} without a total.
     *
     * @throws IllegalArgumentException If the marks obtained are negative.
     */
    public Score(int marksObtained) {
        if (marksObtained < 0) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        this.marksObtained = marksObtained;
        this.totalMarks = null;
    }

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
     * Returns the score represented by the input, either {@code MARKS} or {@code MARKS_OBTAINED/TOTAL_MARKS}, ignoring
     * surrounding whitespace.
     *
     * @throws IllegalArgumentException With {@link #MESSAGE_LEADING_ZEROES} if a number has leading zeroes but the
     *         input is otherwise in one of the formats, with {@link #MESSAGE_INVALID_FORMAT} if the input is in
     *         neither format, with {@link #MESSAGE_TOO_LARGE} if a number in the right format is larger than
     *         {@link Integer#MAX_VALUE}, with {@link #MESSAGE_TOTAL_TOO_SMALL} if the total is 0, or with
     *         {@link #MESSAGE_MARKS_ABOVE_TOTAL} if the marks obtained are more than the total.
     */
    public static Score parse(String rawScore) {
        requireNonNull(rawScore);
        String strippedScore = rawScore.strip();
        Matcher matcher = SCORE_FORMAT.matcher(strippedScore);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(SCORE_FORMAT_WITH_LEADING_ZEROES.matcher(strippedScore).matches()
                    ? MESSAGE_LEADING_ZEROES
                    : MESSAGE_INVALID_FORMAT);
        }
        int marksObtained = parseMarks(matcher.group(1));
        if (matcher.group(2) == null) {
            return new Score(marksObtained);
        }
        int totalMarks = parseMarks(matcher.group(2));
        if (totalMarks < 1) {
            throw new IllegalArgumentException(MESSAGE_TOTAL_TOO_SMALL);
        }
        if (marksObtained > totalMarks) {
            // Locale.ROOT prints the marks in ASCII digits, as the user typed them, in every locale
            throw new IllegalArgumentException(String.format(Locale.ROOT, MESSAGE_MARKS_ABOVE_TOTAL, marksObtained,
                    totalMarks));
        }
        return new Score(marksObtained, totalMarks);
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

    /**
     * Returns the total marks, or an empty {@code Optional} if the score has no total.
     */
    public Optional<Integer> getTotalMarks() {
        return Optional.ofNullable(totalMarks);
    }

    @Override
    public String toString() {
        return totalMarks == null ? String.valueOf(marksObtained) : marksObtained + "/" + totalMarks;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Score score
                && marksObtained == score.marksObtained
                && Objects.equals(totalMarks, score.totalMarks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(marksObtained, totalMarks);
    }
}
