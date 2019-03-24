package com.mainstreetcode.teammate.viewmodel.events;

import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Model;
import com.tunjid.androidbootstrap.functions.Consumer;

public abstract class Alert<T extends Model<T>> {

    private T model;

    private Alert(T model) { this.model = model; }

    public T getModel() { return model; }

    public static <T extends Model<T>> Creation<T> creation(T model) { return new Creation<>(model); }

    public static <T extends Model<T>> Deletion<T> deletion(T model) { return new Deletion<>(model); }

    public static Alert<Event> eventAbsentee(Event event) {return new EventAbsentee(event);}

    public static Alert<JoinRequest> requestProcessed(JoinRequest joinRequest) {return new JoinRequestProcessed(joinRequest);}

    public static class Creation<T extends Model<T>> extends Alert<T> {
        private Creation(T model) { super(model); }
    }

    public static class Deletion<T extends Model<T>> extends Alert<T> {
        private Deletion(T model) { super(model); }

    }
    public static class EventAbsentee extends Alert<Event> {
        private EventAbsentee(Event model) { super(model); }

    }
    public static class JoinRequestProcessed extends Alert<JoinRequest> {
        private JoinRequestProcessed(JoinRequest model) { super(model); }
    }

    @SuppressWarnings("unchecked")
    public static void matches(Alert alert, Capsule... testers) {
        for (Capsule capsule : testers) {
            if (!alert.getClass().equals(capsule.alertClass)) return;

            Object model = alert.getModel();
            if (model.getClass().equals(capsule.item)) capsule.consumer.accept(model);
        }
    }

    public static <A extends Alert<T>, T extends Model<T>>Capsule of(Class<A> alert, Class<T> item, Consumer<T> consumer) {
        return new Capsule<>(alert, item, consumer);
    }

    public static class Capsule<A extends Alert<T>, T extends Model<T>> {
        Class<A> alertClass;
        Class<T> item;
        Consumer<T> consumer;

         Capsule(Class<A> alertClass, Class<T> item, Consumer<T> consumer) {
            this.alertClass = alertClass;
            this.item = item;
            this.consumer = consumer;
        }

    }
}
