package seedu.address.testutil;

import java.util.List;

import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;

/**
 * A utility class containing a list of {@code Homework} objects to be used in tests.
 */
public class TypicalHomeworks {
    public static final Homework ALGEBRA = new HomeworkBuilder().withTitle("Complete algebra worksheet")
            .withSubject("MATH").withDueDate("2026-10-15").build();
    public static final Homework MECHANICS = new HomeworkBuilder().withTitle("Attempt mechanics questions 1-5")
            .withSubject("PHYSICS").withDueDate("2026-10-20").build();
    public static final Homework ATOMIC_STRUCTURE = new HomeworkBuilder().withTitle("Revise atomic structure")
            .withSubject("CHEMISTRY").withDueDate("2026-10-18").withStatus(HomeworkStatus.COMPLETED)
            .withScore(85).build();

    private TypicalHomeworks() {} // prevents instantiation

    /** Returns the typical homework records in insertion order. */
    public static List<Homework> getTypicalHomeworks() {
        return List.of(ALGEBRA, MECHANICS, ATOMIC_STRUCTURE);
    }
}
