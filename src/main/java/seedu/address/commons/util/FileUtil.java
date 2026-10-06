package seedu.address.commons.util;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Writes and reads files
 */
public class FileUtil {

    private static final String CHARSET = "UTF-8";
    private static final String TEMP_FILE_SUFFIX = ".tmp";

    /**
     * Creates parent directories of file if it has a parent directory
     */
    private static void createParentDirsOfFile(Path file) throws IOException {
        Path parentDir = file.getParent();

        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }

    /**
     * Assumes file exists
     */
    public static String readFromFile(Path file) throws IOException {
        return new String(Files.readAllBytes(file), CHARSET);
    }

    /**
     * Writes given string to a file, creating the file and its missing parent directories if needed.
     * The content is first written to a new temporary file with a unique name next to {@code file}, which then
     * replaces {@code file}, so {@code file} is never left partially written: if writing fails, it keeps its
     * previous content. Other files in the folder are never changed.
     * Where the file system supports POSIX file permissions, an existing {@code file} keeps its permissions, and a
     * new {@code file} can be read and written only by its owner.
     *
     * @throws AccessDeniedException If {@code file} exists but is read-only, or its folder cannot be written to.
     * @throws IOException If the content cannot be written. An error about the temporary file names {@code file}.
     */
    public static void writeToFile(Path file, String content) throws IOException {
        // A move can replace a read-only file, so check it first to keep the file read-only for the user
        if (Files.exists(file) && !Files.isWritable(file)) {
            throw new AccessDeniedException(file.toString());
        }

        createParentDirsOfFile(file);
        Path folder = file.toAbsolutePath().getParent();
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile(folder, file.getFileName().toString(), TEMP_FILE_SUFFIX);
            Files.write(tempFile, content.getBytes(CHARSET));
            copyPosixPermissions(file, tempFile);
            replaceFile(tempFile, file);
        } catch (IOException e) {
            deleteAfterFailure(tempFile, e);
            throw toErrorAboutFile(e, file);
        } catch (RuntimeException e) {
            deleteAfterFailure(tempFile, e);
            throw e;
        }
    }

    /**
     * Gives {@code target} the POSIX file permissions of {@code source} if {@code source} exists and the file system
     * supports POSIX file permissions.
     */
    private static void copyPosixPermissions(Path source, Path target) throws IOException {
        if (Files.exists(source) && source.getFileSystem().supportedFileAttributeViews().contains("posix")) {
            Files.setPosixFilePermissions(target, Files.getPosixFilePermissions(source));
        }
    }

    /**
     * Moves {@code source} to {@code target}, replacing {@code target}, atomically if the file system supports it.
     */
    private static void replaceFile(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Deletes {@code tempFile}, if it was created, after {@code failure}, adding any error from the deletion to
     * {@code failure}.
     */
    private static void deleteAfterFailure(Path tempFile, Exception failure) {
        if (tempFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException deleteException) {
            failure.addSuppressed(deleteException);
        }
    }

    /**
     * Returns {@code exception}, or, if it is about the temporary file used to write {@code file}, an exception of the
     * same kind about {@code file} caused by {@code exception}, so that error messages name the user's file.
     */
    private static IOException toErrorAboutFile(IOException exception, Path file) {
        // While writing file, an error about any other path is about the temporary file
        if (!(exception instanceof FileSystemException fileException)
                || fileException.getFile() == null
                || file.toString().equals(fileException.getFile())) {
            return exception;
        }
        FileSystemException errorAboutFile = exception instanceof AccessDeniedException
                ? new AccessDeniedException(file.toString(), null, fileException.getReason())
                : new FileSystemException(file.toString(), null, fileException.getReason());
        errorAboutFile.initCause(exception);
        return errorAboutFile;
    }

}
