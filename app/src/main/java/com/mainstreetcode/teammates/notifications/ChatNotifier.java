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
import com.mainstreetcode.teammates.repository.ModelRepository;
import com.mainstreetcode.teammates.repository.ChatRepository;
import com.mainstreetcode.teammates.repository.TeamRepository;
import com.mainstreetcode.teammates.repository.UserRepository;
import com.mainstreetcode.teammates.util.ErrorHandler;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.functions.Predicate;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;


public class ChatNotifier extends Notifier<Chat> {

    private static final int MAX_LINES = 5;
    private static ChatNotifier INSTANCE;

    private final UserRepository userRepository;
    private Team sender = Team.empty();

    private ChatNotifier() {userRepository = UserRepository.getInstance();}

    public static ChatNotifier getInstance() {
        if (INSTANCE == null) INSTANCE = new ChatNotifier();
        return INSTANCE;
    }

    @Override
    protected ModelRepository<Chat> getRepository() {
        return ChatRepository.getInstance();
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected NotificationChannel[] getNotificationChannels() {
        return new NotificationChannel[]{buildNotificationChannel(FeedItem.CHAT, R.string.chats, R.string.chats_notifier_description, NotificationManager.IMPORTANCE_HIGH)};
    }

    @Override
    public Predicate<Chat> getNotificationFilter() {
        return teamChat -> !teamChat.getUser().equals(userRepository.getCurrentUser());
    }

    @Override
    protected void handleNotification(FeedItem<Chat> item) {
        TeamRepository teamRepository = TeamRepository.getInstance();
        Chat chat = item.getModel();

        teamRepository.get(chat.getTeam()).firstOrError()
                .flatMap(team -> fetchUnreadChats(item, team))
                .map(unreadChats -> buildNotification(item, unreadChats))
                .subscribe(this::sendNotification, ErrorHandler.EMPTY);
    }

    private Single<List<Chat>> fetchUnreadChats(FeedItem<Chat> item, Team team) {
        ChatRepository repository = ChatRepository.getInstance();
        sender.update(team);
        return repository.fetchUnreadChats(item.getModel().getTeam())
                .subscribeOn(io())
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
