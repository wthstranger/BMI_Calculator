# Implementation Plan - BMI Track Login Screen

Create a professional Login Screen for the BMI Tracking application with Firebase Authentication and Cloud Firestore integration.

## User Review Required

> [!IMPORTANT]
> This plan converts the project to use **Jetpack Compose** for a modern, professional UI as requested.
> It also introduces **Hilt** for Dependency Injection to maintain a clean architecture.
> Please ensure that the `google-services.json` provided is correctly configured for your Firebase project.

## Proposed Changes

### Build Configuration & Dependencies

#### [MODIFY] [libs.versions.toml](file:///C:/Users/ASUS/Desktop/Android project/BMI_Calculator/gradle/libs.versions.toml)
- Add Kotlin, Compose, Firebase, Hilt, and Navigation dependencies.

#### [MODIFY] [build.gradle.kts (Root)](file:///C:/Users/ASUS/Desktop/Android project/BMI_Calculator/build.gradle.kts)
- Add Kotlin and Hilt plugins.

#### [MODIFY] [build.gradle.kts (App)](file:///C:/Users/ASUS/Desktop/Android project/BMI_Calculator/app/build.gradle.kts)
- Enable Compose.
- Apply Kotlin, Hilt, and Google Services plugins.
- Add all necessary implementation dependencies.

### Domain Model

#### [NEW] [UserModel.kt](file:///C:/Users/ASUS/Desktop/Android project/BMI_Calculator/app/src/main/java/com/wthstranger/bmi_calculator/model/UserModel.kt)
- Data class for user profile information.

### Data Layer (Firebase)

#### [NEW] [AuthRepository.kt](file:///C:/Users/ASUS/Desktop/Android project/BMI_Calculator/app/src/main/java/com/wthstranger/bmi_calculator/data/auth/AuthRepository.kt)
- Interface for authentication operations.

#### [NEW] [FirebaseAuthRepository.kt](file:///C:/Users/ASUS/Desktop/Android project/BMI_Calculator/app/src/main/java/com/wthstranger/bmi_calculator/data/auth/FirebaseAuthRepository.kt)
- Firebase implementation of AuthRepository.

#### [NEW] [UserRepository.kt](file:///C:/Users/ASUS/Desktop/Android project/BMI_Calculator/app/src/main/java/com/wthstranger/bmi_calculator/data/firestore/UserRepository.kt)
- Interface for Firestore operations.

#### [NEW] [FirestoreUserRepository.kt](file:///C:/Users/ASUS/Desktop/Android project/BMI_Calculator/app/src/main/java/com/wthstranger/bmi_calculator/data/firestore/FirestoreUserRepository.kt)
- Firestore implementation of UserRepository.

### Dependency Injection

#### [NEW] [AppModule.kt](file:///C:/Users/ASUS/Desktop/Android project/BMI_Calculator/app/src/main/java/com/wthstranger/bmi_calculator/di/AppModule.kt)
- Hilt module to provide Firebase instances and Repositories.

#### [NEW] [BMIApp.kt](file:///C:/Users/ASUS/Desktop/Android project/BMI_Calculator/app/src/main/java/com/wthstranger/bmi_calculator/BMIApp.kt)
- Application class for Hilt initialization.

### Presentation Layer (Login)

#### [NEW] [LoginState.kt](file:///C:/Users/ASUS/Desktop/Android project/BMI_Calculator/app/src/main/java/com/wthstranger/bmi_calculator/presentation/login/LoginState.kt)
- UI state for the Login screen.

#### [NEW] [LoginViewModel.kt](file:///C:/Users/ASUS/Desktop/Android project/BMI_Calculator/app/src/main/java/com/wthstranger/bmi_calculator/presentation/login/LoginViewModel.kt)
- ViewModel handling login logic and interacting with repositories.

#### [NEW] [LoginScreen.kt](file:///C:/Users/ASUS/Desktop/Android project/BMI_Calculator/app/src/main/java/com/wthstranger/bmi_calculator/presentation/login/LoginScreen.kt)
- Composable UI for the Login screen.

### Navigation & Entry Point

#### [NEW] [AppNavigation.kt](file:///C:/Users/ASUS/Desktop/Android project/BMI_Calculator/app/src/main/java/com/wthstranger/bmi_calculator/navigation/AppNavigation.kt)
- Compose Navigation setup.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/ASUS/Desktop/Android project/BMI_Calculator/app/src/main/java/com/wthstranger/bmi_calculator/MainActivity.kt)
- Convert to Kotlin and set up Compose content with Hilt.

## Verification Plan

### Automated Tests
- Build the project to ensure all dependencies are correctly resolved.
- Run unit tests for LoginViewModel (if time permits).

### Manual Verification
- Deploy to an Android device/emulator.
- Verify the UI layout against the requirements.
- Test Email/Password sign-in flow.
- Test Google Sign-In flow.
- Verify data is correctly saved to Firestore.
- Test Forgot Password flow.
- Test auth state persistence by restarting the app.
