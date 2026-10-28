package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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
 * Lists the homework of a student identified by its displayed index: by default only the homework due today or
 * later, or all of it. The homework is sorted by due date, earliest first, with ties in insertion order, and each
 * record is numbered with its one-based index in insertion order. When homework due before today is left
 * out, the summary line says how many records are not shown and how to list them. The model is not changed.
 */
public class HomeworkListCommand extends Command {
    public static final String MESSAGE_USAGE = "homework|hw list|ls STUDENT_INDEX [all]";
    /**
     * Summary line, with the note about records left out (empty if none) before its period,
     * then the header.
     */
    public static final String MESSAGE_SUCCESS = "Listed %d upcoming homework %s for %s%s.\nHomework for %s:";
    public static final String MESSAGE_SUCCESS_ALL = "Listed %d homework %s for %s.\nHomework for %s:";
    public static final String MESSAGE_HOMEWORK_LINE = "\n%d. %s (%s) - due %s - score %s - status %s";
    public static final String MESSAGE_NO_HOMEWORK = "No homework found for %s.";
    public static final String MESSAGE_NO_UPCOMING_HOMEWORK = "No upcoming homework found for %s%s.";
    public static final String MESSAGE_HIDDEN_NOTE_SINGULAR =
            " (1 record due before today not shown; use homework list %d all to see it)";
    public static final String MESSAGE_HIDDEN_NOTE_PLURAL =
            " (%d records due before today not shown; use homework list %d all to see them)";
    public static final String MESSAGE_RECORD_SINGULAR = "record";
    public static final String MESSAGE_RECORD_PLURAL = "records";
    public static final String MESSAGE_INTERNAL_ERROR =
            "TutorFlow could not display the student's homework due to an internal error.";

    private static final Logger logger = LogsCenter.getLogger(HomeworkListCommand.class);

    private final Index studentIndex;
    private final LocalDate today;
    private final boolean isAllShown;

    /**
     * Creates a command that lists the homework of the student at {@code studentIndex}.
     *
     * @param studentIndex Index of the student in the displayed student list.
     * @param today Date from which homework counts as upcoming.
     * @param isAllShown Whether homework due before {@code today} is listed too.
     */
    public HomeworkListCommand(Index studentIndex, LocalDate today, boolean isAllShown) {
        requireAllNonNull(studentIndex, today);
        this.studentIndex = studentIndex;
        this.today = today;
        this.isAllShown = isAllShown;
    }

    /**
     * {@inheritDoc}
     * An unexpected error is logged and reported as an internal error.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            return listHomework(model);
        } catch (RuntimeException exception) {
            logger.log(Level.SEVERE, "Unexpected error while listing homework", exception);
            throw new CommandException(MESSAGE_INTERNAL_ERROR, exception);
        }
    }

    /**
     * Returns the result listing the homework of the selected student.
     */
    private CommandResult listHomework(Model model) throws CommandException {
        Student student = HomeworkCommandUtil.getDisplayedStudent(model, studentIndex);
        return new CommandResult(formatHomeworkList(student, studentIndex, today, isAllShown));
    }

    /**
     * Returns the homework of {@code student} in the format this command shows it, so that other homework commands
     * can show the same list.
     *
     * @param student Student whose homework is listed.
     * @param studentIndex Index of {@code student} in the displayed student list, named in the command that lists
     *     the homework left out.
     * @param today Date from which homework counts as upcoming.
     * @param isAllShown Whether homework due before {@code today} is listed too.
     * @return The summary line, the header and one line per listed homework, or a message saying there is none.
     */
    static String formatHomeworkList(Student student, Index studentIndex, LocalDate today, boolean isAllShown) {
        List<Homework> homeworks = student.getHomeworks();
        if (homeworks.isEmpty()) {
            return String.format(MESSAGE_NO_HOMEWORK, student.getName());
        }

        // Stream#sorted is stable, so homework due on the same date stays in insertion order
        List<Integer> listedIndices = IntStream.range(0, homeworks.size())
                .filter(i -> isAllShown || homeworks.get(i).isUpcoming(today))
                .boxed()
                .sorted(Comparator.comparing(i -> homeworks.get(i).getDueDate().getDate()))
                .toList();
        String hiddenNote = formatHiddenNote(homeworks.size() - listedIndices.size(), studentIndex);
        if (listedIndices.isEmpty()) {
            return String.format(MESSAGE_NO_UPCOMING_HOMEWORK, student.getName(), hiddenNote);
        }

        String recordWord = getRecordWord(listedIndices.size());
        StringBuilder message = new StringBuilder(isAllShown
                ? String.format(MESSAGE_SUCCESS_ALL, listedIndices.size(), recordWord, student.getName(),
                        student.getName())
                : String.format(MESSAGE_SUCCESS, listedIndices.size(), recordWord, student.getName(), hiddenNote,
                        student.getName()));
        for (int zeroBasedIndex : listedIndices) {
            message.append(formatHomeworkLine(zeroBasedIndex + 1, homeworks.get(zeroBasedIndex)));
        }
        return message.toString();
    }

    /**
     * Returns the line, starting with a line break, that this command shows for {@code homework} numbered
     * {@code oneBasedIndex}: its title, subject, due date, score and status.
     */
    static String formatHomeworkLine(int oneBasedIndex, Homework homework) {
        // Locale.ROOT prints the index in ASCII digits, which the index parsers accept, in every locale
        return String.format(Locale.ROOT, MESSAGE_HOMEWORK_LINE, oneBasedIndex, homework.getTitle(),
                homework.getSubject(), homework.getDueDate(), Messages.formatScore(homework), homework.getStatus());
    }

    /**
     * Returns the note, starting with a space, that {@code hiddenCount} records due before today are not
     * shown and how to list them, or an empty string if {@code hiddenCount} is 0.
     */
    private static String formatHiddenNote(int hiddenCount, Index studentIndex) {
        if (hiddenCount == 0) {
            return "";
        }
        // Locale.ROOT prints the student index in ASCII digits, which the index parsers accept, in every locale
        return hiddenCount == 1
                ? String.format(Locale.ROOT, MESSAGE_HIDDEN_NOTE_SINGULAR, studentIndex.getOneBased())
                : String.format(Locale.ROOT, MESSAGE_HIDDEN_NOTE_PLURAL, hiddenCount, studentIndex.getOneBased());
    }

    /**
     * Returns the word for homework records that follows {@code count} in a summary line.
     */
    static String getRecordWord(int count) {
        return count == 1 ? MESSAGE_RECORD_SINGULAR : MESSAGE_RECORD_PLURAL;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof HomeworkListCommand otherCommand)) {
            return false;
        }

        return studentIndex.equals(otherCommand.studentIndex)
                && today.equals(otherCommand.today)
                && isAllShown == otherCommand.isAllShown;
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex.getZeroBased(), today, isAllShown);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .add("today", today)
                .add("isAllShown", isAllShown)
                .toString();
    }
}
