package com.mainstreetcode.teammates.util;


import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ViewHolderUtil {

    public static final int AD = 0;
    public static final int TEAM = 1;
    public static final int CHAT = 2;
    public static final int ROLE = 3;
    public static final int EVENT = 4;
    public static final int MEDIA = 5;
    public static final int FEED_ITEM = 6;
    public static final int JOIN_REQUEST = 7;

    public static View getItemView(@LayoutRes int res, ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(res, parent, false);
    }

    public static String getTransitionName(Object item, @IdRes int id) {
        return item.hashCode() + "-" + id;
    }
}
