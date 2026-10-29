package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.HomeworkAddCommandParser.ALLOWED_PREFIXES;
import static seedu.address.logic.parser.HomeworkAddCommandParser.PREFIX_DUE_DATE;
import static seedu.address.logic.parser.HomeworkAddCommandParser.PREFIX_SUBJECT;
import static seedu.address.logic.parser.HomeworkAddCommandParser.PREFIX_TITLE;
import static seedu.address.logic.parser.HomeworkAddCommandParser.PREFIX_TITLE_ALIAS;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.HomeworkEditCommand;
import seedu.address.logic.commands.HomeworkEditCommand.EditHomeworkDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.homework.DueDate;
import seedu.address.model.homework.Title;
import seedu.address.model.student.TuitionSubject;

/**
 * Parses the arguments of {@code homework edit} into a {@link HomeworkEditCommand}.
 * Errors are checked in this order, and only the first one found is reported:
 * unknown or repeated prefix (in the order the prefixes appear), both {@code title/} and {@code t/} given,
 * indices that are missing or not exactly two words, no field to edit, student index syntax, homework index syntax,
 * title, subject, and due date.
 */
public class HomeworkEditCommandParser implements Parser<HomeworkEditCommand> {
    private static final String MESSAGE_INVALID_FORMAT = String.format(Messages.MESSAGE_INVALID_FORMAT_WITH_USAGE,
            HomeworkEditCommand.MESSAGE_USAGE);

    private final Clock clock;

    /**
     * Creates a parser that takes today's date from {@code clock}, to infer the year of short-form due dates and to
     * decide which homework the command lists as upcoming.
     */
    public HomeworkEditCommandParser(Clock clock) {
        this.clock = requireNonNull(clock);
    }

    /**
     * Returns a command that edits the homework identified by the indices in {@code args} with the given values.
     *
     * @throws ParseException If the command structure or any value is invalid.
     */
    @Override
    public HomeworkEditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        StrictArgumentTokenizer.TokenizedArguments tokenizedArguments =
                StrictArgumentTokenizer.tokenizeWithPreamble(args, ALLOWED_PREFIXES, MESSAGE_INVALID_FORMAT);
        Map<String, String> values = tokenizedArguments.values();
        HomeworkAddCommandParser.validateTitlePrefixes(values);

        String[] indices = tokenizedArguments.preamble().split("\\s+");
        if (indices.length != 2 || values.isEmpty()) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }

        Index studentIndex = ParserUtil.parseStrictIndex(indices[0], Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX,
                Messages.MESSAGE_STUDENT_INDEX_TOO_LARGE);
        Index homeworkIndex = ParserUtil.parseStrictIndex(indices[1], Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX,
                Messages.MESSAGE_HOMEWORK_INDEX_TOO_LARGE);
        String rawTitle = values.getOrDefault(PREFIX_TITLE, values.get(PREFIX_TITLE_ALIAS));
        String rawSubject = values.get(PREFIX_SUBJECT);
        String rawDueDate = values.get(PREFIX_DUE_DATE);
        LocalDate today = LocalDate.now(clock);
        try {
            EditHomeworkDescriptor descriptor = new EditHomeworkDescriptor(
                    rawTitle == null ? null : new Title(rawTitle),
                    rawSubject == null ? null : TuitionSubject.parse(rawSubject),
                    rawDueDate == null ? null : DueDate.parse(rawDueDate, today));
            return new HomeworkEditCommand(studentIndex, homeworkIndex, descriptor,
                    rawDueDate != null && DueDate.isShortForm(rawDueDate), today);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }
}
