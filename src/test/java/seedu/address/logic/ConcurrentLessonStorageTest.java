package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class ConcurrentLessonStorageTest {
    private static final String ADD_ALPHA =
            "add n/Alpha l/S3 s/MATH p/91234567 gn/Guardian gp/98765432";
    private static final String ADD_BETA =
            "add n/Beta l/S1 s/MATH p/91234568 gn/Guardian gp/98765432";

    @TempDir
    Path temporaryFolder;

    @Test
    public void separateProcessHoldsLock_rejectsSaveUntilWriterFinishes() throws Exception {
        seedLessons();
        Session session = openSession();
        byte[] saved = Files.readAllBytes(dataFile());
        Path source = temporaryFolder.resolve("LockHolder.java");
        Files.writeString(source, """
                import java.nio.channels.*;
                import java.nio.file.*;
                public class LockHolder {
                    public static void main(String[] args) throws Exception {
                        try (FileChannel c = FileChannel.open(Path.of(args[0]),
                                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                                FileLock lock = c.lock()) {
                            System.out.println("locked");
                            System.out.flush();
                            System.in.read();
                        }
                    }
                }
                """);
        Process writer = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                source.toString(), dataFile().resolveSibling("tutorflow.json.lock").toString())
                .redirectErrorStream(true).start();
        try {
            assertEquals("locked", writer.inputReader().readLine());
            CommandException error = assertThrows(CommandException.class, () ->
                    session.logic().execute("lesson delete 1"));
            assertTrue(error.getMessage().contains("Try again"));
            assertArrayEquals(saved, Files.readAllBytes(dataFile()));
        } finally {
            writer.getOutputStream().close();
            if (!writer.waitFor(5, TimeUnit.SECONDS)) {
                writer.destroyForcibly();
            }
        }

        session.logic().execute("lesson delete 1");
        assertTrue(session.model().getAddressBook().getStudentList().getFirst().getLessons().isEmpty());
    }

    @Test
    public void staleWindow_saveAfterDeletion_doesNotResurrectDeletedLesson() throws Exception {
        seedLessons();
        Session first = openSession();
        Session second = openSession();
        AddressBook staleBefore = new AddressBook(second.model().getAddressBook());
        first.logic().execute("lesson delete 1");
        byte[] savedDeletion = Files.readAllBytes(dataFile());

        CommandException error = assertThrows(CommandException.class, () ->
                second.logic().execute("lesson add 2 s/MATH d/FRIDAY st/10:00 et/11:00"));

        assertTrue(error.getMessage().contains("Restart TutorFlow"));
        assertEquals(staleBefore, second.model().getAddressBook());
        assertArrayEquals(savedDeletion, Files.readAllBytes(dataFile()));
        second.logic().execute("lesson list");
        assertArrayEquals(savedDeletion, Files.readAllBytes(dataFile()));
        Session restarted = openSession();
        assertEquals(first.model().getAddressBook(), restarted.model().getAddressBook());
        restarted.logic().execute("lesson add 2 s/MATH d/FRIDAY st/10:00 et/11:00");
        assertTrue(restarted.model().getAddressBook().getStudentList().getFirst().getLessons().isEmpty());
    }

    @Test
    public void missingFile_twoSessions_onlyFirstSaveCreatesRoster() throws Exception {
        Session first = openSession();
        Session second = openSession();
        first.logic().execute(ADD_ALPHA);
        byte[] saved = Files.readAllBytes(dataFile());

        assertThrows(CommandException.class, () -> second.logic().execute(ADD_BETA));

        assertTrue(second.model().getStudentList().isEmpty());
        assertArrayEquals(saved, Files.readAllBytes(dataFile()));
    }

    @Test
    public void anotherWriterHoldsLock_rejectsWithoutChangesAndAllowsRetry() throws Exception {
        seedLessons();
        Session session = openSession();
        AddressBook before = new AddressBook(session.model().getAddressBook());
        byte[] saved = Files.readAllBytes(dataFile());
        Path lockFile = dataFile().resolveSibling("tutorflow.json.lock");
        try (FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock lock = channel.lock()) {
            assertTrue(lock.isValid());
            CommandException error = assertThrows(CommandException.class, () ->
                    session.logic().execute("lesson delete 1"));
            assertTrue(error.getMessage().contains("Try again"));
            assertEquals(before, session.model().getAddressBook());
            assertArrayEquals(saved, Files.readAllBytes(dataFile()));
        }

        session.logic().execute("lesson delete 1");
        assertTrue(session.model().getAddressBook().getStudentList().getFirst().getLessons().isEmpty());
    }

    private void seedLessons() throws Exception {
        Session session = openSession();
        session.logic().execute(ADD_ALPHA);
        session.logic().execute(ADD_BETA);
        session.logic().execute("lesson add 1 s/MATH d/MONDAY st/08:00 et/10:00");
        session.logic().execute("lesson add 2 s/MATH d/TUESDAY st/10:00 et/11:00");
    }

    private Session openSession() throws Exception {
        JsonAddressBookStorage dataStorage = new JsonAddressBookStorage(dataFile());
        ModelManager model = new ModelManager(dataStorage.readAddressBook().orElseGet(AddressBook::new),
                new UserPrefs());
        LogicManager logic = new LogicManager(model, new StorageManager(dataStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        return new Session(model, logic);
    }

    private Path dataFile() {
        return temporaryFolder.resolve("tutorflow.json");
    }

    private record Session(ModelManager model, LogicManager logic) {
    }
}
