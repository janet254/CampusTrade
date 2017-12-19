package com.janet.campustrade;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by Janet on 19/11/2017.
 */

public class Singleton {
    private static Singleton singleton;
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;
    private static Context mContext;

    private Singleton(Context context){
        mContext = context;
        requestQueue = getRequestQueue();

        imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap>
            cache = new LruCache<>(20);
            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });
    }

    public static synchronized  Singleton getInstance(Context context){
        if(singleton == null){
            singleton = new Singleton(context);
        }
        return singleton;
    }

    public RequestQueue getRequestQueue(){
        if (requestQueue == null){
            requestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request, String tag){
        request.setTag(tag);
        getRequestQueue().add(request);
    }

    public ImageLoader getImageLoader(){
        return imageLoader;
    }

    public void cancelPendingRequests(Object tag){
        if (requestQueue != null){
            requestQueue.cancelAll(tag);
        }
    }
}
