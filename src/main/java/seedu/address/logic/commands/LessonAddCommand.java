package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.Student;

/** Adds a regular weekly lesson to the student at a displayed index. */
public class LessonAddCommand extends Command {
    public static final String MESSAGE_USAGE = "lesson add INDEX s/SUBJECT d/DAY st/START_TIME et/END_TIME";
    public static final String MESSAGE_SUBJECT = "The selected student is not registered for this subject.";
    public static final String MESSAGE_DUPLICATE = "This lesson already exists.";
    public static final String MESSAGE_CLASH = "This lesson clashes with an existing lesson: ";
    public static final String MESSAGE_INTERNAL = "TutorFlow could not add the lesson due to an internal error.";
    public static final String MESSAGE_SAVE_FAILURE = "The lesson could not be added because TutorFlow could not "
            + "save the updated data. No lesson data was changed.";
    private static final Logger logger = LogsCenter.getLogger(LessonAddCommand.class);

    private final Index studentIndex;
    private final RegularLesson lesson;

    /** Creates a command to add the given lesson to the selected student. */
    public LessonAddCommand(Index studentIndex, RegularLesson lesson) {
        requireAllNonNull(studentIndex, lesson);
        this.studentIndex = studentIndex;
        this.lesson = lesson;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            return addLesson(model);
        } catch (RuntimeException exception) {
            logger.log(Level.SEVERE, "Unexpected error while adding a lesson", exception);
            throw new CommandException(MESSAGE_INTERNAL, exception);
        }
    }

    /** Validates the schedule before replacing the immutable student. */
    private CommandResult addLesson(Model model) throws CommandException {
        List<Student> students = model.getStudentList();
        if (studentIndex.getZeroBased() >= students.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        }
        Student student = students.get(studentIndex.getZeroBased());
        if (!student.getSubjects().getSubjects().contains(lesson.getSubject())) {
            throw new CommandException(MESSAGE_SUBJECT);
        }
        if (student.getLessons().contains(lesson)) {
            throw new CommandException(MESSAGE_DUPLICATE);
        }
        for (Student other : students) {
            for (RegularLesson existing : other.getLessons()) {
                if (lesson.clashesWith(existing)) {
                    throw new CommandException(MESSAGE_CLASH + other.getName() + "; " + existing);
                }
            }
        }
        List<RegularLesson> updated = new ArrayList<>(student.getLessons());
        updated.add(lesson);
        Student updatedStudent = student.withLessons(updated);
        CommandResult result = new CommandResult("New lesson added: " + student.getName() + "; " + lesson);
        model.setStudent(student, updatedStudent);
        return result;
    }

    @Override
    public String getSaveFailureMessage(String defaultMessage) {
        return MESSAGE_SAVE_FAILURE;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof LessonAddCommand command && studentIndex.equals(command.studentIndex)
                && lesson.equals(command.lesson);
    }
}
