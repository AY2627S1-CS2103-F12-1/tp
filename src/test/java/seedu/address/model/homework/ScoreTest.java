package seedu.address.model.homework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ScoreTest {
    @Test
    public void getValue() {
        assertEquals(85, new Score(85).getValue());
    }

    @Test
    public void equals() {
        Score score = new Score(85);

        // same object -> returns true
        assertTrue(score.equals(score));

        // same value -> returns true
        assertTrue(score.equals(new Score(85)));
        assertEquals(score.hashCode(), new Score(85).hashCode());

        // null -> returns false
        assertFalse(score.equals(null));

        // different type -> returns false
        assertFalse(score.equals(85));

        // different value -> returns false
        assertFalse(score.equals(new Score(86)));
    }

    @Test
    public void toStringMethod() {
        assertEquals("85", new Score(85).toString());
    }
}
