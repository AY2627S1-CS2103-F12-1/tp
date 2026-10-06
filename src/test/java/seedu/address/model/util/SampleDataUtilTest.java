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
import seedu.address.model.student.Student;

public class SampleDataUtilTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-05T00:00:00Z"), ZoneOffset.UTC);

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
    public void getSampleAddressBook_holdsSampleStudentsInOrder() {
        assertEquals(List.of(sampleStudents), SampleDataUtil.getSampleAddressBook().getStudentList());
    }

    @Test
    public void getSampleStudents_userGuideHomeworkExamples_succeed() {
        // Each example in the User Guide's homework sections, tried on the roster that TutorFlow starts with
        String[][] examples = {
            {"homework add 1 title/Complete algebra worksheet s/MATH due/2026-10-15"},
            {"hw add 1 t/Attempt mechanics questions 1-5 s/PHYSICS due/2026-10-20"},
            {"homework add 2 due/10-18 s/chemistry title/Revise atomic structure"},
            {"hw add 1 t/Read chapter 3 s/physics due/10-5"},
            {"homework list 1"},
            {"hw ls 3"},
            {"homework list 1", "homework delete 1 2"},
            {"hw del 3 1"}
        };
        AddressBookParser parser = new AddressBookParser(CLOCK);
        for (String[] example : examples) {
            Model model = getModelWithStudents(List.of(sampleStudents));
            for (String commandText : example) {
                assertDoesNotThrow(() -> parser.parseCommand(commandText).execute(model), commandText);
            }
        }
    }
}
