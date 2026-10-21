package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

/**
 * Carries a completed sort and its display-only recovery action for the UI rendering boundary.
 */
public class SortCommandResult extends CommandResult {
    private final Runnable restoreOrder;

    /**
     * Creates a sort result whose recovery action restores the immediately preceding displayed order.
     */
    public SortCommandResult(String feedback, Runnable restoreOrder) {
        super(feedback);
        this.restoreOrder = requireNonNull(restoreOrder);
    }

    /**
     * Restores the previous displayed order if rendering the sort fails.
     */
    public void restorePreviousOrder() {
        restoreOrder.run();
    }
}
