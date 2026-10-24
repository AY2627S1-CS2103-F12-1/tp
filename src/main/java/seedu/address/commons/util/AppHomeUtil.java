package seedu.address.commons.util;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Resolves the folder in which the application may keep its local files. */
public class AppHomeUtil {

    /**
     * Returns the containing folder for a packaged JAR, or the working directory for a development launch.
     * URI conversion preserves spaces and literal percent signs in the installation path.
     */
    public static Path getHomeDirectory() {
        try {
            Path codeLocation = Path.of(AppHomeUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return resolveHomeDirectory(codeLocation, Path.of(""));
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Cannot determine the TutorFlow home folder.", exception);
        }
    }

    /** Resolves a code location without changing the process working directory. */
    static Path resolveHomeDirectory(Path codeLocation, Path workingDirectory) {
        return (Files.isDirectory(codeLocation) ? workingDirectory : codeLocation.toAbsolutePath().getParent())
                .toAbsolutePath().normalize();
    }
}
