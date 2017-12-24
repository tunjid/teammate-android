package com.mainstreetcode.teammates.util;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Identifiable;

import java.util.List;

public class ModelDiffCallback extends DiffUtil.Callback {

    private final List<? extends Identifiable> stale;
    private final List<? extends Identifiable> updated;

    public ModelDiffCallback(List<? extends Identifiable> updated, List<? extends Identifiable> stale) {
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
