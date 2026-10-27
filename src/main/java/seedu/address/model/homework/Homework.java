package seedu.address.model.homework;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.student.TuitionSubject;

/**
 * Represents a homework record assigned to a student.
 * Guarantees: immutable; title, subject, due date and completion status are present; the score is optional.
 */
public final class Homework {
    private final Title title;
    private final TuitionSubject subject;
    private final DueDate dueDate;
    private final Score score;
    private final HomeworkStatus status;

    /**
     * Creates homework without a score.
     *
     * @throws NullPointerException If any field is null.
     */
    public Homework(Title title, TuitionSubject subject, DueDate dueDate) {
        this(title, subject, dueDate, null);
    }

    /**
     * Creates assigned homework with an independently optional score.
     *
     * @param title Title of the homework.
     * @param subject Subject the homework is for.
     * @param dueDate Date the homework is due.
     * @param score Score received for the homework, or null if none is recorded.
     * @throws NullPointerException If {@code title}, {@code subject} or {@code dueDate} is null.
     */
    public Homework(Title title, TuitionSubject subject, DueDate dueDate, Score score) {
        this(title, subject, dueDate, score, HomeworkStatus.ASSIGNED);
    }

    /**
     * Creates homework with an explicit status and an independently optional score.
     */
    public Homework(Title title, TuitionSubject subject, DueDate dueDate, Score score, HomeworkStatus status) {
        requireAllNonNull(title, subject, dueDate, status);
        this.title = title;
        this.subject = subject;
        this.dueDate = dueDate;
        this.score = score;
        this.status = status;
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

    public HomeworkStatus getStatus() {
        return status;
    }

    /**
     * Returns a copy with only the completion state changed.
     */
    public Homework withStatus(HomeworkStatus newStatus) {
        return new Homework(title, subject, dueDate, score, newStatus);
    }

    /**
     * Returns a copy with only the score changed.
     *
     * @param newScore Score to record, or null to clear the recorded score.
     */
    public Homework withScore(Score newScore) {
        return new Homework(title, subject, dueDate, newScore, status);
    }

    /**
     * Returns true if this homework is due on or after {@code today}.
     */
    public boolean isUpcoming(LocalDate today) {
        return !dueDate.getDate().isBefore(today);
    }

    /**
     * Returns true if this homework is assigned and was due before {@code today}, regardless of its score.
     */
    public boolean isOverdue(LocalDate today) {
        return status == HomeworkStatus.ASSIGNED && !isUpcoming(today);
    }

    /**
     * Returns true if both homework records have the same normalized title ignoring case, the same subject and the
     * same due date. Neither score nor completion status affects identity.
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
                && Objects.equals(score, otherHomework.score)
                && status == otherHomework.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, subject, dueDate, score, status);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("title", title)
                .add("subject", subject)
                .add("dueDate", dueDate)
                .add("score", score)
                .add("status", status)
                .toString();
    }
}
