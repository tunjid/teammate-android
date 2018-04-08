package com.mainstreetcode.teammate.notifications;


import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.mainstreetcode.teammate.MediaTransferIntentService;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Media;
import com.mainstreetcode.teammate.repository.MediaRepository;
import com.mainstreetcode.teammate.repository.ModelRepository;
import com.mainstreetcode.teammate.rest.ProgressRequestBody;
import com.mainstreetcode.teammate.util.ErrorHandler;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Single;
import okhttp3.RequestBody;

import static android.content.Context.NOTIFICATION_SERVICE;


public class MediaNotifier extends Notifier<Media> {

    private static final String MEDIA_UPLOADS = "media_uploads";

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
        return new NotificationChannel[]{
                buildNotificationChannel(FeedItem.MEDIA, R.string.media, R.string.media_notifier_description, NotificationManager.IMPORTANCE_MIN),
                buildNotificationChannel(MEDIA_UPLOADS, R.string.media_upload_channel_name, R.string.media_upload_channel_description, NotificationManager.IMPORTANCE_LOW)};
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
        MediaTransferIntentService.UploadStats stats = MediaTransferIntentService.getStats();

        notifyOfUpload(progressNotificationBuilder()
                .setContentText(app.getString(R.string.upload_progress_status, stats.getNumAttempted(), stats.getNumToUpload(), stats.getNumErrors()))
                .setContentTitle(app.getString(R.string.uploading_media))
                .setProgress(100, percentage, false));
    }

    private void onUploadComplete() {
        MediaTransferIntentService.UploadStats stats = MediaTransferIntentService.getStats();
        if (!stats.isComplete()) return;

        Completable.timer(800, TimeUnit.MILLISECONDS).subscribe(
                () -> notifyOfUpload(progressNotificationBuilder()
                        .setContentText(getUploadCompletionContentText(stats))
                        .setContentTitle(getUploadCompletionContentTitle(stats))
                        .setProgress(0, 0, false)),
                ErrorHandler.EMPTY);
    }

    private NotificationCompat.Builder progressNotificationBuilder() {
        return new NotificationCompat.Builder(app, FeedItem.MEDIA)
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSmallIcon(R.drawable.ic_notification)
                .setChannelId(MEDIA_UPLOADS);
    }

    private void notifyOfUpload(NotificationCompat.Builder builder) {
        NotificationManager notifier = (NotificationManager) app.getSystemService(NOTIFICATION_SERVICE);
        if (notifier != null) notifier.notify(1, builder.build());
    }

    @NonNull
    private String getUploadCompletionContentTitle(MediaTransferIntentService.UploadStats stats) {
        return stats.isAtMaxStorage() ? app.getString(R.string.upload_failed): app.getString(R.string.upload_complete);
    }

    @NonNull
    private String getUploadCompletionContentText(MediaTransferIntentService.UploadStats stats) {
        return stats.isAtMaxStorage() ? stats.getMaxStorageMessage() : app.getString(R.string.upload_complete_status, stats.getNumErrors());
    }
}
