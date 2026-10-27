package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.HomeworkAddCommand;
import seedu.address.logic.commands.HomeworkDeleteCommand;
import seedu.address.logic.commands.HomeworkEditCommand;
import seedu.address.logic.commands.HomeworkFilterCommand;
import seedu.address.logic.commands.HomeworkListCommand;
import seedu.address.logic.commands.HomeworkMarksCommand;
import seedu.address.logic.commands.HomeworkStatusCommand;
import seedu.address.logic.commands.HomeworkUpcomingCommand;
import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.logic.commands.LessonDeleteCommand;
import seedu.address.logic.commands.LessonEditCommand;
import seedu.address.logic.commands.LessonListCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.SearchCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.parser.AddressBookParser;

public class HelpContentTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-05T00:00:00Z"), ZoneOffset.UTC);

    @Test
    public void getSections_sectionTitles_inDisplayOrder() {
        List<String> titles = HelpContent.getSections().stream().map(HelpContent.Section::title).toList();
        assertEquals(List.of(HelpContent.SECTION_STUDENTS, HelpContent.SECTION_HOMEWORK,
                HelpContent.SECTION_LESSONS, HelpContent.SECTION_GENERAL), titles);
    }

    @Test
    public void getSections_entries_inUserGuideOrder() {
        Map<String, List<String>> commandWordsBySection = HelpContent.getSections().stream()
                .collect(Collectors.toMap(HelpContent.Section::title, section -> section.entries().stream()
                        .map(HelpContent.Entry::commandWord).toList()));

        assertEquals(List.of("add", "edit", "list", "search", "filter", "sort", "delete"),
                commandWordsBySection.get(HelpContent.SECTION_STUDENTS));
        assertEquals(List.of("homework add", "homework list", "homework upcoming", "homework edit", "homework marks",
                "homework delete", "homework filter", "homework status"),
                commandWordsBySection.get(HelpContent.SECTION_HOMEWORK));
        assertEquals(List.of("lesson add", "lesson list", "lesson delete", "lesson edit"),
                commandWordsBySection.get(HelpContent.SECTION_LESSONS));
        assertEquals(List.of("help", "clear", "exit"), commandWordsBySection.get(HelpContent.SECTION_GENERAL));
    }

    @Test
    public void getSections_listEntry_showsAlias() {
        HelpContent.Entry listEntry = getEntries().stream()
                .filter(entry -> entry.commandWord().equals(ListCommand.COMMAND_WORD)).findFirst().orElseThrow();
        assertEquals("list|ls", listEntry.format());
    }

    @Test
    public void getSections_listedCommands_useUsageConstants() {
        Map<String, String> expectedFormats = Map.ofEntries(
                Map.entry(AddCommand.COMMAND_WORD, AddCommand.MESSAGE_USAGE),
                Map.entry(DeleteCommand.COMMAND_WORD, DeleteCommand.MESSAGE_USAGE),
                Map.entry(EditCommand.COMMAND_WORD, EditCommand.MESSAGE_USAGE),
                Map.entry(ListCommand.COMMAND_WORD, ListCommand.MESSAGE_USAGE),
                Map.entry(SearchCommand.COMMAND_WORD, SearchCommand.MESSAGE_USAGE),
                Map.entry(SortCommand.COMMAND_WORD, SortCommand.MESSAGE_USAGE),
                Map.entry(FilterCommand.COMMAND_WORD, FilterCommand.MESSAGE_USAGE),
                Map.entry("homework add", HomeworkAddCommand.MESSAGE_USAGE),
                Map.entry("homework list", HomeworkListCommand.MESSAGE_USAGE),
                Map.entry("homework upcoming", HomeworkUpcomingCommand.MESSAGE_USAGE),
                Map.entry("homework edit", HomeworkEditCommand.MESSAGE_USAGE),
                Map.entry("homework filter", HomeworkFilterCommand.MESSAGE_USAGE),
                Map.entry("homework status", HomeworkStatusCommand.MESSAGE_USAGE),
                Map.entry("homework marks", HomeworkMarksCommand.MESSAGE_USAGE),
                Map.entry("homework delete", HomeworkDeleteCommand.MESSAGE_USAGE),
                Map.entry("lesson add", LessonAddCommand.MESSAGE_USAGE),
                Map.entry("lesson list", LessonListCommand.MESSAGE_USAGE),
                Map.entry("lesson delete", LessonDeleteCommand.MESSAGE_USAGE),
                Map.entry("lesson edit", LessonEditCommand.MESSAGE_USAGE),
                Map.entry(HelpCommand.COMMAND_WORD, HelpCommand.COMMAND_WORD),
                Map.entry(ClearCommand.COMMAND_WORD, ClearCommand.COMMAND_WORD),
                Map.entry(ExitCommand.COMMAND_WORD, ExitCommand.COMMAND_WORD));

        List<HelpContent.Entry> entries = getEntries();
        assertEquals(expectedFormats.size(), entries.size());
        for (HelpContent.Entry entry : entries) {
            assertEquals(expectedFormats.get(entry.commandWord()), entry.format(), entry.commandWord());
        }
    }

    @Test
    public void getSections_examples_parseIntoListedCommand() throws Exception {
        Map<String, Class<? extends Command>> expectedCommandTypes = Map.ofEntries(
                Map.entry(AddCommand.COMMAND_WORD, AddCommand.class),
                Map.entry(DeleteCommand.COMMAND_WORD, DeleteCommand.class),
                Map.entry(EditCommand.COMMAND_WORD, EditCommand.class),
                Map.entry(ListCommand.COMMAND_WORD, ListCommand.class),
                Map.entry(SearchCommand.COMMAND_WORD, SearchCommand.class),
                Map.entry(SortCommand.COMMAND_WORD, SortCommand.class),
                Map.entry(FilterCommand.COMMAND_WORD, FilterCommand.class),
                Map.entry("homework add", HomeworkAddCommand.class),
                Map.entry("homework list", HomeworkListCommand.class),
                Map.entry("homework upcoming", HomeworkUpcomingCommand.class),
                Map.entry("homework edit", HomeworkEditCommand.class),
                Map.entry("homework filter", HomeworkFilterCommand.class),
                Map.entry("homework status", HomeworkStatusCommand.class),
                Map.entry("homework marks", HomeworkMarksCommand.class),
                Map.entry("homework delete", HomeworkDeleteCommand.class),
                Map.entry("lesson add", LessonAddCommand.class),
                Map.entry("lesson list", LessonListCommand.class),
                Map.entry("lesson delete", LessonDeleteCommand.class),
                Map.entry("lesson edit", LessonEditCommand.class),
                Map.entry(HelpCommand.COMMAND_WORD, HelpCommand.class),
                Map.entry(ClearCommand.COMMAND_WORD, ClearCommand.class),
                Map.entry(ExitCommand.COMMAND_WORD, ExitCommand.class));
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
