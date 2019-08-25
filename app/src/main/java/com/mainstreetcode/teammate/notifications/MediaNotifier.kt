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

package com.mainstreetcode.teammate.notifications


import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.MediaTransferIntentService
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.Media
import com.mainstreetcode.teammate.repository.ModelRepo
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.rest.ProgressRequestBody
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.viewmodel.events.Alert

import java.util.concurrent.TimeUnit
import androidx.core.app.NotificationCompat
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody

import android.content.Context.NOTIFICATION_SERVICE
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread


class MediaNotifier internal constructor() : Notifier<Media>() {

    override val notifyId: String
        get() = FeedItem.MEDIA

    override val repository: ModelRepo<Media>
        get() = RepoProvider.forModel(Media::class.java)

    override val notificationChannels: Array<NotificationChannel>?
        @TargetApi(Build.VERSION_CODES.O)
        get() = arrayOf(buildNotificationChannel(FeedItem.MEDIA, R.string.media, R.string.media_notifier_description, NotificationManager.IMPORTANCE_MIN), buildNotificationChannel(MEDIA_UPLOADS, R.string.media_upload_channel_name, R.string.media_upload_channel_description, NotificationManager.IMPORTANCE_LOW))

    @SuppressLint("CheckResult")
    fun notifyOfUploads(mediaSingle: Single<Media>, requestBody: RequestBody): Single<Media> {
        if (requestBody !is ProgressRequestBody) return mediaSingle

        requestBody.progressFlowable
                .doFinally(this::onUploadComplete)
                .subscribe(this::updateProgress, ErrorHandler.EMPTY::invoke)

        return mediaSingle.doOnSuccess { media -> App.instance.pushAlert(Alert.creation(media)) }
    }

    fun notifyDownloadComplete() {
        notifyOfDownload(mediaTransferBuilder()
                .setContentTitle(app.getString(R.string.download_complete)))
    }

    private fun updateProgress(percentage: Int) {
        val stats = MediaTransferIntentService.uploadStats

        notifyOfUpload(mediaTransferBuilder()
                .setContentText(app.getString(R.string.upload_progress_status, stats.numAttempted, stats.numToUpload, stats.numErrors))
                .setContentTitle(app.getString(R.string.uploading_media))
                .setProgress(100, percentage, false))
    }

    @SuppressLint("CheckResult")
    private fun onUploadComplete() {
        val stats = MediaTransferIntentService.uploadStats
        if (!stats.isComplete) return


        Completable.timer(1200, TimeUnit.MILLISECONDS).observeOn(mainThread()).subscribe(
                {
                    notifyOfUpload(mediaTransferBuilder()
                            .setContentText(getUploadCompletionContentText(stats))
                            .setContentTitle(getUploadCompletionContentTitle(stats))
                            .setProgress(0, 0, false))
                },
                ErrorHandler.EMPTY::invoke)
    }

    private fun mediaTransferBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(app, FeedItem.MEDIA)
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSmallIcon(R.drawable.ic_notification)
                .setChannelId(MEDIA_UPLOADS)
    }

    private fun notifyOfUpload(builder: NotificationCompat.Builder) {
        notifyOfMediaTransfer(builder, UPLOAD_NOTIFICATION_ID)
    }

    private fun notifyOfDownload(builder: NotificationCompat.Builder) {
        notifyOfMediaTransfer(builder, DOWNLOAD_NOTIFICATION_ID)
    }

    private fun notifyOfMediaTransfer(builder: NotificationCompat.Builder, notificationId: Int) {
        val notifier = app.getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
        notifier?.notify(notificationId, builder.build())
    }

    private fun getUploadCompletionContentTitle(stats: MediaTransferIntentService.UploadStats): String =
            if (stats.isAtMaxStorage) app.getString(R.string.upload_failed) else app.getString(R.string.upload_complete)

    private fun getUploadCompletionContentText(stats: MediaTransferIntentService.UploadStats): String =
            if (stats.isAtMaxStorage) stats.maxStorageMessage else app.getString(R.string.upload_complete_status, stats.numErrors)

    companion object {

        private const val MEDIA_UPLOADS = "media_uploads"
        private const val UPLOAD_NOTIFICATION_ID = 1
        private const val DOWNLOAD_NOTIFICATION_ID = 2
    }
}
