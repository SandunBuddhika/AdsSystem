package com.sandun.adsSystem.model.adsModel;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeBannerAd;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.LoadAdError;
import com.sandun.adsSystem.R;
import com.sandun.adsSystem.dialog.PersonalAdView;
import com.sandun.adsSystem.model.AdMethodType;
import com.sandun.adsSystem.model.AdType;
import com.sandun.adsSystem.model.AdsMediator;
import com.sandun.adsSystem.model.ErrorHandler;
import com.sandun.adsSystem.model.exceptions.FailedToLoadAdException;
import com.sandun.adsSystem.model.handler.AdRequestHandler;
import com.sandun.adsSystem.model.handler.ViewAdRequestHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NativeAd extends ViewAdsCompact {
    private boolean isMedium;
    private PersonalAdView personalAdView;

    public NativeAd(AdsMediator adsMediator, AdMethodType adMethodType, Map<AdMethodType, Object> preLoadedAds, LinearLayout container, boolean isMedium) {
        super(adsMediator, adMethodType, preLoadedAds, container);
        this.adType = isMedium ? AdType.NATIVE_MEDIUM : AdType.NATIVE_SMALL;
        this.isMedium = isMedium;
        this.personalAdView = new PersonalAdView(adsMediator.getActivity());
    }

    @Override
    public void showAds(AdRequestHandler handler, ErrorHandler errorHandler) throws FailedToLoadAdException {
        String counterKey = isMedium ? "native_medium_sum" : "native_small_sum";
        
        // Migrate legacy "native_sum" to "native_small_sum" for backward compatibility
        if (!pref.contains("native_small_sum") && pref.contains("native_sum")) {
            pref.edit().putLong("native_small_sum", pref.getLong("native_sum", 1)).apply();
        }

        long adSum = pref.getLong(counterKey, 1);
        int frequency = adsMediator.initializer.getAdFrequency();
        this.errorHandler = errorHandler;

        boolean showPersonal = adsMediator.initializer.isPersonalAdsActive() && frequency > 0 && (adSum % (frequency + 1) == 0);
        System.out.println("NativeAd: key=" + counterKey + " count=" + adSum + " freq=" + frequency + " showPersonal=" + showPersonal);

        pref.edit().putLong(counterKey, adSum + 1).apply();

        if (showPersonal) {
            personalAdView.showNative(
                container,
                adsMediator.initializer.getBackendUrl(),
                adsMediator.initializer.getAppId(),
                isMedium,
                handler,
                new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Personal native ad failed, falling back to AdMob/Meta.");
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
            try {
                showAdMob(handler);
            } catch (FailedToLoadAdException e) {
                errorHandler.onFailed();
            }
        } else {
            try {
                showMeta(handler);
            } catch (FailedToLoadAdException e) {
                errorHandler.onFailed();
            }
        }
    }

    @Override
    public void showAdMob(AdRequestHandler handler) throws FailedToLoadAdException {
        try {
            ViewAdRequestHandler viewHandler = (ViewAdRequestHandler) handler;

            LinearLayout nativeAdContainer = (LinearLayout) container;

            LinearLayout layout = (LinearLayout) LayoutInflater.from(adsMediator.getActivity()).inflate(isMedium ? R.layout.medium_native_ad_layout : R.layout.small_native_ad_layout, nativeAdContainer, false);
            nativeAdContainer.removeAllViews();
            nativeAdContainer.addView(layout);
            TemplateView nativeAdView = layout.findViewById(R.id.my_template);
            AdLoader adLoader = new AdLoader.Builder(adsMediator.getActivity(), adsMediator.getEffectiveGoogleIds(adsMediator.getActivity()).getNativeId())
                    .forNativeAd(nativeAd -> {
                        NativeTemplateStyle styles = new NativeTemplateStyle.Builder().build();
                        nativeAdView.setVisibility(View.VISIBLE);
                        nativeAdView.setStyles(styles);
                        nativeAdView.setNativeAd(nativeAd);
                        viewHandler.viewHandler(nativeAdView);
                        viewHandler.onSuccess();
                    }).withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            errorHandler.onFailed();
                        }
                    })
                    .build();
            adLoader.loadAd(adRequest);
        } catch (Exception e) {
            errorHandler.onFailed();
            e.printStackTrace();
        }
    }

    @Override
    public void showMeta(AdRequestHandler handler) throws FailedToLoadAdException {
        try {
            ViewAdRequestHandler viewHandler = (ViewAdRequestHandler) handler;
            NativeBannerAd nativeAd = new NativeBannerAd(adsMediator.getActivity(), adsMediator.getEffectiveFacebookIds(adsMediator.getActivity()).getNativeId());
            NativeAdListener nativeAdListener = new NativeAdListener() {
                @Override
                public void onMediaDownloaded(Ad ad) {
                    System.out.println("Native ad finished downloading all assets.");
                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    System.out.println("Native ad failed to load: " + adError.getErrorMessage());
                    errorHandler.onFailed();
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    if (nativeAd == null || nativeAd != ad) {
                        return;
                    }
                    inflateAd(nativeAd, viewHandler, errorHandler);
                    viewHandler.onSuccess();
                }

                @Override
                public void onAdClicked(Ad ad) {
                    System.out.println("Native ad clicked!");
                }

                @Override
                public void onLoggingImpression(Ad ad) {
                    System.out.println("Native ad impression logged!");
                }
            };
            nativeAd.loadAd(
                    nativeAd.buildLoadAdConfig()
                            .withAdListener(nativeAdListener)
                            .build());
        } catch (Exception e) {
            errorHandler.onFailed();
            e.printStackTrace();
        }
    }

    private void inflateAd(NativeBannerAd nativeBannerAd, ViewAdRequestHandler handler, ErrorHandler errorHandler) {
        try {
            nativeBannerAd.unregisterView();

            NativeAdLayout nativeAdLayout = new NativeAdLayout(adsMediator.getActivity());

            LayoutInflater inflater = LayoutInflater.from(adsMediator.getActivity());
            LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.banner_native_ad_layout, nativeAdLayout, false);
            nativeAdLayout.addView(adView);

            handler.viewHandler(nativeAdLayout);

            container.removeAllViews();
            container.addView(nativeAdLayout);

            RelativeLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);
            AdOptionsView adOptionsView = new AdOptionsView(adsMediator.getActivity(), nativeBannerAd, nativeAdLayout);
            adChoicesContainer.removeAllViews();
            adChoicesContainer.addView(adOptionsView, 0);

            TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
            TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
            TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
            MediaView nativeAdIconView = adView.findViewById(R.id.native_icon_view);
            Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

            nativeAdCallToAction.setText(nativeBannerAd.getAdCallToAction());
            nativeAdCallToAction.setVisibility(
                    nativeBannerAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
            nativeAdTitle.setText(nativeBannerAd.getAdvertiserName());
            nativeAdSocialContext.setText(nativeBannerAd.getAdSocialContext());
            sponsoredLabel.setText(nativeBannerAd.getSponsoredTranslation());

            List<View> clickableViews = new ArrayList<>();
            clickableViews.add(nativeAdTitle);
            clickableViews.add(nativeAdCallToAction);
            nativeBannerAd.registerViewForInteraction(adView, nativeAdIconView, clickableViews);
        } catch (Exception e) {
            errorHandler.onFailed();
            e.printStackTrace();
        }
    }
}
