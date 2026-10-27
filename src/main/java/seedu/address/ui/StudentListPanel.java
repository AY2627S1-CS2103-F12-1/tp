package seedu.address.ui;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentFilterPredicate;

/**
 * Displays the current student roster, its count, and any active level or subject criteria.
 */
public class StudentListPanel extends UiPart<Region> {
    private static final String FXML = "StudentListPanel.fxml";
    private static final Logger logger = LogsCenter.getLogger(StudentListPanel.class);

    private final Runnable renderingErrorHandler;
    private final BiFunction<Student, Integer, Region> cardFactory;
    private boolean hasRenderingError;
    private boolean isExecutingCommand;
    private Map<Integer, RenderedCard> renderedCards = new HashMap<>();
    private boolean isSorting;
    private RuntimeException renderingFailure;

    @FXML
    private Label studentCount;
    @FXML
    private Label filterDescription;
    @FXML
    private ListView<Student> studentListView;

    /**
     * Creates a panel that updates when the given student list changes.
     */
    public StudentListPanel(ObservableList<Student> students) {
        this(students, () -> { });
    }

    /**
     * Creates a panel that reports ordinary rendering failures after logging their technical cause.
     */
    public StudentListPanel(ObservableList<Student> students, Runnable renderingErrorHandler) {
        this(students, renderingErrorHandler, (student, index) ->
                new StudentCard(student, index, LocalDate.now()).getRoot());
    }

    /**
     * Creates a panel with a controllable card renderer for recovery tests.
     */
    StudentListPanel(ObservableList<Student> students, BiFunction<Student, Integer, Region> cardFactory) {
        this(students, () -> { }, cardFactory);
    }

    /**
     * Creates a panel with a controllable renderer and the owner's rendering error callback.
     */
    StudentListPanel(ObservableList<Student> students, Runnable renderingErrorHandler,
            BiFunction<Student, Integer, Region> cardFactory) {
        super(FXML);
        requireAllNonNull(students, renderingErrorHandler, cardFactory);
        this.renderingErrorHandler = renderingErrorHandler;
        this.cardFactory = cardFactory;
        filterDescription.setMinHeight(Region.USE_PREF_SIZE);
        filterDescription.visibleProperty().bind(filterDescription.textProperty().isNotEmpty());
        filterDescription.managedProperty().bind(filterDescription.visibleProperty());
        // Register before ListView so independent updates reset diagnostics before rendering starts.
        students.addListener((ListChangeListener<Student>) change -> {
            if (!isExecutingCommand) {
                clearRenderingError();
            }
            if (!isSorting) {
                renderedCards.clear();
            }
            updateStudentCount(students.size());
        });
        studentListView.setItems(students);
        studentListView.setCellFactory(unused -> new StudentListViewCell());
        ScrollSupport.enableScrolling(getRoot(), studentListView);
        updateStudentCount(students.size());
    }

    /**
     * Observes active criteria independently of whether the displayed student list changes.
     */
    void bindStudentListPredicate(ReadOnlyObjectProperty<Predicate<Student>> predicate) {
        requireAllNonNull(predicate);
        filterDescription.textProperty().bind(Bindings.createStringBinding(() -> describeFilter(predicate.get()),
                predicate));
    }

    /**
     * Returns the active level and subject description, or empty text for other student views.
     */
    private static String describeFilter(Predicate<Student> predicate) {
        if (!(predicate instanceof StudentFilterPredicate filter)) {
            return "";
        }
        List<String> criteria = new ArrayList<>();
        filter.getAcademicLevel().ifPresent(level -> criteria.add("level " + level));
        filter.getSubject().ifPresent(subject -> criteria.add("subject " + subject));
        return "Filtered: " + String.join("; ", criteria);
    }

    private void updateStudentCount(int count) {
        studentCount.setText(count == 1 ? "1 student" : count + " students");
    }

    boolean hasRenderingError() {
        return hasRenderingError;
    }

    /**
     * Starts a new command's rendering attempt without changing the list, selection or scroll position.
     */
    void clearRenderingError() {
        hasRenderingError = false;
    }

    /**
     * Starts one rendering attempt shared by all display updates emitted during the command.
     */
    void beginCommandRendering() {
        isExecutingCommand = true;
        clearRenderingError();
    }

    /**
     * Allows a later, independent display update to start a fresh rendering attempt.
     */
    void endCommandRendering() {
        isExecutingCommand = false;
    }

    /**
     * Logs the technical cause and notifies the owner once per rendering attempt.
     */
    private void handleRenderingError(RuntimeException exception) {
        if (hasRenderingError) {
            return;
        }
        hasRenderingError = true;
        logger.log(Level.WARNING, "Unexpected error while rendering student results", exception);
        renderingErrorHandler.run();
    }

    /**
     * Clears the current selection and returns to the first student.
     */
    public void showFromTop() {
        studentListView.getSelectionModel().clearSelection();
        studentListView.scrollTo(0);
    }

