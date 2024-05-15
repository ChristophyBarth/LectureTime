# LectureTime

LectureTime is a robust and user-friendly offline app designed to streamline the scheduling and management of lectures for both students and lecturers. Built with Kotlin, the app incorporates advanced Android development technologies such as Kotlin Coroutines for efficient background processing, Room Database for secure local storage, and Hilt for seamless dependency injection. Work Manager ensures the timely delivery of notifications, while View Model and Navigation Graphs enhance the app's responsiveness and user navigation. Users can effortlessly add, manage, and receive reminders for their lectures without needing an internet connection, making it an indispensable tool for maintaining punctuality and organization in academic settings.

## Features

- **Personalized Lecture Schedule**
  - Easily add and manage lecture schedules.
  - Set recurring lectures for the semester.

- **Timely Reminders**
  - Receive notifications 5 minutes before lectures start (Not implemented yet) to help you prepare and arrive on time.

- **Offline Functionality**
  - Access your schedules and reminders without an internet connection.

## Tech Stack

- **Kotlin Coroutines**
  - Efficient background processing for smooth, responsive UI interactions.
  
- **Room Database**
  - Local storage ensures data persistence and fast access.

- **Hilt Dependency Injection**
  - Simplifies dependency management for clean, maintainable code.

- **Work Manager**
  - Schedules and manages background tasks, ensuring timely reminders.

- **View Model**
  - Manages UI-related data in a lifecycle-conscious way, ensuring data survives configuration changes.

- **Navigation Graphs**
  - Simplifies app navigation for an intuitive user experience.

## Screenshots

![Home Screen](screenshots/home_screen.png)
![Add Lecture](screenshots/add_lecture.png)
![Lecture List](screenshots/lecture_list.png)

## Getting Started

### Prerequisites

- Android Studio (latest version recommended)
- Kotlin (latest version recommended)
- A physical device or an emulator

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/LectureTime.git
2. Open the project in Android Studio.
3. Build and run the app on an emulator or physical device.

### Usage

1. **Add Lecture**: Click the 'Add Lecture' button to input your lecture details.
2. **View Schedule**: Navigate to the 'Lecture List' to see all your scheduled lectures.
3. **Receive Reminders**: Get notified before your lectures start.

## Contributing

Contributions are welcomed! Please follow these steps:

1. Fork the repository.
2. Create a new branch: `git checkout -b feature/YourFeature`
3. Commit your changes: `git commit -m 'Add your feature'`
4. Push to the branch: `git push origin feature/YourFeature`
5. Open a pull request.

## Acknowledgements

- [Jetpack Components](https://developer.android.com/jetpack)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- [Work Manager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Navigation Component](https://developer.android.com/guide/navigation)

## Contact

If you have any questions or feedback, please open an issue or reach out to me at [christopherbartholomewokeke@gmail.com](mailto:christopherbartholomewokeke@gmail.com).

---

*LectureTime* - Never miss a lecture again!
