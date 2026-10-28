package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class CommandHistoryTest {
    private final CommandHistory history = new CommandHistory();

    @Test
    public void add_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> history.add(null));
    }

    @Test
    public void previousAndNext_emptyHistory_returnEmpty() {
        assertEquals(Optional.empty(), history.previous());
        assertEquals(Optional.empty(), history.next());
    }

    @Test
    public void add_blankCommand_notStored() {
        history.add("");
        history.add("   ");
        assertEquals(Optional.empty(), history.previous());
    }

    @Test
    public void previous_singleEntry_staysAtEntry() {
        history.add("list");
        assertEquals(Optional.of("list"), history.previous());
        assertEquals(Optional.of("list"), history.previous());
    }

    @Test
    public void next_singleEntry_givesEmptyTextAfterEntry() {
        history.add("list");
        history.previous();
        assertEquals(Optional.of(""), history.next());
    }

    @Test
    public void previousAndNext_multipleEntries_stepThroughInOrder() {
        history.add("list");
        history.add("hw ls 2");
        history.add("hw ls 99");

        assertEquals(Optional.of("hw ls 99"), history.previous());
        assertEquals(Optional.of("hw ls 2"), history.previous());
        assertEquals(Optional.of("list"), history.previous());
        assertEquals(Optional.of("hw ls 2"), history.next());
        assertEquals(Optional.of("hw ls 99"), history.next());
    }

    @Test
    public void previous_atOldest_staysAtOldest() {
        history.add("list");
        history.add("hw ls 2");
        history.previous();
        history.previous();

        assertEquals(Optional.of("list"), history.previous());
        assertEquals(Optional.of("hw ls 2"), history.next());
    }

    @Test
    public void next_pastNewest_givesEmptyTextOnce() {
        history.add("list");
        history.add("hw ls 2");
        history.previous();

        assertEquals(Optional.of(""), history.next());
        // Already after the newest entry, so there is nothing further to show
        assertEquals(Optional.empty(), history.next());
        assertEquals(Optional.of("hw ls 2"), history.previous());
    }

    @Test
    public void next_noEntryShown_returnsEmpty() {
        history.add("list");
        assertEquals(Optional.empty(), history.next());
    }

    @Test
    public void add_sameAsNewest_storedOnce() {
        history.add("list");
        history.add("list");

        assertEquals(Optional.of("list"), history.previous());
        assertEquals(Optional.of("list"), history.previous());
        assertEquals(Optional.of(""), history.next());
    }

    @Test
    public void add_sameAsOlderEntry_storedAgain() {
        history.add("list");
        history.add("hw ls 2");
        history.add("list");

        assertEquals(Optional.of("list"), history.previous());
        assertEquals(Optional.of("hw ls 2"), history.previous());
        assertEquals(Optional.of("list"), history.previous());
    }

    @Test
    public void add_whileBrowsing_resetsToAfterNewest() {
        history.add("list");
        history.add("hw ls 2");
        history.previous();
        history.previous();

        history.add("hw ls 99");

        assertEquals(Optional.empty(), history.next());
        assertEquals(Optional.of("hw ls 99"), history.previous());
        assertEquals(Optional.of("hw ls 2"), history.previous());
    }

    @Test
    public void add_sameAsNewestWhileBrowsing_resetsToAfterNewest() {
        history.add("list");
        history.add("hw ls 2");
        history.previous();
        history.previous();

        history.add("hw ls 2");

        assertEquals(Optional.of("hw ls 2"), history.previous());
        assertEquals(Optional.of("list"), history.previous());
    }
}
