package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Keeps the commands entered in the command box during the current session, oldest first,
 * and steps back and forth through them like a shell history.
 */
public class CommandHistory {
    private final List<String> commands = new ArrayList<>();

    /** Index of the command being shown; equal to the number of commands when none is shown. */
    private int position;

    /**
     * Adds {@code command} as the newest entry unless it is blank or the same as the newest entry,
     * then moves the position to after the newest entry.
     */
    public void add(String command) {
        requireNonNull(command);
        if (!command.isBlank() && (commands.isEmpty() || !commands.getLast().equals(command))) {
            commands.add(command);
        }
        position = commands.size();
    }

    /**
     * Returns the command before the one being shown, or the oldest command if it is already shown.
     * Returns an empty {@code Optional} if there are no commands.
     */
    public Optional<String> previous() {
        if (commands.isEmpty()) {
            return Optional.empty();
        }
        position = Math.max(position - 1, 0);
        return Optional.of(commands.get(position));
    }

    /**
     * Returns the command after the one being shown, or an empty string when moving past the newest command.
     * Returns an empty {@code Optional} if no command is being shown.
     */
    public Optional<String> next() {
        if (position >= commands.size()) {
            return Optional.empty();
        }
        position++;
        return Optional.of(position == commands.size() ? "" : commands.get(position));
    }
}
