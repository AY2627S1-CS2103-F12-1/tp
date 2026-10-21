package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.model.student.Student;

/**
 * Lists the homework of every student in the roster that is due from today to a given number of days after today,
 * both inclusive. The homework is sorted by due date, earliest first, with ties in roster order and then in
 * insertion order. The full roster is displayed again, as after {@code list}, but the stored data is not changed.
 */
public class HomeworkUpcomingCommand extends Command {
    public static final String MESSAGE_USAGE = "homework|hw upcoming|up [due/DAYS]";
    public static final String MESSAGE_SUCCESS = "Found %d homework %s due from %s to %s.";
    public static final String MESSAGE_HOMEWORK_LINE = "\nStudent %d: %s; %s; %s; Due: %s; Score: %s; Status: %s";
    public static final String MESSAGE_NO_HOMEWORK = "No homework due from %s to %s.";
    public static final String MESSAGE_INTERNAL_ERROR =
            "TutorFlow could not display upcoming homework due to an internal error.";

    private static final Logger logger = LogsCenter.getLogger(HomeworkUpcomingCommand.class);

    private final LocalDate today;
    private final int days;

    /**
     * Creates a command that lists the homework due from {@code today} to {@code days} days after it.
     *
     * @param today First due date to list.
     * @param days Number of days after {@code today} up to which homework is listed; not negative.
     */
    public HomeworkUpcomingCommand(LocalDate today, int days) {
        this.today = requireNonNull(today);
        this.days = days;
    }

    /**
     * {@inheritDoc}
     * The full roster is displayed first, so that each {@code STUDENT_INDEX} in the result matches the student list.
     * An unexpected error is logged and reported as an internal error.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        model.updateFilteredStudentList(Model.PREDICATE_SHOW_ALL_STUDENTS);
        try {
            return new CommandResult(listUpcomingHomework(model));
        } catch (RuntimeException exception) {
            logger.log(Level.SEVERE, "Unexpected error while listing upcoming homework", exception);
            throw new CommandException(MESSAGE_INTERNAL_ERROR, exception);
        }
    }

    /**
     * Returns the summary line and one line per homework due in the window, or a message saying there is none.
     * Every student in the roster is searched, not only the students that are displayed. Each line starts with the
     * student's position in the roster, which is the student's {@code STUDENT_INDEX}, so that students with the
     * same name can be told apart.
     */
    private String listUpcomingHomework(Model model) {
        LocalDate lastDay = today.plusDays(days);
        List<Student> students = model.getAddressBook().getStudentList();

        // Each entry maps the zero-based roster position of a student to one homework of that student
        // Stream#sorted is stable, so homework due on the same date stays in roster order, then insertion order
        List<Map.Entry<Integer, Homework>> dueHomeworks = IntStream.range(0, students.size()).boxed()
                .flatMap(position -> students.get(position).getHomeworks().stream()
                        .map(homework -> Map.entry(position, homework)))
                .filter(entry -> entry.getValue().isUpcoming(today)
                        && !entry.getValue().getDueDate().getDate().isAfter(lastDay))
                .sorted(Comparator.comparing(entry -> entry.getValue().getDueDate().getDate()))
                .toList();
        if (dueHomeworks.isEmpty()) {
            return String.format(MESSAGE_NO_HOMEWORK, today, lastDay);
        }

        String recordWord = dueHomeworks.size() == 1
                ? HomeworkListCommand.MESSAGE_RECORD_SINGULAR
                : HomeworkListCommand.MESSAGE_RECORD_PLURAL;
        StringBuilder message = new StringBuilder(String.format(MESSAGE_SUCCESS, dueHomeworks.size(), recordWord,
                today, lastDay));
        for (Map.Entry<Integer, Homework> entry : dueHomeworks) {
            int position = entry.getKey();
            Homework homework = entry.getValue();
            message.append(String.format(MESSAGE_HOMEWORK_LINE, Index.fromZeroBased(position).getOneBased(),
                    students.get(position).getName(), homework.getTitle(), homework.getSubject(),
                    homework.getDueDate(), Messages.formatScore(homework), homework.getStatus()));
        }
        return message.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof HomeworkUpcomingCommand otherCommand)) {
            return false;
        }

        return today.equals(otherCommand.today)
                && days == otherCommand.days;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("today", today)
                .add("days", days)
                .toString();
    }
}
