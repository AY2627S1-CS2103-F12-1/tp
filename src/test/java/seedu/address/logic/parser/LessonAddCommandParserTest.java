package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.parser.CommandParserTestUtil.notPrefixWithQuoteHint;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class LessonAddCommandParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_reorderedAndCaseInsensitiveValues_sameCommand() throws Exception {
        assertEquals(parser.parseCommand("lesson add 1 s/MATH d/MONDAY st/16:00 et/18:00"),
                parser.parseCommand(" lesson add 1 et/18:00 d/monday s/math st/16:00 "));
    }

    @Test
    public void parse_quotedValues_sameCommand() throws Exception {
        assertEquals(parser.parseCommand("lesson add 1 s/MATH d/MONDAY st/16:00 et/18:00"),
                parser.parseCommand("lesson add 1 s/\"MATH\" d/\"MONDAY\" st/\"16:00\" et/ \"18:00\" "));
    }

    @Test
    public void parse_invalidStructure_rejects() {
        for (String args : new String[]{"add", "add s/MATH d/MONDAY st/16:00 et/18:00",
            "add 1 extra s/MATH d/MONDAY st/16:00 et/18:00",
            "add 1 s/MATH d/MONDAY st/16:00 et/18:00 extra"}) {
            assertEquals(LessonAddCommandParser.MESSAGE_FORMAT,
                    assertThrows(ParseException.class, () ->
                        parser.parseCommand("lesson " + args)).getMessage());
        }
        assertEquals(
                "Prefix s/ must be specified exactly once.", assertThrows(ParseException.class, () ->
                        parser.parseCommand("lesson add 1 s/MATH s/PHYSICS d/MONDAY st/16:00 et/18:00")).getMessage());
    }

    @Test
    public void parse_missingPrefixes_namesEveryMissingField() {
        assertEquals(String.format(LessonAddCommandParser.MESSAGE_MISSING_PREFIXES, "d/, et/"),
                assertThrows(ParseException.class, () ->
                        parser.parseCommand("lesson add 1 s/MATH st/16:00")).getMessage());
    }

    @Test
    public void parse_largePositiveIndex_reportsSupportedRange() throws Exception {
        parser.parseCommand("lesson add 2147483647 s/MATH d/MONDAY st/16:00 et/18:00");
        for (String index : new String[]{"2147483648", "999999999999999999999"}) {
            assertEquals(LessonAddCommandParser.MESSAGE_INDEX_TOO_LARGE,
                    assertThrows(ParseException.class, () -> parser.parseCommand(
                            "lesson add " + index + " s/MATH d/MONDAY st/16:00 et/18:00")).getMessage());
        }
    }

    @Test
    public void parse_unknownPrefix_namesPrefix() {
        // the end time before x/ is only digits and a colon, so no example is given
        assertEquals(notPrefixWithQuoteHint("x/") + "\nUsage: " + LessonAddCommand.MESSAGE_USAGE,
                assertThrows(ParseException.class, () ->
                        parser.parseCommand("lesson add 1 s/MATH d/MONDAY st/16:00 et/18:00 x/test")).getMessage());
    }

    @Test
    public void parse_invalidIndexAndValues_rejects() {
        for (String index : new String[]{"0", "01", "-1", "+1", "1.0", "one", "999999999999999999999"}) {
            assertThrows(ParseException.class, () ->
                        parser.parseCommand("lesson add " + index + " s/MATH d/MONDAY st/16:00 et/18:00"));
        }
        for (String day : new String[]{"MON", "", "FUNDAY"}) {
            assertThrows(ParseException.class, () ->
                        parser.parseCommand("lesson add 1 s/MATH d/" + day + " st/16:00 et/18:00"));
        }
        for (String time : new String[]{"9:00", "24:00", "18:60", "4pm", "16:00:00", ""}) {
            assertThrows(ParseException.class, () ->
                        parser.parseCommand("lesson add 1 s/MATH d/MONDAY st/" + time + " et/18:00"));
            assertThrows(ParseException.class, () ->
                        parser.parseCommand("lesson add 1 s/MATH d/MONDAY st/16:00 et/" + time));
        }
        for (String end : new String[]{"16:00", "15:00", "01:00"}) {
            assertThrows(ParseException.class, () ->
                        parser.parseCommand("lesson add 1 s/MATH d/MONDAY st/16:00 et/" + end));
        }
        assertThrows(ParseException.class, () ->
                        parser.parseCommand("lesson add 1 s/BIOLOGY d/MONDAY st/16:00 et/18:00"));
    }
}
