AI-Based Smart Meal Planner

COIT13230 – Application Development Project
Central Queensland University | Term 2, 2026

Project Overview

AI-Based Smart Meal Planner is an Android application developed in Kotlin using Android Studio.

The application uses Google's Gemini Generative AI API to generate personalised meal plans based on user information, dietary requirements, food preferences and available ingredients.

It also integrates the Australian Food Composition Database (AFCD) to calculate nutritional information for generated meals.

Main Features

User profile management, including weight goals and dietary preferences.

AI-generated meal plans using Google's Gemini API.

Recipe generation with ingredients and cooking instructions.

Nutritional analysis using the Australian Food Composition Database (AFCD).

Daily calorie and macronutrient summaries.

Grocery list generation based on meal plan ingredients.

Local storage of application data.

Android interface with navigation between Home, Meal Plan, Recipes, Grocery List and Profile screens.

Technologies Used

Language: Kotlin

Development Environment: Android Studio

Platform: Android

Generative AI: Google Gemini API

AI Model: Gemini 3.5 Flash-Lite

Networking: Retrofit and OkHttp

Nutrition Data: Australian Food Composition Database (AFCD)

User Interface: XML layouts and Material Design

Installation and Setup

1. Download the Project

Clone the GitHub repository or download the project as a ZIP file.

Open the project folder in Android Studio and allow Gradle to synchronise.

2. Configure the Gemini API

The application requires a Google Gemini API key to generate meal plans.

Visit Google AI Studio.

Sign in with a Google account.

Create a Gemini API key.

Open the Android Studio project.

Locate the local.properties file in the project's root directory.

Add the following line, replacing the placeholder with your own API key:

GEMINI_API_KEY=YOUR_API_KEY_HERE

The application accesses the API key through BuildConfig.GEMINI_API_KEY in GenAIClient.kt. The project's Gradle configuration must expose the value from local.properties as this BuildConfig field.

Important: Do not commit your API key to GitHub. Keep local.properties excluded from version control.

3. Build and Run

Open the project in Android Studio.

Allow Gradle to finish synchronising.

Select an Android emulator or connect a physical Android device.

Build and run the application.

Ensure the device has an active internet connection.