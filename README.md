# ShoppingCartApp 🛒

**Android shopping prototype · Java · Firebase · 2020**

An early Android application bringing together phone and email authentication, cloud-backed product listings, a shopping cart, profile management, search suggestions, and location-assisted checkout. This is a **historical, archived learning project**, not a currently supported shopping service.

> Built before the mainstream adoption of LLM-assisted development. Preserved as part of my software development journey.

## What it implements

| Area | Implementation |
| --- | --- |
| Account access | Firebase Authentication: phone-number OTP, email/password sign-in, email verification, and password-reset email |
| User profiles | Profile viewing and editing with Firebase-backed user information and image-storage integration |
| Store browsing | Product data loaded from Firebase Realtime Database and displayed in a `RecyclerView` |
| Search | Product filtering and suggestions, including a Firestore-backed suggestion list |
| Shopping cart | Item selection, checkout-summary list, total-price calculation, and confirmation dialog |
| Location | Fused Location Provider and Android `Geocoder` for address assistance |
| Additional content | Firebase-backed content feed displayed in a separate screen |

**Scope:** The checkout screen presents a cart summary and confirmation flow. This repository does **not** establish a production payment-gateway integration; do not use it to process real purchases.

## Technology stack

- **Language/UI:** Java, Android SDK, AndroidX, XML layouts, `RecyclerView`.
- **Cloud:** Firebase Authentication, Cloud Firestore, Firebase Realtime Database, Firebase Storage.
- **Location:** Google Play Services Location and Android `Geocoder`.
- **Images and search:** Picasso, Glide, MaterialSearchBar; Retrofit and RxJava dependencies are also declared.
- **Original build:** Android Gradle Plugin 3.5.3; compile SDK 28; min SDK 25; target SDK 27.

## Application structure

```text
app/src/main/java/com/justforfun/phoneverification/
├── phoneOTP.java            # Phone authentication entry screen
├── LoginActivity.java       # Email/password login and password recovery
├── RegisterActivity.java    # Registration and verification
├── ProfilePage.java         # User profile
├── EditProfile.java         # Profile editing
├── StoreListings.java       # Store, search, cart, checkout and location
├── ListingAdapter.java      # Product list rendering
├── CheckOutAdapter.java     # Cart-summary rendering
├── WebContent.java          # Additional Firebase-backed content
└── ...                      # Models, adapters and OTP receiver
```

The app uses Android activities and adapters with Firebase SDK calls in the application layer. It is a historical prototype, not a demonstration of a modern layered or clean-architecture implementation.

## Explore the source

- [`app/src/main/java/`](app/src/main/java/com/justforfun/phoneverification/) — Java application logic.
- [`app/src/main/res/`](app/src/main/res/) — XML layouts and Android resources.
- [`app/build.gradle`](app/build.gradle) — Android and Firebase dependencies.
- [`app/src/main/AndroidManifest.xml`](app/src/main/AndroidManifest.xml) — activities and permissions.

## Running locally

1. Clone this repository and open it in Android Studio.
2. Use a compatible legacy Android Gradle Plugin/JDK/SDK environment. A build with current Android Studio has **not** been verified.
3. Create **your own Firebase project** and configure the services used by the app. Do not rely on the historical Firebase configuration in this repository.
4. Configure appropriate authentication providers, Firebase database rules, and test data before exercising cloud-backed features.
5. Build and run on a compatible Android emulator or test device.

```bash
 git clone https://github.com/TechMafia-dev/ShoppingCartApp.git
```

The historical `app-debug.apk` is included in the original repository, but its compatibility and security have not been verified. Prefer building from reviewed source in an isolated test environment.

## Historical limitations and security notes

This is **not production-ready**. Its legacy dependencies, target SDK, runtime permissions, Firebase configuration, and network settings require review before reuse. The manifest permits cleartext traffic and requests broad phone/SMS/storage/location permissions; modern Android versions impose different restrictions. No current-device build, end-to-end test, security audit, or live backend availability is claimed.

The source archive also contains `app/google-services.json`. Although Firebase client configuration is not equivalent to an administrative service-account key, review the associated Firebase project, security rules, authorized clients, and any historical credentials before redistributing or reactivating the app. Avoid publishing real user data.

## Why it is in my portfolio

ShoppingCartApp documents an early stage of my development journey: integrating Android screens, asynchronous cloud operations, authentication, data-backed lists, and device capabilities in a single application. My later work expanded into computational engineering, Python backends, optimization, and applied AI.

**Developer:** [Shreyash Patidar](https://github.com/TechMafia-dev) · [GitHub profile](https://github.com/TechMafia-dev)
