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
    public void readJsonFile_bomBeforeInvalidJson_stillRejectsInvalidContent() throws IOException {
        Path file = testFolder.resolve("invalid.json");
        Files.writeString(file, "\ufeffnot JSON");

        assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(file, SerializableTestClass.class));
    }

    @Test
    public void readJsonFile_nullRoot_throwsDataLoadingException() throws IOException {
        Path file = testFolder.resolve("serialize.json");
        Files.writeString(file, "null");

        assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(file, SerializableTestClass.class));
    }

    @Test
    public void readJsonFile_trailingJsonObject_throwsDataLoadingException() throws IOException {
        Path file = testFolder.resolve("serialize.json");
        Files.writeString(file, SerializableTestClass.JSON_STRING_REPRESENTATION + "{\"name\" : \"Second\"}");

        assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(file, SerializableTestClass.class));
    }

    @Test
    public void readJsonFile_trailingText_throwsDataLoadingException() throws IOException {
        Path file = testFolder.resolve("serialize.json");
        Files.writeString(file, SerializableTestClass.JSON_STRING_REPRESENTATION + "\nextra");

        assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(file, SerializableTestClass.class));
    }

    @Test
    public void readJsonFile_trailingCloseBrace_throwsDataLoadingException() throws IOException {
        Path file = testFolder.resolve("serialize.json");
        Files.writeString(file, SerializableTestClass.JSON_STRING_REPRESENTATION + "}");

        assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(file, SerializableTestClass.class));
    }

    @Test
    public void readJsonFile_bomAndTrailingWhitespace_success() throws Exception {
        Path file = testFolder.resolve("serialize.json");
        Files.writeString(file, "\ufeff" + SerializableTestClass.JSON_STRING_REPRESENTATION + " \t\r\n\n");

        SerializableTestClass serializableTestClass = JsonUtil.readJsonFile(file, SerializableTestClass.class)
                .orElseThrow();

        assertEquals(SerializableTestClass.getNameTestValue(), serializableTestClass.getName());
    }

    @Test
    public void readJsonFile_duplicateKey_throwsDataLoadingException() throws IOException {
        Path file = testFolder.resolve("serialize.json");
        Files.writeString(file, "{\"name\" : \"First\", \"name\" : \"Second\"}");

        assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(file, SerializableTestClass.class));
    }

    @Test
    public void readJsonFile_duplicateNestedKey_throwsDataLoadingException() throws IOException {
        Path file = testFolder.resolve("serialize.json");
        Files.writeString(file, "{\"mapOfIntegerToString\" : {\"1\" : \"One\", \"1\" : \"Uno\"}}");

        assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(file, SerializableTestClass.class));
    }

    @Test
    public void fromJsonString_trailingJsonObject_throwsIoException() {
        assertThrows(IOException.class, () -> JsonUtil.fromJsonString("{} {}", SerializableTestClass.class));
    }

    //TODO: @Test jsonUtil_readJsonStringToObjectInstance_correctObject()

    //TODO: @Test jsonUtil_writeThenReadObjectToJson_correctObject()
}
