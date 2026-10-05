package seedu.address.ui;

import java.util.List;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.HomeworkAddCommand;
import seedu.address.logic.commands.HomeworkDeleteCommand;
import seedu.address.logic.commands.HomeworkListCommand;
import seedu.address.logic.parser.HomeworkCommandParser;
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
    public static final String SECTION_GENERAL = "General";
    public static final String FORMAT_NOTE = "In each format, items separated by | are alternatives; type any one"
            + " of them.";

    private static final List<Section> SECTIONS = List.of(
            new Section(SECTION_STUDENTS, List.of(
                    new Entry(AddCommand.COMMAND_WORD, AddCommand.MESSAGE_USAGE,
                            "Adds a student. " + AcademicLevel.MESSAGE_CONSTRAINTS + " "
                                    + TuitionSubjects.MESSAGE_CONSTRAINTS,
                            "add n/John Tan l/S3 s/MATH, PHYSICS p/91234567 gn/Mary Tan gp/98765432"))),
            new Section(SECTION_HOMEWORK, List.of(
                    new Entry(HomeworkCommandParser.COMMAND_WORD + " " + HomeworkCommandParser.SUBCOMMAND_ADD,
                            HomeworkAddCommand.MESSAGE_USAGE,
                            "Adds homework to the student at STUDENT_INDEX. " + TuitionSubject.MESSAGE_CONSTRAINTS
                                    + " " + DueDate.MESSAGE_CONSTRAINTS + " An MM-DD date is in this year, or in"
                                    + " the next year if it has passed.",
                            "hw add 1 t/Algebra worksheet s/MATH due/2026-10-15"),
                    new Entry(HomeworkCommandParser.COMMAND_WORD + " " + HomeworkCommandParser.SUBCOMMAND_LIST,
                            HomeworkListCommand.MESSAGE_USAGE,
                            "Lists the homework of the student at STUDENT_INDEX, numbered from 1.",
                            "hw ls 1"),
                    new Entry(HomeworkCommandParser.COMMAND_WORD + " " + HomeworkCommandParser.SUBCOMMAND_DELETE,
                            HomeworkDeleteCommand.MESSAGE_USAGE,
                            "Deletes homework number HOMEWORK_INDEX, as numbered by homework list, from the student"
                                    + " at STUDENT_INDEX.",
                            "hw del 1 2"))),
            new Section(SECTION_GENERAL, List.of(
                    new Entry(HelpCommand.COMMAND_WORD, HelpCommand.COMMAND_WORD,
                            "Opens this help window. F1 also opens it, and Esc closes it.", HelpCommand.COMMAND_WORD),
                    new Entry(ClearCommand.COMMAND_WORD, ClearCommand.COMMAND_WORD,
                            "Deletes all students and their homework. This cannot be undone.",
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
