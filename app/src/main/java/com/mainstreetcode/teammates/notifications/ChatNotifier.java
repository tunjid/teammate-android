package com.mainstreetcode.teammates.notifications;


import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Chat;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.repository.ChatRepository;
import com.mainstreetcode.teammates.repository.ModelRepository;
import com.mainstreetcode.teammates.repository.TeamRepository;
import com.mainstreetcode.teammates.repository.UserRepository;
import com.mainstreetcode.teammates.util.ErrorHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.functions.Predicate;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;


public class ChatNotifier extends Notifier<Chat> {

    private static final int MAX_LINES = 5;
    private static ChatNotifier INSTANCE;

    private final Map<Team, Boolean> visibleChatMap = new HashMap<>();
    private final UserRepository userRepository;
    private Team sender = Team.empty();

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
        return new NotificationChannel[]{buildNotificationChannel(FeedItem.CHAT, R.string.chats, R.string.chats_notifier_description, NotificationManager.IMPORTANCE_HIGH)};
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
    protected void handleNotification(FeedItem<Chat> item) {
        TeamRepository teamRepository = TeamRepository.getInstance();
        Chat chat = item.getModel();

        teamRepository.get(chat.getTeam()).firstOrError()
                .flatMap(team -> fetchUnreadChats(item, team))
                .map(unreadChats -> buildNotification(item, unreadChats))
                .subscribe(notification -> sendNotification(notification, chat), ErrorHandler.EMPTY);
    }

    public void setChatVisibility(Team team, boolean visible) {
        visibleChatMap.put(team, visible);
    }

    private Single<List<Chat>> fetchUnreadChats(FeedItem<Chat> item, Team team) {
        sender.update(team);
        ChatRepository repository = ChatRepository.getInstance();
        return repository.fetchUnreadChats(item.getModel().getTeam())
                .observeOn(mainThread());
    }

    private Notification buildNotification(FeedItem<Chat> item, List<Chat> chats) {
        int size = chats.size();
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = getNotificationBuilder(item)
                .setContentIntent(getDeepLinkIntent(item))
                .setSound(defaultSoundUri)
                .setAutoCancel(true);

        if (size < 2) return notificationBuilder
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(item.getTitle())
                .setContentText(item.getBody())
                .build();

        int min = Math.min(size, MAX_LINES);
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

        for (int i = 0; i < min; i++) {
            Chat chat = chats.get(i);
            style.addLine(app.getString(R.string.chat_notification_multiline_item,
                    chat.getUser().getFirstName(), chat.getContent()));
        }

        if (size > MAX_LINES) {
            style.setSummaryText(app.getString(R.string.chat_notification_multiline_summary, (size - MAX_LINES)));
        }

        return notificationBuilder
                .setContentTitle(app.getString(R.string.chat_notification_multiline_title, size, sender.getName()))
                .setSmallIcon(R.drawable.ic_notification)
                .setStyle(style)
                .build();
    }
}
