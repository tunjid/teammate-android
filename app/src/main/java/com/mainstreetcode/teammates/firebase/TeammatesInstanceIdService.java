package com.mainstreetcode.teammates.firebase;


import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.repository.UserRepository;
import com.mainstreetcode.teammates.util.ErrorHandler;

public class TeammatesInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        if (refreshedToken == null) return;

        UserRepository userRepository = UserRepository.getInstance();
        User user = userRepository.getCurrentUser();

        if (user == null) return;

        user.setFcmToken(refreshedToken);

        userRepository.createOrUpdate(user).subscribe(ignored -> {}, ErrorHandler.EMPTY);
    }
}
