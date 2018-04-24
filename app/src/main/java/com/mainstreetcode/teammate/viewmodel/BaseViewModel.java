package com.mainstreetcode.teammate.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.mainstreetcode.teammate.viewmodel.events.Alert;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.PublishProcessor;


abstract class BaseViewModel extends ViewModel {

    private static final int AD_THRESH = 5;

    private static final PublishProcessor<Alert> eventSource = PublishProcessor.create();

    private LinkedList<Identifiable> ads = new LinkedList<>();
    private AtomicInteger pullToRefreshCount = new AtomicInteger(0);
    private CompositeDisposable disposable = new CompositeDisposable();

    BaseViewModel() {
        disposable.add(eventSource.subscribe(this::onModelAlert, ErrorHandler.EMPTY));
        fetchAds();
    }

    boolean hasNativeAds() {return true;}

    boolean sortsAscending() {return false;}

    void pushModelAlert(Alert alert) { eventSource.onNext(alert); }

    void onModelAlert(Alert alert) {}

    @Override
    protected void onCleared() {
        disposable.clear();
        super.onCleared();
    }

    final List<Identifiable> preserveList(List<Identifiable> source, List<Identifiable> additions) {
        if (sortsAscending()) ModelUtils.preserveAscending(source, additions);
        else ModelUtils.preserveDescending(source, additions);

        afterPreserveListDiff(source);
        if (hasNativeAds()) distributeAds(source);
        return source;
    }

    final List<Identifiable> pullToRefresh(List<Identifiable> source, List<Identifiable> additions) {
        int count;
        if ((count = pullToRefreshCount.incrementAndGet()) == 1) source.clear();
        if (count >= 2) pullToRefreshCount.set(0);

        preserveList(source, additions);
        afterPullToRefreshDiff(source);
        return source;
    }

    void afterPullToRefreshDiff(List<Identifiable> source) {}

    void afterPreserveListDiff(List<Identifiable> source) {}

    private void distributeAds(List<Identifiable> source) {
        //filterAds(source);
        if (source.isEmpty() || ads.isEmpty()) return;

        int numToShuffle = 0;
        int adSize = ads.size();
        int sourceSize = source.size();
        int count = 0;

        if (sourceSize <= AD_THRESH) {
            source.add(ads.get(0));
            shuffleAds(++numToShuffle);
            return;
        }

        for (int i = AD_THRESH; i < sourceSize; i += AD_THRESH) {
            if (count >= adSize) break;
            source.add(i, ads.get(count));
            numToShuffle++;
            count++;
        }
        shuffleAds(numToShuffle);
    }

    private void fetchAds() {
//        App app = App.getInstance();
//        AdLoader adLoader = new AdLoader.Builder(app, app.getString(R.string.admob_ad_id))
//                .forAppInstallAd(appInstallAd -> ads.add(new InstallAd(appInstallAd)))
//                .forContentAd(contentAd -> ads.add(new ContentAd(contentAd)))
//                .withAdListener(new AdListener() {
//                    @Override
//                    public void onAdFailedToLoad(int errorCode) {
//                        // Handle the failure by logging, altering the UI, and so on.
//                    }
//                })
//                .withNativeAdOptions(new NativeAdOptions.Builder()
//                        .build())
//                .build();
//
//        adLoader.loadAds(new AdRequest.Builder().build(), 2);
    }
    //        while (iterator.hasNext()) if (iterator.next() instanceof ContentAd) iterator.remove();
    //        Iterator<Identifiable> iterator = source.iterator();
//    private void filterAds(List<Identifiable> source) {

//    }

    private void shuffleAds(int count) {
        for (int i = 0; i < count; i++) ads.add(ads.removeFirst());
    }

}
