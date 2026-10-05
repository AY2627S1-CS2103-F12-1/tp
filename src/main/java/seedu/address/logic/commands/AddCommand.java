package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.StudentAddParser;
import seedu.address.model.Model;
import seedu.address.model.student.Student;

/**
 * Adds a student to the student roster.
 */
public class AddCommand extends Command {
    public static final String COMMAND_WORD = "add";
    public static final String MESSAGE_USAGE = StudentAddParser.USAGE;
    public static final String MESSAGE_SUCCESS = "New student added: %s";
    public static final String MESSAGE_DUPLICATE_STUDENT = "This student already exists.";

    private final Student studentToAdd;

    /**
     * Creates a command that adds the specified student.
     */
    public AddCommand(Student student) {
        studentToAdd = requireNonNull(student);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (model.hasStudent(studentToAdd)) {
            throw new CommandException(MESSAGE_DUPLICATE_STUDENT);
        }

        model.addStudent(studentToAdd);
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(studentToAdd)));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AddCommand command && studentToAdd.equals(command.studentToAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("studentToAdd", studentToAdd).toString();
    }
}
