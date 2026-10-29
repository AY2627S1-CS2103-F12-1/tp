---
layout: page
title: User Guide
---

TutorFlow is a **desktop application for private tutors to manage their students, homework and regular lessons, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, TutorFlow can help you manage your students faster than traditional GUI applications.

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` is installed on your computer. Run `java -version` in your terminal and check that it reports version `25`.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/AY2627S1-CS2103-F12-1/tp/releases).

1. Copy the file to an empty, writable folder you want to use as the _home folder_ for TutorFlow. The app must be able to create its data and preferences here.

1. Open a terminal, `cd` to that home folder, and run `java -jar "addressbook.jar"` (replace the filename if your download has a different name). Keep the JAR in this folder to use the same saved records. Double-clicking may not work on some systems.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Press **Up** and **Down** in the command box to recall commands entered during the current session. Pressing Up replaces any text currently in the box.<br>
   Some example commands you can try:

   * `add n/Aaron Koh l/S2 s/MATH p/91112222 gn/Koh Mei Hua gp/98887777` : Adds a student named `Aaron Koh` to the student list.

   * `edit 1 n/Updated Student` : Changes the name of the 1st student shown in the list.

   * `search Tan` : Shows students whose name, phone, guardian name or guardian phone contains `Tan`.

   * `filter d/MONDAY` : Shows students with a regular lesson on Monday.

   * `sort by/level o/desc` : Orders the currently displayed students by academic level, from J2 down to S1.

   * `list` : Lists all students.

   * `homework list 1` : Lists the upcoming homework of the 1st student shown in the student list.

   * `hw add 1 t/Algebra worksheet s/MATH due/2026-12-01` : Adds homework to the 1st student.

   * `delete 3` : Deletes the 3rd student shown in the student list.

   * `lesson list` : Lists all regular lessons in weekly chronological order.

   * `clear` : Deletes all students together with their homework and regular lessons.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<div markdown="block" class="alert alert-info">

**:information_source: Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `search KEYWORD`, replace `KEYWORD` with a value such as `Tan`.

* Items in square brackets are optional.<br>
  For example, `[s/SUBJECT]` in `homework edit` may be left out, so both `hw e 1 2 t/Kinematics practice set A s/PHYSICS` and `hw e 1 2 t/Kinematics practice set C` are accepted.

* Items separated by `|` are alternatives; type any one of them.<br>
  For example, `homework|hw list|ls STUDENT_INDEX` can be used as `homework list 1` or as `hw ls 1`.<br>
  In a parameter with a prefix, `|` separates only the prefixes or only the value formats: `title/|t/TITLE` means `title/TITLE` or `t/TITLE`, and `due/YYYY-MM-DD|MM-DD` means `due/YYYY-MM-DD` or `due/MM-DD`.

* Parameters with a prefix can be in any order. Indices such as `STUDENT_INDEX` and `HOMEWORK_INDEX` have no prefix and must come first, right after the command word, or right after the subcommand in `homework` and `lesson` commands, e.g. `hw del 1 2`.<br>
  For example, if the command specifies `n/STUDENT_NAME p/STUDENT_PHONE`, `p/STUDENT_PHONE n/STUDENT_NAME` is also acceptable.

* A prefix counts only when it comes right after a space. Text glued to the end of a word stays in the value, even if it looks like a prefix.<br>
  For example, `hw add 1 t/Xs/MATH s/MATH due/2026-12-01` adds homework titled `Xs/MATH`, as the first `s/` follows `X` without a space.

* A word of lowercase English letters (`a` to `z`) directly followed by `/` after a space (such as `km/h` or `and/or`) is read as a prefix. To use such a word in a value, wrap the whole value in double quotes (`"`). The outer quotes are removed before the value is checked. This works for every value that follows a prefix. A word with a digit or a letter outside `a` to `z`, such as `p1/` or `é/`, is not a prefix and stays in the value without quotes (for capital letters, see below).<br>
  For example, `hw add 1 t/"Speed in km/h" s/PHYSICS due/2026-12-01` adds homework titled `Speed in km/h`, while `hw add 1 t/Speed in km/h s/PHYSICS due/2026-12-01` is rejected because `km/` is not a prefix of `hw add`:

  ```text
  km/ is not a prefix of this command. If km/ is part of the title, put the whole title in straight double quotes, e.g. t/"Pg 5 w/ answers".
  Usage: homework|hw add STUDENT_INDEX title/|t/TITLE s/SUBJECT due/YYYY-MM-DD|MM-DD
  ```

* The error for such a word names it and shows the command's usage on the next line. In `homework add` and `homework edit`, if the word comes right after a title without quotes, with no other prefix in between, the error also says to put the whole title in double quotes, as above. The example in this hint is always one of three fixed titles, not your own title: `t/"True t/f quiz"` for `t/` or `title/`, `t/"Read s/o notes"` for `s/`, and `t/"Pg 5 w/ answers"` for any other word. A word after another value gets no hint: `hw add 1 t/A s/PHYSICS due/2026-12-01 x/y` gives `x/ is not a prefix of this command.` and the usage. Neither does a word in `homework marks` or `homework upcoming`, which have no title, e.g. `hw m 1 2 score/5 t/X` gives `t/ is not a prefix of this command.`, or a word that is not inside any value, such as `score/` in `hw e 1 2 score/5/10`.

* In the other commands, such as `filter` and `lesson add`, the hint is given for a word inside any value, and its example is that value, up to the next prefix of the command, in quotes: `filter l/S4 p/123` gives `p/ is not a prefix of this command. To use it inside a value, wrap the whole value in straight double quotes, e.g. l/"S4 p/123".` The example is left out when the text before the word is only digits, date or time punctuation and spaces: `lesson add 1 s/MATH d/MONDAY st/16:00 et/18:00 x/y` gives `x/ is not a prefix of this command. To use it inside a value, wrap the whole value in straight double quotes.`

* Only straight double quotes (`"`) can wrap a value. Curly quotes (`“ ”`), which some editors type automatically, are read as part of the value, so `hw add 1 t/“Speed in km/h” s/PHYSICS due/2026-12-01` is rejected, with `Use straight quotes ("), not curly quotes (“ ”).` on its own line before the usage.

* Spaces at both ends of a quoted value are removed, both outside and inside the quotes, as they are for a value without quotes. Quotes protect prefix-shaped text; they do not override a field's validation or whitespace rules. Student names, guardian names and homework titles still collapse repeated whitespace between words to one space, so `gn/"Parent  Alpha"` is saved as `Parent Alpha`.<br>
  For example, `n/" Ravi s/o Kumar "` gives the name `Ravi s/o Kumar`, and `hw up due/" 3 "` is the same as `hw up due/3`. Empty quotes (`""`), or quotes with only spaces inside, give an empty value, which is treated like a prefix with nothing after it: `t/""` is rejected like `t/`, with `The homework title is empty. Enter 1 to 100 characters.`

* A quoted value ends at the first `"` that is followed by the end of the command, or by a space and another prefix of the same command. Any other `"` is kept as typed.<br>
  For example, `t/Read "Hamlet" act 1` gives the title `Read "Hamlet" act 1`, and `t/"Read "Hamlet" km/h"` gives `Read "Hamlet" km/h`.

* A value whose opening `"` is never closed in this way is read as typed, as if it had no quotes.<br>
  For example, `t/"Speed" extra` gives the title `"Speed" extra`, and `hw add 1 t/"Speed in km/h s/PHYSICS due/2026-12-01` is rejected because the quote is not closed, so `km/` is read as a prefix.

* In `homework add`, `homework edit`, `homework marks` and `homework upcoming`, a prefix of the command typed with capital letters, such as `S/` or `Due/`, is rejected instead of being kept in the value before it. For example, `hw add 1 t/X S/MATH due/2026-12-01` gives `S/ is not a prefix of this command; prefixes are lowercase (s/). If S/ is part of the title, put the whole title in straight double quotes, e.g. t/"Pg 5 w/ answers".`, and `hw add 1 t/Essay s/MATH Due/2026-12-01`, where `Due/` comes after the subject, gives `Due/ is not a prefix of this command; prefixes are lowercase (due/).` Other commands keep such a word in the value, so a name such as `Ravi S/O Kumar` is accepted.

* In `homework add`, `homework edit`, `homework marks` and `homework upcoming`, each prefix can be given only once. A repeated prefix is rejected with `Prefix t/ can be given only once.` (naming the repeated prefix) and the command's usage on the next line. This often happens when a title contains a word such as `t/f` or `s/o`. If either copy of the prefix comes right after a title without quotes, the error also says to quote the title, with a fixed example as described above. A copy of `s/` or `due/` whose first word (up to a space or comma) is a valid subject or due date, such as `s/MATH` or `due/2026-12-01`, is not taken as part of the title, so it gets no such hint. For example, `hw add 1 t/Revise t/f questions s/MATH due/2026-12-01` gives:

  ```text
  Prefix t/ can be given only once. If t/ is part of the title, put the whole title in straight double quotes, e.g. t/"True t/f quiz".
  Usage: homework|hw add STUDENT_INDEX title/|t/TITLE s/SUBJECT due/YYYY-MM-DD|MM-DD
  ```

  `hw add 1 t/Read s/o notes s/MATH due/2026-12-01` gives `Prefix s/ can be given only once. If s/ is part of the title, put the whole title in straight double quotes, e.g. t/"Read s/o notes".`, as `o` is not a subject. `hw add 1 t/A s/MATH due/2026-12-01 s/PHYSICS` gives `Prefix s/ can be given only once.` and the usage only, as the first `s/` starts with the subject `MATH`, and `hw m 1 2 score/1 score/2` gives `Prefix score/ can be given only once.` and the usage only. Other commands keep their own messages for a repeated prefix.

* The command words `homework` and `hw`, homework subcommands and the word `all` must be typed in lowercase: `HW e 1 2 t/X` gives `Command words are lowercase: type hw, not HW.`, `hw E 1 2 t/X` gives `Homework subcommands are lowercase: type e, not E.`, and `hw ls 1 ALL` gives `The word all is lowercase: type all, not ALL.`

