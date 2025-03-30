# Xpense+ Android Application

## Project Overview

Xpense+ is a comprehensive personal finance tracking application that allows users to manage their expenses, income, and financial interactions with other users. Built with modern Android development technologies, the app features a robust user authentication system, real-time database synchronization, and interactive financial visualizations.

## Features

### Core Functionality

- **Expense & Income Tracking**: Record and categorize daily and monthly transactions
- **Dashboard View**: Visual summary of financial status with income, expenses, and balance
- **Financial Analytics**: Charts and graphs for spending patterns analysis
- **Goals Management**: Set and track financial goals

### Social Features

- **Handshake Transactions**: Connect with other users to track shared expenses or debts
- **Settlement System**: Manage and settle debts with other users
- **Transaction Requests**: Send and receive money requests

### User Management

- **User Authentication**: Secure sign-up and login with Firebase Auth
- **Profile Management**: Update personal information and account details
- **Multiple Account Support**: Track cash and bank account balances separately

### Additional Features

- **Financial News**: Stay updated with latest financial news
- **Exchange Rates**: Real-time currency exchange rates
- **Push Notifications**: Get alerts for transaction updates and requests
- **Theme Customization**: Personalize app appearance

## Technical Implementation

### Firebase Integration

- **Authentication**: User management with email and phone verification
- **Realtime Database**: Synchronize data across devices with Firebase RTDB
- **Cloud Messaging**: Push notification service

### UI/UX

- **Material Design**: Modern, intuitive interface with Google's Material Design components
- **Data Visualization**: Interactive charts using MPAndroidChart library
- **Responsive Layouts**: Support for various screen sizes and orientations

### Network Services

- **API Integration**: Financial news and exchange rate services
- **Retrofit & Volley**: Network request handling
- **Offline Support**: Data persistence for offline usage

## Technical Stack

### Firebase Integration

- Firebase Analytics
- Firebase Auth
- Firebase Messaging

### UI Components

- AndroidX RecyclerView
- Google Material Design
- MPAndroidChart for data visualization

### Networking

- Retrofit2 for API calls
- OkHttp3 for HTTP logging
- Volley for alternative networking
- Gson converter for JSON parsing

### Image Processing

- Glide for image loading and caching

### Authentication

- Google Play Services Auth

## Development Setup

### Prerequisites

- JDK 17 or higher
- Android Studio
- Firebase account (with google-services.json properly configured)

### Building the Project

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run the application using the Gradle wrapper:

```bash
# For Unix-like systems
./gradlew build

# For Windows
gradlew.bat build
```

# Technical Requirements

Minimum SDK: 24 (Android 7.0 Nougat)
Target SDK: 34 (Android 14)
Compile SDK: 34
Version: 1.0

# Project Structure

The project follows standard Android project structure with:

- Application source code in app directory
- Gradle configuration in build.gradle.kts files
- Firebase configuration in google-services.json

# Configuration

The project uses:

- ViewBinding for easier view access
- AndroidX support libraries
- Non-transitive R class for reduced APK size
- JVM memory settings configured for up to 2048MB
