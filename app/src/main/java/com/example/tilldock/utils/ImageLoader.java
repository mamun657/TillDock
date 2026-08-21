package com.example.tilldock.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ImageLoader {

    private static final ImageLoader INSTANCE = new ImageLoader();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Handler main = new Handler(Looper.getMainLooper());
    private final LruCache<String, Bitmap> cache;

    private ImageLoader() {
        int maxKb = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheKb = maxKb / 8;
        cache = new LruCache<String, Bitmap>(cacheKb) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }

    public static ImageLoader get() {
        return INSTANCE;
    }

    public void load(String imageUrl, ImageView target) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            target.setImageDrawable(null);
            return;
        }
        String resolved = resolveUrl(imageUrl);
        Bitmap cached = cache.get(resolved);
        if (cached != null) {
            target.setImageBitmap(cached);
            return;
        }
        target.setTag(resolved);
        target.setImageDrawable(null);
        executor.execute(() -> {
            Bitmap bmp = download(resolved);
            main.post(() -> {
                if (resolved.equals(target.getTag())) {
                    if (bmp != null) {
                        cache.put(resolved, bmp);
                        target.setImageBitmap(bmp);
                    }
                }
            });
        });
    }

    private static String resolveUrl(String imageUrl) {
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl;
        }
        return "http://10.0.2.2:8080" + (imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl);
    }

    private static Bitmap download(String urlStr) {
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestMethod("GET");
            conn.connect();
            if (conn.getResponseCode() != 200) return null;
            is = conn.getInputStream();
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, bounds);
            int sample = 1;
            while (bounds.outWidth / sample > 1280 || bounds.outHeight / sample > 1280) {
                sample *= 2;
            }
            conn.disconnect();
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.connect();
            is = conn.getInputStream();
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sample;
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeStream(is, null, opts);
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ignored) {
            }
            if (conn != null) conn.disconnect();
        }
    }

    public void clear() {
        cache.evictAll();
    }
}
