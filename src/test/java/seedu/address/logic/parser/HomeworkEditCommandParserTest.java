package seedu.address.logic.parser;

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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.HomeworkEditCommand;
import seedu.address.logic.commands.HomeworkEditCommand.EditHomeworkDescriptor;
import seedu.address.model.homework.DueDate;
import seedu.address.model.homework.Title;
import seedu.address.model.homework.TitleTest;
import seedu.address.model.student.TuitionSubject;

public class HomeworkEditCommandParserTest {
    /** Fixed clock at 2026-10-05, so short-form due dates always infer the same year and today never changes. */
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-05T00:00:00Z"), ZoneOffset.UTC);
    private static final String USAGE_LINE = "\nUsage: " + HomeworkEditCommand.MESSAGE_USAGE;
    private static final String EXPECTED_WORDS = "STUDENT_INDEX HOMEWORK_INDEX before the first prefix";
    private static final Index FIRST = Index.fromOneBased(1);
    private static final Index SECOND = Index.fromOneBased(2);
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 5);
    private static final Title TITLE = new Title("Complete algebra questions 1-10");

    private final HomeworkEditCommandParser parser = new HomeworkEditCommandParser(CLOCK);

    @Test
    public void parse_titleAliasOnly_success() {
        assertParseSuccess(parser, "1 2 t/Complete algebra questions 1-10",
                editCommand(new EditHomeworkDescriptor(TITLE, null, null), false));
    }

    @Test
    public void parse_titleOnly_success() {
        assertParseSuccess(parser, "1 2 title/Complete algebra questions 1-10",
                editCommand(new EditHomeworkDescriptor(TITLE, null, null), false));
    }

    @Test
    public void parse_subjectOnly_success() {
        // case and surrounding whitespace ignored
        assertParseSuccess(parser, "1 2 s/  physics ",
                editCommand(new EditHomeworkDescriptor(null, TuitionSubject.PHYSICS, null), false));
    }

    @Test
    public void parse_fullDueDateOnly_success() {
        assertParseSuccess(parser, "2 1 due/2026-11-05", new HomeworkEditCommand(SECOND, FIRST,
                new EditHomeworkDescriptor(null, null, DueDate.parseFullDate("2026-11-05")), false, TODAY));
    }

    @Test
    public void parse_pastFullDueDate_success() {
        assertParseSuccess(parser, "1 2 due/2025-01-01",
                editCommand(new EditHomeworkDescriptor(null, null, DueDate.parseFullDate("2025-01-01")), false));
    }

    @Test
    public void parse_shortDueDate_infersYear() {
        // 10-04 is before the clock's date, so it is in the next year
        assertParseSuccess(parser, "1 2 due/10-4",
                editCommand(new EditHomeworkDescriptor(null, null, DueDate.parseFullDate("2027-10-04")), true));
        assertParseSuccess(parser, "1 2 due/11-05",
                editCommand(new EditHomeworkDescriptor(null, null, DueDate.parseFullDate("2026-11-05")), true));
    }

    @Test
    public void parse_allFieldsInAnyOrder_success() {
        HomeworkEditCommand expectedCommand = editCommand(new EditHomeworkDescriptor(new Title("Revise mechanics"),
                TuitionSubject.PHYSICS, DueDate.parseFullDate("2026-11-10")), false);

        assertParseSuccess(parser, "1 2 t/Revise mechanics s/PHYSICS due/2026-11-10", expectedCommand);
        assertParseSuccess(parser, "  1   2   due/2026-11-10 s/PHYSICS title/  Revise   mechanics  ",
                expectedCommand);
    }

    @Test
    public void parse_quotedValues_success() {
        HomeworkEditCommand expectedCommand = editCommand(new EditHomeworkDescriptor(new Title("Speed in km/h"),
                TuitionSubject.PHYSICS, DueDate.parseFullDate("2026-11-10")), false);

        assertParseSuccess(parser, "1 2 title/\"Speed in km/h\" s/\"PHYSICS\" due/\"2026-11-10\"",
                expectedCommand);
    }

    @Test
    public void parse_noIndices_failure() {
        assertParseFailure(parser, "", unexpectedWords(EXPECTED_WORDS, "none", HomeworkEditCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "t/Algebra", unexpectedWords(EXPECTED_WORDS, "none",
                HomeworkEditCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "t/X", unexpectedWords(EXPECTED_WORDS, "none", HomeworkEditCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_oneIndex_failure() {
        assertParseFailure(parser, "1 t/Algebra", unexpectedWords(EXPECTED_WORDS, "1 word (1)",
                HomeworkEditCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_threeIndices_failure() {
        assertParseFailure(parser, "1 2 3 t/Algebra", unexpectedWords(EXPECTED_WORDS, "3 words (1 2 3)",
                HomeworkEditCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "1 2 3 t/X", unexpectedWords(EXPECTED_WORDS, "3 words (1 2 3)",
                HomeworkEditCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "1 2 extra t/X", unexpectedWords(EXPECTED_WORDS, "3 words (1 2 extra)",
                HomeworkEditCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_noEditableField_failure() {
        assertParseFailure(parser, "1 2", HomeworkEditCommandParser.MESSAGE_NO_FIELDS);
        assertParseFailure(parser, "1 2", "Specify at least one field to edit: title/ (or t/), s/ or due/."
                + USAGE_LINE);
    }

    @Test
    public void parse_unknownPrefix_failure() {
        assertParseFailure(parser, "1 2 t/Algebra x/extra", notPrefix("x/")
                + titleQuoteHint("x/", TITLE_EXAMPLE_OTHER_PREFIX) + USAGE_LINE);
        assertParseFailure(parser, "1 2 t/Speed in km/h", notPrefix("km/")
                + titleQuoteHint("km/", TITLE_EXAMPLE_OTHER_PREFIX) + USAGE_LINE);
        // not in the title, so there is no hint to quote it
        assertParseFailure(parser, "1 2 s/MATH x/extra", notPrefix("x/") + USAGE_LINE);

        // score is recorded with homework marks, not edited here; it is not inside any value
        assertParseFailure(parser, "1 2 score/5/10", notPrefix("score/") + USAGE_LINE);
    }

    @Test
    public void parse_uppercasePrefix_failure() {
        assertParseFailure(parser, "1 2 T/Algebra", wrongCasePrefix("T/", "t/") + USAGE_LINE);
        assertParseFailure(parser, "1 2 Title/Algebra", wrongCasePrefix("Title/", "title/") + USAGE_LINE);
        // not inside any value, so no quote hint
        assertParseFailure(parser, "1 2 Due/2026-11-11", wrongCasePrefix("Due/", "due/") + USAGE_LINE);
        // not saved into the title, leaving the intended field unchanged
        assertParseFailure(parser, "1 2 t/X Due/2026-11-11", wrongCasePrefix("Due/", "due/")
                + titleQuoteHint("Due/", TITLE_EXAMPLE_OTHER_PREFIX) + USAGE_LINE);
        assertParseFailure(parser, "1 2 t/Y S/MATH", wrongCasePrefix("S/", "s/")
                + titleQuoteHint("S/", TITLE_EXAMPLE_OTHER_PREFIX) + USAGE_LINE);
        assertParseFailure(parser, "1 2 t/Z DUE/11-11", wrongCasePrefix("DUE/", "due/")
                + titleQuoteHint("DUE/", TITLE_EXAMPLE_OTHER_PREFIX) + USAGE_LINE);
        // after the subject, not in the title
        assertParseFailure(parser, "1 2 s/MATH Due/11-11", wrongCasePrefix("Due/", "due/") + USAGE_LINE);
    }

    @Test
    public void parse_quotedWrongCasePrefixInTitle_keptInTitle() {
        assertParseSuccess(parser, "1 2 t/\"X Due/2026-11-11\"",
                editCommand(new EditHomeworkDescriptor(new Title("X Due/2026-11-11"), null, null), false));
    }

    @Test
    public void parse_prefixGluedToTitle_keptInTitle() {
        // a prefix only counts after a space
        assertParseSuccess(parser, "1 2 t/Xs/MATH",
                editCommand(new EditHomeworkDescriptor(new Title("Xs/MATH"), null, null), false));
        assertParseSuccess(parser, "1 2 t/X,s/MATH",
                editCommand(new EditHomeworkDescriptor(new Title("X,s/MATH"), null, null), false));
    }

    @Test
    public void parse_repeatedPrefix_failure() {
        assertParseFailure(parser, "1 2 s/MATH s/PHYSICS", givenOnce("s/") + USAGE_LINE);
        assertParseFailure(parser, "1 2 due/11-05 due/11-06", givenOnce("due/") + USAGE_LINE);
        assertParseFailure(parser, "1 2 t/X t/Y", givenOnce("t/") + titleQuoteHint("t/", TITLE_EXAMPLE_TITLE_PREFIX)
                + USAGE_LINE);
        // r3-emu-01: two prefix-shaped words in the title
        assertParseFailure(parser, "1 2 t/Q1 t/f Q2 t/f", givenOnce("t/")
                + titleQuoteHint("t/", TITLE_EXAMPLE_TITLE_PREFIX) + USAGE_LINE);
        // r3-emu-02: a quote in the title before the prefix-shaped word
        assertParseFailure(parser, "1 2 t/Say \"hi\" t/f questions", givenOnce("t/")
                + titleQuoteHint("t/", TITLE_EXAMPLE_TITLE_PREFIX) + USAGE_LINE);
    }

    @Test
    public void parse_titleAndTitleAlias_reportsUseEither() {
        assertParseFailure(parser, "1 2 t/A title/B", MESSAGE_TITLE_AND_ALIAS
                + titleQuoteHint("title/", TITLE_EXAMPLE_TITLE_PREFIX) + USAGE_LINE);
        assertParseFailure(parser, "1 2 title/A t/B", MESSAGE_TITLE_AND_ALIAS
                + titleQuoteHint("t/", TITLE_EXAMPLE_TITLE_PREFIX) + USAGE_LINE);
        // the later one is not in a title
        assertParseFailure(parser, "1 2 t/A s/MATH title/B", MESSAGE_TITLE_AND_ALIAS + USAGE_LINE);
    }

    @Test
    public void parse_unknownPrefixAndTitleConflict_reportsUnknownPrefix() {
        assertParseFailure(parser, "1 2 t/A title/B x/C", notPrefix("x/")
                + titleQuoteHint("x/", TITLE_EXAMPLE_OTHER_PREFIX) + USAGE_LINE);
    }

    @Test
    public void parse_titleConflictAndInvalidIndex_reportsUseEither() {
        assertParseFailure(parser, "0 2 t/A title/B", MESSAGE_TITLE_AND_ALIAS
                + titleQuoteHint("title/", TITLE_EXAMPLE_TITLE_PREFIX) + USAGE_LINE);
    }

    @Test
    public void parse_invalidStudentIndex_failure() {
        // zero
        assertParseFailure(parser, "0 1 t/A", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // leading zero
        assertParseFailure(parser, "01 1 t/A", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // negative
        assertParseFailure(parser, "-1 1 t/A", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // plus sign
        assertParseFailure(parser, "+1 1 t/A", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // decimal
        assertParseFailure(parser, "1.0 1 t/A", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // word
        assertParseFailure(parser, "one 1 t/A", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);

        // larger than the largest int
        assertParseFailure(parser, "2147483648 1 t/X", Messages.MESSAGE_STUDENT_INDEX_TOO_LARGE);
    }

    @Test
    public void parse_largestIntIndices_success() {
        assertParseSuccess(parser, "2147483647 2147483647 t/Complete algebra questions 1-10",
                new HomeworkEditCommand(Index.fromOneBased(Integer.MAX_VALUE), Index.fromOneBased(Integer.MAX_VALUE),
                        new EditHomeworkDescriptor(TITLE, null, null), false, TODAY));
    }

    @Test
    public void parse_invalidHomeworkIndex_failure() {
        // zero
        assertParseFailure(parser, "1 0 t/A", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);

        // leading zero
        assertParseFailure(parser, "1 01 t/A", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);

        // negative
        assertParseFailure(parser, "1 -1 t/A", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);

        // plus sign
        assertParseFailure(parser, "1 +1 t/A", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);

        // decimal
        assertParseFailure(parser, "1 1.0 t/A", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);

        // word
        assertParseFailure(parser, "1 one t/A", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);

        // larger than the largest int
        assertParseFailure(parser, "1 2147483648 t/A", Messages.MESSAGE_HOMEWORK_INDEX_TOO_LARGE);
    }

    @Test
    public void parse_bothIndicesInvalid_reportsStudentIndex() {
        assertParseFailure(parser, "0 0 t/A", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);
    }

    @Test
    public void parse_invalidIndexAndInvalidValue_reportsIndex() {
        assertParseFailure(parser, "1 0 s/BIOLOGY", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);
    }

    @Test
    public void parse_invalidTitle_failure() {
        // empty, r3-ald-06
        assertParseFailure(parser, "1 2 t/", TitleTest.MESSAGE_EMPTY);
        assertParseFailure(parser, "1 2 title/   s/MATH", TitleTest.MESSAGE_EMPTY);
        assertParseFailure(parser, "1 2 t/\"\"", TitleTest.MESSAGE_EMPTY);
        assertParseFailure(parser, "1 2 t/\" \" s/MATH", TitleTest.MESSAGE_EMPTY);

        // 101 characters
        assertParseFailure(parser, "1 2 t/" + "a".repeat(101),
                "Homework title must be at most 100 characters, but has 101.");
    }

    @Test
    public void parse_titleOfMaximumLength_success() {
        String longTitle = "a".repeat(100);
        assertParseSuccess(parser, "1 2 t/" + longTitle,
                editCommand(new EditHomeworkDescriptor(new Title(longTitle), null, null), false));
    }

    @Test
    public void parse_invalidSubject_failure() {
        // empty
        assertParseFailure(parser, "1 2 s/", MESSAGE_SUBJECT_EMPTY);

        // unsupported subject
        assertParseFailure(parser, "1 2 s/BIOLOGY", notSubject("BIOLOGY"));

        // two subjects
        assertParseFailure(parser, "1 2 s/MATH, PHYSICS", notSubject("MATH, PHYSICS"));
    }

    @Test
    public void parse_subjectPrefixWordInTitle_namesValueAndHintsToQuoteTitle() {
        // r3-emu-03: s/ is optional in edit, so the rest of the title is read as the subject
        String quoteHint = titleQuoteHint("s/", TITLE_EXAMPLE_SUBJECT_PREFIX);
        assertParseFailure(parser, "1 2 t/Read s/o notes", notSubject("o notes") + quoteHint);
        assertParseFailure(parser, "1 2 t/Physics s/w set", notSubject("w set") + quoteHint);
    }

    @Test
    public void parse_invalidDueDate_failure() {
        // empty
        assertParseFailure(parser, "1 2 due/", MESSAGE_EMPTY);

        // wrong format
        assertParseFailure(parser, "1 2 due/05/11/2026", wrongFormatMessage("05/11/2026"));
        assertParseFailure(parser, "1 2 due/30-10-2026", wrongFormatMessage("30-10-2026"));

        // date that does not exist
        assertParseFailure(parser, "1 2 due/2026-02-30", notRealDateMessage("2026-02-30"));

        // space inside
        assertParseFailure(parser, "1 2 due/2026-11 -05", wrongFormatMessage("2026-11 -05"));

        // typo year
        assertParseFailure(parser, "1 2 due/2206-10-30", MESSAGE_YEAR_OUT_OF_RANGE);

        // today is 2026-10-05, and neither 2026 nor 2027 is a leap year
        assertParseFailure(parser, "1 2 due/2-29", leapDayMessage(2028));
    }

    @Test
    public void parse_invalidTitleAndSubject_reportsTitle() {
        assertParseFailure(parser, "1 2 s/BIOLOGY t/", TitleTest.MESSAGE_EMPTY);
    }

    @Test
    public void parse_invalidSubjectAndDueDate_reportsSubject() {
        assertParseFailure(parser, "1 2 due/2026-02-30 s/BIOLOGY", notSubject("BIOLOGY"));
    }

    /**
     * Returns a command that edits the 2nd homework of the 1st student with {@code descriptor}.
     */
    private static HomeworkEditCommand editCommand(EditHomeworkDescriptor descriptor, boolean isYearInferred) {
        return new HomeworkEditCommand(FIRST, SECOND, descriptor, isYearInferred, TODAY);
    }
}
