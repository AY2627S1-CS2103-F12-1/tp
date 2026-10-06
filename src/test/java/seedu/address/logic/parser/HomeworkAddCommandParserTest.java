package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalHomeworks.MECHANICS;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.HomeworkAddCommand;
import seedu.address.model.homework.DueDate;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.Title;
import seedu.address.model.student.TuitionSubject;
import seedu.address.testutil.HomeworkBuilder;

public class HomeworkAddCommandParserTest {
    /** Fixed clock at 2026-10-05, so short-form due dates always infer the same year. */
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-05T00:00:00Z"), ZoneOffset.UTC);
    private static final String VALID_FIELDS = " title/Complete algebra worksheet s/MATH due/2026-10-15";
    private static final String MESSAGE_INVALID_FORMAT = "Invalid command format.\n"
            + "Usage: homework|hw add STUDENT_INDEX title/|t/TITLE s/SUBJECT due/YYYY-MM-DD|MM-DD";
    private static final String MESSAGE_MISSING_PREFIXES = "Missing required prefix(es): %s\n"
            + "Usage: homework|hw add STUDENT_INDEX title/|t/TITLE s/SUBJECT due/YYYY-MM-DD|MM-DD";

    private final HomeworkAddCommandParser parser = new HomeworkAddCommandParser(CLOCK);

