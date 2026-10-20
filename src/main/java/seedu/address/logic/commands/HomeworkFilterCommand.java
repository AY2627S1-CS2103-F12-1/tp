package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.DueDate;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.student.Student;
import seedu.address.model.student.TuitionSubject;

/**
 * Lists matching homework of displayed students without changing their view or saved records.
 */
public class HomeworkFilterCommand extends Command {
    public static final String MESSAGE_USAGE = "homework|hw filter [STUDENT_INDEX] [s/SUBJECT] [due/DATE]"
            + " [status/STATUS]";
    public static final String MESSAGE_NO_MATCHES = "No homework matches the supplied filters.";
    public static final String MESSAGE_SUCCESS = "Found %d matching homework records.";
    public static final String MESSAGE_LINE = "\nStudent %d: %s; Homework %d: %s; %s; Due: %s; Score: %s; Status: %s";
    public static final String MESSAGE_INTERNAL = "TutorFlow could not filter homework due to an internal error.";
    private static final Logger logger = LogsCenter.getLogger(HomeworkFilterCommand.class);

    private final Optional<Index> studentIndex;
    private final Optional<TuitionSubject> subject;
    private final Optional<DueDate> dueDate;
    private final Optional<HomeworkStatus> status;

    /**
     * Creates a filter; supplied criteria must all match. At least one criterion is required.
     */
    public HomeworkFilterCommand(Optional<Index> studentIndex, Optional<TuitionSubject> subject,
            Optional<DueDate> dueDate, Optional<HomeworkStatus> status) {
        requireAllNonNull(studentIndex, subject, dueDate, status);
        if (studentIndex.isEmpty() && subject.isEmpty() && dueDate.isEmpty() && status.isEmpty()) {
            throw new IllegalArgumentException("Specify at least one homework filter.");
        }
        this.studentIndex = studentIndex;
        this.subject = subject;
        this.dueDate = dueDate;
        this.status = status;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            return filterHomework(model);
        } catch (RuntimeException exception) {
            logger.log(Level.SEVERE, "Unexpected error while filtering homework", exception);
            throw new CommandException(MESSAGE_INTERNAL, exception);
        }
    }

    private CommandResult filterHomework(Model model) throws CommandException {
        List<Student> students = model.getStudentList();
        if (studentIndex.isPresent() && studentIndex.get().getZeroBased() >= students.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        }
        int matches = 0;
        StringBuilder lines = new StringBuilder();
        for (int i = 0; i < students.size(); i++) {
            if (studentIndex.isPresent() && studentIndex.get().getZeroBased() != i) {
                continue;
            }
            Student student = students.get(i);
            List<Homework> homeworks = student.getHomeworks();
            for (int j = 0; j < homeworks.size(); j++) {
                Homework homework = homeworks.get(j);
                if (subject.filter(value -> value != homework.getSubject()).isPresent()
                        || dueDate.filter(value -> !value.equals(homework.getDueDate())).isPresent()
                        || status.filter(value -> value != homework.getStatus()).isPresent()) {
                    continue;
                }
                matches++;
                lines.append(String.format(MESSAGE_LINE, i + 1, student.getName(), j + 1, homework.getTitle(),
                        homework.getSubject(), homework.getDueDate(), Messages.formatScore(homework),
                        homework.getStatus()));
            }
        }
        return new CommandResult(matches == 0 ? MESSAGE_NO_MATCHES
                : String.format(MESSAGE_SUCCESS, matches) + lines);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof HomeworkFilterCommand command
                && studentIndex.equals(command.studentIndex) && subject.equals(command.subject)
                && dueDate.equals(command.dueDate) && status.equals(command.status);
    }
}
