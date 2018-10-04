package com.mainstreetcode.teammate.viewmodel;

import com.mainstreetcode.teammate.model.Prefs;
import com.mainstreetcode.teammate.repository.PrefsRepository;

public class PrefsViewModel extends BaseViewModel {

    private Prefs prefs;
    private final PrefsRepository prefsRepository = PrefsRepository.getInstance();

    public PrefsViewModel() {
        prefs = prefsRepository.getCurrent();
    }
    public boolean isOnBoarded() { return prefs.isOnBoarded(); }

    public void setOnBoarded(boolean isOnBoarded) {
        prefs.setOnBoarded(isOnBoarded);
        prefsRepository.createOrUpdate(prefs);
    }
}
