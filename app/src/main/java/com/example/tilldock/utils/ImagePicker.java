package com.example.tilldock.utils;

import android.content.Context;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;

public final class ImagePicker {

    public interface CapturedFileCallback {
        void onCaptured(File file, Uri uri);
    }

    public static class Result {
        public final ImageUtil.Prepared prepared;
        public final File cacheFile;

        Result(ImageUtil.Prepared prepared, File cacheFile) {
            this.prepared = prepared;
            this.cacheFile = cacheFile;
        }
    }

    private ImagePicker() {
    }

    public static ActivityResultLauncher<String> pickGallery(Fragment fragment, ImagePickCallback callback) {
        return fragment.registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri == null) {
                callback.onCancelled();
                return;
            }
            try {
                ImageUtil.Prepared prepared = ImageUtil.fromUri(fragment.requireContext(), uri);
                callback.onPicked(prepared);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public static ActivityResultLauncher<Uri> takePhoto(Fragment fragment, CaptureCallback callback) {
        return fragment.registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success == null || !success) {
                callback.onCancelled();
                return;
            }
            try {
                ImageUtil.Prepared prepared = ImageUtil.fromUri(fragment.requireContext(), callback.targetUri());
                callback.onCaptured(prepared);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public static Uri createCaptureUri(Context context) throws IOException {
        File dir = new File(context.getCacheDir(), "capture");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Cannot create capture directory");
        }
        File file = new File(dir, "capture_" + System.currentTimeMillis() + ".jpg");
        if (file.exists()) {
            if (!file.delete()) throw new IOException("Cannot create capture file");
        }
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
    }

    public interface ImagePickCallback {
        void onPicked(ImageUtil.Prepared prepared);

        void onCancelled();

        void onError(String message);
    }

    public interface CaptureCallback {
        Uri targetUri();

        void onCaptured(ImageUtil.Prepared prepared);

        void onCancelled();

        void onError(String message);
    }
}
