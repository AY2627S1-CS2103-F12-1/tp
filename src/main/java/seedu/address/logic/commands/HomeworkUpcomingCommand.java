package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import seedu.address.commons.core.LogsCenter;
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
     * @throws IllegalArgumentException If {@code days} is negative.
     */
    public HomeworkUpcomingCommand(LocalDate today, int days) {
        requireNonNull(today);
        if (days < 0) {
            throw new IllegalArgumentException("days must not be negative: " + days);
        }
        this.today = today;
        this.days = days;
    }

    /**
     * {@inheritDoc}
     * The full roster is then displayed, so that each {@code STUDENT_INDEX} in the result matches the student list.
     * An unexpected error is logged and reported as an internal error, leaving the displayed students unchanged.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            String message = listUpcomingHomework(model);
            model.updateFilteredStudentList(Model.PREDICATE_SHOW_ALL_STUDENTS);
            return new CommandResult(message);
        } catch (RuntimeException exception) {
            logger.log(Level.SEVERE, "Unexpected error while listing upcoming homework", exception);
            throw new CommandException(MESSAGE_INTERNAL_ERROR, exception);
        }
    }

    /**
     * Returns the summary line and one line per homework due in the window, or a message saying there is none.
     * Every student in the roster is searched, not only the students that are displayed. Each line starts with the
     * student's position in the roster, which is the student's {@code STUDENT_INDEX}, and the student's name and
     * phone, as in the other homework messages.
     */
    private String listUpcomingHomework(Model model) {
        LocalDate lastDay = today.plusDays(days);
        List<Student> students = model.getAddressBook().getStudentList();

        // Stream#sorted is stable, so homework due on the same date stays in roster order, then insertion order
        List<DueHomework> dueHomeworks = IntStream.range(0, students.size()).boxed()
                .flatMap(position -> students.get(position).getHomeworks().stream()
                        .map(homework -> new DueHomework(position, homework)))
                .filter(dueHomework -> dueHomework.homework().isUpcoming(today)
                        && !dueHomework.homework().getDueDate().getDate().isAfter(lastDay))
                .sorted(Comparator.comparing(dueHomework -> dueHomework.homework().getDueDate().getDate()))
                .toList();
        if (dueHomeworks.isEmpty()) {
            return String.format(MESSAGE_NO_HOMEWORK, today, lastDay);
        }

        StringBuilder message = new StringBuilder(String.format(MESSAGE_SUCCESS, dueHomeworks.size(),
                HomeworkListCommand.getRecordWord(dueHomeworks.size()), today, lastDay));
        for (DueHomework dueHomework : dueHomeworks) {
            int position = dueHomework.rosterPosition();
            Homework homework = dueHomework.homework();
            // Locale.ROOT prints the index in ASCII digits, which the index parsers accept, in every locale
            message.append(String.format(Locale.ROOT, MESSAGE_HOMEWORK_LINE, position + 1,
                    Messages.formatLessonOwner(students.get(position)), homework.getTitle(), homework.getSubject(),
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
    public int hashCode() {
        return Objects.hash(today, days);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("today", today)
                .add("days", days)
                .toString();
    }

    /**
     * Holds one homework due in the window and the zero-based roster position of its student.
     */
    private record DueHomework(int rosterPosition, Homework homework) {
    }
}
