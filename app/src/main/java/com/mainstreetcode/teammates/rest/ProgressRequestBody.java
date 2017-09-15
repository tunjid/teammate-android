package com.mainstreetcode.teammates.rest;


import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProgressRequestBody extends RequestBody {

    private static final int UPLOAD_BUFFER_SIZE = 2048;

    private int numWriteToCalls;
    private final int numWriteToCallsToIgnore;

    private final File file;
    private final MediaType mediaType;
    private final PublishProcessor<Integer> floatPublishSubject = PublishProcessor.create();

    public ProgressRequestBody(File file, int numWriteToCallsToIgnore, MediaType mediaType) {
        this.file = file;
        this.numWriteToCallsToIgnore = numWriteToCallsToIgnore;
        this.mediaType = mediaType;
    }

    public Flowable<Integer> getProgressSubject() {
        return floatPublishSubject;
    }

    @Override
    public MediaType contentType() {
        return mediaType;
    }

    @Override
    public long contentLength() throws IOException {
        return file.length();
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        numWriteToCalls++;

        // when using HttpLoggingInterceptor it calls writeTo and passes data into
        // a local buffer just for logging purposes.
        // the second call to write to is the progress we actually want to track
        boolean isUploading = numWriteToCalls > numWriteToCallsToIgnore;

        float uploaded = 0;
        float fileLength = file.length();
        byte[] buffer = new byte[UPLOAD_BUFFER_SIZE];

        try (FileInputStream in = new FileInputStream(file)) {
            float overallProgress = 0;
            int read;
            read = in.read(buffer);

            while (read != -1) {
                uploaded += read;
                sink.write(buffer, 0, read);
                read = in.read(buffer);

                if (!isUploading) continue;
                float currentProgress = (uploaded / fileLength) * 100;

                // prevent publishing too many updates, which slows upload,
                // by checking if the upload has progressed by at least 1 percent
                if (currentProgress - overallProgress <= 1) continue;

                // publish progress
                floatPublishSubject.onNext((int) currentProgress);
                overallProgress = currentProgress;
            }

            if (isUploading) floatPublishSubject.onComplete();
        }
        catch (Exception exception) {
            floatPublishSubject.onError(exception);
            throw exception;
        }
    }
}

