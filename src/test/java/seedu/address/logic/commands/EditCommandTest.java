package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalHomeworks.ALGEBRA;
import static seedu.address.testutil.TypicalRegularLessons.MONDAY_MATH;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditStudentDescriptor;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.EditCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentName;
import seedu.address.testutil.StudentBuilder;

public class EditCommandTest {
    private final EditCommandParser parser = new EditCommandParser();

    @Test
    public void execute_oneField_preservesUnspecifiedFieldsAndLinkedRecords() throws Exception {
        Student original = new StudentBuilder().withHomeworks(ALGEBRA).withLessons(MONDAY_MATH).build();
        ModelManager model = modelWith(original);
        Student expected = new StudentBuilder().withGuardianName("Jane Tan")
                .withHomeworks(ALGEBRA).withLessons(MONDAY_MATH).build();

        CommandResult result = execute("1 gn/Jane Tan", model);

        assertEquals(String.format(EditCommand.MESSAGE_SUCCESS, Messages.format(expected)),
                result.getFeedbackToUser());
        assertEquals(List.of(expected), model.getStudentList());
        assertEquals(original.getHomeworks(), model.getStudentList().get(0).getHomeworks());
        assertEquals(original.getLessons(), model.getStudentList().get(0).getLessons());
    }

    @Test
    public void execute_allFields_replacesOnlySelectedStudentAtSameIndex() throws Exception {
        Student first = new StudentBuilder().build();
        Student original = new StudentBuilder().withName("Jane Tan").withPhone("92345678")
                .withHomeworks(ALGEBRA).withLessons(MONDAY_MATH).build();
        ModelManager model = modelWith(first, original);
        Student expected = new StudentBuilder().withName("Jenna Tan").withAcademicLevel("S4")
                .withSubjects("MATH, PHYSICS").withPhone("93334444").withGuardianName("May Tan")
                .withGuardianPhone("95554444").withHomeworks(ALGEBRA).withLessons(MONDAY_MATH).build();

        execute("2 n/Jenna Tan l/S4 s/MATH, PHYSICS p/93334444 gn/May Tan gp/95554444", model);

        assertEquals(List.of(first, expected), model.getStudentList());
    }

    @Test
    public void execute_sameNameDifferentPhoneAndSamePhoneDifferentName_areAllowed() throws Exception {
        Student first = new StudentBuilder().build();
        Student second = new StudentBuilder().withName("Jane Tan").withPhone("92345678").build();
        ModelManager model = modelWith(first, second);

        execute("2 n/John Tan", model);
        assertEquals(new StudentBuilder().withPhone("92345678").build(), model.getStudentList().get(1));

        execute("2 n/Jane Tan p/91234567", model);
        assertEquals(new StudentBuilder().withName("Jane Tan").build(), model.getStudentList().get(1));
    }

    @Test
    public void execute_duplicateIdentity_isRejectedWithoutChangingRoster() throws ParseException {
        Student first = new StudentBuilder().build();
        Student second = new StudentBuilder().withName("Jane Tan").withPhone("92345678").build();
        ModelManager model = modelWith(first, second);

        assertFailure("2 n/JOHN TAN p/91234567", model, EditCommand.MESSAGE_DUPLICATE);
        assertEquals(List.of(first, second), model.getStudentList());
    }

    @Test
    public void execute_caseOnlyNameChange_succeeds() throws Exception {
        ModelManager model = modelWith(new StudentBuilder().build());

        execute("1 n/JOHN TAN", model);

        assertEquals("JOHN TAN", model.getStudentList().get(0).getName().toString());
    }

    @Test
    public void execute_sameValues_isRejectedWithoutChangingRoster() throws ParseException {
        Student original = new StudentBuilder().build();
        ModelManager model = modelWith(original);

        assertFailure("1 l/s3 s/math gn/Mary   Tan", model, EditCommand.MESSAGE_NO_CHANGES);
        assertEquals(List.of(original), model.getStudentList());
    }

    @Test
    public void execute_removingHomeworkSubject_isRejected() throws ParseException {
        Student original = new StudentBuilder().withSubjects("MATH, PHYSICS")
                .withHomeworks(ALGEBRA).build();
        ModelManager model = modelWith(original);

        assertFailure("1 s/PHYSICS", model, EditCommand.MESSAGE_SUBJECT_IN_USE);
        assertEquals(List.of(original), model.getStudentList());
    }

