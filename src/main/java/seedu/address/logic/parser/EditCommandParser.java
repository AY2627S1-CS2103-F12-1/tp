package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditStudentDescriptor;
import seedu.address.logic.parser.StrictArgumentTokenizer.TokenizedArguments;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.StudentName;
import seedu.address.model.student.StudentPhone;
import seedu.address.model.student.TuitionSubjects;

/** Parses a displayed student index and one or more replacement profile fields. */
public class EditCommandParser implements Parser<EditCommand> {
    private static final List<String> PREFIXES = List.of("n/", "l/", "s/", "p/", "gn/", "gp/");

    @Override
    public EditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        TokenizedArguments tokens = StrictArgumentTokenizer.tokenizeWithPreamble(args, PREFIXES,
                EditCommand.MESSAGE_INVALID_FORMAT, StudentAddParser::isPartOfName);
        Index index = ParserUtil.parseStrictIndex(tokens.preamble(), Messages.MESSAGE_INVALID_STUDENT_INDEX_SYNTAX);
        Map<String, String> fields = tokens.values();
        if (fields.isEmpty()) {
            throw new ParseException(EditCommand.MESSAGE_NO_FIELDS);
        }

        try {
            EditStudentDescriptor descriptor = new EditStudentDescriptor(
                    fields.containsKey("n/") ? new StudentName(fields.get("n/")) : null,
                    fields.containsKey("l/") ? AcademicLevel.parse(fields.get("l/")) : null,
                    fields.containsKey("s/") ? new TuitionSubjects(fields.get("s/")) : null,
                    fields.containsKey("p/") ? new StudentPhone(fields.get("p/")) : null,
                    fields.containsKey("gn/") ? new StudentName(fields.get("gn/")) : null,
                    fields.containsKey("gp/") ? new StudentPhone(fields.get("gp/")) : null);
            return new EditCommand(index, descriptor);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }
}
