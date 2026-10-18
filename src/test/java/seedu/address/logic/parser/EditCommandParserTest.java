package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditStudentDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.StudentName;
import seedu.address.model.student.StudentPhone;
import seedu.address.model.student.TuitionSubjects;

public class EditCommandParserTest {
    private final EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_allFieldsInAnyOrder_returnsEditCommand() throws ParseException {
        EditStudentDescriptor descriptor = new EditStudentDescriptor(new StudentName("Ravi s/o Kumar"),
                AcademicLevel.S4, new TuitionSubjects("PHYSICS, MATH"), new StudentPhone("92345678"),
                new StudentName("Maya d/o Raj"), new StudentPhone("98765432"));

        assertEquals(new EditCommand(Index.fromOneBased(2), descriptor), parser.parse(" 2 gp/98765432"
                + " gn/Maya d/o Raj p/92345678 s/physics, math l/s4 n/Ravi s/o Kumar "));
    }

    @Test
    public void parse_eachSingleField_returnsEditCommand() throws ParseException {
        assertEquals(command(new StudentName("Jane"), null, null, null, null, null), parser.parse("1 n/Jane"));
        assertEquals(command(null, AcademicLevel.S2, null, null, null, null), parser.parse("1 l/S2"));
        assertEquals(command(null, null, new TuitionSubjects("MATH"), null, null, null), parser.parse("1 s/MATH"));
        assertEquals(command(null, null, null, new StudentPhone("92345678"), null, null),
                parser.parse("1 p/92345678"));
        assertEquals(command(null, null, null, null, new StudentName("Mary"), null), parser.parse("1 gn/Mary"));
        assertEquals(command(null, null, null, null, null, new StudentPhone("98765432")),
                parser.parse("1 gp/98765432"));
    }

    @Test
    public void parse_nameContainingSlash_preservesName() throws ParseException {
        assertEquals(command(new StudentName("A/P Roe"), null, null, null, null, null),
                parser.parse("1 n/A/P Roe"));
    }

    @Test
    public void parse_missingFields_reportsUsage() {
        assertParseFailure("1", EditCommand.MESSAGE_NO_FIELDS);
        assertParseFailure(" 1   ", EditCommand.MESSAGE_NO_FIELDS);
    }

    @Test
    public void parse_invalidIndex_reportsSyntax() {
        String[] invalidArguments = {"n/Jane", "0 n/Jane", "01 n/Jane", "-1 n/Jane",
            "1.0 n/Jane", "abc n/Jane", "2147483648 n/Jane", "1 2 n/Jane"};
        for (String arguments : invalidArguments) {
            assertParseFailure(arguments, Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);
        }
    }

    @Test
    public void parse_unknownOrRepeatedPrefix_reportsError() {
        assertParseFailure("1 l/S3 x/value", EditCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure("1 n/Jane n/Mary",
                String.format(StrictArgumentTokenizer.MESSAGE_REPEATED_PREFIX, "n/"));
        assertParseFailure("1 gn/Mary gn/Jane",
                String.format(StrictArgumentTokenizer.MESSAGE_REPEATED_PREFIX, "gn/"));
    }

    @Test
    public void parse_invalidField_reportsFieldError() {
        assertParseFailure("1 n/", StudentName.MESSAGE_CONSTRAINTS);
        assertParseFailure("1 l/S 3", AcademicLevel.MESSAGE_CONSTRAINTS);
        assertParseFailure("1 s/MATH,math", TuitionSubjects.MESSAGE_CONSTRAINTS);
        assertParseFailure("1 p/9123-4567", StudentPhone.MESSAGE_CONSTRAINTS);
        assertParseFailure("1 gn/", StudentName.MESSAGE_CONSTRAINTS);
        assertParseFailure("1 gp/", StudentPhone.MESSAGE_CONSTRAINTS);
    }

    private static EditCommand command(StudentName name, AcademicLevel level, TuitionSubjects subjects,
            StudentPhone phone, StudentName guardianName, StudentPhone guardianPhone) {
        return new EditCommand(Index.fromOneBased(1),
                new EditStudentDescriptor(name, level, subjects, phone, guardianName, guardianPhone));
    }

    private void assertParseFailure(String arguments, String expectedMessage) {
        ParseException exception = assertThrows(ParseException.class, () -> parser.parse(arguments));
        assertEquals(expectedMessage, exception.getMessage());
    }
}
