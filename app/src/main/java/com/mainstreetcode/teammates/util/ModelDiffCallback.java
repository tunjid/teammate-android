package com.mainstreetcode.teammates.util;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Model;

import java.util.List;

public class ModelDiffCallback extends DiffUtil.Callback {

    private final List<? extends Model> stale;
    private final List<? extends Model> updated;

    public ModelDiffCallback(List<? extends Model> updated, List<? extends Model> stale) {
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
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
