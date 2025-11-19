


## ToDoList — Smart Task Manager for Android 🎯

An elegant, lightweight Android app to capture tasks, set timely reminders, and stay organized with a clean, modern UI. The APK is already available for download.

> Ready to try it? 🚀 [Get the latest build 📥](https://github.com/abdllhdlc/todoapp/releases/tag/v1.0-release)

---

### ✨ Features
- **Create, edit, and delete tasks** with a streamlined flow
- **Reminders and notifications** using exact alarms on supported devices
- **Offline-first local storage** powered by Room
- **Modern UI** built with Jetpack Compose and Material 3
- **Preferences storage** using DataStore
- **Optimized colors** via Palette for a polished look

---

### 🖼️ Screenshots

<div align="center">
<table>
  <tr>
    <td><a href="https://raw.githubusercontent.com/abdllhdlc/todoapp/main/screenshots/Homepage.jpg" target="_blank"><img src="https://raw.githubusercontent.com/abdllhdlc/todoapp/main/screenshots/Homepage.jpg" width="220" alt="Ana Ekran" /></a></td>
    <td><a href="https://raw.githubusercontent.com/abdllhdlc/todoapp/main/screenshots/Add_Reminder.jpg" target="_blank"><img src="https://raw.githubusercontent.com/abdllhdlc/todoapp/main/screenshots/Add_Reminder.jpg" width="220" alt="Hatırlatma Ekle" /></a></td>
    <td><a href="https://raw.githubusercontent.com/abdllhdlc/todoapp/main/screenshots/Sorting_Options_Newest.jpg" target="_blank"><img src="https://raw.githubusercontent.com/abdllhdlc/todoapp/main/screenshots/Sorting_Options_Newest.jpg" width="220" alt="Sıralama Seçenekleri" /></a></td>
    <td><a href="https://raw.githubusercontent.com/abdllhdlc/todoapp/main/screenshots/Theme_Selection.jpg" target="_blank"><img src="https://raw.githubusercontent.com/abdllhdlc/todoapp/main/screenshots/Theme_Selection.jpg" width="220" alt="Tema Seçimi" /></a></td>
  </tr>
  <tr>
    <td><a href="https://raw.githubusercontent.com/abdllhdlc/todoapp/main/screenshots/Existing_Reminders_List.jpg" target="_blank"><img src="https://raw.githubusercontent.com/abdllhdlc/todoapp/main/screenshots/Existing_Reminders_List.jpg" width="220" alt="Mevcut Hatırlatmalar" /></a></td>
    <td><a href="https://raw.githubusercontent.com/abdllhdlc/todoapp/main/screenshots/Task_Detail.jpg" target="_blank"><img src="https://raw.githubusercontent.com/abdllhdlc/todoapp/main/screenshots/Task_Detail.jpg" width="220" alt="Görev Detayı" /></a></td>
    <td><a href="https://raw.githubusercontent.com/abdllhdlc/todoapp/main/screenshots/Task_Detail_File_Selected.jpg" target="_blank"><img src="https://raw.githubusercontent.com/abdllhdlc/todoapp/main/screenshots/Task_Detail_File_Selected.jpg" width="220" alt="Görev Detayı Dosya" /></a></td>
    <td><a href="https://raw.githubusercontent.com/abdllhdlc/todoapp/main/screenshots/Delete.jpg" target="_blank"><img src="https://raw.githubusercontent.com/abdllhdlc/todoapp/main/screenshots/Delete.jpg" width="220" alt="Delete" /></a></td>
  </tr>
</table>
</div>


---

### 📥 Installation

**Option A — Download APK (recommended):**
- Download the latest APK from the project's Releases:
  - [Latest Release](/../../releases/latest)
  - Download and install the app.

**Option B — Build from source:**
1. Clone this repository and open it in Android Studio (Giraffe+).
2. Ensure you have JDK 17 and Android SDK 34 installed.
3. Build and run:
   - From Android Studio: Run ▶ on a device/emulator
   - Or via terminal:
     - macOS/Linux: `./gradlew assembleDebug`
     - Windows: `gradlew.bat assembleDebug`
4. The APK will be in `app/build/outputs/apk/debug/`.

---

### 🛠️ Usage
1. Launch the app and tap the ➕ button to add a task.
2. Set a title, optional notes, and pick a date/time for reminders.
3. Save the task to schedule notifications.
4. Tap a task to edit or long-press to delete.

---

### 💻 Technologies Used
- **Language**: Kotlin
- **UI**: Jetpack Compose, Material 3
- **Data**: Room (SQLite), DataStore (Preferences)
- **Architecture Components**: ViewModel, Lifecycle
- **Utilities**: Gson, Palette
- **AndroidX**: Core KTX, Activity Compose, Navigation Compose

---

### ✉️ Contact / Author
**Author**: Abdullah Delice

For questions, feature requests, or bug reports, please open an Issue on this repository.

---

### ⚖️ License
This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

---

### ℹ️ App Details
- **App name**: ToDoList
- **Package**: `com.abdullah.todoapp`
- **Minimum SDK**: 24
- **Target SDK**: 34
- **Version**: 1.0 (1)
