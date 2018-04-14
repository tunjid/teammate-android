package com.mainstreetcode.teammate.viewmodel;

import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.repository.JoinRequestRepository;
import com.mainstreetcode.teammate.repository.RoleRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * ViewModel for roles in a team
 */

public class RoleViewModel extends MappedViewModel<Class<Role>, Role> {

    private final RoleRepository roleRepository;
    private final JoinRequestRepository joinRequestRepository;

    static final List<Identifiable> roles = new ArrayList<>();

    public RoleViewModel() {
        roleRepository = RoleRepository.getInstance();
        joinRequestRepository = JoinRequestRepository.getInstance();
    }

    @Override
    boolean sortsAscending() {
        return true;
    }

    public Single<JoinRequest> joinTeam(JoinRequest joinRequest) {
        return joinRequestRepository.createOrUpdate(joinRequest).observeOn(mainThread());
    }

    @Override
    Flowable<List<Role>> fetch(Class<Role> roleClass, boolean fetchLatest) {
        return roleRepository.getMyRoles();
    }

    @Override
    public List<Identifiable> getModelList(Class<Role> roleClass) {
        return roles;
    }
}
