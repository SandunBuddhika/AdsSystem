package com.sandun.adsSystem.model.adsModel;

import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.sandun.adsSystem.dialog.PersonalAdView;
import com.sandun.adsSystem.model.AdMethodType;
import com.sandun.adsSystem.model.AdType;
import com.sandun.adsSystem.model.AdsMediator;
import com.sandun.adsSystem.model.ErrorHandler;
import com.sandun.adsSystem.model.exceptions.FailedToLoadAdException;
import com.sandun.adsSystem.model.handler.AdRequestHandler;
import com.sandun.adsSystem.model.handler.ViewAdRequestHandler;

import java.util.Map;

public class BannerAd extends ViewAdsCompact {
    private PersonalAdView personalAdView;

    public BannerAd(AdsMediator adsMediator, AdMethodType adMethodType, Map<AdMethodType, Object> preLoadedAds, LinearLayout container) {
        super(adsMediator, adMethodType, preLoadedAds, container);
        adType = AdType.BANNER;
        personalAdView = new PersonalAdView(adsMediator.getActivity());
    }

    @Override
    public void showAds(AdRequestHandler handler, ErrorHandler errorHandler) throws FailedToLoadAdException {
        long adSum = pref.getLong("banner_sum", 1);
        int frequency = adsMediator.initializer.getAdFrequency();
        this.errorHandler = errorHandler;

        boolean showPersonal = adsMediator.initializer.isPersonalAdsActive() && frequency > 0 && (adSum % (frequency + 1) == 0);
        System.out.println("BannerAd: count=" + adSum + " freq=" + frequency + " showPersonal=" + showPersonal);

        pref.edit().putLong("banner_sum", adSum + 1).apply();

        if (showPersonal) {
            personalAdView.showBanner(
                container,
                adsMediator.initializer.getBackendUrl(),
                adsMediator.initializer.getAppId(),
                handler,
                new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Personal banner ad failed, falling back to AdMob/Meta.");
                        showNetworkAds(handler);
                    }
                }
            );
        } else {
            showNetworkAds(handler);
        }
    }

    private void showNetworkAds(AdRequestHandler handler) {
        if (adMethodType == AdMethodType.ADMOB) {
            showAdMob(handler);
        } else {
            try {
                showMeta(handler);
            } catch (FailedToLoadAdException e) {
                errorHandler.onFailed();
            }
        }
    }

    @Override
    public void showAdMob(AdRequestHandler handler) {
        try {
            AdView adView = new AdView(adsMediator.getActivity());
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    if (handler instanceof ViewAdRequestHandler) {
                        ((ViewAdRequestHandler) handler).viewHandler(adView);
                    }
                    handler.onSuccess();
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    errorHandler.onFailed();
                    super.onAdFailedToLoad(loadAdError);
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            adView.setLayoutParams(layoutParams);
            adView.setAdUnitId(adsMediator.initializer.getGoogleIds().getBannerId());
            adView.setAdSize(AdSize.BANNER);
            container.removeAllViews();
            container.addView(adView);
            adView.loadAd(adRequest);
        } catch (Exception e) {
            errorHandler.onFailed();
        }
    }

    @Override
    public void showMeta(AdRequestHandler handler) throws FailedToLoadAdException {
        try {
            com.facebook.ads.AdView bannerAdView = new com.facebook.ads.AdView(adsMediator.getActivity(), adsMediator.initializer.getFacebookIds().getBannerId(), com.facebook.ads.AdSize.BANNER_HEIGHT_50);
            container.removeAllViews();
            container.addView(bannerAdView);
            bannerAdView.loadAd(bannerAdView.buildLoadAdConfig().withAdListener(new com.facebook.ads.AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {
                    errorHandler.onFailed();
                }
                @Override
                public void onAdLoaded(Ad ad) {
                    if (handler instanceof ViewAdRequestHandler) {
                        ((ViewAdRequestHandler) handler).viewHandler(bannerAdView);
                    }
                    handler.onSuccess();
                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            }).build());
        } catch (Exception e) {
            errorHandler.onFailed();
        }
    }
}
