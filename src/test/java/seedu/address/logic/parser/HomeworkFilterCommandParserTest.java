package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.parser.CommandParserTestUtil.notPrefix;
import static seedu.address.logic.parser.CommandParserTestUtil.notPrefixWithQuoteHint;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.HomeworkFilterCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class HomeworkFilterCommandParserTest {
    private final AddressBookParser parser = new AddressBookParser(
            Clock.fixed(Instant.parse("2026-10-20T00:00:00Z"), ZoneOffset.UTC));

    @Test
    public void parse_aliasesQuotesAndPrefixOrder_equivalent() throws Exception {
        var command = parser.parseCommand("homework filter 1 s/MATH due/2026-10-21");
        assertEquals(command, parser.parseCommand("hw filter 1 due/\"10-21\" s/math"));
        assertNotEquals(command, parser.parseCommand("hw filter 2 due/10-21 s/MATH"));
        for (String input : new String[]{"1", "s/MATH", "due/2026-10-15", "due/10-21", "s/MATH due/10-21"}) {
            assertInstanceOf(HomeworkFilterCommand.class, parser.parseCommand("hw filter " + input));
        }
    }

    @Test
    public void parse_shortDate_rollsToNextYear() throws Exception {
        assertEquals(parser.parseCommand("hw filter due/2027-10-19"), parser.parseCommand("hw filter due/10-19"));
    }

    @Test
    public void parse_statusAloneOrCombined_normalizesAndParticipatesInEquality() throws Exception {
        assertEquals(parser.parseCommand("hw filter status/COMPLETED"),
                parser.parseCommand("homework filter status/\" completed \""));
        assertEquals(parser.parseCommand("hw filter 1 s/MATH due/10-21 status/ASSIGNED"),
                parser.parseCommand("hw filter 1 status/assigned due/2026-10-21 s/math"));
        assertNotEquals(parser.parseCommand("hw filter status/COMPLETED"),
                parser.parseCommand("hw filter status/ASSIGNED"));
    }

    @Test
    public void parse_invalidOrUnsupportedCriteria_rejected() {
        String[] invalid = {"", "1 2", "all", "0", "01", "-1", "2147483648", "s/", "due/",
            "s/BIOLOGY", "due/2026-02-29", "due/3", "due/2026-10-20 extra", "s/MATH extra",
            "s/MATH s/PHYSICS", "due/10-20 due/10-21", "status/", "status/done", "status/COMPLETED extra",
            "status/ASSIGNED status/COMPLETED", "score/17", "x/value"};
        for (String input : invalid) {
            assertThrows(ParseException.class, () -> parser.parseCommand("hw filter " + input), input);
        }
    }

    @Test
    public void parse_unknownPrefix_namesPrefix() {
        String usageLine = "\nUsage: " + HomeworkFilterCommand.MESSAGE_USAGE;
        assertEquals(notPrefixWithQuoteHint("x/", "s/\"MATH x/value\"") + usageLine, assertThrows(
                ParseException.class, () -> parser.parseCommand("hw filter s/MATH x/value")).getMessage());
        assertEquals(notPrefix("score/") + usageLine, assertThrows(
                ParseException.class, () -> parser.parseCommand("hw filter score/17")).getMessage());
    }
}
