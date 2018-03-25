package com.mainstreetcode.teammate.repository;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.model.Media;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.notifications.MediaNotifier;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.persistence.MediaDao;
import com.mainstreetcode.teammate.rest.ProgressRequestBody;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;
import com.mainstreetcode.teammate.util.TeammateException;

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

public class MediaRepository extends QueryRepository<Media> {

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
        MultipartBody.Part body = getBody(model.getUrl(), Media.UPLOAD_KEY);

        if (body == null) return Single.error(new TeammateException("Unable to upload media"));

        Single<Media> mediaSingle = api.uploadTeamMedia(model.getTeam().getId(), body)
                .map(getLocalUpdateFunction(model))
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

    public Single<Media> flag(Media model) {
        return api.flagMedia(model.getId())
                .map(getLocalUpdateFunction(model))
                .map(getSaveFunction());
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

    @Override
    Maybe<List<Media>> localModelsBefore(Team team, @Nullable Date date) {
        if (date == null) date = new Date();
        return mediaDao.getTeamMedia(team, date).subscribeOn(io());
    }

    @Override
    Maybe<List<Media>> remoteModelsBefore(Team team, @Nullable Date date) {
        return api.getTeamMedia(team.getId(), date).map(getSaveManyFunction()).toMaybe();
    }

    public Single<List<Media>> ownerDelete(List<Media> models) {
        return api.deleteMedia(models).doAfterSuccess(this::delete);
    }

    public Single<List<Media>> privilegedDelete(Team team, List<Media> models) {
        return api.adminDeleteMedia(team.getId(), models).doAfterSuccess(this::delete);
    }

    @Nullable
    MultipartBody.Part getBody(String path, String photoKey) {
        Uri uri = Uri.parse(path);
        String type = App.getInstance().getContentResolver().getType(uri);

        if (type == null) return null;
        RequestBody requestBody = new ProgressRequestBody(uri, num, MediaType.parse(type));

        return MultipartBody.Part.createFormData(photoKey, "test.jpg", requestBody);
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
