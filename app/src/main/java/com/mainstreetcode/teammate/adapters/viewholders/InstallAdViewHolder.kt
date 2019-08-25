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

package com.mainstreetcode.teammate.adapters.viewholders


import android.view.View

import com.mainstreetcode.teammate.model.InstallAd
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter

class InstallAdViewHolder(itemView: View, adapterListener: InteractiveAdapter.AdapterListener) : AdViewHolder<InstallAd>(itemView, adapterListener) {

    override val imageUrl: String?
        get() = null
    //
    //    private NativeAppInstallAdView adView;
    //
    //    public InstallAdViewHolder(View itemView, InteractiveAdapter.AdapterListener adapterListener) {
    //        super(itemView, adapterListener);
    //        adView = (NativeAppInstallAdView) itemView;
    //
    //        adView.setHeadlineView(title);
    //        adView.setBodyView(subtitle);
    //        adView.setImageView(thumbnail);
    //    }
    //
    //
    //    @Override
    //    public void bind(InstallAd ad) {
    //        super.bind(ad);
    //        //Some assets are guaranteed to be in every NativeContentAd.
    //        NativeAppInstallAd installAd = ad.getNativeAd();
    //
    //        title.setText(installAd.getHeadline());
    //        subtitle.setText(installAd.getBody());
    //
    ////        ((TextView) adView.getCallToActionView()).setText(installAd.getCallToAction());
    ////        ((TextView) adView.getAdvertiserView()).setText(installAd.getAdvertiser());
    //
    ////        List<NativeAd.Image> images = installAd.getImages();
    ////
    ////        if (images.size() > 0) {
    ////            images.get(0).getScale();
    ////            thumbnail.setImageDrawable(images.get(0).getDrawable());
    ////        }
    //
    //        // Some aren't guaranteed, however, and should be checked.
    //        //NativeAd.Image logoImage = installAd.getLogo();
    //
    ////        if (logoImage == null) {
    ////            adView.getLogoView().setVisibility(View.INVISIBLE);
    ////        }
    ////        else {
    ////            ((ImageView) adView.getLogoView()).setImageDrawable(logoImage.getDrawable());
    ////            adView.getLogoView().setVisibility(View.VISIBLE);
    ////        }
    //
    //        // Assign native ad object to the native view.
    //        adView.setNativeAd(installAd);
    //    }
    //
    //    @Nullable
    //    @Override
    //    String getImageUrl() {
    //        NativeAd.Image image = ad.getImage();
    //        return image == null ? null : image.getUri().toString();
    //    }
}
