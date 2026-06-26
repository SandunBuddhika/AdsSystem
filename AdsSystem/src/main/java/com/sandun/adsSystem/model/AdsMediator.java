package com.sandun.adsSystem.model;

import android.app.Application;
import android.app.Activity;
import android.widget.LinearLayout;

import com.facebook.ads.AudienceNetworkAds;
import com.sandun.adsSystem.R;
import com.sandun.adsSystem.model.adsModel.InterstitialAd;
import com.sandun.adsSystem.model.handler.AdRequestHandler;
import com.sandun.adsSystem.AdsInitializer;
import com.sandun.adsSystem.model.adsModel.BannerAd;
import com.sandun.adsSystem.model.adsModel.NativeAd;
import com.sandun.adsSystem.model.adsModel.OpenAd;
import com.sandun.adsSystem.model.adsModel.RewardAd;
import com.sandun.adsSystem.model.handler.ViewAdRequestHandler;
import com.sandun.adsSystem.model.enums.AdStatus;
import android.content.Context;
import android.content.pm.ApplicationInfo;

public class AdsMediator {
    private static AdsMediator adsMediator;
    public AdsInitializer initializer;
    private java.lang.ref.WeakReference<Activity> activityRef;
    private Application application;

    public Activity getActivity() {
        return activityRef != null ? activityRef.get() : null;
    }

    public Context getContext() {
        Activity act = getActivity();
        return act != null ? act : application;
    }

    private PreLoader preLoader;
    private AdStatus adStatus = AdStatus.ACTIVE;
    private AdMethodType adMethodType;
    private int loadingLayoutId = R.layout.dialog_loading_ads_layout;

    private AdsMediator() {
        preLoader = new PreLoader(this);
    }

    public static AdsMediator getInstance(Application app, AdsInitializer initializer) {
        init(app, initializer);
        return adsMediator;
    }

    public static AdsMediator getInstance() {
        return adsMediator;
    }

