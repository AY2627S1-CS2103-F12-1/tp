package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.HomeworkAddCommand;
import seedu.address.logic.commands.HomeworkDeleteCommand;
import seedu.address.logic.commands.HomeworkListCommand;
import seedu.address.logic.parser.AddressBookParser;

public class HelpContentTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-05T00:00:00Z"), ZoneOffset.UTC);

    @Test
    public void getSections_sectionTitles_inDisplayOrder() {
        List<String> titles = HelpContent.getSections().stream().map(HelpContent.Section::title).toList();
        assertEquals(List.of(HelpContent.SECTION_STUDENTS, HelpContent.SECTION_HOMEWORK,
                HelpContent.SECTION_GENERAL), titles);
    }

    @Test
    public void getSections_listedCommands_useUsageConstants() {
        Map<String, String> expectedFormats = Map.of(
                AddCommand.COMMAND_WORD, AddCommand.MESSAGE_USAGE,
                DeleteCommand.COMMAND_WORD, DeleteCommand.MESSAGE_USAGE,
                "homework add", HomeworkAddCommand.MESSAGE_USAGE,
                "homework list", HomeworkListCommand.MESSAGE_USAGE,
                "homework delete", HomeworkDeleteCommand.MESSAGE_USAGE,
                HelpCommand.COMMAND_WORD, HelpCommand.COMMAND_WORD,
                ClearCommand.COMMAND_WORD, ClearCommand.COMMAND_WORD,
                ExitCommand.COMMAND_WORD, ExitCommand.COMMAND_WORD);

        List<HelpContent.Entry> entries = getEntries();
        assertEquals(expectedFormats.size(), entries.size());
        for (HelpContent.Entry entry : entries) {
            assertEquals(expectedFormats.get(entry.commandWord()), entry.format(), entry.commandWord());
        }
    }

    @Test
    public void getSections_examples_parseIntoListedCommand() throws Exception {
        Map<String, Class<? extends Command>> expectedCommandTypes = Map.of(
                AddCommand.COMMAND_WORD, AddCommand.class,
                DeleteCommand.COMMAND_WORD, DeleteCommand.class,
                "homework add", HomeworkAddCommand.class,
                "homework list", HomeworkListCommand.class,
                "homework delete", HomeworkDeleteCommand.class,
                HelpCommand.COMMAND_WORD, HelpCommand.class,
                ClearCommand.COMMAND_WORD, ClearCommand.class,
                ExitCommand.COMMAND_WORD, ExitCommand.class);
        AddressBookParser parser = new AddressBookParser(FIXED_CLOCK);

        for (HelpContent.Entry entry : getEntries()) {
            assertInstanceOf(expectedCommandTypes.get(entry.commandWord()), parser.parseCommand(entry.example()),
                    entry.example());
        }
    }

    private static List<HelpContent.Entry> getEntries() {
        return HelpContent.getSections().stream().flatMap(section -> section.entries().stream()).toList();
    }
}
