package com.mainstreetcode.teammate.adapters.viewholders;


import androidx.annotation.Nullable;
import android.view.View;

import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeContentAdView;
import com.mainstreetcode.teammate.model.ContentAd;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;

public class ContentAdViewHolder extends AdViewHolder<ContentAd> {

    private NativeContentAdView adView;

    public ContentAdViewHolder(View itemView, InteractiveAdapter.AdapterListener adapterListener) {
        super(itemView, adapterListener);
        adView = (NativeContentAdView) itemView;

        adView.setHeadlineView(title);
        adView.setBodyView(subtitle);
        adView.setImageView(thumbnail);
    }


    @Override
    public void bind(ContentAd ad) {
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
        //NativeAd.Image logoImage = nativeContentAd.getLogo();

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

    @Nullable
    @Override
    String getImageUrl() {
        NativeAd.Image image = ad.getImage();
        return image == null ? null : image.getUri().toString();
    }
}
