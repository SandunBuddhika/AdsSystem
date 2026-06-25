package com.sandun.adsSystem;

public class AdsInitializer {

    private static AdsInitializer initializer;
    private FacebookIds facebookIds;
    private GoogleIds googleIds;
    private String backendUrl = "";
    private int appId = 0;
    private int adFrequency = 5;
    private boolean personalAdsActive = true;

    private AdsInitializer(FacebookIds facebookIds, GoogleIds googleIds) {
        this.facebookIds = facebookIds == null ? new AdsInitializer.FacebookIds("123", "123", "123", "123") : facebookIds;
        this.googleIds = googleIds;
        checkNull();
    }

    public void checkNull() {
        if (this.googleIds == null) {
            this.googleIds = new GoogleIds();
        }
        if (this.facebookIds == null) {
            this.facebookIds = new FacebookIds();
        }
    }

    public static AdsInitializer getInstance(FacebookIds facebookIds, GoogleIds googleIds) {
        if (initializer == null) {
            initializer = new AdsInitializer(facebookIds, googleIds);
        }
        return initializer;
    }

    public static AdsInitializer getInstance() {
        return initializer;
    }

    public FacebookIds getFacebookIds() {
        return facebookIds;
    }

    public void setFacebookIds(FacebookIds facebookIds) {
        this.facebookIds = facebookIds;
    }

    public GoogleIds getGoogleIds() {
        return googleIds;
    }

    public static GoogleIds getTestGoogleIds() {
        return new GoogleIds(
            "ca-app-pub-3940256099942544~3347511713",
            "ca-app-pub-3940256099942544/1033173712",
            "ca-app-pub-3940256099942544/6300978111",
            "ca-app-pub-3940256099942544/9257395921",
            "ca-app-pub-3940256099942544/5224354917",
            "ca-app-pub-3940256099942544/2247696110"
        );
    }

    public void setGoogleIds(GoogleIds googleIds) {
        this.googleIds = googleIds;
    }

    public static class GoogleIds {
        private String appId;
        private String initId;
        private String bannerId;
        private String appOpenId;
        private String rewardId;
        private String nativeId;

        private GoogleIds() {
            checkNull();
        }

        public GoogleIds(String appId, String initId, String bannerId, String appOpenId, String rewardId, String nativeId) {
            this.appId = appId;
            this.initId = initId;
            this.bannerId = bannerId;
            this.appOpenId = appOpenId;
            this.rewardId = rewardId;
            this.nativeId = nativeId;
            checkNull();
        }

        public void checkNull() {
            if (this.appId == null) {
                this.appId = "";
            }
            if (this.initId == null) {
                this.initId = "";
            }
            if (this.bannerId == null) {
                this.bannerId = "";
            }
            if (this.appOpenId == null) {
                this.appOpenId = "";
            }
            if (this.rewardId == null) {
                this.rewardId = "";
            }
            if (this.nativeId == null) {
                this.nativeId = "";
            }
        }

        public void setAppId(String id) {
            appId = id;
        }

        public void setInitId(String id) {
            initId = id;
        }

        public void setBannerId(String id) {
            bannerId = id;
        }

        public void setOpenId(String id) {
            appOpenId = id;
        }

        public void setRewardId(String id) {
            rewardId = id;
        }

        public void setNativeId(String id) {
            nativeId = id;
        }

        public String getAppId() {
            return appId;
        }

        public String getInitId() {
            return initId;
        }

        public String getBannerId() {
            return bannerId;
        }

        public String getAppOpenId() {
            return appOpenId;
        }

        public String getRewardId() {
            return rewardId;
        }

        public String getNativeId() {
            return nativeId;
        }
    }

    public static class FacebookIds {
        private String appId;
        private String initId;
        private String bannerId;
        private String nativeId;

        private FacebookIds() {
            checkNull();
        }

        public FacebookIds(String appId, String initId, String bannerId, String nativeId) {
            this.appId = appId;
            this.initId = initId;
            this.bannerId = bannerId;
            this.nativeId = nativeId;

            checkNull();
        }

        public void checkNull() {
            if (this.appId == null) {
                this.appId = "";
            }
            if (this.initId == null) {
                this.initId = "";
            }
            if (this.bannerId == null) {
                this.bannerId = "";
            }
            if (this.nativeId == null) {
                this.nativeId = "";
            }
        }

        public void setAppId(String id) {
            appId = id;
        }

        public void setInitId(String id) {
            initId = id;
        }

        public void setBannerId(String id) {
            bannerId = id;
        }

        public void setNativeId(String id) {
            nativeId = id;
        }

        public String getAppId() {
            return appId;
        }

        public String getInitId() {
            return initId;
        }

        public String getBannerId() {
            return bannerId;
        }

        public String getNativeId() {
            return nativeId;
        }

    }

    public static AdsInitializer getInstance(FacebookIds facebookIds, GoogleIds googleIds, String backendUrl, int appId) {
        if (initializer == null) {
            initializer = new AdsInitializer(facebookIds, googleIds);
        }
        initializer.backendUrl = backendUrl;
        initializer.appId = appId;
        return initializer;
    }

    public String getBackendUrl() {
        return backendUrl;
    }

    public int getAppId() {
        return appId;
    }

    public int getAdFrequency() {
        return adFrequency;
    }

    public void setAdFrequency(int adFrequency) {
        this.adFrequency = adFrequency;
    }

    public boolean isPersonalAdsActive() {
        return personalAdsActive;
    }

    public void setPersonalAdsActive(boolean personalAdsActive) {
        this.personalAdsActive = personalAdsActive;
    }
}

