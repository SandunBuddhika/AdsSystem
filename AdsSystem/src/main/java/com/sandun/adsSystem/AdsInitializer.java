package com.sandun.adsSystem;

public class AdsInitializer {

    private static AdsInitializer initializer;
    private FacebookIds facebookIds;
    private GoogleIds googleIds;

    private AdsInitializer(FacebookIds facebookIds, GoogleIds googleIds) {
        this.facebookIds = facebookIds;
        this.googleIds = googleIds;
        checkNull();
    }

    public void checkNull() {
        if (this.googleIds == null) {
            this.googleIds =new GoogleIds();
        }
        if (this.facebookIds == null) {
            this.facebookIds =new FacebookIds();
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

}

