package seedu.address.logic.commands;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.model.student.Student;

/**
 * Looks up the student and homework that a homework command names by index.
 */
final class HomeworkCommandUtil {
    private HomeworkCommandUtil() {
    }

    /**
     * Returns the student at {@code studentIndex} in the displayed student list of {@code model}.
     *
     * @throws CommandException If {@code studentIndex} is beyond the displayed student list.
     */
    static Student getDisplayedStudent(Model model, Index studentIndex) throws CommandException {
        List<Student> students = model.getStudentList();
        if (studentIndex.getZeroBased() >= students.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        }
        return students.get(studentIndex.getZeroBased());
    }

    /**
     * Returns the homework at {@code homeworkIndex} in the homework list of {@code student}.
     *
     * @param student Student whose homework is looked up.
     * @param homeworkIndex Index of the homework in that student's homework list.
     * @param noHomeworkMessage Message reported if {@code student} has no homework.
     * @return The homework at {@code homeworkIndex}.
     * @throws CommandException If {@code student} has no homework, or {@code homeworkIndex} is beyond the list.
     */
    static Homework getHomework(Student student, Index homeworkIndex, String noHomeworkMessage)
            throws CommandException {
        List<Homework> homeworks = student.getHomeworks();
        if (homeworks.isEmpty()) {
            throw new CommandException(noHomeworkMessage);
        }
        if (homeworkIndex.getZeroBased() >= homeworks.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_HOMEWORK_DISPLAYED_INDEX);
        }
        return homeworks.get(homeworkIndex.getZeroBased());
    }
}
