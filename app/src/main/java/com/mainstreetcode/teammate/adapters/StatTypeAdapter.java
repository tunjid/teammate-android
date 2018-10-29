package com.mainstreetcode.teammate.adapters;

import androidx.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.ChipViewHolder;
import com.mainstreetcode.teammate.model.enums.StatAttribute;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;

import java.util.List;

public class StatTypeAdapter extends InteractiveAdapter<ChipViewHolder, StatTypeAdapter.AdapterListener> {

    public StatTypeAdapter(AdapterListener adapterListener) {
        super(adapterListener);
    }

    @NonNull
    @Override
    public ChipViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ChipViewHolder(getItemView(R.layout.viewholder_stat_attribute, viewGroup), adapterListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ChipViewHolder chipViewHolder, int i) {
        chipViewHolder.bind(adapterListener.getAttributes().get(i));
    }

    @Override
    public int getItemCount() {
        return adapterListener.getAttributes().size();
    }

    public interface AdapterListener extends InteractiveAdapter.AdapterListener {

        List<StatAttribute> getAttributes();

        boolean isEnabled();

        boolean isSelected(StatAttribute attribute);

        void onAttributeTapped(StatAttribute attribute);
    }
}
