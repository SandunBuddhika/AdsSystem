
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
```css
implementation 'com.github.SandunBuddhika:AdsSystem:1.0.3'
```

* Step 3

```java  
 //STEP 1: Initialize your abmod and meta ads code
 AdsInitializer initializer = AdsInitializer.getInstance(  
        new AdsInitializer.FacebookIds("123", "123", "123", "123"),  
        new AdsInitializer.GoogleIds("123", "123", "123", "123", "123", "123"));  
```  

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

There is few more functions, i'm sure you will figure it out :)
