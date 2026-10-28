package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.CommandParserTestUtil.notPrefix;
import static seedu.address.logic.parser.CommandParserTestUtil.notPrefixWithQuoteHint;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.RegularLesson;
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
            assertParseFailure(parser, arguments, "Invalid command format.\nUsage: " + FilterCommand.MESSAGE_USAGE
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
            assertParseFailure(parser, arguments, "Invalid command format.\nUsage: " + FilterCommand.MESSAGE_USAGE);
        }
    }

    @Test
    public void parse_repeatedPrefix_reportsRepeatedFilter() {
        for (String arguments : List.of(" l/S4 l/S4", " s/MATH s/PHYSICS", " s/MATH l/S4 s/MATH", " l/ l/S4",
                " d/MONDAY d/TUESDAY", " d/ d/MONDAY")) {
            assertParseFailure(parser, arguments, "Each filter may only be specified once");
        }
    }

    @Test
    public void parse_emptyLevel_reportsEmptyValue() {
        for (String arguments : List.of(" l/", " l/   ", " l/\"\"", " l/\"   \"", " s/MATH l/")) {
            assertParseFailure(parser, arguments, "Academic level must be a non-empty value");
        }
    }

    @Test
    public void parse_emptySubject_reportsEmptyValue() {
        for (String arguments : List.of(" s/", " s/   ", " s/\"\"", " s/\"   \"", " l/S4 s/")) {
            assertParseFailure(parser, arguments, "Subject must be a non-empty value");
        }
    }

    @Test
    public void parse_unsupportedLevel_reportsSupportedLevels() {
        for (String arguments : List.of(" l/S6", " l/Sec 4", " l/S4,J1", " s/MATH l/bad")) {
            assertParseFailure(parser, arguments, AcademicLevel.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_unsupportedSubject_reportsSupportedSubjects() {
        for (String arguments : List.of(" s/BIOLOGY", " s/Math Physics", " l/S4 s/bad")) {
            assertParseFailure(parser, arguments, TuitionSubject.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_commaSeparatedSubjects_reportsSingleSubjectRestriction() {
        for (String arguments : List.of(" s/MATH,PHYSICS", " s/Math, Physics", " s/\"Math, Physics\"",
                " l/S4 s/MATH,CHEMISTRY", " s/MATH,MATH", " s/MATH,")) {
            assertParseFailure(parser, arguments, "Filter accepts only one subject. Use s/SUBJECT.");
        }
    }

    @Test
    public void parse_unsupportedValues_preservesValidationCause() {
        for (String arguments : List.of(" l/S6", " s/Biology", " d/Mon")) {
            ParseException exception = assertThrows(ParseException.class, () -> parser.parse(arguments));

            assertInstanceOf(IllegalArgumentException.class, exception.getCause());
            assertEquals(exception.getCause().getMessage(), exception.getMessage());
        }
    }

    @Test
    public void parse_bothValuesInvalid_reportsFirstSuppliedValue() {
        assertParseFailure(parser, " s/bad l/bad", TuitionSubject.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " l/bad s/bad", AcademicLevel.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " d/bad l/bad", RegularLesson.MESSAGE_DAY);
        assertParseFailure(parser, " l/bad d/bad", AcademicLevel.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parseCommand_filter_routesArgumentsAndValidation() throws Exception {
        AddressBookParser addressBookParser = new AddressBookParser();
        String missingMessage = "Invalid command format.\nUsage: " + FilterCommand.MESSAGE_USAGE
                + "\nAt least one filter must be provided";

        assertEquals(parser.parse(" l/S4 s/MATH"), addressBookParser.parseCommand("filter l/S4 s/MATH"));
        ParseException exception = assertThrows(ParseException.class, () -> addressBookParser.parseCommand("filter"));
        assertEquals(missingMessage, exception.getMessage());
    }

    @Test
    public void parse_dayOnly_acceptsEveryFullWeekday() {
        for (DayOfWeek day : DayOfWeek.values()) {
            FilterCommand expected = new FilterCommand(new StudentFilterPredicate(
                    Optional.empty(), Optional.empty(), Optional.of(day)));
            assertParseSuccess(parser, " d/" + day, expected);
            assertParseSuccess(parser, " d/\" " + day.name().toLowerCase(Locale.ROOT) + " \"", expected);
        }
    }

    @Test
    public void parse_dayWithOtherCriteria_acceptsCombinationsAndPrefixOrders() {
        FilterCommand expected = new FilterCommand(new StudentFilterPredicate(
                Optional.of(AcademicLevel.S4), Optional.of(TuitionSubject.MATH), Optional.of(DayOfWeek.MONDAY)));
        for (String arguments : List.of(" l/S4 s/Math d/monday", " d/Monday s/math l/s4",
                " s/MATH l/S4 d/\" monday \"")) {
            assertParseSuccess(parser, arguments, expected);
        }
        assertParseSuccess(parser, " l/S4 d/MONDAY", new FilterCommand(new StudentFilterPredicate(
                Optional.of(AcademicLevel.S4), Optional.empty(), Optional.of(DayOfWeek.MONDAY))));
        assertParseSuccess(parser, " s/MATH d/MONDAY", new FilterCommand(new StudentFilterPredicate(
                Optional.empty(), Optional.of(TuitionSubject.MATH), Optional.of(DayOfWeek.MONDAY))));
    }

    @Test
    public void parse_emptyDay_reportsEmptyValue() {
        for (String arguments : List.of(" d/", " d/   ", " d/\"\"", " d/\"  \"", " l/S4 d/")) {
            assertParseFailure(parser, arguments, "Day must be a non-empty value");
        }
    }

    @Test
    public void parse_unsupportedDay_reportsFullWeekdayConstraint() {
        for (String arguments : List.of(" d/MON", " d/1", " d/Monday,Tuesday", " d/FUNDAY")) {
            assertParseFailure(parser, arguments, RegularLesson.MESSAGE_DAY);
        }
    }
}
