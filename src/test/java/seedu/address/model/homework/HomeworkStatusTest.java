package seedu.address.model.homework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class HomeworkStatusTest {
    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> HomeworkStatus.parse(null));
    }

    @Test
    public void parse_validStatus_success() {
        assertEquals(HomeworkStatus.ASSIGNED, HomeworkStatus.parse("ASSIGNED"));
        assertEquals(HomeworkStatus.COMPLETED, HomeworkStatus.parse("COMPLETED"));
        assertEquals(HomeworkStatus.COMPLETED, HomeworkStatus.parse("completed")); // case ignored
        assertEquals(HomeworkStatus.ASSIGNED, HomeworkStatus.parse("  Assigned  ")); // whitespace ignored
    }

    @Test
    public void parse_invalidStatus_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, HomeworkStatus.MESSAGE_CONSTRAINTS, () ->
                HomeworkStatus.parse("")); // empty
        assertThrows(IllegalArgumentException.class, HomeworkStatus.MESSAGE_CONSTRAINTS, () ->
                HomeworkStatus.parse("DONE")); // unsupported status
        assertThrows(IllegalArgumentException.class, HomeworkStatus.MESSAGE_CONSTRAINTS, () ->
                HomeworkStatus.parse("ASSIGNED COMPLETED")); // two statuses
    }
}