* `homework` or `hw` must be followed by a space and one of the homework subcommands. `hw` on its own gives `Missing homework subcommand. Use add, list|ls, upcoming|up, edit|e, marks|m, delete|del, filter or status.`, and a word that is not a subcommand is named, e.g. `hw rm 1 1` gives `rm is not a homework subcommand. Use add, list|ls, upcoming|up, edit|e, marks|m, delete|del, filter or status.` The subcommand must be followed by a space too, so `hw ls1` is rejected in the same way, naming `ls1`.

* In the homework subcommands other than `filter` and `status`, a space character other than the ordinary space, such as the no-break space that text copied from a web page or document may contain, is read as an ordinary space in the text after the subcommand. So `hw del 1 2` typed with a no-break space between `1` and `2` deletes the homework as usual. The spaces after `hw` and after the subcommand must be ordinary spaces: with a no-break space after `hw`, `hw add 1 t/A s/MATH due/2026-12-01` gives `Unknown command.`

* In homework commands, a missing index or an extra word before the first prefix is rejected with what was expected and what was found. For example, `hw del 1 1 1` gives `Expected STUDENT_INDEX HOMEWORK_INDEX, but found 3 words (1 1 1).`, and `hw add 1 extra t/X s/MATH due/2026-12-01` gives `Expected STUDENT_INDEX before the first prefix, but found 2 words (1 extra).` These errors show the command's usage on the next line.

* Extraneous parameters for commands that take no parameters, such as `help`, `exit`, and `clear`, are ignored. The `list` command rejects extra parameters.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</div>

### Viewing lessons and homework at a glance

The **At a glance** panel on the right shows every student's regular lessons in weekly chronological order and every homework record with status `ASSIGNED`, regardless of score, in due-date order, earliest first. Each entry includes the student's name, phone number and subject, so students sharing a name can be distinguished. Homework entries also show the due date; overdue homework (`ASSIGNED` and due before today) is marked `(overdue)` in bold red text.

The headings show the total number of lessons and outstanding homework tasks. Empty sections show `No regular lessons` or `No outstanding homework`. The panel updates when roster data changes and always covers all students, even while `search`, `filter` or `sort` changes the displayed student cards. Marking homework `COMPLETED` removes it from the sidebar and overdue count; recording a score alone does not. Date-sensitive entries and card counts refresh at midnight, when the window becomes active again, and before a command is run.

Each student card shows **Upcoming homework**, counting all of that student's homework due today or later,
including completed records, and **Overdue homework**, counting only assigned records due before today.
Recording or clearing a score does not change either count. The upcoming count can therefore differ from the
number of that student's outstanding tasks in the sidebar.

Drag the dividers to resize the command box, result display, student list and sidebar. You can scroll over a section's heading or padding as well as its entries while keeping the cursor in the command box.

### Viewing help: `help`

Opens a searchable help window with commands grouped into Students, Homework, Lessons and General. Each collapsed row gives the command name and a short description. Select a row to see its full description, format and example. Search by command name, format or description.

Format: `help`

* Pressing `F1` or choosing **Help** in the menu bar also opens the help window.
* Use `Up`, `Down`, `Page Up` and `Page Down` to scroll the command list. The search field stays focused while you browse.
* Press `Esc` to close the help window. The cursor goes back to the command box, so you can type your next command straight away.


### Adding a student: `add`

Adds a student to the end of the student list.

Format: `add n/STUDENT_NAME l/ACADEMIC_LEVEL s/SUBJECTS p/STUDENT_PHONE gn/GUARDIAN_NAME gp/GUARDIAN_PHONE`

* All six fields are required, and each prefix must appear exactly once.
* `STUDENT_NAME` and `GUARDIAN_NAME` must be 1 to 70 characters long and may contain letters, numbers, spaces, apostrophes, hyphens, periods, parentheses and `/`. Letters and numbers from non-English languages are accepted. Spaces at both ends are removed, and repeated whitespace between words becomes one space before the length is checked. Capitalization is kept.
* A name may contain words such as `s/o` and `d/o`, but not this command's other prefixes (such as `p/` or `gn/`) after a space, unless the whole name is wrapped in double quotes (see the [notes about the command format](#features)).
* `ACADEMIC_LEVEL` must be one of `S1`, `S2`, `S3`, `S4`, `S5`, `J1` or `J2` (in any case).
* `SUBJECTS` is one or more of `MATH`, `PHYSICS` and `CHEMISTRY` (in any case), separated by commas, with no subject repeated. The student card shows the subjects in the order you type them.
* `STUDENT_PHONE` and `GUARDIAN_PHONE` must each be an 8-digit Singapore number starting with 6, 8 or 9, or an international number written as `+` followed by 8 to 15 digits, without spaces within the number. Spaces at both ends are removed.
* A student cannot be added if a student with the same name (ignoring case and extra whitespace) and the same student phone number is already in the roster, including students hidden by a search or filter. A local number and the same number prefixed by `+65` count as the same phone: `91234567` and `+6591234567` are equivalent. Academic details and guardian contacts do not affect this duplicate check.
* A successful addition shows the full roster in stored order, clearing any earlier search, filter or sort.

Examples:
* `add n/Aaron Koh l/S2 s/MATH p/91112222 gn/Koh Mei Hua gp/98887777`
* `add n/Siti Rahmah binte Ismail l/j1 s/math, chemistry, physics p/87654321 gn/Ismail bin Yusof gp/+60123456789` adds a J1 student who takes all three subjects, with a Malaysian number for the guardian.
* `add gp/96543210 gn/Ramesh s/o Krishnan p/93334444 s/PHYSICS, MATH n/Kavin Ramesh l/S4` gives the fields in a different order. `s/o` in the guardian's name is part of the name, not the `s/` prefix.

### Listing all students: `list`

Shows all students in stored roster order, starting from the top, including after a search, filter or sort. The displayed indexes start at 1.
If the roster is empty, the app shows `No students found.` This command does not change the roster.
With one student, the result says `Listed 1 student.`; with more students, it says `Listed N students.` using the actual count.

Format: `list` or `ls` (no parameters)

### Editing a student: `edit`

Changes one or more details of the student at the displayed index without removing their homework or lessons. The
change is saved automatically. In the full roster, the updated student card stays in the same position. After a
search or filter, the card remains visible only if the edited details still match. If the displayed students have
been sorted, editing does not reorder them; run `sort` again to use the updated values.

Format: `edit INDEX [n/STUDENT_NAME] [l/ACADEMIC_LEVEL] [s/SUBJECTS] [p/STUDENT_PHONE] [gn/GUARDIAN_NAME] [gp/GUARDIAN_PHONE]`

* `INDEX` is a positive whole number without leading zeroes, as shown beside the student. At least one field prefix
  must be supplied; fields you omit stay unchanged. Prefixes may be in any order, but must not be repeated.
* Edited values follow the same rules as in `add`. The new name and student phone must not duplicate another
  student's name and phone, including the local/`+65` equivalence described above. Changes to other fields do not
  affect identity.
* A subject cannot be removed if any of this student's homework or lessons uses it. You can add subjects while
  retaining all used subjects.
* If all supplied values are identical to the existing details, or the index is invalid, nothing is changed. If
  saving fails, the original profile, homework and lessons are restored.

Examples:
* `edit 1 l/S4 p/91234568` changes the first student's academic level and phone.
* `edit 2 gn/Ramesh s/o Krishnan gp/96543210` changes the guardian's name and phone.

### Searching for students: `search`

Shows students whose student name, student phone, guardian name or guardian phone contains the keyword. Name matching
ignores letter case. Matches stay in roster order and are numbered from 1 in the displayed list.

Format: `search KEYWORD`

* `KEYWORD` must not be blank; it can contain spaces. Repeated and Unicode whitespace is treated as one space.
  For example, `search Mary Tan` searches for that full phrase.
* `search 9123` can find a student by part of their phone number. `search Tan` may match a student or a guardian.
* The feedback repeats the keyword used, e.g. `Found 2 students for "Tan".` If nothing matches, TutorFlow shows
  `No students found for "Nobody".` and displays no student cards. In both cases, the feedback reminds you to type
  `list` to show all students again.
* Student indexes in subsequent commands, such as `delete 1` or `homework list 1`, refer to the currently displayed
  search results. Run `list` or `ls` to show the full roster again. Adding a student or running `homework upcoming`
  also restores the full roster.
* Searching only changes which students are displayed; it does not change the saved roster.
* A new search replaces any active filter and searches the full roster.

### Filtering students: `filter`

Shows students matching any combination of academic level, tuition subject, and regular meeting day.

Format: `filter [l/LEVEL] [s/SUBJECT] [d/DAY]`

* Supply at least one filter. Each prefix may appear only once, in any order.
* `LEVEL` must be `S1`, `S2`, `S3`, `S4`, `S5`, `J1` or `J2`.
* `SUBJECT` must be one of `MATH`, `PHYSICS` or `CHEMISTRY`. A student taking several subjects matches if one
  of them is the selected subject. Supply only one subject to the filter command.
* `DAY` must be a full weekday: `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY` or `SUNDAY`.
  A student matches if any regular lesson is on that day. Students without regular lessons do not match.
  Supply only one day; abbreviations such as `MON` are not accepted.
* Values ignore letter case and surrounding whitespace, so `s/Math` matches `MATH`.
* A student must match **every supplied criterion**. Subject checks registration independently of the lesson
  that matches the day: a student taking MATH with a PHYSICS lesson on Monday matches `s/MATH d/MONDAY`.
  Matches return to stored roster order, discarding an earlier sort, and receive displayed indexes starting at 1.
  Subsequent student-index commands use these displayed indexes. A successful filter clears the selection and scrolls
  the student list to the top.
* Each filter examines the full roster and replaces any previous search or filter. To show all students again,
  run `list` or `ls`. Adding a student or running `homework upcoming` also restores the full roster.
* Filtering changes only the displayed students. It does not change records or write to the data file.

The student panel keeps the active criteria below its heading, such as `Filtered: day MONDAY` or
`Filtered: level S3; subject MATH; day MONDAY`. This description remains visible after other commands replace the
feedback, including when no students match or every student matches. The count beside the heading is the number
of displayed students. Long descriptions wrap to fit the panel.

