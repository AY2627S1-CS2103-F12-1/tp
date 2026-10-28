package seedu.address.testutil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * A utility class for test cases.
 */
public class TestUtil {

    /** Locale whose default number format uses Arabic-Indic digits, e.g. U+0661 for 1. */
    public static final Locale ARABIC_EGYPT = Locale.forLanguageTag("ar-EG");

    /**
     * Folder used for temp files created during testing. Ignored by Git.
     */
    private static final Path SANDBOX_FOLDER = Paths.get("src", "test", "data", "sandbox");

    /**
     * Appends {@code fileName} to the sandbox folder path and returns the resulting path.
     * Creates the sandbox folder if it doesn't exist.
     */
    public static Path getFilePathInSandboxFolder(String fileName) {
        try {
            Files.createDirectories(SANDBOX_FOLDER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return SANDBOX_FOLDER.resolve(fileName);
    }

    /**
     * Runs {@code action} with {@code locale} as the default locale for formatting numbers, then restores the
     * previous default, so that a test can check output that must not depend on the user's locale.
     */
    public static void runWithFormatLocale(Locale locale, Runnable action) {
        Locale previousLocale = Locale.getDefault(Locale.Category.FORMAT);
        Locale.setDefault(Locale.Category.FORMAT, locale);
        try {
            action.run();
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, previousLocale);
        }
    }
}
