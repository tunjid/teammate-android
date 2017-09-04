package com.mainstreetcode.teammates.notifications;


import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.repository.JoinRequestRepository;
import com.mainstreetcode.teammates.repository.ModelRespository;


public class JoinRequestNotifier extends Notifier<JoinRequest> {

    private static JoinRequestNotifier INSTANCE;

    private JoinRequestNotifier() {

    }

    public static JoinRequestNotifier getInstance() {
        if (INSTANCE == null) INSTANCE = new JoinRequestNotifier();
        return INSTANCE;
    }

    @Override
    protected ModelRespository<JoinRequest> getRepository() {
        return JoinRequestRepository.getInstance();
    }
}
