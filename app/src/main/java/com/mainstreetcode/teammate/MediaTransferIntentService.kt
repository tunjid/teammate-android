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

package com.mainstreetcode.teammate

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import android.webkit.MimeTypeMap
import com.mainstreetcode.teammate.model.Media
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.model.toMessage
import com.mainstreetcode.teammate.notifications.MediaNotifier
import com.mainstreetcode.teammate.notifications.NotifierProvider
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.prettyPrint
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class MediaTransferIntentService : IntentService("MediaUploadIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) when (intent.action ?: "") {
            ACTION_UPLOAD -> handleActionUpload(
                    intent.getParcelableExtra<User>(EXTRA_USER),
                    intent.getParcelableExtra<Team>(EXTRA_TEAM),
                    intent.getParcelableArrayListExtra(EXTRA_URIS)
            )
            ACTION_DOWNLOAD -> handleActionDownload(intent.getParcelableArrayListExtra(EXTRA_MEDIA))
        }
    }

    /**
     * Handle action Upload in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionUpload(user: User?, team: Team?, mediaUris: List<Uri>?) {
        if (user == null || team == null || mediaUris == null) return

        if (uploadStatsUnInitialized) uploadStats = UploadStats()
        for (uri in mediaUris) uploadStats.enqueue(Media.fromUri(user, team, uri))
    }

    private fun handleActionDownload(mediaList: List<Media>?) {
        if (mediaList == null) return

        if (downloadStatsUnInitialized) downloadStats = DownloadStats()
        for (media in mediaList) downloadStats.enqueue(media)
    }

    class UploadStats {

        var numErrors = 0
            private set

        var numToUpload = 0
            private set

        var numAttempted = 0
            private set

        private var isOnGoing: Boolean = false

        var maxStorageMessage = ""
            private set

        private val uploadQueue = ConcurrentLinkedQueue<Media>()
        private val repository = RepoProvider.forModel(Media::class.java)

        val isComplete: Boolean
            get() = uploadQueue.isEmpty()

        val isAtMaxStorage: Boolean
            get() = !TextUtils.isEmpty(maxStorageMessage)

        internal fun enqueue(media: Media) {
            numToUpload++
            uploadQueue.add(media)

            if (uploadQueue.isEmpty()) numErrors = 0
            if (!isOnGoing) invoke()
        }

        @SuppressLint("CheckResult")
        private operator fun invoke() {
            if (uploadQueue.isEmpty()) {
                numToUpload = 0
                numAttempted = 0
                isOnGoing = false
                return
            }

            numAttempted++
            isOnGoing = true


            repository.createOrUpdate(uploadQueue.remove())
                    .doOnSuccess { maxStorageMessage = "" }
                    .doOnError(this::onError)
                    .doFinally(this::invoke)
                    .subscribe({ }, ErrorHandler.EMPTY::invoke)
        }

        override fun toString(): String {
            return "Queue size: " + uploadQueue.size +
                    ", numErrors : " + numErrors +
                    ", numToUpload : " + numToUpload +
                    ", numAttempted : " + numAttempted
        }

        private fun onError(throwable: Throwable) {
            numErrors++
            val message = throwable.toMessage()
            if (message == null || !message.isAtMaxStorage) return

            maxStorageMessage = message.message
        }
    }

    class DownloadStats {

        private val downloadQueue = HashSet<Long>()

        private val isExternalStorageWritable: Boolean
            get() {
                val state = Environment.getExternalStorageState()
                return Environment.MEDIA_MOUNTED == state
            }

        internal fun enqueue(media: Media) {
            if (!isExternalStorageWritable) return

            val app = App.instance
            val downloadManager = app.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                    ?: return

            val url = media.url

            val destination = getDownloadDestination(media) ?: return

            val request = DownloadManager.Request(Uri.parse(url))
            request.setTitle(getMediaTitle(media, app))
            request.allowScanningByMediaScanner()

            downloadQueue.add(downloadManager.enqueue(request
                    .setDestinationUri(Uri.fromFile(destination))
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)))
        }

        internal fun onDownloadComplete(id: Long) {
            if (downloadQueue.remove(id) && downloadQueue.isEmpty())
                NotifierProvider.forNotifier(MediaNotifier::class.java).notifyDownloadComplete()
        }

        private fun getMediaTitle(media: Media, app: App): String =
                app.getString(R.string.media_download_title, media.team.name, media.created.prettyPrint())

        private fun getExtension(url: String): String? = MimeTypeMap.getFileExtensionFromUrl(url)

        private fun getDownloadDestination(media: Media): File? {
            val url = media.url
            var fileName = media.id
            val extension = getExtension(url)
            if (extension != null) fileName += ".$extension"

            val directory = File(Environment.getExternalStorageDirectory().toString() + File.separator + APP_ROOT_DIR)

            val canWrite = directory.exists() || directory.mkdir()
            if (!canWrite) return null

            val output = File(directory, fileName)
            return if (output.exists()) null else output

        }
    }

    companion object {

        private const val ACTION_UPLOAD = "com.mainstreetcode.teammates.action.UPLOAD"
        private const val ACTION_DOWNLOAD = "com.mainstreetcode.teammates.action.DOWNLOAD"

        private const val EXTRA_USER = "com.mainstreetcode.teammates.extra.user"
        private const val EXTRA_TEAM = "com.mainstreetcode.teammates.extra.team"
        private const val EXTRA_URIS = "com.mainstreetcode.teammates.extra.uris"
        private const val EXTRA_MEDIA = "com.mainstreetcode.teammates.extra.media"
        private const val APP_ROOT_DIR = "teammate"

        lateinit var uploadStats: UploadStats
            private set

        lateinit var downloadStats: DownloadStats
            private set

        val uploadStatsUnInitialized: Boolean
            get() = !::uploadStats.isInitialized

        val downloadStatsUnInitialized: Boolean
            get() = !::downloadStats.isInitialized

        fun startActionUpload(context: Context, user: User, team: Team, mediaUris: List<Uri>) {
            val intent = Intent(context, MediaTransferIntentService::class.java)
            intent.action = ACTION_UPLOAD
            intent.putExtra(EXTRA_USER, user)
            intent.putExtra(EXTRA_TEAM, team)
            intent.putParcelableArrayListExtra(EXTRA_URIS, ArrayList(mediaUris))
            context.startService(intent)
        }

        @SuppressLint("CheckResult")
        fun startActionDownload(context: Context, mediaList: List<Media>) {
            val intent = Intent(context, MediaTransferIntentService::class.java)
            intent.action = ACTION_DOWNLOAD

            intent.putParcelableArrayListExtra(EXTRA_MEDIA, ArrayList(mediaList.filter { media -> media.url.isNotBlank() }))
            context.startService(intent)
        }
    }
}
