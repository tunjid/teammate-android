package com.mainstreetcode.teammate.adapters.viewholders;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.SettingsAdapter;
import com.mainstreetcode.teammate.model.SettingsItem;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

public class SettingsViewHolder extends InteractiveViewHolder<SettingsAdapter.SettingsAdapterListener>
        implements View.OnClickListener {

    private SettingsItem item;
    private TextView itemName;

   public SettingsViewHolder(View itemView, SettingsAdapter.SettingsAdapterListener adapterListener) {
        super(itemView, adapterListener);
        itemName = itemView.findViewById(R.id.item_name);
        itemName.setOnClickListener(this);
    }

   public void bind(SettingsItem item) {
        this.item = item;
        itemName.setText(item.getStringRes());
        itemName.setCompoundDrawablesRelativeWithIntrinsicBounds(getIcon(), null, null, null);
    }

    @Nullable
    Drawable getIcon() {
        Drawable src = ContextCompat.getDrawable(itemView.getContext(), item.getDrawableRes());
        if (src == null) return null;

        Drawable wrapped = DrawableCompat.wrap(src);
        DrawableCompat.setTint(wrapped, ViewHolderUtil.resolveThemeColor(itemView.getContext(), R.attr.alt_icon_tint));

        return wrapped;
    }

    @Override
    public void onClick(View view) {
        adapterListener.onSettingsItemClicked(item);
    }
}
