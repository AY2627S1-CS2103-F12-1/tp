package seedu.address.model.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TuitionSubjectTest {
    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> TuitionSubject.parse(null));
    }

    @Test
    public void parse_caseAndWhitespaceVariants_success() {
        assertEquals(TuitionSubject.MATH, TuitionSubject.parse("MATH"));
        assertEquals(TuitionSubject.MATH, TuitionSubject.parse("math"));
        assertEquals(TuitionSubject.PHYSICS, TuitionSubject.parse("  Physics  "));
        assertEquals(TuitionSubject.CHEMISTRY, TuitionSubject.parse("cHeMiStRy"));
    }

    @Test
    public void parse_invalidSubject_throwsIllegalArgumentException() {
        assertInvalidSubject(""); // empty
        assertInvalidSubject("   "); // whitespace only
        assertInvalidSubject("BIOLOGY"); // unsupported subject
        assertInvalidSubject("MATHS"); // near miss
        assertInvalidSubject("MA TH"); // internal space
        assertInvalidSubject("MATH, PHYSICS"); // more than one subject
    }

    private static void assertInvalidSubject(String rawSubject) {
        assertThrows(IllegalArgumentException.class, TuitionSubject.MESSAGE_CONSTRAINTS, () ->
                TuitionSubject.parse(rawSubject));
    }
}
