package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.HomeworkFilterCommand;
import seedu.address.logic.commands.HomeworkStatusCommand;
import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.logic.commands.LessonEditCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.TuitionSubject;

public class CommandIndexValidationTest {
    private static final Index MAXIMUM_INDEX = Index.fromOneBased(Integer.MAX_VALUE);
    private static final Index FIRST_INDEX = Index.fromOneBased(1);
    private static final List<IndexCase> CASES = List.of(
            new IndexCase("hw filter %s", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX,
                    Messages.MESSAGE_STUDENT_INDEX_TOO_LARGE,
                    new HomeworkFilterCommand(Optional.of(MAXIMUM_INDEX), Optional.empty(), Optional.empty(),
                            Optional.empty())),
            new IndexCase("hw status %s 1 status/COMPLETED", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX,
                    Messages.MESSAGE_STUDENT_INDEX_TOO_LARGE,
                    new HomeworkStatusCommand(MAXIMUM_INDEX, FIRST_INDEX, HomeworkStatus.COMPLETED)),
            new IndexCase("hw status 1 %s status/COMPLETED", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX,
                    Messages.MESSAGE_HOMEWORK_INDEX_TOO_LARGE,
                    new HomeworkStatusCommand(FIRST_INDEX, MAXIMUM_INDEX, HomeworkStatus.COMPLETED)),
            new IndexCase("lesson edit %s d/MONDAY", Messages.MESSAGE_INVALID_LESSON_INDEX_SYNTAX,
                    Messages.MESSAGE_LESSON_INDEX_TOO_LARGE,
                    new LessonEditCommand(MAXIMUM_INDEX,
                            new LessonEditCommand.EditLessonDescriptor(null, DayOfWeek.MONDAY, null, null))),
            new IndexCase("lesson add %s s/MATH d/MONDAY st/16:00 et/18:00",
                    Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX, Messages.MESSAGE_STUDENT_INDEX_TOO_LARGE,
                    new LessonAddCommand(MAXIMUM_INDEX, new RegularLesson(TuitionSubject.MATH, DayOfWeek.MONDAY,
                            LocalTime.of(16, 0), LocalTime.of(18, 0)))));

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_maximumIndex_preservesTargetIndex() throws Exception {
        for (IndexCase indexCase : CASES) {
            String command = String.format(indexCase.format(), "2147483647");
            assertEquals(indexCase.maximumCommand(), parser.parseCommand(command), command);
        }
    }

    @Test
    public void parse_positiveIndexOverflow_reportsSupportedRange() {
        for (IndexCase indexCase : CASES) {
            for (String index : List.of("2147483648", "999999999999999999999")) {
                String command = String.format(indexCase.format(), index);
                assertEquals(indexCase.rangeMessage(),
                        assertThrows(ParseException.class, () -> parser.parseCommand(command)).getMessage(), command);
            }
        }
    }

    @Test
    public void parse_invalidIndexSyntax_keepsSyntaxError() {
        for (IndexCase indexCase : CASES) {
            for (String index : List.of("0", "01", "02147483648", "-2147483648", "+1", "1.0", "one")) {
                String command = String.format(indexCase.format(), index);
                assertEquals(indexCase.syntaxMessage(),
                        assertThrows(ParseException.class, () -> parser.parseCommand(command)).getMessage(), command);
            }
        }
    }

    @Test
    public void parse_lessonAddMissingFieldAndOverflow_reportsMissingFieldFirst() {
        String command = "lesson add 2147483648 s/MATH d/MONDAY st/16:00";
        assertEquals(String.format(LessonAddCommandParser.MESSAGE_MISSING_PREFIXES, "et/"),
                assertThrows(ParseException.class, () -> parser.parseCommand(command)).getMessage());
    }

    @Test
    public void parse_statusStudentOverflowAndHomeworkSyntax_reportsStudentRangeFirst() {
        String command = "hw status 2147483648 0 status/COMPLETED";
        assertEquals(Messages.MESSAGE_STUDENT_INDEX_TOO_LARGE,
                assertThrows(ParseException.class, () -> parser.parseCommand(command)).getMessage());
    }

    private record IndexCase(String format, String syntaxMessage, String rangeMessage, Command maximumCommand) {
    }
}
