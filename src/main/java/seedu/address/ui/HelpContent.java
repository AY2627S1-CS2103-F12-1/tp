package seedu.address.ui;

import java.util.List;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.HomeworkAddCommand;
import seedu.address.logic.commands.HomeworkDeleteCommand;
import seedu.address.logic.commands.HomeworkEditCommand;
import seedu.address.logic.commands.HomeworkListCommand;
import seedu.address.logic.commands.HomeworkMarksCommand;
import seedu.address.logic.commands.HomeworkUpcomingCommand;
import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.logic.commands.LessonDeleteCommand;
import seedu.address.logic.commands.LessonListCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.SearchCommand;
import seedu.address.logic.parser.HomeworkCommandParser;
import seedu.address.logic.parser.LessonCommandParser;
import seedu.address.model.homework.DueDate;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.TuitionSubject;
import seedu.address.model.student.TuitionSubjects;

/**
 * Lists the commands shown in the help window, grouped into sections.
 * Each command format is taken from the usage constant of its command, so the help window shows the same format
 * as the error messages of the parsers.
 */
public final class HelpContent {
    public static final String SECTION_STUDENTS = "Students";
    public static final String SECTION_HOMEWORK = "Homework";
    public static final String SECTION_LESSONS = "Lessons";
    public static final String SECTION_GENERAL = "General";
    public static final String FORMAT_NOTE = "In each format, items separated by | are alternatives; type any one"
            + " of them.";