A new filter replaces the description. `list`, `ls`, a successful student addition, or `homework upcoming`
removes it by restoring the full roster. A new search also removes the filter description because it
replaces that filter. The criteria stay active after sorting, editing or deleting students; `clear` retains them on an empty roster.
Invalid commands leave the description unchanged. If saving fails, the previous filter and description are restored.
Lesson additions, edits and deletions re-evaluate day matches immediately while retaining the selected criteria.

Examples:

* `filter l/S4` shows every S4 student.
* `filter s/Math` shows every student taking MATH, including those taking other subjects too.
* `filter l/S4 s/Physics` and `filter s/Physics l/S4` both show S4 students taking PHYSICS, such as
  Nurul Aisyah in the sample roster.
* `filter d/Monday` shows students with any Monday lesson, such as Ethan Lim in the sample roster.
* `filter l/S3 s/Math d/Monday` shows S3 students taking MATH who also have a Monday lesson; Ethan Lim matches.

For one match, the feedback is `Found 1 student.`; for multiple matches, it is `Found N students.`, using the
actual count. If nothing matches, including when the roster is empty, no student cards are displayed and the
feedback is `No students match the selected filters`.

With no filters, TutorFlow shows:

```text
Invalid command format.
Usage: filter [l/LEVEL] [s/SUBJECT] [d/DAY]
At least one filter must be provided
```

Other invalid commands report:

