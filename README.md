
# Introduction
* This is library to implement google admob and meta ads totherger into a one android appllication, and new and easy way to implement into a android application.

# Key Features
* Pre loading ads
* handling error and success states of a ads
* if a ad failed to load the first time try to load another before going to failed state (admob ad failed this try to load a meta ad before going to failed state).


# Basic mechanism
![image](https://github.com/user-attachments/assets/34350043-16b5-41d7-9528-9f99b00aebe5)


# Implementation

* Step 1

```java  
 //STEP 1: Initialize your abmod and meta ads code
 AdsInitializer initializer = AdsInitializer.getInstance(  
        new AdsInitializer.FacebookIds("123", "123", "123", "123"),  
        new AdsInitializer.GoogleIds("123", "123", "123", "123", "123", "123"));  
```  

* Step 2 

```java
//STEP 2: Create AdsMediator
//This this allow you to load, pre load and manage ads.
AdsMediator mediator = AdsMediator.getInstance(this, initializer);
        mediator.setAdMethodType(AdMethodType.ADMOB);
```

* Step 3
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
* Step 4
```java
//Pre load ads
mediator.preLoadAds(AdType.INTERSTITIAL);
```

* Step 5
```java
mediator.clearPreLoadedAd(AdType.INTERSTITIAL);
```

There is few more functions, i'm sure you will figure it out :)
