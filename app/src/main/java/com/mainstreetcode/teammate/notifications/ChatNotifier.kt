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
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.Chat
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.repository.ChatRepo
import com.mainstreetcode.teammate.repository.ModelRepo
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.repository.UserRepo
import com.mainstreetcode.teammate.util.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min


class ChatNotifier internal constructor() : Notifier<Chat>() {

    private val visibleChatMap = HashMap<Team, Boolean>()

    override val notifyId: String
        get() = FeedItem.CHAT

    override val repository: ModelRepo<Chat>
        get() = RepoProvider.forModel(Chat::class.java)

    override val notificationChannels: Array<NotificationChannel>?
        @TargetApi(O)
        get() {
            val channel = buildNotificationChannel(FeedItem.CHAT, R.string.chats, R.string.chats_notifier_description, NotificationManager.IMPORTANCE_HIGH)
            channel.lightColor = Color.GREEN
            channel.enableVibration(true)
            return arrayOf(channel)
        }

    init {
        val filter = IntentFilter()
        filter.addAction(ACTION_MARK_AS_READ)
        filter.addAction(ACTION_REPLY)

        app.registerReceiver(NotificationActionReceiver(), filter)
    }

    override fun filterNotifications(item: Chat): Boolean {
        val visible = visibleChatMap[item.team]
        return visible == null || !visible
    }

    override fun getNotificationTag(model: Chat): String = model.team.id

    override fun handleNotification(item: FeedItem<Chat>) = aggregateConversations(item, item.model)

    fun setChatVisibility(team: Team, visible: Boolean) {
        visibleChatMap[team] = visible
    }

    @SuppressLint("CheckResult")
    private fun aggregateConversations(item: FeedItem<Chat>?, received: Chat) {
        val repository = RepoProvider.forRepo(ChatRepo::class.java)
        val count = AtomicInteger(0)

        repository[received]
                .flatMap { repository.fetchUnreadChats() }
                .doOnNext { count.incrementAndGet() }
                .map { unreadChats -> buildNotification(item, unreadChats, count.get()) to unreadChats[0] }
                .observeOn(mainThread())
                .subscribe(
                        { notificationChatPair -> sendNotification(notificationChatPair.first, notificationChatPair.second) },
                        ErrorHandler.EMPTY::accept,
                        { buildSummary(item, count.get()) })
    }

    private fun buildNotification(item: FeedItem<Chat>?, chats: List<Chat>, count: Int): Notification {
        val size = chats.size
        val latest = chats[0]
        val teamName = latest.team.name

        val notificationBuilder = getNotificationBuilder(item!!)
                .setContentIntent(getDeepLinkIntent(latest))
                .setSmallIcon(R.drawable.ic_notification)
                .addAction(getReplyAction(item, latest))
                .addAction(getMarkAsReadAction(latest))
                .setGroup(NOTIFICATION_GROUP)
                .setAutoCancel(true)

        notificationBuilder.setSound(getNotificationSound(count))
        setGroupAlertSummary(notificationBuilder)

        if (size < 2)
            return notificationBuilder
                    .setContentTitle(app.getString(R.string.chats_notification_title, teamName, latest.user.firstName))
                    .setContentText(latest.content)
                    .build()

        val min = min(size - 1, MAX_LINES)
        val style = NotificationCompat.InboxStyle()

        for (i in min downTo 0) style.addLine(getChatLine(chats[i]))

        if (size > MAX_LINES) style.setSummaryText(app.getString(R.string.chat_notification_multiline_summary, size - MAX_LINES))

        return notificationBuilder
                .setContentTitle(app.getString(R.string.chat_notification_multiline_title, size, teamName))
                .setContentText(getChatLine(latest))
                .setStyle(style)
                .build()
    }

    private fun buildSummary(item: FeedItem<Chat>?, count: Int) {
        val notificationBuilder = getNotificationBuilder(item!!)

        setGroupAlertSummary(notificationBuilder)

        sendNotification(notificationBuilder
                .setContentTitle(app.getString(R.string.chat_notification_group_summary, count))
                .setContentText(app.getString(R.string.chat_notification_group_summary, count))
                .setSmallIcon(R.drawable.ic_notification)
                .setGroup(NOTIFICATION_GROUP)
                .setGroupSummary(true)
                .setAutoCancel(true)
                .build(), Chat.empty()) // Empty chat as the summary is it's own notification
    }

