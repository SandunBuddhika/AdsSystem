

[![](https://jitpack.io/v/SandunBuddhika/AdsSystem.svg)](https://jitpack.io/#SandunBuddhika/AdsSystem)

# Introduction
* This is library to easily implement google admob and meta ads together into a one android appllication with few lines code.

# Key Features
* Pre loading ads
* handling error and success states of a ads
* if a ad failed to load the first time try to load another before going to failed state (admob ad failed this try to load a meta ad before going to failed state).

# Ads types
* Interstitial
* App open
* Reward
* Native
* Banner

# Basic mechanism
![image](https://github.com/user-attachments/assets/34350043-16b5-41d7-9528-9f99b00aebe5)


# Implementation

* Step 1
  You need to put this in to your setting.gradle file
```java
maven { url = uri("https://www.jitpack.io") }
```
```java
//example

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven { url = uri("https://www.jitpack.io") }
        mavenCentral()
    }
}
```

* Step 2
```kotlin
implementation 'com.github.SandunBuddhika:AdsSystem:1.1.2'

//other
implementation ("com.airbnb.android:lottie:6.6.2")
implementation("com.google.android.gms:play-services-ads:23.5.0")
implementation("com.facebook.android:audience-network-sdk:6.18.0")
```

* Step 3

```java  
 //STEP 1: Initialize your abmod and meta ads code
 AdsInitializer initializer = AdsInitializer.getInstance(  
        new AdsInitializer.FacebookIds("123", "123", "123", "123"),  
        new AdsInitializer.GoogleIds("123", "123", "123", "123", "123", "123"));  
```

Don't forget to set app ids in the AndroidManifest.xml

* Step 4

```java
//STEP 2: Create AdsMediator
//This this allow you to load, pre load and manage ads.
AdsMediator mediator = AdsMediator.getInstance(this, initializer);
        mediator.setAdMethodType(AdMethodType.ADMOB);
```

* Step 5
```java
// Create a ad
mediator.showInterstitialAd(new AdRequestHandler() {
  @Override
  public void onSuccess() {
    System.out.println("onSuccess");
  }

  @Override
    public void onError() {
      System.out.println("onError");
  }
});
```
* Step 6
```java
//Pre load ads
mediator.preLoadAds(AdType.INTERSTITIAL);
```

* Step 7
```java
mediator.clearPreLoadedAd(AdType.INTERSTITIAL);
```

* Additionally

When you use native ads or banner ads state handling process is bit different
```java

mediator.showBannerAd(
// need ViewAdRequestHandler to handler state of ad
new ViewAdRequestHandler() {
    @Override
    public void onSuccess() {
        System.out.println("onSuccess");
    }
    @Override
    public void onError() {
        System.out.println("onError");
    }
    @Override
    public void viewHandler(View adView) {
//      this method for manually handle ad view if needed
    }
},
// need to parse a empty layout container to place the native and banner ad
findViewById(R.id.banner_ad_container));
```

Loading screen
```java

// set your own loading layout
mediator.setLoadingLayoutId(R.layout.dialog_loading_ads_layout);

```

There is few more functions, i'm sure you will figure it out :)
