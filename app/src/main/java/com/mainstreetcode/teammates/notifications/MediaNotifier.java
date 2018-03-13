package com.mainstreetcode.teammates.notifications;


import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.mainstreetcode.teammates.MediaUploadIntentService;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.repository.MediaRepository;
import com.mainstreetcode.teammates.repository.ModelRepository;
import com.mainstreetcode.teammates.rest.ProgressRequestBody;
import com.mainstreetcode.teammates.util.ErrorHandler;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Single;
import okhttp3.RequestBody;

import static android.content.Context.NOTIFICATION_SERVICE;


public class MediaNotifier extends Notifier<Media> {

    private static final long[] NO_VIBRATION_PATTERN = {0L};

    private static MediaNotifier INSTANCE;

    private MediaNotifier() {}

    public static MediaNotifier getInstance() {
        if (INSTANCE == null) INSTANCE = new MediaNotifier();
        return INSTANCE;
    }

    @Override
    String getNotifyId() {return FeedItem.MEDIA;}

    @Override
    protected ModelRepository<Media> getRepository() {return MediaRepository.getInstance();}

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected NotificationChannel[] getNotificationChannels() {
        return new NotificationChannel[]{buildNotificationChannel(FeedItem.MEDIA, R.string.media, R.string.media_notifier_description, NotificationManager.IMPORTANCE_DEFAULT)};
    }

    public Single<Media> notifyOfUploads(Single<Media> mediaSingle, RequestBody requestBody) {
        if (!(requestBody instanceof ProgressRequestBody)) return mediaSingle;

        ProgressRequestBody progressRequestBody = (ProgressRequestBody) requestBody;
        progressRequestBody.getProgressSubject()
                .doFinally(this::onUploadComplete)
                .subscribe(this::updateProgress, ErrorHandler.EMPTY);

        return mediaSingle;
    }

    private void updateProgress(int percentage) {
        MediaUploadIntentService.UploadStats stats = MediaUploadIntentService.getStats();

        notifyOfUpload(progressNotificationBuilder()
                .setContentText(app.getString(R.string.upload_progress_status, stats.getNumAttempted(), stats.getNumToUpload(), stats.getNumErrors()))
                .setContentTitle(app.getString(R.string.uploading_media))
                .setProgress(100, percentage, false));
    }

    private void onUploadComplete() {
        MediaUploadIntentService.UploadStats stats = MediaUploadIntentService.getStats();
        if (!stats.isComplete()) return;

        Completable.timer(800, TimeUnit.MILLISECONDS).subscribe(
                () -> notifyOfUpload(progressNotificationBuilder()
                        .setContentText(app.getString(R.string.upload_complete_status, stats.getNumErrors()))
                        .setContentTitle(app.getString(R.string.upload_complete))
                        .setProgress(0, 0, false)),
                ErrorHandler.EMPTY);
    }

    private NotificationCompat.Builder progressNotificationBuilder() {
        return new NotificationCompat.Builder(app, FeedItem.MEDIA)
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setSmallIcon(R.drawable.ic_notification)
                .setVibrate(NO_VIBRATION_PATTERN)
                .setChannelId(FeedItem.MEDIA);
    }

    private void notifyOfUpload(NotificationCompat.Builder builder) {
        NotificationManager notifier = (NotificationManager) app.getSystemService(NOTIFICATION_SERVICE);
        if (notifier != null) notifier.notify(1, builder.build());
    }
}
