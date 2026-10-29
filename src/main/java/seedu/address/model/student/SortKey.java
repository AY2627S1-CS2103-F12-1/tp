package seedu.address.model.student;

import java.time.DayOfWeek;
import java.util.Comparator;

/**
 * Defines the single value used to order a student for each supported sort key.
 */
public enum SortKey {
    LEVEL("academic level"), SUBJECT("subject"), DAY("regular meeting day");

    private final String description;

    SortKey(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns an ordering that keeps missing meeting days last in either direction.
     */
    public Comparator<Student> getComparator(SortOrder order) {
        return switch (this) {
            case LEVEL -> order.applyTo(Comparator.comparing(Student::getAcademicLevel));
            case SUBJECT -> order.applyTo(Comparator.comparing(SortKey::getFirstSubject,
                    String.CASE_INSENSITIVE_ORDER));
            case DAY -> Comparator.comparing(SortKey::getFirstDay,
                    Comparator.nullsLast(order.applyTo(Comparator.<DayOfWeek>naturalOrder())));
        };
    }

    /**
     * Returns the alphabetically first subject, independently of registration order.
     */
    private static String getFirstSubject(Student student) {
        return student.getSubjects().getSubjects().stream().map(Enum::name)
                .min(String.CASE_INSENSITIVE_ORDER).orElseThrow();
    }

    /**
     * Returns the earliest weekday, or null when the student has no regular meetings.
     */
    private static DayOfWeek getFirstDay(Student student) {
        return student.getLessons().stream().map(lesson -> lesson.getDayOfWeek())
                .min(Comparator.naturalOrder()).orElse(null);
    }
}
