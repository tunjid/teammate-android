package com.mainstreetcode.teammates.notifications;


import android.text.TextUtils;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.mainstreetcode.teammates.model.Device;
import com.mainstreetcode.teammates.repository.DeviceRepository;
import com.mainstreetcode.teammates.repository.UserRepository;
import com.mainstreetcode.teammates.util.ErrorHandler;

public class TeammatesInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        updateFcmToken();
    }

    public static void updateFcmToken() {
        String token = FirebaseInstanceId.getInstance().getToken();
        if (TextUtils.isEmpty(token) || UserRepository.getInstance().getCurrentUser() == null)
            return;

        Device device = new Device();
        device.setFcmToken(token);

        DeviceRepository.getInstance().createOrUpdate(device).subscribe(ignored -> {}, ErrorHandler.EMPTY);
    }
}
