package com.mainstreetcode.teammate.adapters.viewholders;


import androidx.annotation.Nullable;
import android.view.View;

import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeAppInstallAdView;
import com.mainstreetcode.teammate.model.InstallAd;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;

public class InstallAdViewHolder extends AdViewHolder<InstallAd> {

    private NativeAppInstallAdView adView;

    public InstallAdViewHolder(View itemView, InteractiveAdapter.AdapterListener adapterListener) {
        super(itemView, adapterListener);
        adView = (NativeAppInstallAdView) itemView;

        adView.setHeadlineView(title);
        adView.setBodyView(subtitle);
        adView.setImageView(thumbnail);
    }


    @Override
    public void bind(InstallAd ad) {
        super.bind(ad);
        //Some assets are guaranteed to be in every NativeContentAd.
        NativeAppInstallAd installAd = ad.getNativeAd();

        title.setText(installAd.getHeadline());
        subtitle.setText(installAd.getBody());

//        ((TextView) adView.getCallToActionView()).setText(installAd.getCallToAction());
//        ((TextView) adView.getAdvertiserView()).setText(installAd.getAdvertiser());

//        List<NativeAd.Image> images = installAd.getImages();
//
//        if (images.size() > 0) {
//            images.get(0).getScale();
//            thumbnail.setImageDrawable(images.get(0).getDrawable());
//        }

        // Some aren't guaranteed, however, and should be checked.
        //NativeAd.Image logoImage = installAd.getLogo();

//        if (logoImage == null) {
//            adView.getLogoView().setVisibility(View.INVISIBLE);
//        }
//        else {
//            ((ImageView) adView.getLogoView()).setImageDrawable(logoImage.getDrawable());
//            adView.getLogoView().setVisibility(View.VISIBLE);
//        }

        // Assign native ad object to the native view.
        adView.setNativeAd(installAd);
    }

    @Nullable
    @Override
    String getImageUrl() {
        NativeAd.Image image = ad.getImage();
        return image == null ? null : image.getUri().toString();
    }
}
