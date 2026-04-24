# FileOrganiser (JavaFX)

A simple desktop automation tool that automatically organises files in a folder (like Downloads) based on file type.

Built using JavaFX as a learning project for file automation and UI development.

---

## Features

- Automatically sorts files into folders (Documents, Images, Archives, etc.)
- Uses file extensions to determine categories
- Customisable sorting rules
- Saves user preferences locally
- Keeps a log of moved files
- Toggle option for unknown file types (“Others” folder)
- Reset settings to default

---

## Tech Stack

- Java 21+
- JavaFX 21
- Maven
- Java NIO (file handling)

---

## Screenshot


<img src="screenshots/app-dashboard.png" alt="App Dashboard" width="350"/>
<img src="screenshots/app-rules.png" alt="App Rules" width="350"/>
<img src="screenshots/app-logs.png" alt="App Logs" width="350"/>


---

## What I Learned

- JavaFX UI development
- File handling with Java NIO
- MVC-style separation of logic and UI
- Persistent storage using system AppData directories

---

## How to Run

### Prerequisites
- Java 21 or higher
- Maven installed

### Steps

```bash
git clone https://github.com/bl4-k/FileOrganiser.git
cd FileOrganiser
mvn clean install
mvn javafx:run
```

---

## Production Release Checklist

- Run `mvn clean test` and confirm all tests pass.
- Launch app with `mvn javafx:run` and verify folder selection, run summary, rule add/remove, and log actions.
- Validate safe move behavior by placing duplicate filenames in destination and confirming automatic rename (no overwrite).
- Confirm failure states show clear status messages (invalid folder input, missing selection, file move errors).
- Inspect saved logs in the app data location and verify both clear actions work as intended.
- Review `pom.xml` dependencies before release and keep only required libraries.
