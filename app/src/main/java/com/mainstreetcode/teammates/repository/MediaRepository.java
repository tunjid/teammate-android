package com.mainstreetcode.teammates.repository;

import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.notifications.MediaNotifier;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.EntityDao;
import com.mainstreetcode.teammates.persistence.MediaDao;
import com.mainstreetcode.teammates.rest.ProgressRequestBody;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;
import com.mainstreetcode.teammates.util.TeammateException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

import static io.reactivex.schedulers.Schedulers.io;

public class MediaRepository extends ModelRepository<Media> {

    private final int num;
    private final TeammateApi api;
    private final MediaDao mediaDao;
    private final ModelRepository<User> userRepository;
    private final ModelRepository<Team> teamRepository;

    private static MediaRepository ourInstance;

    private MediaRepository() {
        num = getNumCallsToIgnore();
        api = TeammateService.getApiInstance();
        mediaDao = AppDatabase.getInstance().mediaDao();
        userRepository = UserRepository.getInstance();
        teamRepository = TeamRepository.getInstance();
    }

    public static MediaRepository getInstance() {
        if (ourInstance == null) ourInstance = new MediaRepository();
        return ourInstance;
    }

    @Override
    public EntityDao<? super Media> dao() {
        return mediaDao;
    }

    @Override
    public Single<Media> createOrUpdate(Media model) {
        MultipartBody.Part body = getBody(model.getImageUrl(), Media.UPLOAD_KEY);

        if (body == null) return Single.error(new TeammateException("Unable to upload media"));

        Single<Media> mediaSingle = api.uploadTeamMedia(model.getTeam().getId(), body)
                .map(localMapper(model))
                .map(getSaveFunction());

        return MediaNotifier.getInstance().notifyOfUploads(mediaSingle, body.body());
    }

    @Override
    public Flowable<Media> get(String id) {
        Maybe<Media> local = mediaDao.get(id).subscribeOn(io());
        Maybe<Media> remote = api.getMedia(id).map(getSaveFunction()).toMaybe();

        return fetchThenGetModel(local, remote);
    }

    @Override
    public Single<Media> delete(Media model) {
        return api.deleteMedia(model.getId())
                .map(this::deleteLocally)
                .doOnError(throwable -> deleteInvalidModel(model, throwable));
    }

    @Override
    Function<List<Media>, List<Media>> provideSaveManyFunction() {
        return models -> {
            List<User> users = new ArrayList<>(models.size());
            List<Team> teams = new ArrayList<>(models.size());

            for (Media media : models) {
                users.add(media.getUser());
                teams.add(media.getTeam());
            }

            userRepository.getSaveManyFunction().apply(users);
            teamRepository.getSaveManyFunction().apply(teams);

            mediaDao.upsert(Collections.unmodifiableList(models));

            return models;
        };
    }

    public Flowable<List<Media>> getTeamMedia(Team team, Date date) {
        Maybe<List<Media>> local = mediaDao.getTeamMedia(team, date).subscribeOn(io());
        Maybe<List<Media>> remote = api.getTeamMedia(team.getId(), date).map(getSaveManyFunction()).toMaybe();

        return fetchThenGet(local, remote);
    }

    public Single<List<Media>> ownerDelete(List<Media> models) {
        return api.deleteMedia(models).doAfterSuccess(this::delete);
    }

    public Single<List<Media>> privilegedDelete(Team team, List<Media> models) {
        return api.adminDeleteMedia(team.getId(), models).doAfterSuccess(this::delete);
    }

    @Nullable
    MultipartBody.Part getBody(String path, String photoKey) {
        File file = new File(path);

        if (!file.exists()) return null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);

        if (extension == null) return null;
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        if (type == null) return null;
        RequestBody requestBody = new ProgressRequestBody(file, num, MediaType.parse(type));

        return MultipartBody.Part.createFormData(photoKey, file.getName(), requestBody);
    }

    private void delete(List<Media> list) {
        mediaDao.delete(Collections.unmodifiableList(list));
    }

    private int getNumCallsToIgnore() {
        int callsToIgnore = 0;
        OkHttpClient client = TeammateService.getHttpClient();

        for (Interceptor i : client.interceptors()) if (logsRequestBody(i)) callsToIgnore++;
        return callsToIgnore;
    }

    private boolean logsRequestBody(Interceptor interceptor) {
        return interceptor instanceof HttpLoggingInterceptor
                && ((HttpLoggingInterceptor) interceptor).getLevel().equals(HttpLoggingInterceptor.Level.BODY);
    }
}
