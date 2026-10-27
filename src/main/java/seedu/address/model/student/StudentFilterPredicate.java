package seedu.address.model.student;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Matches students against an academic level, a tuition subject, or both.
 * Every supplied criterion must match; omitted criteria impose no restriction.
 */
public final class StudentFilterPredicate implements Predicate<Student> {
    public static final String MESSAGE_CONSTRAINTS = "At least one filter must be provided";

    private final Optional<AcademicLevel> academicLevel;
    private final Optional<TuitionSubject> subject;

    /**
     * Creates a predicate with at least one of {@code academicLevel} and {@code subject} present.
     *
     * @throws IllegalArgumentException If both criteria are absent.
     * @throws NullPointerException If either optional is null.
     */
    public StudentFilterPredicate(Optional<AcademicLevel> academicLevel, Optional<TuitionSubject> subject) {
        requireAllNonNull(academicLevel, subject);
        if (academicLevel.isEmpty() && subject.isEmpty()) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        this.academicLevel = academicLevel;
        this.subject = subject;
    }

    public Optional<AcademicLevel> getAcademicLevel() {
        return academicLevel;
    }

    public Optional<TuitionSubject> getSubject() {
        return subject;
    }

    @Override
    public boolean test(Student student) {
        requireNonNull(student);
        boolean matchesLevel = academicLevel.map(level -> level == student.getAcademicLevel()).orElse(true);
        boolean matchesSubject = subject.map(value -> student.getSubjects().getSubjects().contains(value)).orElse(true);
        return matchesLevel && matchesSubject;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof StudentFilterPredicate predicate
                && academicLevel.equals(predicate.academicLevel) && subject.equals(predicate.subject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(academicLevel, subject);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("academicLevel", academicLevel).add("subject", subject).toString();
    }
}
