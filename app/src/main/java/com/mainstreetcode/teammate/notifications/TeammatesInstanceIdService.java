package com.mainstreetcode.teammate.notifications;


import android.text.TextUtils;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.mainstreetcode.teammate.model.Device;
import com.mainstreetcode.teammate.repository.DeviceRepository;
import com.mainstreetcode.teammate.repository.UserRepository;
import com.mainstreetcode.teammate.util.ErrorHandler;

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
