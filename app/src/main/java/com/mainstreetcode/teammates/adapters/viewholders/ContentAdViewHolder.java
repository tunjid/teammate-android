package com.mainstreetcode.teammates.adapters.viewholders;


import android.view.View;

import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeContentAdView;
import com.mainstreetcode.teammates.model.Ad;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

public class ContentAdViewHolder extends AdViewHolder<NativeContentAd> {

    private NativeContentAdView adView;

    public ContentAdViewHolder(View itemView, BaseRecyclerViewAdapter.AdapterListener adapterListener) {
        super(itemView, adapterListener);
        adView = (NativeContentAdView) itemView;

        adView.setHeadlineView(title);
        adView.setBodyView(subtitle);
        adView.setImageView(thumbnail);
    }


    @Override
    public void bind(Ad<NativeContentAd> ad) {
        super.bind(ad);
        //Some assets are guaranteed to be in every NativeContentAd.
        NativeContentAd nativeContentAd = ad.getNativeAd();

        title.setText(nativeContentAd.getHeadline());
        subtitle.setText(nativeContentAd.getBody());

//        ((TextView) adView.getCallToActionView()).setText(nativeContentAd.getCallToAction());
//        ((TextView) adView.getAdvertiserView()).setText(nativeContentAd.getAdvertiser());

//        List<NativeAd.Image> images = nativeContentAd.getImages();
//
//        if (images.size() > 0) {
//            images.get(0).getScale();
//            thumbnail.setImageDrawable(images.get(0).getDrawable());
//        }

        // Some aren't guaranteed, however, and should be checked.
        NativeAd.Image logoImage = nativeContentAd.getLogo();

//        if (logoImage == null) {
//            adView.getLogoView().setVisibility(View.INVISIBLE);
//        }
//        else {
//            ((ImageView) adView.getLogoView()).setImageDrawable(logoImage.getDrawable());
//            adView.getLogoView().setVisibility(View.VISIBLE);
//        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeContentAd);
    }
}
