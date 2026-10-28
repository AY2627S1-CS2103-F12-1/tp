package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stores commands entered in the command box during the current application session and supports navigating them.
 */
public class CommandHistory {
    private final List<String> commands = new ArrayList<>();

    /** Index of the command to show next; equal to the number of commands when positioned after the newest. */
    private int position;

    /**
     * Adds {@code command} unless it is blank or duplicates the newest entry, then resets navigation to the newest.
     *
     * @param command the command that was submitted
     */
    public void add(String command) {
        requireNonNull(command);
        if (!command.isBlank() && (commands.isEmpty() || !commands.getLast().equals(command))) {
            commands.add(command);
        }
        position = commands.size();
    }

    /**
     * Moves backward through the history, staying at the oldest command when the start is reached.
     *
     * @return the command to display, or empty when there is no history
     */
    public Optional<String> previous() {
        if (commands.isEmpty()) {
            return Optional.empty();
        }
        position = Math.max(position - 1, 0);
        return Optional.of(commands.get(position));
    }

    /**
     * Moves forward through the history, returning an empty string after the newest command.
     *
     * @return the next command, an empty string after the newest command, or empty when no command is being shown
     */
    public Optional<String> next() {
        if (position >= commands.size()) {
            return Optional.empty();
        }
        position++;
        return Optional.of(position == commands.size() ? "" : commands.get(position));
    }
}
