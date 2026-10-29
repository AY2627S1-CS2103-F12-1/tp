package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;

/**
 * Converts a Java object instance to JSON and vice versa
 */
public class JsonUtil {
    public static final String MESSAGE_EMPTY_FILE = "The file is empty.";
    public static final String MESSAGE_INVALID_JSON = "The file is not valid JSON near line %d, column %d.";
    public static final String MESSAGE_WRONG_VALUE_TYPE =
            "The file has a value of the wrong type near line %d, column %d.";
    public static final String MESSAGE_CONTENT_AFTER_DATA =
            "The file has unexpected content after the JSON data near line %d, column %d.";
    public static final String MESSAGE_REPEATED_KEY = "The key \"%s\" is repeated near line %d, column %d.";

    private static final Logger logger = LogsCenter.getLogger(JsonUtil.class);
    /** Start of Jackson's message for a key repeated in one object, which gives no position. */
    private static final String JACKSON_REPEATED_KEY_PREFIX = "Duplicate field '";

    private static ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            // Rejects a repeated key, whose last value would otherwise silently replace the earlier ones
            .configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true)
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    static <T> void serializeObjectToJsonFile(Path jsonFile, T objectToSerialize) throws IOException {
        FileUtil.writeToFile(jsonFile, toJsonString(objectToSerialize));
    }

    static <T> T deserializeObjectFromJsonFile(Path jsonFile, Class<T> classOfObjectToDeserialize)
            throws IOException {
        String json = FileUtil.readFromFile(jsonFile);
        // Editors may add a UTF-8 signature. It is an encoding marker, not JSON content.
        if (json.startsWith("\ufeff")) {
            json = json.substring(1);
        }
        // Jackson's message for this names no position and shows the file's content
        if (json.isBlank()) {
            throw new IOException(MESSAGE_EMPTY_FILE);
        }
        try {
            return fromJsonString(json, classOfObjectToDeserialize);
        } catch (JsonProcessingException e) {
            throw describeJsonError(json, e);
        }
    }

    /**
     * Returns an exception whose message says whether {@code json} repeats a key in one object, breaks the JSON
     * syntax or has a value of the wrong type, and gives the line and column near which {@code exception} found it,
     * or {@code exception} itself if no position is known.
     */
    private static IOException describeJsonError(String json, JsonProcessingException exception) {
        JsonParseException syntaxError = findSyntaxError(exception);
        if (syntaxError != null && syntaxError.getOriginalMessage().startsWith(JACKSON_REPEATED_KEY_PREFIX)) {
            IOException repeatedKeyError = findRepeatedKey(json, exception);
            if (repeatedKeyError != null) {
                return repeatedKeyError;
            }
        }
        JsonLocation location = syntaxError == null ? exception.getLocation() : syntaxError.getLocation();
        if (location == null || location.getCharOffset() < 0) {
            return exception;
        }
        String format = syntaxError == null ? MESSAGE_WRONG_VALUE_TYPE : MESSAGE_INVALID_JSON;
        // A value of the wrong type is located at its start, but a syntax error just after the character that
        // breaks the JSON, so the position of a syntax error is moved back onto that character
        long errorOffset = syntaxError == null ? location.getCharOffset() : location.getCharOffset() - 1;
        // At the end of the input, Jackson's column counts from the start of the file, so the position is worked
        // out here instead; a file that ends too early points at its last character that is not whitespace.
        int lastContentIndex = Math.max(json.stripTrailing().length() - 1, 0);
        int offset = (int) Math.max(Math.min(errorOffset, lastContentIndex), 0);
        int lineStart = json.lastIndexOf('\n', offset - 1) + 1;
        long line = json.substring(0, lineStart).chars().filter(c -> c == '\n').count() + 1;
        int column = offset - lineStart + 1;
        // Locale.ROOT prints the position in ASCII digits, which editors use for line and column numbers
        return new IOException(String.format(Locale.ROOT, format, line, column), exception);
    }

    /**
     * Returns an exception naming the first key in {@code json} that is repeated in one object and the line and
     * column where it is repeated, caused by {@code exception}, or null if no such key is found.
     * Jackson reports such a key without its position, so {@code json} is read again here, up to that key.
     */
    private static IOException findRepeatedKey(String json, JsonProcessingException exception) {
        // A new factory does not reject repeated keys, so the reading reaches the repeated key
        try (JsonParser parser = new JsonFactory().createParser(json)) {
            Deque<Set<String>> keysOfOpenObjects = new ArrayDeque<>();
            for (JsonToken token = parser.nextToken(); token != null; token = parser.nextToken()) {
                if (token == JsonToken.START_OBJECT) {
                    keysOfOpenObjects.push(new HashSet<>());
                } else if (token == JsonToken.END_OBJECT) {
                    keysOfOpenObjects.pop();
                } else if (token == JsonToken.FIELD_NAME && !keysOfOpenObjects.peek().add(parser.getCurrentName())) {
                    JsonLocation location = parser.getTokenLocation();
                    return new IOException(String.format(Locale.ROOT, MESSAGE_REPEATED_KEY, parser.getCurrentName(),
                            location.getLineNr(), location.getColumnNr()), exception);
                }
            }
        } catch (IOException e) {
            // Content after the repeated key that is not valid JSON is never reached, so this is not expected
            logger.warning("Could not find the repeated key: " + e);
        }
        return null;
    }

    /**
     * Returns {@code exception} or its nearest cause that is about JSON that breaks the JSON syntax, or null if there
     * is none. Jackson wraps such an error found while mapping a value, e.g. a file that ends inside a list, and the
     * wrapper's position is the start of that value, not the error.
     */
    private static JsonParseException findSyntaxError(Throwable exception) {
        Throwable cause = exception;
        while (cause != null && !(cause instanceof JsonParseException)) {
            cause = cause.getCause();
        }
        return (JsonParseException) cause;
    }

    /**
     * Returns the JSON object from the given file or {@code Optional.empty()} object if the file is not found.
     * If any values are missing from the file, default values will be used, as long as the file is a valid JSON file.
     *
     * @param filePath cannot be null.
     * @param classOfObjectToDeserialize JSON file has to correspond to the structure in the class given here.
     * @throws DataLoadingException if loading of the JSON file failed. A file that Jackson cannot read, e.g. one that
     *         is not valid JSON, gives a cause whose message names the line and column, without Jackson's technical
     *         text.
     */
    public static <T> Optional<T> readJsonFile(
            Path filePath, Class<T> classOfObjectToDeserialize) throws DataLoadingException {
        requireNonNull(filePath);

        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        logger.info("JSON file " + filePath + " found.");

        T jsonFile;

        try {
            jsonFile = deserializeObjectFromJsonFile(filePath, classOfObjectToDeserialize);
            if (jsonFile == null) {
                // Jackson reads a file that holds only the JSON value null as a null object
                throw new IOException("The file holds null instead of a JSON object.");
            }
        } catch (IOException e) {
            logger.warning("Error reading from jsonFile file " + filePath + ": " + e);
            throw new DataLoadingException(e);
        }

        return Optional.of(jsonFile);
    }

    /**
     * Saves the Json object to the specified file.
     * Overwrites existing file if it exists, creates a new file if it doesn't.
     * @param jsonFile cannot be null
     * @param filePath cannot be null
     * @throws IOException if there was an error during writing to the file
     */
    public static <T> void saveJsonFile(T jsonFile, Path filePath) throws IOException {
        requireNonNull(filePath);
        requireNonNull(jsonFile);

        serializeObjectToJsonFile(filePath, jsonFile);
    }


    /**
     * Converts a given string representation of JSON data to an instance of a class
     * @param <T> The generic type to create an instance of
     * @return The instance of T with the specified values in the JSON string
     * @throws IOException If the string is not one JSON value of that class, or anything but whitespace follows it.
     */
    public static <T> T fromJsonString(String json, Class<T> instanceClass) throws IOException {
        try (JsonParser parser = objectMapper.getFactory().createParser(json)) {
            T instance = objectMapper.readValue(parser, instanceClass);
            // Jackson stops after the first value, so content after it would be ignored and lost at the next save
            if (parser.nextToken() != null) {
                JsonLocation location = parser.getTokenLocation();
                throw new IOException(String.format(Locale.ROOT, MESSAGE_CONTENT_AFTER_DATA, location.getLineNr(),
                        location.getColumnNr()));
            }
            return instance;
        }
    }

    /**
     * Converts a given instance of a class into its JSON data string representation
     * @param instance The T object to be converted into the JSON string
     * @param <T> The generic type to create an instance of
     * @return JSON data representation of the given class instance, in string
     */
    public static <T> String toJsonString(T instance) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(instance);
    }

}
