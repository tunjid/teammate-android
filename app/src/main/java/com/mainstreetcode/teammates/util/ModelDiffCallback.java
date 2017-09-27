package com.mainstreetcode.teammates.util;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Model;

import java.util.List;

public class ModelDiffCallback<T extends Model> extends DiffUtil.Callback {

    private final List<T> stale;
    private final List<T> updated;

    public ModelDiffCallback(List<T> updated, List<T> stale) {
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
