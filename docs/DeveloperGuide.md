---
layout: page
title: Developer Guide
---
* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* TutorFlow adapts the architecture, shared utilities, and documentation of the [AddressBook-Level3 project](https://github.com/se-edu/addressbook-level3), created by the [SE-EDU initiative](https://se-education.org).

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

<div markdown="span" class="alert alert-primary">

:bulb: **Tip:** The `.puml` files used to create diagrams are in `docs/diagrams`. Refer to the [_PlantUML Tutorial_ at se-edu/guides](https://se-education.org/guides/tutorials/plantUml.html) to learn how to create and edit diagrams.
</div>

### Architecture

<img src="images/ArchitectureDiagram.png" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103-F12-1/tp/blob/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103-F12-1/tp/blob/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `hw del 1 2`.

<img src="images/ArchitectureSequenceDiagram.png" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<img src="images/ComponentManagers.png" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103-F12-1/tp/blob/master/src/main/java/seedu/address/ui/Ui.java)

![Structure of the UI Component](images/UiClassDiagram.png)

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `StudentListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `HelpWindow` lists the commands from `HelpContent`, a plain Java class that groups the commands into sections. Each command format in `HelpContent` is the usage constant of the command (e.g. `HomeworkAddCommand.MESSAGE_USAGE`), so the help window shows the same format as the parser error messages, and `HelpContentTest` checks that every example in it parses into the listed command.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103-F12-1/tp/blob/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103-F12-1/tp/blob/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Student` objects from the model.

`MainWindow` shows one `StudentListPanel`, which lists every student as a `StudentCard`, and a `StatusBarFooter` that shows the path of the data file.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103-F12-1/tp/blob/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<img src="images/LogicClassDiagram.png" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("hw del 1 2")` API call as an example.

![Interactions Inside the Logic Component for the `hw del 1 2` Command](images/HomeworkDeleteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifelines for `HomeworkCommandParser` and `HomeworkDeleteCommandParser` should end at the destroy markers (X), but due to a limitation of PlantUML, they continue to the end of the diagram.
</div>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `HomeworkCommandParser`, which passes the rest of the command to `HomeworkDeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `HomeworkDeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to add homework to a student).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. If the command changed the data, `LogicManager` saves the data through the `Storage` component. Commands that leave the data unchanged, such as `homework list` and `help`, do not write the data file. If saving fails, `LogicManager` undoes the change (see [Saving after a command](#saving-after-a-command)).
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<img src="images/ParserClasses.png" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `HomeworkAddCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103-F12-1/tp/blob/master/src/main/java/seedu/address/model/Model.java)

<img src="images/ModelClassDiagram.png" width="450" />

This partial diagram shows the student and homework classes. Students also hold regular lessons, as described below.

The `Model` component,

* stores all app data in an `AddressBook` object: the student roster (a `StudentRoster` of `Student` objects in insertion order), in which each immutable `Student` holds its own `Homework` records and `RegularLesson` entries.
* uses `LessonSchedule#from` to provide a view of all regular lessons ordered by weekday, start time, and student name without changing roster order.
* exposes the students as an unmodifiable `ObservableList<Student>` that the UI can observe and bind to, so the UI updates when the list changes. `ModelManager` passes every student operation (`hasStudent`, `addStudent`, `setStudent`, `deleteStudent`, `getStudentList`) to its `AddressBook`.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

The classes keep AB3's names (`AddressBook`, `ReadOnlyAddressBook`, `JsonAddressBookStorage`) to keep the change from AB3 small, but they now hold TutorFlow's students, homework, and regular lessons.

### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103-F12-1/tp/blob/master/src/main/java/seedu/address/storage/Storage.java)

<img src="images/StorageClassDiagram.png" width="550" />

This partial diagram shows the student and homework adapters. Regular lessons use `JsonAdaptedLesson`.

The `Storage` component,
* can save both the app data (students, their homework, and regular lessons) and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

The app data is saved in `data/tutorflow.json`. `JsonSerializableAddressBook` holds a `students` list of `JsonAdaptedStudent`. Each student adapter holds a `homeworks` list of `JsonAdaptedHomework` and a `lessons` list of `JsonAdaptedLesson`. Jackson reads and writes the fields of these classes directly:

* Student fields: `name`, `academicLevel`, `subjects`, `phone`, `guardianName`, `guardianPhone`, `homeworks`, and `lessons`.
* Homework fields: `title`, `subject`, `dueDate`, and `score`.
* Lesson fields: `subject`, `dayOfWeek`, `startTime`, and `endTime`.

Contact and academic fields, homework titles and dates, and lesson fields are strings. `subjects` is a list of strings, `homeworks` and `lessons` are lists of objects, and `score` is a `MARKS` or `MARKS_OBTAINED/TOTAL_MARKS` string such as `"17"` or `"5/10"`, or `null`. Lessons store weekdays by name and times in `HH:mm` format.

Each adapted class checks its values in `toModelType()` with the same value classes that commands use. Student and homework adapters report missing fields with `MISSING_FIELD_MESSAGE_FORMAT` and invalid values with the value class's `MESSAGE_CONSTRAINTS`. `JsonAdaptedLesson` reports missing lesson fields together and delegates value validation to `TuitionSubject` and `RegularLesson`. Invalid data produces an `IllegalValueException`. Loading also rejects:

* a missing `students` list, or an empty (`null`) student, homework, or lesson entry.
* two students that are the same by `Student#isSameStudent`.
* homework for a subject the student does not take, and two homework records of one student that are the same by `Homework#isSameHomework`.
* a lesson for a subject the student does not take, duplicate lessons within a student, or overlapping lessons within or across students.
* a lesson with a missing or invalid subject, weekday, start time, or end time.
* a due date that is not a full `YYYY-MM-DD` date. `JsonAdaptedHomework` uses `DueDate#parseFullDate`, so the year of a saved date is never inferred from today's date. Dates are saved padded (`2026-02-05`).
* a text score that `Score#parse` rejects (e.g. `"5.5/10"`, `"11/10"`, `"5/0"`, `"017"`), with `Score#MESSAGE_CONSTRAINTS`, or `Score#MESSAGE_TOO_LARGE` for a number beyond the `int` range, and a score that is neither text nor a number (e.g. `true`), with `Score#MESSAGE_CONSTRAINTS`.

