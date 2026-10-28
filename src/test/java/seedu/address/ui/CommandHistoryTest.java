package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class CommandHistoryTest {
    private final CommandHistory history = new CommandHistory();

    @Test
    public void previous_emptyHistory_returnsEmpty() {
        assertEquals(Optional.empty(), history.previous());
    }

    @Test
    public void next_emptyHistory_returnsEmpty() {
        assertEquals(Optional.empty(), history.next());
    }

    @Test
    public void add_blankCommands_areNotStored() {
        history.add("");
        history.add(" \t ");

        assertEquals(Optional.empty(), history.previous());
    }

    @Test
    public void add_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> history.add(null));
    }

    @Test
    public void previousAndNext_multipleCommands_navigateAndStayWithinBounds() {
        history.add("list");
        history.add("search Alice");
        history.add("filter l/S4");

        assertEquals(Optional.of("filter l/S4"), history.previous());
        assertEquals(Optional.of("search Alice"), history.previous());
        assertEquals(Optional.of("list"), history.previous());
        assertEquals(Optional.of("list"), history.previous());
        assertEquals(Optional.of("search Alice"), history.next());
        assertEquals(Optional.of("filter l/S4"), history.next());
        assertEquals(Optional.of(""), history.next());
        assertEquals(Optional.empty(), history.next());
    }

    @Test
    public void add_consecutiveDuplicate_isStoredOnlyOnce() {
        history.add("list");
        history.add("list");

        assertEquals(Optional.of("list"), history.previous());
        assertEquals(Optional.of("list"), history.previous());
        assertEquals(Optional.of(""), history.next());
    }

    @Test
    public void add_nonConsecutiveDuplicate_isStoredAgain() {
        history.add("list");
        history.add("search Alice");
        history.add("list");

        assertEquals(Optional.of("list"), history.previous());
        assertEquals(Optional.of("search Alice"), history.previous());
        assertEquals(Optional.of("list"), history.previous());
    }

    @Test
    public void add_whileBrowsing_resetsPositionToNewest() {
        history.add("list");
        history.add("search Alice");
        history.previous();
        history.previous();

        history.add("filter l/S4");

        assertEquals(Optional.of("filter l/S4"), history.previous());
        assertEquals(Optional.of("search Alice"), history.previous());
    }
}
