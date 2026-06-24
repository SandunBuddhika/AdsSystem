package com.sandun.adsSystem.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.CountDownTimer;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.sandun.adsSystem.R;
import com.sandun.adsSystem.model.handler.AdRequestHandler;
import com.sandun.adsSystem.model.VolleySingleton;
import org.json.JSONObject;

public class AdDialog {
    private Dialog dialog;
    private Context context;
    private CountDownTimer countDownTimer;
    private final long START_TIME_IN_MILLIS = 5000;
    private AdRequestHandler currentHandler;
    private Runnable onDismissCallback;

    public AdDialog(Context context) {
        this.context = context;
    }

    public void show(String backendUrl, int appId, String adTypeStr, AdRequestHandler handler, LoadingDialog loadingDialog, Runnable onFailedFallback) {
        this.currentHandler = handler;
        System.out.println("Fetching....1");

        if (loadingDialog != null) {
            loadingDialog.show();
        }

        if (backendUrl == null || backendUrl.isEmpty()) {
            if (loadingDialog != null) {
                loadingDialog.dismiss();
            }
            onFailedFallback.run();
            return;
        }
        System.out.println("Fetching....2");

        // Fetch ad details metadata from API
        String detailsUrl = backendUrl + "/api/get-personal-ad/" + appId + "/" + adTypeStr;
        System.out.println(detailsUrl);
        RequestQueue queue = VolleySingleton.getInstance(context).getRequestQueue();

        JsonObjectRequest jsonRequest = new JsonObjectRequest(
            Request.Method.GET,
            detailsUrl,
            null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    System.out.println(response);
                    try {
                        if (response.has("ad")) {
                            JSONObject adObj = response.getJSONObject("ad");
                            String imageUrl = adObj.getString("image_url");
                            String clickUrl = adObj.optString("click_url", "");
                            downloadAdImageAndShow(imageUrl, clickUrl, loadingDialog, onFailedFallback);
                        } else {
                            if (loadingDialog != null) {
                                loadingDialog.dismiss();
                            }
                            onFailedFallback.run();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (loadingDialog != null) {
                            loadingDialog.dismiss();
                        }
                        onFailedFallback.run();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("AdDialog failed to load ad details: " + error.getMessage());
                    if (loadingDialog != null) {
                        loadingDialog.dismiss();
                    }
                    onFailedFallback.run();
                }
            }
        );
        queue.add(jsonRequest);
    }

    private void downloadAdImageAndShow(String imageUrl, String clickUrl, LoadingDialog loadingDialog, Runnable onFailedFallback) {
        RequestQueue queue = VolleySingleton.getInstance(context).getRequestQueue();
        ImageRequest imageRequest = new ImageRequest(
            imageUrl,
            new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    if (loadingDialog != null) {
                        loadingDialog.dismiss();
                    }
                    if (response != null) {
                        renderDialog(response, clickUrl);
                    } else {
                        onFailedFallback.run();
                    }
                }
            },
            0,
            0,
            ImageView.ScaleType.FIT_CENTER,
            Bitmap.Config.RGB_565,
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("AdDialog failed to download image: " + error.getMessage());
                    if (loadingDialog != null) {
                        loadingDialog.dismiss();
                    }
                    onFailedFallback.run();
                }
            }
        );
        queue.add(imageRequest);
    }

    private void renderDialog(Bitmap adBitmap, String clickUrl) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.full_screen_ad);
        dialog.setCancelable(false);

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(params);
        dialog.show();

        ImageView nextBtn = dialog.findViewById(R.id.skip_icon);
        ImageView adContainer = dialog.findViewById(R.id.ad_container);

        adContainer.setImageBitmap(adBitmap);

        // Click-through logic
        if (clickUrl != null && !clickUrl.isEmpty()) {
            adContainer.setOnClickListener(v -> {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(clickUrl));
                    context.startActivity(browserIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            
            // Support "Open" button card click
            android.view.View openButton = null;
            android.view.ViewParent parent1 = dialog.findViewById(R.id.ad_container).getParent();
            if (parent1 instanceof android.view.ViewGroup) {
                android.view.ViewParent parent2 = parent1.getParent();
                if (parent2 instanceof android.view.ViewGroup) {
                    openButton = ((android.view.ViewGroup) parent2).findViewWithTag("open_tag");
                }
            }
            if (openButton != null) {
                openButton.setOnClickListener(v -> {
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(clickUrl));
                        context.startActivity(browserIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        nextBtn.setOnClickListener(btn -> {
            dismiss();
            if (currentHandler != null) {
                currentHandler.onSuccess();
            }
        });

        ProgressBar progressBar = dialog.findViewById(R.id.timer_progress);
        TextView timerText = dialog.findViewById(R.id.timer_text);
        RelativeLayout timerContainer = dialog.findViewById(R.id.timer_container);

        countDownTimer = new CountDownTimer(START_TIME_IN_MILLIS, 100) {
            @Override
            public void onTick(long l) {
                timerText.setText(String.valueOf((l / 1000) + 1));
                int progress = (int) (((double) l / (double) START_TIME_IN_MILLIS) * 100.0);
                progressBar.setProgress(100 - progress);
            }

            @Override
            public void onFinish() {
                nextBtn.setVisibility(TextView.VISIBLE);
                timerContainer.setVisibility(android.view.View.INVISIBLE);
                progressBar.setProgress(100);
            }
        }.start();
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            try {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
