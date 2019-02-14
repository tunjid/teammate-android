package com.mainstreetcode.teammate.adapters;

import android.content.Context;

import androidx.annotation.NonNull;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.SettingsViewHolder;
import com.mainstreetcode.teammate.model.SettingsItem;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;

import java.util.List;


public class SettingsAdapter extends InteractiveAdapter<SettingsViewHolder, SettingsAdapter.SettingsAdapterListener> {

    private final List<SettingsItem> items;

    public SettingsAdapter(List<SettingsItem> items, SettingsAdapterListener listener) {
        super(listener);
        this.items = items;
    }

    @NonNull
    @Override
    public SettingsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.viewholder_settings, viewGroup, false);
        return new SettingsViewHolder(itemView, adapterListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsViewHolder settingsViewHolder, int i) {
        settingsViewHolder.bind(items.get(i));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface SettingsAdapterListener extends InteractiveAdapter.AdapterListener {
        void onSettingsItemClicked(SettingsItem item);
    }

}
