package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * ViewModel for signed in team
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

public class RoleViewModel extends ViewModel {

    private TeammateApi api = TeammateService.getApiInstance();
    private BehaviorSubject<List<String>> roleSubject = BehaviorSubject.create();

    public Observable<List<String>> getRoleValues() {
        // Use new subject if previous call errored out for whatever reason.
        if (roleSubject.hasThrowable()) roleSubject = BehaviorSubject.create();
        api.getRoleValues().subscribe(roleSubject);
        return roleSubject;
    }

}
