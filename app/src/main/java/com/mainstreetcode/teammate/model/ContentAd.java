package com.mainstreetcode.teammate.model;


import com.google.android.gms.ads.formats.NativeContentAd;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.CONTENT_AD;

public class ContentAd extends Ad<NativeContentAd> {

    public ContentAd(NativeContentAd nativeAd) {
        super(nativeAd);
    }

    @Override
    public int getType() {return CONTENT_AD;}
}
