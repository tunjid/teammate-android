package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.mainstreetcode.teammates.App;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Ad;
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


abstract class BaseViewModel extends ViewModel {

    private static final int AD_THRESH = 5;

    private LinkedList<Model> ads = new LinkedList<>();

    BaseViewModel() {fetchAds();}

    void distributeAds(List<Identifiable> source) {
        if (source.isEmpty() || ads.isEmpty()) return;

        Iterator<Identifiable> iterator = source.iterator();
        while (iterator.hasNext()) if (iterator.next() instanceof Ad) iterator.remove();

        int adSize = ads.size();
        int sourceSize = source.size();
        int count = 0;

        if (sourceSize < AD_THRESH) {
            source.add(ads.get(0));
            return;
        }

        for (int i = AD_THRESH; i < sourceSize; i += AD_THRESH) {
            if (count > adSize) break;
            source.add(i, ads.get(count));
            count++;
        }
    }

    private void fetchAds() {
        App app = App.getInstance();
        AdLoader adLoader = new AdLoader.Builder(app, app.getString(R.string.admob_ad_id))
                .forAppInstallAd(appInstallAd -> {
                    // Show the app install ad.
                })
                .forContentAd(contentAd -> {
                    ads.add(new Ad<>(contentAd));
                    if (ads.size() < 5) fetchAds();
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // Handle the failure by logging, altering the UI, and so on.
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build();

        // Load the Native Express ad.
        adLoader.loadAd(new AdRequest.Builder().build());
    }
}
