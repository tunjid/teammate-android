package com.mainstreetcode.teammates.model;


import com.google.android.gms.ads.formats.NativeAppInstallAd;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.INSTALL_AD;

public class InstallAd extends Ad<NativeAppInstallAd> {

    public InstallAd(NativeAppInstallAd nativeAd) {
        super(nativeAd);
    }

    @Override
    public int getType() {return INSTALL_AD;}
}
