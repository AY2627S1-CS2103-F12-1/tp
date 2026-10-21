package seedu.address.model.student;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

/**
 * Keeps a transient permutation of the filtered roster, tracking each source occurrence separately.
 */
public final class StudentDisplayList extends TransformationList<Student, Student> {
    private List<Integer> sourceIndices;
    private List<Student> displayedStudents;

    /**
     * Creates a display in source order that follows later source changes.
     */
    public StudentDisplayList(ObservableList<Student> source) {
        super(requireNonNull(source));
        sourceIndices = IntStream.range(0, source.size()).boxed().toList();
        displayedStudents = List.copyOf(source);
    }

    @Override
    public Student get(int index) {
        return displayedStudents.get(index);
    }

    @Override
    public int size() {
        return displayedStudents.size();
    }

    @Override
    public int getSourceIndex(int index) {
        return sourceIndices.get(index);
    }

    @Override
    public int getViewIndex(int index) {
        return sourceIndices.indexOf(index);
    }

    /**
     * Reorders the same source occurrences, rejecting missing, added or substituted records before publication.
     */
    public void setOrder(List<Student> students) {
        requireNonNull(students);
        if (students.size() != size()) {
            throw new IllegalArgumentException("Display order must contain every displayed occurrence.");
        }
        List<Integer> indices = new ArrayList<>();
        for (Student student : students) {
            indices.add(findUnusedSourceIndex(student, indices));
        }
        publishOrder(indices);
    }

    /**
     * Restores source order after a search or filter establishes its own result set.
     */
    public void resetOrder() {
        publishOrder(IntStream.range(0, getSource().size()).boxed().toList());
    }

    @Override
    protected void sourceChanged(ListChangeListener.Change<? extends Student> change) {
        List<Integer> indices = new ArrayList<>(sourceIndices);
        while (change.next()) {
            if (change.wasPermutated()) {
                indices.replaceAll(index -> index >= change.getFrom() && index < change.getTo()
                        ? change.getPermutation(index) : index);
            } else if (!change.wasUpdated()) {
                indices = updateSourceIndices(indices, change);
            }
        }
        publishOrder(indices);
    }

    /**
     * Finds an occurrence by reference so equal records and repeated references cannot be collapsed.
     */
    private int findUnusedSourceIndex(Student student, List<Integer> usedIndices) {
        for (int i = 0; i < getSource().size(); i++) {
            if (getSource().get(i) == student && !usedIndices.contains(i)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Display order must contain the same student occurrences.");
    }

    /**
     * Preserves surviving positions on deletion or replacement and appends newly visible occurrences.
     */
    private static List<Integer> updateSourceIndices(List<Integer> indices,
            ListChangeListener.Change<? extends Student> change) {
        int from = change.getFrom();
        int removedSize = change.getRemovedSize();
        int addedSize = change.getAddedSize();
        List<Integer> updatedIndices = new ArrayList<>();
        for (int index : indices) {
            if (index < from) {
                updatedIndices.add(index);
            } else if (index >= from + removedSize) {
                updatedIndices.add(index + addedSize - removedSize);
            } else if (index - from < addedSize) {
                updatedIndices.add(index);
            }
        }
        for (int i = Math.min(removedSize, addedSize); i < addedSize; i++) {
            updatedIndices.add(from + i);
        }
        return updatedIndices;
    }

    /**
     * Builds the complete replacement before notifying observers of a display-only change.
     */
    private void publishOrder(List<Integer> indices) {
        List<Student> replacement = indices.stream().<Student>map(index -> getSource().get(index)).toList();
        List<Student> previousStudents = displayedStudents;
        sourceIndices = List.copyOf(indices);
        displayedStudents = replacement;
        assert sourceIndices.size() == getSource().size() : "Every source occurrence must remain displayed";
        beginChange();
        nextReplace(0, size(), previousStudents);
        endChange();
    }
}
