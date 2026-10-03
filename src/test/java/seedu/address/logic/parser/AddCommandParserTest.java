package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentPhone;
import seedu.address.testutil.StudentBuilder;

public class AddCommandParserTest {
    private final AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_allFieldsPresent_returnsAddCommand() {
        Student expectedStudent = new StudentBuilder().build();

        assertParseSuccess(parser, " n/John Tan l/S3 s/MATH p/91234567 gn/Mary Tan gp/98765432",
                new AddCommand(expectedStudent));
    }

    @Test
    public void parse_missingPrefix_reportsSpecificPrefixes() {
        String expectedMessage = String.format(StudentAddParser.MESSAGE_MISSING_PREFIXES, "gp/");

        assertParseFailure(parser, " n/John Tan l/S3 s/MATH p/91234567 gn/Mary Tan", expectedMessage);
    }

    @Test
    public void parse_invalidPhone_reportsFieldError() {
        assertParseFailure(parser, " n/John Tan l/S3 s/MATH p/9123-4567 gn/Mary Tan gp/98765432",
                StudentPhone.MESSAGE_CONSTRAINTS);
    }
}
