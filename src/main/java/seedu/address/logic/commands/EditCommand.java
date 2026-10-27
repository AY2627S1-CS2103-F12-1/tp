package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentName;
import seedu.address.model.student.StudentPhone;
import seedu.address.model.student.TuitionSubjects;

/**
 * Edits the profile of a student identified by its displayed index.
 */
public class EditCommand extends Command {
    public static final String COMMAND_WORD = "edit";
    public static final String MESSAGE_USAGE = "edit INDEX [n/STUDENT_NAME] [l/ACADEMIC_LEVEL] [s/SUBJECTS] "
            + "[p/STUDENT_PHONE] [gn/GUARDIAN_NAME] [gp/GUARDIAN_PHONE]";
    public static final String MESSAGE_INVALID_FORMAT = "Invalid command format.\nUsage: " + MESSAGE_USAGE;
    public static final String MESSAGE_NO_FIELDS = "Specify at least one field to edit.\nUsage: " + MESSAGE_USAGE;
    public static final String MESSAGE_SUCCESS = "Edited student: %s";
    public static final String MESSAGE_NO_CHANGES = "No changes made; the student details are unchanged.";
    public static final String MESSAGE_DUPLICATE = "A student with this name and phone already exists: %s (%s).";
    public static final String MESSAGE_SUBJECT_IN_USE =
            "Cannot remove a subject used by this student's homework or lessons.";
    public static final String MESSAGE_INTERNAL_ERROR =
            "TutorFlow could not edit the student due to an internal error.";
    public static final String MESSAGE_SAVE_FAILURE = "The student could not be edited because TutorFlow could not "
            + "save %s: %s. No student data was changed.";

    private static final Logger logger = LogsCenter.getLogger(EditCommand.class);

    private final Index index;
    private final EditStudentDescriptor descriptor;

    /**
     * Creates an edit command for the student at {@code index} in the displayed list.
     */
    public EditCommand(Index index, EditStudentDescriptor descriptor) {
        requireAllNonNull(index, descriptor);
        this.index = index;
        this.descriptor = descriptor;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            return editStudent(model);
        } catch (RuntimeException exception) {
            logger.log(Level.SEVERE, "Unexpected error while editing a student", exception);
            throw new CommandException(MESSAGE_INTERNAL_ERROR, exception);
        }
    }

    /**
     * Validates a replacement before changing the model.
     */
    private CommandResult editStudent(Model model) throws CommandException {
        List<Student> displayedStudents = model.getStudentList();
        if (index.getZeroBased() >= displayedStudents.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        }

        Student student = displayedStudents.get(index.getZeroBased());
        Student editedStudent = descriptor.applyTo(student);
        if (editedStudent.equals(student)) {
            throw new CommandException(MESSAGE_NO_CHANGES);
        }
        Student duplicate = model.getAddressBook().getStudentList().stream()
                .filter(existing -> existing != student && existing.isSameStudent(editedStudent))
                .findFirst().orElse(null);
        if (duplicate != null) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE, duplicate.getName(), duplicate.getPhone()));
        }
        if (editedStudent.getHomeworks().stream()
                .anyMatch(homework -> !editedStudent.getSubjects().getSubjects().contains(homework.getSubject()))
                || editedStudent.getLessons().stream()
                .anyMatch(lesson -> !editedStudent.getSubjects().getSubjects().contains(lesson.getSubject()))) {
            throw new CommandException(MESSAGE_SUBJECT_IN_USE);
        }

        String feedback = String.format(MESSAGE_SUCCESS, Messages.format(editedStudent));
        model.setStudent(student, editedStudent);
        return new CommandResult(feedback);
    }

    @Override
    protected String getSaveFailureFormat() {
        return MESSAGE_SAVE_FAILURE;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof EditCommand command && index.equals(command.index)
                && descriptor.equals(command.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index.getZeroBased(), descriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("descriptor", descriptor).toString();
    }

    /**
     * Holds replacement profile fields; null means the existing value is kept.
     */
    public record EditStudentDescriptor(StudentName name, AcademicLevel level, TuitionSubjects subjects,
            StudentPhone phone, StudentName guardianName, StudentPhone guardianPhone) {
        /**
         * Returns a copy with the supplied fields changed and all homework and lessons preserved.
         */
        public Student applyTo(Student student) {
            requireNonNull(student);
            return new Student(Objects.requireNonNullElse(name, student.getName()),
                    Objects.requireNonNullElse(level, student.getAcademicLevel()),
                    Objects.requireNonNullElse(subjects, student.getSubjects()),
                    Objects.requireNonNullElse(phone, student.getPhone()),
                    Objects.requireNonNullElse(guardianName, student.getGuardianName()),
                    Objects.requireNonNullElse(guardianPhone, student.getGuardianPhone()),
                    student.getHomeworks(), student.getLessons());
        }
    }
}
