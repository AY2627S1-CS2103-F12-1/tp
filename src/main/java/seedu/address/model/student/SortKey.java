package seedu.address.model.student;

import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.function.Function;

/**
 * Defines the single value used to order a student for each supported sort key.
 */
public enum SortKey {
    LEVEL("academic level", order -> order.applyTo(Comparator.comparing(Student::getAcademicLevel))),
    SUBJECT("subject", order -> order.applyTo(Comparator.comparing(SortKey::getFirstSubject,
            String.CASE_INSENSITIVE_ORDER))),
    DAY("regular meeting day", order -> Comparator.comparing(SortKey::getFirstDay,
            Comparator.nullsLast(order.applyTo(Comparator.<DayOfWeek>naturalOrder()))));

    private final String description;
    private final Function<SortOrder, Comparator<Student>> comparatorFactory;

    SortKey(String description, Function<SortOrder, Comparator<Student>> comparatorFactory) {
        this.description = description;
        this.comparatorFactory = comparatorFactory;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns an ordering that keeps missing meeting days last in either direction.
     */
    public Comparator<Student> getComparator(SortOrder order) {
        return comparatorFactory.apply(order);
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
