package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.LessonEditCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class LessonEditCommandParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_optionalFieldsAnyOrderAndQuotedValues_success() throws Exception {
        assertEquals(parser.parseCommand("lesson edit 1 s/PHYSICS d/TUESDAY st/09:00 et/10:00"),
                parser.parseCommand("lesson edit 1 et/\"10:00\" st/09:00 d/tuesday s/physics"));
        for (String field : new String[]{"s/MATH", "d/MONDAY", "st/09:00", "et/10:00"}) {
            assertInstanceOf(LessonEditCommand.class, parser.parseCommand("lesson edit 1 " + field));
        }
    }

    @Test
    public void parse_invalidInput_rejected() {
        String[] invalid = {"", "1", "d/MONDAY", "1 2 d/MONDAY", "0 d/MONDAY", "01 d/MONDAY",
            "-1 d/MONDAY", "2147483648 d/MONDAY", "1 x/a", "1 s/", "1 d/", "1 st/", "1 et/",
            "1 d/MON", "1 s/BIOLOGY", "1 st/9:00", "1 et/24:00", "1 st/09:00:00",
            "1 d/MONDAY d/TUESDAY", "1 st/09:00 extra", "1 et/10:00 et/11:00"};
        for (String input : invalid) {
            assertThrows(ParseException.class, () -> parser.parseCommand("lesson edit " + input), input);
        }
    }
}
