package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.student.SortKey;
import seedu.address.model.student.SortOrder;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentSorter;

/**
 * Sorts only the displayed students without changing the active search or stored records.
 */
public class SortCommand extends Command {
    public static final String COMMAND_WORD = "sort";
    public static final String MESSAGE_USAGE = "Usage: sort by/KEY [o/ORDER]";
    public static final String MESSAGE_INVALID_FORMAT = "Invalid command format\n" + MESSAGE_USAGE;
    public static final String MESSAGE_MISSING_KEY = MESSAGE_INVALID_FORMAT + "\nExactly one sort key must be provided";
    public static final String MESSAGE_REPEATED_PREFIX = "Each sort parameter may only be specified once";
    public static final String MESSAGE_INVALID_KEY = "Sort key must be one of: level, subject, day.";
    public static final String MESSAGE_INVALID_ORDER = "Sort order must be either asc or desc.";
    public static final String MESSAGE_EMPTY = "No students to sort.";
    public static final String MESSAGE_SUCCESS = "Sorted %d student(s) by %s (%s).";
    public static final String MESSAGE_INTERNAL_ERROR =
            "TutorFlow could not sort the students due to an internal error.";

    private static final Logger logger = LogsCenter.getLogger(SortCommand.class);

    private final SortKey key;
    private final SortOrder order;

    /**
     * Creates a sort using one key and direction.
     */
    public SortCommand(SortKey key, SortOrder order) {
        requireAllNonNull(key, order);
        this.key = key;
        this.order = order;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            return sortStudents(model);
        } catch (RuntimeException exception) {
            Level level = exception.getSuppressed().length == 0 ? Level.WARNING : Level.SEVERE;
            logger.log(level, "Student sort failed: key=" + key + ", order=" + order
                    + ", phase=ordering, recovery=" + (level == Level.WARNING ? "preserved" : "failed"), exception);
            throw new CommandException(MESSAGE_INTERNAL_ERROR, exception);
        }
    }

    /**
     * Computes the candidate before publication and retains a recovery action for rendering failures.
     */
    private CommandResult sortStudents(Model model) {
        List<Student> previousStudents = List.copyOf(model.getStudentList());
        if (previousStudents.isEmpty()) {
            return new CommandResult(MESSAGE_EMPTY);
        }
        List<Student> sortedStudents = StudentSorter.sort(previousStudents, key, order);
        logger.fine(() -> "Applying student sort: key=" + key + ", order=" + order
                + ", count=" + previousStudents.size());
        try {
            model.setStudentListOrder(sortedStudents);
        } catch (RuntimeException exception) {
            restoreAfterFailure(model, previousStudents, exception);
            throw exception;
        }
        return new SortCommandResult(String.format(MESSAGE_SUCCESS, previousStudents.size(),
                key.getDescription(), order.getDescription()), () -> model.setStudentListOrder(previousStudents));
    }

    /**
     * Retains both failures when an unexpected display publication error also prevents restoration.
     */
    private static void restoreAfterFailure(Model model, List<Student> previousStudents, RuntimeException exception) {
        try {
            model.setStudentListOrder(previousStudents);
        } catch (RuntimeException recoveryFailure) {
            if (recoveryFailure != exception) {
                exception.addSuppressed(recoveryFailure);
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SortCommand command && key == command.key && order == command.order;
    }

    @Override
    public int hashCode() {
        return 31 * key.hashCode() + order.hashCode();
    }
}
