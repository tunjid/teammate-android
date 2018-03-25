package com.mainstreetcode.teammate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.SettingsItem;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

/**
 * Adapter for {@link com.mainstreetcode.teammate.model.SettingsItem}
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class SettingsAdapter extends BaseRecyclerViewAdapter<SettingsAdapter.SettingsViewHolder, SettingsAdapter.SettingsAdapterListener> {

    private final List<SettingsItem> items;

    public SettingsAdapter(List<SettingsItem> items, SettingsAdapterListener listener) {
        super(listener);
        this.items = items;
    }

    @Override
    public SettingsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.viewholder_settings, viewGroup, false);
        return new SettingsViewHolder(itemView, adapterListener);
    }

    @Override
    public void onBindViewHolder(SettingsViewHolder settingsViewHolder, int i) {
        settingsViewHolder.bind(items.get(i));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface SettingsAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onSettingsItemClicked(SettingsItem item);
    }

    static class SettingsViewHolder extends BaseViewHolder<SettingsAdapterListener>
            implements View.OnClickListener {

        private SettingsItem item;
        private TextView itemName;

        SettingsViewHolder(View itemView, SettingsAdapterListener adapterListener) {
            super(itemView, adapterListener);
            itemName = itemView.findViewById(R.id.item_name);
            itemName.setOnClickListener(this);
        }

        void bind(SettingsItem item) {
            this.item = item;
            itemName.setText(item.getStringResorce());
        }

        @Override
        public void onClick(View view) {
            adapterListener.onSettingsItemClicked(item);
        }
    }
}
