/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.services;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mainstreetcode.teammate.model.Device;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.notifications.FeedItem;
import com.mainstreetcode.teammate.notifications.NotifierProvider;
import com.mainstreetcode.teammate.repository.DeviceRepo;
import com.mainstreetcode.teammate.repository.RepoProvider;
import com.mainstreetcode.teammate.repository.UserRepo;
import com.mainstreetcode.teammate.util.ErrorHandler;


public class TeammateMessagingService extends FirebaseMessagingService {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(result -> updateFcmToken(result.getToken()));
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) { handleMessage(remoteMessage); }

    @Override
    public void onNewToken(String token) { updateFcmToken(token); }

    public <T extends Model<T>> void handleMessage(RemoteMessage remoteMessage) {
        FeedItem<T> item = FeedItem.Companion.fromNotification(remoteMessage);
        if (item == null) return;

        if (item.isDeleteAction())
            RepoProvider.Companion.forModel(item.getItemClass()).queueForLocalDeletion(item.getModel());
        else NotifierProvider.Companion.forModel(item.getItemClass()).notify(item);
    }

    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void updateFcmToken(String token) {
        if (TextUtils.isEmpty(token) || RepoProvider.Companion.forRepo(UserRepo.class).getCurrentUser().isEmpty())
            return;

        RepoProvider.Companion.forRepo(DeviceRepo.class).createOrUpdate(Device.withFcmToken(token)).subscribe(__ -> {}, ErrorHandler.EMPTY);
    }

}