A missing `homeworks` list, `lessons` list, or `score` means no homework, no lessons, or no score. This allows older student data without a `lessons` field to load. `JsonAdaptedHomework` keeps `score` as a Jackson `JsonNode`, so it can tell a JSON number from text. A JSON number such as `85` is the format saved before scores were text; `JsonAdaptedHomework` passes its digits (`JsonNode#asText`) to `Score#parse`, so `85` and `"85"` both load as the bare score `85`, which is saved back as `"85"`. A JSON number that `Score#parse` rejects (e.g. `85.5`, `-3`, `3000000000`) makes the homework load with no score, and a warning is logged (`Ignored invalid homework score 85.5 for 'Quadratic equations worksheet'`). Rejecting it would make the whole file invalid and start the app with no students, losing all other data at the next save. Subjects, levels, and weekdays are accepted in any case and saved in upper case.

`MainApp#readInitialData` decides what the app starts with: the saved data if the data file is valid, the sample students from `SampleDataUtil#getSampleAddressBook()` if the data file does not exist (the file is not created at startup; the sample students are first saved by the first command that changes the data), and no students if the data file cannot be read or is invalid (a warning is logged). In the last case the invalid file stays on disk until the next command that changes the data overwrites it.

`FileUtil#writeToFile` saves a file atomically: it writes the content to a new temporary file with a unique name next to it (such as `tutorflow.json123456789.tmp`, created by `Files#createTempFile`) and then moves that file over the data file (an atomic move where the file system supports it). If writing fails, only that temporary file is deleted and the data file keeps its previous content, so it is never left partially written. Other files in the folder, even one named `tutorflow.json.tmp`, are never changed. Before the data is written to it, the temporary file gets the data file's POSIX file permissions or, on Windows, its access control list, so saving never changes who can read the data file. A read-only data file is reported as an `AccessDeniedException` before anything is written, because a move could otherwise replace it.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Listing and deleting students

`AddressBookParser` routes `list` and its alias `ls` to `ListCommandParser`, which rejects non-blank arguments. `ListCommand` reads `Model#getStudentList()` without changing it and asks the UI to show the roster from the top. It reports `No students found.` for an empty roster, `Listed 1 student.` for one student, and `Listed N students.` otherwise. Because the model is unchanged, `LogicManager` does not save the data file.

`AddressBookParser` routes `delete INDEX` to `DeleteCommandParser`. It uses `ParserUtil#parseStrictIndex`, so zero, signs, leading zeroes, extra arguments, and numbers beyond the supported integer range are rejected before execution. `DeleteCommand` checks the one-based index against the current roster, then calls `Model#deleteStudent` for that student. The remaining students retain their order and receive new displayed indexes. Homework and regular lessons belong to the deleted `Student`, so they are removed with it. `LogicManager` saves the changed roster; if saving fails, it restores the previous roster, including that student's homework and lessons.

### Homework management

Homework is managed with six commands: `homework add`, `homework list`, `homework upcoming`, `homework edit`, `homework marks` and `homework delete`. `hw` is an alias for `homework`.

**Parsing.** `AddressBookParser` passes the arguments of both `homework` and `hw` to `HomeworkCommandParser`. This dispatcher reads the first word as the subcommand and hands the rest to `HomeworkAddCommandParser` (`add`), `HomeworkListCommandParser` (`list` or `ls`), `HomeworkUpcomingCommandParser` (`upcoming` or `up`), `HomeworkEditCommandParser` (`edit` or `e`), `HomeworkMarksCommandParser` (`marks` or `m`) or `HomeworkDeleteCommandParser` (`delete` or `del`). A missing or unknown subcommand produces an "Invalid command format." error that lists the usage of all six subcommands. Usage messages show alternatives (aliases and the short date form) separated by `|` and optional parts in `[ ]`, e.g. `homework|hw list|ls STUDENT_INDEX [all]`. This keeps one `AddressBookParser` case per command word, and a new subcommand only needs a new case in the dispatcher.

The subcommand parsers use `StrictArgumentTokenizer`, which rejects unknown and repeated prefixes, and `ParserUtil#parseStrictIndex`, which rejects signs and leading zeroes. They use `StrictArgumentTokenizer` with plain string prefixes instead of AB3's `ArgumentTokenizer` with `CliSyntax` prefixes (both since removed) because the specification requires unknown prefixes to be rejected, which `ArgumentTokenizer` cannot do: it only splits on prefixes it is given, so an unknown prefix silently becomes part of the previous value. `StudentAddParser` shares the same tokenizer for the same reason. `HomeworkAddCommandParser` reports only the first error it finds, in this order:

1. An unknown or repeated prefix, in the order the prefixes appear.
1. Both `title/` and `t/` given, reported as a repeated `title/`. This is checked only after all prefixes are tokenized, so an unknown or repeated prefix anywhere in the input is reported first. For example, `t/A title/B x/C` reports an invalid command format because of `x/`.
1. A student index that is missing or is not a single word.
1. Missing prefixes, all listed in the order `title/, s/, due/`.
1. Student index syntax.
1. The title, then the subject, then the due date.

**Model.** `Student` is immutable and holds its `Homework` records in insertion order. A `Homework` has a title, subject and due date, and an optional `Score`. To add, edit, score or delete homework, a command copies the student's homework list, changes the copy (`add`, `set` or `remove`), and calls `Model#setStudent(target, target.withHomeworks(updatedList))`, which replaces the student at the same position in the roster. The change to the observable student list refreshes the student card, which shows `Student#getUpcomingHomeworkCount(today)`, the number of homework records for which `Homework#isUpcoming(today)` holds (due today or later). `StudentListPanel` passes `LocalDate.now()` as `today` when it creates a card.

