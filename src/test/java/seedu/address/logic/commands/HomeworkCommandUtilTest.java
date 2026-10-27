package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalHomeworks.ATOMIC_STRUCTURE;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.BENSON;
import static seedu.address.testutil.TypicalStudents.CARL;
import static seedu.address.testutil.TypicalStudents.getModelWithTypicalStudents;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

public class HomeworkCommandUtilTest {
    private static final String MESSAGE_NO_HOMEWORK = "No homework.";

    private final Model model = getModelWithTypicalStudents();

    @Test
    public void getDisplayedStudent_lastDisplayedIndex_returnsStudent() throws CommandException {
        assertEquals(CARL, HomeworkCommandUtil.getDisplayedStudent(model, Index.fromOneBased(3)));
    }

    @Test
    public void getDisplayedStudent_indexBeyondDisplayedList_throwsCommandException() {
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX, ()
                -> HomeworkCommandUtil.getDisplayedStudent(model, Index.fromOneBased(4)));
    }

    @Test
    public void getDisplayedStudent_filteredList_usesDisplayedIndex() throws CommandException {
        model.updateFilteredStudentList(CARL::equals);

        assertEquals(CARL, HomeworkCommandUtil.getDisplayedStudent(model, Index.fromOneBased(1)));
    }

    @Test
    public void getDisplayedStudent_indexBeyondFilteredList_throwsCommandException() {
        model.updateFilteredStudentList(CARL::equals);

        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX, ()
                -> HomeworkCommandUtil.getDisplayedStudent(model, Index.fromOneBased(2)));
    }

    @Test
    public void getHomework_lastIndex_returnsHomework() throws CommandException {
        assertEquals(ATOMIC_STRUCTURE, HomeworkCommandUtil.getHomework(ALICE, Index.fromOneBased(3),
                MESSAGE_NO_HOMEWORK));
    }

    @Test
    public void getHomework_indexBeyondList_throwsCommandException() {
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_HOMEWORK_DISPLAYED_INDEX, ()
                -> HomeworkCommandUtil.getHomework(ALICE, Index.fromOneBased(4), MESSAGE_NO_HOMEWORK));
    }

    @Test
    public void getHomework_studentWithoutHomework_throwsCommandException() {
        assertThrows(CommandException.class, MESSAGE_NO_HOMEWORK, ()
                -> HomeworkCommandUtil.getHomework(BENSON, Index.fromOneBased(1), MESSAGE_NO_HOMEWORK));
    }
}
