package com.mainstreetcode.teammate;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.mainstreetcode.teammate.model.Media;
import com.mainstreetcode.teammate.model.Message;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.repository.MediaRepository;
import com.mainstreetcode.teammate.repository.ModelRepository;
import com.mainstreetcode.teammate.util.ErrorHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MediaUploadIntentService extends IntentService {

    private static final String ACTION_UPLOAD = "com.mainstreetcode.teammates.action.UPLOAD";

    private static final String EXTRA_USER = "com.mainstreetcode.teammates.extra.user";
    private static final String EXTRA_TEAM = "com.mainstreetcode.teammates.extra.team";
    private static final String EXTRA_URIS = "com.mainstreetcode.teammates.extra.uris";

    private static UploadStats stats;

    public MediaUploadIntentService() {
        super("MediaUploadIntentService");
    }

    public static void startActionUpload(Context context, User user, Team team, List<Uri> mediaUris) {
        Intent intent = new Intent(context, MediaUploadIntentService.class);
        intent.setAction(ACTION_UPLOAD);
        intent.putExtra(EXTRA_USER, user);
        intent.putExtra(EXTRA_TEAM, team);
        intent.putParcelableArrayListExtra(EXTRA_URIS, new ArrayList<>(mediaUris));
        context.startService(intent);
    }

    public static UploadStats getStats() {
        return stats;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) return;
        final String action = intent.getAction();
        switch (action == null ? "" : action) {
            case ACTION_UPLOAD: {
                final User user = intent.getParcelableExtra(EXTRA_USER);
                final Team team = intent.getParcelableExtra(EXTRA_TEAM);
                final List<Uri> uris = intent.getParcelableArrayListExtra(EXTRA_URIS);
                handleActionUpload(user, team, uris);
                break;
            }
        }
    }

    /**
     * Handle action Upload in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpload(User user, Team team, List<Uri> mediaUris) {
        if (stats == null) stats = new UploadStats();
        for (Uri uri : mediaUris) stats.enqueue(Media.fromUri(user, team, uri));
    }

    public static class UploadStats {

        private int numErrors = 0;
        private int numToUpload = 0;
        private int numAttempted = 0;
        private boolean isOnGoing;
        private String maxStorageMessage = "";

        private final Queue<Media> uploadQueue = new ConcurrentLinkedQueue<>();
        private final ModelRepository<Media> repository = MediaRepository.getInstance();

        void enqueue(Media media) {
            numToUpload++;
            uploadQueue.add(media);

            if (uploadQueue.isEmpty()) numErrors = 0;
            if (!isOnGoing) invoke();
        }

        private void invoke() {
            if (uploadQueue.isEmpty()) {
                numToUpload = 0;
                numAttempted = 0;
                isOnGoing = false;
                return;
            }

            numAttempted++;
            isOnGoing = true;

            repository.createOrUpdate(uploadQueue.remove())
                    .doOnSuccess(media -> maxStorageMessage = "")
                    .doOnError(this::onError)
                    .doFinally(this::invoke)
                    .subscribe(ignored -> {}, ErrorHandler.EMPTY);
        }

        public int getNumErrors() {return numErrors;}

        public int getNumToUpload() {return numToUpload;}

        public int getNumAttempted() {return numAttempted;}

        public boolean isComplete() {return uploadQueue.isEmpty();}

        public boolean isAtMaxStorage() {return !TextUtils.isEmpty(maxStorageMessage);}

        public String getMaxStorageMessage() {return maxStorageMessage;}

        @Override
        public String toString() {
            return "Queue size: " + uploadQueue.size() +
                    ", numErrors : " + numErrors +
                    ", numToUpload : " + numToUpload +
                    ", numAttempted : " + numAttempted;
        }

        private void onError(Throwable throwable) {
            numErrors++;
            Message message = Message.fromThrowable(throwable);
            if (message == null || !message.isAtMaxStorage()) return;

            maxStorageMessage = message.getMessage();
        }
    }
}
