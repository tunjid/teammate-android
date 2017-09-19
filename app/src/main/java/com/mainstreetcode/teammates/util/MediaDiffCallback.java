package com.mainstreetcode.teammates.util;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Media;

import java.util.List;

public class MediaDiffCallback extends DiffUtil.Callback {

    private final List<Media> stale;
    private final List<Media> updated;

    public MediaDiffCallback(List<Media> updated, List<Media> stale) {
        this.updated = updated;
        this.stale = stale;
    }

    @Override
    public int getOldListSize() {
        return stale.size();
    }

    @Override
    public int getNewListSize() {
        return updated.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return stale.get(oldItemPosition).getId().equals(updated.get(newItemPosition).getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return areItemsTheSame(oldItemPosition, newItemPosition);
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
