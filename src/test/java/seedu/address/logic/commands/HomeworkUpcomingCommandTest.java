package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalHomeworks.COMPLETED_MECHANICS;
import static seedu.address.testutil.TypicalStudents.CARL;
import static seedu.address.testutil.TypicalStudents.getModelWithStudents;
import static seedu.address.testutil.TypicalStudents.getModelWithTypicalStudents;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.student.Student;
import seedu.address.testutil.HomeworkBuilder;
import seedu.address.testutil.StudentBuilder;

/**
 * Tests {@code HomeworkUpcomingCommand}. Today is 2026-10-17. Zoe Lee is first in the roster and Adam Ng second,
 * so roster order is not alphabetical; within each student, insertion order is not alphabetical either.
 */
public class HomeworkUpcomingCommandTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 17);

    private static final Student ZOE = new StudentBuilder().withName("Zoe Lee").withPhone("91111111")
            .withSubjects("MATH, PHYSICS").withHomeworks(
                    new HomeworkBuilder().withTitle("Overdue essay").withDueDate("2026-10-16").build(),
                    new HomeworkBuilder().withTitle("Zeta last day").withSubject("PHYSICS").withDueDate("2026-10-20")
                            .build(),
                    new HomeworkBuilder().withTitle("Today worksheet").withDueDate("2026-10-17").withScore("5/10")
                            .build(),
                    new HomeworkBuilder().withTitle("After window").withDueDate("2026-10-21").build(),
                    new HomeworkBuilder().withTitle("Alpha last day").withDueDate("2026-10-20").build())
            .build();
    private static final Student ADAM = new StudentBuilder().withName("Adam Ng").withPhone("92222222")
            .withSubjects("MATH, CHEMISTRY").withHomeworks(
                    new HomeworkBuilder().withTitle("Last day practice").withDueDate("2026-10-20").build(),
                    new HomeworkBuilder().withTitle("Today quiz").withSubject("CHEMISTRY").withDueDate("2026-10-17")
                            .withScore("0/20").build())
            .build();
    private static final Student NO_HOMEWORK = new StudentBuilder().withName("Benson Lim").withPhone("93333333")
            .build();

    private final Model model = getModelWithStudents(List.of(ZOE, NO_HOMEWORK, ADAM));
    private final Model expectedModel = getModelWithStudents(List.of(ZOE, NO_HOMEWORK, ADAM));

    @Test
    public void constructor_nullDate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkUpcomingCommand(null, 7));
    }

    @Test
    public void constructor_negativeDays_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new HomeworkUpcomingCommand(TODAY, -1));
    }

    @Test
    public void constructor_nullTodayAndNegativeDays_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkUpcomingCommand(null, -1));
    }

    @Test
    public void execute_threeDays_listsHomeworkFromTodayToLastDayInOrder() {
        // overdue (2026-10-16) and day after the window (2026-10-21) are left out; ties keep roster order, then
        // insertion order
        String expectedMessage = "Found 5 homework records due from 2026-10-17 to 2026-10-20.\n"
                + "Student 1: Zoe Lee; Today worksheet; MATH; Due: 2026-10-17; Score: 5/10; Status: ASSIGNED\n"
                + "Student 3: Adam Ng; Today quiz; CHEMISTRY; Due: 2026-10-17; Score: 0/20; Status: ASSIGNED\n"
                + "Student 1: Zoe Lee; Zeta last day; PHYSICS; Due: 2026-10-20; Score: Not recorded; Status: ASSIGNED\n"
                + "Student 1: Zoe Lee; Alpha last day; MATH; Due: 2026-10-20; Score: Not recorded; Status: ASSIGNED\n"
                + "Student 3: Adam Ng; Last day practice; MATH; Due: 2026-10-20; Score: Not recorded; Status: ASSIGNED";

        assertCommandSuccess(new HomeworkUpcomingCommand(TODAY, 3), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_zeroDays_listsHomeworkDueTodayOnly() {
        String expectedMessage = "Found 2 homework records due from 2026-10-17 to 2026-10-17.\n"
                + "Student 1: Zoe Lee; Today worksheet; MATH; Due: 2026-10-17; Score: 5/10; Status: ASSIGNED\n"
                + "Student 3: Adam Ng; Today quiz; CHEMISTRY; Due: 2026-10-17; Score: 0/20; Status: ASSIGNED";

        assertCommandSuccess(new HomeworkUpcomingCommand(TODAY, 0), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_windowEndsDayBeforeDueDate_excludesHomework() {
        // the window ends on 2026-10-19, one day before the homework due on 2026-10-20
        String expectedMessage = "Found 2 homework records due from 2026-10-17 to 2026-10-19.\n"
                + "Student 1: Zoe Lee; Today worksheet; MATH; Due: 2026-10-17; Score: 5/10; Status: ASSIGNED\n"
                + "Student 3: Adam Ng; Today quiz; CHEMISTRY; Due: 2026-10-17; Score: 0/20; Status: ASSIGNED";

        assertCommandSuccess(new HomeworkUpcomingCommand(TODAY, 2), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_oneRecord_listsSingleRecord() {
        Model singleModel = getModelWithStudents(List.of(ADAM));
        String expectedMessage = "Found 1 homework record due from 2026-10-17 to 2026-10-17.\n"
                + "Student 1: Adam Ng; Today quiz; CHEMISTRY; Due: 2026-10-17; Score: 0/20; Status: ASSIGNED";

        assertCommandSuccess(new HomeworkUpcomingCommand(TODAY, 0), singleModel, expectedMessage,
                getModelWithStudents(List.of(ADAM)));
    }

    @Test
    public void execute_typicalStudents_listsAcrossStudents() {
        String expectedMessage = "Found 4 homework records due from 2026-10-15 to 2026-10-22.\n"
                + "Student 1: Alice Tan; Complete algebra worksheet; MATH; Due: 2026-10-15; Score: Not recorded"
                + "; Status: ASSIGNED\n"
                + "Student 1: Alice Tan; Revise atomic structure; CHEMISTRY; Due: 2026-10-18; Score: 17/20"
                + "; Status: ASSIGNED\n"
                + "Student 1: Alice Tan; Attempt mechanics questions 1-5; PHYSICS; Due: 2026-10-20; "
                + "Score: Not recorded; Status: ASSIGNED\n"
                + "Student 3: Carl Ng; Attempt mechanics questions 1-5; PHYSICS; Due: 2026-10-20; Score: Not recorded"
                + "; Status: ASSIGNED";

        assertCommandSuccess(new HomeworkUpcomingCommand(LocalDate.of(2026, 10, 15), 7),
                getModelWithTypicalStudents(), expectedMessage, getModelWithTypicalStudents());
    }

    @Test
    public void execute_sameNameStudents_showsDistinctStudentIndexes() {
        // two students named John Tan, told apart by phone in the roster and by STUDENT_INDEX in the result
        Student firstJohn = new StudentBuilder().withPhone("91111111")
                .withHomeworks(new HomeworkBuilder().withDueDate("2026-10-17").build()).build();
        Student secondJohn = new StudentBuilder().withPhone("92222222")
                .withHomeworks(new HomeworkBuilder().withDueDate("2026-10-17").withScore("7/10").build()).build();
        String expectedMessage = "Found 2 homework records due from 2026-10-17 to 2026-10-17.\n"
                + "Student 2: John Tan; Complete algebra worksheet; MATH; Due: 2026-10-17; Score: Not recorded"
                + "; Status: ASSIGNED\n"
                + "Student 3: John Tan; Complete algebra worksheet; MATH; Due: 2026-10-17; Score: 7/10"
                + "; Status: ASSIGNED";

        assertCommandSuccess(new HomeworkUpcomingCommand(TODAY, 0),
                getModelWithStudents(List.of(NO_HOMEWORK, firstJohn, secondJohn)), expectedMessage,
                getModelWithStudents(List.of(NO_HOMEWORK, firstJohn, secondJohn)));
    }

    @Test
    public void execute_bareScore_showsScoreAsEntered() {
        Student student = new StudentBuilder().withHomeworks(
                new HomeworkBuilder().withTitle("Bare").withDueDate("2026-10-17").withScore("17").build(),
                new HomeworkBuilder().withTitle("Ratio").withDueDate("2026-10-18").withScore("17/20").build())
                .build();
        String expectedMessage = "Found 2 homework records due from 2026-10-17 to 2026-10-24.\n"
                + "Student 1: John Tan; Bare; MATH; Due: 2026-10-17; Score: 17; Status: ASSIGNED\n"
                + "Student 1: John Tan; Ratio; MATH; Due: 2026-10-18; Score: 17/20; Status: ASSIGNED";

        assertCommandSuccess(new HomeworkUpcomingCommand(TODAY, 7), getModelWithStudents(List.of(student)),
                expectedMessage, getModelWithStudents(List.of(student)));
    }

    @Test
    public void execute_noStudentDisplayed_listsAndShowsWholeRoster() {
        // no student is displayed, but all students in the roster are searched and then displayed again
        model.updateFilteredStudentList(student -> false);
        String expectedMessage = "Found 2 homework records due from 2026-10-17 to 2026-10-17.\n"
                + "Student 1: Zoe Lee; Today worksheet; MATH; Due: 2026-10-17; Score: 5/10; Status: ASSIGNED\n"
                + "Student 3: Adam Ng; Today quiz; CHEMISTRY; Due: 2026-10-17; Score: 0/20; Status: ASSIGNED";

        assertCommandSuccess(new HomeworkUpcomingCommand(TODAY, 0), model, expectedMessage, expectedModel);
        assertEquals(List.of(ZOE, NO_HOMEWORK, ADAM), model.getStudentList());
    }

    @Test
    public void execute_noHomeworkInWindowWhileFiltered_showsWholeRoster() {
        // the full roster is displayed again even when no homework is due; the last homework is due on 2026-10-21
        model.updateFilteredStudentList(student -> student.equals(ADAM));

        assertCommandSuccess(new HomeworkUpcomingCommand(LocalDate.of(2026, 10, 22), 7), model,
                "No homework due from 2026-10-22 to 2026-10-29.", expectedModel);
        assertEquals(List.of(ZOE, NO_HOMEWORK, ADAM), model.getStudentList());
    }

    @Test
    public void execute_noHomeworkInWindow_showsNoHomeworkDue() {
        // all homework of the typical students is due by 2026-10-20
        assertCommandSuccess(new HomeworkUpcomingCommand(LocalDate.of(2026, 10, 21), 7),
                getModelWithTypicalStudents(), "No homework due from 2026-10-21 to 2026-10-28.",
                getModelWithTypicalStudents());
    }

    @Test
    public void execute_completedHomework_showsCompletedStatus() {
        Model completedModel = getModelWithStudents(List.of(CARL.withHomeworks(List.of(COMPLETED_MECHANICS))));
        String expectedMessage = "Found 1 homework record due from 2026-10-17 to 2026-10-20.\n"
                + "Student 1: Carl Ng; Attempt mechanics questions 1-5; PHYSICS; Due: 2026-10-20; Score: Not recorded;"
                + " Status: COMPLETED";

        assertCommandSuccess(new HomeworkUpcomingCommand(TODAY, 3), completedModel, expectedMessage,
                getModelWithStudents(List.of(CARL.withHomeworks(List.of(COMPLETED_MECHANICS)))));
    }

    @Test
    public void execute_emptyRoster_showsNoHomeworkDue() {
        assertCommandSuccess(new HomeworkUpcomingCommand(TODAY, 365), getModelWithStudents(List.of()),
                "No homework due from 2026-10-17 to 2027-10-17.", getModelWithStudents(List.of()));
    }

    @Test
    public void execute_unexpectedError_reportsInternalErrorWithoutChangingModel() {
        Model failingModel = getModelFailingOnGetAddressBook();

        assertThrows(CommandException.class, HomeworkUpcomingCommand.MESSAGE_INTERNAL_ERROR, ()
                -> new HomeworkUpcomingCommand(TODAY, 7).execute(failingModel));
        // ModelManager#equals compares the stored rosters directly, not through getAddressBook()
        assertEquals(expectedModel, failingModel);
    }

    @Test
    public void execute_unexpectedError_keepsDisplayedStudents() {
        Model failingModel = getModelFailingOnGetAddressBook();
        failingModel.updateFilteredStudentList(student -> student.equals(ADAM));

        assertThrows(CommandException.class, HomeworkUpcomingCommand.MESSAGE_INTERNAL_ERROR, ()
                -> new HomeworkUpcomingCommand(TODAY, 7).execute(failingModel));
        assertEquals(List.of(ADAM), failingModel.getStudentList());
    }

    @Test
    public void equals() {
        HomeworkUpcomingCommand upcomingCommand = new HomeworkUpcomingCommand(TODAY, 7);

        // same object -> returns true
        assertTrue(upcomingCommand.equals(upcomingCommand));

        // same values -> returns true
        HomeworkUpcomingCommand upcomingCommandCopy = new HomeworkUpcomingCommand(LocalDate.of(2026, 10, 17), 7);
        assertTrue(upcomingCommand.equals(upcomingCommandCopy));
        assertEquals(upcomingCommand.hashCode(), upcomingCommandCopy.hashCode());

        // null -> returns false
        assertFalse(upcomingCommand.equals(null));

        // different types -> returns false
        assertFalse(upcomingCommand.equals(1));

        // different date -> returns false
        assertFalse(upcomingCommand.equals(new HomeworkUpcomingCommand(TODAY.plusDays(1), 7)));

        // different number of days -> returns false
        assertFalse(upcomingCommand.equals(new HomeworkUpcomingCommand(TODAY, 3)));
    }

    @Test
    public void toStringMethod() {
        String expected = HomeworkUpcomingCommand.class.getCanonicalName() + "{today=2026-10-17, days=7}";
        assertEquals(expected, new HomeworkUpcomingCommand(TODAY, 7).toString());
    }

    /**
     * Returns a new model holding Zoe, Benson and Adam whose {@code getAddressBook} throws an unexpected
     * {@code IllegalStateException}.
     */
    private static Model getModelFailingOnGetAddressBook() {
        Model failingModel = new ModelManager() {
            @Override
            public ReadOnlyAddressBook getAddressBook() {
                throw new IllegalStateException("dummy internal error");
            }
        };
        List.of(ZOE, NO_HOMEWORK, ADAM).forEach(failingModel::addStudent);
        return failingModel;
    }
}
