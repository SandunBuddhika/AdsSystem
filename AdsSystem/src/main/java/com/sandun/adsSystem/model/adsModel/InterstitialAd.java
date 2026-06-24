package com.sandun.adsSystem.model.adsModel;

import androidx.annotation.NonNull;

import com.facebook.ads.Ad;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.sandun.adsSystem.dialog.AdDialog;
import com.sandun.adsSystem.model.AdMethodType;
import com.sandun.adsSystem.model.AdType;
import com.sandun.adsSystem.model.AdsMediator;
import com.sandun.adsSystem.model.ErrorHandler;
import com.sandun.adsSystem.model.exceptions.FailedToLoadAdException;
import com.sandun.adsSystem.model.handler.AdRequestHandler;

import java.util.Map;

public class InterstitialAd extends AdsCompact {

    public InterstitialAd(AdsMediator adsMediator, AdMethodType adMethodType, Map<AdMethodType, Object> preLoadedAds) {
        super(adsMediator, adMethodType, preLoadedAds);
        adType = AdType.INTERSTITIAL;
    }

    @Override
    public void showAds(AdRequestHandler handler, ErrorHandler errorHandler) throws FailedToLoadAdException {
        long adSum = pref.getLong("interstitial_sum", 1);
        int frequency = adsMediator.initializer.getAdFrequency();
        this.errorHandler = errorHandler;

        // Cycle length = frequency + 1 (e.g., frequency=2 means 2 network ads then 1 personal)
        // Personal ad triggers when counter hits the cycle boundary
        boolean showPersonal = adsMediator.initializer.isPersonalAdsActive() && frequency > 0 && (adSum % (frequency + 1) == 0);
        System.out.println("InterstitialAd: count=" + adSum + " freq=" + frequency + " showPersonal=" + showPersonal);

        // Always increment counter
        pref.edit().putLong("interstitial_sum", adSum + 1).apply();

        if (showPersonal) {
            // Try loading personal ad. If it fails, fallback immediately to network ads
            adDialog.show(
                adsMediator.initializer.getBackendUrl(), 
                adsMediator.initializer.getAppId(), 
                "INIT", 
                handler, 
                loadingDialog,
                new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Personal interstitial ad failed, falling back to AdMob/Meta.");
                        showNetworkAds(handler);
                    }
                }
            );
        } else {
            showNetworkAds(handler);
        }
    }

    private void showNetworkAds(AdRequestHandler handler) {
        loadingDialog.show();
        if (adMethodType == AdMethodType.ADMOB) {
            showAdMob(handler);
        } else {
            showMeta(handler);
        }
    }

    @Override
    public void showAdMob(AdRequestHandler handler) throws FailedToLoadAdException {
        Object ad = preLoadedAds.get(adMethodType);
        if (ad instanceof com.google.android.gms.ads.interstitial.InterstitialAd) {
            com.google.android.gms.ads.interstitial.InterstitialAd interstitialAd = ((com.google.android.gms.ads.interstitial.InterstitialAd) preLoadedAds.get(adMethodType));
            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    System.out.println("Ad Dismiss");
                    handler.onSuccess();
                    adsMediator.clearPreLoadedAd(AdType.INTERSTITIAL);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    System.out.println(adError.getMessage());
                    errorHandler.onFailed();
                }
            });
            loadingDialog.dismiss();
            interstitialAd.show(adsMediator.getActivity());
        } else {
            com.google.android.gms.ads.interstitial.InterstitialAd.load(adsMediator.getActivity(), adsMediator.initializer.getGoogleIds().getInitId(), adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull com.google.android.gms.ads.interstitial.InterstitialAd interstitialAd) {
                            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    System.out.println("Ad Dismiss");
                                    handler.onSuccess();
                                    adsMediator.clearPreLoadedAd(AdType.INTERSTITIAL);
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                    System.out.println(adError.getMessage());
                                    errorHandler.onFailed();
                                }
                            });
                            loadingDialog.dismiss();
                            interstitialAd.show(adsMediator.getActivity());
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            loadingDialog.dismiss();
                            System.out.println(loadAdError.getMessage());
                            errorHandler.onFailed();
                        }
                    });
        }
    }

    @Override
    public void showMeta(AdRequestHandler handler) throws FailedToLoadAdException {
        Object ad = preLoadedAds.get(adMethodType);
        if (ad instanceof com.facebook.ads.InterstitialAd && ((com.facebook.ads.InterstitialAd) ad).isAdLoaded()) {
            com.facebook.ads.InterstitialAd interstitialAd = ((com.facebook.ads.InterstitialAd) ad);
            interstitialAd.loadAd(interstitialAd.buildLoadAdConfig()
                    .withAdListener(new InterstitialAdListener() {
                        @Override
                        public void onInterstitialDisplayed(Ad ad) {
                            System.out.println("onInterstitialDisplayed");
                        }

                        @Override
                        public void onInterstitialDismissed(Ad ad) {
                            System.out.println("onInterstitialDismissed");
                            handler.onSuccess();
                            adsMediator.clearPreLoadedAd(AdType.INTERSTITIAL);
                        }

                        @Override
                        public void onError(Ad ad, com.facebook.ads.AdError adError) {
                            System.out.println(adError.getErrorMessage());
                            errorHandler.onFailed();
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            System.out.println("hi");
                        }

                        @Override
                        public void onAdClicked(Ad ad) {
                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {
                        }
                    })
                    .build());
            loadingDialog.dismiss();
            interstitialAd.show();
        } else {
            com.facebook.ads.InterstitialAd mInterstitialAd = new com.facebook.ads.InterstitialAd(adsMediator.getActivity(), adsMediator.initializer.getFacebookIds().getInitId());
            InterstitialAdListener adListener = new InterstitialAdListener() {
                @Override
                public void onInterstitialDisplayed(Ad ad) {
                    System.out.println("onInterstitialDisplayed");
                }

                @Override
                public void onInterstitialDismissed(Ad ad) {
                    System.out.println("onInterstitialDismissed");
                    handler.onSuccess();
                    adsMediator.clearPreLoadedAd(AdType.INTERSTITIAL);
                }

                @Override
                public void onError(Ad ad, com.facebook.ads.AdError adError) {
                    System.out.println(adError.getErrorMessage());
                    loadingDialog.dismiss();
                    errorHandler.onFailed();
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    System.out.println("onAdLoaded");
                    loadingDialog.dismiss();
                    mInterstitialAd.show();
                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            };
            mInterstitialAd.loadAd(
                    mInterstitialAd.buildLoadAdConfig()
                            .withAdListener(adListener)
                            .build()
            );
        }
    }

}
