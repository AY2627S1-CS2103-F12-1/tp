package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
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

    public static final String MESSAGE_LOAD_CAUSE_FOLDER = "It is a folder, not a file.";
    public static final String MESSAGE_LOAD_CAUSE_UNREADABLE_FILE = "The file cannot be read (no permission).";
    public static final String MESSAGE_LOAD_CAUSE_UNREADABLE_FOLDER =
            "The data folder cannot be read (no permission).";

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
            throw new DataLoadingException(describeLoadFailure(exception, filePath));
        }
    }

    /**
     * Returns an exception with a plain cause if {@code exception} is because {@code filePath} is a folder or cannot
     * be read, or {@code exception} itself otherwise. The system's text for these names only the path or says
     * "Is a directory".
     */
    private static Exception describeLoadFailure(Exception exception, Path filePath) {
        if (Files.isDirectory(filePath)) {
            return new IOException(MESSAGE_LOAD_CAUSE_FOLDER, exception);
        }
        if (!(exception instanceof AccessDeniedException)) {
            return exception;
        }
        Path folder = filePath.toAbsolutePath().getParent();
        // Without read and execute permission on its folder, the file cannot even be found
        boolean canOpenFolder = folder == null || Files.isReadable(folder) && Files.isExecutable(folder);
        return new IOException(canOpenFolder ? MESSAGE_LOAD_CAUSE_UNREADABLE_FILE
                : MESSAGE_LOAD_CAUSE_UNREADABLE_FOLDER, exception);
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
        // Saves through different symbolic links to one file, or by its own path, must lock the same lock file
        Path savedFile = FileUtil.resolveSymbolicLinks(normalized).normalize();
        Path lockFile = savedFile.resolveSibling(savedFile.getFileName() + ".lock");
        try (FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock lock = acquireWriteLock(channel)) {
            byte[] currentVersion = readVersion(normalized);
            // A data file deleted since it was loaded or saved is written again from the data in memory
            if (knownVersions.containsKey(normalized) && currentVersion != null
                    && !Arrays.equals(knownVersions.get(normalized), currentVersion)) {
                throw new DataFileConflictException(knownVersions.get(normalized) == null
                        ? DataFileConflictException.MESSAGE_CREATED : DataFileConflictException.MESSAGE_CHANGED);
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
