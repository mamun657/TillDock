package com.example.tilldock.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public final class ImageUtil {

    private static final String TAG = "ImageUtil";
    private static final long MAX_BYTES = 4L * 1024L * 1024L;
    private static final int MAX_DIMENSION = 1280;
    private static final int JPEG_QUALITY = 85;

    private ImageUtil() {
    }

    public static class Prepared {
        public final byte[] bytes;
        public final String mimeType;
        public final String filename;
        public final int originalWidth;
        public final int originalHeight;

        Prepared(byte[] bytes, String mimeType, String filename, int w, int h) {
            this.bytes = bytes;
            this.mimeType = mimeType;
            this.filename = filename;
            this.originalWidth = w;
            this.originalHeight = h;
        }
    }

    public static Prepared fromUri(Context context, Uri uri) throws IOException {
        if (uri == null) throw new IllegalArgumentException("Image is required");
        Bitmap bitmap = decodeSampledBitmap(context, uri);
        if (bitmap == null) throw new IllegalArgumentException("Image could not be loaded");
        Bitmap rotated = applyExifRotation(context, uri, bitmap);
        Bitmap resized = resize(rotated, MAX_DIMENSION);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out);
        int w = resized.getWidth();
        int h = resized.getHeight();
        if (resized != rotated) resized.recycle();
        if (rotated != bitmap) rotated.recycle();
        bitmap.recycle();
        byte[] bytes = out.toByteArray();
        if (bytes.length > MAX_BYTES) {
            throw new IllegalArgumentException("Image is too large");
        }
        return new Prepared(bytes, "image/jpeg", "product_" + UUID.randomUUID() + ".jpg", w, h);
    }

    public static File writeToCache(Context context, Prepared prepared) throws IOException {
        File dir = new File(context.getCacheDir(), "capture");
        if (!dir.exists()) {
            if (!dir.mkdirs()) throw new IOException("Cannot create cache directory");
        }
        File file = new File(dir, prepared.filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(prepared.bytes);
        }
        return file;
    }

    private static Bitmap decodeSampledBitmap(Context context, Uri uri) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            if (is == null) return null;
            BitmapFactory.decodeStream(is, null, bounds);
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;
        int sample = 1;
        while (bounds.outWidth / sample > MAX_DIMENSION * 2 || bounds.outHeight / sample > MAX_DIMENSION * 2) {
            sample *= 2;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = sample;
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            if (is == null) return null;
            return BitmapFactory.decodeStream(is, null, opts);
        }
    }

    private static Bitmap applyExifRotation(Context context, Uri uri, Bitmap bitmap) {
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            if (is == null) return bitmap;
            ExifInterface exif = new ExifInterface(is);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return bitmap;
            }
            Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (rotated != bitmap) bitmap.recycle();
            return rotated;
        } catch (IOException e) {
            Log.w(TAG, "Failed to read EXIF", e);
            return bitmap;
        }
    }

    private static Bitmap resize(Bitmap bitmap, int max) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w <= max && h <= max) return bitmap;
        if (w >= h) {
            int newH = Math.round((float) max * h / w);
            return Bitmap.createScaledBitmap(bitmap, max, newH, true);
        } else {
            int newW = Math.round((float) max * w / h);
            return Bitmap.createScaledBitmap(bitmap, newW, max, true);
        }
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
    }
}