    private fun setGroupAlertSummary(builder: NotificationCompat.Builder) {
        if (SDK_INT >= O) builder.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
    }

    private fun getNotificationSound(count: Int): Uri = when (count) {
        1 -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        else -> Uri.parse("android.resource://" + app.packageName + "/" + R.raw.silent)
    }

    private fun getChatLine(chat: Chat): CharSequence {
        val firstName = chat.user.firstName
        return app.getString(R.string.chat_notification_multiline_item, firstName, chat.content)
    }

    private fun getReplyAction(item: FeedItem<Chat>, latest: Chat): NotificationCompat.Action {
        val replyLabel = app.resources.getString(R.string.chat_reply_label)
        val pending = getNotificationActionIntent(latest, ACTION_REPLY) { intent -> intent.putExtra(EXTRA_FEED_ITEM, item) }

        return NotificationCompat.Action.Builder(R.drawable.ic_notification, replyLabel, pending)
                .addRemoteInput(RemoteInput.Builder(KEY_TEXT_REPLY).setLabel(replyLabel).build())
                .build()
    }

    private fun getMarkAsReadAction(latest: Chat): NotificationCompat.Action {
        val markLabel = app.resources.getString(R.string.chat_mark_as_read_label)
        val pending = getNotificationActionIntent(latest, ACTION_MARK_AS_READ) { intent -> intent.putExtra(EXTRA_CHAT, latest) }

        return NotificationCompat.Action.Builder(R.drawable.ic_notification, markLabel, pending)
                .build()
    }

    private fun getNotificationActionIntent(latest: Chat, action: String, intentConsumer: (Intent) -> Unit): PendingIntent {
        val pending = Intent(app, NotificationActionReceiver::class.java).setAction(action)
        addNotificationId(pending)

        intentConsumer.invoke(pending)
        return PendingIntent.getBroadcast(app, latest.team.hashCode(), pending, FLAG_UPDATE_CURRENT)
    }

    class NotificationActionReceiver : BroadcastReceiver() {

        @SuppressLint("CheckResult")
        override fun onReceive(context: Context, intent: Intent) {
            val repository = RepoProvider.forRepo(ChatRepo::class.java)
            val notifier = ChatNotifier()
            val action = intent.action
            when (action ?: "") {
                ACTION_REPLY -> {
                    val remoteInput = RemoteInput.getResultsFromIntent(intent) ?: return
                    val message = remoteInput.getCharSequence(KEY_TEXT_REPLY) ?: return
                    val received = intent.getParcelableExtra<FeedItem<Chat>>(EXTRA_FEED_ITEM)
                            ?: return
                    val toSend = Chat.chat(message, RepoProvider.forRepo(UserRepo::class.java).currentUser, received.model.team)

                    repository.createOrUpdate(toSend)
                            .subscribe({ notifier.aggregateConversations(received, toSend) }, ErrorHandler.EMPTY::accept)
                }
                ACTION_MARK_AS_READ -> {
                    val read = intent.getParcelableExtra<Chat>(EXTRA_CHAT) ?: return
                    repository.updateLastSeen(read.team)

                    repository.fetchUnreadChats().count().subscribe { count ->
                        notifier.clearNotifications(read)
                        if (count < 1) notifier.clearNotifications(Chat.empty())
                    }
                }
            }
        }
    }

    companion object {

        private const val NOTIFICATION_GROUP = "com.mainstreetcode.teammates.notifications.ChatNotifier"
        private const val KEY_TEXT_REPLY = "KEY_TEXT_REPLY"
        private const val ACTION_MARK_AS_READ = "MARK_AS_READ"
        private const val ACTION_REPLY = "REPLY"
        private const val EXTRA_FEED_ITEM = "FEED_ITEM"
        private const val EXTRA_CHAT = "CHAT"

        private const val MAX_LINES = 5
    }
}
