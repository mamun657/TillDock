package com.example.tilldock.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.example.tilldock.BuildConfig;

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
            Log.w("ImageLoader", "load called with empty url");
            target.setImageDrawable(null);
            return;
        }
        String resolved = resolveUrl(imageUrl);
        Log.d("ImageLoader", "load url=" + resolved);
        Bitmap cached = cache.get(resolved);
        if (cached != null) {
            Log.i("ImageLoader", "cache hit size=" + cached.getWidth() + "x" + cached.getHeight());
            target.setImageBitmap(cached);
            return;
        }
        target.setTag(resolved);
        target.setImageDrawable(null);
        executor.execute(() -> {
            Bitmap bmp = download(resolved);
            int w = bmp == null ? -1 : bmp.getWidth();
            int h = bmp == null ? -1 : bmp.getHeight();
            Log.i("ImageLoader", "downloaded size=" + w + "x" + h);
            main.post(() -> {
                if (resolved.equals(target.getTag())) {
                    if (bmp != null) {
                        cache.put(resolved, bmp);
                        target.setImageBitmap(bmp);
                        Log.i("ImageLoader", "setImageBitmap applied");
                    } else {
                        Log.w("ImageLoader", "download failed: " + resolved);
                    }
                } else {
                    Log.w("ImageLoader", "tag mismatch, skipping setImageBitmap");
                }
            });
        });
    }

    private static String resolveUrl(String imageUrl) {
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl;
        }
        String base = BuildConfig.API_BASE_URL;
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + (imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl);
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
            conn.setInstanceFollowRedirects(true);
            conn.connect();
            int rc = conn.getResponseCode();
            Log.d("ImageLoader", "http " + rc + " for " + urlStr);
            if (rc != 200) return null;
            byte[] body = readAll(conn.getInputStream());
            Log.d("ImageLoader", "body bytes=" + body.length);
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(body, 0, body.length, bounds);
            Log.d("ImageLoader", "decode bounds w=" + bounds.outWidth + " h=" + bounds.outHeight + " mime=" + bounds.outMimeType);
            int sample = 1;
            while (bounds.outWidth / sample > 1280 || bounds.outHeight / sample > 1280) {
                sample *= 2;
            }
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sample;
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeByteArray(body, 0, body.length, opts);
        } catch (IOException e) {
            Log.w("ImageLoader", "io error: " + e.getMessage());
            return null;
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ignored) {
            }
            if (conn != null) conn.disconnect();
        }
    }

    private static byte[] readAll(InputStream in) throws IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        try {
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
        } finally {
            try { in.close(); } catch (IOException ignored) {}
        }
        return out.toByteArray();
    }

    public void clear() {
        cache.evictAll();
    }
}