    /**
     * Rebuilds cached cards after a local-date change without changing selection or scroll position.
     */
    void refreshDate() {
        renderedCards.clear();
        studentListView.refresh();
    }

    /**
     * Captures the visible state and retains old cards before a sort can notify the list view.
     */
    public ViewState beginSort() {
        ViewState state = captureViewState();
        isSorting = true;
        renderingFailure = null;
        renderedCards = new HashMap<>();
        return state;
    }

    /** Captures navigation and rendered cards before a command can replace the roster. */
    ViewState captureViewState() {
        Map<Orientation, Double> scrollPositions = new EnumMap<>(Orientation.class);
        for (Node node : studentListView.lookupAll(".scroll-bar")) {
            ScrollBar scrollBar = (ScrollBar) node;
            scrollPositions.put(scrollBar.getOrientation(), scrollBar.getValue());
        }
        return new ViewState(List.copyOf(studentListView.getSelectionModel().getSelectedIndices()),
                studentListView.getFocusModel().getFocusedIndex(), scrollPositions, new HashMap<>(renderedCards));
    }

    /**
     * Checks every candidate card, including offscreen records, and forces the visible cell layout.
     */
    public void verifySortRendering() {
        for (int i = 0; i < studentListView.getItems().size(); i++) {
            renderCard(studentListView.getItems().get(i), i);
        }
        studentListView.refresh();
        getRoot().applyCss();
        getRoot().layout();
        if (renderingFailure != null) {
            throw renderingFailure;
        }
    }

    /**
     * Makes the previous cards available before the model restores its previous order.
     */
    public void prepareRestore(ViewState state) {
        renderedCards = new HashMap<>(state.renderedCards);
        renderingFailure = null;
    }

    /**
     * Restores selection, focus and scrollbar positions after the previous order is restored.
     */
    public void restoreViewState(ViewState state) {
        studentListView.refresh();
        getRoot().applyCss();
        getRoot().layout();
        studentListView.getSelectionModel().clearSelection();
        for (int index : state.selectedIndices) {
            studentListView.getSelectionModel().select(index);
        }
        studentListView.getFocusModel().focus(state.focusedIndex);
        for (Node node : studentListView.lookupAll(".scroll-bar")) {
            ScrollBar scrollBar = (ScrollBar) node;
            Double position = state.scrollPositions.get(scrollBar.getOrientation());
            if (position != null) {
                scrollBar.setValue(position);
            }
        }
        if (renderingFailure != null) {
            throw renderingFailure;
        }
    }

    /**
     * Ends the sort rendering boundary after successful rendering or restoration.
     */
    public void endSort() {
        isSorting = false;
        renderingFailure = null;
    }

    /**
     * Reuses verified cards so later virtualized cells do not repeat candidate card creation.
     */
    private Region renderCard(Student student, int index) {
        RenderedCard rendered = renderedCards.get(index);
        if (rendered == null || rendered.student() != student) {
            rendered = new RenderedCard(student, cardFactory.apply(student, index + 1));
            renderedCards.put(index, rendered);
        }
        return rendered.root();
    }

    /**
     * Holds an occurrence's rendered card without using student equality as its identity.
     */
    private record RenderedCard(Student student, Region root) {
    }

    /**
     * Holds the previous UI state privately while a sort is being rendered.
     */
    public static final class ViewState {
        private final List<Integer> selectedIndices;
        private final int focusedIndex;
        private final Map<Orientation, Double> scrollPositions;
        private final Map<Integer, RenderedCard> renderedCards;

        private ViewState(List<Integer> selectedIndices, int focusedIndex,
                Map<Orientation, Double> scrollPositions, Map<Integer, RenderedCard> renderedCards) {
            this.selectedIndices = selectedIndices;
            this.focusedIndex = focusedIndex;
            this.scrollPositions = scrollPositions;
            this.renderedCards = renderedCards;
        }
    }

    /**
     * Displays a student card with its current one-based roster index.
     */
    private class StudentListViewCell extends ListCell<Student> {
        @Override
        protected void updateItem(Student student, boolean isEmpty) {
            super.updateItem(student, isEmpty);
            updateCard();
        }

        @Override
        public void updateIndex(int index) {
            super.updateIndex(index);
            updateCard();
        }

        /**
         * Records cell callback failures at the sort boundary instead of losing them in event dispatch.
         */
        private void updateCard() {
            setText(null);
            try {
                setGraphic(isEmpty() || getItem() == null || getIndex() < 0
                        ? null : renderCard(getItem(), getIndex()));
            } catch (RuntimeException exception) {
                setGraphic(null);
                if (isSorting) {
                    if (renderingFailure == null) {
                        renderingFailure = exception;
                    }
                } else {
                    handleRenderingError(exception);
                }
            }
        }
    }
}
