package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentDisplayList;

/**
 * Represents the in-memory model of the app data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final ReadOnlyObjectWrapper<Predicate<Student>> studentListPredicate =
            new ReadOnlyObjectWrapper<>(PREDICATE_SHOW_ALL_STUDENTS);
    private final FilteredList<Student> filteredStudents;
    private final StudentDisplayList displayedStudents;
    private final ObservableList<Student> unmodifiableDisplayedStudents;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredStudents = new FilteredList<>(this.addressBook.getStudentList());
        filteredStudents.predicateProperty().bind(studentListPredicate);
        displayedStudents = new StudentDisplayList(filteredStudents);
        unmodifiableDisplayedStudents = FXCollections.unmodifiableObservableList(displayedStudents);
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    //=========== Students =================================================================================

    @Override
    public boolean hasStudent(Student student) {
        requireNonNull(student);
        return addressBook.hasStudent(student);
    }

    @Override
    public void addStudent(Student student) {
        addressBook.addStudent(student);
    }

    @Override
    public void setStudent(Student target, Student editedStudent) {
        requireAllNonNull(target, editedStudent);
        addressBook.setStudent(target, editedStudent);
    }

    @Override
    public void deleteStudent(Student student) {
        addressBook.deleteStudent(student);
    }

    @Override
    public ObservableList<Student> getStudentList() {
        return unmodifiableDisplayedStudents;
    }

    @Override
    public void updateFilteredStudentList(Predicate<Student> predicate) {
        studentListPredicate.set(requireNonNull(predicate));
        displayedStudents.resetOrder();
    }

    @Override
    public Predicate<Student> getFilteredStudentListPredicate() {
        return studentListPredicate.get();
    }

    @Override
    public ReadOnlyObjectProperty<Predicate<Student>> studentListPredicateProperty() {
        return studentListPredicate.getReadOnlyProperty();
    }

    @Override
    public void setStudentListOrder(List<Student> students) {
        displayedStudents.setOrder(students);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs);
    }

}
