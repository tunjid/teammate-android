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

import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

public class ViewHolderUtil {

    public static final int USER = 0;
    public static final int TEAM = 1;
    public static final int CHAT = 2;
    public static final int ROLE = 3;
    public static final int GAME = 4;
    public static final int STAT = 5;
    public static final int EVENT = 6;
    public static final int FEED_ITEM = 7;
    public static final int TOURNAMENT = 8;
    public static final int CONTENT_AD = 9;
    public static final int INSTALL_AD = 10;
    public static final int MEDIA_IMAGE = 11;
    public static final int MEDIA_VIDEO = 12;
    public static final int JOIN_REQUEST = 13;
    public static final int BLOCKED_USER = 14;
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
            if (context instanceof Activity) return (Activity) context;
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public interface SimpleAdapterListener<T> extends BaseRecyclerViewAdapter.AdapterListener {
        void onItemClicked(T item);
    }
}
