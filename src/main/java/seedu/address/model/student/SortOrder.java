package seedu.address.model.student;

import java.util.Comparator;

/**
 * Specifies the direction of a student sort without changing its tie-breaking rules.
 */
public enum SortOrder {
    ASCENDING("ascending"), DESCENDING("descending");

    private final String description;

    SortOrder(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns the comparator in this direction, leaving equal values equal.
     */
    public <T> Comparator<T> applyTo(Comparator<T> comparator) {
        return this == ASCENDING ? comparator : comparator.reversed();
    }
}
