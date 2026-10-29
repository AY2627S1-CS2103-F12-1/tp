package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.DueDate;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.Title;
import seedu.address.model.student.Student;
import seedu.address.model.student.TuitionSubject;

/**
 * Edits the title, subject or due date of one homework record, identified by its index in the homework list of a
 * student identified by its displayed index. The homework keeps its position, score and completion status. On success,
 * the student's upcoming homework is listed as by {@link HomeworkListCommand}.
 */
public class HomeworkEditCommand extends Command {
    public static final String MESSAGE_USAGE = "homework|hw edit|e STUDENT_INDEX HOMEWORK_INDEX [title/|t/TITLE] "
            + "[s/SUBJECT] [due/YYYY-MM-DD|MM-DD]";
    public static final String MESSAGE_SUCCESS = "Edited homework:\n%s";
    public static final String MESSAGE_NO_HOMEWORK = "The selected student has no homework to edit.";
    public static final String MESSAGE_DUPLICATE_HOMEWORK =
            "This edit would make the homework a duplicate of an existing homework record.";
    public static final String MESSAGE_NO_CHANGES =
            "No changes made; the supplied values are identical to the existing homework details.";
    public static final String MESSAGE_INTERNAL_ERROR =
            "TutorFlow could not edit the homework due to an internal error.";
    public static final String MESSAGE_SAVE_FAILURE = "The homework could not be edited because TutorFlow could not "
            + "save %s: %s. No homework data was changed.";

    private static final Logger logger = LogsCenter.getLogger(HomeworkEditCommand.class);

    private final Index studentIndex;
    private final Index homeworkIndex;
    private final EditHomeworkDescriptor descriptor;
    private final boolean isYearInferred;
    private final LocalDate today;

    /**
     * Creates a command that edits the homework at {@code homeworkIndex} of the student at {@code studentIndex}.
     *
     * @param studentIndex Index of the student in the displayed student list.
     * @param homeworkIndex Index of the homework in that student's homework list.
     * @param descriptor New values of the fields to edit.
     * @param isYearInferred Whether the year of the new due date was inferred, so the success message reports it.
     * @param today Date from which homework counts as upcoming in the homework list shown on success.
     */
    public HomeworkEditCommand(Index studentIndex, Index homeworkIndex, EditHomeworkDescriptor descriptor,
            boolean isYearInferred, LocalDate today) {
        requireAllNonNull(studentIndex, homeworkIndex, descriptor, today);
        this.studentIndex = studentIndex;
        this.homeworkIndex = homeworkIndex;
        this.descriptor = descriptor;
        this.isYearInferred = isYearInferred;
        this.today = today;
    }

    /**
     * {@inheritDoc}
     * An unexpected error is logged and reported as an internal error, leaving the model unchanged.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            return editHomework(model);
        } catch (RuntimeException exception) {
            logger.log(Level.SEVERE, "Unexpected error while editing homework", exception);
            throw new CommandException(MESSAGE_INTERNAL_ERROR, exception);
        }
    }

    /**
     * Returns the result of editing the homework of the selected student. Changing the model is the last step, so
     * an exception thrown before it leaves the model unchanged.
     */
    private CommandResult editHomework(Model model) throws CommandException {
        Student student = HomeworkCommandUtil.getDisplayedStudent(model, studentIndex);
        Homework homeworkToEdit = HomeworkCommandUtil.getHomework(student, homeworkIndex, MESSAGE_NO_HOMEWORK);
        Homework editedHomework = descriptor.applyTo(homeworkToEdit);
        if (!student.getSubjects().getSubjects().contains(editedHomework.getSubject())) {
            throw new CommandException(HomeworkAddCommand.MESSAGE_SUBJECT_NOT_TAKEN);
        }
        if (editedHomework.equals(homeworkToEdit)) {
            throw new CommandException(MESSAGE_NO_CHANGES);
        }
        if (isDuplicateOfOtherHomework(student.getHomeworks(), editedHomework)) {
            throw new CommandException(MESSAGE_DUPLICATE_HOMEWORK);
        }

        List<Homework> updatedHomeworks = new ArrayList<>(student.getHomeworks());
        updatedHomeworks.set(homeworkIndex.getZeroBased(), editedHomework);
        Student editedStudent = student.withHomeworks(updatedHomeworks);
        String message = buildSuccessMessage(editedStudent, editedHomework);

        model.setStudent(student, editedStudent);
        return new CommandResult(message);
    }

    /**
     * Returns the success message for {@code editedHomework}, with the inferred year if any, followed by the
     * upcoming homework of {@code editedStudent}, and a note on how to list the homework if the edit sets its due
     * date before today.
     */
    private String buildSuccessMessage(Student editedStudent, Homework editedHomework) {
        String message = String.format(Locale.ROOT, MESSAGE_SUCCESS,
                Messages.format(editedStudent, editedHomework));
        if (isYearInferred) {
            // Locale.ROOT prints the year in ASCII digits in every locale, like the due date before it
            message += String.format(Locale.ROOT, HomeworkAddCommand.MESSAGE_INFERRED_YEAR,
                    editedHomework.getDueDate().getDate().getYear());
        }
        message += "\n" + HomeworkListCommand.formatHomeworkList(editedStudent, studentIndex, today, false);
        if (descriptor.dueDate() != null && !editedHomework.isUpcoming(today)) {
            // Locale.ROOT prints the student index in ASCII digits, which the index parsers accept, in every locale
            message += String.format(Locale.ROOT, HomeworkAddCommand.MESSAGE_DUE_BEFORE_TODAY,
                    studentIndex.getOneBased());
        }
        return message;
    }

    /**
     * Returns true if {@code editedHomework} is the same as any homework in {@code homeworks} other than the one
     * being edited.
     */
    private boolean isDuplicateOfOtherHomework(List<Homework> homeworks, Homework editedHomework) {
        for (int i = 0; i < homeworks.size(); i++) {
            if (i != homeworkIndex.getZeroBased() && homeworks.get(i).isSameHomework(editedHomework)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected String getSaveFailureFormat() {
        return MESSAGE_SAVE_FAILURE;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof HomeworkEditCommand otherCommand)) {
            return false;
        }

        return studentIndex.equals(otherCommand.studentIndex)
                && homeworkIndex.equals(otherCommand.homeworkIndex)
                && descriptor.equals(otherCommand.descriptor)
                && isYearInferred == otherCommand.isYearInferred
                && today.equals(otherCommand.today);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex.getZeroBased(), homeworkIndex.getZeroBased(), descriptor,
                isYearInferred, today);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .add("homeworkIndex", homeworkIndex)
                .add("descriptor", descriptor)
                .add("isYearInferred", isYearInferred)
                .add("today", today)
                .toString();
    }

    /**
     * Holds the new values of the homework fields to edit. A null value leaves that field unchanged.
     *
     * @param title New title, or null to keep the title.
     * @param subject New subject, or null to keep the subject.
     * @param dueDate New due date, or null to keep the due date.
     */
    public record EditHomeworkDescriptor(Title title, TuitionSubject subject, DueDate dueDate) {
        /** Returns a copy with edited fields replaced, preserving score and completion status. */
        public Homework applyTo(Homework homework) {
            requireNonNull(homework);
            return new Homework(Objects.requireNonNullElse(title, homework.getTitle()),
                    Objects.requireNonNullElse(subject, homework.getSubject()),
                    Objects.requireNonNullElse(dueDate, homework.getDueDate()),
                    homework.getScore().orElse(null), homework.getStatus());
        }
    }
}
