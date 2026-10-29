package seedu.address.model.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

public class StudentFieldsTest {
    @Test
    public void equals_namesCompareNormalizedValuesAndTypes() {
        StudentName name = new StudentName("Alice Tan");
        StudentName equalName = new StudentName("  Alice   Tan  ");
        StudentName differentCase = new StudentName("alice tan");

        assertEquals(name, equalName);
        assertEquals(name.hashCode(), equalName.hashCode());
        assertFalse(name.equals(differentCase));
        assertTrue(name.isSameNameIgnoringCase(differentCase));
        assertFalse(name.equals(null));
        assertFalse(name.equals("Alice Tan"));
    }

    @Test
    public void equals_phonesCompareStoredValuesAndTypes() {
        StudentPhone phone = new StudentPhone("91234567");
        StudentPhone equalPhone = new StudentPhone(" 91234567 ");
        StudentPhone internationalPhone = new StudentPhone("+6591234567");

        assertEquals(phone, equalPhone);
        assertEquals(phone.hashCode(), equalPhone.hashCode());
        assertFalse(phone.equals(internationalPhone));
        assertTrue(phone.isSameNumber(internationalPhone));
        assertFalse(phone.equals(null));
        assertFalse(phone.equals("91234567"));
    }

    @Test
    public void name_normalizesWhitespaceAndPreservesCase() {
        StudentName name = new StudentName("  J.   A/P\tClark-Andrew  ");

        assertEquals("J. A/P Clark-Andrew", name.toString());
        assertTrue(name.isSameNameIgnoringCase(new StudentName("j. a/p clark-andrew")));
    }

    @Test
    public void name_lengthAndCharactersEnforced() {
        for (String validName : new String[] {"A", "AB", "A".repeat(69), "A".repeat(70)}) {
            assertEquals(validName, new StudentName(validName).toString());
        }
        for (String invalidName : new String[] {"", "  ", "A".repeat(71), "John@Tan", "John_Tan"}) {
            assertEquals(StudentName.MESSAGE_CONSTRAINTS,
                    assertThrows(IllegalArgumentException.class, () -> new StudentName(invalidName)).getMessage());
        }
    }

    @Test
    public void name_maximumLength_appliesAfterWhitespaceNormalization() {
        String maximumName = "A".repeat(70);
        String maximumSpacedName = "A".repeat(68) + " B";

        assertEquals(maximumName, new StudentName("  " + maximumName + " \t ").toString());
        assertEquals(maximumSpacedName, new StudentName("A".repeat(68) + " \t  B").toString());
    }

    @Test
    public void name_nonAsciiLetters_preservesCharacters() {
        assertEquals("李明", new StudentName("李明").toString());
    }

    @Test
    public void name_supplementaryLetters_countsCodePointsAtLengthBoundary() {
        // U+10400 is a letter represented by two UTF-16 code units.
        String supplementaryLetter = "\uD801\uDC00";
        String maximumName = supplementaryLetter.repeat(70);
        String excessiveName = supplementaryLetter.repeat(71);

        assertEquals(maximumName, new StudentName(maximumName).toString());
        assertEquals(StudentName.MESSAGE_CONSTRAINTS,
                assertThrows(IllegalArgumentException.class, () -> new StudentName(excessiveName)).getMessage());
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
        assertEquals(subjects, new TuitionSubjects("physics,math"));
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
    public void identity_requiresNormalizedNameAndEquivalentStudentPhone() {
        Student original = student(" John   Tan ", "91234567", "98765432");

        assertTrue(original.isSameStudent(student("john tan", "91234567", "98765432")));
        assertTrue(original.isSameStudent(student("john tan", "+6591234567", "98765432")));
        assertTrue(student("john tan", "+6591234567", "98765432").isSameStudent(original));
        assertFalse(original.isSameStudent(student("John Tan", "81234567", "98765432")));
        assertFalse(original.isSameStudent(student("John Lim", "91234567", "98765432")));
        assertFalse(original.isSameStudent(null));
    }

    @Test
    public void phoneIdentity_onlyNormalizesEightDigitSingaporeNumbers() {
        StudentPhone local = new StudentPhone("91234567");

        assertFalse(local.isSameNumber(null));
        assertFalse(local.isSameNumber(new StudentPhone("+12123456789")));
        assertFalse(local.isSameNumber(new StudentPhone("+65912345678")));
        assertTrue(new StudentPhone("+6512345678").isSameNumber(new StudentPhone("+6512345678")));
    }

    @Test
    public void studentAndGuardianMaySharePhone() {
        Student student = student("John Tan", "91234567", "91234567");

        assertEquals(student.getPhone(), student.getGuardianPhone());
    }

    @Test
    public void valueTypes_equalValuesHaveEqualHashCodes() {
        StudentName name = new StudentName("John   Tan");
        StudentPhone phone = new StudentPhone(" 91234567 ");
        TuitionSubjects subjects = new TuitionSubjects("MATH, PHYSICS");

        assertEquals(new StudentName("John Tan"), name);
        assertEquals(new StudentName("John Tan").hashCode(), name.hashCode());
        assertEquals(new StudentPhone("91234567"), phone);
        assertEquals(new StudentPhone("91234567").hashCode(), phone.hashCode());
        assertEquals(new TuitionSubjects("MATH, PHYSICS"), subjects);
        assertEquals(new TuitionSubjects("MATH, PHYSICS").hashCode(), subjects.hashCode());
        assertFalse(subjects.equals(new TuitionSubjects("PHYSICS, MATH")));
        assertFalse(subjects.equals(null));
        assertFalse(subjects.equals("MATH"));
        assertFalse(name.equals(new StudentName("Jane Tan")));
        assertFalse(phone.equals(new StudentPhone("81234567")));
        assertFalse(subjects.equals(new TuitionSubjects("CHEMISTRY")));
        assertFalse(name.isSameNameIgnoringCase(null));
    }

    @Test
    public void student_equalityIncludesAllSixFields() {
        Student original = student("John Tan", "91234567", "98765432");
        Student equal = student("John Tan", "91234567", "98765432");

        assertEquals(original, equal);
        assertEquals(original.hashCode(), equal.hashCode());
        assertFalse(original.equals(student("John Tan", "91234567", "81234567")));
        assertFalse(original.equals(student("John Tan", "81234567", "98765432")));
        assertFalse(original.equals(student("Jane Tan", "91234567", "98765432")));
        assertFalse(original.equals(new Student(new StudentName("John Tan"), AcademicLevel.S4,
                new TuitionSubjects("MATH"), new StudentPhone("91234567"), new StudentName("Mary Tan"),
                new StudentPhone("98765432"))));
        assertFalse(original.equals(new Student(new StudentName("John Tan"), AcademicLevel.S3,
                new TuitionSubjects("PHYSICS"), new StudentPhone("91234567"), new StudentName("Mary Tan"),
                new StudentPhone("98765432"))));
        assertFalse(original.equals(new Student(new StudentName("John Tan"), AcademicLevel.S3,
                new TuitionSubjects("MATH"), new StudentPhone("91234567"), new StudentName("Jane Tan"),
                new StudentPhone("98765432"))));
        assertFalse(original.equals(null));
    }

    private static Student student(String name, String phone, String guardianPhone) {
        return new Student(new StudentName(name), AcademicLevel.S3, new TuitionSubjects("MATH"),
                new StudentPhone(phone), new StudentName("Mary Tan"), new StudentPhone(guardianPhone));
    }
}
