package seedu.address.model.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ModifiableObservableListBase;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.testutil.StudentBuilder;

public class StudentDisplayListTest {
    private final Student first = new StudentBuilder().withName("First Student").build();
    private final Student second = new StudentBuilder().withName("Second Student").build();
    private final Student third = new StudentBuilder().withName("Third Student").build();
    private final ObservableList<Student> source = FXCollections.observableArrayList(first, second, third);
    private final StudentDisplayList display = new StudentDisplayList(source);

    @Test
    public void setOrder_preservesSourceAndTracksIndices() {
        display.setOrder(List.of(third, first, second));

        assertEquals(List.of(first, second, third), source);
        assertEquals(List.of(third, first, second), display);
        assertEquals(2, display.getSourceIndex(0));
        assertEquals(1, display.getViewIndex(0));
        display.resetOrder();
        assertEquals(source, display);
    }

    @Test
    public void setOrder_invalidPermutation_preservesPreviousOrder() {
        display.setOrder(List.of(second, third, first));

        assertThrows(IllegalArgumentException.class, () -> display.setOrder(List.of(first)));
        assertThrows(IllegalArgumentException.class, () -> display.setOrder(List.of(first, first, third)));
        assertThrows(IllegalArgumentException.class, () -> display.setOrder(List.of(first, second,
                new StudentBuilder().withName("Third Student").build())));
        assertEquals(List.of(second, third, first), display);
    }

    @Test
    public void sourceChanges_preserveSurvivingDisplayPositions() {
        display.setOrder(List.of(third, first, second));
        Student editedThird = third.withLessons(List.of());
        source.set(2, editedThird);
        assertSame(editedThird, display.get(0));
        source.remove(first);
        assertEquals(List.of(editedThird, second), display);
        source.add(first);
        assertEquals(List.of(editedThird, second, first), display);
        source.clear();
        assertEquals(List.of(), display);
    }

    @Test
    public void sourcePermutation_preservesDisplayOccurrences() {
        display.setOrder(List.of(third, first, second));
        source.sort(Comparator.comparing(student -> student.getName().toString(), Comparator.reverseOrder()));
        assertEquals(List.of(third, first, second), display);
    }

    @Test
    public void filteredSource_resetRestoresNewResultOrder() {
        FilteredList<Student> filtered = new FilteredList<>(source);
        StudentDisplayList filteredDisplay = new StudentDisplayList(filtered);
        filteredDisplay.setOrder(List.of(third, second, first));
        filtered.setPredicate(student -> student != second);
        filteredDisplay.resetOrder();
        assertEquals(List.of(first, third), filteredDisplay);
        filtered.setPredicate(student -> true);
        filteredDisplay.resetOrder();
        assertEquals(source, filteredDisplay);
    }

    @Test
    public void sourcePermutation_interiorRange_preservesOccurrencesAndIndexMappings() {
        Student fourth = new StudentBuilder().withName("Fourth Student").build();
        ControlledStudentList controlledSource = new ControlledStudentList(List.of(first, second, third, fourth));
        StudentDisplayList controlledDisplay = new StudentDisplayList(controlledSource);
        controlledDisplay.setOrder(List.of(third, first, fourth, second));

        controlledSource.permute(1, 3, new int[] {2, 1});

        assertEquals(List.of(first, third, second, fourth), controlledSource);
        assertEquals(List.of(third, first, fourth, second), controlledDisplay);
        assertEquals(List.of(1, 0, 3, 2), List.of(controlledDisplay.getSourceIndex(0),
                controlledDisplay.getSourceIndex(1), controlledDisplay.getSourceIndex(2),
                controlledDisplay.getSourceIndex(3)));
        assertEquals(List.of(1, 0, 3, 2), List.of(controlledDisplay.getViewIndex(0),
                controlledDisplay.getViewIndex(1), controlledDisplay.getViewIndex(2),
                controlledDisplay.getViewIndex(3)));
        assertSame(third, controlledDisplay.get(0));
    }

    @Test
    public void sourceUpdate_preservesOrderAndNotifiesObservers() {
        ControlledStudentList controlledSource = new ControlledStudentList(List.of(first, second, third));
        StudentDisplayList controlledDisplay = new StudentDisplayList(controlledSource);
        controlledDisplay.setOrder(List.of(third, first, second));
        AtomicInteger notifications = new AtomicInteger();
        controlledDisplay.addListener((ListChangeListener<Student>) change -> notifications.incrementAndGet());

        controlledSource.notifyUpdate(1);

        assertEquals(List.of(third, first, second), controlledDisplay);
        assertSame(second, controlledDisplay.get(2));
        assertEquals(1, controlledDisplay.getSourceIndex(2));
        assertEquals(2, controlledDisplay.getViewIndex(1));
        assertEquals(1, notifications.get());
    }

    @Test
    public void sourceUpdate_unreportedInsertion_failsInvariantBeforeNotifyingObservers() {
        ControlledStudentList controlledSource = new ControlledStudentList(List.of(first, second));
        StudentDisplayList controlledDisplay = new StudentDisplayList(controlledSource);
        AtomicInteger notifications = new AtomicInteger();
        controlledDisplay.addListener((ListChangeListener<Student>) change -> notifications.incrementAndGet());

        Runnable insertWithoutNotification = () -> controlledSource.addWithoutReportingInsertion(third);
        AssertionError failure = assertThrows(AssertionError.class, insertWithoutNotification::run);

        assertEquals("Every source occurrence must remain displayed", failure.getMessage());
        assertEquals(0, notifications.get());
    }

    @Test
    public void setOrder_equalAndRepeatedReferences_preservesEveryOccurrence() {
        Student equalFirst = new StudentBuilder().withName("First Student").build();
        source.setAll(first, equalFirst, first);
        display.setOrder(List.of(first, first, equalFirst));

        assertSame(first, display.get(0));
        assertSame(first, display.get(1));
        assertSame(equalFirst, display.get(2));
    }

    private static final class ControlledStudentList extends ModifiableObservableListBase<Student> {
        private final List<Student> students;

        private ControlledStudentList(List<Student> students) {
            this.students = new ArrayList<>(students);
        }

        @Override
        public Student get(int index) {
            return students.get(index);
        }

        @Override
        public int size() {
            return students.size();
        }

        @Override
        protected void doAdd(int index, Student student) {
            students.add(index, student);
        }

        @Override
        protected Student doSet(int index, Student student) {
            return students.set(index, student);
        }

        @Override
        protected Student doRemove(int index) {
            return students.remove(index);
        }

        private void permute(int from, int to, int[] permutation) {
            List<Student> previous = List.copyOf(students);
            for (int i = from; i < to; i++) {
                students.set(permutation[i - from], previous.get(i));
            }
            beginChange();
            nextPermutation(from, to, permutation);
            endChange();
        }

        private void notifyUpdate(int index) {
            beginChange();
            nextUpdate(index);
            endChange();
        }

        private void addWithoutReportingInsertion(Student student) {
            // Deliberately violate the source contract to verify the display's fail-fast invariant.
            students.add(student);
            notifyUpdate(0);
        }
    }
}
