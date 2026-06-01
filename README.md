# Delta Cleaners

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Firebase](https://img.shields.io/badge/firebase-%23039BE5.svg?style=for-the-badge&logo=firebase)

Delta Cleaners is a modern, full-stack Android application designed to streamline the process of booking professional cleaning services. Whether you're a customer looking for a spotless home, a cleaner partner managing your schedule, or an admin overseeing operations, Delta Cleaners provides a seamless experience for all.

## 🚀 Features

### For Customers
*   **Service Catalog**: Browse various cleaning services like Full House Cleaning, Deep Cleaning, Kitchen Cleaning, etc.
*   **Detailed Insights**: View service inclusions, exclusions, FAQs, and real customer reviews.
*   **Smart Booking**: Customize bookings based on property type (e.g., 1 BHK, 2 BHK) and number of bathrooms.
*   **Real-time Tracking**: Stay updated on booking status through the app and push notifications.
*   **Booking History**: Easily manage past and upcoming appointments.

### For Cleaner Partners
*   **Dashboard**: Dedicated view to manage assigned cleaning tasks.
*   **Status Updates**: Update booking progress in real-time.
*   **Schedule Management**: View upcoming work and history.

### For Admins
*   **Operational Oversight**: Manage users, services, and all bookings across the platform.

## 🛠 Tech Stack

*   **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for a modern, declarative UI.
*   **Architecture**: MVVM (Model-View-ViewModel) following Clean Architecture principles.
*   **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) for robust and testable code.
*   **Backend**: 
    *   **Firebase Authentication**: Secure user login and OTP verification.
    *   **Cloud Firestore**: Real-time NoSQL database for service and booking data.
    *   **Firebase Storage**: For hosting service banners and user assets.
    *   **Firebase Cloud Messaging (FCM)**: Reliable push notifications.
    *   **Firebase Crashlytics**: Real-time crash reporting.
*   **Networking & Image Loading**: [Coil](https://coil-kt.github.io/coil/compose/) for efficient image loading.
*   **Logging**: [Timber](https://github.com/JakeWharton/timber) for extensible logging.
*   **Navigation**: [Compose Navigation](https://developer.android.com/jetpack/compose/navigation) for seamless screen transitions.

## 📱 Build Flavors

The project uses product flavors to manage different user roles within a single codebase:
*   `customer`: The main consumer app.
*   `cleaner`: The partner application for cleaning professionals.
*   `admin`: The administrative dashboard.

## 🏁 Getting Started

### Prerequisites
*   Android Studio Ladybug or newer.
*   JDK 17.
*   A Firebase project with `google-services.json` placed in the `app/src/[flavor]` directory.

### Installation
1.  Clone the repository:
    ```bash
    git clone https://github.com/yourusername/DeltaCleaners.git
    ```
2.  Open the project in Android Studio.
3.  Add your `google-services.json` to the appropriate flavor folders.
4.  Sync Project with Gradle Files.
5.  Select the desired build variant (e.g., `customerDebug`) and run.

## 📂 Project Structure

```text
com.example.deltacleaners
├── data          # Repositories, Models, and Data Sources
├── di            # Hilt Modules
├── domain        # Business logic and Use Cases
├── fcm           # Firebase Cloud Messaging service
├── ui            # UI Screens, Components, and Themes
│   ├── auth      # Authentication flow
│   ├── booking   # Booking process
│   ├── home      # Main home screens
│   ├── history   # Booking history
│   ├── profile   # User profile management
│   └── ...       # Other feature-specific packages
└── DeltaApplication.kt # Application class
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
