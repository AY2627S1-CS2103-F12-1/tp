package seedu.address.testutil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;

/**
 * Captures a component's diagnostics in memory and restores its logger when closed.
 */
public final class LogRecorder extends Handler implements AutoCloseable {
    private final Logger logger;
    private final Level previousLevel;
    private final List<LogRecord> records = new ArrayList<>();

    /**
     * Captures all levels after the component has initialized its logger.
     */
    public LogRecorder(Class<?> component) {
        logger = LogsCenter.getLogger(component);
        previousLevel = logger.getLevel();
        logger.setLevel(Level.ALL);
        logger.addHandler(this);
    }

    public List<LogRecord> getRecords() {
        return List.copyOf(records);
    }

    public List<LogRecord> getFailures() {
        return records.stream().filter(record -> record.getThrown() != null).toList();
    }

    @Override
    public void publish(LogRecord record) {
        records.add(record);
    }

    @Override
    public void flush() {
        // Records remain in memory until the test inspects them.
    }

    @Override
    public void close() {
        logger.removeHandler(this);
        logger.setLevel(previousLevel);
    }
}
