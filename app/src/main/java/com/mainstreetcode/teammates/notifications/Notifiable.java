package com.mainstreetcode.teammates.notifications;


import com.mainstreetcode.teammates.model.Model;

public interface Notifiable<T extends Model<T> & Notifiable<T>> {
    Notifier<T> getNotifier();
}