The student index refers to `Model#getStudentList()`. The homework index is the one-based position in that student's homework list (insertion order); it is not stored and changes only when earlier homework is deleted. Editing or scoring homework replaces it at the same position, so its index stays the same. Two homework records of the same student are duplicates when `Homework#isSameHomework` holds: same title ignoring case, same subject and same due date. The score is not part of this identity.

**Score.** `Score` holds the marks obtained as an `int` and the total marks as an `Integer` that is `null` for a bare score such as `17`, which has no total. It keeps the score as given: `5/10` is not simplified, and `toString` gives back `17` or `5/10`. `Score#equals` compares the marks and the total, so `17` and `17/20` are different scores. `Score#parse` matches the whole input against `(0|[1-9][0-9]*)(?:/(0|[1-9][0-9]*))?`, so it accepts `MARKS` or `MARKS_OBTAINED/TOTAL_MARKS` and rejects signs, decimals, spaces and leading zeroes with `Score#MESSAGE_CONSTRAINTS`. A number that matches but does not fit in an `int` is reported with `Score#MESSAGE_TOO_LARGE`, and the constructors then reject negative marks, a total of 0 and marks above the total. The data file stores a score as this text, e.g. `"17"` or `"5/10"`. `Messages#formatScore` shows a missing score as `Not recorded`.

**Listing.** `homework list` is read-only and returns the list as text in the result display. `HomeworkListCommandParser` accepts a student index optionally followed by the word `all`, and passes `LocalDate.now(clock)` and whether `all` was given to `HomeworkListCommand`. `HomeworkListCommand#formatHomeworkList(student, today, isAllShown)` keeps the zero-based positions of the homework to show (all of them, or only the upcoming ones), sorts the positions by due date with the stable `Stream#sorted`, so homework due on the same date stays in insertion order, and prints each record in the format of `HomeworkListCommand#MESSAGE_HOMEWORK_LINE` with its insertion-order number, e.g. `2. Kinematics practice set (PHYSICS) - due 2026-10-28 - score 5/10`. Printing the insertion-order number, not the position in the sorted list, keeps the numbers valid as `HOMEWORK_INDEX` for the other homework commands.

**Upcoming homework across students.** `homework upcoming` is also read-only and returns text in the result display. `HomeworkUpcomingCommandParser` accepts no preamble and at most one `due/` prefix, whose value here is a number of days, not a date. It checks the value against `0|[1-9][0-9]{0,2}` and then against the maximum of 365 (at most three digits, so the value always fits in an `int`), uses 7 if `due/` is left out, and passes `LocalDate.now(clock)` and the number of days to `HomeworkUpcomingCommand`. The command searches `model.getAddressBook().getStudentList()`, the whole roster, rather than relying on the displayed list, so a future filtered student list would not hide any homework. It pairs each homework with its student in roster order and insertion order, keeps the pairs for which `Homework#isUpcoming(today)` holds and the due date is not after `today.plusDays(days)`, and sorts them by due date with the stable `Stream#sorted`, so homework due on the same date stays in roster order, then insertion order. Each pair is printed in the format of `HomeworkUpcomingCommand#MESSAGE_HOMEWORK_LINE`, e.g. `Ethan Lim; Quadratic equations worksheet; MATH; Due: 2026-10-23; Score: 5/10`. The lines are not numbered, because a position in this list is not a `HOMEWORK_INDEX`.

**Editing.** `HomeworkEditCommandParser` uses the prefixes of `homework add`, all optional, and reports errors in this order: an unknown or repeated prefix, both `title/` and `t/` given (reported as a repeated `title/`), indices that are not exactly two words or no field to edit (both reported as an invalid command format), the student index syntax, the homework index syntax, and then the title, subject and due date. It stores the new values in a `HomeworkEditCommand.EditHomeworkDescriptor`, a record in which `null` means "unchanged". `HomeworkEditCommand` checks, in order, the student index, that the student has homework, the homework index, that the student takes the (possibly new) subject, that the edit changes something (`Homework#equals`), and that the edited homework is not the same as any *other* homework of the student. `EditHomeworkDescriptor#applyTo` copies the score, so editing never changes it. On success, the command shows the edited record followed by `HomeworkListCommand#formatHomeworkList(editedStudent, today, false)`, so the tutor sees the upcoming list as `homework list` would show it after the edit.

The sequence diagram below shows how `hw e 1 2 t/Algebra` is executed. `h` is the 2nd homework of the 1st student `s`, and `s2` is `s.withHomeworks(updated)`, a copy of `s` whose homework list holds `edited` in place of `h`. The checks that can reject the edit are left out.

![Interactions inside the Logic component for the `hw e 1 2 t/Algebra` command](images/HomeworkEditSequenceDiagram.png)

**Recording marks.** `HomeworkMarksCommandParser` takes two indices and the required `score/` prefix; a missing `score/` is reported with the same "Missing required prefix(es)" message as in `homework add`. If the value of `score/` is empty or only whitespace, the parser skips `Score#parse` and creates a command that clears the marks with `new HomeworkMarksCommand(studentIndex, homeworkIndex)`, which holds no score (`null`). `HomeworkMarksCommand` checks the student index, that the student has homework, the homework index, and that the new score differs from the recorded one (`Score#equals`, so replacing `17/20` with `17` is a change), then replaces the homework with a copy holding the new score. When clearing, the homework must have a score (otherwise `MESSAGE_NO_MARKS_TO_CLEAR`), the copy holds no score, and the result starts with `Cleared homework marks:` instead of `Recorded homework marks:`. Clearing reuses the index, save-failure and internal-error handling of recording, so the two cases differ only in the score passed on.

The sequence diagram below shows how `hw m 1 2 score/5/10` is executed. The parser turns `5/10` into a `Score` with `Score#parse` before the command is created, so an invalid score never reaches the command. The checks that can reject the command are left out.

