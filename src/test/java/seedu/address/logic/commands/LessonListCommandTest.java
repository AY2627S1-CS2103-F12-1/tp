package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalRegularLessons.MONDAY_MATH;
import static seedu.address.testutil.TypicalRegularLessons.SATURDAY_CHEMISTRY;
import static seedu.address.testutil.TypicalRegularLessons.TUESDAY_PHYSICS;
import static seedu.address.testutil.TypicalStudents.getModelWithStudents;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.Student;
import seedu.address.model.student.TuitionSubject;
import seedu.address.testutil.StudentBuilder;

public class LessonListCommandTest {
    @Test
    public void execute_lessons_listsInWeeklyOrder() {
        RegularLesson mondayEarlier = new RegularLesson(TuitionSubject.PHYSICS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        Student zoe = new StudentBuilder().withName("Zoe Tan").withSubjects("MATH, PHYSICS")
                .withLessons(MONDAY_MATH, mondayEarlier).build();
        Student amy = new StudentBuilder().withName("Amy Lim").withSubjects("PHYSICS, CHEMISTRY")
                .withLessons(TUESDAY_PHYSICS, SATURDAY_CHEMISTRY).build();
        Model model = getModelWithStudents(List.of(amy, zoe));
        Model expectedModel = getModelWithStudents(List.of(amy, zoe));

        String expectedMessage = "Listed 4 regular lessons.\n"
                + "1. Zoe Tan; PHYSICS; MON; 09:00-10:00\n"
                + "2. Zoe Tan; MATH; MON; 17:00-18:30\n"
                + "3. Amy Lim; PHYSICS; TUE; 19:00-20:30\n"
                + "4. Amy Lim; CHEMISTRY; SAT; 15:00-16:30";

        assertCommandSuccess(new LessonListCommand(), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_sameDayAndStartTime_ordersByStudentName() {
        RegularLesson sameTimeLesson = new RegularLesson(TuitionSubject.MATH, DayOfWeek.MONDAY,
                LocalTime.of(17, 0), LocalTime.of(18, 0));
        Student zoe = new StudentBuilder().withName("Zoe Tan").withLessons(MONDAY_MATH).build();
        Student amy = new StudentBuilder().withName("amy Lim").withLessons(sameTimeLesson).build();
        Model model = getModelWithStudents(List.of(zoe, amy));

        String expectedMessage = "Listed 2 regular lessons.\n"
                + "1. amy Lim; MATH; MON; 17:00-18:00\n"
                + "2. Zoe Tan; MATH; MON; 17:00-18:30";

        assertCommandSuccess(new LessonListCommand(), model, expectedMessage,
                getModelWithStudents(List.of(zoe, amy)));
    }

    @Test
    public void execute_noLessons_returnsSuccessfulEmptyResult() {
        Model model = getModelWithStudents(List.of(new StudentBuilder().build()));
        assertCommandSuccess(new LessonListCommand(), model, LessonListCommand.MESSAGE_EMPTY,
                getModelWithStudents(List.of(new StudentBuilder().build())));
    }

    @Test
    public void execute_unexpectedError_reportsInternalErrorWithoutChangingModel() {
        Model failingModel = new ModelManager() {
            @Override
            public ObservableList<Student> getStudentList() {
                throw new IllegalStateException("dummy internal error");
            }
        };

        assertThrows(CommandException.class, LessonListCommand.MESSAGE_INTERNAL_ERROR, () ->
                new LessonListCommand().execute(failingModel));
        assertEquals(new ModelManager(), failingModel);
    }

    @Test
    public void equals() {
        LessonListCommand command = new LessonListCommand();

        assertTrue(command.equals(command));
        assertTrue(command.equals(new LessonListCommand()));
        assertFalse(command.equals(null));
        assertFalse(command.equals(1));
    }
}
