package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.student.Student;

/** Shows students whose name or phone, or whose guardian's name or phone, contains a keyword. */
public class SearchCommand extends Command {
    public static final String COMMAND_WORD = "search";
    public static final String MESSAGE_USAGE = "search KEYWORD";
    public static final String MESSAGE_INVALID_FORMAT = "Invalid command format.\nUsage: " + MESSAGE_USAGE;
    public static final String MESSAGE_NO_MATCHES = "No students found for \"%s\".";
    public static final String MESSAGE_SUCCESS_SINGULAR = "Found 1 student for \"%s\".";
    public static final String MESSAGE_SUCCESS = "Found %d students for \"%s\".";
    public static final String MESSAGE_SHOW_ALL = "Type list to show all students.";
    public static final String MESSAGE_INTERNAL_ERROR =
            "TutorFlow could not search students due to an internal error.";

    private static final Logger logger = LogsCenter.getLogger(SearchCommand.class);

    private final String keyword;

    /** Creates a search for the given non-blank keyword. */
    public SearchCommand(String keyword) {
        this.keyword = requireNonNull(keyword).strip();
        if (this.keyword.isEmpty()) {
            throw new IllegalArgumentException("Search keyword must not be blank.");
        }
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            return searchStudents(model);
        } catch (RuntimeException exception) {
            logger.log(Level.SEVERE, "Unexpected error while searching students", exception);
            throw new CommandException(MESSAGE_INTERNAL_ERROR, exception);
        }
    }

    /** Updates only the displayed student view; the stored roster is unchanged. */
    private CommandResult searchStudents(Model model) {
        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
        Predicate<Student> matchesKeyword = student -> matches(student.getName().toString(), normalizedKeyword)
                || matches(student.getPhone().toString(), normalizedKeyword)
                || matches(student.getGuardianName().toString(), normalizedKeyword)
                || matches(student.getGuardianPhone().toString(), normalizedKeyword);
        model.updateFilteredStudentList(matchesKeyword);

        int count = model.getStudentList().size();
        String feedback;
        if (count == 0) {
            feedback = String.format(MESSAGE_NO_MATCHES, keyword);
        } else if (count == 1) {
            feedback = String.format(MESSAGE_SUCCESS_SINGULAR, keyword);
        } else {
            feedback = String.format(MESSAGE_SUCCESS, count, keyword);
        }
        return new CommandResult(feedback + "\n" + MESSAGE_SHOW_ALL, false, false, true);
    }

    private static boolean matches(String value, String normalizedKeyword) {
        return value.toLowerCase(Locale.ROOT).contains(normalizedKeyword);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SearchCommand command && keyword.equals(command.keyword);
    }

    @Override
    public int hashCode() {
        return keyword.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keyword", keyword).toString();
    }
}
