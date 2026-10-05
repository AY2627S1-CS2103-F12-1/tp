package seedu.address.model.student;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a nonempty set of a student's tuition subjects.
 * Iteration and display retain the order in which subjects were supplied.
 */
public final class TuitionSubjects {
    public static final String MESSAGE_CONSTRAINTS = "Subjects must contain one or more of: MATH, PHYSICS, "
            + "CHEMISTRY, separated by commas.";

    private final Set<TuitionSubject> values;

    /**
     * Creates a set from a comma-separated list of subject names, ignoring case and surrounding whitespace.
     *
     * @throws IllegalArgumentException If any subject is empty, unsupported, or repeated.
     */
    public TuitionSubjects(String rawSubjects) {
        requireNonNull(rawSubjects);
        Set<TuitionSubject> parsedSubjects = new LinkedHashSet<>();
        for (String rawSubject : rawSubjects.split(",", -1)) {
            TuitionSubject subject = parseSubject(rawSubject);
            if (!parsedSubjects.add(subject)) {
                throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
            }
        }
        values = Collections.unmodifiableSet(parsedSubjects);
    }

    /**
     * Returns a supported subject after removing surrounding whitespace and ignoring case.
     *
     * @throws IllegalArgumentException If the subject is empty or unsupported.
     */
    private static TuitionSubject parseSubject(String rawSubject) {
        try {
            return TuitionSubject.valueOf(rawSubject.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS, exception);
        }
    }

    /** Returns an unmodifiable set whose iteration order matches the input order. */
    public Set<TuitionSubject> getSubjects() {
        return values;
    }

    @Override
    public String toString() {
        return values.stream().map(Enum::name).collect(Collectors.joining(", "));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TuitionSubjects subjects && values.equals(subjects.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }
}
