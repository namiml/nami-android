![Nami SDK logo](https://cdn.namiml.com/brand/sdk/Nami-SDK@0.5x.png)

## What is Nami?

Nami ML gives you everything you need to power your paywall, streamline subscription management, and drive revenue growth through instantly deployable paywalls, precise targeting and segmentation, and enterprise-grade security and scaleability.

Go beyond basic Google Play Billing infrastructure and focus on results with:

* Library of smart paywall templates to choose from, implemented natively using Jetpack Compose
* No-code paywall creator so you can launch a new paywall design instantly, without submitting an app update
* Experimentation engine to run paywall A/B tests so you can improve your conversion rates
* Built-in IAP & subscription management and analytics, so you don't need another solution

Nami is simple adopt while giving you the tools you need to improve revenue. Our free tier is generous, and gives you everything you need to get started. [Sign up for a free account](https://app.namiml.com/join/)

Get started by heading over to our [quick start guide](https://learn.namiml.com/public-docs/get-started/quickstart-guide)

## Getting the Nami Framework

### Requirements
- Android SDK minimum version 22
- SDK builds target Android 13 (API version 33)
- SDK has been built with Kotlin v1.8.20

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
  implementation "com.namiml:sdk-android:3.2.6"
}

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

Once the SDK is initialized, you're ready to show a paywall. See the [complete guide to Android setup](https://learn.namiml.com/public-docs/integrations/billing-platforms/google-integration/sdk-setup) to find out how.


## Other Resources

### Release Notes
- [Stable](https://github.com/namiml/nami-android/wiki/Nami-SDK-Stable-Releases)
- [Early Access](https://github.com/namiml/nami-android/wiki/Nami-SDK-Early-Access-Releases)

### Documentation

- [SDK Reference](https://learn.namiml.com/public-docs/sdk-reference)
