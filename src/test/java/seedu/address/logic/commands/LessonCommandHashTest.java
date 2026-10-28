package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;

public class LessonCommandHashTest {
    @Test
    public void independentlyParsedEqualCommands_deduplicateInHashCollections() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        for (String input : List.of("lesson add 1 s/MATH d/MONDAY st/16:00 et/17:00",
                "lesson edit 1 d/TUESDAY", "lesson edit 1 s/MATH st/16:00 et/17:00")) {
            Command first = parser.parseCommand(input);
            Command second = parser.parseCommand(input);

            assertEquals(first, second);
            assertEquals(first.hashCode(), second.hashCode());
            assertEquals(1, new HashSet<>(List.of(first, second)).size());
        }
    }
}
