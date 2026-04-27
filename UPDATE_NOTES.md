# Update Notes (Production Readiness + UI/UX)

These notes explain what changed in simple terms.

## 1) Safer File Moving

- Files are no longer overwritten by default.
- If a file with the same name already exists, the app renames the new file (example: `report (1).pdf`).
- This helps prevent accidental data loss.

## 2) Better Rule Loading and Saving

- Loading rules no longer causes extra saves in the background.
- Rule values now safely handle commas and backslashes.
- App data folder handling is safer when system variables are missing.

## 3) Clearer Organise Results

- The app now tracks:
  - how many files were moved,
  - how many were skipped,
  - how many failed.
- Dashboard status shows a clear summary after each run.

## 4) UI No Longer Freezes During Run

- Organising now runs in a background task.
- A progress indicator appears while work is running.
- Key controls are disabled during run to avoid accidental clicks.

## 5) UI/UX Improvements

- `Run` stays disabled until a folder is selected.
- Rule form validates folder paths before saving.
- Added better labels, tooltips, and clearer action names.
- Added confirmations for destructive actions.
- Added consistent status styles: ready, success, warning, and error.

## 6) Consistent Styling

- Added a shared stylesheet: `src/main/resources/app.css`.
- Buttons now have clear visual types:
  - primary,
  - secondary,
  - danger.
- Window sizing is more flexible for better readability.

## 7) Testing and Dependency Cleanup

- Replaced placeholder tests with real tests for core behavior.
- Removed unused heavy dependencies.
- Upgraded JUnit test dependency version.

## 8) Release Checklist Added

- `README.md` now includes a production release checklist for final verification.

---

If you are new to this codebase, start with:
- `src/main/java/com/automation/FileOrganiser.java`
- `src/main/java/com/automation/UIController.java`
- `src/main/resources/MainView.fxml`
- `src/main/resources/app.css`
