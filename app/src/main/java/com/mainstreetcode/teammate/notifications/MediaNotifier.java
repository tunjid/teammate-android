/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.notifications;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.MediaTransferIntentService;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Media;
import com.mainstreetcode.teammate.repository.ModelRepo;
import com.mainstreetcode.teammate.repository.RepoProvider;
import com.mainstreetcode.teammate.rest.ProgressRequestBody;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.viewmodel.events.Alert;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import io.reactivex.Completable;
import io.reactivex.Single;
import okhttp3.RequestBody;

import static android.content.Context.NOTIFICATION_SERVICE;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;


public class MediaNotifier extends Notifier<Media> {

    private static final String MEDIA_UPLOADS = "media_uploads";
    private static final int UPLOAD_NOTIFICATION_ID = 1;
    private static final int DOWNLOAD_NOTIFICATION_ID = 2;

    MediaNotifier() {}

    @Override
    String getNotifyId() { return FeedItem.MEDIA; }

    @Override
    protected ModelRepo<Media> getRepository() { return RepoProvider.forModel(Media.class); }

    @Override
    @TargetApi(Build.VERSION_CODES.O)
    protected NotificationChannel[] getNotificationChannels() {
        return new NotificationChannel[]{
                buildNotificationChannel(FeedItem.MEDIA, R.string.media, R.string.media_notifier_description, NotificationManager.IMPORTANCE_MIN),
                buildNotificationChannel(MEDIA_UPLOADS, R.string.media_upload_channel_name, R.string.media_upload_channel_description, NotificationManager.IMPORTANCE_LOW)};
    }

    @SuppressLint("CheckResult")
    public Single<Media> notifyOfUploads(Single<Media> mediaSingle, RequestBody requestBody) {
        if (!(requestBody instanceof ProgressRequestBody)) return mediaSingle;

        ProgressRequestBody progressRequestBody = (ProgressRequestBody) requestBody;
        //noinspection ResultOfMethodCallIgnored
        progressRequestBody.getProgressSubject()
                .doFinally(this::onUploadComplete)
                .subscribe(this::updateProgress, ErrorHandler.EMPTY);

        return mediaSingle.doOnSuccess(media -> App.getInstance().pushAlert(Alert.creation(media)));
    }

    public void notifyDownloadComplete() {
        notifyOfDownload(mediaTransferBuilder()
                .setContentTitle(app.getString(R.string.download_complete)));
    }

    private void updateProgress(int percentage) {
        MediaTransferIntentService.UploadStats stats = MediaTransferIntentService.getUploadStats();

        notifyOfUpload(mediaTransferBuilder()
                .setContentText(app.getString(R.string.upload_progress_status, stats.getNumAttempted(), stats.getNumToUpload(), stats.getNumErrors()))
                .setContentTitle(app.getString(R.string.uploading_media))
                .setProgress(100, percentage, false));
    }

    @SuppressLint("CheckResult")
    private void onUploadComplete() {
        MediaTransferIntentService.UploadStats stats = MediaTransferIntentService.getUploadStats();
        if (!stats.isComplete()) return;

        //noinspection ResultOfMethodCallIgnored
        Completable.timer(1200, TimeUnit.MILLISECONDS).observeOn(mainThread()).subscribe(
                () -> notifyOfUpload(mediaTransferBuilder()
                        .setContentText(getUploadCompletionContentText(stats))
                        .setContentTitle(getUploadCompletionContentTitle(stats))
                        .setProgress(0, 0, false)),
                ErrorHandler.EMPTY);
    }

    private NotificationCompat.Builder mediaTransferBuilder() {
        return new NotificationCompat.Builder(app, FeedItem.MEDIA)
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSmallIcon(R.drawable.ic_notification)
                .setChannelId(MEDIA_UPLOADS);
    }

    private void notifyOfUpload(NotificationCompat.Builder builder) {
        notifyOfMediaTransfer(builder, UPLOAD_NOTIFICATION_ID);
    }

    private void notifyOfDownload(NotificationCompat.Builder builder) {
        notifyOfMediaTransfer(builder, DOWNLOAD_NOTIFICATION_ID);
    }

    private void notifyOfMediaTransfer(NotificationCompat.Builder builder, int notificationId) {
        NotificationManager notifier = (NotificationManager) app.getSystemService(NOTIFICATION_SERVICE);
        if (notifier != null) notifier.notify(notificationId, builder.build());
    }

    @NonNull
    private String getUploadCompletionContentTitle(MediaTransferIntentService.UploadStats stats) {
        return stats.isAtMaxStorage() ? app.getString(R.string.upload_failed) : app.getString(R.string.upload_complete);
    }

    @NonNull
    private String getUploadCompletionContentText(MediaTransferIntentService.UploadStats stats) {
        return stats.isAtMaxStorage() ? stats.getMaxStorageMessage() : app.getString(R.string.upload_complete_status, stats.getNumErrors());
    }
}
