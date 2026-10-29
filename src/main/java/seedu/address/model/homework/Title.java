package seedu.address.model.homework;

import static java.util.Objects.requireNonNull;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the title of a homework record.
 * Guarantees: immutable; whitespace and Unicode (to NFC) are normalized; length and characters satisfy the Add
 * Homework specification.
 */
public final class Title {
    public static final int MAX_LENGTH = 100;
    public static final String MESSAGE_CONSTRAINTS = "Homework title must be 1 to " + MAX_LENGTH + " characters and"
            + " may contain only letters, numbers, punctuation, symbols and spaces.";
    public static final String MESSAGE_EMPTY = "The homework title is empty. Enter 1 to " + MAX_LENGTH + " characters.";
    public static final String MESSAGE_TOO_LONG =
            "Homework title must be at most " + MAX_LENGTH + " characters, but has %d.";
    public static final String MESSAGE_UNSUPPORTED_CHARACTER = "Homework title has an unsupported character: %s. "
            + "Titles may contain only letters, numbers, punctuation, symbols and spaces.";

    private static final Pattern WHITESPACE = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);
    /** A character that is not a letter, number, punctuation, symbol or space. */
    private static final Pattern UNSUPPORTED_CHARACTER = Pattern.compile("[^\\p{L}\\p{N}\\p{P}\\p{S} ]");

    private final String value;

    /**
     * Creates a title after converting it to Unicode NFC, replacing consecutive whitespace with one space and
     * removing surrounding whitespace. NFC gives canonically equivalent text one encoding, e.g. {@code é} written as
     * one character or as {@code e} followed by a combining accent. Capitalization is preserved for display.
     *
     * @throws IllegalArgumentException With {@link #MESSAGE_EMPTY} if the normalized title is empty, with
     *         {@link #MESSAGE_TOO_LONG} if it is longer than {@link #MAX_LENGTH} characters, or with
     *         {@link #MESSAGE_UNSUPPORTED_CHARACTER} naming the first unsupported character.
     */
    public Title(String rawTitle) {
        requireNonNull(rawTitle);
        String nfcTitle = Normalizer.normalize(rawTitle, Normalizer.Form.NFC);
        String normalizedTitle = WHITESPACE.matcher(nfcTitle).replaceAll(" ").strip();
        int length = normalizedTitle.codePointCount(0, normalizedTitle.length());
        if (length < 1) {
            throw new IllegalArgumentException(MESSAGE_EMPTY);
        }
        if (length > MAX_LENGTH) {
            // Locale.ROOT prints the length in ASCII digits in every locale
            throw new IllegalArgumentException(String.format(Locale.ROOT, MESSAGE_TOO_LONG, length));
        }
        Matcher unsupportedCharacter = UNSUPPORTED_CHARACTER.matcher(normalizedTitle);
        if (unsupportedCharacter.find()) {
            throw new IllegalArgumentException(String.format(MESSAGE_UNSUPPORTED_CHARACTER,
                    describeCharacter(normalizedTitle.codePointAt(unsupportedCharacter.start()))));
        }
        value = normalizedTitle;
    }

    /**
     * Returns the Unicode code of {@code codePoint} and its name if it has one, e.g. {@code U+200B (ZERO WIDTH
     * SPACE)}, so that an invisible character can be identified.
     */
    private static String describeCharacter(int codePoint) {
        String code = String.format(Locale.ROOT, "U+%04X", codePoint);
        String name = Character.getName(codePoint);
        return name == null ? code : code + " (" + name + ")";
    }

    /**
     * Returns true if both normalized titles have the same characters regardless of case.
     */
    public boolean isSameTitleIgnoringCase(Title other) {
        return other != null && value.equalsIgnoreCase(other.value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Title title && value.equals(title.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
