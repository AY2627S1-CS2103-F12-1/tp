package seedu.address.testutil;

import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalHomeworks.ATOMIC_STRUCTURE;
import static seedu.address.testutil.TypicalHomeworks.MECHANICS;

import java.util.List;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.student.Student;

/**
 * A utility class containing a list of {@code Student} objects to be used in tests.
 */
public class TypicalStudents {
    /** Takes all three subjects and has three homework records, the last one completed. */
    public static final Student ALICE = new StudentBuilder().withName("Alice Tan").withPhone("91111111")
            .withSubjects("MATH, PHYSICS, CHEMISTRY").withHomeworks(ALGEBRA, MECHANICS, ATOMIC_STRUCTURE).build();
    /** Takes math only and has no homework. */
    public static final Student BENSON = new StudentBuilder().withName("Benson Lim").withPhone("92222222")
            .withSubjects("MATH").build();
    /** Takes physics only and has one homework record. */
    public static final Student CARL = new StudentBuilder().withName("Carl Ng").withPhone("93333333")
            .withSubjects("PHYSICS").withHomeworks(MECHANICS).build();

    private TypicalStudents() {} // prevents instantiation

    /** Returns the typical students in roster order. */
    public static List<Student> getTypicalStudents() {
        return List.of(ALICE, BENSON, CARL);
    }

    /** Returns an {@code AddressBook} with all the typical students. */
    public static AddressBook getTypicalAddressBook() {
        AddressBook ab = new AddressBook();
        getTypicalStudents().forEach(ab::addStudent);
        return ab;
    }

    /** Returns a new model whose student roster holds {@code students} in order. */
    public static Model getModelWithStudents(List<Student> students) {
        Model model = new ModelManager();
        students.forEach(model::addStudent);
        return model;
    }

    /** Returns a new model whose student roster holds the typical students. */
    public static Model getModelWithTypicalStudents() {
        return getModelWithStudents(getTypicalStudents());
    }
}
