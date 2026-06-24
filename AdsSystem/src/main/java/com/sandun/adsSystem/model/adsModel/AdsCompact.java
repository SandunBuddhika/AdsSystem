package com.sandun.adsSystem.model.adsModel;


import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.ads.AdRequest;
import com.sandun.adsSystem.dialog.AdDialog;
import com.sandun.adsSystem.dialog.LoadingDialog;
import com.sandun.adsSystem.model.AdMethodType;
import com.sandun.adsSystem.model.AdType;
import com.sandun.adsSystem.model.AdsMediator;
import com.sandun.adsSystem.model.ErrorHandler;
import com.sandun.adsSystem.model.exceptions.FailedToLoadAdException;
import com.sandun.adsSystem.model.handler.AdRequestHandler;

import java.util.Map;

public abstract class AdsCompact {
    protected static final String TAG = AdsCompact.class.getName();
    protected AdType adType;
    protected AdsMediator adsMediator;
    protected AdRequest adRequest;
    protected AdMethodType adMethodType;
    protected ErrorHandler errorHandler;
    protected Map<AdMethodType, Object> preLoadedAds;
    protected LoadingDialog loadingDialog;
    protected AdDialog adDialog;
    protected SharedPreferences pref;

    public AdsCompact(AdsMediator adsMediator, AdMethodType adMethodType, Map<AdMethodType, Object> preLoadedAds) {
        this.adsMediator = adsMediator;
        this.adMethodType = adMethodType;
        this.adRequest = new AdRequest.Builder().build();
        this.preLoadedAds = preLoadedAds;
        this.loadingDialog = new LoadingDialog(adsMediator.getActivity(),adsMediator.getLoadingLayoutId());
        this.adDialog = new AdDialog(adsMediator.getActivity());
        this.pref = adsMediator.getActivity().getSharedPreferences("adsPref", Context.MODE_PRIVATE);
    }

    public abstract void showAds(AdRequestHandler handler, ErrorHandler errorHandler) throws FailedToLoadAdException;

    public abstract void showAdMob(AdRequestHandler handler) throws FailedToLoadAdException;

    public abstract void showMeta(AdRequestHandler handler) throws FailedToLoadAdException;

    public void changeType() {
        adMethodType = AdMethodType.ADMOB == adMethodType ? AdMethodType.META : AdMethodType.ADMOB;
    }

    public AdType getAdType() {
        return adType;
    }

    public AdMethodType getAdMethodType() {
        return adMethodType;
    }
}
