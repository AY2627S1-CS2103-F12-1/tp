package seedu.address.model.homework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.HomeworkBuilder;

public class HomeworkStatusTest {
    @Test
    public void parse_caseAndWhitespaceIgnored_invalidValuesRejected() {
        assertEquals(HomeworkStatus.COMPLETED, HomeworkStatus.parse(" completed "));
        assertEquals(HomeworkStatus.ASSIGNED, HomeworkStatus.parse("assigned"));
        for (String invalid : new String[]{"", "done", "true", "COMPLETED extra"}) {
            assertThrows(IllegalArgumentException.class, () -> HomeworkStatus.parse(invalid));
        }
        assertThrows(NullPointerException.class, () -> HomeworkStatus.parse(null));
    }

    @Test
    public void homework_statusIndependentOfScoreAndIdentity_butIncludedInEquality() {
        Homework assigned = new HomeworkBuilder().withScore("17/20").build();
        Homework completed = new HomeworkBuilder().withScore("17/20").withStatus("COMPLETED").build();
        assertEquals(HomeworkStatus.ASSIGNED, assigned.getStatus());
        assertEquals(completed, assigned.withStatus(HomeworkStatus.COMPLETED));
        assertTrue(assigned.isSameHomework(completed));
        assertFalse(assigned.equals(completed));
        assertEquals(completed.hashCode(), new HomeworkBuilder(completed).build().hashCode());
        assertEquals(HomeworkStatus.COMPLETED, new HomeworkBuilder(completed).withoutScore().build().getStatus());
        assertThrows(NullPointerException.class, () -> assigned.withStatus(null));
        assertEquals(HomeworkStatus.ASSIGNED, new HomeworkBuilder().build().getStatus());
    }
}
