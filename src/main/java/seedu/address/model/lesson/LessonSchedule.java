package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;

import seedu.address.model.student.Student;

/**
 * Provides a consistent weekly ordering of regular lessons across students.
 */
public final class LessonSchedule {
    private static final Comparator<Entry> LESSON_ORDER = Comparator
            .comparingInt((Entry entry) -> entry.lesson().getDayOfWeek().getValue())
            .thenComparing(entry -> entry.lesson().getStartTime())
            .thenComparing(entry -> entry.student().getName().toString(), String.CASE_INSENSITIVE_ORDER);

    private LessonSchedule() {
    }

    /**
     * Returns all regular lessons belonging to {@code students}, ordered by weekday, start time and student name.
     */
    public static List<Entry> from(List<Student> students) {
        requireNonNull(students);
        return students.stream()
                .flatMap(student -> student.getLessons().stream()
                        .map(lesson -> new Entry(student, lesson)))
                .sorted(LESSON_ORDER)
                .toList();
    }

    /**
     * Associates a regular lesson with its owning student.
     */
    public record Entry(Student student, RegularLesson lesson) {
        /**
         * Creates an entry with a non-null student and lesson.
         */
        public Entry {
            requireNonNull(student);
            requireNonNull(lesson);
        }
    }
}
