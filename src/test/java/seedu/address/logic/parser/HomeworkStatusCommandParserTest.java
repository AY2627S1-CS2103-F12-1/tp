package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.HomeworkStatusCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.homework.HomeworkStatus;

public class HomeworkStatusCommandParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_statusAndCommandAlias_normalizeValues() throws Exception {
        assertEquals(new HomeworkStatusCommand(Index.fromOneBased(1), Index.fromOneBased(2), HomeworkStatus.COMPLETED),
                parser.parseCommand("hw status 1 2 status/\" completed \""));
        assertEquals(new HomeworkStatusCommand(Index.fromOneBased(1), Index.fromOneBased(2), HomeworkStatus.ASSIGNED),
                parser.parseCommand("homework status 1 2 status/assigned"));
    }

    @Test
    public void parse_invalidArguments_rejected() {
        for (String args : new String[]{"", "1", "1 2", "1 2 3 status/COMPLETED", "status/COMPLETED",
            "0 1 status/COMPLETED", "1 0 status/COMPLETED", "01 2 status/COMPLETED", "1 02 status/COMPLETED",
            "2147483648 1 status/COMPLETED", "1 2 status/", "1 2 status/done", "1 2 status/true",
            "1 2 status/COMPLETED extra", "1 2 status/COMPLETED status/ASSIGNED", "1 2 score/17",
            "1 2 status/COMPLETED x/value"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand("hw status " + args), args);
        }
    }
}
