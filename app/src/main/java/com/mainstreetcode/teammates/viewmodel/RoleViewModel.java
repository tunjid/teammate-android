package com.mainstreetcode.teammates.viewmodel;

import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.repository.JoinRequestRepository;
import com.mainstreetcode.teammates.repository.RoleRepository;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;
import com.mainstreetcode.teammates.util.ErrorHandler;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * ViewModel for roles in a team
 */

public class RoleViewModel extends MappedViewModel<Class<Role>, Role> {

    private int count;
    private final RoleRepository roleRepository;
    private final JoinRequestRepository joinRequestRepository;

    private final TeammateApi api = TeammateService.getApiInstance();
    private final List<String> roleNames = new ArrayList<>();
    static final List<Identifiable> roles = new ArrayList<>();


    public RoleViewModel() {
        roleRepository = RoleRepository.getInstance();
        joinRequestRepository = JoinRequestRepository.getInstance();
    }

    public List<String> getRoleNames() {
        return roleNames;
    }

    public void fetchRoleValues() {
        Maybe<List<String>> listMaybe = !roleNames.isEmpty() && count++ % 5 != 0
                ? Maybe.empty()
                : api.getRoleValues().toMaybe();

        listMaybe.observeOn(mainThread()).subscribe(names -> {
            roleNames.clear();
            roleNames.addAll(names);
            count++;
        }, ErrorHandler.EMPTY);
    }

    public Single<JoinRequest> joinTeam(JoinRequest joinRequest) {
        return joinRequestRepository.createOrUpdate(joinRequest).observeOn(mainThread());
    }

    public Flowable<DiffUtil.DiffResult> getMyRoles(String userId) {
        Flowable<List<Identifiable>> sourceFlowable = roleRepository.getMyRoles(userId).map(toIdentifiable);
        return Identifiable.diff(sourceFlowable, () -> getModelList(Role.class), preserveList);
    }

    @Override
    public List<Identifiable> getModelList(Class<Role> key) {
        return roles;
    }
}
