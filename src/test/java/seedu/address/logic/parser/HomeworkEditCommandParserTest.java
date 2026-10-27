package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.MESSAGE_TITLE_AND_ALIAS;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.CommandParserTestUtil.notPrefix;
import static seedu.address.logic.parser.CommandParserTestUtil.notPrefixWithQuoteHint;

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
import seedu.address.model.student.TuitionSubject;

public class HomeworkEditCommandParserTest {
    /** Fixed clock at 2026-10-05, so short-form due dates always infer the same year and today never changes. */
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-05T00:00:00Z"), ZoneOffset.UTC);
    private static final String USAGE_LINE = "\nUsage: " + HomeworkEditCommand.MESSAGE_USAGE;
    private static final String MESSAGE_INVALID_FORMAT = "Invalid command format.\n"
            + "Usage: homework|hw edit|e STUDENT_INDEX HOMEWORK_INDEX [title/|t/TITLE] [s/SUBJECT] "
            + "[due/YYYY-MM-DD|MM-DD]";
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
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "t/Algebra", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_oneIndex_failure() {
        assertParseFailure(parser, "1 t/Algebra", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_threeIndices_failure() {
        assertParseFailure(parser, "1 2 3 t/Algebra", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_noEditableField_failure() {
        assertParseFailure(parser, "1 2", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_unknownPrefix_failure() {
        assertParseFailure(parser, "1 2 t/Algebra x/extra",
                notPrefixWithQuoteHint("x/", "t/\"Algebra x/extra\"") + USAGE_LINE);
        assertParseFailure(parser, "1 2 t/Speed in km/h", notPrefixWithQuoteHint("km/", "t/\"Speed in km/h\"")
                + USAGE_LINE);

        // score is recorded with homework marks, not edited here; it is not inside any value
        assertParseFailure(parser, "1 2 score/5/10", notPrefix("score/") + USAGE_LINE);
    }

    @Test
    public void parse_uppercasePrefix_failure() {
        assertParseFailure(parser, "1 2 T/Algebra", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_repeatedPrefix_failure() {
        assertParseFailure(parser, "1 2 s/MATH s/PHYSICS",
                String.format(StrictArgumentTokenizer.MESSAGE_REPEATED_PREFIX, "s/"));
        assertParseFailure(parser, "1 2 due/11-05 due/11-06",
                String.format(StrictArgumentTokenizer.MESSAGE_REPEATED_PREFIX, "due/"));
        assertParseFailure(parser, "1 2 t/A t/B",
                String.format(StrictArgumentTokenizer.MESSAGE_REPEATED_PREFIX, "t/"));
    }

    @Test
    public void parse_titleAndTitleAlias_reportsUseEither() {
        assertParseFailure(parser, "1 2 t/A title/B", MESSAGE_TITLE_AND_ALIAS);
        assertParseFailure(parser, "1 2 title/A t/B", MESSAGE_TITLE_AND_ALIAS);
    }

    @Test
    public void parse_unknownPrefixAndTitleConflict_reportsUnknownPrefix() {
        assertParseFailure(parser, "1 2 t/A title/B x/C", notPrefixWithQuoteHint("x/", "title/\"B x/C\"")
                + USAGE_LINE);
    }

    @Test
    public void parse_titleConflictAndInvalidIndex_reportsUseEither() {
        assertParseFailure(parser, "0 2 t/A title/B", MESSAGE_TITLE_AND_ALIAS);
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
        assertParseFailure(parser, "2147483648 1 t/A", Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);
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
        assertParseFailure(parser, "1 2147483648 t/A", Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);
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
        // empty
        assertParseFailure(parser, "1 2 t/", Title.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 2 title/   s/MATH", Title.MESSAGE_CONSTRAINTS);

        // 101 characters
        assertParseFailure(parser, "1 2 t/" + "a".repeat(101), Title.MESSAGE_CONSTRAINTS);
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
        assertParseFailure(parser, "1 2 s/", TuitionSubject.MESSAGE_CONSTRAINTS);

        // unsupported subject
        assertParseFailure(parser, "1 2 s/BIOLOGY", TuitionSubject.MESSAGE_CONSTRAINTS);

        // two subjects
        assertParseFailure(parser, "1 2 s/MATH, PHYSICS", TuitionSubject.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidDueDate_failure() {
        // empty
        assertParseFailure(parser, "1 2 due/", DueDate.MESSAGE_CONSTRAINTS);

        // wrong format
        assertParseFailure(parser, "1 2 due/05/11/2026", DueDate.MESSAGE_CONSTRAINTS);

        // date that does not exist
        assertParseFailure(parser, "1 2 due/2026-02-30", DueDate.MESSAGE_CONSTRAINTS);

        // space inside
        assertParseFailure(parser, "1 2 due/2026-11 -05", DueDate.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidTitleAndSubject_reportsTitle() {
        assertParseFailure(parser, "1 2 s/BIOLOGY t/", Title.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidSubjectAndDueDate_reportsSubject() {
        assertParseFailure(parser, "1 2 due/2026-02-30 s/BIOLOGY", TuitionSubject.MESSAGE_CONSTRAINTS);
    }

    /**
     * Returns a command that edits the 2nd homework of the 1st student with {@code descriptor}.
     */
    private static HomeworkEditCommand editCommand(EditHomeworkDescriptor descriptor, boolean isYearInferred) {
        return new HomeworkEditCommand(FIRST, SECOND, descriptor, isYearInferred, TODAY);
    }
}
