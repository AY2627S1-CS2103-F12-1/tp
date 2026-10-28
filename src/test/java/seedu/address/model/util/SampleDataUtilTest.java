package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalStudents.getModelWithStudents;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.RegularLesson;
import seedu.address.model.student.Student;

public class SampleDataUtilTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-05T00:00:00Z"), ZoneOffset.UTC);
    /** Date of the documented {@code homework upcoming} results. */
    private static final Clock UPCOMING_EXAMPLE_CLOCK =
            Clock.fixed(Instant.parse("2026-10-17T00:00:00Z"), ZoneOffset.UTC);

    private final Student[] sampleStudents = SampleDataUtil.getSampleStudents();

    @Test
    public void getSampleStudents_nonEmpty() {
        assertTrue(sampleStudents.length > 0);
    }

    @Test
    public void getSampleStudents_noDuplicateStudents() {
        for (int i = 0; i < sampleStudents.length; i++) {
            for (int j = i + 1; j < sampleStudents.length; j++) {
                assertFalse(sampleStudents[i].isSameStudent(sampleStudents[j]),
                        sampleStudents[i].getName() + " and " + sampleStudents[j].getName());
            }
        }
    }

    @Test
    public void getSampleStudents_homeworkSubjectsTakenByStudent() {
        for (Student student : sampleStudents) {
            for (Homework homework : student.getHomeworks()) {
                assertTrue(student.getSubjects().getSubjects().contains(homework.getSubject()),
                        student.getName() + ": " + homework.getTitle());
            }
        }
    }

    @Test
    public void getSampleStudents_noDuplicateHomeworkPerStudent() {
        for (Student student : sampleStudents) {
            List<Homework> homeworks = student.getHomeworks();
            for (int i = 0; i < homeworks.size(); i++) {
                for (int j = i + 1; j < homeworks.size(); j++) {
                    assertFalse(homeworks.get(i).isSameHomework(homeworks.get(j)),
                            student.getName() + ": " + homeworks.get(i).getTitle());
                }
            }
        }
    }

    @Test
    public void getSampleStudents_someStudentWithoutHomework() {
        assertTrue(Arrays.stream(sampleStudents).anyMatch(student -> student.getHomeworks().isEmpty()));
    }

    @Test
    public void getSampleStudents_regularLessonSubjectsTakenByStudent() {
        for (Student student : sampleStudents) {
            for (RegularLesson lesson : student.getLessons()) {
                assertTrue(student.getSubjects().getSubjects().contains(lesson.getSubject()),
                        student.getName() + ": " + lesson.getSubject());
            }
        }
    }

    @Test
    public void getSampleAddressBook_holdsSampleStudentsInOrder() {
        assertEquals(List.of(sampleStudents), SampleDataUtil.getSampleAddressBook().getStudentList());
    }

    @Test
    public void getSampleStudents_userGuideCommandExamples_succeed() {
        // Each example below is tried on the roster that TutorFlow starts with
        String[][] examples = {
            {"homework add 1 title/Complete algebra worksheet s/MATH due/2026-10-15"},
            {"hw add 1 t/Attempt mechanics questions 1-5 s/PHYSICS due/2026-10-20"},
            {"homework add 2 due/10-18 s/chemistry title/Revise atomic structure"},
            {"hw add 1 t/Read chapter 3 s/physics due/10-5"},
            {"homework list 1"},
            {"hw ls 2 all"},
            {"hw up"},
            {"hw up due/3"},
            {"hw up due/7"},
            {"hw up due/0"},
            {"hw upcoming due/7"},
            {"hw e 1 2 t/Kinematics practice set B"},
            {"hw e 2 1 due/2026-11-05"},
            {"hw edit 1 2 t/Revise mechanics s/PHYSICS due/2026-11-10"},
            {"hw e 3 1 due/10-5"},
            {"hw m 1 2 score/5/10"},
            {"hw m 2 1 score/35/50"},
            {"homework marks 4 2 score/0/10"},
            {"homework list 1", "homework delete 1 2"},
            {"hw del 3 1"},
            {"lesson list"}
        };
        AddressBookParser parser = new AddressBookParser(CLOCK);
        for (String[] example : examples) {
            Model model = getModelWithStudents(List.of(sampleStudents));
            for (String commandText : example) {
                assertDoesNotThrow(() -> parser.parseCommand(commandText).execute(model), commandText);
            }
        }
    }

    @Test
    public void getSampleStudents_userGuideEditExample_showsDocumentedResult() throws Exception {
        String expectedMessage = "Edited homework:\n"
                + "Priya Nair; Title: Integration by parts exercise; Subject: MATH; Due: 2026-11-05\n"
                + "Listed 3 upcoming homework records for Priya Nair.\n"
                + "Homework for Priya Nair:\n"
                + "2. Organic reaction pathways map (CHEMISTRY) - due 2026-11-02 - score Not recorded"
                + " - status ASSIGNED\n"
                + "1. Integration by parts exercise (MATH) - due 2026-11-05 - score Not recorded - status ASSIGNED\n"
                + "3. Chemical equilibrium past paper (CHEMISTRY) - due 2026-11-09 - score Not recorded"
                + " - status ASSIGNED";
        Model model = getModelWithStudents(List.of(sampleStudents));

        assertEquals(expectedMessage, new AddressBookParser(CLOCK).parseCommand("hw e 2 1 due/2026-11-05")
                .execute(model).getFeedbackToUser());
    }

    @Test
    public void getSampleStudents_userGuideUpcomingExamples_showDocumentedResults() throws Exception {
        AddressBookParser parser = new AddressBookParser(UPCOMING_EXAMPLE_CLOCK);
        Model model = getModelWithStudents(List.of(sampleStudents));

        assertEquals("Found 1 homework record due from 2026-10-17 to 2026-10-24.\n"
                + "Student 1: Ethan Lim; Quadratic equations worksheet; MATH; Due: 2026-10-23; Score: Not recorded"
                + "; Status: ASSIGNED",
                parser.parseCommand("hw up").execute(model).getFeedbackToUser());
        assertEquals("No homework due from 2026-10-17 to 2026-10-20.",
                parser.parseCommand("hw up due/3").execute(model).getFeedbackToUser());
    }
}