| Problem | Feedback |
| --- | --- |
| Unknown prefix, e.g. `filter l/S4 p/123` | `p/ is not a prefix of this command. To use it inside a value, wrap the whole value in straight double quotes, e.g. l/"S4 p/123".`, followed by `Usage: filter [l/LEVEL] [s/SUBJECT] [d/DAY]` on the next line (see the [notes about the command format](#features)) |
| Text before the first prefix | `Invalid command format.`, followed by `Usage: filter [l/LEVEL] [s/SUBJECT] [d/DAY]` on the next line |
| Repeated prefix | `Each filter may only be specified once` |
| Empty level | `Academic level must be a non-empty value` |
| Unsupported level | `Academic level must be one of: S1, S2, S3, S4, S5, J1, J2.` |
| Empty subject | `Subject must be a non-empty value` |
| Unsupported subject | `Subject must be one of: MATH, PHYSICS, CHEMISTRY.` |
| Comma-separated subjects | `Filter accepts only one subject. Use s/SUBJECT.` |
| Empty day | `Day must be a non-empty value` |
| Unsupported day or multiple days | `Day must be one of: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY.` |

After checking the prefixes, TutorFlow checks values in the order you write them and reports the first invalid
value. For example, `filter s/ l/` reports `Subject must be a non-empty value`, while `filter d/ s/` reports
`Day must be a non-empty value`.

Invalid commands leave the displayed students, selection, scroll position and data file unchanged.
If an internal problem prevents rendering, TutorFlow shows
`TutorFlow could not display the filtered results due to an internal error`. Student records remain unchanged;
you can retry the filter or use `list` to show all students again.

### Sorting displayed students: `sort`

Orders the displayed students while preserving any current search or filter results and the saved roster order.

Format: `sort by/KEY [o/ORDER]`

* Give exactly one `by/` parameter. `KEY` is `level`, `subject` or `day`.
* `ORDER` is `asc` (the default) or `desc`. Values ignore letter case and surrounding spaces.
  The parameters may appear in either order, and each prefix may appear only once.

Key | Ascending order | Value used per student
----|-----------------|----------------------
`level` | `S1`, `S2`, `S3`, `S4`, `S5`, `J1`, `J2` | The student's academic level
`subject` | Alphabetical, ignoring case | Alphabetically first subject, e.g. `MATH` for `PHYSICS, MATH`
`day` | Monday through Sunday | Earliest regular meeting day; meeting times are ignored

Descending uses the same value in reverse order. Students without regular meetings stay last in both directions.
Students with equal values, including those without meetings, retain their order immediately before this command.

Examples:

* `sort by/level` orders students by academic level, ascending.
* `sort by/subject o/desc` orders students by their alphabetically first subject, descending.
* `sort o/ASC by/ DAY` orders students by earliest regular meeting day, ascending.

The feedback is `Sorted N student(s) by academic level (ascending).`, with the actual count, key and direction.
An empty result reports `No students to sort.` Sorting one student also succeeds.

Displayed indexes restart at 1. Subsequent commands such as `delete 1`, `lesson add 1` and `homework list 1`
use these displayed positions. `list` restores the complete roster in stored order. A new search uses its own
roster ordering; successful `add` and `homework upcoming` also restore the full roster in stored order.
A new `filter` command also discards the previous sort and displays matching students in stored roster order.
Sorting does not edit student records, change the data file or trigger a save.

Sorting is applied once. If a student's academic level, subjects or regular meetings change, the student keeps
their displayed position while they still match the active search or filter. Students who no longer match disappear
from the view. Run `sort` again to order the updated values. For example, after `list` and then `sort by/day`, deleting the first
student's only meeting leaves them first. Running `sort by/day` again moves them behind students with meetings.

Invalid input leaves the current roster, order, selection and scroll position unchanged:

Condition | Message
----------|--------
Missing `by/` | `Invalid command format` followed by `Usage: sort by/KEY [o/ORDER]` and `Exactly one sort key must be provided` on separate lines
Unknown prefix, e.g. `sort by/level x/value` | `x/ is not a prefix of this command. To use it inside a value, wrap the whole value in straight double quotes, e.g. by/"level x/value".` followed by `Usage: sort by/KEY [o/ORDER]` on a new line (see the [notes about the command format](#features))
Text before the parameters | `Invalid command format` followed by `Usage: sort by/KEY [o/ORDER]` on a new line
Repeated prefix | `Each sort parameter may only be specified once`
Empty or invalid key | `Sort key must be one of: level, subject, day.`
Empty or invalid order | `Sort order must be either asc or desc.`

If sorting or rendering fails unexpectedly, TutorFlow attempts to restore the previous displayed order,
selection and scroll position, and shows `TutorFlow could not sort the students due to an internal error.`
Student records and the saved roster remain unchanged. If the previous view cannot be fully restored, try `list`
to reset the view to the full roster in stored order. Technical details of the failure and recovery are recorded in the logs.

### Deleting a student: `delete`

Deletes the specified student, together with their homework and regular lessons, from the student list. The change is saved automatically.

Format: `delete INDEX`

* `INDEX` is the number shown next to the student, starting at 1. It must be a positive whole number without leading zeroes.
* If the list is empty or the index exceeds its size, no student is deleted.
  The app shows `The student index provided is invalid.`
* Remaining students retain their order and are renumbered. Deletion cannot be undone.

Example: `delete 2` deletes the second student and their homework and regular lessons.

### Clearing all students: `clear`

Deletes all students together with their homework and regular lessons.

Format: `clear`

* Clearing cannot be undone. To keep a copy of your data, back up the data file first (see [Editing the data file](#editing-the-data-file)).

### Adding homework to a student: `homework add`

Adds a homework record to the end of a student's homework list.

Format: `homework|hw add STUDENT_INDEX title/|t/TITLE s/SUBJECT due/YYYY-MM-DD|MM-DD`

* `homework` can be shortened to `hw`, and `title/` can be shortened to `t/`. Use either `title/` or `t/`, not both; giving both is rejected with `Use either title/ or t/, not both.` and the usage. If the second one is in the title, e.g. `hw add 1 t/Read title/abstract s/MATH due/2026-12-01`, the error also says to quote the title: `Use either title/ or t/, not both. If title/ is part of the title, put the whole title in straight double quotes, e.g. t/"True t/f quiz".`
* All three of the title, `s/` and `due/` are required. A missing one is named in the error, followed by the usage, e.g. `hw add 1 s/MATH due/2026-12-01` gives `Missing required prefix(es): title/ (or t/)`.
* `STUDENT_INDEX` refers to the index number shown in the displayed student list. It **must be a positive whole number without leading zeroes**, such as 1, 2, 3, …​ A number above 2147483647 is rejected with `STUDENT_INDEX must be no greater than 2147483647.`
* `TITLE` must be 1 to 100 characters long and may contain only letters, numbers, punctuation, symbols and spaces, such as `Café`, `x² + π ≈ 3.14`, `😀 quiz` or `Ch 3 Q1-10 (pg. 45)`. Extra spaces are removed, and capitalization is kept. An empty title, such as `t/`, `t/""` or `t/" "`, is rejected with `The homework title is empty. Enter 1 to 100 characters.` A longer title is rejected with a message that gives its length, e.g. `Homework title must be at most 100 characters, but has 101.`
* Invisible characters, such as a zero-width space (U+200B) or the variation selector U+FE0F at the end of some emoji, are rejected wherever they are in the title. So emoji that contain an invisible character, such as ❤️ (U+2764 followed by U+FE0F) or 👨‍👩‍👧 (joined by U+200D), are rejected. The error names the first such character by its code and name, e.g. `t/Love ❤️` gives `Homework title has an unsupported character: U+FE0F (VARIATION SELECTOR-16). Titles may contain only letters, numbers, punctuation, symbols and spaces.`
* Scripts that use combining vowel signs, such as Tamil, Hindi or Thai, are not supported, the same as in student names: a title that contains such a sign, e.g. `தமிழ்`, is rejected with `Homework title has an unsupported character: U+0BBF (TAMIL VOWEL SIGN I). Titles may contain only letters, numbers, punctuation, symbols and spaces.`
* A word of lowercase English letters (`a` to `z`) directly followed by `/` in `TITLE` (such as `km/h` or `and/or`) is read as a prefix, so the command is rejected. Wrap the title in double quotes to keep such words, e.g. `t/"Speed in km/h"` (see the [notes about the command format](#features)).
* `SUBJECT` must be `MATH`, `PHYSICS` or `CHEMISTRY` (in any case). Any other value is named in the error, e.g. `s/BIOLOGY` gives `BIOLOGY is not a subject. Subject must be one of: MATH, PHYSICS, CHEMISTRY.`, and an empty `s/` gives `The subject is empty. Subject must be one of: MATH, PHYSICS, CHEMISTRY.`
* A value of more than one word right after a title without quotes may be the rest of the title, so its error also says to quote the title, unless its first word (up to a space or comma) is a subject. For example, `hw add 1 t/Read s/o notes due/2026-12-01` gives `o notes is not a subject. Subject must be one of: MATH, PHYSICS, CHEMISTRY. If s/ is part of the title, put the whole title in straight double quotes, e.g. t/"Read s/o notes".`, while `hw add 1 t/Essay s/MATH PHYSICS due/2026-12-01` gives only `MATH PHYSICS is not a subject. Subject must be one of: MATH, PHYSICS, CHEMISTRY.`
* The student must take the subject. If the student does not take it, the error names the student and the subjects they take, e.g. with the sample students, `hw add 1 t/Stoichiometry s/CHEMISTRY due/2026-12-01` gives `Ethan Lim (91234567) does not take CHEMISTRY. They take MATH, PHYSICS.`
* The due date must be a real calendar date in `YYYY-MM-DD` format, with a year from 2000 to 2099. Past dates are accepted, so you can record homework that was assigned earlier. If the due date is before today, the result ends with a note that `homework list` shows the homework only with `all`.
* The due date can also be given as `MM-DD`. The year is then this year, or next year if that date has already passed this year, and the result shows the year used.
* For 29 February (`02-29` or `2-29`), the year is this year only if this year is a leap year and 29 February has not passed yet. Otherwise it is next year, and if next year is not a leap year, the date is rejected with a message that names the next leap year. For example, `due/2-29` gives `2028-02-29` throughout 2027, and throughout 2026 it is rejected with `29 February does not occur in the next 12 months. Enter the full date, e.g. 2028-02-29.`
* In both formats, the month and day may have 1 or 2 digits, so `2026-2-5` means 5 February 2026 and `10-5` means 5 October. The year must have 4 digits.
* A wrong due date is rejected with a message that says what is wrong:
  * `due/30-10-2026` gives `30-10-2026 is not in YYYY-MM-DD or MM-DD format, e.g. 2026-10-30 or 10-30.`
  * `due/2026-02-30` gives `2026-02-30 is not a real calendar date.`
  * `due/2206-10-30` gives `The due date's year must be from 2000 to 2099.` An `MM-DD` date whose year would be after 2099 gets the same message.
* A student cannot have two homework records with the same title (ignoring case), subject and due date. The same homework can be added to different students. New homework starts as `ASSIGNED` with no recorded score.
* Adding such a duplicate is rejected, and the error shows the record that already exists, with its number. For example, with the sample students, `hw add 1 t/quadratic equations worksheet s/MATH due/2026-11-03` gives:

  ```text
  This homework already exists for Ethan Lim (91234567):
  1. Quadratic equations worksheet (MATH) - due 2026-11-03 - score Not recorded - status ASSIGNED
  ```

* An accented letter counts as the same whether it is typed as one character (`é`) or as a letter followed by a combining accent. Letters that only look alike, such as a Cyrillic `а` and a Latin `a`, are different.
* Each student card shows `Upcoming homework: N`, the number of the student's homework records due today or later (including scored or completed homework), and `Overdue homework: N`, the number of `ASSIGNED` homework records due before today, regardless of score.
* On success, the result names the student and shows the new homework as `homework list` shows it, with its number. Use this number as `HOMEWORK_INDEX` in `homework edit`, `homework marks`, `homework delete` and `homework status`. For a due date in `MM-DD` form, a line such as `Inferred year: 2026` follows.

Examples:
* `homework add 1 title/Complete algebra worksheet s/MATH due/2026-10-15` adds homework due before today. With the sample students, the result is:

  ```text
  New homework added for Ethan Lim (91234567):
  3. Complete algebra worksheet (MATH) - due 2026-10-15 - score Not recorded - status ASSIGNED
  Note: this homework is due before today, so homework list 1 shows it only with all.
  ```
* `hw add 1 t/Attempt mechanics questions 1-5 s/PHYSICS due/2026-11-20`
* `homework add 2 due/10-18 s/chemistry title/Revise atomic structure` adds homework due on 18 October of this year, or of next year if 18 October has passed.
* `hw add 1 t/Read chapter 3 s/physics due/10-5` adds homework due on 5 October of this year, or of next year if 5 October has passed.
* `hw add 1 t/"Speed in km/h" s/PHYSICS due/2026-12-01` adds homework titled `Speed in km/h`; the quotes keep `km/h` in the title.

### Listing a student's homework: `homework list`

Shows a student's homework that is due today or later, earliest due date first, with the score and completion status of each. Add `all` to also show homework due before today. Listing includes both assigned and completed homework; use `homework filter` to narrow by completion status.

Format: `homework|hw list|ls STUDENT_INDEX [all]`

* `homework` can be shortened to `hw`, and `list` can be shortened to `ls`.
* `STUDENT_INDEX` follows the same rules as in `homework add`.
* Without `all`, homework due before today is left out. With `all` (in lowercase), all of the student's homework is shown. Any other word after `STUDENT_INDEX` is rejected with the usage, e.g. `hw ls 1 alll` gives `The word after STUDENT_INDEX can only be all, but found alll.`
* Homework is sorted by due date, earliest first. Homework due on the same date is shown in the order it was added.
* The result starts with a line such as `Listed N upcoming homework records for NAME (PHONE).` (`Listed N homework records for NAME (PHONE).` with `all`, and `record` when there is only one) and `Homework for NAME (PHONE):`, followed by one line per homework with its number, title, subject, due date, score and status, e.g. `2. Kinematics practice set (PHYSICS) - due 2026-11-11 - score 5/10 - status ASSIGNED`. Homework without marks shows `score Not recorded`.
* Without `all`, if any homework is left out because it is due before today, the first line also says how many records are not shown and how to see them, e.g. `Listed 2 upcoming homework records for Priya Nair (82345671) (1 record due before today not shown; use homework list 2 all to see it).`
* Each homework is numbered by its position in the order the homework was added, so the numbers may appear out of order. These numbers are the `HOMEWORK_INDEX` values used by `homework edit`, `homework marks` and `homework delete`. They change only when homework is deleted. They are always written with the digits 0 to 9, even if your computer's language and region settings use other digits, so you can type them into these commands.
* If the student has no homework, the result is `No homework found for NAME (PHONE).` If none of the student's homework is due today or later, the result is `No upcoming homework found for NAME (PHONE) (N records due before today not shown; use homework list STUDENT_INDEX all to see them).` (`1 record` and `see it` when there is only one).

Examples:
* `homework list 1`
* `hw ls 2 all` lists all homework of the 2nd student, including homework due before today.

### Listing upcoming homework of all students: `homework upcoming`

Shows the homework of all students that is due in the next few days, earliest due date first, with the score and completion status of each. Unlike `homework list`, which shows all of a student's homework due today or later, this command shows only the homework due within a number of days from today.

Format: `homework|hw upcoming|up [due/DAYS]`

* `homework` can be shortened to `hw`, and `upcoming` can be shortened to `up`.
* `DAYS` is a **number of days, not a date**: the homework shown is due from today to `DAYS` days after today, both days included. For example, on 2026-11-06, `due/3` shows homework due from 2026-11-06 to 2026-11-09, and `due/0` shows homework due today only.
* `DAYS` must be a whole number from 0 to 365 without signs, decimals, spaces or leading zeroes (`0` itself is accepted). If `due/` is left out, `DAYS` is 7. Other values are rejected with `Days must be a whole number from 0 to 365 without leading zeroes.`
* A date given as `DAYS`, i.e. numbers joined by `-` or `/` such as `due/2026-11-20` or `due/11-20`, is rejected with `In homework upcoming, due/ is a number of days from 0 to 365, not a date, e.g. due/7.` To see the homework due on one date, use [`homework filter`](#filtering-homework-homework-filter) with `due/DATE`.
* Homework due before today is left out. Homework is shown whether or not it has marks, including completed homework. Upcoming lists and the student card's upcoming count are based on due dates only; use `homework filter status/ASSIGNED` to find unfinished work.
* Unlike the **At a glance** panel, which shows every `ASSIGNED` homework of every student, including overdue homework, this command shows homework of any status due within the period and leaves out homework due before today.
* The homework of every student is searched, not only the students shown in the student list.
* Like [`list`](#listing-all-students-list), this command shows all students again in stored roster order, even when no homework is due. It clears any `search` or `filter` and undoes any `sort` of the student list, so `Student N` always matches the student list shown afterwards. Run `sort` again if you want the sorted order back.
* Homework is sorted by due date, earliest first. Homework due on the same date is shown in the order of the students in the student list, and then in the order it was added.
* The result starts with `Found N homework records due from START to END.` (`record` when there is only one), followed by one line per homework with the student's `STUDENT_INDEX`, name and phone, title, subject, due date, score and status, e.g. `Student 1: Ethan Lim (91234567); Kinematics practice set; PHYSICS; Due: 2026-11-11; Score: 5/10; Status: ASSIGNED`. Homework without marks shows `Score: Not recorded`.
* The title is shown as typed, so a `;` in a title also appears in the line, e.g. a homework titled `Quiz; part 2` is shown as `...; Quiz; part 2; MATH; Due: ...`.
* `Student N` is the index of the student in the displayed student list, the `STUDENT_INDEX` that other commands take. It tells apart students with the same name. Like the numbers in `homework list`, it is always written with the digits 0 to 9.
* If no homework is due in that period, the result is `No homework due from START to END.`
* The lines are not numbered, and their order is not the `HOMEWORK_INDEX` used by other homework commands. To edit, score or delete a record shown here, find its number with [`homework list STUDENT_INDEX`](#listing-a-students-homework-homework-list), using the student index shown after `Student`.
* This command does not change your data.

Examples:
* `hw up` shows the homework due from today to 7 days after today. The result depends on today's date. For example, with the sample students on 2026-11-06, the result is:

  ```
  Found 3 homework records due from 2026-11-06 to 2026-11-13.
  Student 2: Priya Nair (82345671); Organic reaction pathways map; CHEMISTRY; Due: 2026-11-09; Score: Not recorded; Status: ASSIGNED
  Student 1: Ethan Lim (91234567); Kinematics practice set; PHYSICS; Due: 2026-11-11; Score: Not recorded; Status: ASSIGNED
  Student 3: Marcus Tan (87654329); Algebraic expansion worksheet; MATH; Due: 2026-11-12; Score: Not recorded; Status: ASSIGNED
  ```

  On other dates the dates and the number of records differ.
* `hw up due/3` shows the homework due from today to 3 days after today. For example, with the sample students on 2026-11-06, the result is:

  ```
  Found 1 homework record due from 2026-11-06 to 2026-11-09.
  Student 2: Priya Nair (82345671); Organic reaction pathways map; CHEMISTRY; Due: 2026-11-09; Score: Not recorded; Status: ASSIGNED
  ```

  On other dates the dates and the number of records differ.
* `hw up due/7` is the same as `hw up`.
* `hw up due/0` shows the homework due today.
* `hw upcoming due/7` is the same as `hw up due/7`.

### Editing a student's homework: `homework edit`

Changes the title, subject or due date of one homework record of a student.

Format: `homework|hw edit|e STUDENT_INDEX HOMEWORK_INDEX [title/|t/TITLE] [s/SUBJECT] [due/YYYY-MM-DD|MM-DD]`

* `homework` can be shortened to `hw`, `edit` can be shortened to `e`, and `title/` can be shortened to `t/`.
* `STUDENT_INDEX` follows the same rules as in `homework add`.
* `HOMEWORK_INDEX` refers to the number shown by `homework list STUDENT_INDEX` (or by `homework list STUDENT_INDEX all` for homework due before today). It **must be a positive whole number without leading zeroes**. A number above 2147483647 is rejected with `HOMEWORK_INDEX must be no greater than 2147483647.`
* If the student has no homework, the result is `The selected student has no homework to edit.` If `HOMEWORK_INDEX` is larger than the student's number of homework records, it is `The homework index provided is invalid.`
* Give at least one of `title/` (or `t/`), `s/` and `due/`, in any order and each at most once. Giving none is rejected with `Specify at least one field to edit: title/ (or t/), s/ or due/.` A repeated prefix, or `title/` and `t/` together, is rejected as in [`homework add`](#adding-homework-to-a-student-homework-add). The details you do not give stay unchanged.
* `TITLE`, `SUBJECT` and the due date follow the same rules as in [`homework add`](#adding-homework-to-a-student-homework-add). A due date in `MM-DD` form gets its year in the same way, and the result shows the year used.
* The homework keeps its number, score and completion status. To change the score, use [`homework marks`](#recording-homework-marks-homework-marks).
* The edit is rejected if it changes nothing (`No changes made; the supplied values are identical to the existing homework details.`), if the student does not take the new subject (the same error as in `homework add`), or if the edited homework would have the same title (ignoring case), subject and due date as another homework of the student. The last error shows that other record, e.g. with the sample students, `hw e 1 2 t/Quadratic equations worksheet s/MATH due/2026-11-03` gives:

  ```text
  This edit would make the homework the same as another homework for Ethan Lim (91234567):
  1. Quadratic equations worksheet (MATH) - due 2026-11-03 - score Not recorded - status ASSIGNED
  ```

* On success, the result shows the edited homework, followed by the student's upcoming homework in the format of `homework list STUDENT_INDEX`.
* If the new due date is before today, the result ends with a note that `homework list` shows the homework only with `all`, as in `homework add`, e.g. `Note: this homework is due before today, so homework list 1 shows it only with all.` The note is not shown when `due/` is not given, or gives today or a later date.

Examples:
* `hw e 1 2 t/Kinematics practice set B` changes the title of the 2nd homework of the 1st student. The upcoming homework listed depends on today's date. For example, with the sample students on 2026-11-06, the result is:

  ```
  Edited homework:
  Ethan Lim (91234567); Title: Kinematics practice set B; Subject: PHYSICS; Due: 2026-11-11
  Listed 1 upcoming homework record for Ethan Lim (91234567) (1 record due before today not shown; use homework list 1 all to see it).
  Homework for Ethan Lim (91234567):
  2. Kinematics practice set B (PHYSICS) - due 2026-11-11 - score Not recorded - status ASSIGNED
  ```
* `hw e 2 1 due/2026-12-05` changes the due date of the 1st homework of the 2nd student. For example, with the sample students on 2026-11-06, the result is:

  ```
  Edited homework:
  Priya Nair (82345671); Title: Integration by parts exercise; Subject: MATH; Due: 2026-12-05
  Listed 3 upcoming homework records for Priya Nair (82345671).
  Homework for Priya Nair (82345671):
  2. Organic reaction pathways map (CHEMISTRY) - due 2026-11-09 - score Not recorded - status ASSIGNED
  3. Chemical equilibrium past paper (CHEMISTRY) - due 2026-11-23 - score Not recorded - status ASSIGNED
  1. Integration by parts exercise (MATH) - due 2026-12-05 - score Not recorded - status ASSIGNED
  ```
* `hw edit 1 2 t/Revise mechanics s/PHYSICS due/2026-11-10` changes the title, subject and due date together.
* `hw e 1 2 t/"Speed in km/h"` changes the title to `Speed in km/h`.
* `hw e 3 1 due/10-5` moves the due date to 5 October of this year, or of next year if 5 October has passed.
* `hw e 1 2 due/2026-11-01` moves the due date of the 2nd homework of the 1st student to a date before today. For example, with the sample students on 2026-11-06, the result is:

  ```text
  Edited homework:
  Ethan Lim (91234567); Title: Kinematics practice set; Subject: PHYSICS; Due: 2026-11-01
  No upcoming homework found for Ethan Lim (91234567) (2 records due before today not shown; use homework list 1 all to see them).
  Note: this homework is due before today, so homework list 1 shows it only with all.
  ```

### Recording homework marks: `homework marks`

Records the marks a student obtained for one homework record, replacing any marks recorded before, or clears the recorded marks.

Format: `homework|hw marks|m STUDENT_INDEX HOMEWORK_INDEX score/[SCORE]`

* `homework` can be shortened to `hw`, and `marks` can be shortened to `m`.
* `STUDENT_INDEX` and `HOMEWORK_INDEX` follow the same rules as in `homework edit`.
* If the student has no homework, the result is `The selected student has no homework to record marks for.` If `HOMEWORK_INDEX` is larger than the student's number of homework records, it is `The homework index provided is invalid.`
* `SCORE` is either the marks on their own, e.g. `17`, or `MARKS_OBTAINED/TOTAL_MARKS`, e.g. `5/10`, when the homework has a total.
* Every number in `SCORE` is a whole number without a sign, decimals, spaces or leading zeroes, and no greater than 2147483647. So `0`, `17` and `150` are accepted, but `-1`, `+17`, `017` and `17.5` are not. A score with a leading zero in any number, such as `05/10` or `017`, is rejected with `Marks must not have leading zeroes. Enter whole numbers such as 5/10, not 05/10.`
* With a total, `TOTAL_MARKS` must be at least 1, and `MARKS_OBTAINED` must be from 0 to `TOTAL_MARKS`. So `0/10` and `10/10` are accepted, but `11/10`, `5/0`, `5.5/10` and `5 / 10` are not. The error names the rule that failed:
  * `score/11/10` gives `Marks obtained (11) must not be more than the total marks (10).`
  * `score/5/0` gives `Total marks must be at least 1.`
  * A number above 2147483647, e.g. `score/2147483648`, gives `Marks are too large. Enter whole numbers no greater than 2147483647.`
  * A score in neither form, e.g. `score/5.5/10` or `score/17.5`, gives `Score must be whole marks such as 17, or marks out of a total such as 5/10, with no signs, decimals, spaces or other characters.`
* The score is kept and shown as you give it: `5/10` is not simplified to `1/2`, and `17` and `17/20` are different scores.
* Recording the score that the homework already has is rejected with `No changes made; the homework already has this score.`
* Homework without marks shows `Not recorded`, not `0`, in `homework list`.
* On success, the result shows the homework with its new score, e.g. `Recorded homework marks:` followed by `Ethan Lim (91234567); Title: Kinematics practice set; Subject: PHYSICS; Due: 2026-11-11; Score: 5/10`.
* Leaving `SCORE` empty (nothing or only spaces after `score/`) clears the recorded marks, so the homework shows `Not recorded` again. The result is `Cleared homework marks:` followed by the homework, e.g. `Ethan Lim (91234567); Title: Kinematics practice set; Subject: PHYSICS; Due: 2026-11-11; Score: Not recorded`. Clearing homework that has no marks is rejected with `No changes made; the homework has no recorded marks.`
* The `score/` prefix is required even when clearing: `hw m 1 2` without it is rejected.
* Recording or clearing marks does not change the homework's completion status. Use [`homework status`](#updating-homework-completion-homework-status) to mark it completed.

Examples:
* `hw m 1 2 score/5/10`
* `hw m 1 2 score/17` records 17 marks, without a total, for the 2nd homework of the 1st student.
* `hw m 2 1 score/35/50`
* `homework marks 4 2 score/0/10` records that the 4th student scored no marks for their 2nd homework.
* `hw m 1 2 score/`, after `hw m 1 2 score/5/10` above, clears the marks of the 2nd homework of the 1st student.

### Deleting a student's homework: `homework delete`

Deletes one homework record from a student.

Format: `homework|hw delete|del STUDENT_INDEX HOMEWORK_INDEX`

* `homework` can be shortened to `hw`, and `delete` can be shortened to `del`.
* `STUDENT_INDEX` follows the same rules as in `homework add`.
* `HOMEWORK_INDEX` refers to the number shown by `homework list STUDENT_INDEX` (or by `homework list STUDENT_INDEX all` for homework due before today). It **must be a positive whole number without leading zeroes**. A number above 2147483647 is rejected with `HOMEWORK_INDEX must be no greater than 2147483647.`
* If the student has no homework, the result is `The selected student has no homework to delete.` If `HOMEWORK_INDEX` is larger than the student's number of homework records, it is `The homework index provided is invalid.`
* The remaining homework keeps its order and is renumbered.
* The result shows the deleted record as `homework list` shows it, with the number it had, its score and its status. Below it is the student's upcoming homework with the new numbers, in the format of `homework list STUDENT_INDEX`. Use these numbers for the next command, as the numbers shown before the deletion may now point to different homework.
* Deletion cannot be undone.

Examples:
* `homework list 1` followed by `homework delete 1 2` deletes the 2nd homework of the 1st student.
* `hw del 3 1`
* For example, with the sample students on 2026-11-06, after `hw add 2 t/Vectors revision s/MATH due/2026-11-30`, `hw del 2 2` shows the following. On other dates, the upcoming homework listed differs.

  ```text
  Deleted homework:
  2. Organic reaction pathways map (CHEMISTRY) - due 2026-11-09 - score Not recorded - status ASSIGNED
  Listed 2 upcoming homework records for Priya Nair (82345671) (1 record due before today not shown; use homework list 2 all to see it).
  Homework for Priya Nair (82345671):
  2. Chemical equilibrium past paper (CHEMISTRY) - due 2026-11-23 - score Not recorded - status ASSIGNED
  3. Vectors revision (MATH) - due 2026-11-30 - score Not recorded - status ASSIGNED
  ```

### Updating homework completion: `homework status`

Marks homework completed or reopens it as assigned, independently of its score.

Format: `homework|hw status STUDENT_INDEX HOMEWORK_INDEX status/STATUS`

* `STUDENT_INDEX` selects a student in the current displayed order. `HOMEWORK_INDEX` is the original homework number shown by `homework list STUDENT_INDEX all` or `homework filter`. Both indices must be positive whole numbers without leading zeroes.
* `STATUS` is `ASSIGNED` or `COMPLETED`, ignoring case and surrounding spaces. Supply `status/` exactly once. Empty or unsupported values and extra arguments are rejected.
* `COMPLETED` marks homework complete, even if it has no score. `ASSIGNED` reopens it, retaining any score. Recording, replacing or clearing a score does not change completion status. Editing the homework's title, subject or due date also preserves status.
* The homework retains its insertion position and all other fields. If it already has the requested status, TutorFlow reports that no changes were made.
* Changes are saved automatically. If saving fails, TutorFlow restores the original homework and reports that no homework data was changed.

Examples:

* `hw status 1 2 status/COMPLETED` marks homework 2 of displayed student 1 completed.
* `hw status 1 2 status/ASSIGNED` reopens that homework without changing its score.
* `hw filter 1 status/COMPLETED` shows that student's completed homework, including overdue records.

### Filtering homework: `homework filter`

Lists homework matching a student, subject, exact due date and/or completion status.

Format: `homework|hw filter [STUDENT_INDEX] [s/SUBJECT] [due/DATE] [status/STATUS]`

* Supply at least one criterion. All supplied criteria must match (AND).
* Without `STUDENT_INDEX`, searches homework of every currently displayed student. Run `list` first to include the full roster. An index selects one student in the current displayed order, including after searching, filtering or sorting students.
* `STUDENT_INDEX` must be a positive whole number without leading zeroes. Put it before the prefixes.
* `SUBJECT` is MATH, PHYSICS or CHEMISTRY, ignoring case. Each prefix may appear once, in any order; unknown prefixes and empty values are rejected.
* `DATE` is an exact due date in `YYYY-MM-DD` or `MM-DD` format, with one or two digits for the month and day. Short dates infer this year or the next year if the date has passed, as in `homework add`. Use a full date to find overdue homework. Here `due/` takes a date; in `homework upcoming` it takes a number of days.
* Includes overdue homework and homework with or without scores. `STATUS` is `ASSIGNED` or `COMPLETED`, ignoring case; it must match the explicit status, independently of the score. Omit `status/` to include both states.
* Results follow displayed student order, then homework insertion order. Each line identifies both `Student N` and `Homework M`, which can be used directly in homework edit, marks and delete commands while the lists remain unchanged. The command preserves the displayed students, sorting and stored data.
* If nothing matches, TutorFlow reports `No homework matches the supplied filters.`

Examples:

* `hw filter 1` lists all homework of displayed student 1, including overdue homework.
* `hw filter s/PHYSICS` lists Physics homework among displayed students.
* `hw filter due/2026-10-20` lists homework due on 20 October 2026.
* `hw filter 1 s/MATH due/2026-10-15` lists student 1's Math homework due on 15 October 2026.
* `hw filter 1 s/MATH due/2026-10-15 status/ASSIGNED` adds the condition that the homework is still assigned.
* `hw filter status/COMPLETED` lists completed homework among displayed students, whether or not a score is recorded.

For a matching assignment, a result line looks like `Student 1: John Tan; Homework 2: Algebra; MATH; Due: 2026-10-15; Score: Not recorded; Status: ASSIGNED`. Use `hw edit 1 2 t/Algebra practice` to edit that assignment.

### Adding a regular lesson: `lesson add`

Each lesson belongs to one student. All overlapping lessons are rejected as tutor clashes, including lessons belonging to different students. Simultaneous group lessons are not supported.

Adds a weekly lesson to the student at the displayed student index.

Format: `lesson add STUDENT_INDEX s/SUBJECT d/DAY st/START_TIME et/END_TIME`

Example: `lesson add 1 s/MATH d/TUESDAY st/16:00 et/18:00`

* Use a positive student index without leading zeroes. Use `list` to see the current indices.
* Supply each prefix exactly once, in any order. Unknown prefixes are rejected.
* `SUBJECT` must be MATH, PHYSICS or CHEMISTRY and must be one of the student's tuition subjects.
* `DAY` must be a full weekday name, such as MONDAY. Subjects and days ignore case and surrounding spaces.
* Times must use the 24-hour `HH:mm` format, such as `09:00`. End time must be later than start time on the same day; overnight lessons are not supported.
* An identical lesson for the same student is rejected. A lesson that overlaps any existing lesson, for any student, is rejected. TutorFlow lists every conflicting lesson, with its student's name and lesson details, in weekly order. Lessons that meet exactly at an endpoint are allowed.

With the sample roster, this shows `New lesson added: Ethan Lim (91234567); MATH; TUESDAY; 16:00-18:00` and saves the lesson automatically. Tuesday avoids Ethan's existing Monday lesson. The student's position, profile and homework remain unchanged. If saving fails, the addition is undone and TutorFlow reports that no lesson data was changed.

`STUDENT_INDEX` refers to the displayed student list and must be from 1 to 2147483647, without signs or leading zeroes. Larger numbers report the supported upper bound. A missing field names every missing prefix, for example `Missing required prefix(es): d/, et/`, followed by usage guidance. Full weekday names and same-day times keep recurring schedules unambiguous; abbreviated days, `24:00` and overnight intervals are not supported.

### Editing a regular lesson: `lesson edit`

Updates a regular lesson identified by its number in `lesson list`.

Format: `lesson edit LESSON_INDEX [s/SUBJECT] [d/DAY] [st/START_TIME] [et/END_TIME]`

* Run `lesson list` first. `LESSON_INDEX` is a positive whole number without leading zeroes, referring to the full weekly schedule, including lessons whose students are hidden by a student search or filter.
* Supply at least one field. Omitted fields retain their original values. Prefixes may appear once, in any order; unknown prefixes and empty values are rejected. Subjects, weekdays and times follow the rules for `lesson add`.
* The proposed end must be after the proposed start, using the existing value for any omitted time. The student must take the proposed subject.
* TutorFlow checks the proposed day and times against every other lesson in the full roster, including the same student's other lessons and lessons hidden by a student search or filter. Only the original lesson is excluded. Changing just the subject is allowed when it is one of the student's tuition subjects.
* Lessons clash when they share a weekday and their times overlap, even by one minute. Lessons on different weekdays and lessons that meet exactly at an endpoint are allowed. If there are multiple clashes, all are listed in weekly order. Duplicates and edits that change no values are rejected.
* A rejected edit leaves the original lesson, displayed student list and saved data unchanged. The error identifies each conflicting student's name and phone, subject, weekday and times so you can choose another slot.
* The lesson remains with the same student, preserving their profile, homework and lesson insertion order. Its weekly schedule number may change after an edit; run `lesson list` again before another edit or deletion.
* The edit is saved automatically. If saving fails, TutorFlow restores the original data and reports that no lesson data was changed.

Examples:

* `lesson edit 1 d/TUESDAY` moves the first lesson to Tuesday, retaining its subject and times.
* `lesson edit 1 st/17:00 et/19:00` changes both times of the first lesson.

For example, suppose John Tan has a Monday lesson at 16:00-18:00 and Mary Lim (student phone 91234568) has one at 18:00-19:00. Editing John's lesson with `lesson edit 1 et/18:30` is rejected with `This lesson clashes with an existing lesson: Mary Lim (91234568); MATH; MONDAY; 18:00-19:00` (assuming Mary's subject is MATH). Editing John's lesson with `lesson edit 1 st/17:00` succeeds: 17:00-18:00 overlaps only its own original slot and ends exactly when Mary's lesson starts. The same checks apply after restarting TutorFlow.

### Deleting a regular lesson: `lesson delete`

Deletes a regular lesson from the weekly schedule. Use `lesson list` first to see the displayed lesson indices.

Format: `lesson delete LESSON_INDEX`

* `LESSON_INDEX` must be a positive whole number without leading zeroes.
* The lesson index is the number shown by `lesson list`, rather than a student index.
* The schedule includes lessons belonging to students hidden by `search` or `filter`. These lessons keep their global lesson indices.
* An index beyond the schedule, including a positive whole number too large to represent, gives `The lesson index provided is invalid.`
* The remaining lessons keep their weekly order and are renumbered.
* If there are no lessons, TutorFlow reports `There are no regular lessons to delete.`
* Deletion cannot be undone.

Example: `lesson delete 2`

On the default sample data, this deletes Priya Nair's lesson and shows:

```text
Deleted regular lesson:
Priya Nair (82345671); CHEMISTRY; SAT; 15:00-16:30
```

### Listing regular lessons: `lesson list`

Lists every student's regular lessons in one weekly schedule, or shows just one displayed student's lessons.

Format: `lesson list [STUDENT_INDEX]`

Use `lesson list 1` to see the first displayed student's regular lessons. After a search or sort, use the student's current displayed index. The output names the student and phone and keeps the full schedule's lesson numbers, so `lesson edit` and `lesson delete` still target the same lessons. For example, one student's lessons may be numbered 1 and 3 because another student owns lesson 2. The displayed roster and saved data remain unchanged. A student without lessons receives `No regular lessons found for NAME (PHONE).`; an index outside the displayed list is rejected.

* Lessons are ordered from Monday to Sunday, then by start time.
* Lesson numbers use the digits 0 to 9 in every locale, so they can be reused directly in edit/delete commands.
* Lessons with the same weekday and start time are ordered by student name.
* Each row shows the student name and phone, subject, abbreviated weekday and time range. The phone distinguishes students who share a name; lesson add, edit, delete and clash feedback use the same owner details.
* If no lessons have been added, TutorFlow reports `No regular lessons found.`

Example output:

```text
Listed 2 regular lessons.
1. Ethan Lim (91234567); MATH; MON; 17:00-18:30
2. Priya Nair (82345671); CHEMISTRY; SAT; 15:00-16:30
```

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

TutorFlow saves your students, homework and regular lessons automatically after every command that changes them. You do not need to save manually.

* Commands that do not change the data, such as `filter`, `sort`, `search`, `list`, `homework list`, `homework upcoming`, `lesson list`, `help` and `exit`, do not write to the data file.
* If TutorFlow cannot save the data, the command is cancelled: its changes are undone and the error names the data file and the likely cause, e.g. `Could not save data/tutorflow.json: the data folder is read-only. Nothing was changed.` The homework and lesson commands, `edit` and `delete` say which data was not changed, e.g. `The homework could not be added because TutorFlow could not save data/tutorflow.json: the data folder is read-only. No homework data was changed.`
* The causes TutorFlow recognizes are: `it is a folder, not a file` (a folder named `tutorflow.json` was put in the `data` folder while TutorFlow is open; if the folder is already there when TutorFlow starts, TutorFlow cannot load the data and shows an error instead), `the file is read-only`, `the file cannot be read (no permission)` (the data file may be written but not read), `the data folder is read-only`, `the home folder is read-only` (the folder containing the JAR, when it has no `data` folder yet), `the folder FOLDER is read-only` (`data/tutorflow.json` is a symbolic link to a file in the read-only folder `FOLDER`, or the home folder was moved or deleted while TutorFlow is open and `FOLDER`, the folder that held it, is read-only; `FOLDER` is shown from the home folder if it is inside it, e.g. `the folder synced is read-only`, and as its full path otherwise), `the folder FOLDER does not exist` (`data/tutorflow.json` is a symbolic link to a file in the folder `FOLDER`, which does not exist), `data links to a folder that does not exist` (the `data` folder is a symbolic link to a folder that was moved or deleted), `a file named data is in the way of the data folder` (a file, not a folder, named `data` is next to the JAR), and `the lock file data/tutorflow.json.lock is read-only` or `the lock file data/tutorflow.json.lock is a folder, not a file` (see [Editing the data file](#editing-the-data-file) for the lock file). Fix the cause, e.g. make the file or folder writable again, and repeat the command. For any other problem, the error shows the system's description of it. On Windows, the file is shown as `data\tutorflow.json`.
* If another copy of TutorFlow or an editor changes the data file, a stale session refuses to overwrite it. Restart TutorFlow to load the latest data before making more changes. If another copy is saving at the same moment, wait for it to finish and try again.
* In the first case, the next command that changes the data is refused with `Another program or copy of TutorFlow changed data/tutorflow.json after this copy last loaded or saved it. Restart TutorFlow before making changes. No data was changed.` In the second case, it is refused with `Another copy of TutorFlow is saving data/tutorflow.json. Try again. No data was changed.`
* If `data/tutorflow.json` is a symbolic link (a shortcut that the system follows, made with e.g. `ln -s`) to a file elsewhere, such as in a synced folder, TutorFlow loads that file and saves to it, and the link stays in place. If the file the link points to does not exist, TutorFlow starts with the sample students, and its first save creates that file and keeps the link. The same happens if the file is deleted while TutorFlow is open: the next save writes the file again. If the folder the link points into does not exist, saving fails with a cause such as `the folder gone does not exist`, and the link is kept. If the `data` folder itself is a symbolic link to a folder that was moved or deleted, TutorFlow starts with the sample students, and saving fails with the cause `data links to a folder that does not exist`, e.g. `The student could not be deleted because TutorFlow could not save data/tutorflow.json: data links to a folder that does not exist. No student data was changed.`
* If the data file is deleted while TutorFlow is open, the next command that changes the data saves all the current data to a new data file. If TutorFlow started without a data file and another copy of TutorFlow then creates one, the next command that changes the data is refused with `Another program or copy of TutorFlow created data/tutorflow.json after this copy started. Restart TutorFlow before making changes. No data was changed.`

### Editing the data file

TutorFlow data is saved automatically as `data/tutorflow.json` inside the folder containing the JAR. Preferences (`preferences.json`) and log files (`addressbook.log.*`) stay in that same home folder. Launching the JAR by its full path from a different terminal folder still uses the files beside the JAR. The status bar shows the data path (widen the window to see a long path). When it saves, TutorFlow also keeps an empty file named `tutorflow.json.lock` in the data folder, which stops two copies of TutorFlow from saving at the same time. If `data/tutorflow.json` is a symbolic link, this lock file is next to the file the link points to instead, e.g. `~/Shared/tutorflow.json.lock` for a link to `~/Shared/tutorflow.json`, so copies that save the same file through different links also take turns. It holds no data; leave it there.

To move TutorFlow, close it and copy the JAR together with its `data` folder and `preferences.json` to the new folder. If you previously launched v1.4 from a different terminal folder, copy your saved files from that folder into the folder containing the JAR before starting v1.5; back up both locations first. The JSON structure is unchanged, but saved records must satisfy v1.5's validation rules. In particular, students with the same name and equivalent local/+65 phone numbers are duplicates; resolve these in v1.4 before upgrading. If a file is rejected, TutorFlow preserves it and reports the reason.

Advanced users may edit this JSON file while TutorFlow is closed. Back it up first.

`preferences.json` holds only the window size and position. If it cannot be read, for example because it is not valid JSON, or it holds `"guiSettings" : null`, TutorFlow opens with the default window size and position and saves the default preferences over that file. TutorFlow does not check the window width and height themselves: a width or height of `null` or `"NaN"` makes only that side open at its minimum (600 wide or 450 high), and the other side keeps its saved size. If TutorFlow cannot write `preferences.json`, for example because it is read-only or is a folder, the window size and position are not remembered for the next start. Your student data is not affected.

If the data file does not exist when TutorFlow starts, for example the first time you run it, TutorFlow starts with some sample students and saves them to a new data file after the first command that changes the data. To get the sample students back, close TutorFlow, delete the data file and start TutorFlow again.

The data file holds a list of `students`. Each student can hold `homeworks` and `lessons`. For example:

```json
{
  "students" : [ {
    "name" : "Ethan Lim",
    "academicLevel" : "S3",
    "subjects" : [ "MATH", "PHYSICS" ],
    "phone" : "91234567",
    "guardianName" : "Grace Lim",
    "guardianPhone" : "98765432",
    "homeworks" : [ {
      "title" : "Quadratic equations worksheet",
      "subject" : "MATH",
      "dueDate" : "2026-11-03",
      "score" : null,
      "status" : "ASSIGNED"
    } ],
    "lessons" : [ {
      "subject" : "MATH",
      "dayOfWeek" : "MONDAY",
      "startTime" : "17:00",
      "endTime" : "18:30"
    } ]
  } ]
}
```

When editing the file, follow these rules:

* Students initially appear in file order. Search and filter show matching students in that order; `sort` temporarily reorders the displayed students without changing the file. Use `list` to return to the full roster in file order.
* Each student needs `name`, `academicLevel`, `subjects`, `phone`, `guardianName` and `guardianPhone`, with the same rules as in [`add`](#adding-a-student-add). `subjects` is a list with one subject per entry. No two students may have the same name (ignoring case) and the same `phone`; a local number and the same number with `+65` count as the same phone.
* `homeworks` may be left out, or written as `[ ]`, for a student with no homework.
* `lessons` may be left out, or written as `[ ]`, for a student with no regular lessons. Each lesson has `subject`, `dayOfWeek`, `startTime` and `endTime`, for example `{"subject":"MATH","dayOfWeek":"MONDAY","startTime":"16:00","endTime":"18:00"}`. The same subject, weekday, time and overlap rules as `lesson add` apply. Older files without `lessons` continue to work.
* Each homework needs `title`, `subject` and `dueDate`. `title` follows the same rules as in [`homework add`](#adding-homework-to-a-student-homework-add), and an invalid title gives the same reason, e.g. `Homework title must be at most 100 characters, but has 101.`, except that an empty title gives `Homework title must be 1 to 100 characters and may contain only letters, numbers, punctuation, symbols and spaces.` `subject` must be one of the student's `subjects`.
* `dueDate` must be a real calendar date in `YYYY-MM-DD` format with a year from 2000 to 2099, such as `2026-11-03`. The month and day may have 1 or 2 digits: `2026-1-5` loads as 2026-01-05. The short `MM-DD` form is not accepted in the data file, as TutorFlow never guesses the year of a saved date.
* `score` is text in the same format as `SCORE` in [`homework marks`](#recording-homework-marks-homework-marks): the marks on their own, such as `"17"`, or `MARKS_OBTAINED/TOTAL_MARKS`, such as `"5/10"`. Use `null` (or leave `score` out) if the homework has no score. A score saved by an older version of TutorFlow as a number without quotes, such as `85`, loads as the score `85` and is saved as `"85"` the next time TutorFlow saves. A number that is not a valid score, such as `85.5` or `-3`, or that is written with a decimal point or an exponent, such as `85.0` or `1e2`, is ignored: the homework loads with no score (`Not recorded`) and the rest of the file loads as usual. The next command that changes the data saves this homework with `"score" : null`, so the invalid number is removed from the file; back up the file first if you want to keep it. Invalid text, such as `"85.5"`, `"abc"` or the empty text `""`, makes the data file invalid, and so does a value that is neither text nor a number, such as `true`, `[5]` or `{}`. Both give the reason `Score must be whole marks such as 17, or marks out of a total such as 5/10, with no signs, decimals, spaces or other characters.`, or the rule that failed, as in `homework marks`.
* A student cannot have two homework records with the same title (ignoring case), subject and due date.
* Values such as `math` or `s3` are accepted in any case, and extra spaces in names and titles are removed, with each line break or tab in them read as a space. TutorFlow writes them in its own format the next time it saves.
* Spell every key exactly as shown above. A misspelled key that may be left out (`homeworks`, `lessons`, `score` or `status`) is ignored without an error: for example, with `homework` instead of `homeworks`, the student loads with no homework, and the next command that changes the data removes that homework from the file. A misspelled required key, such as `phne` instead of `phone`, makes the data file invalid (see the caution below).
* `status` is `"ASSIGNED"` or `"COMPLETED"`, in any case (`"completed"` is accepted) and with any spaces around it ignored, independent of `score`. Older files that include this key retain their explicit status. Missing or `null` status loads as `ASSIGNED`, including for scored homework; update historical records with `homework status` when needed. An invalid explicit status makes the data file invalid.
* Keys that TutorFlow does not use are ignored and removed the next time TutorFlow saves.
* Save the file as UTF-8 text, with or without a byte order mark. A file in another encoding that holds a non-ASCII character, such as a title with `é` saved as Windows-1252, usually makes the data file invalid, and the reason gives the place of the first character that is not UTF-8 (see the caution below); TutorFlow does not guess the encoding or replace such characters. A file saved as UTF-16 is invalid too. A few such characters happen to form valid UTF-8 and load as different text: for example, `Ã©` saved as Windows-1252 loads as `é`.
* The file holds one JSON object, from the first `{` to its matching `}`. Anything after that `}` other than spaces and line breaks, such as a second `{ ... }` object, makes the data file invalid; TutorFlow does not merge it into the roster.
* Each key appears at most once in an object. A repeated key, such as a second `"students"` key or a student with two `"phone"` keys, makes the data file invalid; TutorFlow does not pick one of the values.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
If the data file cannot be read or contains invalid data, TutorFlow shows an error with the full file path and the reason, and stops before opening the roster. A reason about one student or homework starts with its position in the file and its name or title, e.g. `Reason: Student 1 (Ethan Lim): Homework 1 (Quadratic equations worksheet): 10-15 is not in YYYY-MM-DD format, e.g. 2026-10-30.`, and a duplicate names the earlier record, e.g. `Reason: Student 2 (ETHAN LIM) has the same name and phone number as student 1.` An invalid student value is named by its key, e.g. `Reason: Student 1 (Ethan Lim): guardianPhone: Phone numbers must be an 8-digit Singapore number starting with 6, 8, or 9, or an international number in +<country code><number> format.`, and an invalid `subjects` list says what is wrong with it, e.g. `Reason: Student 1 (Ethan Lim): subjects: "MATH, PHYSICS" holds more than one subject. Subjects must be a list of one or more of MATH, PHYSICS and CHEMISTRY, each in its own quotes and listed once, e.g. ["MATH", "PHYSICS"].` An entry with one subject and a stray comma, such as `"MATH,"`, is not a subject: it gives `Reason: Student 1 (Ethan Lim): subjects: "MATH," is not a subject. Subjects must be a list of one or more of MATH, PHYSICS and CHEMISTRY, each in its own quotes and listed once, e.g. ["MATH", "PHYSICS"].` A reason about a lesson gives its position and its day and times as written in the file, e.g. `Reason: Student 1 (Ethan Lim): Lesson 1 (MOONDAY 17:00-18:30): Day must be one of: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY.`, and overlapping lessons name both lessons, also as written, e.g. `Reason: Student 2 (Priya Nair): Lesson 2 (monday 17:00-19:00) overlaps lesson 1 (MONDAY 17:00-18:30) of student 1 (Ethan Lim).` A lesson without one of its keys names the first missing key, in the order `subject`, `dayOfWeek`, `startTime`, `endTime`, e.g. `Reason: Student 1 (Ethan Lim): Lesson 1: Lesson's startTime field is missing!` A student, homework or lesson written as `null` gives its position, e.g. `Reason: Student 1 (Ethan Lim): Homework 1 is empty (null).` For a file that is not valid JSON, the reason gives the place where reading stopped, e.g. `Reason: The file is not valid JSON near line 1, column 13.` For valid JSON with a value of the wrong kind, such as a list or object where text is expected, or text or a number where a list is expected, the reason is e.g. `Reason: The file has a value of the wrong type near line 1, column 14.` for the file `{"students": 5}`. A number, `true` or `false` where text is expected is read as text, so `"title": 42` gives the title `42`. For a file that is empty or holds only spaces and line breaks, it is `Reason: The file is empty.` For a file that is not UTF-8 text, the reason gives the place of the first character that is not UTF-8, e.g. `Reason: The file is not valid UTF-8 text near line 3, column 22. Save the file as UTF-8.` when `Ethan Lim` is changed to `Ethan Lém` and the file is saved as Windows-1252. A repeated key gives e.g. `Reason: The key "phone" is repeated near line 32, column 27.`, and content after the JSON data gives e.g. `Reason: The file has unexpected content after the JSON data near line 121, column 2.` If `tutorflow.json` is a folder, the reason is `Reason: It is a folder, not a file.`, and without read permission it is `Reason: The file cannot be read (no permission).` or `Reason: The data folder cannot be read (no permission).` The original data file remains unchanged. Close the error dialog, restore a valid backup or correct the file, and restart from the same home folder. To start with sample data instead, move the original file to a safe location before restarting; keep it if you need to recover records.<br>
Edit the data file only if you are confident that you can update it correctly: a value that breaks any rule above stops TutorFlow from opening the file, and a misspelled optional key or an invalid numeric score is dropped the next time TutorFlow saves.
</div>

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Close TutorFlow on both computers and copy the `data` folder from the old TutorFlow folder into the new one, beside the JAR.

**Q**: How do I start with an empty student list instead of the sample students?<br>
**A**: Run `clear`. It deletes all students, including the sample students, and the empty list is saved.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.
3. **If the JAR file is in a folder whose name ends with `!`**, such as `TutorFlow!`, or inside such a folder at any depth, such as `Work!/TutorFlow`, TutorFlow cannot start: no window opens, and an error is shown if it was launched from a terminal. This is a limit of Java on JAR file paths. The remedy is to move the JAR file, together with its `data` folder and `preferences.json`, to a folder with no `!` at the end of its name or of any folder above it.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action | Format, Examples
--------|------------------
**Add** | `add n/STUDENT_NAME l/ACADEMIC_LEVEL s/SUBJECTS p/STUDENT_PHONE gn/GUARDIAN_NAME gp/GUARDIAN_PHONE` <br> e.g., `add n/Aaron Koh l/S2 s/MATH p/91112222 gn/Koh Mei Hua gp/98887777`
**List** | `list` or `ls`
**Edit student** | `edit INDEX [n/STUDENT_NAME] [l/ACADEMIC_LEVEL] [s/SUBJECTS] [p/STUDENT_PHONE] [gn/GUARDIAN_NAME] [gp/GUARDIAN_PHONE]`<br> e.g., `edit 1 l/S4 p/91234568`
**Search** | `search KEYWORD`<br> e.g., `search Tan`
**Filter** | `filter [l/LEVEL] [s/SUBJECT] [d/DAY]`<br> e.g., `filter l/S4 s/Physics` or `filter d/MONDAY`
**Sort** | `sort by/KEY [o/ORDER]`<br> e.g., `sort by/level`, `sort by/day o/desc`
**Delete student** | `delete INDEX`<br> e.g., `delete 2`
**Clear** | `clear`
**Homework add** | <code>homework&#124;hw add STUDENT_INDEX title/&#124;t/TITLE s/SUBJECT due/YYYY-MM-DD&#124;MM-DD</code><br> e.g., `homework add 1 title/Complete algebra worksheet s/MATH due/2026-12-01`, `hw add 1 t/"Speed in km/h" s/PHYSICS due/2026-12-01`
**Homework list** | <code>homework&#124;hw list&#124;ls STUDENT_INDEX [all]</code><br> e.g., `homework list 1`, `hw ls 2 all`
**Homework upcoming** | <code>homework&#124;hw upcoming&#124;up [due/DAYS]</code><br> e.g., `hw up`, `hw up due/3`
**Homework edit** | <code>homework&#124;hw edit&#124;e STUDENT_INDEX HOMEWORK_INDEX [title/&#124;t/TITLE] [s/SUBJECT] [due/YYYY-MM-DD&#124;MM-DD]</code><br> e.g., `hw edit 1 2 t/Revise mechanics s/PHYSICS due/2026-11-10`
**Homework marks** | <code>homework&#124;hw marks&#124;m STUDENT_INDEX HOMEWORK_INDEX score/[SCORE]</code><br> e.g., `hw m 1 2 score/5/10`, `hw m 1 2 score/17`, `hw m 1 2 score/`
**Homework delete** | <code>homework&#124;hw delete&#124;del STUDENT_INDEX HOMEWORK_INDEX</code><br> e.g., `homework delete 1 2`
**Homework status** | <code>homework&#124;hw status STUDENT_INDEX HOMEWORK_INDEX status/STATUS</code><br> e.g., `hw status 1 2 status/COMPLETED`, `hw status 1 2 status/ASSIGNED`
**Homework filter** | <code>homework&#124;hw filter [STUDENT_INDEX] [s/SUBJECT] [due/DATE] [status/STATUS]</code><br> e.g., `hw filter 1 s/MATH due/2026-10-15 status/ASSIGNED`
**Lesson add** | `lesson add STUDENT_INDEX s/SUBJECT d/DAY st/START_TIME et/END_TIME`<br> e.g., `lesson add 1 s/MATH d/TUESDAY st/16:00 et/18:00`
**Lesson list** | `lesson list [STUDENT_INDEX]`<br> e.g., `lesson list 1`
**Lesson edit** | `lesson edit LESSON_INDEX [s/SUBJECT] [d/DAY] [st/START_TIME] [et/END_TIME]`<br> e.g., `lesson edit 1 d/TUESDAY`
**Lesson delete** | `lesson delete LESSON_INDEX`<br> e.g., `lesson delete 2`
**Help** | `help`
**Exit** | `exit`
