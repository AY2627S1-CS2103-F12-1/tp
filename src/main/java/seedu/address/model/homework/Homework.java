package seedu.address.model.homework;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.student.TuitionSubject;

/**
 * Represents a homework record assigned to a student.
 * Guarantees: immutable; title, subject, due date and status are present; the score is optional.
 */
public final class Homework {
    private final Title title;
    private final TuitionSubject subject;
    private final DueDate dueDate;
    private final HomeworkStatus status;
    private final Score score;

    /**
     * Creates newly assigned homework without a score.
     *
     * @throws NullPointerException If any field is null.
     */
    public Homework(Title title, TuitionSubject subject, DueDate dueDate) {
        this(title, subject, dueDate, HomeworkStatus.ASSIGNED, null);
    }

    /**
     * Creates homework with every field given.
     *
     * @param title Title of the homework.
     * @param subject Subject the homework is for.
     * @param dueDate Date the homework is due.
     * @param status Whether the homework is still assigned or completed.
     * @param score Score received for the homework, or null if none is recorded.
     * @throws NullPointerException If {@code title}, {@code subject}, {@code dueDate} or {@code status} is null.
     */
    public Homework(Title title, TuitionSubject subject, DueDate dueDate, HomeworkStatus status, Score score) {
        requireAllNonNull(title, subject, dueDate, status);
        this.title = title;
        this.subject = subject;
        this.dueDate = dueDate;
        this.status = status;
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

    public HomeworkStatus getStatus() {
        return status;
    }

    public Optional<Score> getScore() {
        return Optional.ofNullable(score);
    }

    /** Returns true if this homework is still assigned, i.e. not yet completed. */
    public boolean isAssigned() {
        return status == HomeworkStatus.ASSIGNED;
    }

    /**
     * Returns true if both homework records have the same normalized title ignoring case, the same subject and the
     * same due date. Status and score do not affect identity.
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
                && status == otherHomework.status
                && Objects.equals(score, otherHomework.score);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, subject, dueDate, status, score);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("title", title)
                .add("subject", subject)
                .add("dueDate", dueDate)
                .add("status", status)
                .add("score", score)
                .toString();
    }
}
