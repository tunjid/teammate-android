package com.mainstreetcode.teammate.notifications;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Chat;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.repository.ChatRepository;
import com.mainstreetcode.teammate.repository.ModelRepository;
import com.mainstreetcode.teammate.repository.UserRepository;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.tunjid.androidbootstrap.functions.Consumer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;
import androidx.core.util.Pair;
import io.reactivex.functions.Predicate;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;


public class ChatNotifier extends Notifier<Chat> {

    private static final String NOTIFICATION_GROUP = "com.mainstreetcode.teammates.notifications.ChatNotifier";
    private static final String KEY_TEXT_REPLY = "KEY_TEXT_REPLY";
    private static final String ACTION_MARK_AS_READ = "MARK_AS_READ";
    private static final String ACTION_REPLY = "REPLY";
    private static final String EXTRA_FEED_ITEM = "FEED_ITEM";
    private static final String EXTRA_CHAT = "CHAT";

    private static final int MAX_LINES = 5;
    private static ChatNotifier INSTANCE;

    private final Map<Team, Boolean> visibleChatMap = new HashMap<>();

    private ChatNotifier() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_MARK_AS_READ);
        filter.addAction(ACTION_REPLY);

        app.registerReceiver(new NotificationActionReceiver(), filter);
    }

    public static ChatNotifier getInstance() {
        if (INSTANCE == null) INSTANCE = new ChatNotifier();
        return INSTANCE;
    }

    @Override
    String getNotifyId() {return FeedItem.CHAT;}

    @Override
    String getNotificationTag(Chat model) {return model.getTeam().getId();}

    @Override
    protected ModelRepository<Chat> getRepository() {return ChatRepository.getInstance();}

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected NotificationChannel[] getNotificationChannels() {
        NotificationChannel channel = buildNotificationChannel(FeedItem.CHAT, R.string.chats, R.string.chats_notifier_description, NotificationManager.IMPORTANCE_HIGH);
        channel.setLightColor(Color.GREEN);
        channel.enableVibration(true);
        return new NotificationChannel[]{channel};
    }

    @Override
    public Predicate<Chat> getNotificationFilter() {
        return teamChat -> {
            Boolean visible = visibleChatMap.get(teamChat.getTeam());
            return visible == null || !visible;
        };
    }

    @Override
    protected void handleNotification(FeedItem<Chat> item) {
        aggregateConversations(item, item.getModel());
    }

    public void setChatVisibility(Team team, boolean visible) {
        visibleChatMap.put(team, visible);
    }

    @SuppressLint("CheckResult")
    private void aggregateConversations(FeedItem<Chat> item, Chat received) {
        ChatRepository repository = ChatRepository.getInstance();
        AtomicInteger count = new AtomicInteger(0);
        //noinspection ResultOfMethodCallIgnored
        repository.get(received)
                .flatMap(chat -> repository.fetchUnreadChats())
                .doOnNext(chats -> count.incrementAndGet())
                .map(unreadChats -> new Pair<>(buildNotification(item, unreadChats, count.get()), unreadChats.get(0)))
                .observeOn(mainThread())
                .subscribe(
                        notificationChatPair -> sendNotification(notificationChatPair.first, notificationChatPair.second),
                        ErrorHandler.EMPTY,
                        () -> buildSummary(item, count.get()));
    }

    private Notification buildNotification(FeedItem<Chat> item, List<Chat> chats, int count) {
        int size = chats.size();
        Chat latest = chats.get(0);
        CharSequence teamName = latest.getTeam().getName();

        NotificationCompat.Builder notificationBuilder = getNotificationBuilder(item)
                .setContentIntent(getDeepLinkIntent(latest))
                .setSmallIcon(R.drawable.ic_notification)
                .setGroup(NOTIFICATION_GROUP)
                .setAutoCancel(true);

        notificationBuilder.setSound(getNotificationSound(count));
        setGroupAlertSummary(notificationBuilder);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.addAction(getReplyAction(item, latest));
            notificationBuilder.addAction(getMarkAsReadAction(latest));
        }

        if (size < 2) return notificationBuilder
                .setContentTitle(app.getString(R.string.chats_notification_title, teamName, latest.getUser().getFirstName()))
                .setContentText(latest.getContent())
                .build();

        int min = Math.min(size - 1, MAX_LINES);
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

        for (int i = min; i >= 0; i--) style.addLine(getChatLine(chats.get(i)));

        if (size > MAX_LINES) {
            style.setSummaryText(app.getString(R.string.chat_notification_multiline_summary, (size - MAX_LINES)));
        }

        return notificationBuilder
                .setContentTitle(app.getString(R.string.chat_notification_multiline_title, size, teamName))
                .setContentText(getChatLine(latest))
                .setStyle(style)
                .build();
    }

    private void buildSummary(FeedItem<Chat> item, int count) {
        NotificationCompat.Builder notificationBuilder = getNotificationBuilder(item);

        setGroupAlertSummary(notificationBuilder);

        sendNotification(notificationBuilder
                .setContentTitle(app.getString(R.string.chat_notification_group_summary, count))
                .setContentText(app.getString(R.string.chat_notification_group_summary, count))
                .setSmallIcon(R.drawable.ic_notification)
                .setGroup(NOTIFICATION_GROUP)
                .setGroupSummary(true)
                .setAutoCancel(true)
                .build(), Chat.empty()); // Empty chat as the summary is it's own notification
    }

    private void setGroupAlertSummary(NotificationCompat.Builder notificationBuilder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY);
        }
    }

    private Uri getNotificationSound(int count) {
        return count == 1
                ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                : Uri.parse("android.resource://" + app.getPackageName() + "/" + R.raw.silent);
    }

    @NonNull
    private CharSequence getChatLine(Chat chat) {
        CharSequence firstName = chat.getUser().getFirstName();
        return app.getString(R.string.chat_notification_multiline_item, firstName, chat.getContent());
    }

    private NotificationCompat.Action getReplyAction(FeedItem<Chat> item, Chat latest) {
        String replyLabel = app.getResources().getString(R.string.chat_reply_label);
        PendingIntent pending = getNotificationActionIntent(latest, ACTION_REPLY, intent -> intent.putExtra(EXTRA_FEED_ITEM, item));

        return new NotificationCompat.Action.Builder(R.drawable.ic_notification, replyLabel, pending)
                .addRemoteInput(new RemoteInput.Builder(KEY_TEXT_REPLY).setLabel(replyLabel).build())
                .build();
    }

    private NotificationCompat.Action getMarkAsReadAction(Chat latest) {
        String markLabel = app.getResources().getString(R.string.chat_mark_as_read_label);
        PendingIntent pending = getNotificationActionIntent(latest, ACTION_MARK_AS_READ, intent -> intent.putExtra(EXTRA_CHAT, latest));

        return new NotificationCompat.Action.Builder(R.drawable.ic_notification, markLabel, pending)
                .build();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private PendingIntent getNotificationActionIntent(Chat latest, String action, Consumer<Intent> intentConsumer) {
        Intent pending = new Intent(app, NotificationActionReceiver.class)
                .putExtra(EXTRA_NOTIFICATION_ID, getNotifyId())
                .setAction(action);

        intentConsumer.accept(pending);
        return PendingIntent.getBroadcast(app, latest.getTeam().hashCode(), pending, FLAG_UPDATE_CURRENT);
    }

    public static final class NotificationActionReceiver extends BroadcastReceiver {

        @Override
        @SuppressLint("CheckResult")
        public void onReceive(Context context, Intent intent) {
            ChatRepository repository = ChatRepository.getInstance();
            ChatNotifier notifier = ChatNotifier.getInstance();
            String action = intent.getAction();
            switch (action == null ? "" : action) {
                case ACTION_REPLY:
                    Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
                    if (remoteInput == null) break;

                    FeedItem<Chat> received = intent.getParcelableExtra(EXTRA_FEED_ITEM);
                    CharSequence message = remoteInput.getCharSequence(KEY_TEXT_REPLY);
                    Chat toSend = Chat.chat(message, UserRepository.getInstance().getCurrentUser(), received.getModel().getTeam());

                    //noinspection ResultOfMethodCallIgnored
                    repository.createOrUpdate(toSend).subscribe(__ -> notifier.aggregateConversations(received, toSend), ErrorHandler.EMPTY);
                    break;
                case ACTION_MARK_AS_READ:
                    Chat read = intent.getParcelableExtra(EXTRA_CHAT);

                    repository.updateLastSeen(read.getTeam());
                    //noinspection ResultOfMethodCallIgnored
                    repository.fetchUnreadChats().count().subscribe(count -> {
                        notifier.clearNotifications(read);
                        if (count < 1) notifier.clearNotifications(Chat.empty());
                    });
                    break;
            }
        }
    }
}
