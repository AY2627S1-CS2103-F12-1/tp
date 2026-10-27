package seedu.address.model.student;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.DayOfWeek;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Matches students against an academic level, a tuition subject, and a regular meeting day.
 * Every supplied criterion must match; omitted criteria impose no restriction.
 */
public final class StudentFilterPredicate implements Predicate<Student> {
    public static final String MESSAGE_CONSTRAINTS = "At least one filter must be provided";

    private final Optional<AcademicLevel> academicLevel;
    private final Optional<TuitionSubject> subject;
    private final Optional<DayOfWeek> day;

    /**
     * Creates a predicate with at least one of {@code academicLevel} and {@code subject} present.
     *
     * @throws IllegalArgumentException If both criteria are absent.
     * @throws NullPointerException If either optional is null.
     */
    public StudentFilterPredicate(Optional<AcademicLevel> academicLevel, Optional<TuitionSubject> subject) {
        this(academicLevel, subject, Optional.empty());
    }

    /** Creates a predicate requiring every supplied criterion, including any lesson on {@code day}. */
    public StudentFilterPredicate(Optional<AcademicLevel> academicLevel, Optional<TuitionSubject> subject,
            Optional<DayOfWeek> day) {
        requireAllNonNull(academicLevel, subject, day);
        if (academicLevel.isEmpty() && subject.isEmpty() && day.isEmpty()) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        this.academicLevel = academicLevel;
        this.subject = subject;
        this.day = day;
    }

    public Optional<AcademicLevel> getAcademicLevel() {
        return academicLevel;
    }

    public Optional<TuitionSubject> getSubject() {
        return subject;
    }

    public Optional<DayOfWeek> getDay() {
        return day;
    }

    @Override
    public boolean test(Student student) {
        requireNonNull(student);
        boolean matchesLevel = academicLevel.map(level -> level == student.getAcademicLevel()).orElse(true);
        boolean matchesSubject = subject.map(value -> student.getSubjects().getSubjects().contains(value)).orElse(true);
        boolean matchesDay = day.map(value -> student.getLessons().stream()
                .anyMatch(lesson -> lesson.getDayOfWeek() == value)).orElse(true);
        return matchesLevel && matchesSubject && matchesDay;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof StudentFilterPredicate predicate
                && academicLevel.equals(predicate.academicLevel) && subject.equals(predicate.subject)
                && day.equals(predicate.day);
    }

    @Override
    public int hashCode() {
        return Objects.hash(academicLevel, subject, day);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("academicLevel", academicLevel).add("subject", subject)
                .add("day", day).toString();
    }
}
