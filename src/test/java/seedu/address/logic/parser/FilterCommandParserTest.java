package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.CommandParserTestUtil.notPrefix;
import static seedu.address.logic.parser.CommandParserTestUtil.notPrefixWithQuoteHint;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.StudentFilterPredicate;
import seedu.address.model.student.TuitionSubject;

public class FilterCommandParserTest {
    private final FilterCommandParser parser = new FilterCommandParser();

    @Test
    public void parse_levelOnly_acceptsEverySupportedLevel() {
        for (AcademicLevel level : AcademicLevel.values()) {
            FilterCommand expected = new FilterCommand(new StudentFilterPredicate(
                    Optional.of(level), Optional.empty()));
            assertParseSuccess(parser, " l/" + level, expected);
        }
    }

    @Test
    public void parse_subjectOnly_acceptsEverySupportedSubject() {
        for (TuitionSubject subject : TuitionSubject.values()) {
            FilterCommand expected = new FilterCommand(new StudentFilterPredicate(
                    Optional.empty(), Optional.of(subject)));
            assertParseSuccess(parser, " s/" + subject, expected);
        }
    }

    @Test
    public void parse_bothCriteria_normalizesCaseWhitespaceAndPrefixOrder() {
        FilterCommand expected = new FilterCommand(new StudentFilterPredicate(
                Optional.of(AcademicLevel.S4), Optional.of(TuitionSubject.MATH)));
        for (String arguments : List.of(" l/S4 s/Math", " s/ math  l/ s4 ", " l/\" s4 \" s/\" Math \"")) {
            assertParseSuccess(parser, arguments, expected);
        }
    }

    @Test
    public void parse_noCriteria_reportsMissingFilters() {
        for (String arguments : List.of("", "  ", "\t")) {
            assertParseFailure(parser, arguments, "Invalid Command Format\nUsage: filter [l/LEVEL] [s/SUBJECT]"
                    + "\nAt least one filter must be provided");
        }
    }

    @Test
    public void parse_unknownPrefix_namesPrefix() {
        String usageLine = "\nUsage: " + FilterCommand.MESSAGE_USAGE;
        assertParseFailure(parser, " x/S4", notPrefix("x/") + usageLine);
        assertParseFailure(parser, " l/S4 p/123", notPrefixWithQuoteHint("p/", "l/\"S4 p/123\"") + usageLine);
    }

    @Test
    public void parse_preamble_reportsInvalidFormat() {
        for (String arguments : List.of(" all s/MATH", "S4", " L/S4")) {
            assertParseFailure(parser, arguments, "Invalid command format\nUsage: filter [l/LEVEL] [s/SUBJECT]");
        }
    }

    @Test
    public void parse_repeatedPrefix_reportsRepeatedFilter() {
        for (String arguments : List.of(" l/S4 l/S4", " s/MATH s/PHYSICS", " s/MATH l/S4 s/MATH", " l/ l/S4")) {
            assertParseFailure(parser, arguments, "Each filter may only be specified once");
        }
    }

    @Test
    public void parse_emptyOrInvalidLevel_reportsLevelError() {
        for (String arguments : List.of(" l/", " l/   ", " l/S6", " l/S4,J1", " l/\"\"", " s/MATH l/bad")) {
            assertParseFailure(parser, arguments, "Academic level must be a non-empty value");
        }
    }

    @Test
    public void parse_emptyOrInvalidSubject_reportsSubjectError() {
        for (String arguments : List.of(" s/", " s/   ", " s/BIOLOGY", " s/MATH,PHYSICS", " s/\"\"",
                " l/S4 s/bad")) {
            assertParseFailure(parser, arguments, "Subject must be a non-empty value");
        }
    }

    @Test
    public void parse_bothValuesInvalid_reportsFirstSuppliedValue() {
        assertParseFailure(parser, " s/bad l/bad", "Subject must be a non-empty value");
        assertParseFailure(parser, " l/bad s/bad", "Academic level must be a non-empty value");
    }

    @Test
    public void parseCommand_filter_routesArgumentsAndValidation() throws Exception {
        AddressBookParser addressBookParser = new AddressBookParser();
        String missingMessage = "Invalid Command Format\nUsage: filter [l/LEVEL] [s/SUBJECT]"
                + "\nAt least one filter must be provided";

        assertEquals(parser.parse(" l/S4 s/MATH"), addressBookParser.parseCommand("filter l/S4 s/MATH"));
        assertThrows(ParseException.class, missingMessage, () -> addressBookParser.parseCommand("filter"));
    }
}
