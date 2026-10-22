package seedu.address.ui;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.input.ScrollEvent;

/**
 * Routes scrolling over section headings and padding to the section's scrollable content.
 */
final class ScrollSupport {
    private ScrollSupport() {
    }

    /**
     * Enables scrolling throughout a section containing a list view or text area without changing keyboard focus.
     */
    static void enableScrolling(Node section, Control control) {
        section.addEventHandler(ScrollEvent.SCROLL, event -> {
            String selector = control instanceof ListView<?> ? ".virtual-flow" : ".scroll-pane > .viewport";
            Node scrollTarget = control.lookup(selector);
            if (scrollTarget == null || !scrollTarget.isVisible() || isWithin(event.getTarget(), scrollTarget)) {
                return;
            }

            // Let the control's skin interpret wheel lines, touchpad pixels, and gesture inertia.
            Event.fireEvent(scrollTarget, event.copyFor(scrollTarget, scrollTarget));
            event.consume();
        });
    }

    private static boolean isWithin(EventTarget target, Node ancestor) {
        if (target instanceof Node node) {
            for (Node current = node; current != null; current = current.getParent()) {
                if (current == ancestor) {
                    return true;
                }
            }
        }
        return false;
    }
}
