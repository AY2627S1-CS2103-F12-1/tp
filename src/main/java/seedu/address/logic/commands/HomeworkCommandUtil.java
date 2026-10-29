package seedu.address.logic.commands;

import java.util.List;
import java.util.Locale;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.model.student.Student;

/**
 * Looks up the student and homework that a homework command names by index, and checks a homework that a command
 * adds or edits against the student's subjects and other homework.
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

    /**
     * Checks that {@code student} takes the subject of {@code homework}.
     *
     * @throws CommandException If {@code student} does not take it, naming the subjects they take.
     */
    static void requireSubjectTaken(Student student, Homework homework) throws CommandException {
        if (!student.getSubjects().getSubjects().contains(homework.getSubject())) {
            throw new CommandException(String.format(Locale.ROOT, HomeworkAddCommand.MESSAGE_SUBJECT_NOT_TAKEN,
                    Messages.formatLessonOwner(student), homework.getSubject(), student.getSubjects()));
        }
    }

    /**
     * Checks that no homework of {@code student}, other than the one at {@code skippedIndex}, is the same as
     * {@code homework}.
     *
     * @param student Student whose homework is checked.
     * @param homework Homework to look for.
     * @param skippedIndex Index of the homework being edited, which is not checked, or null to check all homework.
     * @param duplicateMessage Message format for a duplicate, filled with the student and the line that
     *     {@code homework list} shows for the homework that is the same.
     * @throws CommandException If such a homework is found.
     */
    static void requireNoSameHomework(Student student, Homework homework, Index skippedIndex,
            String duplicateMessage) throws CommandException {
        List<Homework> homeworks = student.getHomeworks();
        for (int i = 0; i < homeworks.size(); i++) {
            boolean isSkipped = skippedIndex != null && i == skippedIndex.getZeroBased();
            if (!isSkipped && homeworks.get(i).isSameHomework(homework)) {
                throw new CommandException(String.format(Locale.ROOT, duplicateMessage,
                        Messages.formatLessonOwner(student), HomeworkListCommand.formatHomeworkLine(i + 1,
                                homeworks.get(i))));
            }
        }
    }
}
