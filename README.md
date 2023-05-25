![Nami SDK logo](https://cdn.namiml.com/brand/sdk/Nami-SDK@0.5x.png)

## What is Nami?

Nami is on a mission to help you grow your app business using IAPs and subscriptions.
Go beyond basic Google Play Billing infrastructure and focus on results with:

* Library of smart paywall templates to choose from, implemented natively using Jetpack Compose
* Paywall CMS so you can make change instantly, without submitting an app update
* Experimentation engine to run paywall A/B tests so you can improve your conversion rates
* Built-in IAP & subscription management and analytics, so you don't need another solution

Nami is simple adopt while giving you the tools you need to improve revenue. Our free tier is generous, and gives you everything you need to get started. [Sign up for a free account](https://app.namiml.com/join/)

Get started by heading over to our [quick start guide](https://docs.namiml.com/docs/nami-quickstart-guide)

## Getting the Nami Framework

### Requirements
- Android SDK minimum version 22
- SDK builds target Android 12 (API version 31)
- SDK has been built with Java v8 and Kotlin v1.6.10

### Add Maven repostiory

```java
allprojects {
    repositories {
        google()
        mavenLocal()
        maven { url "https://packages.namiml.com/NamiSDK/Android/"}
    }
}
```

### Add Nami SDK dependency

dependencies {
  implementation "com.namiml:sdk-android:3.0.10"
}

### Add Java 8 compatibility

```java
android {
  ...
  // Configure only for each module that uses Java 8
  // language features (either in its source code or
  // through dependencies).
  compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }
  // For Kotlin projects
  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_1_8.toString()
  }
}
```


## Configure the SDK

We recommend that you configure the Nami SDK as early in your app's launch as possible. This will ensure the SDK is ready to receive and process purchases.

The best spot to do this is in the onCreate() method in your class that creates your Application and inherits from Application(). Here's a full code example.

You can find the Nami App Platform ID under the Nami Control Center's [Integrations > Google Play](https://app.namiml.com/integrations/) section.

```kotlin
import com.namiml.Nami
import com.namiml.NamiConfiguration
import com.namiml.NamiLogLevel

class DemoApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    Nami.configure(
      NamiConfiguration.build(this, "YOUR_APP_PLATFORM_ID") {
        LogLevel = NamiLogLevel.INFO.takeIf { BuildConfig.DEBUG } ?: NamiLogLevel.WARN
      }
    )
  }
}
```

## Next Steps

Once the SDK is initialized, you're ready to show a paywall. See the [complete guide to Android setup](https://docs.namiml.com/docs/google-play-android-setup#show-a-paywall) to find out how.


## Other Resources

### Release Notes
- [Stable](https://github.com/namiml/nami-android/wiki/Nami-SDK-Stable-Releases)
- [Early Access](https://github.com/namiml/nami-android/wiki/Nami-SDK-Early-Access-Releases)

### Documentation

- [SDK Reference](https://docs.namiml.com/reference/)
