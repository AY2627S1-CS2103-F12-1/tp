package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.student.Student;
import seedu.address.testutil.StudentBuilder;

/** Tests student search against all four searchable fields and displayed indexes. */
public class SearchCommandTest {
    private final Student first = new StudentBuilder().withName("John Tan").withPhone("91111111")
            .withGuardianName("Mary Tan").withGuardianPhone("98888888").build();
    private final Student second = new StudentBuilder().withName("Benson Lim").withPhone("92222222")
            .withGuardianName("Alex Tan").withGuardianPhone("97777777").build();
    private final Student third = new StudentBuilder().withName("Jane Ong").withPhone("93333333")
            .withGuardianName("Helen Ong").withGuardianPhone("96666666").build();

    private ModelManager model() {
        ModelManager model = new ModelManager();
        List.of(first, second, third).forEach(model::addStudent);
        return model;
    }

    @Test
    public void execute_studentName_partialCaseInsensitiveMatch() throws Exception {
        ModelManager model = model();
        AddressBook before = new AddressBook(model.getAddressBook());

        CommandResult result = new SearchCommand("  jOhN  ").execute(model);

        assertEquals("Found 1 student for \"jOhN\".\nType list to show all students.",
                result.getFeedbackToUser());
        assertEquals(List.of(first), model.getStudentList());
        assertTrue(result.shouldShowStudentList());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_dottedCapitalI_matchesLowercaseName() throws Exception {
        Student student = new StudentBuilder().withName("İpek Tan").build();
        ModelManager model = new ModelManager();
        model.addStudent(student);

        new SearchCommand("ipek").execute(model);

        assertEquals(List.of(student), model.getStudentList());
    }

    @Test
    public void execute_repeatedWhitespace_matchesNormalizedName() throws Exception {
        ModelManager model = model();

        CommandResult result = new SearchCommand("John\u00a0\u00a0Tan").execute(model);

        assertEquals(List.of(first), model.getStudentList());
        assertEquals("Found 1 student for \"John Tan\".\nType list to show all students.",
                result.getFeedbackToUser());
    }

    @Test
    public void execute_guardianName_multipleMatchesKeepRosterOrder() throws Exception {
        ModelManager model = model();

        CommandResult result = new SearchCommand("tAn").execute(model);

        assertEquals("Found 2 students for \"tAn\".\nType list to show all students.",
                result.getFeedbackToUser());
        assertEquals(List.of(first, second), model.getStudentList());
    }

    @Test
    public void execute_studentPhone_partialMatch() throws Exception {
        ModelManager model = model();

        new SearchCommand("3333").execute(model);

        assertEquals(List.of(third), model.getStudentList());
    }

    @Test
    public void execute_guardianPhone_partialMatch() throws Exception {
        ModelManager model = model();

        new SearchCommand("7777").execute(model);

        assertEquals(List.of(second), model.getStudentList());
    }

    @Test
    public void execute_noMatches_showsEmptyMessageWithoutChangingRoster() throws Exception {
        ModelManager model = model();
        AddressBook before = new AddressBook(model.getAddressBook());

        CommandResult result = new SearchCommand("Nobody").execute(model);

        assertEquals("No students found for \"Nobody\".\nType list to show all students.",
                result.getFeedbackToUser());
        assertTrue(model.getStudentList().isEmpty());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_secondSearch_usesFullRosterNotPreviousResults() throws Exception {
        ModelManager model = model();
        new SearchCommand("John").execute(model);

        new SearchCommand("Benson").execute(model);

        assertEquals(List.of(second), model.getStudentList());
    }

    @Test
    public void execute_listAfterSearch_restoresFullRoster() throws Exception {
        ModelManager model = model();
        new SearchCommand("John").execute(model);

        CommandResult result = new ListCommand().execute(model);

        assertEquals("Listed 3 students.", result.getFeedbackToUser());
        assertEquals(List.of(first, second, third), model.getStudentList());
    }

    @Test
    public void execute_deleteAfterSearch_usesDisplayedIndex() throws Exception {
        ModelManager model = model();
        new SearchCommand("Benson").execute(model);

        new DeleteCommand(Index.fromOneBased(1)).execute(model);

        assertEquals(List.of(first, third), model.getAddressBook().getStudentList());
        assertTrue(model.getStudentList().isEmpty());
    }

    @Test
    public void execute_addAfterSearch_restoresFullRoster() throws Exception {
        ModelManager model = model();
        new SearchCommand("Nobody").execute(model);
        Student added = new StudentBuilder().withName("New Student").withPhone("94444444").build();

        new AddCommand(added).execute(model);

        assertEquals(List.of(first, second, third, added), model.getStudentList());
    }

    @Test
    public void execute_unexpectedModelError_reportsInternalError() {
        ModelManager model = new ModelManager() {
            @Override
            public void updateFilteredStudentList(Predicate<Student> predicate) {
                throw new IllegalStateException("dummy internal error");
            }
        };

        CommandException exception = assertThrows(CommandException.class, () ->
                new SearchCommand("John").execute(model));

        assertEquals(SearchCommand.MESSAGE_INTERNAL_ERROR, exception.getMessage());
    }

    @Test
    public void constructor_blankKeyword_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SearchCommand("  "));
        assertThrows(IllegalArgumentException.class, () -> new SearchCommand("\u00a0"));
    }

    @Test
    public void equals_sameKeyword_returnsTrue() {
        SearchCommand command = new SearchCommand("John");
        assertTrue(command.equals(new SearchCommand(" John ")));
        assertFalse(command.equals(new SearchCommand("Jane")));
        assertFalse(command.equals(null));
    }
}
