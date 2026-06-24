package com.sandun.adsSystem.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import com.android.volley.toolbox.Volley;

public class AdsLoader {

    public void loadAd(Context context, String url) {
        RequestQueue queue = VolleySingleton.getInstance(context).getRequestQueue();

        ImageRequest imageRequest = new ImageRequest(
                url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
//                        ImageView imageView = findViewById(R.id.my_image_view);
//                        imageView.setImageBitmap(response);
                    }
                },
                0,
                0,
                ImageView.ScaleType.FIT_CENTER,
                Bitmap.Config.RGB_565,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );

        queue.add(imageRequest);
    }


}
