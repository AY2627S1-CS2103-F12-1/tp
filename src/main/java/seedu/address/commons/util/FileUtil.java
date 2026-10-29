package seedu.address.commons.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Writes and reads files
 */
public class FileUtil {

    /** Message for a file that is not UTF-8, with the line and column of its first invalid byte. */
    public static final String MESSAGE_INVALID_UTF8 =
            "The file is not valid UTF-8 text near line %d, column %d. Save the file as UTF-8.";

    private static final String CHARSET = "UTF-8";
    private static final String TEMP_FILE_SUFFIX = ".tmp";
    private static final String ATTRIBUTE_VIEW_POSIX = "posix";
    private static final String ATTRIBUTE_VIEW_ACL = "acl";
    /** Largest number of symbolic links followed in a chain, as on Linux, so that a loop of links ends. */
    private static final int MAX_SYMBOLIC_LINKS = 40;

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
     * Returns the content of {@code file}, decoded as UTF-8. Assumes file exists.
     *
     * @throws IOException If the file cannot be read, or is not valid UTF-8, with {@link #MESSAGE_INVALID_UTF8}.
     */
    public static String readFromFile(Path file) throws IOException {
        ByteBuffer bytes = ByteBuffer.wrap(Files.readAllBytes(file));
        // Report invalid bytes instead of replacing them with U+FFFD, which the next save would make permanent
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT);
        try {
            return decoder.decode(bytes).toString();
        } catch (CharacterCodingException e) {
            // After a decoding error, the buffer is positioned at the first invalid byte, so the text before it is
            // valid; its lines and characters give the position as an editor shows it
            String validText = new String(bytes.array(), 0, bytes.position(), StandardCharsets.UTF_8);
            long line = validText.chars().filter(c -> c == '\n').count() + 1;
            int column = validText.length() - (validText.lastIndexOf('\n') + 1) + 1;
            // Locale.ROOT prints the position in ASCII digits, which editors use for line and column numbers
            throw new IOException(String.format(Locale.ROOT, MESSAGE_INVALID_UTF8, line, column), e);
        }
    }

    /**
     * Writes given string to a file, creating the file and its missing parent directories if needed.
     * The content is first written to a new temporary file with a unique name next to {@code file}, which then
     * replaces {@code file}, so {@code file} is never left partially written: if writing fails, it keeps its
     * previous content. Other files in the folder are never changed. If {@code file} is a symbolic link, the file it
     * points to is written in the same way, even if it does not exist yet, and the link is kept; the folder of that
     * file must exist.
     * An existing {@code file} keeps its POSIX file permissions or its access control list (on Windows), whichever
     * the file system supports. A new {@code file} gets the permissions that any new file gets, e.g. from the
     * user's umask on POSIX file systems.
     *
     * @throws AccessDeniedException If {@code file} exists but is read-only, or its folder cannot be written to.
     * @throws IOException If the content cannot be written, e.g. because {@code file} is a symbolic link to a file in
     *         a folder that does not exist. An error about the temporary file names {@code file}.
     */
    public static void writeToFile(Path file, String content) throws IOException {
        // A move can replace a read-only file, so check it first to keep the file read-only for the user
        if (Files.exists(file) && !Files.isWritable(file)) {
            throw new AccessDeniedException(file.toString());
        }

        createParentDirsOfFile(file);
        // Replacing a symbolic link would leave the file it points to with the old content, so that file is replaced
        Path target = resolveSymbolicLinks(file);
        Path folder = target.toAbsolutePath().getParent();
        Path tempFile = null;
        try {
            tempFile = createTempFile(folder, target.getFileName().toString());
            // Give the temporary file the data file's permissions before it holds the user's data
            copyPermissions(target, tempFile);
            Files.write(tempFile, content.getBytes(CHARSET));
            replaceFile(tempFile, target);
        } catch (IOException e) {
            deleteAfterFailure(tempFile, e);
            throw toErrorAboutFile(e, file);
        } catch (RuntimeException e) {
            deleteAfterFailure(tempFile, e);
            throw e;
        }
    }

    /**
     * Returns the file that {@code file} points to, following a chain of symbolic links, each relative to the folder
     * of its link, or {@code file} itself if it is not a symbolic link. The file returned need not exist.
     *
     * @throws IOException If a link cannot be read, or the chain has more than {@link #MAX_SYMBOLIC_LINKS} links,
     *         e.g. because it loops.
     */
    public static Path resolveSymbolicLinks(Path file) throws IOException {
        Path target = file;
        int linkCount = 0;
        while (Files.isSymbolicLink(target)) {
            if (linkCount == MAX_SYMBOLIC_LINKS) {
                throw new FileSystemException(file.toString(), null, "Too many levels of symbolic links");
            }
            target = target.resolveSibling(Files.readSymbolicLink(target));
            linkCount++;
        }
        return target;
    }

    /**
     * Returns a new empty file in {@code folder} with a unique name that starts with {@code prefix}. Unlike
     * {@link Files#createTempFile}, which lets only its owner read and write it, it gets the permissions that any new
     * file gets.
     */
    private static Path createTempFile(Path folder, String prefix) throws IOException {
        while (true) {
            Path tempFile = folder.resolve(prefix + Long.toUnsignedString(ThreadLocalRandom.current().nextLong())
                    + TEMP_FILE_SUFFIX);
            try {
                return Files.createFile(tempFile);
            } catch (FileAlreadyExistsException e) {
                // Another file has this name, so another name is tried
            }
        }
    }

    /**
     * Gives {@code target} the POSIX file permissions and the access control list of {@code source}, each if
     * {@code source} exists and the file system supports it.
     */
    private static void copyPermissions(Path source, Path target) throws IOException {
        if (!Files.exists(source)) {
            return;
        }
        Set<String> attributeViews = source.getFileSystem().supportedFileAttributeViews();
        if (attributeViews.contains(ATTRIBUTE_VIEW_POSIX)) {
            Files.setPosixFilePermissions(target, Files.getPosixFilePermissions(source));
        }
        if (attributeViews.contains(ATTRIBUTE_VIEW_ACL)) {
            List<AclEntry> acl = Files.getFileAttributeView(source, AclFileAttributeView.class).getAcl();
            Files.getFileAttributeView(target, AclFileAttributeView.class).setAcl(acl);
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
