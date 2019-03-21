package com.mainstreetcode.teammate.notifications;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.util.Pair;

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

import io.reactivex.functions.Predicate;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;


public class ChatNotifier extends Notifier<Chat> {

    private static final String NOTIFICATION_GROUP = "com.mainstreetcode.teammates.notifications.ChatNotifier";

    private static final int MAX_LINES = 5;
    private static ChatNotifier INSTANCE;

    private final Map<Team, Boolean> visibleChatMap = new HashMap<>();
    private final UserRepository userRepository;

    private ChatNotifier() {userRepository = UserRepository.getInstance();}

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
            boolean showNotification = visible == null || !visible;
            return showNotification && !teamChat.getUser().equals(userRepository.getCurrentUser());
        };
    }

    @Override
    @SuppressLint("CheckResult")
    protected void handleNotification(FeedItem<Chat> item) {
        ChatRepository repository = ChatRepository.getInstance();
        AtomicInteger count = new AtomicInteger(0);
        repository.createOrUpdate(item.getModel())
                .toFlowable()
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
                .setContentIntent(getDeepLinkIntent(item))
                .setSmallIcon(R.drawable.ic_notification)
                .setGroup(NOTIFICATION_GROUP)
                .setAutoCancel(true);

        Chat first = chats.get(0);
        CharSequence teamName = first.getTeam().getName();

        notificationBuilder.setSound(getNotificationSound(count));
        setGroupAlertSummary(notificationBuilder);

        if (size < 2) return notificationBuilder
                .setContentTitle(app.getString(R.string.chats_notification_title, teamName, first.getUser().getFirstName()))
                .setContentText(first.getContent())
                .build();

        int min = Math.min(size, MAX_LINES);
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

        for (int i = 0; i < min; i++) {
            Chat chat = chats.get(i);
            style.addLine(getChatLine(chat));
        }

        if (size > MAX_LINES) {
            style.setSummaryText(app.getString(R.string.chat_notification_multiline_summary, (size - MAX_LINES)));
        }

        return notificationBuilder
                .setContentTitle(app.getString(R.string.chat_notification_multiline_title, size, teamName))
                .setContentText(getChatLine(first))
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
}
