---
layout: page
title: Developer Guide
---
* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

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

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<img src="images/ArchitectureSequenceDiagram.png" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<img src="images/ComponentManagers.png" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

![Structure of the UI Component](images/UiClassDiagram.png)

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<img src="images/LogicClassDiagram.png" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

![Interactions Inside the Logic Component for the `delete 1` Command](images/DeleteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<img src="images/ParserClasses.png" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<img src="images/ModelClassDiagram.png" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<img src="images/BetterModelClassDiagram.png" width="450" />

</div>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<img src="images/StorageClassDiagram.png" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Homework management

Homework is managed with three commands: `homework add`, `homework list` and `homework delete`. `hw` is an alias for `homework`.

**Parsing.** `AddressBookParser` passes the arguments of both `homework` and `hw` to `HomeworkCommandParser`. This dispatcher reads the first word as the subcommand and hands the rest to `HomeworkAddCommandParser` (`add`), `HomeworkListCommandParser` (`list` or `ls`) or `HomeworkDeleteCommandParser` (`delete` or `del`). A missing or unknown subcommand produces an "Invalid command format." error that lists the usage of all three subcommands. Usage messages show alternatives (aliases and the short date form) separated by `|`, e.g. `homework|hw list|ls STUDENT_INDEX`. This keeps one `AddressBookParser` case per command word, and future subcommands (edit, status, score; their classes exist as placeholders) only need a new case in the dispatcher.

The subcommand parsers use `StrictArgumentTokenizer`, which rejects unknown and repeated prefixes, and `ParserUtil#parseStrictIndex`, which rejects signs and leading zeroes. They use `StrictArgumentTokenizer` with plain string prefixes instead of AB3's `ArgumentTokenizer` with `CliSyntax` prefixes because the specification requires unknown prefixes to be rejected, which `ArgumentTokenizer` cannot do: it only splits on prefixes it is given, so an unknown prefix silently becomes part of the previous value. `StudentAddParser` shares the same tokenizer for the same reason. `HomeworkAddCommandParser` reports only the first error it finds, in this order:

1. An unknown or repeated prefix, in the order the prefixes appear.
1. Both `title/` and `t/` given, reported as a repeated `title/`. This is checked only after all prefixes are tokenized, so an unknown or repeated prefix anywhere in the input is reported first. For example, `t/A title/B x/C` reports an invalid command format because of `x/`.
1. A student index that is missing or is not a single word.
1. Missing prefixes, all listed in the order `title/, s/, due/`.
1. Student index syntax.
1. The title, then the subject, then the due date.

**Model.** `Student` is immutable and holds its `Homework` records in insertion order. To add or delete homework, a command copies the student's homework list, changes the copy, and calls `Model#setStudent(target, target.withHomeworks(updatedList))`, which replaces the student at the same position in the roster. The change to the observable student list refreshes the student card, which shows `Student#getAssignedHomeworkCount()`. `homework list` is read-only and returns the list as text in the result display, one line per record in the format of `HomeworkListCommand#MESSAGE_HOMEWORK_LINE` (e.g. `1. Complete algebra worksheet (MATH) - due 2026-10-15`).

The student index refers to `Model#getStudentList()`. The homework index is the one-based position in that student's homework list; it is not stored and changes when earlier homework is deleted. Two homework records of the same student are duplicates when `Homework#isSameHomework` holds: same title ignoring case, same subject and same due date.

**Due date forms.** A due date is given as `YYYY-MM-DD` or `MM-DD`, and in both forms the month and day may have 1 or 2 digits (`2026-2-5`, `10-5`). `DueDate` matches each form with a regular expression that allows only 4-digit years and 1- or 2-digit months and days, then builds the date with `LocalDate#of`, which rejects dates that do not exist (such as `2-30`). The stored date is a `LocalDate`, so it is always shown padded (`2026-02-05`), and `10-5` and `10-05` give equal due dates.

**Year inference.** For a due date given as `MM-DD`, `DueDate#parse(rawDate, today)` picks this year, or next year if the date has passed. If the date does not exist this year (29 February in a non-leap year), next year is used; if it does not exist next year either, the date is invalid. A next year after 9999 is also invalid. `HomeworkAddCommandParser` holds a `java.time.Clock` (`Clock.systemDefaultZone()` in production) and computes `today` as `LocalDate.now(clock)`, so tests pass a fixed clock and never depend on the real date. The parser tells `HomeworkAddCommand` whether the year was inferred, so the success message can show it.

**Unexpected errors.** Each homework command catches any unexpected `RuntimeException` in `execute`, logs it with its stack trace, and throws a `CommandException` with a short internal-error message instead, so the user never sees a stack trace. `Model#setStudent` is the last step of `homework add` and `homework delete`, so such an error leaves the student's homework unchanged.

**Persistence.** Homework, like the student roster, is kept in memory only: `StudentRoster` lives in `ModelManager` and is not written by `Storage`. `LogicManager` therefore does not save the address book after `add` or a homework command, so a failed save cannot report an error for a change that was already made. Because nothing is saved, `MainApp#initModelManager` adds `SampleDataUtil#getSampleStudents()` (students with sample homework) to the model at every launch. This happens outside the `ModelManager` constructor so that tests start with an empty roster. Once students are saved, the sample students should be used only when no data file exists, as with the sample persons.

#### Design considerations:

**Aspect: Where homework is stored:**

* **Alternative 1 (current choice):** Each `Student` holds its own homework list.
  * Pros: A student and their homework stay together, and a student index plus a homework index identify one record without separate homework IDs.
  * Cons: Every change to homework builds a new `Student`.

* **Alternative 2:** One homework list in the model, with each record referring to its student.
  * Pros: Easy to show all homework across students.
  * Cons: Homework must be kept consistent when a student is edited or deleted, and per-student indices must be computed.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` — Saves the current address book state in its history.
* `VersionedAddressBook#undo()` — Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` — Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

![UndoRedoState0](images/UndoRedoState0.png)

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

![UndoRedoState1](images/UndoRedoState1.png)

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

![UndoRedoState2](images/UndoRedoState2.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.

</div>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

![UndoRedoState3](images/UndoRedoState3.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.

</div>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Logic.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.

</div>

Similarly, how an undo operation goes through the `Model` component is shown below:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Model.png)

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.

</div>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

![UndoRedoState4](images/UndoRedoState4.png)

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …​` command. This is the behavior that most modern desktop applications follow.

![UndoRedoState5](images/UndoRedoState5.png)

The following activity diagram summarizes what happens when a user executes a new command:

<img src="images/CommitActivityDiagram.png" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


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
| US26 | `*` | independent tutor | view and filter homework by student, due date, subject, or completion status | find homework that requires my attention |
| US27 | `*` | independent tutor | edit recorded homework | correct or update an assignment's details |
| US28 | `*` | independent tutor | delete recorded homework | remove assignments entered incorrectly or no longer required |
| US29 | `*` | independent tutor | update a homework assignment's completion status | track which assignments remain unfinished |
| US30 | `*` | independent tutor | record a student's homework score | keep track of the student's performance |

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
* **Lesson clash**: Two regular lessons on the same weekday whose time intervals overlap.
* **Regular lesson**: A tuition lesson that repeats weekly for a student at a specified weekday and time.
* **Student profile**: The record containing a student's identity, contact information, academic information, guardian information, and associated regular lessons.
* **Student roster**: The collection of student profiles managed by TutorFlow.
* **Tuition subject**: A subject that the tutor teaches to a student, such as Mathematics, Physics, or Chemistry.

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
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases …​ }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases …​ }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases …​ }_
