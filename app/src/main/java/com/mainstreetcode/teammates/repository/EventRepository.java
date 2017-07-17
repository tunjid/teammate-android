package com.mainstreetcode.teammates.repository;


import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.EventDao;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import okhttp3.MultipartBody;

import static com.mainstreetcode.teammates.repository.RepoUtils.getBody;
import static io.reactivex.Observable.concat;
import static io.reactivex.Observable.fromCallable;
import static io.reactivex.Observable.just;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;

public class EventRepository extends CrudRespository<Event> {

    private static EventRepository ourInstance;

    private final TeammateApi api;
    private final EventDao eventDao;
    private final TeamRepository teamRepository;

    private EventRepository() {
        api = TeammateService.getApiInstance();
        eventDao = AppDatabase.getInstance().eventDao();
        teamRepository = TeamRepository.getInstance();
    }

    public static EventRepository getInstance() {
        if (ourInstance == null) ourInstance = new EventRepository();
        return ourInstance;
    }

    @Override
    public Observable<Event> createOrUpdate(Event event) {
        Observable<Event> eventObservable = event.isEmpty()
                ? api.createEvent(event).flatMap(created -> updateLocal(created, event))
                : api.updateEvent(event.getId(), event).flatMap(created -> updateLocal(created, event));

        MultipartBody.Part body = getBody(event.get(Event.LOGO_POSITION).getValue(), Event.PHOTO_UPLOAD_KEY);
        if (body != null) {
            eventObservable = eventObservable.flatMap(put -> api.uploadEventPhoto(event.getId(), body));
        }

        return eventObservable.flatMap(this::save).observeOn(mainThread());
    }

    @Override
    public Observable<Event> get(String id) {
        return api.getEvent(id)
                .flatMap(this::save)
                .observeOn(mainThread());
    }

    @Override
    public Observable<List<Event>> getList(String userId) {
        Observable<List<Event>> local = fromCallable(() -> eventDao.getEvents(userId)).subscribeOn(io());
        Observable<List<Event>> remote = api.getEvents().flatMap(this::saveList);

        return concat(local, remote).observeOn(mainThread());
    }


//    public Observable<Event> deleteEvent(Event event) {
//        return api.deleteEvent(event.getId())
//                .flatMap(team1 -> {
////                    List<User> users = event.getUsers();
////                    List<Role> roles = new ArrayList<>(users.size());
////
////                    for (User user : users) roles.add(user.getRole());
////
////                    roleDao.delete(roles);
////                    teamDao.delete(event);
//
//                    return just(event);
//                });
//    }

    @Override
    Observable<List<Event>> saveList(List<Event> events) {
        List<Team> teams = new ArrayList<>(events.size());
        for (Event event : events) teams.add(event.getTeam());

        return teamRepository.saveTeams(teams).flatMap(savedTeams -> {
            eventDao.insert(Collections.unmodifiableList(events));
            return just(events);
        });
    }

    /**
     * Used to save a event that has it's users populated
     */
    @Override
    Observable<Event> save(Event event) {
        return saveList(Collections.singletonList(event)).flatMap(events -> just(events.get(0)));
    }
}
