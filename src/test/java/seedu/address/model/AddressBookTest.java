package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalStudents.BENSON;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.student.Student;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.StudentBuilder;
import seedu.address.testutil.TypicalStudents;

public class AddressBookTest {

    private final AddressBook addressBook = new AddressBook();

    @Test
    public void constructor() {
        assertEquals(List.of(), addressBook.getPersonList());
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
    public void resetData_withDuplicatePersons_throwsDuplicatePersonException() {
        // Two persons with the same identity fields
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        List<Person> newPersons = List.of(ALICE, editedAlice);
        AddressBookStub newData = new AddressBookStub(newPersons, List.of());

        assertThrows(DuplicatePersonException.class, () -> addressBook.resetData(newData));
    }

    @Test
    public void resetData_withItself_keepsData() {
        AddressBook studentAddressBook = TypicalStudents.getTypicalAddressBook();
        studentAddressBook.resetData(studentAddressBook);
        assertEquals(TypicalStudents.getTypicalAddressBook(), studentAddressBook);
    }

    @Test
    public void resetData_withDuplicateStudents_throwsIllegalArgumentException() {
        // Two students with the same identity: same name ignoring case and same phone
        Student editedAlice = new StudentBuilder().withName("ALICE TAN").withPhone("91111111").build();
        AddressBookStub newData = new AddressBookStub(List.of(), List.of(TypicalStudents.ALICE, editedAlice));

        assertThrows(IllegalArgumentException.class, () -> addressBook.resetData(newData));
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        assertTrue(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personWithSameIdentityFieldsInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(addressBook.hasPerson(editedAlice));
    }

    @Test
    public void getPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getPersonList().remove(0));
    }

    @Test
    public void hasStudent_nullStudent_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasStudent(null));
    }

    @Test
    public void hasStudent_studentNotInAddressBook_returnsFalse() {
        assertFalse(addressBook.hasStudent(TypicalStudents.ALICE));
    }

    @Test
    public void hasStudent_studentWithSameIdentityInAddressBook_returnsTrue() {
        addressBook.addStudent(TypicalStudents.ALICE);
        Student editedAlice = new StudentBuilder().withName("alice tan").withPhone("91111111")
                .withGuardianName("Other Guardian").build();
        assertTrue(addressBook.hasStudent(editedAlice));
    }

    @Test
    public void addStudent_duplicateStudent_throwsIllegalArgumentException() {
        addressBook.addStudent(TypicalStudents.ALICE);
        assertThrows(IllegalArgumentException.class, () -> addressBook.addStudent(TypicalStudents.ALICE));
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
        AddressBook aliceAddressBook = new AddressBookBuilder().withStudent(TypicalStudents.ALICE).build();

        // same object -> returns true
        assertTrue(aliceAddressBook.equals(aliceAddressBook));

        // same students -> returns true
        assertTrue(aliceAddressBook.equals(new AddressBookBuilder().withStudent(TypicalStudents.ALICE).build()));

        // null -> returns false
        assertFalse(aliceAddressBook.equals(null));

        // different type -> returns false
        assertFalse(aliceAddressBook.equals(5));

        // different students -> returns false
        assertFalse(aliceAddressBook.equals(new AddressBookBuilder().withStudent(BENSON).build()));

        // same students in a different order -> returns false
        assertNotEquals(new AddressBookBuilder().withStudent(TypicalStudents.ALICE).withStudent(BENSON).build(),
                new AddressBookBuilder().withStudent(BENSON).withStudent(TypicalStudents.ALICE).build());

        // different homework of the same student -> returns false
        assertFalse(new AddressBookBuilder().withStudent(BENSON).build().equals(
                new AddressBookBuilder().withStudent(BENSON.withHomeworks(List.of(ALGEBRA))).build()));
    }

    @Test
    public void toStringMethod() {
        String expected = AddressBook.class.getCanonicalName() + "{persons=" + addressBook.getPersonList()
                + ", students=" + addressBook.getStudentList() + "}";
        assertEquals(expected, addressBook.toString());
    }

    /**
     * A stub ReadOnlyAddressBook whose persons and students lists can violate interface constraints.
     */
    private static class AddressBookStub implements ReadOnlyAddressBook {
        private final ObservableList<Person> persons = FXCollections.observableArrayList();
        private final ObservableList<Student> students = FXCollections.observableArrayList();

        AddressBookStub(Collection<Person> persons, Collection<Student> students) {
            this.persons.setAll(persons);
            this.students.setAll(students);
        }

        @Override
        public ObservableList<Person> getPersonList() {
            return persons;
        }

        @Override
        public ObservableList<Student> getStudentList() {
            return students;
        }
    }

}
