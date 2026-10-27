package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class StudentLessonListIntegrationTest {
    @TempDir
    Path temporaryFolder;

    @Test
    public void studentSchedule_keepsGlobalIndicesAndFilteredView_withoutSaving() throws Exception {
        ModelManager model = new ModelManager();
        RecordingStorage storage = new RecordingStorage(temporaryFolder);
        LogicManager logic = new LogicManager(model, storage);
        logic.execute("add n/Alice Tan l/S3 s/MATH p/91234567 gn/Mary Tan gp/98765432");
        logic.execute("add n/Alice Tan l/S3 s/MATH p/91234568 gn/Mary Tan gp/98765432");
        logic.execute("add n/No Lessons l/S3 s/MATH p/91234569 gn/Mary Tan gp/98765432");
        logic.execute("lesson add 1 s/MATH d/MONDAY st/09:00 et/10:00");
        logic.execute("lesson add 2 s/MATH d/TUESDAY st/09:00 et/10:00");
        logic.execute("lesson add 1 s/MATH d/WEDNESDAY st/09:00 et/10:00");
        logic.execute("search 91234567");
        var view = List.copyOf(model.getStudentList());
        byte[] before = Files.readAllBytes(storage.getAddressBookFilePath());
        int savesBefore = storage.saveCount;

        String feedback = logic.execute("lesson list 1").getFeedbackToUser();

        assertTrue(feedback.startsWith("Listed 2 regular lessons for Alice Tan (91234567)."));
        assertTrue(feedback.contains("\n1. Alice Tan (91234567); MATH; MON; 09:00-10:00"));
        assertTrue(feedback.contains("\n3. Alice Tan (91234567); MATH; WED; 09:00-10:00"));
        assertFalse(feedback.contains("91234568"));
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("ar-EG"));
            assertEquals(feedback, logic.execute("lesson list 1").getFeedbackToUser());
        } finally {
            Locale.setDefault(previous);
        }
        assertEquals(view, model.getStudentList());
        assertEquals(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX,
                assertThrows(CommandException.class, () -> logic.execute("lesson list 2")).getMessage());
        logic.execute("list");
        assertEquals("No regular lessons found for No Lessons (91234569).",
                logic.execute("lesson list 3").getFeedbackToUser());
        assertTrue(logic.execute("lesson list 2").getFeedbackToUser()
                .startsWith("Listed 1 regular lesson for Alice Tan (91234568).\n2."));
        assertEquals(savesBefore, storage.saveCount);
        assertArrayEquals(before, Files.readAllBytes(storage.getAddressBookFilePath()));

        logic.execute("lesson delete 3");
        assertEquals(1, model.getAddressBook().getStudentList().getFirst().getLessons().size());
        assertEquals(1, model.getAddressBook().getStudentList().get(1).getLessons().size());
    }

    private static class RecordingStorage extends StorageManager {
        private int saveCount;

        RecordingStorage(Path folder) {
            super(new JsonAddressBookStorage(folder.resolve("tutorflow.json")),
                    new JsonUserPrefsStorage(folder.resolve("preferences.json")));
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saveCount++;
            super.saveAddressBook(addressBook);
        }
    }
}
