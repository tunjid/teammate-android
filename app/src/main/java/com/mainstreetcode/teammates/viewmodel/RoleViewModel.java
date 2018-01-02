package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.repository.JoinRequestRepository;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;
import com.mainstreetcode.teammates.util.ErrorHandler;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Single;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * ViewModel for roles in a team
 */

public class RoleViewModel extends ViewModel {

    private int count;
    private final JoinRequestRepository joinRequestRepository;

    private final TeammateApi api = TeammateService.getApiInstance();
    private final List<String> roleNames = new ArrayList<>();

    public RoleViewModel() {
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
}
