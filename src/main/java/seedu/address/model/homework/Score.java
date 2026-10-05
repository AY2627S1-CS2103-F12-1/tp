package seedu.address.model.homework;

/**
 * Represents the score a student received for a homework record.
 * Guarantees: immutable.
 * TODO: Add validation rules together with the record-score feature; any int is accepted for now.
 */
public final class Score {
    private final int value;

    /** Creates a score holding {@code value}. */
    public Score(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Score score && value == score.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }
}
