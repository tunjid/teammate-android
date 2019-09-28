/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.viewmodel

import androidx.lifecycle.ViewModel
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.preserveAscending
import com.mainstreetcode.teammate.util.preserveDescending
import com.mainstreetcode.teammate.viewmodel.events.Alert
import com.tunjid.androidx.recyclerview.diff.Differentiable
import io.reactivex.disposables.CompositeDisposable
import java.util.*


abstract class BaseViewModel : ViewModel() {

    private val ads = LinkedList<Differentiable>()
    private val disposable = CompositeDisposable()

    init {
        @Suppress("LeakingThis")
        disposable.add(App.instance.alerts().subscribe(this::onModelAlert, ErrorHandler.EMPTY::invoke))
        fetchAds()
    }

    internal open fun hasNativeAds(): Boolean = true

    internal open fun sortsAscending(): Boolean = false

    fun pushModelAlert(alert: Alert<*>) {
        App.instance.pushAlert(alert)
    }

    internal open fun onModelAlert(alert: Alert<*>) {}

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }

    fun preserveList(source: List<Differentiable>, additions: List<Differentiable>): List<Differentiable> {
        var output =
                if (sortsAscending()) preserveAscending(source, additions)
                else preserveDescending(source, additions)

        output = afterPreserveListDiff(output)
        if (hasNativeAds()) output = distributeAds(output)

        return output
    }

    internal open fun afterPreserveListDiff(source: List<Differentiable>): List<Differentiable> = source

    private fun distributeAds(source: List<Differentiable>): List<Differentiable> {
        //filterAds(source);
        if (source.isEmpty() || ads.isEmpty()) return source
        val mutableList = source.toMutableList()

        var numToShuffle = 0
        val adSize = ads.size
        val sourceSize = source.size
        var count = 0

        if (sourceSize <= AD_THRESH) {
            mutableList.add(ads[0])
            shuffleAds(++numToShuffle)
            return mutableList
        }

        var i = AD_THRESH
        while (i < sourceSize) {
            if (count >= adSize) break
            mutableList.add(i, ads[count])
            numToShuffle++
            count++
            i += AD_THRESH
        }

        shuffleAds(numToShuffle)

        return mutableList
    }

    private fun fetchAds() {
        //        App app = App.Companion.getInstance();
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
    //        Iterator<Differentiable> iterator = source.iterator();
    //    private void filterAds(List<Differentiable> source) {

    //    }

    private fun shuffleAds(count: Int) {
        for (i in 0 until count) ads.add(ads.removeFirst())
    }

    companion object {

        private const val AD_THRESH = 5
    }

}