![Interactions inside the Logic component for the `hw m 1 2 score/5/10` command](images/HomeworkMarksSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** As in the `hw del 1 2` diagram, the lifelines of the parsers should end at the destroy markers (X), but due to a limitation of PlantUML, they continue to the end of the diagram.
</div>

**Due date forms.** A due date is given as `YYYY-MM-DD` or `MM-DD`, and in both forms the month and day may have 1 or 2 digits (`2026-2-5`, `10-5`). `DueDate` matches each form with a regular expression that allows only 4-digit years and 1- or 2-digit months and days, then builds the date with `LocalDate#of`, which rejects dates that do not exist (such as `2-30`). The stored date is a `LocalDate`, so it is always shown padded (`2026-02-05`), and `10-5` and `10-05` give equal due dates.

**Year inference.** For a due date given as `MM-DD`, `DueDate#parse(rawDate, today)` picks this year, or next year if the date has passed. If the date does not exist this year (29 February in a non-leap year), next year is used; if it does not exist next year either, the date is invalid. A next year after 9999 is also invalid. `HomeworkAddCommandParser` and `HomeworkEditCommandParser` hold a `java.time.Clock` (`Clock.systemDefaultZone()` in production, passed down from `AddressBookParser` through `HomeworkCommandParser`) and compute `today` as `LocalDate.now(clock)`, so tests pass a fixed clock and never depend on the real date. `HomeworkListCommandParser` and `HomeworkUpcomingCommandParser` use the same clock to decide which homework is upcoming. Because the parsers run for every command entered, today is recomputed each time. The parsers tell `HomeworkAddCommand` and `HomeworkEditCommand` whether the year was inferred, so the success message can show it.

**Unexpected errors.** Each homework command catches any unexpected `RuntimeException` in `execute`, logs it with its stack trace, and throws a `CommandException` with a short internal-error message instead, so the user never sees a stack trace. `Model#setStudent` is the last step of `homework add`, `homework edit`, `homework marks` and `homework delete`, so such an error leaves the student's homework unchanged.

**Persistence.** Homework is saved as part of each student (see [Storage component](#storage-component)). `homework add`, `homework edit`, `homework marks` and `homework delete` change the data, so `LogicManager` saves it after them; `homework list` and `homework upcoming` change nothing and never write the data file. If saving fails, the change is undone and the error message is the one from the specification, e.g. `HomeworkAddCommand#MESSAGE_SAVE_FAILURE` ("The homework could not be added because TutorFlow could not save the updated data. No homework data was changed."), as described in [Saving after a command](#saving-after-a-command).

#### Design considerations:

**Aspect: Where homework is stored:**

* **Alternative 1 (current choice):** Each `Student` holds its own homework list.
  * Pros: A student and their homework stay together, and a student index plus a homework index identify one record without separate homework IDs.
  * Cons: Every change to homework builds a new `Student`, and listing homework across students, as `homework upcoming` does, has to go through every student's list.

* **Alternative 2:** One homework list in the model, with each record referring to its student.
  * Pros: Easy to show all homework across students.
  * Cons: Homework must be kept consistent when a student is edited or deleted, and per-student indices must be computed.

### Adding regular lessons

`lesson add INDEX s/SUBJECT d/DAY st/START_TIME et/END_TIME` is routed by `LessonCommandParser` to `LessonAddCommandParser`. The parser reuses `StrictArgumentTokenizer` and strict student-index parsing. `RegularLesson` stores a subject, `DayOfWeek`, and two minute-precision `LocalTime` values, with end strictly after start.

`LessonAddCommand` checks the selected student's subjects and duplicate lessons before checking overlaps across the entire roster. Overlap uses `start < other.end && end > other.start` on the same weekday, allowing adjacent lessons. The command replaces the selected immutable `Student` using `withLessons`, preserving homework and roster order. Homework updates using `withHomeworks` likewise preserve lessons.

`JsonAdaptedLesson` stores the four lesson fields inside each student's `lessons` array. A missing array is read as empty so existing data files remain compatible. Loading rejects invalid lesson fields, subject mismatches, duplicates and overlapping schedules. The existing `LogicManager` change detection and save rollback apply to lessons because `Student.equals` includes its lessons. The command supplies the lesson-specific save-failure message.

`LessonListCommand` and `LessonDeleteCommand` use `LessonSchedule` to flatten lessons into the same weekday, start-time and student-name order. Therefore, the one-based index accepted by `lesson delete` always identifies the corresponding row from `lesson list`. Deletion replaces only that lesson's owner through `Student.withLessons()`, without changing the storage format.

Manual check: add a MATH student, run `lesson add 1 s/MATH d/MONDAY st/16:00 et/18:00`, and restart. Inspect `data/tutorflow.json` to confirm that the lesson remains. Try the same lesson again, an overlapping slot, and an adjacent slot; the first two should fail and the last should succeed. With the data file read-only, a new lesson should fail without changing either the file or the in-memory lessons.

### Saving after a command

`LogicManager#execute` saves the data after each command that changes it, and undoes the change if the data cannot be saved, so the data shown in the app always matches the data file.

1. Before executing the command, `LogicManager` copies the current data: `new AddressBook(model.getAddressBook())`. The copy is cheap because `Student` and `Homework` are immutable, so only the list is copied.
1. After the command succeeds, `LogicManager` compares the data with the copy. If they are equal, the command changed nothing (e.g. `homework list`, `homework upcoming`, `help`, `exit`, or `clear` with no students), and nothing is saved.
1. Otherwise, `LogicManager` calls `Storage#saveAddressBook`. If this throws an `IOException`, `LogicManager` restores the copy with `Model#setAddressBook` and throws a `CommandException` whose message comes from `Command#getSaveFailureMessage(defaultMessage)`.
1. By default, `getSaveFailureMessage` returns the AB3 message (`LogicManager#FILE_OPS_ERROR_FORMAT`, or `FILE_OPS_PERMISSION_ERROR_FORMAT` when access is denied). The homework commands that change data (`HomeworkAddCommand`, `HomeworkEditCommand`, `HomeworkMarksCommand`, `HomeworkDeleteCommand`), `LessonAddCommand` and `LessonDeleteCommand` override it to return their own messages (the homework messages are from the specification). A new command that needs its own message only overrides this method.

#### Design considerations:

**Aspect: When to save:**

* **Alternative 1 (current choice):** Save only when the command changed the data.
  * Pros: Read-only commands such as `homework list` never write the data file, as the specification requires, and cannot fail because of a save error.
  * Cons: Each command copies and compares the data. This is fast for the number of students one tutor has.

* **Alternative 2 (AB3):** Save after every successful command.
  * Pros: Simpler.
  * Cons: A read-only command can fail with a save error, and it rewrites the data file even though nothing changed.

**Aspect: How to handle a failed save:**

* **Alternative 1 (current choice):** Undo the change by restoring the copy taken before the command.
  * Pros: Works for every command without command-specific undo code, and the app never shows data that is not in the data file.
  * Cons: The copy is taken even when saving succeeds.

* **Alternative 2 (AB3):** Report the error but keep the change in memory.
  * Pros: No copy is needed.
  * Cons: The message says the data could not be saved, but the change is still shown and is lost when the app closes, so the user cannot tell what is saved.

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* is an independent part-time tutor in Singapore
* teaches approximately 10 to 20 secondary or junior-college students through individual lessons
* needs to manage student details, guardian contact details, academic information, and regular lesson schedules
* prefers a desktop application that can be operated efficiently using the keyboard
* is reasonably comfortable using command-line interfaces

**Value proposition**: TutorFlow helps independent tutors manage student information and regular lesson schedules faster and more reliably than scattered notes, while reducing the risk of timetable clashes.

### User stories

Priorities: High (must-have) - `* * *`, Medium (should-have) - `* *`, Low (nice-to-have) - `*`

| ID | Priority | As a/an | I want to | So that I can |
| --- | --- | --- | --- | --- |
| US01 | `* * *` | independent tutor | add a student profile | keep the student's essential information in one place |
| US02 | `* * *` | independent tutor | list all my students | review my current student roster |
| US03 | `* * *` | independent tutor | view a student's profile | retrieve the student's contact and academic information |
| US04 | `* * *` | independent tutor | delete a student record | remove obsolete or incorrectly created records from my roster |
| US05 | `* * *` | independent tutor | add a regular lesson to a student | record when I teach that student each week |
| US06 | `* * *` | independent tutor | view the regular lessons assigned to a student | check that student's regular lesson schedule |
| US07 | `* * *` | independent tutor | view all regular lessons in weekday-and-time order | understand my overall teaching schedule |
| US08 | `* * *` | independent tutor | delete a regular lesson | remove lessons that no longer take place |
| US09 | `* * *` | independent tutor | be prevented from adding an overlapping regular lesson | avoid double-booking myself |
| US10 | `* * *` | independent tutor | reopen TutorFlow and recover my student and lesson records | avoid entering them again |
| US11 | `* *` | independent tutor | search for a student by name | find the student's record quickly |
| US12 | `* *` | independent tutor | filter students by academic level | focus on students at a particular stage of study |
| US13 | `* *` | independent tutor | filter students by subject | find the students whom I teach a particular subject |
| US14 | `* *` | independent tutor | view regular lessons scheduled on a selected weekday | focus on one day's teaching schedule |
| US15 | `* *` | independent tutor | edit a student's profile | correct changed or incorrectly entered information |
| US16 | `* *` | independent tutor | change a regular lesson's timeslot | keep my schedule accurate when a regular arrangement changes |
| US17 | `*` | independent tutor | archive an inactive student | keep former students from cluttering my current roster |
| US18 | `*` | independent tutor | restore an archived student | resume managing a returning student |
| US19 | `*` | independent tutor | identify students without a regular lesson | notice students whose schedules are incomplete |
| US20 | `*` | independent tutor | export my regular lesson schedule | view or print it outside TutorFlow |
| US21 | `*` | independent tutor | sort students alphabetically | scan my roster more easily |
| US22 | `*` | independent tutor | sort students by academic level | group students at similar stages of study |
| US23 | `*` | independent tutor | search for a student by phone number | identify a student from an unfamiliar contact number |
| US24 | `*` | independent tutor | view available periods in my regular lesson schedule | propose lesson times without checking each lesson manually |
| US25 | `*` | independent tutor | add homework to a student | record the work assigned to that student |
| US26 | `*` | independent tutor | view and filter homework by student, due date, or subject | find homework that requires my attention |
| US27 | `*` | independent tutor | edit recorded homework | correct or update an assignment's details |
| US28 | `*` | independent tutor | delete recorded homework | remove assignments entered incorrectly or no longer required |
| US29 | `*` | independent tutor | record a student's homework score | keep track of the student's performance |

### Use cases

For all use cases below, the **System** is `TutorFlow` and the **Actor** is the `Tutor`, unless specified otherwise.

#### Use case: UC01 - Add a regular lesson

**MSS**

1. Tutor requests to view the student roster.
2. TutorFlow displays the student roster with displayed indices.
3. Tutor requests to add a regular lesson to a displayed student, specifying the subject, weekday, start time, and end time.
4. TutorFlow records the regular lesson and confirms its details.
5. Use case ends.

**Extensions**

* 2a. The student roster is empty.
  * 2a1. TutorFlow informs the tutor that there are no students.
  * 2a2. Use case ends.
* 3a. The supplied student index does not identify a displayed student.
  * 3a1. TutorFlow displays an error.
  * 3a2. Use case resumes at step 3.
* 3b. One or more lesson details are invalid.
  * 3b1. TutorFlow displays the relevant validation error.
  * 3b2. Use case resumes at step 3.
* 3c. The selected student is not registered for the supplied subject.
  * 3c1. TutorFlow rejects the lesson and displays an error.
  * 3c2. Use case resumes at step 3.
* 3d. The regular lesson already exists.
  * 3d1. TutorFlow informs the tutor that the lesson already exists.
  * 3d2. Use case ends.
* 3e. The lesson overlaps another regular lesson on the same weekday.
  * 3e1. TutorFlow rejects the new lesson and identifies the conflicting lesson.
  * 3e2. Use case ends.
* 4a. TutorFlow cannot save the updated data.
  * 4a1. TutorFlow leaves the lesson schedule unchanged.
  * 4a2. TutorFlow informs the tutor that no lesson data was changed.
  * 4a3. Use case ends.

#### Use case: UC02 - Delete a regular lesson

**MSS**

1. Tutor requests to view all regular lessons.
2. TutorFlow displays the regular lessons in weekday-and-time order with displayed indices.
3. Tutor requests to delete a lesson using its displayed index.
4. TutorFlow deletes the selected lesson and confirms its details.
5. Use case ends.

**Extensions**

* 2a. There are no regular lessons.
  * 2a1. TutorFlow informs the tutor that no regular lessons were found.
  * 2a2. Use case ends.
* 3a. The supplied index does not identify a displayed lesson.
  * 3a1. TutorFlow displays an error.
  * 3a2. Use case resumes at step 3.
* 4a. TutorFlow cannot save the updated data.
  * 4a1. TutorFlow leaves the lesson schedule unchanged.
  * 4a2. TutorFlow informs the tutor that no lesson data was changed.
  * 4a3. Use case ends.

#### Use case: UC03 - Edit a homework record

**MSS**

1. Tutor requests to list a student's homework.
2. TutorFlow displays the student's upcoming homework, each with its homework index.
3. Tutor requests to edit one homework record of the student using its homework index, specifying a new title, subject or due date.
4. TutorFlow updates the homework record, keeping its homework index and score, and displays it together with the student's upcoming homework.
5. Use case ends.

**Extensions**

* 2a. The homework to edit is overdue, so it is not displayed.
  * 2a1. Tutor requests to list all of the student's homework.
  * 2a2. TutorFlow displays all of the student's homework, including overdue homework, each with its homework index.
  * 2a3. Use case resumes at step 3.
* 3a. The supplied student index or homework index does not identify a homework record.
  * 3a1. TutorFlow displays an error.
  * 3a2. Use case resumes at step 3.
* 3b. No new detail is supplied, or a new detail is invalid.
  * 3b1. TutorFlow displays the relevant error.
  * 3b2. Use case resumes at step 3.
* 3c. The student does not take the new subject.
  * 3c1. TutorFlow rejects the edit and displays an error.
  * 3c2. Use case resumes at step 3.
* 3d. The new details are identical to the existing details.
  * 3d1. TutorFlow informs the tutor that no changes were made.
  * 3d2. Use case ends.
* 3e. The edited homework would duplicate another homework record of the student.
  * 3e1. TutorFlow rejects the edit and informs the tutor.
  * 3e2. Use case ends.
* 4a. TutorFlow cannot save the updated data.
  * 4a1. TutorFlow leaves the homework unchanged.
  * 4a2. TutorFlow informs the tutor that no homework data was changed.
  * 4a3. Use case ends.

#### Use case: UC04 - Record homework marks

**MSS**

1. Tutor requests to list a student's homework.
2. TutorFlow displays the student's upcoming homework, each with its homework index and score.
3. Tutor requests to record the marks of one homework record of the student using its homework index, specifying the marks obtained, optionally together with the total marks.
4. TutorFlow records the score, replacing any score recorded before, and displays the homework record with its score.
5. Use case ends.

**Extensions**

* 2a. The homework is overdue, so it is not displayed.
  * 2a1. Tutor requests to list all of the student's homework.
  * 2a2. TutorFlow displays all of the student's homework, including overdue homework.
  * 2a3. Use case resumes at step 3.
* 3a. The supplied student index or homework index does not identify a homework record.
  * 3a1. TutorFlow displays an error.
  * 3a2. Use case resumes at step 3.
* 3b. The score is invalid, for example the marks obtained exceed the total marks.
  * 3b1. TutorFlow displays the score format and its rules.
  * 3b2. Use case resumes at step 3.
* 3c. The homework already has this score.
  * 3c1. TutorFlow informs the tutor that no changes were made.
  * 3c2. Use case ends.
* 3d. The tutor leaves the score empty to remove the recorded marks.
  * 3d1. TutorFlow clears the score and displays the homework record with its score shown as not recorded.
  * 3d2. Use case ends.
* 3e. The tutor leaves the score empty, but the homework has no recorded marks.
  * 3e1. TutorFlow informs the tutor that no changes were made.
  * 3e2. Use case ends.
* 4a. TutorFlow cannot save the updated data.
  * 4a1. TutorFlow leaves the score unchanged.
  * 4a2. TutorFlow informs the tutor that no homework data was changed.
  * 4a3. Use case ends.

### Non-Functional Requirements

1. TutorFlow should work on Windows, Linux, and macOS on a computer with Java `25` installed.
2. TutorFlow should be distributed as a single JAR file no larger than 100 MB and should not require an installer or any additional software beyond Java 25.
3. TutorFlow should support one user, with its data file used only by that user during regular operation.
4. TutorFlow should store all application data locally in a human-editable UTF-8 text file and should not require a database management system.
5. TutorFlow's core student and lesson-management features should work without an Internet connection or a team-maintained remote server.
6. TutorFlow's GUI should work without inconvenience at resolutions of 1920x1080 or higher with 100% or 125% display scaling, and remain usable at resolutions of 1280x720 or higher with 150% display scaling.
7. TutorFlow should respond to commands within one second when managing up to 100 student profiles with up to 10 regular lessons per student on a computer with a 2.0 GHz dual-core processor, 4 GB of RAM, and solid-state storage.
8. After reading the User Guide for no more than 15 minutes, a target user should be able to add, list, and delete students and regular lessons without assistance.
9. Adding, listing, and deleting students and regular lessons should be possible using only the keyboard.
10. If a save operation fails, TutorFlow should preserve the previously stored data without leaving a partially written data file.

### Glossary

* **Academic level**: The student's current secondary or junior-college year, such as `S3` or `J1`.
* **Command-line interface (CLI)**: A text-based interface in which the user performs operations by entering commands.
* **Displayed index**: A temporary one-based number assigned to an item in the currently displayed list. It is not a permanent identifier.
* **Guardian**: The person recorded as the primary guardian or contact for a student.
* **Homework**: Work assigned to a student and recorded in TutorFlow for follow-up.
* **Homework index**: The one-based position of a homework record in its student's homework list, in the order the homework was added. `homework list` shows it next to each record, and `homework edit`, `homework marks` and `homework delete` use it to identify the record.
* **Lesson clash**: Two regular lessons on the same weekday whose time intervals overlap.
* **Regular lesson**: A tuition lesson that repeats weekly for a student at a specified weekday and time.
* **Score**: The marks a student obtained for a homework record, written on their own, such as `17`, or together with the total marks as `MARKS_OBTAINED/TOTAL_MARKS`, such as `5/10`.
* **Student profile**: The record containing a student's identity, contact information, academic information, guardian information, and associated regular lessons.
* **Student roster**: The collection of student profiles managed by TutorFlow.
* **Tuition subject**: A subject that the tutor teaches to a student, such as Mathematics, Physics, or Chemistry.
* **Upcoming homework**: Homework that is due today or later.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<div markdown="span" class="alert alert-info">:information_source: **Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</div>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample students with sample homework. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases …​ }_

### Listing and deleting students

1. Listing students

   1. In a fresh test folder, launch the app and run `list` (or `ls`).<br>
      Expected: The student cards are shown from the top in roster order and the result says `Listed 6 students.` for the six sample students. `list 1` should instead show a format error without changing the roster.

   1. In a test copy of the data, leave exactly one student and run `list`. Then delete that student and run `list` again.<br>
      Expected: The first result says `Listed 1 student.` and the second says `No students found.`

1. Deleting a student with related records

   1. In a fresh test folder, add a student taking MATH. Use the index shown on their card to add a homework record and a regular lesson, then run `delete INDEX` using that student's index.<br>
      Expected: Only that student disappears; the remaining students retain their order and are renumbered. Restart the app and run `list`: the deleted student is still absent. Their homework and lesson are absent from `data/tutorflow.json`.

   1. Try `delete 0`, `delete 01`, and an index larger than the roster size.<br>
      Expected: Each shows an error and leaves the roster unchanged.

### Listing, editing and scoring homework

1. Listing upcoming and overdue homework

   1. Prerequisites: Start with the sample students (delete `data/tutorflow.json` first). Run `hw add 5 t/Old worksheet s/MATH due/2026-10-01`, which gives Daniel Wong (student 5) homework that is already overdue.

   1. Test case: `hw ls 5`<br>
      Expected: `No upcoming homework found for Daniel Wong.` Daniel Wong's card shows `Upcoming homework: 0`.

   1. Test case: `hw ls 5 all`<br>
      Expected: `Listed 1 homework record for Daniel Wong.` followed by `1. Old worksheet (MATH) - due 2026-10-01 - score Not recorded`.

   1. Test case: `hw ls 5 ALL`<br>
      Expected: An invalid command format error, as `all` must be in lowercase.

1. Listing upcoming homework of all students

   1. Prerequisites: Start with the sample students. Run `hw add 3 t/Today drill s/MATH due/TODAY`, then `hw add 1 t/Today forces s/PHYSICS due/TODAY` and `hw add 5 t/Old worksheet s/MATH due/2026-10-01`, replacing `TODAY` with today's date in `YYYY-MM-DD` form.

   1. Test case: `hw up due/0`<br>
      Expected: `Found 2 homework records due from TODAY to TODAY.`, then Ethan Lim's `Today forces` line before Marcus Tan's `Today drill` line (roster order, not the order they were added). `Old worksheet` is not shown, as it is overdue.

   1. Test case: `hw up`<br>
      Expected: The summary ends with the date 7 days after today, and the lines are sorted by due date. The data file is not modified.

   1. Test case: `hw up due/365`, then `hw ls 1`<br>
      Expected: Every homework due today or later is shown. The order in `hw up` is not the numbering in `hw ls 1`.

   1. Incorrect commands to try: `hw up due/366`, `hw up due/007`, `hw up due/-1`, `hw up due/1.5`, `hw up due/2026-10-20`, `hw up due/`<br>
      Expected: `Days must be a whole number from 0 to 365 without leading zeroes.`

   1. Other incorrect commands to try: `hw up 3`, `hw up x/3`, `hw up due/3 due/7`<br>
      Expected: An invalid command format error with the usage `homework|hw upcoming|up [due/DAYS]`, or, for the last one, `Prefix due/ must be specified exactly once.`

1. Editing homework

   1. Prerequisites: Start with the sample students.

   1. Test case: `hw e 2 1 due/2026-11-05`<br>
      Expected: The result shows the edited homework, then Priya Nair's upcoming homework in the order 2, 1, 3, as homework 1 is now due after homework 2.

   1. Test case: `hw e 1 1 t/Quadratic equations worksheet`<br>
      Expected: `No changes made; the supplied values are identical to the existing homework details.`

   1. Test case: `hw e 1 2 t/quadratic EQUATIONS worksheet s/MATH due/2026-10-23`<br>
      Expected: `This edit would make the homework a duplicate of an existing homework record.`

   1. Other incorrect edit commands to try: `hw e 1 2` (no field), `hw e 1 1 t/A title/B`, `hw e 6 1 s/PHYSICS` (subject not taken), `hw e 1 1 score/5/10`<br>
      Expected: An error message each time; `hw ls 1` and `hw ls 2` are unchanged.

1. Recording homework marks

   1. Prerequisites: Start with the sample students.

   1. Test case: `hw m 1 2 score/5/10`, then `hw ls 1`<br>
      Expected: The result shows `Score: 5/10`, and the list shows `2. Kinematics practice set (PHYSICS) - due 2026-10-28 - score 5/10`. Restart the app: `hw ls 1` still shows the score.

   1. Test case: `hw m 1 2 score/5/10` again<br>
      Expected: `No changes made; the homework already has this score.`

   1. Test case: `hw m 1 2 score/17`, then `hw ls 1`<br>
      Expected: `Recorded homework marks:` followed by `Ethan Lim; Title: Kinematics practice set; Subject: PHYSICS; Due: 2026-10-28; Score: 17`, and the list shows `2. Kinematics practice set (PHYSICS) - due 2026-10-28 - score 17`. Running `hw m 1 2 score/17` again gives `No changes made; the homework already has this score.`, while `hw m 1 2 score/17/20` records `Score: 17/20`.

   1. Test case: `hw m 1 2 score/`, then `hw ls 1`<br>
      Expected: `Cleared homework marks:` followed by `Ethan Lim; Title: Kinematics practice set; Subject: PHYSICS; Due: 2026-10-28; Score: Not recorded`, and the list shows `2. Kinematics practice set (PHYSICS) - due 2026-10-28 - score Not recorded`. Restart the app: `hw ls 1` still shows `score Not recorded`.

   1. Test case: `hw m 1 2 score/` again, and `hw m 1 1 score/   ` (only spaces)<br>
      Expected: `No changes made; the homework has no recorded marks.` each time.

   1. Other incorrect commands to try: `hw m 1 2 score/11/10`, `hw m 1 2 score/5/0`, `hw m 1 2 score/5.5/10`, `hw m 1 2 score/17.5`, `hw m 1 2 score/017`, `hw m 1 2 score/2147483648/10`, `hw m 1 2 score/2147483648`, `hw m 1 2`<br>
      Expected: An error message each time, and the score is unchanged.

1. Saving fails

   1. Make `data/tutorflow.json` read-only, then run `hw e 1 1 t/Changed title` and `hw m 1 1 score/9/10`.<br>
      Expected: Each shows that the homework could not be edited, or that its marks could not be recorded, and that no homework data was changed. `hw ls 1` is unchanged.

### Deleting homework

1. Deleting homework of a student who has homework

   1. Prerequisites: The 1st student has at least 2 homework records. Check with `homework list 1`.

   1. Test case: `homework delete 1 2`<br>
      Expected: The 2nd homework of the 1st student is deleted. The result display shows the deleted homework. The student card shows the new number of upcoming homework.

   1. Test case: `homework delete 1 0`<br>
      Expected: No homework is deleted. The result display shows an error message.

   1. Other incorrect delete commands to try: `homework delete`, `homework delete 1`, `homework delete 1 x` (where x is larger than the number of homework of the student)<br>
      Expected: Similar to previous.

1. _{ more test cases …​ }_

### Deleting regular lessons

1. Deleting a lesson from the weekly schedule

   1. Prerequisites: At least two regular lessons exist. Check with `lesson list`.

   1. Test case: `lesson delete 2`<br>
      Expected: The second lesson shown by `lesson list` is deleted, its owner retains any other lessons, and the
      result display shows the deleted lesson's details.

   1. Test case: `lesson delete 0`<br>
      Expected: No lesson is deleted. The result display shows an error message.

   1. Other incorrect delete commands to try: `lesson delete`, `lesson delete 1 2`, `lesson delete x` (where x is
      larger than the number of displayed lessons)<br>
      Expected: Similar to previous.

### Saving data

1. Saving students, homework and lessons

   1. Add a student with `add n/Aaron Koh l/S2 s/MATH p/91112222 gn/Koh Mei Hua gp/98887777`. With the sample students, Aaron Koh is student 7, so run `homework add 7 t/Algebra worksheet s/MATH due/2026-12-01` and `lesson add 7 s/MATH d/SUNDAY st/20:00 et/21:00` (use Aaron Koh's index if it is not 7). Close the app and launch it again.<br>
      Expected: The student, the homework and the lesson are still shown: `homework list 7` shows `Listed 1 upcoming homework record for Aaron Koh.` and `1. Algebra worksheet (MATH) - due 2026-12-01 - score Not recorded`, and `lesson list` shows the lesson as `Aaron Koh; MATH; SUN; 20:00-21:00`.

1. Dealing with a missing data file

   1. Close the app and delete `data/tutorflow.json`. Launch the app.<br>
      Expected: The app shows the sample students. A new data file is created after the first command that changes the data, e.g. `clear`.

1. Dealing with a corrupted data file

   1. Close the app. In `data/tutorflow.json`, change the `dueDate` of a homework to `10-15` (no year), or delete the closing `}`. Launch the app.<br>
      Expected: The app shows no students, and a warning is logged. The data file is unchanged until a command changes the data.

1. Dealing with a score saved by an older version

   1. Close the app. In `data/tutorflow.json`, change the `score` of a homework to `85` (a number, without quotes). Launch the app.<br>
      Expected: All students are shown, and `homework list` for that student shows the homework with `score 85`. The `85` stays in the data file until a command changes the data, which saves the homework with `"score" : "85"`.

   1. Change the `score` to `"85"` (text, with quotes) instead. Launch the app.<br>
      Expected: Same as above.

   1. Change the `score` to `85.5` (a number that is not a valid score) instead. Launch the app.<br>
      Expected: All students are shown, and the homework shows `score Not recorded`. A warning such as `Ignored invalid homework score 85.5 for 'Quadratic equations worksheet'` is logged.

   1. Change the `score` to `"85.5"` (text, with quotes) instead. Launch the app.<br>
      Expected: Similar to a corrupted data file: the app shows no students.

1. Dealing with a data file that cannot be written

   1. Make `data/tutorflow.json` read-only, then run `homework add 1 t/Test s/MATH due/2026-12-01` (the 1st student must take MATH).<br>
      Expected: An error message says that the homework could not be added and that no homework data was changed. `homework list 1` does not show the homework.

1. _{ more test cases …​ }_
