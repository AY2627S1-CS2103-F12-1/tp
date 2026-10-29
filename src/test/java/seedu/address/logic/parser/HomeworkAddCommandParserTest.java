package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.MESSAGE_CURLY_QUOTES;
import static seedu.address.logic.parser.CommandParserTestUtil.MESSAGE_SUBJECT_EMPTY;
import static seedu.address.logic.parser.CommandParserTestUtil.MESSAGE_TITLE_AND_ALIAS;
import static seedu.address.logic.parser.CommandParserTestUtil.TITLE_EXAMPLE_OTHER_PREFIX;
import static seedu.address.logic.parser.CommandParserTestUtil.TITLE_EXAMPLE_SUBJECT_PREFIX;
import static seedu.address.logic.parser.CommandParserTestUtil.TITLE_EXAMPLE_TITLE_PREFIX;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.CommandParserTestUtil.givenOnce;
import static seedu.address.logic.parser.CommandParserTestUtil.notPrefix;
import static seedu.address.logic.parser.CommandParserTestUtil.notSubject;
import static seedu.address.logic.parser.CommandParserTestUtil.titleQuoteHint;
import static seedu.address.logic.parser.CommandParserTestUtil.unexpectedWords;
import static seedu.address.logic.parser.CommandParserTestUtil.wrongCasePrefix;
import static seedu.address.model.homework.DueDateTest.MESSAGE_EMPTY;
import static seedu.address.model.homework.DueDateTest.MESSAGE_YEAR_OUT_OF_RANGE;
import static seedu.address.model.homework.DueDateTest.leapDayMessage;
import static seedu.address.model.homework.DueDateTest.notRealDateMessage;
import static seedu.address.model.homework.DueDateTest.wrongFormatMessage;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalHomeworks.MECHANICS;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.HomeworkAddCommand;
import seedu.address.model.homework.DueDate;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.Title;
import seedu.address.model.homework.TitleTest;
import seedu.address.model.student.TuitionSubject;
import seedu.address.testutil.HomeworkBuilder;

