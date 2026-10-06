package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.testutil.SerializableTestClass;
import seedu.address.testutil.TestUtil;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    private static final Path SERIALIZATION_FILE = TestUtil.getFilePathInSandboxFolder("serialize.json");
    private static final String OLD_CONTENT = "old content";

    @TempDir
    public Path testFolder;

    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }

    @Test
    public void saveJsonFile_existingFile_replacedWithoutTemporaryFile() throws IOException {
        Path file = testFolder.resolve("serialize.json");
        Files.writeString(file, OLD_CONTENT);
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.saveJsonFile(serializableTestClass, file);

        assertEquals(SerializableTestClass.JSON_STRING_REPRESENTATION, FileUtil.readFromFile(file));
        try (Stream<Path> files = Files.list(testFolder)) {
            assertEquals(List.of(file), files.toList());
        }
    }

    @Test
    public void saveJsonFile_serializationFails_keepsOldContent() throws IOException {
        Path file = testFolder.resolve("serialize.json");
        Files.writeString(file, OLD_CONTENT);

        // Jackson cannot serialize an object without properties
        assertThrows(IOException.class, () -> JsonUtil.saveJsonFile(new Object(), file));

        assertEquals(OLD_CONTENT, FileUtil.readFromFile(file));
    }

    @Test
    public void readJsonFile_nullRoot_throwsDataLoadingException() throws IOException {
        Path file = testFolder.resolve("serialize.json");
        Files.writeString(file, "null");

        assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(file, SerializableTestClass.class));
    }

    //TODO: @Test jsonUtil_readJsonStringToObjectInstance_correctObject()

    //TODO: @Test jsonUtil_writeThenReadObjectToJson_correctObject()
}
