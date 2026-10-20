package seedu.address.logic.parser;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.student.SortKey;
import seedu.address.model.student.SortOrder;

/**
 * Parses one student sorting key and an optional direction before any display changes occur.
 */
public class SortCommandParser implements Parser<SortCommand> {
    private static final String PREFIX_KEY = "by/";
    private static final String PREFIX_ORDER = "o/";
    private static final List<String> ALLOWED_PREFIXES = List.of(PREFIX_KEY, PREFIX_ORDER);
    private static final Pattern UNKNOWN_PREFIX = Pattern.compile("(?<!\\S)[^\\s/]+/");
    private static final Logger logger = LogsCenter.getLogger(SortCommandParser.class);

    @Override
    public SortCommand parse(String arguments) throws ParseException {
        try {
            Map<String, String> values = StrictArgumentTokenizer.tokenize(arguments, ALLOWED_PREFIXES,
                    SortCommand.MESSAGE_INVALID_FORMAT, SortCommand.MESSAGE_REPEATED_PREFIX).values();
            if (!values.containsKey(PREFIX_KEY)) {
                throw new ParseException(SortCommand.MESSAGE_MISSING_KEY);
            }
            for (String value : values.values()) {
                if (UNKNOWN_PREFIX.matcher(value).find()) {
                    throw new ParseException(SortCommand.MESSAGE_INVALID_FORMAT);
                }
            }
            SortKey key = parseKey(values.get(PREFIX_KEY));
            SortOrder order = values.containsKey(PREFIX_ORDER)
                    ? parseOrder(values.get(PREFIX_ORDER)) : SortOrder.ASCENDING;
            return new SortCommand(key, order);
        } catch (ParseException exception) {
            logger.fine(() -> "Rejected student sort: " + exception.getMessage());
            throw exception;
        }
    }

    /**
     * Returns the supported key represented by a stripped, case-insensitive value.
     */
    private static SortKey parseKey(String value) throws ParseException {
        return switch (value.strip().toLowerCase(Locale.ROOT)) {
            case "level" -> SortKey.LEVEL;
            case "subject" -> SortKey.SUBJECT;
            case "day" -> SortKey.DAY;
            default -> throw new ParseException(SortCommand.MESSAGE_INVALID_KEY);
        };
    }

    /**
     * Returns the supported direction represented by a stripped, case-insensitive value.
     */
    private static SortOrder parseOrder(String value) throws ParseException {
        return switch (value.strip().toLowerCase(Locale.ROOT)) {
            case "asc" -> SortOrder.ASCENDING;
            case "desc" -> SortOrder.DESCENDING;
            default -> throw new ParseException(SortCommand.MESSAGE_INVALID_ORDER);
        };
    }
}
