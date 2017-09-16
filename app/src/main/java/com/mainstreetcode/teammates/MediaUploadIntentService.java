package com.mainstreetcode.teammates;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.repository.MediaRepository;
import com.mainstreetcode.teammates.repository.ModelRespository;
import com.mainstreetcode.teammates.util.ErrorHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MediaUploadIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_UPLOAD = "com.mainstreetcode.teammates.action.UPLOAD";
    private static final String ACTION_BAZ = "com.mainstreetcode.teammates.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.mainstreetcode.teammates.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.mainstreetcode.teammates.extra.PARAM2";

    public MediaUploadIntentService() {
        super("MediaUploadIntentService");
    }

    /**
     * Starts this service to perform action Upload with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionUpload(Context context, Team team, List<Uri> mediaUris) {
        Intent intent = new Intent(context, MediaUploadIntentService.class);
        intent.setAction(ACTION_UPLOAD);
        intent.putExtra(EXTRA_PARAM1, team);
        intent.putParcelableArrayListExtra(EXTRA_PARAM2, new ArrayList<>(mediaUris));
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, MediaUploadIntentService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPLOAD.equals(action)) {
                final Team team = intent.getParcelableExtra(EXTRA_PARAM1);
                final List<Uri> param2 = intent.getParcelableArrayListExtra(EXTRA_PARAM2);
                handleActionUpload(team, param2);
            }
            else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Upload in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpload(Team team, List<Uri> mediaUris) {
        ModelRespository<Media> respository = MediaRepository.getInstance();

        for (Uri uri : mediaUris) {
            Media media = new Media("", new File(uri.getPath()).toString(),
                    "", "", User.empty(), team, new Date());

            respository.createOrUpdate(media).subscribe(media1 -> {}, ErrorHandler.EMPTY);
        }
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