public class HomeworkAddCommandParserTest {
    /** Fixed clock at 2026-10-05, so short-form due dates always infer the same year and today never changes. */
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-05T00:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 5);
    private static final String VALID_FIELDS = " title/Complete algebra worksheet s/MATH due/2026-10-15";
    private static final String USAGE_LINE = "\nUsage: " + HomeworkAddCommand.MESSAGE_USAGE;
    private static final String EXPECTED_WORDS = "STUDENT_INDEX before the first prefix";
    private static final String MESSAGE_ZERO_WIDTH_SPACE = "Homework title has an unsupported character: U+200B "
            + "(ZERO WIDTH SPACE). Titles may contain only letters, numbers, punctuation, symbols and spaces.";
    private static final String MESSAGE_MISSING_PREFIXES = "Missing required prefix(es): %s\n"
            + "Usage: homework|hw add STUDENT_INDEX title/|t/TITLE s/SUBJECT due/YYYY-MM-DD|MM-DD";

    private final HomeworkAddCommandParser parser = new HomeworkAddCommandParser(CLOCK);

    @Test
    public void parse_titlePrefix_success() {
        assertParseSuccess(parser, "1 title/Complete algebra worksheet s/MATH due/2026-10-15",
                new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, false, TODAY));
    }

    @Test
    public void parse_titleAliasPrefix_success() {
        assertParseSuccess(parser, "2 t/Attempt mechanics questions 1-5 s/PHYSICS due/2026-10-20",
                new HomeworkAddCommand(Index.fromOneBased(2), MECHANICS, false, TODAY));
    }

    @Test
    public void parse_prefixesInAnyOrderLowercaseSubject_success() {
        Homework expectedHomework = new HomeworkBuilder().withTitle("Revise atomic structure")
                .withSubject("CHEMISTRY").withDueDate("2026-10-18").build();

        assertParseSuccess(parser, "3 due/2026-10-18 s/chemistry title/Revise atomic structure",
                new HomeworkAddCommand(Index.fromOneBased(3), expectedHomework, false, TODAY));
    }

    @Test
    public void parse_extraWhitespace_success() {
        assertParseSuccess(parser, "  1   title/  Complete   algebra worksheet  s/  Math  due/ 2026-10-15  ",
                new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, false, TODAY));
    }

    @Test
    public void parse_largestStudentIndex_success() {
        assertParseSuccess(parser, "2147483647" + VALID_FIELDS,
                new HomeworkAddCommand(Index.fromOneBased(Integer.MAX_VALUE), ALGEBRA, false, TODAY));
    }

    @Test
    public void parse_pastFullDate_success() {
        Homework expectedHomework = new HomeworkBuilder().withDueDate("2025-01-01").build();

        assertParseSuccess(parser, "1 title/Complete algebra worksheet s/MATH due/2025-01-01",
                new HomeworkAddCommand(Index.fromOneBased(1), expectedHomework, false, TODAY));

        // second year of the allowed range
        assertParseSuccess(parser, "1 title/Complete algebra worksheet s/MATH due/2001-01-01",
                new HomeworkAddCommand(Index.fromOneBased(1), new HomeworkBuilder().withDueDate("2001-01-01").build(),
                        false, TODAY));
    }

    @Test
    public void parse_leapDayFullDate_success() {
        Homework expectedHomework = new HomeworkBuilder().withDueDate("2028-02-29").build();

        assertParseSuccess(parser, "1 title/Complete algebra worksheet s/MATH due/2028-02-29",
                new HomeworkAddCommand(Index.fromOneBased(1), expectedHomework, false, TODAY));
    }

    @Test
    public void parse_shortDateAfterToday_infersCurrentYear() {
        assertParseSuccess(parser, "1 title/Complete algebra worksheet s/MATH due/10-15",
                new HomeworkAddCommand(Index.fromOneBased(1), ALGEBRA, true, TODAY));
    }

    @Test
    public void parse_shortDateToday_infersCurrentYear() {
        Homework expectedHomework = new HomeworkBuilder().withDueDate("2026-10-05").build();

        assertParseSuccess(parser, "1 title/Complete algebra worksheet s/MATH due/10-05",
                new HomeworkAddCommand(Index.fromOneBased(1), expectedHomework, true, TODAY));
    }

    @Test
    public void parse_shortDateBeforeToday_infersNextYear() {
        Homework expectedHomework = new HomeworkBuilder().withDueDate("2027-10-04").build();

        assertParseSuccess(parser, "1 title/Complete algebra worksheet s/MATH due/10-04",
                new HomeworkAddCommand(Index.fromOneBased(1), expectedHomework, true, TODAY));
    }

    @Test
    public void parse_shortDateSingleDigitDay_infersCurrentYear() {
        Homework expectedHomework = new HomeworkBuilder().withDueDate("2026-10-05").build();

        assertParseSuccess(parser, "1 title/Complete algebra worksheet s/MATH due/10-5",
                new HomeworkAddCommand(Index.fromOneBased(1), expectedHomework, true, TODAY));
    }

    @Test
    public void parse_shortDateSingleDigitMonthAndDay_infersNextYear() {
        Homework expectedHomework = new HomeworkBuilder().withDueDate("2027-09-09").build();

        assertParseSuccess(parser, "1 title/Complete algebra worksheet s/MATH due/9-9",
                new HomeworkAddCommand(Index.fromOneBased(1), expectedHomework, true, TODAY));
    }

    @Test
    public void parse_fullDateSingleDigitMonthAndDay_success() {
        Homework expectedHomework = new HomeworkBuilder().withDueDate("2026-02-05").build();

        assertParseSuccess(parser, "1 title/Complete algebra worksheet s/MATH due/2026-2-5",
                new HomeworkAddCommand(Index.fromOneBased(1), expectedHomework, false, TODAY));
    }

    @Test
    public void parse_titleWithSlash_success() {
        Homework expectedHomework = new Homework(new Title("Exercise 3/4 and 5/6"), TuitionSubject.MATH,
                DueDate.parse("2026-10-15", HomeworkBuilder.DEFAULT_TODAY));

        assertParseSuccess(parser, "1 title/Exercise 3/4 and 5/6 s/MATH due/2026-10-15",
                new HomeworkAddCommand(Index.fromOneBased(1), expectedHomework, false, TODAY));
    }

    @Test
    public void parse_titleWithSpacedOrCapitalizedSlashWord_success() {
        Homework expectedHomework = new HomeworkBuilder().withTitle("Speed in km / h, And/or units").build();

        assertParseSuccess(parser, "1 title/Speed in km / h, And/or units s/MATH due/2026-10-15",
                new HomeworkAddCommand(Index.fromOneBased(1), expectedHomework, false, TODAY));
    }

    @Test
    public void parse_quotedTitleWithLowercaseSlashWord_success() {
        Homework expectedHomework = new HomeworkBuilder().withTitle("Speed in km/h").withSubject("PHYSICS")
                .withDueDate("2026-11-01").build();

        assertParseSuccess(parser, "1 t/\"Speed in km/h\" s/PHYSICS due/2026-11-01",
                new HomeworkAddCommand(Index.fromOneBased(1), expectedHomework, false, TODAY));

        // spaces at both ends inside the quotes are removed
        assertParseSuccess(parser, "1 t/\"  Speed in km/h  \" s/PHYSICS due/2026-11-01",
                new HomeworkAddCommand(Index.fromOneBased(1), expectedHomework, false, TODAY));
    }

    @Test
    public void parse_titleWithQuotesInside_keepsQuotes() {
        // not wrapped in quotes
        assertParseSuccess(parser, "1 t/Read \"Hamlet\" act 1 s/MATH due/2026-10-15", new HomeworkAddCommand(
                Index.fromOneBased(1), new HomeworkBuilder().withTitle("Read \"Hamlet\" act 1").build(), false, TODAY));

        // wrapped in quotes, with quotes inside
        assertParseSuccess(parser, "1 t/\"Read \"Hamlet\" km/h\" s/MATH due/2026-10-15", new HomeworkAddCommand(
                Index.fromOneBased(1), new HomeworkBuilder().withTitle("Read \"Hamlet\" km/h").build(), false, TODAY));
    }

    @Test
    public void parse_unclosedQuotedTitle_failure() {
        // km/ is read as an unknown prefix in the title
        assertParseFailure(parser, "1 t/\"Speed in km/h s/MATH due/2026-10-15", notPrefix("km/")
                + titleQuoteHint("km/", TITLE_EXAMPLE_OTHER_PREFIX) + USAGE_LINE);
    }

    @Test
    public void parse_noSpaceAfterClosingQuote_throwsParseExceptionNamingWord() {
        assertParseFailure(parser, "1 t/\"Speed in km/h\"s/PHYSICS due/2026-11-01", notPrefix("km/")
                + titleQuoteHint("km/", TITLE_EXAMPLE_OTHER_PREFIX) + USAGE_LINE);
    }

    @Test
    public void parse_curlyQuotes_throwsParseExceptionWithStraightQuoteHint() {
        assertParseFailure(parser, "1 t/“Speed in km/h” s/PHYSICS due/2026-11-01", notPrefix("km/")
                + titleQuoteHint("km/", TITLE_EXAMPLE_OTHER_PREFIX) + "\n" + MESSAGE_CURLY_QUOTES + USAGE_LINE);
    }

    @Test
    public void parse_titleQuoteHintExamples_giveExampleTitles() {
        // each example in the hint, typed as shown, gives the title in it
        assertParseSuccess(parser, "1 " + TITLE_EXAMPLE_TITLE_PREFIX + " s/MATH due/2026-10-15",
                new HomeworkAddCommand(Index.fromOneBased(1), new HomeworkBuilder().withTitle("True t/f quiz").build(),
                        false, TODAY));
        assertParseSuccess(parser, "1 " + TITLE_EXAMPLE_SUBJECT_PREFIX + " s/MATH due/2026-10-15",
                new HomeworkAddCommand(Index.fromOneBased(1), new HomeworkBuilder().withTitle("Read s/o notes")
                        .build(), false, TODAY));
        assertParseSuccess(parser, "1 " + TITLE_EXAMPLE_OTHER_PREFIX + " s/MATH due/2026-10-15",
                new HomeworkAddCommand(Index.fromOneBased(1), new HomeworkBuilder().withTitle("Pg 5 w/ answers")
                        .build(), false, TODAY));
    }

    @Test
    public void parse_quotesInUnquotedTitle_exampleWithoutUserText() {
        // r3-ald-02: the example does not repeat the title, whose own quotes would end a quoted title early
        assertParseFailure(parser, "1 t/\"Hamlet\" and/or Macbeth s/MATH due/2026-12-01", notPrefix("and/")
                + titleQuoteHint("and/", TITLE_EXAMPLE_OTHER_PREFIX) + USAGE_LINE);
        assertParseFailure(parser, "1 t/Read \"Hamlet\" s/o notes s/MATH due/2026-12-01", givenOnce("s/")
                + titleQuoteHint("s/", TITLE_EXAMPLE_SUBJECT_PREFIX) + USAGE_LINE);
    }

    @Test
    public void parse_emptyQuotedTitle_failure() {
        // r3-ald-06
        assertParseFailure(parser, "1 t/\"\" s/MATH due/2026-10-15", TitleTest.MESSAGE_EMPTY);

        // only spaces inside the quotes
        assertParseFailure(parser, "1 t/\" \" s/MATH due/2026-10-15", TitleTest.MESSAGE_EMPTY);
    }

    @Test
    public void parse_titleWithLowercaseSlashWord_failure() {
        // km/ is read as an unknown prefix
        assertParseFailure(parser, "1 title/Speed in km/h s/MATH due/2026-10-15", notPrefix("km/")
                + titleQuoteHint("km/", TITLE_EXAMPLE_OTHER_PREFIX) + USAGE_LINE);
        assertParseFailure(parser, "1 t/Speed in km/h s/PHYSICS due/2026-11-01", notPrefix("km/")
                + titleQuoteHint("km/", TITLE_EXAMPLE_OTHER_PREFIX) + USAGE_LINE);

        // and/ is read as an unknown prefix
        assertParseFailure(parser, "1 t/Read and/or summarize s/MATH due/2026-10-15", notPrefix("and/")
                + titleQuoteHint("and/", TITLE_EXAMPLE_OTHER_PREFIX) + USAGE_LINE);

        // w/ followed by a space is read as an unknown prefix
        assertParseFailure(parser, "1 t/Kinematics w/ solutions s/PHYSICS due/2026-11-01", notPrefix("w/")
                + titleQuoteHint("w/", TITLE_EXAMPLE_OTHER_PREFIX) + USAGE_LINE);
    }

    @Test
    public void parse_emptyArguments_failure() {
        assertParseFailure(parser, "", unexpectedWords(EXPECTED_WORDS, "none", HomeworkAddCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_missingStudentIndex_failure() {
        assertParseFailure(parser, VALID_FIELDS, unexpectedWords(EXPECTED_WORDS, "none",
                HomeworkAddCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_indexAfterPrefixes_reportsMissingIndex() {
        // the index is read as part of the due date, so the message names the missing index, not the date
        assertParseFailure(parser, "t/Essay s/MATH due/2026-11-20 1", unexpectedWords(EXPECTED_WORDS, "none",
                HomeworkAddCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "t/Algebra s/MATH due/2026-12-01 1", unexpectedWords(EXPECTED_WORDS, "none",
                HomeworkAddCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_extraPreambleWord_failure() {
        assertParseFailure(parser, "1 2" + VALID_FIELDS, unexpectedWords(EXPECTED_WORDS, "2 words (1 2)",
                HomeworkAddCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "1 extra t/A s/MATH due/2026-11-01", unexpectedWords(EXPECTED_WORDS,
                "2 words (1 extra)", HomeworkAddCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_unknownPrefix_failure() {
        // x/ follows the due date, not the title, so there is no hint to quote the title
        assertParseFailure(parser, "1" + VALID_FIELDS + " x/extra", notPrefix("x/") + USAGE_LINE);
        // r3-ald-01
        assertParseFailure(parser, "1 t/Essay s/MATH due/tomorrow x/y", notPrefix("x/") + USAGE_LINE);
    }

    @Test
    public void parse_uppercasePrefix_failure() {
        assertParseFailure(parser, "1 TITLE/Complete algebra worksheet s/MATH due/2026-10-15",
                wrongCasePrefix("TITLE/", "title/") + USAGE_LINE);
        assertParseFailure(parser, "1 T/A s/MATH due/2026-11-01", wrongCasePrefix("T/", "t/") + USAGE_LINE);
        // S/MATH is not kept in the title, which would leave s/ missing
        assertParseFailure(parser, "1 t/A S/MATH due/2026-11-01", wrongCasePrefix("S/", "s/")
                + titleQuoteHint("S/", TITLE_EXAMPLE_OTHER_PREFIX) + USAGE_LINE);
        // r3-ald-01: after the subject, not in the title, so there is no hint to quote it
        assertParseFailure(parser, "1 t/Essay s/MATH Due/2026-12-01", wrongCasePrefix("Due/", "due/")
                + USAGE_LINE);
    }

    @Test
    public void parse_quotedWrongCasePrefixInTitle_keptInTitle() {
        Homework expectedHomework = new Homework(new Title("Notes S/MATH"), TuitionSubject.MATH,
                DueDate.parseFullDate("2026-11-01"));
        assertParseSuccess(parser, "1 t/\"Notes S/MATH\" s/MATH due/2026-11-01",
                new HomeworkAddCommand(Index.fromOneBased(1), expectedHomework, false, TODAY));
    }

    @Test
    public void parse_prefixGluedToTitle_reportsMissingSubject() {
        // a prefix only counts after a space, so s/MATH stays in the title
        assertParseFailure(parser, "1 t/Xs/MATH due/2026-11-11", String.format(MESSAGE_MISSING_PREFIXES, "s/"));
    }

    @Test
    public void parse_repeatedSubjectPrefix_failure() {
        // r4-01: the first s/ follows the title, but starts with a subject, so is not part of it
        assertParseFailure(parser, "1" + VALID_FIELDS + " s/PHYSICS", givenOnce("s/") + USAGE_LINE);
        assertParseFailure(parser, "1 t/A s/MATH due/2026-12-01 s/PHYSICS", givenOnce("s/") + USAGE_LINE);
        // in any case, as the subject is read
        assertParseFailure(parser, "1 t/A s/math, more due/2026-12-01 s/PHYSICS", givenOnce("s/") + USAGE_LINE);
        // the second s/ follows the title, but starts with a subject
        assertParseFailure(parser, "1 s/PHYSICS t/A s/MATH due/2026-12-01", givenOnce("s/") + USAGE_LINE);
        // neither s/ follows the title
        assertParseFailure(parser, "1 s/MATH t/Essay due/2026-10-15 s/PHYSICS", givenOnce("s/") + USAGE_LINE);
    }

    @Test
    public void parse_repeatedDueDatePrefix_failure() {
        // r4-01: the first due/ follows the title, but starts with a due date, so is not part of it
        assertParseFailure(parser, "1 t/Essay due/2026-12-01 s/MATH due/2026-12-02", givenOnce("due/")
                + USAGE_LINE);
        assertParseFailure(parser, "1 t/Essay due/12-01 s/MATH due/12-02", givenOnce("due/") + USAGE_LINE);
        // not a due date the command accepts, so may be part of the title
        assertParseFailure(parser, "1 t/Essay due/2026-02-30 s/MATH due/2026-12-02", givenOnce("due/")
                + titleQuoteHint("due/", TITLE_EXAMPLE_OTHER_PREFIX) + USAGE_LINE);
        assertParseFailure(parser, "1 t/Essay due/02-29 s/MATH due/2026-12-02", givenOnce("due/")
                + titleQuoteHint("due/", TITLE_EXAMPLE_OTHER_PREFIX) + USAGE_LINE);
    }

    @Test
    public void parse_repeatedTitleAliasPrefix_failure() {
        assertParseFailure(parser, "1 t/First t/Second s/MATH due/2026-10-15", givenOnce("t/")
                + titleQuoteHint("t/", TITLE_EXAMPLE_TITLE_PREFIX) + USAGE_LINE);
    }

    @Test
    public void parse_prefixWordInTitle_hintsToQuoteTitle() {
        // the second t/ is in the title
        assertParseFailure(parser, "1 t/Revise t/f questions s/MATH due/2026-12-01", givenOnce("t/")
                + titleQuoteHint("t/", TITLE_EXAMPLE_TITLE_PREFIX) + USAGE_LINE);
        // the first s/ is in the title
        assertParseFailure(parser, "1 t/Read s/o notes s/MATH due/2026-12-01", givenOnce("s/")
                + titleQuoteHint("s/", TITLE_EXAMPLE_SUBJECT_PREFIX) + USAGE_LINE);
        assertParseFailure(parser, "1 t/Read title/abstract s/MATH due/2026-12-01", MESSAGE_TITLE_AND_ALIAS
                + titleQuoteHint("title/", TITLE_EXAMPLE_TITLE_PREFIX) + USAGE_LINE);
    }

    @Test
    public void parse_prefixWordInTitleNotNextToRepeat_hintsToQuoteTitle() {
        // r3-ald-03: the first due/ is in the title, though the second does not follow it
        assertParseFailure(parser, "1 t/Revise due/dates s/MATH due/2026-12-02", givenOnce("due/")
                + titleQuoteHint("due/", TITLE_EXAMPLE_OTHER_PREFIX) + USAGE_LINE);
        assertParseFailure(parser, "1 t/Revise s/o notes due/12-02 s/MATH", givenOnce("s/")
                + titleQuoteHint("s/", TITLE_EXAMPLE_SUBJECT_PREFIX) + USAGE_LINE);
        // the second s/ is in the title
        assertParseFailure(parser, "1 s/MATH t/Read s/o notes due/12-02", givenOnce("s/")
                + titleQuoteHint("s/", TITLE_EXAMPLE_SUBJECT_PREFIX) + USAGE_LINE);
    }

    @Test
    public void parse_titleThenTitleAlias_reportsUseEither() {
        assertParseFailure(parser, "1 title/First t/Second s/MATH due/2026-10-15", MESSAGE_TITLE_AND_ALIAS
                + titleQuoteHint("t/", TITLE_EXAMPLE_TITLE_PREFIX) + USAGE_LINE);
    }

    @Test
    public void parse_titleAliasThenTitle_reportsUseEither() {
        assertParseFailure(parser, "1 t/First title/Second s/MATH due/2026-10-15", MESSAGE_TITLE_AND_ALIAS
                + titleQuoteHint("title/", TITLE_EXAMPLE_TITLE_PREFIX) + USAGE_LINE);
    }

    @Test
    public void parse_allPrefixesMissing_reportsAllInOrder() {
        assertParseFailure(parser, "1", String.format(MESSAGE_MISSING_PREFIXES, "title/ (or t/), s/, due/"));
    }

    @Test
    public void parse_titleMissing_failure() {
        assertParseFailure(parser, "1 s/MATH due/2026-10-15", String.format(MESSAGE_MISSING_PREFIXES,
                "title/ (or t/)"));
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

        // title alias, as in the developer guide's manual test
        assertParseFailure(parser, "1 t/A s/MATH", String.format(MESSAGE_MISSING_PREFIXES, "due/"));
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
        assertParseFailure(parser, "2147483648" + VALID_FIELDS, Messages.MESSAGE_STUDENT_INDEX_TOO_LARGE);
        assertParseFailure(parser, "9999999999" + VALID_FIELDS, Messages.MESSAGE_STUDENT_INDEX_TOO_LARGE);
    }

    @Test
    public void parse_emptyTitle_failure() {
        assertParseFailure(parser, "1 title/ s/MATH due/2026-10-15", TitleTest.MESSAGE_EMPTY);
    }

    @Test
    public void parse_emptyTitleAlias_failure() {
        // r3-ald-06
        assertParseFailure(parser, "1 t/ s/MATH due/2026-12-01", TitleTest.MESSAGE_EMPTY);
    }

    @Test
    public void parse_zeroWidthSpaceTitle_failure() {
        // a zero-width space (U+200B) followed by an ordinary space
        assertParseFailure(parser, "3 t/\u200B s/MATH due/2026-12-01", MESSAGE_ZERO_WIDTH_SPACE);

        // a zero-width space inside the title
        assertParseFailure(parser, "3 t/Alge\u200Bbra drill s/MATH due/2026-12-02", MESSAGE_ZERO_WIDTH_SPACE);
    }

    @Test
    public void parse_titleTooLong_failure() {
        assertParseFailure(parser, "1 title/" + "a".repeat(101) + " s/MATH due/2026-10-15",
                "Homework title must be at most 100 characters, but has 101.");
    }

    @Test
    public void parse_emptySubject_failure() {
        // no hint, as a title is unlikely to end with s/
        assertParseFailure(parser, "1 title/Complete algebra worksheet s/ due/2026-10-15", MESSAGE_SUBJECT_EMPTY);
        assertParseFailure(parser, "1 s/   t/Essay due/2026-10-15", MESSAGE_SUBJECT_EMPTY);
    }

    @Test
    public void parse_unsupportedSubject_failure() {
        // one word after the title, so more likely a mistyped subject than part of the title
        assertParseFailure(parser, "1 title/Complete algebra worksheet s/BIOLOGY due/2026-10-15",
                notSubject("BIOLOGY"));
        assertParseFailure(parser, "1 t/Read s/o due/2026-10-15", notSubject("o"));
        // s/ does not follow the title
        assertParseFailure(parser, "1 s/BIOLOGY t/Essay due/2026-10-15", notSubject("BIOLOGY"));
        assertParseFailure(parser, "1 t/\"Essay\" s/biology due/2026-10-15", notSubject("biology"));
    }

    @Test
    public void parse_subjectPrefixWordInTitle_namesValueAndHintsToQuoteTitle() {
        // r3-emu-03: without another s/, the rest of the title is read as the subject
        assertParseFailure(parser, "1 t/Read s/o notes due/2026-12-01", notSubject("o notes")
                + titleQuoteHint("s/", TITLE_EXAMPLE_SUBJECT_PREFIX));
    }

    @Test
    public void parse_multipleSubjects_failure() {
        // r4-02: starts with a subject, so is not the rest of the title
        assertParseFailure(parser, "1 title/Complete algebra worksheet s/MATH, PHYSICS due/2026-10-15",
                notSubject("MATH, PHYSICS"));
        assertParseFailure(parser, "1 t/Essay s/MATH PHYSICS due/2026-12-01", notSubject("MATH PHYSICS"));
        assertParseFailure(parser, "1 t/Essay s/MATH,PHYSICS due/2026-12-01", notSubject("MATH,PHYSICS"));
        assertParseFailure(parser, "1 t/Essay s/MATH 1 due/2026-12-01", notSubject("MATH 1"));
    }

    @Test
    public void parse_subjectStartingWithNonSubject_hintsToQuoteTitle() {
        // r4-02: Bio is not a subject, so s/ may be part of the title
        assertParseFailure(parser, "1 t/Essay s/Bio logy due/2026-12-01", notSubject("Bio logy")
                + titleQuoteHint("s/", TITLE_EXAMPLE_SUBJECT_PREFIX));
    }

    @Test
    public void parse_emptyDueDate_failure() {
        assertParseFailure(parser, "1 title/Complete algebra worksheet s/MATH due/", MESSAGE_EMPTY);
    }

    @Test
    public void parse_invalidDueDate_failure() {
        String fieldsWithoutDueDate = "1 title/Complete algebra worksheet s/MATH due/";

        // wrong separator and field order
        assertParseFailure(parser, fieldsWithoutDueDate + "15/10/2026", wrongFormatMessage("15/10/2026"));

        // day-month-year order
        assertParseFailure(parser, "1 t/X s/MATH due/30-10-2026", wrongFormatMessage("30-10-2026"));

        // three-digit month
        assertParseFailure(parser, fieldsWithoutDueDate + "010-05", wrongFormatMessage("010-05"));

        // single-digit short-form date that does not exist
        assertParseFailure(parser, fieldsWithoutDueDate + "2-30", notRealDateMessage("2-30"));

        // date that does not exist
        assertParseFailure(parser, fieldsWithoutDueDate + "2026-02-30", notRealDateMessage("2026-02-30"));

        // month 13
        assertParseFailure(parser, "1 t/X s/MATH due/2026-13-01", notRealDateMessage("2026-13-01"));

        // short-form date that never exists
        assertParseFailure(parser, fieldsWithoutDueDate + "02-30", notRealDateMessage("02-30"));
    }

    @Test
    public void parse_shortLeapDayNotWithinYear_throwsLeapDayMessage() {
        // today is 2026-10-05, and neither 2026 nor 2027 is a leap year
        assertParseFailure(parser, "1 t/X s/MATH due/2-29", leapDayMessage(2028));
    }

    @Test
    public void parse_dueDateYearOutOfRange_throwsYearMessage() {
        // typo year
        assertParseFailure(parser, "1 t/Typo year s/MATH due/2206-10-30", MESSAGE_YEAR_OUT_OF_RANGE);

        // day before the range
        assertParseFailure(parser, "1 t/X s/MATH due/1999-12-31", MESSAGE_YEAR_OUT_OF_RANGE);
    }

    @Test
    public void parse_unknownPrefixAndMissingPrefixes_reportsUnknownPrefix() {
        assertParseFailure(parser, "1 x/extra", notPrefix("x/") + USAGE_LINE);
    }

    @Test
    public void parse_unknownPrefixAndTitleAliasConflict_reportsUnknownPrefix() {
        assertParseFailure(parser, "1 t/Algebra title/Mechanics x/extra", notPrefix("x/")
                + titleQuoteHint("x/", TITLE_EXAMPLE_OTHER_PREFIX) + USAGE_LINE);
        assertParseFailure(parser, "1 x/extra t/Algebra title/Mechanics", notPrefix("x/") + USAGE_LINE);
    }

    @Test
    public void parse_repeatedPrefixAndTitleAliasConflict_reportsRepeatedPrefix() {
        assertParseFailure(parser, "1 t/Algebra title/Mechanics s/MATH s/PHYSICS", givenOnce("s/") + USAGE_LINE);
    }

    @Test
    public void parse_titleAliasConflictAndMissingIndex_reportsUseEither() {
        assertParseFailure(parser, "t/Algebra title/Mechanics", MESSAGE_TITLE_AND_ALIAS
                + titleQuoteHint("title/", TITLE_EXAMPLE_TITLE_PREFIX) + USAGE_LINE);
    }

    @Test
    public void parse_repeatedPrefixAndInvalidIndex_reportsRepeatedPrefix() {
        assertParseFailure(parser, "0" + VALID_FIELDS + " due/2026-10-16", givenOnce("due/") + USAGE_LINE);
    }

    @Test
    public void parse_extraPreambleWordAndMissingPrefixes_reportsInvalidFormat() {
        assertParseFailure(parser, "1 2 title/Complete algebra worksheet", unexpectedWords(EXPECTED_WORDS,
                "2 words (1 2)", HomeworkAddCommand.MESSAGE_USAGE));
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
        assertParseFailure(parser, "1 title/ s/BIOLOGY due/2026-10-15", TitleTest.MESSAGE_EMPTY);
    }

    @Test
    public void parse_invalidSubjectAndInvalidDueDate_reportsSubject() {
        assertParseFailure(parser, "1 title/Complete algebra worksheet s/BIOLOGY due/2026-02-30",
                notSubject("BIOLOGY"));
    }
}
