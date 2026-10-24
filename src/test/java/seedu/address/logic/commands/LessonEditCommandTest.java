package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.Student;
import seedu.address.model.student.TuitionSubject;
import seedu.address.testutil.HomeworkBuilder;
import seedu.address.testutil.StudentBuilder;

public class LessonEditCommandTest {
    private static final RegularLesson MONDAY_MATH = new RegularLesson(TuitionSubject.MATH,
            DayOfWeek.MONDAY, LocalTime.of(16, 0), LocalTime.of(18, 0));
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void execute_unexpectedModelFailure_reportsInternalError() throws Exception {
        ModelManager failing = new ModelManager() {
            @Override
            public ReadOnlyAddressBook getAddressBook() {
                throw new IllegalStateException("test failure");
            }
        };
        var command = parser.parseCommand("lesson edit 1 d/TUESDAY");
        assertEquals(LessonEditCommand.MESSAGE_INTERNAL,
                assertThrows(CommandException.class, () -> command.execute(failing)).getMessage());
    }

    @Test
    public void execute_partialEditOverlappingOriginal_excludesSelfAndPreservesOtherData() throws Exception {
        ModelManager model = model();
        Student before = model.getStudentList().getFirst();

        parser.parseCommand("lesson edit 1 st/17:00").execute(model);

        Student after = model.getStudentList().getFirst();
        assertEquals(before.withLessons(List.of(new RegularLesson(TuitionSubject.MATH, DayOfWeek.MONDAY,
                LocalTime.of(17, 0), LocalTime.of(18, 0)))), after);
        assertEquals(before.getHomeworks(), after.getHomeworks());
        assertEquals(before.getLessons().getFirst().getEndTime(), after.getLessons().getFirst().getEndTime());
    }

    @Test
    public void execute_subjectOnlyEdit_excludesOriginalTimeslot() throws Exception {
        ModelManager model = model();
        parser.parseCommand("lesson edit 1 s/PHYSICS").execute(model);
        assertEquals("PHYSICS", model.getStudentList().getFirst().getLessons().getFirst().getSubject().toString());
    }

    @Test
    public void execute_clashWithHiddenStudent_rejectsAndPreservesData() throws Exception {
        ModelManager model = model();
        parser.parseCommand("lesson add 2 s/MATH d/MONDAY st/18:00 et/19:00").execute(model);
        parser.parseCommand("search John").execute(model);
        AddressBook before = new AddressBook(model.getAddressBook());

        assertEquals(LessonAddCommand.MESSAGE_CLASH + "Mary Lim (91234568); MATH; MONDAY; 18:00-19:00",
                assertThrows(CommandException.class, () -> parser.parseCommand("lesson edit 1 et/18:30")
                        .execute(model)).getMessage());
        assertEquals(before, model.getAddressBook());
        assertEquals(1, model.getStudentList().size());
    }

    @Test
    public void execute_multipleClashes_reportsAllInWeeklyOrder() throws Exception {
        ModelManager model = model();
        parser.parseCommand("lesson add 2 s/MATH d/MONDAY st/18:00 et/19:00").execute(model);
        parser.parseCommand("lesson add 2 s/MATH d/MONDAY st/20:00 et/21:00").execute(model);
        AddressBook before = new AddressBook(model.getAddressBook());
        assertEquals(LessonEditCommand.MESSAGE_MULTIPLE_CLASHES
                + "Mary Lim (91234568); MATH; MONDAY; 18:00-19:00\nMary Lim (91234568); MATH; MONDAY; 20:00-21:00",
                assertThrows(CommandException.class, () -> parser.parseCommand("lesson edit 1 et/20:30")
                        .execute(model)).getMessage());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_adjacentAndDifferentDayEdits_succeed() throws Exception {
        ModelManager model = model();
        parser.parseCommand("lesson add 2 s/MATH d/MONDAY st/18:00 et/19:00").execute(model);
        parser.parseCommand("lesson edit 1 st/17:00 et/18:00").execute(model);
        parser.parseCommand("lesson edit 1 d/TUESDAY st/18:00 et/19:00").execute(model);
        assertEquals(DayOfWeek.TUESDAY, model.getStudentList().getFirst().getLessons().getFirst()
                .getDayOfWeek());
    }

    @Test
    public void execute_indexUsesGlobalWeeklyOrderEvenAfterSearchAndSort() throws Exception {
        ModelManager model = model();
        parser.parseCommand("lesson add 2 s/MATH d/MONDAY st/09:00 et/10:00").execute(model);
        parser.parseCommand("search John").execute(model);
        parser.parseCommand("sort by/level o/desc").execute(model);
        parser.parseCommand("lesson edit 1 d/SUNDAY").execute(model);
        assertEquals(DayOfWeek.SUNDAY, model.getAddressBook().getStudentList().get(1).getLessons()
                .getFirst().getDayOfWeek());
        assertEquals(MONDAY_MATH, model.getStudentList().getFirst().getLessons().getFirst());
    }

    @Test
    public void execute_invalidEdits_leaveModelUnchanged() throws Exception {
        ModelManager model = model();
        parser.parseCommand("lesson add 1 s/MATH d/TUESDAY st/16:00 et/18:00").execute(model);
        AddressBook before = new AddressBook(model.getAddressBook());
        String[] invalid = {"lesson edit 1 s/CHEMISTRY", "lesson edit 1 d/TUESDAY", "lesson edit 1 st/18:00",
            "lesson edit 1 et/15:00", "lesson edit 1 s/MATH", "lesson edit 99 d/SUNDAY"};
        for (String command : invalid) {
            assertThrows(CommandException.class, () -> parser.parseCommand(command).execute(model), command);
            assertEquals(before, model.getAddressBook(), command);
        }
        assertEquals(Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX,
                assertThrows(CommandException.class, () -> parser.parseCommand("lesson edit 99 d/SUNDAY")
                        .execute(model)).getMessage());
        assertEquals(LessonEditCommand.MESSAGE_NO_LESSONS,
                assertThrows(CommandException.class, () -> parser.parseCommand("lesson edit 1 d/SUNDAY")
                        .execute(new ModelManager())).getMessage());
    }

    private ModelManager model() {
        ModelManager model = new ModelManager();
        model.addStudent(new StudentBuilder().withSubjects("MATH, PHYSICS")
                .withHomeworks(new HomeworkBuilder().build()).withLessons(MONDAY_MATH).build());
        model.addStudent(new StudentBuilder().withName("Mary Lim").withPhone("91234568").build());
        return model;
    }
}
