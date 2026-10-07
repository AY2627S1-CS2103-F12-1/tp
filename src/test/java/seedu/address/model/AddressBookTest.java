package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.BENSON;
import static seedu.address.testutil.TypicalStudents.getTypicalAddressBook;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.student.Student;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.StudentBuilder;

public class AddressBookTest {

    private final AddressBook addressBook = new AddressBook();

    @Test
    public void constructor() {
        assertEquals(List.of(), addressBook.getStudentList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyAddressBook_replacesData() {
        AddressBook newData = getTypicalAddressBook();
        addressBook.resetData(newData);
        assertEquals(newData, addressBook);
    }

    @Test
    public void resetData_withItself_keepsData() {
        AddressBook typicalAddressBook = getTypicalAddressBook();
        typicalAddressBook.resetData(typicalAddressBook);
        assertEquals(getTypicalAddressBook(), typicalAddressBook);
    }

    @Test
    public void resetData_withDuplicateStudents_throwsIllegalArgumentException() {
        // Two students with the same identity: same name ignoring case and same phone
        Student editedAlice = new StudentBuilder().withName("ALICE TAN").withPhone("91111111").build();
        AddressBookStub newData = new AddressBookStub(List.of(ALICE, editedAlice));

        assertThrows(IllegalArgumentException.class, () -> addressBook.resetData(newData));
    }

    @Test
    public void hasStudent_nullStudent_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasStudent(null));
    }

    @Test
    public void hasStudent_studentNotInAddressBook_returnsFalse() {
        assertFalse(addressBook.hasStudent(ALICE));
    }

    @Test
    public void hasStudent_studentWithSameIdentityInAddressBook_returnsTrue() {
        addressBook.addStudent(ALICE);
        Student editedAlice = new StudentBuilder().withName("alice tan").withPhone("91111111")
                .withGuardianName("Other Guardian").build();
        assertTrue(addressBook.hasStudent(editedAlice));
    }

    @Test
    public void addStudent_duplicateStudent_throwsIllegalArgumentException() {
        addressBook.addStudent(ALICE);
        assertThrows(IllegalArgumentException.class, () -> addressBook.addStudent(ALICE));
    }

    @Test
    public void setStudent_studentInAddressBook_replacesStudent() {
        addressBook.addStudent(BENSON);
        Student editedBenson = BENSON.withHomeworks(List.of(ALGEBRA));

        addressBook.setStudent(BENSON, editedBenson);

        assertEquals(List.of(editedBenson), addressBook.getStudentList());
    }

    @Test
    public void getStudentList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getStudentList().remove(0));
    }

    @Test
    public void equals() {
        AddressBook aliceAddressBook = new AddressBookBuilder().withStudent(ALICE).build();

        // same object -> returns true
        assertTrue(aliceAddressBook.equals(aliceAddressBook));

        // same students -> returns true
        assertTrue(aliceAddressBook.equals(new AddressBookBuilder().withStudent(ALICE).build()));

        // null -> returns false
        assertFalse(aliceAddressBook.equals(null));

        // different type -> returns false
        assertFalse(aliceAddressBook.equals(5));

        // different students -> returns false
        assertFalse(aliceAddressBook.equals(new AddressBookBuilder().withStudent(BENSON).build()));

        // same students in a different order -> returns false
        assertNotEquals(new AddressBookBuilder().withStudent(ALICE).withStudent(BENSON).build(),
                new AddressBookBuilder().withStudent(BENSON).withStudent(ALICE).build());

        // different homework of the same student -> returns false
        assertFalse(new AddressBookBuilder().withStudent(BENSON).build().equals(
                new AddressBookBuilder().withStudent(BENSON.withHomeworks(List.of(ALGEBRA))).build()));
    }

    @Test
    public void toStringMethod() {
        String expected = AddressBook.class.getCanonicalName() + "{students=" + addressBook.getStudentList() + "}";
        assertEquals(expected, addressBook.toString());
    }

    /**
     * A stub ReadOnlyAddressBook whose students list can violate interface constraints.
     */
    private static class AddressBookStub implements ReadOnlyAddressBook {
        private final ObservableList<Student> students = FXCollections.observableArrayList();

        AddressBookStub(Collection<Student> students) {
            this.students.setAll(students);
        }

        @Override
        public ObservableList<Student> getStudentList() {
            return students;
        }
    }

}
