package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AppHomeUtilTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void resolveHomeDirectory_packagedJar_usesJarFolderInsteadOfWorkingDirectory() throws Exception {
        Path home = Files.createDirectory(temporaryFolder.resolve("TutorFlow 100%"));
        Path jar = Files.createFile(home.resolve("renamed-app.jar"));
        Path other = Files.createDirectory(temporaryFolder.resolve("other"));

        assertEquals(home, AppHomeUtil.resolveHomeDirectory(Path.of(jar.toUri()), other));
    }

    @Test
    public void resolveHomeDirectory_unpackedClasses_usesDevelopmentWorkingDirectory() throws Exception {
        Path classes = Files.createDirectory(temporaryFolder.resolve("classes"));
        Path project = Files.createDirectory(temporaryFolder.resolve("project"));

        assertEquals(project, AppHomeUtil.resolveHomeDirectory(classes, project.resolve(".")));
        assertEquals(Path.of("").toAbsolutePath().normalize(), AppHomeUtil.getHomeDirectory());
    }
}
