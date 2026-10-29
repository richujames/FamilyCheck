# FamilyCheck

A beginner-friendly Java/XML Android project that helps older adults pause, record a suspicious interaction, and ask someone they trust.

## Open the correct project

In Android Studio, choose **File > Open** and select **this FamilyCheck folder**, containing `settings.gradle`. The parent `Project` folder contains the separate course-list lab app.

1. Let Gradle sync finish. The project uses the existing machine's AGP 9.3.2, Gradle 9.5.0 and SDK 36.1 configuration.
2. Select Android Studio's bundled JDK in Gradle settings.
3. Select an API 26+ emulator or a USB-debugging Android phone.
4. Click **Run**. On Android 13+, grant notification permission when using a reminder.
5. Generate a debug APK from Android Studio's Build menu, or run `gradlew.bat assembleDebug lintDebug`.

The APK output is `app/build/outputs/apk/debug/app-debug.apk`. On another computer, let Studio recreate `local.properties` with that computer's SDK path.

The application ID is `org.familycheck.senior`; it does not replace the course-list lab app (`org.familycheck.app`).

## What's included

- Navy, teal and white design with rounded cards, four bottom tabs and a left navigation panel.
- **Home:** start a check, see the number awaiting follow-up, call the first favourite contact and launch practice.
- **Checks:** search messages and notes, filter by outcome, change date order, open details and long-press to delete.
- **Guided check:** choose the source, describe the concern and select warning signs. Accepts shared text from other apps.
- **Details:** editable sharing preview, trusted-person navigation, outcome and note, reminder link and confirmed deletion.
- **People:** add/edit/remove contacts, optional relationship, favourites, search, dialer and SMS composer. Touch and hold for a context menu.
- **Learn:** six short awareness cards in a GridView and links to FTC scam guidance.
- **Practice:** five offline scenarios, answer explanations, score and retry. Progress survives screen rotation.
- **Reading settings:** a larger-text preference saved on the phone; system font scaling also applies.
- **Reminder:** date/time pickers, one scheduled follow-up, cancellation, notification settings and an immediate test notification.
- **Local persistence:** SQLite for checks/contacts; SharedPreferences for reading settings/reminder time.

## Syllabus mapping

| Unit | Concept | Where to demonstrate it |
|---|---|---|
| 1 | Project structure, Java, API levels, APK | Gradle files, manifest, Java/res folders, Android Studio Run/Build |
| 2 | Views, layouts, drawables, input events | `Ui`, XML shell/row layouts, rounded cards and form buttons |
| 2 | ListView and custom adapter | Checks/People tabs, `CardAdapter` and `list_card.xml` |
| 2 | GridView | Learn tab (one column on phones, two on wider screens) |
| 2 | Action bar, options menu and submenu | Toolbar > Help & settings |
| 2 | Context menu | Touch and hold a check or contact |
| 2 | Spinner | Source selection and outcome filter |
| 2 | DatePicker and TimePicker | Follow-up reminder |
| 2 | Activity and Fragment lifecycle | `MainActivity`, `DashboardFragment`, saved instance state, database cleanup |
| 3 | Explicit Intents and extras | Check to Detail with case ID; navigation with tab index |
| 3 | Implicit Intents | Share chooser, received text, dialer, SMS composer, browser and settings |
| 3 | Navigation drawer | Toolbar navigation button opens the left panel |
| 3 | AlertDialog and Toast | Contact editing, deletion, explanations and save feedback |
| 3 | NotificationManager | Test notification and scheduled reminder, channel and permission handling |

Remote **push notifications** are not included: this version has local notifications and no server/Firebase account. A blocking ProgressDialog is not added because the app has no long-running network operation. These are scope limits, not implemented features.

Platform `android.app.Fragment` is used to avoid extra dependencies and match the basic course approach. It is a legacy API; an expanded production app should use AndroidX. The navigation bar and drawer are implemented from standard views.

## Code tour

- `MainActivity`: toolbar, navigation, drawer and settings.
- `DashboardFragment`: the four tab screens; refreshes data in `onResume` and closes its database in `onDestroy`.
- `Ui` / `BaseActivity`: shared styling and screen construction.
- `Db`: schema, parameterised inserts/updates/deletes and cursor cleanup.
- `CardAdapter`: inflates and recycles custom XML rows.
- `ContactDialog`: validates and saves contact details; preserves the form on rotation.
- `CheckActivity` / `DetailActivity`: the concern-to-outcome workflow.
- `Lessons` / `QuizActivity`: awareness content and practice.
- `ReminderActivity` / `ReminderReceiver`: picker, permission, alarm and notification flow.

The logic uses Activities, a Fragment, listeners, basic collections, SQLite and standard widgets. No external UI libraries, AI services or backend are required.

## Classroom demo

1. Add a fictional contact with your own test number and mark it as a favourite.
2. Create a check: "Your account will close today. Send money now. Do not tell anyone."
3. Select the pressure/secrecy signs, save and show the case-ID Intent opening Detail.
4. Open the sharing preview, edit it and cancel the chooser without sending anything.
5. Open the People tab and dialer/SMS composer; cancel before calling or sending.
6. Save an outcome, then demonstrate search, filter and sorting.
7. Long-press a row to show the context menu. Show the toolbar submenu and navigation drawer.
8. Answer a practice question and show its explanation. Rotate to show saved state.
9. Enable larger text and return to the same tab.
10. Choose a reminder date/time and use **Send a test notification**. Tap the notification to open Checks.

## Manual acceptance checks

- Reject blank concerns and invalid contacts; enforce 5,000-character concerns and 2,000-character notes.
- Verify input survives rotation in Check, Detail and the contact dialog.
- Verify favourites sort before other contacts and Home calls the first favourite (or first contact if none are favourites).
- Verify the new case and changed outcome appear after returning to Checks.
- Try empty results, deletion of the last row, Unicode, apostrophes and a long list.
- Verify independently checked status requires a note.
- Verify cancelled sharing/calling/SMS does not claim delivery or change an outcome.
- Try a device with no compatible external app; a friendly Toast should appear.
- Test large system text, larger-text mode, landscape orientation and a tablet.
- Try notification permission denied, permission granted and channel disabled.
- Reject past reminder times; schedule, replace and cancel a reminder.
- Reboot after scheduling and verify restoration. Timing is approximate and device-dependent.
- Test incoming text via another app's Android Share menu.

## Privacy and limitations

Data stays in the app's private storage unless the user chooses to share. Backups are disabled. This app does not read SMS, monitor calls or access the phone's address book. Stored data is not additionally encrypted by the app. Uninstalling removes local records.

Dialing and SMS sharing open another app; the user completes the action. Family replies do not sync back. A selected outcome is the user's assessment, and no selected warning signs does not prove genuineness.

There is one general follow-up reminder, not one per case. Android may delay it for battery management. Reboot restoration is included, but force-stop and disabled notifications can prevent delivery. This is not an emergency service or automatic fraud detector.

Awareness reference: https://consumer.ftc.gov/scams

## Verification status

Java syntax parsing passed for all 13 source files. All six XML files parse; Java resource references and manifest component names were checked. SQLite schema and CRUD smoke checks passed, including favourite ordering, Unicode and apostrophes. These checks do not establish Android type correctness or runtime behaviour.

APK compilation and lint were attempted but blocked before Java compilation: this session cannot read the installed SDK metadata at `platforms/android-36.1/package.xml` (Access denied). Running `adb.exe` is also denied, so emulator/device behaviour and visual appearance have not been verified. No working APK is claimed. Run Gradle sync, build and the manual checks in Android Studio before submitting.

