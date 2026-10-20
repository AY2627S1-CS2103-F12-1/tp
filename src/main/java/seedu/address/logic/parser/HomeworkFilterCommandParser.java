package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.HomeworkFilterCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.homework.DueDate;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.student.TuitionSubject;

/**
 * Parses an optional displayed student index, subject and exact due date using homework date conventions.
 */
public class HomeworkFilterCommandParser implements Parser<HomeworkFilterCommand> {
    private static final String MESSAGE_FORMAT = String.format(Messages.MESSAGE_INVALID_FORMAT_WITH_USAGE,
            HomeworkFilterCommand.MESSAGE_USAGE);
    private final Clock clock;

    /**
     * Creates a parser that infers short-form date years from {@code clock}.
     */
    public HomeworkFilterCommandParser(Clock clock) {
        this.clock = requireNonNull(clock);
    }

    @Override
    public HomeworkFilterCommand parse(String args) throws ParseException {
        requireNonNull(args);
        var tokens = StrictArgumentTokenizer.tokenizeWithPreamble(args, List.of("s/", "due/", "status/"),
                MESSAGE_FORMAT);
        Map<String, String> values = tokens.values();
        String preamble = tokens.preamble();
        if (preamble.isBlank() && values.isEmpty() || !preamble.isBlank() && preamble.split("\\s+").length != 1) {
            throw new ParseException(MESSAGE_FORMAT);
        }
        Optional<Index> student = preamble.isBlank() ? Optional.empty()
                : Optional.of(ParserUtil.parseStrictIndex(preamble, Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX));
        try {
            Optional<TuitionSubject> subject = values.containsKey("s/")
                    ? Optional.of(TuitionSubject.parse(values.get("s/"))) : Optional.empty();
            Optional<DueDate> due = values.containsKey("due/")
                    ? Optional.of(DueDate.parse(values.get("due/"), LocalDate.now(clock))) : Optional.empty();
            Optional<HomeworkStatus> status = values.containsKey("status/")
                    ? Optional.of(HomeworkStatus.parse(values.get("status/"))) : Optional.empty();
            return new HomeworkFilterCommand(student, subject, due, status);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }
}
