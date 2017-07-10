package com.mainstreetcode.teammates.repository;

import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Repository utilities
 */

class RepoUtils {
    @Nullable
    static MultipartBody.Part getBody(String path, String photokey) {
        File file = new File(path);

        if (file.exists()) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(path);
            if (extension != null) {
                String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if (type != null) {
                    RequestBody requestFile = RequestBody.create(MediaType.parse(type), file);
                    return MultipartBody.Part.createFormData(photokey, file.getName(), requestFile);
                }
            }
        }
        return null;
    }
}