    @Test
    public void execute_removingLessonSubject_isRejected() throws ParseException {
        Student original = new StudentBuilder().withSubjects("MATH, PHYSICS")
                .withLessons(MONDAY_MATH).build();
        ModelManager model = modelWith(original);

        assertFailure("1 s/PHYSICS", model, EditCommand.MESSAGE_SUBJECT_IN_USE);
        assertEquals(List.of(original), model.getStudentList());
    }

    @Test
    public void execute_addingSubject_preservesHomeworkAndLessons() throws Exception {
        Student original = new StudentBuilder().withHomeworks(ALGEBRA).withLessons(MONDAY_MATH).build();
        ModelManager model = modelWith(original);

        execute("1 s/MATH, PHYSICS", model);

        assertEquals(new StudentBuilder().withSubjects("MATH, PHYSICS")
                .withHomeworks(ALGEBRA).withLessons(MONDAY_MATH).build(), model.getStudentList().get(0));
    }

    @Test
    public void execute_reorderingSubjects_updatesDisplayedOrder() throws Exception {
        Student original = new StudentBuilder().withSubjects("MATH, PHYSICS").withHomeworks(ALGEBRA).build();
        ModelManager model = modelWith(original);

        execute("1 s/PHYSICS, MATH", model);

        Student edited = model.getStudentList().get(0);
        assertEquals("PHYSICS, MATH", edited.getSubjects().toString());
        assertEquals(original.getHomeworks(), edited.getHomeworks());
    }

    @Test
    public void execute_removingUnusedSubject_succeeds() throws Exception {
        Student original = new StudentBuilder().withSubjects("MATH, PHYSICS")
                .withHomeworks(ALGEBRA).withLessons(MONDAY_MATH).build();
        ModelManager model = modelWith(original);

        execute("1 s/MATH", model);

        assertEquals(new StudentBuilder().withHomeworks(ALGEBRA).withLessons(MONDAY_MATH).build(),
                model.getStudentList().get(0));
    }

    @Test
    public void execute_emptyOrOutOfRangeIndex_isRejected() throws ParseException {
        ModelManager emptyModel = modelWith();
        assertFailure("1 n/Jane", emptyModel, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);

        Student original = new StudentBuilder().build();
        ModelManager model = modelWith(original);
        assertFailure("2 n/Jane", model, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        assertEquals(List.of(original), model.getStudentList());
    }

    @Test
    public void getSaveFailureMessage_returnsEditSpecificMessage() throws ParseException {
        assertEquals(EditCommand.MESSAGE_SAVE_FAILURE,
                parser.parse("1 n/Jane").getSaveFailureMessage("default"));
    }

    @Test
    public void equalsAndToString() throws ParseException {
        EditCommand editFirst = parser.parse("1 n/Jane");
        assertTrue(editFirst.equals(editFirst));
        assertTrue(editFirst.equals(parser.parse("1 n/Jane")));
        assertFalse(editFirst.equals(parser.parse("2 n/Jane")));
        assertFalse(editFirst.equals(parser.parse("1 n/Mary")));
        assertFalse(editFirst.equals(null));
        assertFalse(editFirst.equals("edit"));
        EditStudentDescriptor descriptor = new EditStudentDescriptor(new StudentName("Jane"), null,
                null, null, null, null);
        assertEquals(EditCommand.class.getCanonicalName()
                + "{index=" + Index.fromOneBased(1) + ", descriptor=" + descriptor + "}", editFirst.toString());
    }

    private CommandResult execute(String arguments, ModelManager model) throws ParseException, CommandException {
        return parser.parse(arguments).execute(model);
    }

    private void assertFailure(String arguments, ModelManager model, String expectedMessage) throws ParseException {
        CommandException exception = assertThrows(CommandException.class, () -> execute(arguments, model));
        assertEquals(expectedMessage, exception.getMessage());
    }

    private static ModelManager modelWith(Student... students) {
        ModelManager model = new ModelManager();
        for (Student student : students) {
            model.addStudent(student);
        }
        return model;
    }
}
