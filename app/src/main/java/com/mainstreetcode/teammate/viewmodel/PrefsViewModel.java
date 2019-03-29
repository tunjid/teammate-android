package com.mainstreetcode.teammate.viewmodel;

import com.mainstreetcode.teammate.model.Prefs;
import com.mainstreetcode.teammate.repository.PrefsRepo;
import com.mainstreetcode.teammate.repository.RepoProvider;

public class PrefsViewModel extends BaseViewModel {

    private Prefs prefs;
    private final PrefsRepo prefsRepository = RepoProvider.forRepo(PrefsRepo.class);

    public PrefsViewModel() {
        prefs = prefsRepository.getCurrent();
    }
    public boolean isOnBoarded() { return prefs.isOnBoarded(); }

    public void setOnBoarded(boolean isOnBoarded) {
        prefs.setOnBoarded(isOnBoarded);
        prefsRepository.createOrUpdate(prefs);
    }
}
