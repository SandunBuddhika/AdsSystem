package com.sandun.adsSystem.model;

import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.sandun.adsSystem.R;
import com.sandun.adsSystem.dialog.LoadingDialog;
import com.sandun.adsSystem.model.adsModel.InterstitialAd;
import com.sandun.adsSystem.model.handler.AdRequestHandler;
import com.sandun.adsSystem.AdsInitializer;
import com.sandun.adsSystem.model.adsModel.BannerAd;
import com.sandun.adsSystem.model.adsModel.NativeAd;
import com.sandun.adsSystem.model.adsModel.OpenAd;
import com.sandun.adsSystem.model.adsModel.RewardAd;
import com.sandun.adsSystem.model.handler.ViewAdRequestHandler;

public class AdsMediator {
    private static AdsMediator adsMediator;
    public AdsInitializer initializer;
    public AppCompatActivity activity;
    private PreLoader preLoader;
    private boolean isIgnoreAds;
    private AdMethodType adMethodType;
    private int loadingLayoutId= R.layout.dialog_loading_ads_layout;

    private AdsMediator() {
        preLoader = new PreLoader(this);
    }

    public static AdsMediator getInstance(AppCompatActivity activity, AdsInitializer initializer) {
        init(activity, initializer);
        return adsMediator;
    }

    public static AdsMediator getInstance(AppCompatActivity activity) {
        init(activity, null);
        return adsMediator;
    }

    private static void init(AppCompatActivity activity, AdsInitializer initializer) {
        if (adsMediator == null) {
            adsMediator = new AdsMediator();
            AudienceNetworkAds.initialize(activity);
        }
        if (initializer != null) {
            adsMediator.initializer = initializer;
        }
        adsMediator.activity = activity;
    }

    public void setIgnoreAds(boolean ignoreAds) {
        isIgnoreAds = ignoreAds;
    }

    public void setAdMethodType(AdMethodType adMethodType) {
        this.adMethodType = adMethodType;
    }

    public Object getNewPreLoadedAd(AdMethodType methodType, AdType adType) {
        return null;
    }

    public int getLoadingLayoutId() {
        return loadingLayoutId;
    }

    public void setLoadingLayoutId(int loadingLayoutId) {
        this.loadingLayoutId = loadingLayoutId;
    }

    public void preLoadAds(AdType adType) {
        if (!isIgnoreAds) {
            switch (adType) {
                case INTERSTITIAL:
                    preLoader.preLoadInterstitialAds();
                    break;
                case REWARD:
                    preLoader.preLoadRewardAds();
                    break;
                case OPEN:
                    preLoader.preOpenAds();
                    break;
            }
        }
    }

    public void clearPreLoadedAd(AdType adType) {
        switch (adType) {
            case INTERSTITIAL:
                preLoader.clearInterstitialAd();
                break;
            case REWARD:
                preLoader.clearRewardAds();
                break;
            case OPEN:
                preLoader.clearOpenAds();
                break;
        }

    }

    public void showInterstitialAd(AdRequestHandler handler) {
        if (!isIgnoreAds) {
            InterstitialAd ad = new InterstitialAd(this, adMethodType, preLoader.getInterstitialAd());
            new ErrorHandler(ad, handler, this);
        } else {
            handler.onSuccess();
        }
    }

    public void showRewardAd(AdRequestHandler handler) {
        if (!isIgnoreAds) {
            RewardAd ad = new RewardAd(this, adMethodType, preLoader.getRewardAds());
            new ErrorHandler(ad, handler, this);
        } else {
            handler.onSuccess();
        }
    }

    public void showOpenAd(AdRequestHandler handler) {
        if (!isIgnoreAds) {
            OpenAd ad = new OpenAd(this, adMethodType, preLoader.getOpenAds());
            new ErrorHandler(ad, handler, this);
        } else {
            handler.onSuccess();
        }
    }

    public void showNativeAd(ViewAdRequestHandler handler, LinearLayout container) {
        if (!isIgnoreAds) {
            NativeAd ad = new NativeAd(this, adMethodType, preLoader.getOpenAds(), container);
            new ErrorHandler(ad, handler, this);
        } else {
            handler.onSuccess();
        }
    }

    public void showBannerAd(ViewAdRequestHandler handler, LinearLayout container) {
        if (!isIgnoreAds) {
            BannerAd ad = new BannerAd(this, adMethodType, preLoader.getOpenAds(), container);
            new ErrorHandler(ad, handler, this);
        } else {
            handler.onSuccess();
        }
    }

}
