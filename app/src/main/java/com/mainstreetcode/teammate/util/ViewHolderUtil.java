package com.mainstreetcode.teammate.util;


import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ViewHolderUtil {

    public static final int TEAM = 0;
    public static final int CHAT = 1;
    public static final int ROLE = 2;
    public static final int EVENT = 3;
    public static final int FEED_ITEM = 4;
    public static final int CONTENT_AD = 5;
    public static final int INSTALL_AD = 6;
    public static final int MEDIA_IMAGE = 7;
    public static final int MEDIA_VIDEO = 8;
    public static final int JOIN_REQUEST = 9;
    public static final int BLOCKED_USER = 10;
    public static final int THUMBNAIL_SIZE = 250;
    public static final int FULL_RES_LOAD_DELAY = 200;

    public static View getItemView(@LayoutRes int res, ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(res, parent, false);
    }

    public static String getTransitionName(Object item, @IdRes int id) {
        return item.hashCode() + "-" + id;
    }

    public static ViewGroup.MarginLayoutParams getLayoutParams(View view) {
        return (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    }

    @Nullable
    public static Activity getActivity(RecyclerView.ViewHolder viewHolder) {
        Context context = viewHolder.itemView.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) return (Activity)context;
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }
}
