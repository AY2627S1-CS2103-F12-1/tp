package seedu.address.model.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.StudentBuilder;
import seedu.address.testutil.TypicalHomeworks;

public class StudentRosterTest {
    @Test
    public void addStudent_updatesObservableListInInsertionOrder() {
        StudentRoster roster = new StudentRoster();
        Student first = new StudentBuilder().build();
        Student second = new StudentBuilder().withName("Jane Tan").build();

        roster.addStudent(first);
        roster.addStudent(second);

        assertEquals(2, roster.getStudents().size());
        assertEquals(first, roster.getStudents().get(0));
        assertEquals(second, roster.getStudents().get(1));
        assertThrows(UnsupportedOperationException.class, () -> roster.getStudents().add(first));
    }

    @Test
    public void addStudent_sameIdentity_rejectsWithoutChangingRoster() {
        StudentRoster roster = new StudentRoster();
        Student first = new StudentBuilder().build();
        Student duplicate = new StudentBuilder().withName(" john   tan ").withSubjects("PHYSICS").build();

        roster.addStudent(first);

        assertTrue(roster.hasStudent(duplicate));
        assertThrows(IllegalArgumentException.class, () -> roster.addStudent(duplicate));
        assertEquals(1, roster.getStudents().size());
        assertEquals(first, roster.getStudents().get(0));
    }

    @Test
    public void addStudent_differentPhone_allowsSameName() {
        StudentRoster roster = new StudentRoster();
        Student first = new StudentBuilder().build();
        Student differentPhone = new StudentBuilder().withPhone("81234567").build();

        roster.addStudent(first);

        assertFalse(roster.hasStudent(differentPhone));
        roster.addStudent(differentPhone);
        assertEquals(2, roster.getStudents().size());
    }

    @Test
    public void setStudent_existingTarget_replacesAtSamePosition() {
        StudentRoster roster = new StudentRoster();
        Student first = new StudentBuilder().build();
        Student second = new StudentBuilder().withName("Jane Tan").build();
        Student third = new StudentBuilder().withName("Ali Tan").build();
        roster.addStudent(first);
        roster.addStudent(second);
        roster.addStudent(third);
        Student editedSecond = second.withHomeworks(TypicalHomeworks.getTypicalHomeworks());

        roster.setStudent(second, editedSecond);

        assertEquals(3, roster.getStudents().size());
        assertEquals(first, roster.getStudents().get(0));
        assertEquals(editedSecond, roster.getStudents().get(1));
        assertEquals(third, roster.getStudents().get(2));
    }

    @Test
    public void setStudent_editedWithNewUniqueIdentity_replaces() {
        StudentRoster roster = new StudentRoster();
        Student first = new StudentBuilder().build();
        Student renamed = new StudentBuilder().withName("Jane Tan").build();
        roster.addStudent(first);

        roster.setStudent(first, renamed);

        assertEquals(1, roster.getStudents().size());
        assertEquals(renamed, roster.getStudents().get(0));
    }

    @Test
    public void setStudent_targetNotInRoster_rejectsWithoutChangingRoster() {
        StudentRoster roster = new StudentRoster();
        Student first = new StudentBuilder().build();
        Student missing = new StudentBuilder().withName("Jane Tan").build();
        roster.addStudent(first);

        assertThrows(IllegalArgumentException.class, () -> roster.setStudent(missing, missing));
        assertEquals(1, roster.getStudents().size());
        assertEquals(first, roster.getStudents().get(0));
    }

    @Test
    public void setStudent_editedDuplicatesAnotherStudent_rejectsWithoutChangingRoster() {
        StudentRoster roster = new StudentRoster();
        Student first = new StudentBuilder().build();
        Student second = new StudentBuilder().withName("Jane Tan").build();
        roster.addStudent(first);
        roster.addStudent(second);
        Student duplicateOfFirst = new StudentBuilder().withName("JOHN TAN").withSubjects("PHYSICS").build();

        assertThrows(IllegalArgumentException.class, () -> roster.setStudent(second, duplicateOfFirst));
        assertEquals(first, roster.getStudents().get(0));
        assertEquals(second, roster.getStudents().get(1));
    }
}
