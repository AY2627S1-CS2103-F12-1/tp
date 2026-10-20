package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SearchCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class SearchCommandParserTest {
    private final SearchCommandParser parser = new SearchCommandParser();

    @Test
    public void parse_keyword_returnsSearchCommand() throws Exception {
        assertEquals(new SearchCommand("Mary Tan"), parser.parse(" Mary Tan "));
        assertEquals(new SearchCommand("9876"), parser.parse(" 9876"));
    }

    @Test
    public void parse_blankKeyword_throwsParseException() {
        assertThrows(ParseException.class, SearchCommand.MESSAGE_INVALID_FORMAT, () -> parser.parse(""));
        assertThrows(ParseException.class, SearchCommand.MESSAGE_INVALID_FORMAT, () -> parser.parse("   "));
    }
}
