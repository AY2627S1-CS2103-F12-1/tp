package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.Student;
import seedu.address.model.student.TuitionSubject;
import seedu.address.testutil.HomeworkBuilder;
import seedu.address.testutil.StudentBuilder;

public class LessonAddCommandTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void execute_success_preservesRosterAndHomework() throws Exception {
        ModelManager model = model();
        Student before = model.getStudentList().get(0);
        Student other = model.getStudentList().get(1);
        assertEquals("New lesson added: John Tan; MATH; MONDAY; 16:00-18:00",
                parser.parseCommand(command(1, "16:00", "18:00")).execute(model).getFeedbackToUser());
        Student after = model.getStudentList().get(0);
        assertEquals(2, model.getStudentList().size());
        assertEquals(other, model.getStudentList().get(1));
        assertEquals(before.getHomeworks(), after.getHomeworks());
        assertEquals(1, after.getLessons().size());
        assertEquals(after.getLessons(), after.withHomeworks(List.of()).getLessons());
        assertThrows(UnsupportedOperationException.class, () -> after.getLessons().clear());
        List<RegularLesson> mutable = new ArrayList<>(after.getLessons());
        Student copy = after.withLessons(mutable);
        mutable.clear();
        assertEquals(after, copy);
    }

    @Test
    public void execute_duplicateAndCrossStudentClash_leaveDataUnchanged() throws Exception {
        ModelManager model = model();
        parser.parseCommand(command(1, "16:00", "18:00")).execute(model);
        AddressBook before = new AddressBook(model.getAddressBook());
        assertEquals(LessonAddCommand.MESSAGE_DUPLICATE,
                assertThrows(CommandException.class, () ->
                        parser.parseCommand(command(1, "16:00", "18:00")).execute(model)).getMessage());
        assertEquals(LessonAddCommand.MESSAGE_CLASH + "John Tan; MATH; MONDAY; 16:00-18:00",
                assertThrows(CommandException.class, () ->
                        parser.parseCommand(command(2, "17:00", "19:00")).execute(model)).getMessage());
        assertEquals(before, model.getAddressBook());
        parser.parseCommand(command(2, "18:00", "19:00")).execute(model);
        assertEquals(1, model.getStudentList().get(1).getLessons().size());
    }

    @Test
    public void execute_invalidStudentOrSubject_leavesDataUnchanged() throws Exception {
        ModelManager model = model();
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(CommandException.class, () ->
                        parser.parseCommand(command(3, "16:00", "18:00")).execute(model));
        assertEquals(LessonAddCommand.MESSAGE_SUBJECT,
                assertThrows(CommandException.class, () ->
                        parser.parseCommand("lesson add 1 s/PHYSICS d/MONDAY st/16:00 et/18:00")
                        .execute(model)).getMessage());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_unexpectedFailure_reportsInternalError() {
        ModelManager failing = new ModelManager() {
            @Override
            public javafx.collections.ObservableList<Student> getStudentList() {
                throw new IllegalStateException("test failure");
            }
        };
        LessonAddCommand command = new LessonAddCommand(seedu.address.commons.core.index.Index.fromOneBased(1),
                new RegularLesson(TuitionSubject.MATH, DayOfWeek.MONDAY, LocalTime.NOON, LocalTime.of(13, 0)));
        assertEquals(LessonAddCommand.MESSAGE_INTERNAL,
                assertThrows(CommandException.class, () -> command.execute(failing)).getMessage());
    }

    private ModelManager model() {
        ModelManager model = new ModelManager();
        model.addStudent(new StudentBuilder().withHomeworks(new HomeworkBuilder().build()).build());
        model.addStudent(new StudentBuilder().withName("Mary Lim").withPhone("91234568").build());
        return model;
    }

    private String command(int index, String start, String end) {
        return "lesson add " + index + " s/MATH d/MONDAY st/" + start + " et/" + end;
    }
}
