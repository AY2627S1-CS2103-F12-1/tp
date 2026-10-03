package seedu.address.model.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

public class StudentFieldsTest {
    @Test
    public void name_normalizesWhitespaceAndPreservesCase() {
        StudentName name = new StudentName("  J.   A/P\tClark-Andrew  ");

        assertEquals("J. A/P Clark-Andrew", name.toString());
        assertTrue(name.isSameNameIgnoringCase(new StudentName("j. a/p clark-andrew")));
    }

    @Test
    public void name_lengthAndCharactersEnforced() {
        assertEquals("A".repeat(70), new StudentName("A".repeat(70)).toString());
        for (String invalidName : new String[] {"  ", "A".repeat(71), "John@Tan", "John_Tan"}) {
            assertEquals(StudentName.MESSAGE_CONSTRAINTS,
                    assertThrows(IllegalArgumentException.class, () -> new StudentName(invalidName)).getMessage());
        }
    }

    @Test
    public void academicLevel_acceptsFullSpecifiedRange() {
        for (AcademicLevel level : AcademicLevel.values()) {
            assertEquals(level, AcademicLevel.parse(" " + level.name().toLowerCase() + " "));
        }
        for (String invalidLevel : new String[] {"S 3", "S6", "P1", ""}) {
            assertEquals(AcademicLevel.MESSAGE_CONSTRAINTS,
                    assertThrows(IllegalArgumentException.class, () -> AcademicLevel.parse(invalidLevel)).getMessage());
        }
    }

    @Test
    public void subjects_normalizeAndPreserveInputOrder() {
        TuitionSubjects subjects = new TuitionSubjects(" physics, MATH ");

        assertEquals(Set.of(TuitionSubject.PHYSICS, TuitionSubject.MATH), subjects.getSubjects());
        assertEquals("PHYSICS, MATH", subjects.toString());
        assertEquals(subjects, new TuitionSubjects("math,physics"));
        assertThrows(UnsupportedOperationException.class, () -> subjects.getSubjects().add(TuitionSubject.CHEMISTRY));
    }

    @Test
    public void subjects_rejectMissingUnknownAndDuplicates() {
        for (String invalidSubjects : new String[] {"", "MATH,", ",MATH", "MATH,,PHYSICS", "BIOLOGY",
            "MATH,math"}) {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    new TuitionSubjects(invalidSubjects));
            assertEquals(TuitionSubjects.MESSAGE_CONSTRAINTS, exception.getMessage());
        }
    }

    @Test
    public void phone_acceptsLocalAndInternationalBoundaryLengths() {
        for (String validPhone : new String[] {"61234567", "81234567", "91234567", "+12345678",
            "+123456789012345"}) {
            assertEquals(validPhone, new StudentPhone(" " + validPhone + " ").toString());
        }
    }

    @Test
    public void phone_rejectsInvalidFormats() {
        for (String invalidPhone : new String[] {"71234567", "9123456", "912345678", "+1234567",
            "+1234567890123456", "9123 4567", "9123-4567", "(91234567)", "abc"}) {
            assertEquals(StudentPhone.MESSAGE_CONSTRAINTS,
                    assertThrows(IllegalArgumentException.class, () -> new StudentPhone(invalidPhone)).getMessage());
        }
    }

    @Test
    public void identity_requiresBothNormalizedNameAndExactStudentPhone() {
        Student original = student(" John   Tan ", "91234567", "98765432");

        assertTrue(original.isSameStudent(student("john tan", "91234567", "98765432")));
        assertFalse(original.isSameStudent(student("John Tan", "81234567", "98765432")));
        assertFalse(original.isSameStudent(student("John Lim", "91234567", "98765432")));
        assertFalse(original.isSameStudent(null));
    }

    @Test
    public void studentAndGuardianMaySharePhone() {
        Student student = student("John Tan", "91234567", "91234567");

        assertEquals(student.getPhone(), student.getGuardianPhone());
    }

    private static Student student(String name, String phone, String guardianPhone) {
        return new Student(new StudentName(name), AcademicLevel.S3, new TuitionSubjects("MATH"),
                new StudentPhone(phone), new StudentName("Mary Tan"), new StudentPhone(guardianPhone));
    }
}
