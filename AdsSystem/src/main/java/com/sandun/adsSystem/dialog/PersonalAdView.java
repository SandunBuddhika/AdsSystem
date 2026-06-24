package com.sandun.adsSystem.dialog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sandun.adsSystem.R;
import com.sandun.adsSystem.model.VolleySingleton;
import com.sandun.adsSystem.model.handler.AdRequestHandler;
import com.sandun.adsSystem.model.handler.ViewAdRequestHandler;

import org.json.JSONObject;

/**
 * Renders personal ads inline within a LinearLayout container.
 * Used by BannerAd and NativeAd for non-fullscreen personal ad slots.
 */
public class PersonalAdView {

    private Context context;

    public PersonalAdView(Context context) {
        this.context = context;
    }

    /**
     * Loads and renders a personal banner ad into the given container.
     * 
     * @param container   The LinearLayout to render the ad into
     * @param backendUrl  Backend base URL
     * @param appId       App identifier for the backend
     * @param handler     The request handler to notify success
     * @param onFailed    Called if the ad fails to load (so caller can fallback to network ads)
     */
    public void showBanner(LinearLayout container, String backendUrl, int appId, AdRequestHandler handler, Runnable onFailed) {
        fetchAndRender(container, backendUrl, appId, "BANNER", R.layout.personal_banner_ad,
                R.id.personal_banner_image, R.id.personal_banner_cta, handler, onFailed);
    }

    /**
     * Loads and renders a personal native ad into the given container.
     * 
     * @param container   The LinearLayout to render the ad into
     * @param backendUrl  Backend base URL
     * @param appId       App database ID
     * @param isMedium    Whether the native ad format is medium size
     * @param handler     The request handler to notify success
     * @param onFailed    Called if the ad fails to load (so caller can fallback to network ads)
     */
    public void showNative(LinearLayout container, String backendUrl, int appId, boolean isMedium, AdRequestHandler handler, Runnable onFailed) {
        String adTypeStr = isMedium ? "NATIVE_MEDIUM" : "NATIVE_SMALL";
        fetchAndRender(container, backendUrl, appId, adTypeStr, R.layout.personal_native_ad,
                R.id.personal_native_image, R.id.personal_native_cta, handler, onFailed);
    }

    private void fetchAndRender(LinearLayout container, String backendUrl, int appId,
                                 String adTypeStr, int layoutResId, int imageViewId, int ctaViewId,
                                 AdRequestHandler handler, Runnable onFailed) {
        if (backendUrl == null || backendUrl.isEmpty()) {
            onFailed.run();
            return;
        }

        String detailsUrl = backendUrl + "/api/get-personal-ad/" + appId + "/" + adTypeStr;
        System.out.println("PersonalAdView fetching: " + detailsUrl);

        RequestQueue queue = VolleySingleton.getInstance(context).getRequestQueue();
        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET,
                detailsUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.has("ad")) {
                                JSONObject adObj = response.getJSONObject("ad");
                                String imageUrl = adObj.getString("image_url");
                                String clickUrl = adObj.optString("click_url", "");
                                downloadAndInflate(container, imageUrl, clickUrl, layoutResId, imageViewId, ctaViewId, handler, onFailed);
                            } else {
                                onFailed.run();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            onFailed.run();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("PersonalAdView failed to fetch metadata: " + error.getMessage());
                        onFailed.run();
                    }
                }
        );
        queue.add(jsonRequest);
    }

    private void downloadAndInflate(LinearLayout container, String imageUrl, String clickUrl,
                                     int layoutResId, int imageViewId, int ctaViewId, AdRequestHandler handler, Runnable onFailed) {
        RequestQueue queue = VolleySingleton.getInstance(context).getRequestQueue();
        ImageRequest imageRequest = new ImageRequest(
                imageUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        if (bitmap != null) {
                            inflateView(container, bitmap, clickUrl, layoutResId, imageViewId, ctaViewId, handler);
                        } else {
                            onFailed.run();
                        }
                    }
                },
                0, 0,
                ImageView.ScaleType.CENTER_CROP,
                Bitmap.Config.RGB_565,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("PersonalAdView failed to download image: " + error.getMessage());
                        onFailed.run();
                    }
                }
        );
        queue.add(imageRequest);
    }

    private void inflateView(LinearLayout container, Bitmap bitmap, String clickUrl,
                              int layoutResId, int imageViewId, int ctaViewId, AdRequestHandler handler) {
        View adView = LayoutInflater.from(context).inflate(layoutResId, container, false);
        ImageView imageView = adView.findViewById(imageViewId);
        View ctaButton = adView.findViewById(ctaViewId);

        imageView.setImageBitmap(bitmap);

        View.OnClickListener clickListener = null;
        if (clickUrl != null && !clickUrl.isEmpty()) {
            clickListener = v -> {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(clickUrl));
                    context.startActivity(browserIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            imageView.setOnClickListener(clickListener);
            if (ctaButton != null) {
                ctaButton.setOnClickListener(clickListener);
            }
        }

        container.removeAllViews();
        container.addView(adView);
        System.out.println("PersonalAdView: rendered inline personal ad successfully");

        if (handler != null) {
            if (handler instanceof ViewAdRequestHandler) {
                ((ViewAdRequestHandler) handler).viewHandler(adView);
            }
            handler.onSuccess();
        }
    }
}
