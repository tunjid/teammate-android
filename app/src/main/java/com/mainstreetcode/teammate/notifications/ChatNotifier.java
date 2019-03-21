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

import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Chat;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.repository.ChatRepository;
import com.mainstreetcode.teammate.repository.ModelRepository;
import com.mainstreetcode.teammate.repository.UserRepository;
import com.mainstreetcode.teammate.util.ErrorHandler;

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

    private static final int MAX_LINES = 5;
    private static ChatNotifier INSTANCE;

    private final Map<Team, Boolean> visibleChatMap = new HashMap<>();

    private ChatNotifier() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_REPLY);
        filter.addAction(ACTION_MARK_AS_READ);

        App.getInstance().registerReceiver(new NotificationActionReceiver(), filter);
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
    @SuppressLint("CheckResult")
    protected void handleNotification(FeedItem<Chat> item) {
        ChatRepository repository = ChatRepository.getInstance();
        AtomicInteger count = new AtomicInteger(0);
        //noinspection ResultOfMethodCallIgnored
        repository.get(item.getModel())
                .flatMap(chat -> repository.fetchUnreadChats())
                .doOnNext(chats -> count.incrementAndGet())
                .map(unreadChats -> new Pair<>(buildNotification(item, unreadChats, count.get()), unreadChats.get(0)))
                .observeOn(mainThread())
                .subscribe(notificationChatPair -> sendNotification(notificationChatPair.first, notificationChatPair.second),
                        ErrorHandler.EMPTY,
                        () -> buildSummary(item, count.get()));
    }

    public void setChatVisibility(Team team, boolean visible) {
        visibleChatMap.put(team, visible);
    }

    private Notification buildNotification(FeedItem<Chat> item, List<Chat> chats, int count) {
        int size = chats.size();
        NotificationCompat.Builder notificationBuilder = getNotificationBuilder(item)
                .setContentIntent(getDeepLinkIntent(item.getModel()))
                .setSmallIcon(R.drawable.ic_notification)
                .setGroup(NOTIFICATION_GROUP)
                .setAutoCancel(true);

        Chat latest = chats.get(0);
        CharSequence teamName = latest.getTeam().getName();

        notificationBuilder.setSound(getNotificationSound(count));
        setGroupAlertSummary(notificationBuilder);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.addAction(getReplyAction(item));
            notificationBuilder.addAction(getMarkAsReadAction(item));
        }

        if (size < 2) return notificationBuilder
                .setContentTitle(app.getString(R.string.chats_notification_title, teamName, latest.getUser().getFirstName()))
                .setContentText(latest.getContent())
                .build();

        int min = Math.min(size - 1, MAX_LINES);
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

        for (int i = min; i >= 0; i--) {
            Chat chat = chats.get(i);
            style.addLine(getChatLine(chat));
        }

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
                .build(), Chat.chat("", User.empty(), Team.empty()));
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

    private NotificationCompat.Action getReplyAction(FeedItem<Chat> item) {
        String replyLabel = App.getInstance().getResources().getString(R.string.chat_reply_label);
        PendingIntent pending = getNotificationActionIntent(item, ACTION_REPLY);

        return new NotificationCompat.Action.Builder(R.drawable.ic_notification, replyLabel, pending)
                .addRemoteInput(new RemoteInput.Builder(KEY_TEXT_REPLY).setLabel(replyLabel).build())
                .build();
    }

    private NotificationCompat.Action getMarkAsReadAction(FeedItem<Chat> item) {
        String markLabel = App.getInstance().getResources().getString(R.string.chat_mark_as_read_label);
        PendingIntent pending = getNotificationActionIntent(item, ACTION_MARK_AS_READ);

        return new NotificationCompat.Action.Builder(R.drawable.ic_notification, markLabel, pending)
                .build();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private PendingIntent getNotificationActionIntent(FeedItem<Chat> item, String action) {
        Intent pending = new Intent(App.getInstance(), NotificationActionReceiver.class)
                .putExtra(EXTRA_NOTIFICATION_ID, getNotifyId())
                .putExtra(EXTRA_FEED_ITEM, item)
                .setAction(action);

        return PendingIntent.getBroadcast(App.getInstance(), item.getModel().getTeam().hashCode(), pending, FLAG_UPDATE_CURRENT);
    }

    public static final class NotificationActionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action == null ? "" : action) {
                case ACTION_REPLY:
                    Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
                    if (remoteInput == null) break;

                    FeedItem<Chat> received = intent.getParcelableExtra(EXTRA_FEED_ITEM);
                    CharSequence message = remoteInput.getCharSequence(KEY_TEXT_REPLY);
                    Chat chat = Chat.chat(message, UserRepository.getInstance().getCurrentUser(), received.getModel().getTeam());

                    //noinspection ResultOfMethodCallIgnored
                    ChatRepository.getInstance().createOrUpdate(chat).subscribe(__ -> ChatNotifier.getInstance().handleNotification(received), ErrorHandler.EMPTY);
                    break;
                case ACTION_MARK_AS_READ:
                    Chat read = intent.<FeedItem<Chat>>getParcelableExtra(EXTRA_FEED_ITEM).getModel();
                    ChatNotifier.getInstance().clearNotifications(read);
                    ChatRepository.getInstance().updateLastSeen(read.getTeam());
                    break;
            }
        }
    }
}
