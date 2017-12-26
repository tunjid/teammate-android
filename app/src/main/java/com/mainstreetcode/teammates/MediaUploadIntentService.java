package com.mainstreetcode.teammates;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.repository.MediaRepository;
import com.mainstreetcode.teammates.repository.ModelRepository;
import com.mainstreetcode.teammates.util.ErrorHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MediaUploadIntentService extends IntentService {

    private static final String ACTION_UPLOAD = "com.mainstreetcode.teammates.action.UPLOAD";

    private static final String EXTRA_TEAM = "com.mainstreetcode.teammates.extra.PARAM1";
    private static final String EXTRA_URIS = "com.mainstreetcode.teammates.extra.PARAM2";

    private static UploadStats stats;

    public MediaUploadIntentService() {
        super("MediaUploadIntentService");
    }

    public static void startActionUpload(Context context, Team team, List<Uri> mediaUris) {
        Intent intent = new Intent(context, MediaUploadIntentService.class);
        intent.setAction(ACTION_UPLOAD);
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
                final Team team = intent.getParcelableExtra(EXTRA_TEAM);
                final List<Uri> uris = intent.getParcelableArrayListExtra(EXTRA_URIS);
                handleActionUpload(team, uris);
                break;
            }
        }
    }

    /**
     * Handle action Upload in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpload(Team team, List<Uri> mediaUris) {
        if (stats == null) stats = new UploadStats();
        for (Uri uri : mediaUris) stats.enqueue(team, uri);
    }

    public static class UploadStats {

        private int numErrors = 0;
        private int numToUpload = 0;
        private int numAttempted = 0;
        private boolean isOnGoing;

        private final Queue<Media> uploadQueue = new LinkedList<>();
        private final ModelRepository<Media> repository = MediaRepository.getInstance();

        void enqueue(Team team, Uri uri) {
            numToUpload++;
            uploadQueue.add(fromUri(team, uri));

            if (!isOnGoing) invoke();
        }

        private void invoke() {
            if (uploadQueue.isEmpty()) {
                numErrors = 0;
                numToUpload = 0;
                numAttempted = 0;
                isOnGoing = false;
                return;
            }

            numAttempted++;
            isOnGoing = true;

            repository.createOrUpdate(uploadQueue.remove())
                    .doOnError(throwable -> numErrors++)
                    .doFinally(this::invoke)
                    .subscribe(ignored -> {}, ErrorHandler.EMPTY);
        }

        private Media fromUri(Team team, Uri uri) {
            return new Media("", new File(uri.getPath()).toString(), "", "", User.empty(), team, new Date());
        }

        public int getNumErrors() {
            return numErrors;
        }

        public int getNumToUpload() {
            return numToUpload;
        }

        public int getNumAttempted() {
            return numAttempted;
        }

        public boolean isComplete() {
            return uploadQueue.isEmpty();
        }
    }
}
