package seedu.address.model.student;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates a stable student ordering without changing the supplied records or list.
 */
public final class StudentSorter {
    private StudentSorter() {
    }

    /**
     * Returns every input occurrence in sorted order, retaining the input order of ties.
     */
    public static List<Student> sort(List<Student> students, SortKey key, SortOrder order) {
        requireAllNonNull(students, key, order);
        List<Student> sortedStudents = new ArrayList<>(students);
        sortedStudents.sort(key.getComparator(order));
        return List.copyOf(sortedStudents);
    }
}
