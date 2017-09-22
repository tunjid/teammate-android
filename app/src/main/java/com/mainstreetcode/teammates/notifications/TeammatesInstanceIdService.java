package com.mainstreetcode.teammates.notifications;


import android.text.TextUtils;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.repository.UserRepository;
import com.mainstreetcode.teammates.util.ErrorHandler;

public class TeammatesInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        updateFcmToken();
    }

    public static void updateFcmToken() {
        String token = FirebaseInstanceId.getInstance().getToken();
        if (TextUtils.isEmpty(token)) return;

        UserRepository userRepository = UserRepository.getInstance();
        User user = userRepository.getCurrentUser();

        if (user == null) return;

        user.setFcmToken(token);

        userRepository.createOrUpdate(user).subscribe(ignored -> {}, ErrorHandler.EMPTY);
    }
}
