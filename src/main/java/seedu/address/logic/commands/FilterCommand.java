package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.student.StudentFilterPredicate;

/**
 * Displays every stored student matching the supplied academic level and subject criteria.
 */
public final class FilterCommand extends Command {
    public static final String COMMAND_WORD = "filter";
    public static final String MESSAGE_USAGE = "filter [l/LEVEL] [s/SUBJECT]";
    public static final String MESSAGE_SUCCESS = "Found %d student(s)";
    public static final String MESSAGE_NO_MATCHES = "No students match the selected filters";
    public static final String MESSAGE_INTERNAL_ERROR =
            "TutorFlow could not display the filtered results due to an internal error";

    private static final Logger logger = LogsCenter.getLogger(FilterCommand.class);

    private final StudentFilterPredicate predicate;

    /**
     * Creates a filter that applies {@code predicate} to the full stored roster.
     */
    public FilterCommand(StudentFilterPredicate predicate) {
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            model.updateFilteredStudentList(predicate);
            int count = model.getStudentList().size();
            logger.fine(() -> "Student filters: " + predicate + "; matches: " + count);
            String feedback = count == 0 ? MESSAGE_NO_MATCHES : String.format(MESSAGE_SUCCESS, count);
            return new CommandResult(feedback, false, false, true);
        } catch (RuntimeException exception) {
            logger.log(Level.WARNING, "Unexpected error while filtering students", exception);
            throw new CommandException(MESSAGE_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof FilterCommand command && predicate.equals(command.predicate);
    }

    @Override
    public int hashCode() {
        return predicate.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("predicate", predicate).toString();
    }
}
