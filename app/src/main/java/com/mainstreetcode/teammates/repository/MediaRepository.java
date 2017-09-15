package com.mainstreetcode.teammates.repository;

import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.notifications.MediaNotifier;
import com.mainstreetcode.teammates.persistence.AppDatabase;
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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static io.reactivex.Single.just;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;

public class MediaRepository extends ModelRespository<Media> {

    private final TeammateApi api;
    private final MediaDao mediaDao;
    private final ModelRespository<User> userRepository;
    private final ModelRespository<Team> teamRespository;

    private static MediaRepository ourInstance;

    private MediaRepository() {
        api = TeammateService.getApiInstance();
        mediaDao = AppDatabase.getInstance().mediaDao();
        userRepository = UserRepository.getInstance();
        teamRespository = TeamRepository.getInstance();
    }

    public static MediaRepository getInstance() {
        if (ourInstance == null) ourInstance = new MediaRepository();
        return ourInstance;
    }

    @Override
    public Single<Media> createOrUpdate(Media model) {
        MultipartBody.Part body = getBody(model.getImageUrl(), Media.UPLOAD_KEY);

        if (body == null) return Single.error(new TeammateException("Unable to upload media"));

        Single<Media> mediaSingle = api.uploadTeamMedia(model.getTeam().getId(), body);
        mediaSingle = mediaSingle.map(localMapper(model)).map(getSaveFunction()).observeOn(mainThread());

        return MediaNotifier.getInstance().notifyOfUploads(mediaSingle, body.body());
    }

    @Override
    public Flowable<Media> get(String id) {
        Maybe<Media> local = mediaDao.get(id).subscribeOn(io());
        Maybe<Media> remote = api.getMedia(id).map(getSaveFunction()).toMaybe();

        return cacheThenRemote(local, remote);
    }

    @Override
    public Single<Media> delete(Media model) {
        mediaDao.delete(Collections.singletonList(model));
        return just(model);
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
            teamRespository.getSaveManyFunction().apply(teams);

            mediaDao.upsert(Collections.unmodifiableList(models));

            return models;
        };
    }

    public Flowable<List<Media>> getTeamMedia(Team team, Date date) {
        Maybe<List<Media>> local = mediaDao.getTeamMedia(team).subscribeOn(io());
        Maybe<List<Media>> remote = api.getTeamMedia(team.getId(), date).toMaybe();

        return cacheThenRemote(local, remote);
    }

    @Nullable
    MultipartBody.Part getBody(String path, String photokey) {
        File file = new File(path);

        if (!file.exists()) return null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);

        if (extension == null) return null;
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        if (type == null) return null;
        RequestBody requestBody = new ProgressRequestBody(file, 2, MediaType.parse(type));

        return MultipartBody.Part.createFormData(photokey, file.getName(), requestBody);
    }
}
