package com.mainstreetcode.teammates.notifications;


import android.app.Notification;
import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.repository.EventRepository;
import com.mainstreetcode.teammates.repository.ModelRespository;
import com.mainstreetcode.teammates.rest.ProgressRequestBody;
import com.mainstreetcode.teammates.util.ErrorHandler;

import io.reactivex.Single;
import okhttp3.RequestBody;

import static android.content.Context.NOTIFICATION_SERVICE;


public class MediaNotifier extends Notifier<Event> {

    private int uploadsInProgress;
    private int numFailed;
    private int numAttempted;

    private static MediaNotifier INSTANCE;

    private MediaNotifier() {}

    public static MediaNotifier getInstance() {
        if (INSTANCE == null) INSTANCE = new MediaNotifier();
        return INSTANCE;
    }

    public Single<Media> notifyOfUploads(Single<Media> mediaSingle, RequestBody requestBody) {
        if (!(requestBody instanceof ProgressRequestBody)) return mediaSingle;

        ProgressRequestBody progressRequestBody = (ProgressRequestBody) requestBody;

        uploadsInProgress++;

        mediaSingle = mediaSingle
                .doOnSubscribe(disposable -> numAttempted++)
                .doOnError(throwable -> numFailed++)
                .doAfterTerminate(() -> {
                    if (numAttempted == uploadsInProgress) {
                        uploadsInProgress = numAttempted = numFailed = 0;
                    }
                });

        progressRequestBody.getProgressSubject().subscribe(this::updateProgress, ErrorHandler.EMPTY, () -> updateProgress(100));

        return mediaSingle;
    }

    private void updateProgress(int percentage) {
        Notification notification = new NotificationCompat.Builder(app, "")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(app.getString(R.string.uploading_media))
                .setContentText(app.getString(R.string.upload_progress_status, numAttempted, uploadsInProgress, numFailed))
                .setProgress(100, percentage, false)
                .build();

        NotificationManager notifier = (NotificationManager) app.getSystemService(NOTIFICATION_SERVICE);
        notifier.notify(1, notification);
    }

    @Override
    protected ModelRespository<Event> getRepository() {
        return EventRepository.getInstance();
    }
}
