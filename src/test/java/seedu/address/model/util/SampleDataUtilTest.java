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
    /** Date of the documented results, the first day of the practical exam. */
    private static final Clock EXAMPLE_CLOCK = Clock.fixed(Instant.parse("2026-11-06T00:00:00Z"), ZoneOffset.UTC);
    /** The next day, on which the documented results other than {@code homework upcoming} must not change. */
    private static final Clock NEXT_DAY_CLOCK = Clock.fixed(Instant.parse("2026-11-07T00:00:00Z"), ZoneOffset.UTC);

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
            {"hw e 2 1 due/2026-12-05"},
            {"hw edit 1 2 t/Revise mechanics s/PHYSICS due/2026-11-10"},
            {"hw e 3 1 due/10-5"},
            {"hw m 1 2 score/5/10"},
            {"hw m 2 1 score/35/50"},
            {"homework marks 4 2 score/0/10"},
            {"hw m 1 2 score/5/10", "hw m 1 2 score/"},
            {"homework list 1", "homework delete 1 2"},
            {"hw del 3 1"},
            {"hw add 2 t/Vectors revision s/MATH due/2026-11-30", "hw del 2 2"},
            {"lesson list"}
        };
        for (Clock clock : List.of(EXAMPLE_CLOCK, NEXT_DAY_CLOCK)) {
            AddressBookParser parser = new AddressBookParser(clock);
            for (String[] example : examples) {
                Model model = getModelWithStudents(List.of(sampleStudents));
                for (String commandText : example) {
                    assertDoesNotThrow(() -> parser.parseCommand(commandText).execute(model), commandText);
                }
            }
        }
    }

    @Test
    public void getSampleStudents_userGuideAddExample_showsDocumentedResult() throws Exception {
        String expectedMessage = "New homework added for Ethan Lim (91234567):\n"
                + "3. Complete algebra worksheet (MATH) - due 2026-10-15 - score Not recorded - status ASSIGNED\n"
                + "Note: this homework is due before today, so homework list 1 shows it only with all.";
        for (Clock clock : List.of(EXAMPLE_CLOCK, NEXT_DAY_CLOCK)) {
            Model model = getModelWithStudents(List.of(sampleStudents));
            assertEquals(expectedMessage, new AddressBookParser(clock)
                    .parseCommand("homework add 1 title/Complete algebra worksheet s/MATH due/2026-10-15")
                    .execute(model).getFeedbackToUser());
        }
    }

    @Test
    public void getSampleStudents_userGuideEditExample_showsDocumentedResult() throws Exception {
        String expectedMessage = "Edited homework:\n"
                + "Priya Nair (82345671); Title: Integration by parts exercise; Subject: MATH; Due: 2026-12-05\n"
                + "Listed 3 upcoming homework records for Priya Nair (82345671).\n"
                + "Homework for Priya Nair (82345671):\n"
                + "2. Organic reaction pathways map (CHEMISTRY) - due 2026-11-09 - score Not recorded"
                + " - status ASSIGNED\n"
                + "3. Chemical equilibrium past paper (CHEMISTRY) - due 2026-11-23 - score Not recorded"
                + " - status ASSIGNED\n"
                + "1. Integration by parts exercise (MATH) - due 2026-12-05 - score Not recorded - status ASSIGNED";
        for (Clock clock : List.of(EXAMPLE_CLOCK, NEXT_DAY_CLOCK)) {
            Model model = getModelWithStudents(List.of(sampleStudents));
            assertEquals(expectedMessage, new AddressBookParser(clock).parseCommand("hw e 2 1 due/2026-12-05")
                    .execute(model).getFeedbackToUser());
        }
    }

    @Test
    public void getSampleStudents_userGuideDeleteExample_showsDocumentedResult() throws Exception {
        String expectedMessage = "Deleted homework:\n"
                + "2. Organic reaction pathways map (CHEMISTRY) - due 2026-11-09 - score Not recorded"
                + " - status ASSIGNED\n"
                + "Listed 2 upcoming homework records for Priya Nair (82345671) (1 record due before today not shown;"
                + " use homework list 2 all to see it).\n"
                + "Homework for Priya Nair (82345671):\n"
                + "2. Chemical equilibrium past paper (CHEMISTRY) - due 2026-11-23 - score Not recorded"
                + " - status ASSIGNED\n"
                + "3. Vectors revision (MATH) - due 2026-11-30 - score Not recorded - status ASSIGNED";
        for (Clock clock : List.of(EXAMPLE_CLOCK, NEXT_DAY_CLOCK)) {
            AddressBookParser parser = new AddressBookParser(clock);
            Model model = getModelWithStudents(List.of(sampleStudents));
            parser.parseCommand("hw add 2 t/Vectors revision s/MATH due/2026-11-30").execute(model);
            assertEquals(expectedMessage, parser.parseCommand("hw del 2 2").execute(model).getFeedbackToUser());
        }
    }

    @Test
    public void getSampleStudents_userGuideUpcomingExamples_showDocumentedResults() throws Exception {
        AddressBookParser parser = new AddressBookParser(EXAMPLE_CLOCK);
        Model model = getModelWithStudents(List.of(sampleStudents));

        assertEquals("Found 3 homework records due from 2026-11-06 to 2026-11-13.\n"
                + "Student 2: Priya Nair (82345671); Organic reaction pathways map; CHEMISTRY; Due: 2026-11-09"
                + "; Score: Not recorded; Status: ASSIGNED\n"
                + "Student 1: Ethan Lim (91234567); Kinematics practice set; PHYSICS; Due: 2026-11-11"
                + "; Score: Not recorded; Status: ASSIGNED\n"
                + "Student 3: Marcus Tan (87654329); Algebraic expansion worksheet; MATH; Due: 2026-11-12"
                + "; Score: Not recorded; Status: ASSIGNED",
                parser.parseCommand("hw up").execute(model).getFeedbackToUser());
        assertEquals("Found 1 homework record due from 2026-11-06 to 2026-11-09.\n"
                + "Student 2: Priya Nair (82345671); Organic reaction pathways map; CHEMISTRY; Due: 2026-11-09"
                + "; Score: Not recorded; Status: ASSIGNED",
                parser.parseCommand("hw up due/3").execute(model).getFeedbackToUser());
    }
}
