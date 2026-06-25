package com.sandun.adsSystem.model.adsModel;

import androidx.annotation.NonNull;

import com.facebook.ads.Ad;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.sandun.adsSystem.dialog.AdDialog;
import com.sandun.adsSystem.model.AdMethodType;
import com.sandun.adsSystem.model.AdType;
import com.sandun.adsSystem.model.AdsMediator;
import com.sandun.adsSystem.model.ErrorHandler;
import com.sandun.adsSystem.model.exceptions.FailedToLoadAdException;
import com.sandun.adsSystem.model.handler.AdRequestHandler;

import java.util.Map;

public class OpenAd extends AdsCompact {
    public OpenAd(AdsMediator adsMediator, AdMethodType adMethodType, Map<AdMethodType, Object> preLoadedAds) {
        super(adsMediator, adMethodType, preLoadedAds);
        adType = AdType.OPEN;
    }

    @Override
    public void showAds(AdRequestHandler handler, ErrorHandler errorHandler) throws FailedToLoadAdException {
        long adSum = pref.getLong("open_sum", 1);
        int frequency = adsMediator.initializer.getAdFrequency();
        this.errorHandler = errorHandler;

        boolean showPersonal = adsMediator.initializer.isPersonalAdsActive() && frequency > 0 && (adSum % (frequency + 1) == 0);
        System.out.println("OpenAd: count=" + adSum + " freq=" + frequency + " showPersonal=" + showPersonal);

        pref.edit().putLong("open_sum", adSum + 1).apply();

        if (showPersonal) {
            adDialog.show(
                adsMediator.initializer.getBackendUrl(),
                adsMediator.initializer.getAppId(),
                "OPEN",
                handler,
                loadingDialog,
                new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Personal open ad failed, falling back to AdMob/Meta.");
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
        System.out.println("Showing pre loaded app open");
        if (ad instanceof AppOpenAd) {
            AppOpenAd appOpenAd = (AppOpenAd) ad;
            appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    handler.onSuccess();
                    adsMediator.clearPreLoadedAd(adType);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    System.out.println("Failed to show Open ad");
                    errorHandler.onFailed();
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                }
            });
            loadingDialog.dismiss();
            appOpenAd.show(adsMediator.getActivity());
        } else {
            AppOpenAd.load(
                    adsMediator.getActivity(), adsMediator.getEffectiveGoogleIds(adsMediator.getActivity()).getAppOpenId(), adRequest,
                    new AppOpenAd.AppOpenAdLoadCallback() {
                        @Override
                        public void onAdLoaded(AppOpenAd ad) {
                            ad.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdClicked() {
                                    super.onAdClicked();
                                }

                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    handler.onSuccess();
                                    adsMediator.clearPreLoadedAd(adType);
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                    System.out.println("Failed to show Open ad");
                                    errorHandler.onFailed();
                                }

                                @Override
                                public void onAdImpression() {
                                    super.onAdImpression();
                                }
                            });
                            loadingDialog.dismiss();
                            ad.show(adsMediator.getActivity());
                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            loadingDialog.dismiss();
                            System.out.println("Failed to load Open ad");
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
                            adsMediator.clearPreLoadedAd(adType);
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
            com.facebook.ads.InterstitialAd mInterstitialAd = new com.facebook.ads.InterstitialAd(adsMediator.getActivity(), adsMediator.getEffectiveFacebookIds(adsMediator.getActivity()).getInitId());
            InterstitialAdListener adListener = new InterstitialAdListener() {
                @Override
                public void onInterstitialDisplayed(Ad ad) {
                    System.out.println("onInterstitialDisplayed");
                }

                @Override
                public void onInterstitialDismissed(Ad ad) {
                    System.out.println("onInterstitialDismissed");
                    handler.onSuccess();
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
