package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.mainstreetcode.teammates.App;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Ad;
import com.mainstreetcode.teammates.model.Model;

import java.util.ArrayList;
import java.util.List;


public class BaseViewModel extends ViewModel {

    List<Model> ads = new ArrayList<>();
    private AdLoader adLoader;

    public BaseViewModel() {
        App app = App.getInstance();

        adLoader = new AdLoader.Builder(app, app.getString(R.string.admob_ad_id))
                .forAppInstallAd(appInstallAd -> {
                    // Show the app install ad.
                })
                .forContentAd(contentAd -> {
                    // Show the content ad.
                    ads.add((Model) new Ad<>(contentAd));
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
