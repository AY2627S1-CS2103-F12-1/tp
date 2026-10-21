package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.HomeworkStatusCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.homework.HomeworkStatus;

/**
 * Parses two indices and a required explicit status using the shared prefix and index rules.
 */
public class HomeworkStatusCommandParser implements Parser<HomeworkStatusCommand> {
    private static final String MESSAGE_FORMAT = String.format(Messages.MESSAGE_INVALID_FORMAT_WITH_USAGE,
            HomeworkStatusCommand.MESSAGE_USAGE);

    @Override
    public HomeworkStatusCommand parse(String args) throws ParseException {
        requireNonNull(args);
        var tokens = StrictArgumentTokenizer.tokenizeWithPreamble(args, List.of("status/"), MESSAGE_FORMAT);
        String[] indices = tokens.preamble().split("\\s+");
        if (indices.length != 2 || !tokens.values().containsKey("status/")) {
            throw new ParseException(MESSAGE_FORMAT);
        }
        var student = ParserUtil.parseStrictIndex(indices[0], Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);
        var homework = ParserUtil.parseStrictIndex(indices[1], Messages.MESSAGE_INVALID_HOMEWORK_INDEX_SYNTAX);
        try {
            return new HomeworkStatusCommand(student, homework, HomeworkStatus.parse(tokens.values().get("status/")));
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }
}
