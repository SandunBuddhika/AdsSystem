package com.sandun.adsSystem.model;

import com.sandun.adsSystem.model.adsModel.AdsCompact;
import com.sandun.adsSystem.model.handler.AdRequestHandler;

public class ErrorHandler {
    private int count;
    private AdsCompact adsCompact;
    private AdRequestHandler handler;
    private AdsMediator adsMediator;

    public ErrorHandler(AdsCompact adsCompact, AdRequestHandler handle, AdsMediator adsMediator) {
        this.adsCompact = adsCompact;
        this.handler = handle;
        this.adsMediator = adsMediator;
        process();
    }

    public void onFailed() {
        if (count < 2) {
            count++;
            adsCompact.changeType();
            process();
        } else {
            handler.onError();
        }
    }

    public void process() {
        adsCompact.showAds(handler, this);
    }
}
