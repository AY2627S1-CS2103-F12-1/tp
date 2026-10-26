package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * A class to access AddressBook data stored as a JSON file on the hard disk.
 */
public class JsonAddressBookStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonAddressBookStorage.class);

    private Path filePath;
    private final Map<Path, byte[]> knownVersions = new HashMap<>();

    public JsonAddressBookStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getAddressBookFilePath() {
        return filePath;
    }

    /**
     * Returns AddressBook data as a {@link ReadOnlyAddressBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        return readAddressBook(filePath);
    }

    /**
     * Similar to {@link #readAddressBook()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        try {
            byte[] before = readVersion(filePath);
            Optional<JsonSerializableAddressBook> jsonAddressBook = JsonUtil.readJsonFile(
                    filePath, JsonSerializableAddressBook.class);
            Optional<ReadOnlyAddressBook> result = jsonAddressBook.isPresent()
                    ? Optional.of(jsonAddressBook.get().toModelType()) : Optional.empty();
            if (!Arrays.equals(before, readVersion(filePath))) {
                throw new IOException("The data file changed while loading. Try opening TutorFlow again.");
            }
            knownVersions.put(filePath.toAbsolutePath().normalize(), before);
            return result;
        } catch (IOException | IllegalValueException exception) {
            logger.info("Could not load " + filePath + ": " + exception.getMessage());
            throw new DataLoadingException(exception);
        }
    }

    /**
     * Saves the given {@link ReadOnlyAddressBook} to the storage.
     * @param addressBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveAddressBook(addressBook, filePath);
    }

    /**
     * Similar to {@link #saveAddressBook(ReadOnlyAddressBook)}.
     *
     * @param addressBook Data to save. Cannot be null.
     * @param filePath location of the data. Cannot be null.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);

        Path normalized = filePath.toAbsolutePath().normalize();
        Files.createDirectories(normalized.getParent());
        Path lockFile = normalized.resolveSibling(normalized.getFileName() + ".lock");
        try (FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock lock = acquireWriteLock(channel)) {
            if (knownVersions.containsKey(normalized)
                    && !Arrays.equals(knownVersions.get(normalized), readVersion(normalized))) {
                throw new DataFileConflictException(DataFileConflictException.MESSAGE_CHANGED);
            }
            String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(addressBook));
            FileUtil.writeToFile(normalized, json);
            knownVersions.put(normalized, json.getBytes(StandardCharsets.UTF_8));
        }
    }

    /** Returns saved bytes, with null distinguishing a missing file from an empty file. */
    private static byte[] readVersion(Path file) throws IOException {
        return Files.notExists(file) ? null : Files.readAllBytes(file);
    }

    /** Serializes cooperating writers across processes before comparing and replacing the file. */
    private static FileLock acquireWriteLock(FileChannel channel) throws IOException {
        try {
            FileLock lock = channel.tryLock();
            if (lock != null) {
                return lock;
            }
        } catch (OverlappingFileLockException exception) {
            throw new DataFileConflictException(DataFileConflictException.MESSAGE_BUSY);
        }
        throw new DataFileConflictException(DataFileConflictException.MESSAGE_BUSY);
    }

}
