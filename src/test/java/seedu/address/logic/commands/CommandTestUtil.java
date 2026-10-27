package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.student.Student;

/**
 * Contains helper methods for testing commands.
 */
public class CommandTestUtil {
    /** The data file as save-failure messages name it: relative to the home folder, with the system's separator. */
    public static final String SHOWN_DATA_FILE_NAME = Path.of("data", "tutorflow.json").toString();
    public static final String CAUSE_READ_ONLY_FOLDER = "the data folder is read-only";

    /**
     * Executes the given {@code command}, confirms that <br>
     * - the returned {@link CommandResult} matches {@code expectedCommandResult} <br>
     * - the {@code actualModel} matches {@code expectedModel}
     */
    public static void assertCommandSuccess(Command command, Model actualModel, CommandResult expectedCommandResult,
            Model expectedModel) {
        try {
            CommandResult result = command.execute(actualModel);
            assertEquals(expectedCommandResult, result);
            assertEquals(expectedModel, actualModel);
        } catch (CommandException ce) {
            throw new AssertionError("Execution of command should not fail.", ce);
        }
    }

    /**
     * Convenience wrapper to {@link #assertCommandSuccess(Command, Model, CommandResult, Model)}
     * that takes a string {@code expectedMessage}.
     */
    public static void assertCommandSuccess(Command command, Model actualModel, String expectedMessage,
            Model expectedModel) {
        CommandResult expectedCommandResult = new CommandResult(expectedMessage);
        assertCommandSuccess(command, actualModel, expectedCommandResult, expectedModel);
    }

    /**
     * Executes the given {@code command}, confirms that <br>
     * - a {@code CommandException} is thrown <br>
     * - the CommandException message matches {@code expectedMessage} <br>
     * - the address book in {@code actualModel}, i.e. the students and their homework, remains unchanged <br>
     * - the displayed students in {@code actualModel} and their order remain unchanged
     */
    public static void assertCommandFailure(Command command, Model actualModel, String expectedMessage) {
        // we are unable to defensively copy the model for comparison later, so we can
        // only do so by copying its components.
        AddressBook expectedAddressBook = new AddressBook(actualModel.getAddressBook());
        List<Student> expectedDisplayedStudents = List.copyOf(actualModel.getStudentList());

        assertThrows(CommandException.class, expectedMessage, () -> command.execute(actualModel));
        assertEquals(expectedAddressBook, actualModel.getAddressBook());
        assertEquals(expectedDisplayedStudents, actualModel.getStudentList());
    }

    /**
     * Returns the line for one homework record as {@code homework list} shows it, without the line break before it.
     */
    public static String formatHomeworkLine(int index, String title, String subject, String dueDate, String score,
            String status) {
        return index + ". " + title + " (" + subject + ") - due " + dueDate + " - score " + score + " - status "
                + status;
    }

    /**
     * Returns a command that deletes the homework at {@code homeworkIndex} of the student at {@code studentIndex}
     * and then lists that student's homework that is upcoming on {@code today}.
     * The fix for OWN_ISSUES.md B-03 adds {@code today} to the constructor. Until then the two-index constructor is
     * used, so the tests that depend on {@code today} fail on their assertions instead of failing to compile.
     * Replace this with a direct constructor call once the fix is in.
     */
    public static HomeworkDeleteCommand newHomeworkDeleteCommand(Index studentIndex, Index homeworkIndex,
            LocalDate today) {
        try {
            return HomeworkDeleteCommand.class.getConstructor(Index.class, Index.class, LocalDate.class)
                    .newInstance(studentIndex, homeworkIndex, today);
        } catch (NoSuchMethodException exception) {
            return new HomeworkDeleteCommand(studentIndex, homeworkIndex);
        } catch (InvocationTargetException exception) {
            if (exception.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw new AssertionError(exception);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    /**
     * Returns the message {@code command} shows when the data file, named {@code shownName}, could not be saved
     * because of {@code cause}.
     * The fix for OWN_ISSUES.md B-05 passes the name and the cause to {@code getSaveFailureMessage}. Until then the
     * one-argument method is called, so the tests fail on the message instead of failing to compile.
     * Replace this with a direct call once the fix is in.
     */
    public static String getSaveFailureMessage(Command command, String shownName, String cause) {
        try {
            return (String) command.getClass().getMethod("getSaveFailureMessage", String.class, String.class)
                    .invoke(command, shownName, cause);
        } catch (NoSuchMethodException exception) {
            return command.getSaveFailureMessage(shownName + ": " + cause);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

}
