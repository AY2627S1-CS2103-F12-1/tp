package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class LessonOwnerIntegrationTest {
    @TempDir
    Path temporaryFolder;

    @Test
    public void sameNameOwners_reportsIdentifyPhonesAndGlobalIndexTargetsCorrectOwner() throws Exception {
        ModelManager model = new ModelManager();
        JsonAddressBookStorage data = new JsonAddressBookStorage(temporaryFolder.resolve("tutorflow.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(data,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        logic.execute("add n/Alice Tan l/S3 s/MATH p/91234567 gn/Mary Tan gp/98765432");
        logic.execute("add n/Alice Tan l/S3 s/MATH p/91234568 gn/Mary Tan gp/98765432");

        assertTrue(logic.execute("lesson add 1 s/MATH d/MONDAY st/16:00 et/17:00").getFeedbackToUser()
                .contains("Alice Tan (91234567)"));
        assertTrue(logic.execute("lesson add 2 s/MATH d/MONDAY st/17:00 et/18:00").getFeedbackToUser()
                .contains("Alice Tan (91234568)"));
        String schedule = logic.execute("lesson list").getFeedbackToUser();
        assertTrue(schedule.contains("1. Alice Tan (91234567); MATH; MON; 16:00-17:00"));
        assertTrue(schedule.contains("2. Alice Tan (91234568); MATH; MON; 17:00-18:00"));

        logic.execute("search 91234568");
        CommandException addClash = assertThrows(CommandException.class, () ->
                logic.execute("lesson add 1 s/MATH d/MONDAY st/16:30 et/17:00"));
        assertTrue(addClash.getMessage().contains("Alice Tan (91234567)"));
        CommandException editClash = assertThrows(CommandException.class, () ->
                logic.execute("lesson edit 2 st/16:59"));
        assertTrue(editClash.getMessage().contains("Alice Tan (91234567)"));

        assertTrue(logic.execute("lesson edit 2 d/TUESDAY").getFeedbackToUser()
                .contains("Alice Tan (91234568)"));
        assertTrue(logic.execute("lesson delete 1").getFeedbackToUser().contains("Alice Tan (91234567)"));
        assertTrue(model.getAddressBook().getStudentList().getFirst().getLessons().isEmpty());
        assertEquals(1, model.getAddressBook().getStudentList().get(1).getLessons().size());
        assertEquals(model.getAddressBook(), new AddressBook(data.readAddressBook().orElseThrow()));
    }
}
