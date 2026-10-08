package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;

public class LessonAddCommandParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_reorderedAndCaseInsensitiveValues_sameCommand() throws Exception {
        assertEquals(parser.parseCommand("lesson add 1 s/MATH d/MONDAY st/16:00 et/18:00"),
                parser.parseCommand(" lesson add 1 et/18:00 d/monday s/math st/16:00 "));
    }

    @Test
    public void parse_invalidStructure_rejects() {
        for (String args : new String[]{"", "add", "delete 1", "add s/MATH d/MONDAY st/16:00 et/18:00",
            "add 1 extra s/MATH d/MONDAY st/16:00 et/18:00", "add 1 s/MATH d/MONDAY st/16:00",
            "add 1 s/MATH d/MONDAY st/16:00 et/18:00 x/test",
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
