package com.mainstreetcode.teammate.adapters.viewholders;

import androidx.annotation.NonNull;
import com.google.android.material.chip.Chip;
import android.view.View;

import com.mainstreetcode.teammate.adapters.StatTypeAdapter;
import com.mainstreetcode.teammate.model.enums.StatAttribute;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveViewHolder;

public class ChipViewHolder extends InteractiveViewHolder<StatTypeAdapter.AdapterListener> {
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