    private static final List<Section> SECTIONS = List.of(
            new Section(SECTION_STUDENTS, List.of(
                    new Entry(AddCommand.COMMAND_WORD, AddCommand.MESSAGE_USAGE,
                            "Adds a student. " + AcademicLevel.MESSAGE_CONSTRAINTS + " "
                                    + TuitionSubjects.MESSAGE_CONSTRAINTS,
                            "add n/John Tan l/S3 s/MATH, PHYSICS p/91234567 gn/Mary Tan gp/98765432"),
                    new Entry(DeleteCommand.COMMAND_WORD, DeleteCommand.MESSAGE_USAGE,
                            "Deletes the student at INDEX and all of their homework and lessons.", "delete 1"),
                    new Entry(ListCommand.COMMAND_WORD, ListCommand.MESSAGE_USAGE,
                            "Shows all students in roster order, starting from the top.", ListCommand.COMMAND_WORD),
                    new Entry(SearchCommand.COMMAND_WORD, SearchCommand.MESSAGE_USAGE,
                            "Finds students by student or guardian name or phone number.", "search Tan"),
                    new Entry(FilterCommand.COMMAND_WORD, FilterCommand.MESSAGE_USAGE,
                            "Shows students matching every supplied filter. Supply at least one filter. "
                                    + AcademicLevel.MESSAGE_CONSTRAINTS + " " + TuitionSubject.MESSAGE_CONSTRAINTS
                                    + " Type list to show all students again.", "filter l/S4 s/Math"))),
            new Section(SECTION_HOMEWORK, List.of(
                    new Entry(HomeworkCommandParser.COMMAND_WORD + " " + HomeworkCommandParser.SUBCOMMAND_ADD,
                            HomeworkAddCommand.MESSAGE_USAGE,
                            "Adds homework to the student at STUDENT_INDEX. " + TuitionSubject.MESSAGE_CONSTRAINTS
                                    + " " + DueDate.MESSAGE_CONSTRAINTS + " An MM-DD date is in this year, or in"
                                    + " the next year if it has passed.",
                            "hw add 1 t/Algebra worksheet s/MATH due/2026-10-15"),
                    new Entry(HomeworkCommandParser.COMMAND_WORD + " " + HomeworkCommandParser.SUBCOMMAND_LIST,
                            HomeworkListCommand.MESSAGE_USAGE,
                            "Lists the homework of the student at STUDENT_INDEX that is due today or later, earliest"
                                    + " first, with its score. Add all to list overdue homework too. Each homework"
                                    + " keeps its number, the HOMEWORK_INDEX used by the other homework commands.",
                            "hw ls 1"),
                    new Entry(HomeworkCommandParser.COMMAND_WORD + " " + HomeworkCommandParser.SUBCOMMAND_UPCOMING,
                            HomeworkUpcomingCommand.MESSAGE_USAGE,
                            "Lists the homework of all students that is due from today to DAYS days later, earliest"
                                    + " first, with its score. Each line starts with the STUDENT_INDEX of the"
                                    + " student. DAYS is a number of days from 0 to 365, not a date, and is 7 if"
                                    + " not given. The lines are not numbered by HOMEWORK_INDEX; use homework list"
                                    + " STUDENT_INDEX to find it.",
                            "hw up due/3"),
                    new Entry(HomeworkCommandParser.COMMAND_WORD + " " + HomeworkCommandParser.SUBCOMMAND_EDIT,
                            HomeworkEditCommand.MESSAGE_USAGE,
                            "Changes the title, subject or due date of homework number HOMEWORK_INDEX, as numbered"
                                    + " by homework list, of the student at STUDENT_INDEX. Give at least one of them;"
                                    + " the other details and the score stay unchanged.",
                            "hw e 1 2 t/Complete algebra questions 1-10"),
                    new Entry(HomeworkCommandParser.COMMAND_WORD + " " + HomeworkCommandParser.SUBCOMMAND_MARKS,
                            HomeworkMarksCommand.MESSAGE_USAGE,
                            "Records the marks of homework number HOMEWORK_INDEX of the student at STUDENT_INDEX,"
                                    + " replacing any marks recorded before. SCORE is a whole number, e.g. 17, or"
                                    + " MARKS_OBTAINED/TOTAL_MARKS, e.g. 5/10, with whole numbers and marks obtained"
                                    + " from 0 to the total. Leave SCORE empty to clear the recorded marks.",
                            "hw m 1 2 score/5/10"),
                    new Entry(HomeworkCommandParser.COMMAND_WORD + " " + HomeworkCommandParser.SUBCOMMAND_DELETE,
                            HomeworkDeleteCommand.MESSAGE_USAGE,
                            "Deletes homework number HOMEWORK_INDEX, as numbered by homework list, from the student"
                                    + " at STUDENT_INDEX.",
                            "hw del 1 2"))),
            new Section(SECTION_LESSONS, List.of(
                    new Entry("lesson add", LessonAddCommand.MESSAGE_USAGE,
                            "Adds a weekly lesson to a student. Use a full weekday name and 24-hour HH:mm times.",
                            "lesson add 1 s/MATH d/MONDAY st/16:00 et/18:00"),
                    new Entry(LessonCommandParser.COMMAND_WORD + " " + LessonCommandParser.SUBCOMMAND_LIST,
                            LessonListCommand.MESSAGE_USAGE,
                            "Lists every regular lesson in weekly chronological order.",
                            LessonListCommand.MESSAGE_USAGE),
                    new Entry(LessonCommandParser.COMMAND_WORD + " " + LessonCommandParser.SUBCOMMAND_DELETE,
                            LessonDeleteCommand.MESSAGE_USAGE,
                            "Deletes the lesson numbered by lesson list.",
                            "lesson delete 1"))),
            new Section(SECTION_GENERAL, List.of(
                    new Entry(HelpCommand.COMMAND_WORD, HelpCommand.COMMAND_WORD,
                            "Opens this help window. F1 also opens it, and Esc closes it.", HelpCommand.COMMAND_WORD),
                    new Entry(ClearCommand.COMMAND_WORD, ClearCommand.COMMAND_WORD,
                            "Deletes all students, homework and lessons. This cannot be undone.",
                            ClearCommand.COMMAND_WORD),
                    new Entry(ExitCommand.COMMAND_WORD, ExitCommand.COMMAND_WORD,
                            "Exits the app.", ExitCommand.COMMAND_WORD))));

    private HelpContent() {
    }

    /**
     * Returns the help sections in display order.
     */
    public static List<Section> getSections() {
        return SECTIONS;
    }

    /**
     * Represents a titled group of commands in the help window.
     */
    public record Section(String title, List<Entry> entries) {
    }

    /**
     * Represents one command in the help window.
     *
     * @param commandWord Words that start the command, e.g. {@code homework add}.
     * @param format Command format with placeholders in upper case.
     * @param description What the command does.
     * @param example A complete command that the app accepts.
     */
    public record Entry(String commandWord, String format, String description, String example) {
    }
}