    private static void init(Application app, AdsInitializer initializer) {
        if (adsMediator == null) {
            adsMediator = new AdsMediator();
            adsMediator.application = app;
            AudienceNetworkAds.initialize(app);
            try {
                com.google.android.gms.ads.MobileAds.initialize(app, status -> {
                    System.out.println("Google Mobile Ads SDK initialized.");
                });
            } catch (Throwable e) {
                e.printStackTrace();
            }
            app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, android.os.Bundle savedInstanceState) {
                    adsMediator.activityRef = new java.lang.ref.WeakReference<>(activity);
                }

                @Override
                public void onActivityStarted(Activity activity) {
                    adsMediator.activityRef = new java.lang.ref.WeakReference<>(activity);
                }

                @Override
                public void onActivityResumed(Activity activity) {
                    adsMediator.activityRef = new java.lang.ref.WeakReference<>(activity);
                }

                @Override
                public void onActivityPaused(Activity activity) {}

                @Override
                public void onActivityStopped(Activity activity) {}

                @Override
                public void onActivitySaveInstanceState(Activity activity, android.os.Bundle outState) {}

                @Override
                public void onActivityDestroyed(Activity activity) {
                    if (adsMediator.activityRef != null && adsMediator.activityRef.get() == activity) {
                        adsMediator.activityRef.clear();
                    }
                }
            });
        }
        if (initializer != null) {
            adsMediator.initializer = initializer;
            adsMediator.fetchVariablesFromBackend(app, initializer);
        }
    }

    private void fetchVariablesFromBackend(android.content.Context context, AdsInitializer initializer) {
        if (initializer.getBackendUrl() == null || initializer.getBackendUrl().isEmpty()) {
            return;
        }

        String url = initializer.getBackendUrl() + "/api/initialize/" + initializer.getAppId();
        com.android.volley.RequestQueue queue = VolleySingleton.getInstance(context).getRequestQueue();

        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
            com.android.volley.Request.Method.GET,
            url,
            null,
            new com.android.volley.Response.Listener<org.json.JSONObject>() {
                @Override
                public void onResponse(org.json.JSONObject response) {
                    try {
                        if (response.has("variables")) {
                            org.json.JSONObject vars = response.getJSONObject("variables");
                            if (vars.has("ad_frequency")) {
                                int freq = vars.getInt("ad_frequency");
                                initializer.setAdFrequency(freq);
                                System.out.println("Initialized adFrequency to " + freq);
                            }
                            if (vars.has("personal_ads_active")) {
                                boolean active = vars.getBoolean("personal_ads_active");
                                initializer.setPersonalAdsActive(active);
                                System.out.println("Initialized personalAdsActive to " + active);
                            }
                        }
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                    }
                }
            },
            new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(com.android.volley.VolleyError error) {
                    System.out.println("Failed to fetch ad config from backend: " + error.getMessage());
                }
            }
        );
        queue.add(request);
    }


    public void setAdStatus(AdStatus adStatus) {
        this.adStatus = adStatus;
    }

    public AdStatus getAdStatus() {
        return adStatus;
    }

    public PreLoader getPreLoader() {
        return preLoader;
    }

    public AdStatus resolveEffectiveStatus(Context context) {
        if (adStatus == AdStatus.HYBRID) {
            if (context != null) {
                boolean isDebug = (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
                return isDebug ? AdStatus.TESTING : AdStatus.ACTIVE;
            }
            return AdStatus.ACTIVE;
        }
        return adStatus;
    }

    public AdsInitializer.GoogleIds getEffectiveGoogleIds(Context context) {
        AdStatus effective = resolveEffectiveStatus(context);
        if (effective == AdStatus.TESTING) {
            return AdsInitializer.getTestGoogleIds();
        }
        return initializer != null ? initializer.getGoogleIds() : null;
    }

    public AdsInitializer.FacebookIds getEffectiveFacebookIds(Context context) {
        AdStatus effectiveStatus = resolveEffectiveStatus(context);
        try {
            boolean isTest = (effectiveStatus == AdStatus.TESTING);
            com.facebook.ads.AdSettings.setTestMode(isTest);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return initializer != null ? initializer.getFacebookIds() : null;
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
        if (resolveEffectiveStatus(getContext()) != AdStatus.DISABLE) {
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
        if (resolveEffectiveStatus(getContext()) != AdStatus.DISABLE) {
            if (getActivity() == null) {
                System.out.println("Cannot show Interstitial Ad: Active Activity is null");
                handler.onError();
                return;
            }
            InterstitialAd ad = new InterstitialAd(this, adMethodType, preLoader.getInterstitialAd());
            new ErrorHandler(ad, handler, this);
        } else {
            handler.onSuccess();
        }
    }

    public void showRewardAd(AdRequestHandler handler) {
        if (resolveEffectiveStatus(getContext()) != AdStatus.DISABLE) {
            if (getActivity() == null) {
                System.out.println("Cannot show Reward Ad: Active Activity is null");
                handler.onError();
                return;
            }
            RewardAd ad = new RewardAd(this, adMethodType, preLoader.getRewardAds());
            new ErrorHandler(ad, handler, this);
        } else {
            handler.onSuccess();
        }
    }

    public void showOpenAd(AdRequestHandler handler) {
        if (resolveEffectiveStatus(getContext()) != AdStatus.DISABLE) {
            if (getActivity() == null) {
                System.out.println("Cannot show Open Ad: Active Activity is null");
                handler.onError();
                return;
            }
            OpenAd ad = new OpenAd(this, adMethodType, preLoader.getOpenAds());
            new ErrorHandler(ad, handler, this);
        } else {
            handler.onSuccess();
        }
    }

    public void showNativeAd(ViewAdRequestHandler handler, LinearLayout container) {
        showNativeAd(handler, container, false);
    }

    public void showNativeAd(ViewAdRequestHandler handler, LinearLayout container, boolean isMedium) {
        if (resolveEffectiveStatus(getContext()) != AdStatus.DISABLE) {
            if (getActivity() == null) {
                System.out.println("Cannot show Native Ad: Active Activity is null");
                handler.onError();
                return;
            }
            NativeAd ad = new NativeAd(this, adMethodType, preLoader.getOpenAds(), container, isMedium);
            new ErrorHandler(ad, handler, this);
        } else {
            handler.onSuccess();
        }
    }

    public void showBannerAd(ViewAdRequestHandler handler, LinearLayout container) {
        if (resolveEffectiveStatus(getContext()) != AdStatus.DISABLE) {
            if (getActivity() == null) {
                System.out.println("Cannot show Banner Ad: Active Activity is null");
                handler.onError();
                return;
            }
            BannerAd ad = new BannerAd(this, adMethodType, preLoader.getOpenAds(), container);
            new ErrorHandler(ad, handler, this);
        } else {
            handler.onSuccess();
        }
    }

}
