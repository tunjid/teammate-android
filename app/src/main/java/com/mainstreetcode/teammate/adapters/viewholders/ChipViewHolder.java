package com.mainstreetcode.teammate.adapters.viewholders;

import android.support.annotation.NonNull;
import android.support.design.chip.Chip;
import android.view.View;

import com.mainstreetcode.teammate.adapters.StatTypeAdapter;
import com.mainstreetcode.teammate.model.enums.StatAttribute;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

public class ChipViewHolder extends BaseViewHolder<StatTypeAdapter.AdapterListener> {
    private Chip chip;
    private StatAttribute attribute;

    public ChipViewHolder(@NonNull View itemView, StatTypeAdapter.AdapterListener listener) {
        super(itemView, listener);
        chip = (Chip) itemView;
        chip.setOnClickListener(view -> adapterListener.onAttributeTapped(attribute));
    }

    public void bind(StatAttribute attribute) {
        this.attribute = attribute;
        chip.setText(attribute.getName());
        chip.setEnabled(adapterListener.isEnabled());
        chip.setChecked(adapterListener.isSelected(attribute));
    }
}
