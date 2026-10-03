package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentName;
import seedu.address.model.student.StudentPhone;
import seedu.address.model.student.TuitionSubjects;

public class StudentAddParserTest {
    private static final String VALID_ARGUMENTS = " n/John Tan l/S3 s/MATH p/91234567 gn/Mary Tan gp/98765432";

    private final StudentAddParser parser = new StudentAddParser();

    @Test
    public void parse_validArgumentsInAnyOrder_returnsNormalizedStudent() throws ParseException {
        Student expected = new Student(new StudentName("Benjamin Lim"), AcademicLevel.S4,
                new TuitionSubjects("PHYSICS, MATH"), new StudentPhone("92345678"),
                new StudentName("David Lim"), new StudentPhone("+98882748"));

        Student actual = parser.parse(" gp/+98882748 gn/David Lim p/92345678 s/physics, math"
                + " l/s4 n/Benjamin   Lim ");

        assertEquals(expected, actual);
        assertEquals("PHYSICS, MATH", actual.getSubjects().toString());
    }

    @Test
    public void parse_nameContainingSlash_preservesName() throws ParseException {
        Student student = parser.parse(VALID_ARGUMENTS.replace("n/John Tan", "n/A/P Roe"));

        assertEquals("A/P Roe", student.getName().toString());
    }

    @Test
    public void parse_missingPrefixes_reportsAllInSpecifiedOrder() {
        String expected = String.format(StudentAddParser.MESSAGE_MISSING_PREFIXES, "l/, s/, gn/, gp/");

        assertParseFailure(" n/John Tan p/91234567", expected);
    }

    @Test
    public void parse_repeatedPrefix_reportsThatPrefix() {
        assertParseFailure(VALID_ARGUMENTS + " gn/Another Guardian",
                String.format(StudentAddParser.MESSAGE_REPEATED_PREFIX, "gn/"));
    }

    @Test
    public void parse_unknownPrefixOrUnprefixedText_reportsFormat() {
        assertParseFailure(VALID_ARGUMENTS + " x/value", StudentAddParser.MESSAGE_INVALID_FORMAT);
        assertParseFailure(" unexpected" + VALID_ARGUMENTS, StudentAddParser.MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_presentButEmptyField_reportsItsValidationError() {
        assertParseFailure(VALID_ARGUMENTS.replace("s/MATH", "s/"), TuitionSubjects.MESSAGE_CONSTRAINTS);
        assertParseFailure(VALID_ARGUMENTS.replace("gn/Mary Tan", "gn/"), StudentName.MESSAGE_CONSTRAINTS);
        assertParseFailure(VALID_ARGUMENTS.replace("gp/98765432", "gp/"), StudentPhone.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidField_reportsItsValidationError() {
        assertParseFailure(VALID_ARGUMENTS.replace("l/S3", "l/S 3"), AcademicLevel.MESSAGE_CONSTRAINTS);
        assertParseFailure(VALID_ARGUMENTS.replace("s/MATH", "s/MATH,math"), TuitionSubjects.MESSAGE_CONSTRAINTS);
        assertParseFailure(VALID_ARGUMENTS.replace("p/91234567", "p/9123-4567"), StudentPhone.MESSAGE_CONSTRAINTS);
    }

    private void assertParseFailure(String arguments, String expectedMessage) {
        ParseException exception = assertThrows(ParseException.class, () -> parser.parse(arguments));
        assertEquals(expectedMessage, exception.getMessage());
    }
}
