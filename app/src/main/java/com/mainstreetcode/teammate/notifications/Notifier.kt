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
import android.app.Notification
import android.app.Notification.EXTRA_NOTIFICATION_ID
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.app.PendingIntent.getActivity
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.activities.FEED_DEEP_LINK
import com.mainstreetcode.teammate.activities.MainActivity
import com.mainstreetcode.teammate.model.Model
import com.mainstreetcode.teammate.repository.ModelRepo
import com.mainstreetcode.teammate.util.ErrorHandler
import java.util.*

abstract class Notifier<T : Model<T>> {

    protected val app: App = App.instance
    private val channelMap: MutableMap<String, NotificationChannel>

    internal abstract val notifyId: String

    protected abstract val repository: ModelRepo<T>

    protected abstract val notificationChannels: Array<NotificationChannel>?

    init {
        channelMap = HashMap()
        val manager = app.getSystemService(NOTIFICATION_SERVICE) as? NotificationManager

        @Suppress("LeakingThis")
        if (SDK_INT >= O && manager != null) {
            val channels = notificationChannels
            if (channels != null && channels.isNotEmpty()) for (channel in channels) {
                manager.createNotificationChannel(channel)
                channelMap[channel.id] = channel
            }
        }
    }

   open fun filterNotifications(item: T) : Boolean= true

    fun clearNotifications(model: T?) {
        val notifier = app.getSystemService(NOTIFICATION_SERVICE) as? NotificationManager ?: return

        when (model) {
            null -> notifier.cancel(notifyId.hashCode())
            else -> notifier.cancel(getNotificationTag(model), notifyId.hashCode())
        }
    }

    @SuppressLint("CheckResult")
    fun notify(item: FeedItem<T>) {
        repository[item.model].lastElement()
                .filter(this::filterNotifications)
                .map { item }
                .subscribe(this::handleNotification, ErrorHandler.EMPTY::invoke)
    }

    internal fun getNotificationBuilder(item: FeedItem<T>): NotificationCompat.Builder {
        val type = item.type
        val builder = NotificationCompat.Builder(app, type)

        if (channelMap.containsKey(type)) builder.setChannelId(type)
        return builder
    }

    internal fun getDeepLinkIntent(model: T): PendingIntent {
        val intent = Intent(app, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(FEED_DEEP_LINK, model)
        addNotificationId(intent)

        return getActivity(app, DEEP_LINK_REQ_CODE, intent, FLAG_ONE_SHOT)
    }

    internal fun sendNotification(notification: Notification, model: T) {
        val notifier = app.getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
        notifier?.notify(getNotificationTag(model), notifyId.hashCode(), notification)
    }

    internal fun addNotificationId(intent: Intent) {
        if (SDK_INT >= O) intent.putExtra(EXTRA_NOTIFICATION_ID, notifyId)
    }

    protected open fun handleNotification(item: FeedItem<T>) {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        sendNotification(getNotificationBuilder(item)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(item.title)
                .setContentText(item.body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(getDeepLinkIntent(item.model))
                .build(), item.model)
    }

    @TargetApi(O)
    internal fun buildNotificationChannel(id: String, @StringRes name: Int, @StringRes description: Int, importance: Int): NotificationChannel {
        val channel = NotificationChannel(id, app.getString(name), importance)
        channel.description = app.getString(description)

        return channel
    }

    internal open fun getNotificationTag(model: T): String = model.id

    companion object {

        private const val DEEP_LINK_REQ_CODE = 1
    }

}
