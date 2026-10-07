package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.model.student.Student;

/**
 * Appends a homework record to the homework list of a student identified by its displayed index.
 */
public class HomeworkAddCommand extends Command {
    public static final String MESSAGE_USAGE =
            "homework|hw add STUDENT_INDEX title/|t/TITLE s/SUBJECT due/YYYY-MM-DD|MM-DD";
    public static final String MESSAGE_SUCCESS = "New homework added:\n%s";
    public static final String MESSAGE_INFERRED_YEAR = "\nInferred year: %d";
    public static final String MESSAGE_SUBJECT_NOT_TAKEN = "The selected student does not take this subject.";
    public static final String MESSAGE_DUPLICATE_HOMEWORK = "This homework already exists for the selected student.";
    public static final String MESSAGE_INTERNAL_ERROR =
            "TutorFlow could not add the homework due to an internal error.";
    public static final String MESSAGE_SAVE_FAILURE = "The homework could not be added because TutorFlow could not "
            + "save the updated data. No homework data was changed.";

    private static final Logger logger = LogsCenter.getLogger(HomeworkAddCommand.class);

    private final Index studentIndex;
    private final Homework homework;
    private final boolean isYearInferred;

    /**
     * Creates a command that adds {@code homework} to the student at {@code studentIndex}.
     *
     * @param studentIndex Displayed index of the student receiving the homework.
     * @param homework Homework to add.
     * @param isYearInferred Whether the year of the due date was inferred, so the success message reports it.
     */
    public HomeworkAddCommand(Index studentIndex, Homework homework, boolean isYearInferred) {
        requireAllNonNull(studentIndex, homework);
        this.studentIndex = studentIndex;
        this.homework = homework;
        this.isYearInferred = isYearInferred;
    }

    /**
     * {@inheritDoc}
     * An unexpected error is logged and reported as an internal error, leaving the model unchanged.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            return addHomework(model);
        } catch (RuntimeException exception) {
            logger.log(Level.SEVERE, "Unexpected error while adding homework", exception);
            throw new CommandException(MESSAGE_INTERNAL_ERROR, exception);
        }
    }

    /**
     * Returns the result of adding the homework to the selected student. Changing the model is the last step, so
     * an exception thrown before it leaves the model unchanged.
     */
    private CommandResult addHomework(Model model) throws CommandException {
        List<Student> students = model.getStudentList();
        if (studentIndex.getZeroBased() >= students.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        }

        Student student = students.get(studentIndex.getZeroBased());
        if (!student.getSubjects().getSubjects().contains(homework.getSubject())) {
            throw new CommandException(MESSAGE_SUBJECT_NOT_TAKEN);
        }
        if (student.hasHomework(homework)) {
            throw new CommandException(MESSAGE_DUPLICATE_HOMEWORK);
        }

        List<Homework> updatedHomeworks = new ArrayList<>(student.getHomeworks());
        updatedHomeworks.add(homework);
        String message = String.format(MESSAGE_SUCCESS, Messages.format(student, homework));
        if (isYearInferred) {
            message += String.format(MESSAGE_INFERRED_YEAR, homework.getDueDate().getDate().getYear());
        }

        model.setStudent(student, student.withHomeworks(updatedHomeworks));
        return new CommandResult(message);
    }

    @Override
    public String getSaveFailureMessage(String defaultMessage) {
        return MESSAGE_SAVE_FAILURE;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof HomeworkAddCommand otherCommand)) {
            return false;
        }

        return studentIndex.equals(otherCommand.studentIndex)
                && homework.equals(otherCommand.homework)
                && isYearInferred == otherCommand.isYearInferred;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .add("homework", homework)
                .add("isYearInferred", isYearInferred)
                .toString();
    }
}
