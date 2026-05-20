# **Bicel - Marketplace for Buying & Selling Items**

## Overview
**Bicel** is an Android marketplace application inspired by OLX, built with Kotlin. It allows users to list items they want to sell across multiple categories and browse products listed by others to make purchases. Users can communicate directly with sellers through an integrated chat feature.

## Key Features

### 📱 Core Functionality
- **User Authentication**: Secure login and signup with Firebase Authentication
- **Post Ads**: List items for sale with:
  - Multiple product images
  - Detailed descriptions
  - Pricing information
  - Category selection
  - Automatic timestamps
- **Browse Products**: Search and filter ads across multiple categories
- **Categories Supported**:
  - 📱 Mobiles
  - 🏠 Houses
  - 🚗 Cars
  - 🏍️ Motors
  - 💻 PCs

### 💬 Communication
- **Real-time Messaging**: Chat directly with buyers/sellers
- **Message History**: View conversation threads
- **User Profiles**: View seller details and ratings

### 👤 User Management
- **Account Management**: Edit profile and manage account settings
- **My Ads**: View, manage, and track your listings
- **Search History**: Track recent searches

## Technical Stack

### Architecture & Tools
- **Language**: Kotlin
- **Framework**: Android (minSdk 24, targetSdk 34)
- **Build System**: Gradle
- **Architecture Components**: MVVM, LiveData, ViewModels

### Backend & Services
- **Firebase Authentication**: User login/signup
- **Firebase Firestore**: Real-time database for ads and messages
- **Firebase Realtime Database**: Chat data storage
- **Firebase Storage**: Image hosting for product photos

### UI/UX Libraries
- **Material Design**: Material components
- **Image Loading**: Glide & Picasso
- **Image Picker**: Dhaval2404's ImagePicker
- **Animations**: Lottie animations
- **Loading States**: Shimmer effect
- **Navigation**: Android Navigation Component
- **View Binding & Data Binding**: For efficient UI updates
- **Responsive Design**: SDP (Scalable Dimension) for device compatibility

### Networking & Async
- **Kotlin Coroutines**: Asynchronous operations
- **ViewModels & LiveData**: State management and lifecycle-aware updates

## Project Structure

```
bicel/
├── app/src/main/
│   ├── java/com/example/bicel/
│   │   ├── fragments/          # UI fragments (Home, Sell, MyAds, Account, Chats)
│   │   ├── activities/         # Activities (Login, Signup, ChatActivity, AdDetails)
│   │   ├── adapters/           # RecyclerView adapters
│   │   ├── ViewModels/         # MVVM ViewModels
│   │   ├── repositories/       # Data repositories
│   │   ├── dataClasses/        # Data models (Ad, User, Message, ChatUser)
│   │   ├── Utility/            # Utility functions
│   │   └── SharedPreferences/  # Local data storage
│   ├── res/                    # Resources (layouts, drawables, strings)
│   └── AndroidManifest.xml
├── build.gradle                # App-level Gradle configuration
└── gradle.properties
```

## Getting Started

### Prerequisites
- Android Studio (latest version)
- Android Device or Emulator (minSdk 24 or higher)
- Firebase Project Setup

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/itsrehandev/bicel.git
   cd bicel
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Firebase Setup**
   - Create a Firebase project at [firebase.google.com](https://firebase.google.com)
   - Download `google-services.json` and place it in `app/` directory
   - Enable Authentication, Firestore, Realtime Database, and Storage in Firebase Console

4. **Build & Run**
   - Sync Gradle files
   - Build the project: `Build > Make Project`
   - Run on device/emulator: `Run > Run 'app'`

## Future Enhancements
- User ratings and reviews
- Advanced filtering and sorting
- Push notifications
- Payment integration
- Wishlist feature
- Admin dashboard

## Learning Purpose
This is a learning project created to understand Android development concepts including:
- MVVM architecture pattern
- Firebase integration
- Coroutines and asynchronous programming
- RecyclerView and adapters
- Navigation component
- Real-time database operations

## Author
**Rehan Dev** - [GitHub](https://github.com/itsrehandev)
