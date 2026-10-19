---
layout: page
title: Logging guide
---

* The project uses the `java.util.logging` package for logging.
* `LogsCenter` manages logging levels and destinations.
* A class obtains a `Logger` with `LogsCenter.getLogger(Class)`, which logs messages at the configured level.
* Log messages are written to the console and a `.log` file.
* The output logging level can be controlled by changing the `LOG_LEVEL` constant in the `LogsCenter` class.
* **When choosing a level for a log message**, follow the conventions given in [_[se-edu/guides] Java: Logging conventions_](https://se-education.org/guides/conventions/java/logging.html).

## Student sorting diagnostics

Sorting reuses the existing command-entry log in `LogicManager` and result log in `MainWindow`.
The result is logged only after the sorted student cards pass rendering verification.

Event | Level | Owner and context
------|-------|------------------
Invalid sort parameters | `FINE` | `SortCommandParser`: validation reason; expected input errors are not warnings
Applying an order | `FINE` | `SortCommand`: normalized key, direction and displayed count
Unexpected ordering failure with preserved/restored view | `WARNING` | `SortCommand`: key, direction, ordering phase and recovery outcome, with the exception cause
Unexpected rendering failure with restored view | `WARNING` | `MainWindow`: rendering phase, recovery outcome and the sort count/key/direction, with the exception cause
Failure to restore the prior state | `SEVERE` | The recovery owner: original exception and any additional recovery exception

The component that owns recovery writes one technical exception record. Other layers propagate the failure or
display its friendly message without logging the same stack trace again. The command box displays
`TutorFlow could not sort the students due to an internal error.`; diagnostic causes remain in the logs.
New sort diagnostics do not dump student objects, names, phone numbers or the roster.

`LogRecorder` captures records with an in-memory handler in tests. Tests check severity, cause and failure-record
count, then close the recorder to remove its handler and restore the previous logger level. They do not depend
on timestamps, stack-trace text, file rotation or the contents of application log files.
