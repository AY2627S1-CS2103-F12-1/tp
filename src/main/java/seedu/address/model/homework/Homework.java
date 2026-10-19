package seedu.address.model.homework;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.student.TuitionSubject;

/**
 * Represents a homework record assigned to a student.
 * Guarantees: immutable; title, subject and due date are present; the score is optional.
 */
public final class Homework {
    private final Title title;
    private final TuitionSubject subject;
    private final DueDate dueDate;
    private final Score score;

    /**
     * Creates homework without a score.
     *
     * @throws NullPointerException If any field is null.
     */
    public Homework(Title title, TuitionSubject subject, DueDate dueDate) {
        this(title, subject, dueDate, null);
    }

    /**
     * Creates homework with every field given.
     *
     * @param title Title of the homework.
     * @param subject Subject the homework is for.
     * @param dueDate Date the homework is due.
     * @param score Score received for the homework, or null if none is recorded.
     * @throws NullPointerException If {@code title}, {@code subject} or {@code dueDate} is null.
     */
    public Homework(Title title, TuitionSubject subject, DueDate dueDate, Score score) {
        requireAllNonNull(title, subject, dueDate);
        this.title = title;
        this.subject = subject;
        this.dueDate = dueDate;
        this.score = score;
    }

    public Title getTitle() {
        return title;
    }

    public TuitionSubject getSubject() {
        return subject;
    }

    public DueDate getDueDate() {
        return dueDate;
    }

    public Optional<Score> getScore() {
        return Optional.ofNullable(score);
    }

    /** Returns true if this homework is due on or after {@code today}. */
    public boolean isUpcoming(LocalDate today) {
        return !dueDate.getDate().isBefore(today);
    }

    /**
     * Returns true if both homework records have the same normalized title ignoring case, the same subject and the
     * same due date. The score does not affect identity.
     */
    public boolean isSameHomework(Homework other) {
        return other != null
                && title.isSameTitleIgnoringCase(other.title)
                && subject == other.subject
                && dueDate.equals(other.dueDate);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Homework otherHomework)) {
            return false;
        }

        return title.equals(otherHomework.title)
                && subject == otherHomework.subject
                && dueDate.equals(otherHomework.dueDate)
                && Objects.equals(score, otherHomework.score);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, subject, dueDate, score);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("title", title)
                .add("subject", subject)
                .add("dueDate", dueDate)
                .add("score", score)
                .toString();
    }
}
