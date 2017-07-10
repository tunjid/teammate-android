package com.mainstreetcode.teammates.repository;


import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.MultipartBody;

import static com.mainstreetcode.teammates.repository.RepoUtils.getBody;
import static io.reactivex.Observable.just;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public class EventRepository {

    private static EventRepository ourInstance;

    private final TeammateApi api;
    //private final EventDao eventDao;

    private EventRepository() {
        api = TeammateService.getApiInstance();
    }

    public static EventRepository getInstance() {
        if (ourInstance == null) ourInstance = new EventRepository();
        return ourInstance;
    }

    public Observable<List<Event>> getEvents() {
        return api.getEvents();
    }


    public Observable<Event> getEvent(Event event) {
        return api.getEvent(event.getId())
                .flatMap(this::saveEvent)
                .observeOn(mainThread());
    }

    public Observable<Event> updateEvent(Event event) {
        Observable<Event> eventObservable = event.isEmpty()
                ? api.createEvent(event)
                : api.updateEvent(event.getId(), event);

        MultipartBody.Part body = getBody(event.get(Event.LOGO_POSITION).getValue(), Event.PHOTO_UPLOAD_KEY);
        if (body != null) {
            eventObservable = eventObservable.flatMap(put -> api.uploadEventPhoto(event.getId(), body));
        }

        return eventObservable.flatMap(this::saveEvent).observeOn(mainThread());
    }

    public Observable<Event> deleteEvent(Event event) {
        return api.deleteEvent(event.getId())
                .flatMap(team1 -> {
//                    List<User> users = event.getUsers();
//                    List<Role> roles = new ArrayList<>(users.size());
//
//                    for (User user : users) roles.add(user.getRole());
//
//                    roleDao.delete(roles);
//                    teamDao.delete(event);

                    return just(event);
                });
    }

    private Observable<List<Event>> saveEvents(List<Event> events) {
        //teamDao.insert(teams);
        return just(events);
    }

    /**
     * Used to save a event that has it's users populated
     */
    private Observable<Event> saveEvent(Event event) {
//        List<User> users = event.getUsers();
//        List<Role> roles = new ArrayList<>(users.size());
//
//        for (User user : users) roles.add(user.getRole());
//
//        teamDao.insert(Collections.singletonList(event));
//        userDao.insert(users);
//        roleDao.insert(roles);

        return just(event);
    }
}
