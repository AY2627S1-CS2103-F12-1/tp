package seedu.address.model.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
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
    public void setOrder_equalAndRepeatedReferences_preservesEveryOccurrence() {
        Student equalFirst = new StudentBuilder().withName("First Student").build();
        source.setAll(first, equalFirst, first);
        display.setOrder(List.of(first, first, equalFirst));

        assertSame(first, display.get(0));
        assertSame(first, display.get(1));
        assertSame(equalFirst, display.get(2));
    }
}
