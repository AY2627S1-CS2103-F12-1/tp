package seedu.address.testutil;

import java.time.LocalDate;

import seedu.address.model.homework.DueDate;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.homework.Score;
import seedu.address.model.homework.Title;
import seedu.address.model.student.TuitionSubject;

/** Builds a valid homework record for tests, allowing individual fields to be changed. */
public class HomeworkBuilder {
    /** Fixed date used to infer the year of short-form due dates, so tests never depend on the clock. */
    public static final LocalDate DEFAULT_TODAY = LocalDate.of(2026, 10, 5);

    private String title = "Complete algebra worksheet";
    private String subject = "MATH";
    private String dueDate = "2026-10-15";
    private HomeworkStatus status = HomeworkStatus.ASSIGNED;
    private Score score;

    /** Creates a builder with the default homework fields. */
    public HomeworkBuilder() {
    }

    /** Creates a builder with the fields of {@code homeworkToCopy}. */
    public HomeworkBuilder(Homework homeworkToCopy) {
        title = homeworkToCopy.getTitle().toString();
        subject = homeworkToCopy.getSubject().name();
        dueDate = homeworkToCopy.getDueDate().toString();
        status = homeworkToCopy.getStatus();
        score = homeworkToCopy.getScore().orElse(null);
    }

    /** Returns this builder with a different title. */
    public HomeworkBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    /** Returns this builder with a different subject. */
    public HomeworkBuilder withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    /** Returns this builder with a different due date, given in {@code YYYY-MM-DD} or {@code MM-DD} format. */
    public HomeworkBuilder withDueDate(String dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    /** Returns this builder with a different status. */
    public HomeworkBuilder withStatus(HomeworkStatus status) {
        this.status = status;
        return this;
    }

    /** Returns this builder with a score. */
    public HomeworkBuilder withScore(int score) {
        this.score = new Score(score);
        return this;
    }

    /** Returns a homework record using the configured fields. */
    public Homework build() {
        return new Homework(new Title(title), TuitionSubject.parse(subject), DueDate.parse(dueDate, DEFAULT_TODAY),
                status, score);
    }
}