    @Test
    public void parse_titlePrefix_success() {
        assertParseSuccess(parser, "1 title/Complete algebra worksheet s/MATH due/2026-10-15",
                new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, false));
    }

    @Test
    public void parse_titleAliasPrefix_success() {
        assertParseSuccess(parser, "2 t/Attempt mechanics questions 1-5 s/PHYSICS due/2026-10-20",
                new HomeworkAddCommand(Index.fromOneBased(2), MECHANICS, false));
    }

    @Test
    public void parse_prefixesInAnyOrderLowercaseSubject_success() {
        Homework expectedHomework = new HomeworkBuilder().withTitle("Revise atomic structure")
                .withSubject("CHEMISTRY").withDueDate("2026-10-18").build();

        assertParseSuccess(parser, "3 due/2026-10-18 s/chemistry title/Revise atomic structure",
                new HomeworkAddCommand(Index.fromOneBased(3), expectedHomework, false));
    }

    @Test
    public void parse_extraWhitespace_success() {
        assertParseSuccess(parser, "  1   title/  Complete   algebra worksheet  s/  Math  due/ 2026-10-15  ",
                new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, false));
    }

    @Test
    public void parse_largestStudentIndex_success() {
        assertParseSuccess(parser, "2147483647" + VALID_FIELDS,
                new HomeworkAddCommand(Index.fromOneBased(Integer.MAX_VALUE), ALGEBRA, false));
    }

    @Test
    public void parse_pastFullDate_success() {
        Homework expectedHomework = new HomeworkBuilder().withDueDate("2025-01-01").build();

        assertParseSuccess(parser, "1 title/Complete algebra worksheet s/MATH due/2025-01-01",
                new HomeworkAddCommand(Index.fromOneBased(1), expectedHomework, false));
    }

    @Test
    public void parse_shortDateAfterToday_infersCurrentYear() {
        assertParseSuccess(parser, "1 title/Complete algebra worksheet s/MATH due/10-15",
                new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, true));
    }

    @Test
    public void parse_shortDateToday_infersCurrentYear() {
        Homework expectedHomework = new HomeworkBuilder().withDueDate("2026-10-05").build();

        assertParseSuccess(parser, "1 title/Complete algebra worksheet s/MATH due/10-05",
                new HomeworkAddCommand(Index.fromOneBased(1), expectedHomework, true));
    }

    @Test
    public void parse_shortDateBeforeToday_infersNextYear() {
        Homework expectedHomework = new HomeworkBuilder().withDueDate("2027-10-04").build();

        assertParseSuccess(parser, "1 title/Complete algebra worksheet s/MATH due/10-04",
                new HomeworkAddCommand(Index.fromOneBased(1), expectedHomework, true));
    }

    @Test
    public void parse_shortDateSingleDigitDay_infersCurrentYear() {
        Homework expectedHomework = new HomeworkBuilder().withDueDate("2026-10-05").build();

        assertParseSuccess(parser, "1 title/Complete algebra worksheet s/MATH due/10-5",
                new HomeworkAddCommand(Index.fromOneBased(1), expectedHomework, true));
    }

    @Test
    public void parse_shortDateSingleDigitMonthAndDay_infersNextYear() {
        Homework expectedHomework = new HomeworkBuilder().withDueDate("2027-09-09").build();

        assertParseSuccess(parser, "1 title/Complete algebra worksheet s/MATH due/9-9",
                new HomeworkAddCommand(Index.fromOneBased(1), expectedHomework, true));
    }

    @Test
    public void parse_fullDateSingleDigitMonthAndDay_success() {
        Homework expectedHomework = new HomeworkBuilder().withDueDate("2026-02-05").build();

        assertParseSuccess(parser, "1 title/Complete algebra worksheet s/MATH due/2026-2-5",
                new HomeworkAddCommand(Index.fromOneBased(1), expectedHomework, false));
    }

    @Test
    public void parse_titleWithSlash_success() {
        Homework expectedHomework = new Homework(new Title("Exercise 3/4 and 5/6"), TuitionSubject.MATH,
                DueDate.parse("2026-10-15", HomeworkBuilder.DEFAULT_TODAY));

        assertParseSuccess(parser, "1 title/Exercise 3/4 and 5/6 s/MATH due/2026-10-15",
                new HomeworkAddCommand(Index.fromOneBased(1), expectedHomework, false));
    }

    @Test
    public void parse_titleWithSpacedOrCapitalizedSlashWord_success() {
        Homework expectedHomework = new HomeworkBuilder().withTitle("Speed in km / h, And/or units").build();

        assertParseSuccess(parser, "1 title/Speed in km / h, And/or units s/MATH due/2026-10-15",
                new HomeworkAddCommand(Index.fromOneBased(1), expectedHomework, false));
    }

    @Test
    public void parse_titleWithLowercaseSlashWord_failure() {
        // km/ is read as an unknown prefix
        assertParseFailure(parser, "1 title/Speed in km/h s/MATH due/2026-10-15", MESSAGE_INVALID_FORMAT);

        // and/ is read as an unknown prefix
        assertParseFailure(parser, "1 t/Read and/or summarize s/MATH due/2026-10-15", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_emptyArguments_failure() {
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_missingStudentIndex_failure() {
        assertParseFailure(parser, VALID_FIELDS, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_extraPreambleWord_failure() {
        assertParseFailure(parser, "1 2" + VALID_FIELDS, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_unknownPrefix_failure() {
        assertParseFailure(parser, "1" + VALID_FIELDS + " x/extra", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_uppercasePrefix_failure() {
        // TITLE/ is not a prefix, so it is unprefixed text in the preamble
        assertParseFailure(parser, "1 TITLE/Complete algebra worksheet s/MATH due/2026-10-15",
                MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_repeatedSubjectPrefix_failure() {
        assertParseFailure(parser, "1" + VALID_FIELDS + " s/PHYSICS",
                "Prefix s/ must be specified exactly once.");
    }

    @Test
    public void parse_repeatedTitleAliasPrefix_failure() {
        assertParseFailure(parser, "1 t/First t/Second s/MATH due/2026-10-15",
                "Prefix t/ must be specified exactly once.");
    }

    @Test
    public void parse_titleThenTitleAlias_reportsTitleRepeated() {
        assertParseFailure(parser, "1 title/First t/Second s/MATH due/2026-10-15",
                "Prefix title/ must be specified exactly once.");
    }

    @Test
    public void parse_titleAliasThenTitle_reportsTitleRepeated() {
        assertParseFailure(parser, "1 t/First title/Second s/MATH due/2026-10-15",
                "Prefix title/ must be specified exactly once.");
    }

    @Test
    public void parse_allPrefixesMissing_reportsAllInOrder() {
        assertParseFailure(parser, "1", String.format(MESSAGE_MISSING_PREFIXES, "title/, s/, due/"));
    }

    @Test
    public void parse_titleMissing_failure() {
        assertParseFailure(parser, "1 s/MATH due/2026-10-15", String.format(MESSAGE_MISSING_PREFIXES, "title/"));
    }

    @Test
    public void parse_subjectAndDueDateMissing_failure() {
        assertParseFailure(parser, "1 t/Complete algebra worksheet",
                String.format(MESSAGE_MISSING_PREFIXES, "s/, due/"));
    }

    @Test
    public void parse_dueDateMissing_failure() {
        assertParseFailure(parser, "1 title/Complete algebra worksheet s/MATH",
                String.format(MESSAGE_MISSING_PREFIXES, "due/"));
    }

    @Test
    public void parse_invalidStudentIndex_failure() {
        // zero
        assertParseFailure(parser, "0" + VALID_FIELDS, Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // leading zero
        assertParseFailure(parser, "01" + VALID_FIELDS, Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // negative
        assertParseFailure(parser, "-1" + VALID_FIELDS, Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // plus sign
        assertParseFailure(parser, "+1" + VALID_FIELDS, Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // decimal
        assertParseFailure(parser, "1.0" + VALID_FIELDS, Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // word
        assertParseFailure(parser, "one" + VALID_FIELDS, Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // larger than the largest int
        assertParseFailure(parser, "2147483648" + VALID_FIELDS, Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);
    }

    @Test
    public void parse_emptyTitle_failure() {
        assertParseFailure(parser, "1 title/ s/MATH due/2026-10-15", Title.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_emptyTitleAlias_failure() {
        assertParseFailure(parser, "1 t/ s/MATH due/2026-10-15", Title.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_titleTooLong_failure() {
        assertParseFailure(parser, "1 title/" + "a".repeat(101) + " s/MATH due/2026-10-15",
                Title.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_emptySubject_failure() {
        assertParseFailure(parser, "1 title/Complete algebra worksheet s/ due/2026-10-15",
                TuitionSubject.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_unsupportedSubject_failure() {
        assertParseFailure(parser, "1 title/Complete algebra worksheet s/BIOLOGY due/2026-10-15",
                TuitionSubject.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_multipleSubjects_failure() {
        assertParseFailure(parser, "1 title/Complete algebra worksheet s/MATH, PHYSICS due/2026-10-15",
                TuitionSubject.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_emptyDueDate_failure() {
        assertParseFailure(parser, "1 title/Complete algebra worksheet s/MATH due/", DueDate.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidDueDate_failure() {
        String fieldsWithoutDueDate = "1 title/Complete algebra worksheet s/MATH due/";

        // wrong separator and field order
        assertParseFailure(parser, fieldsWithoutDueDate + "15/10/2026", DueDate.MESSAGE_CONSTRAINTS);

        // three-digit month
        assertParseFailure(parser, fieldsWithoutDueDate + "010-05", DueDate.MESSAGE_CONSTRAINTS);

        // single-digit short-form date that does not exist
        assertParseFailure(parser, fieldsWithoutDueDate + "2-30", DueDate.MESSAGE_CONSTRAINTS);

        // date that does not exist
        assertParseFailure(parser, fieldsWithoutDueDate + "2026-02-30", DueDate.MESSAGE_CONSTRAINTS);

        // short-form date that never exists
        assertParseFailure(parser, fieldsWithoutDueDate + "02-30", DueDate.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_unknownPrefixAndMissingPrefixes_reportsInvalidFormat() {
        assertParseFailure(parser, "1 x/extra", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_repeatedPrefixAndInvalidIndex_reportsRepeatedPrefix() {
        assertParseFailure(parser, "0" + VALID_FIELDS + " due/2026-10-16",
                "Prefix due/ must be specified exactly once.");
    }

    @Test
    public void parse_extraPreambleWordAndMissingPrefixes_reportsInvalidFormat() {
        assertParseFailure(parser, "1 2 title/Complete algebra worksheet", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_missingPrefixAndInvalidIndex_reportsMissingPrefix() {
        assertParseFailure(parser, "0 title/Complete algebra worksheet s/MATH",
                String.format(MESSAGE_MISSING_PREFIXES, "due/"));
    }

    @Test
    public void parse_invalidIndexAndInvalidTitle_reportsIndex() {
        assertParseFailure(parser, "0 title/ s/MATH due/2026-10-15", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);
    }

    @Test
    public void parse_invalidTitleAndInvalidSubject_reportsTitle() {
        assertParseFailure(parser, "1 title/ s/BIOLOGY due/2026-10-15", Title.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidSubjectAndInvalidDueDate_reportsSubject() {
        assertParseFailure(parser, "1 title/Complete algebra worksheet s/BIOLOGY due/2026-02-30",
                TuitionSubject.MESSAGE_CONSTRAINTS);
    }
}
