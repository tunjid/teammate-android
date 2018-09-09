package com.mainstreetcode.teammate.adapters;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.ChipViewHolder;
import com.mainstreetcode.teammate.model.enums.StatAttribute;
import com.mainstreetcode.teammate.model.enums.StatType;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

public class StatTypeAdapter extends BaseRecyclerViewAdapter<ChipViewHolder, StatTypeAdapter.AdapterListener> {

    private StatType statType = StatType.empty();

    public StatTypeAdapter(AdapterListener adapterListener) {
        super(adapterListener);
    }

    @NonNull
    @Override
    public ChipViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ChipViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_stat_attribute, viewGroup), adapterListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ChipViewHolder chipViewHolder, int i) {
        chipViewHolder.bind(statType.getAttributes().get(i));
    }

    @Override
    public int getItemCount() {
        return statType.getAttributes().size();
    }

    public void updateStatType(StatType statType) {
        this.statType.update(statType);
        notifyDataSetChanged();
    }

    public interface AdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        boolean isSelected(StatAttribute attribute);

        void onAttributeTapped(StatAttribute attribute);
    }
}
