package com.mainstreetcode.teammate.adapters.viewholders;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;

public class EmptyViewHolder {

    private final TextView text;
    private final ImageView icon;

    @ColorRes private int color = R.color.white;

    public EmptyViewHolder(View itemView, @DrawableRes int iconRes, @StringRes int stringRes) {
        text = itemView.findViewById(R.id.item_title);
        icon = itemView.findViewById(R.id.icon);

        update(iconRes, stringRes);
    }

    public void toggle(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        icon.setVisibility(visibility);
        text.setVisibility(visibility);
    }

    public void update(@DrawableRes int iconRes, @StringRes int stringRes) {
        text.setText(stringRes);
        icon.setImageDrawable(getIcon(iconRes));
    }

    public void setColor(@ColorRes int color) {
        this.color = color;
        text.setTextColor(getColor());
        icon.setImageDrawable(getDrawable(icon.getDrawable()));
    }

    @Nullable
    private Drawable getIcon(@DrawableRes int iconRes) {
        Context context = icon.getContext();
        Drawable original = VectorDrawableCompat.create(context.getResources(), iconRes, context.getTheme());

        return getDrawable(original);
    }

    @Nullable
    private Drawable getDrawable(@Nullable Drawable original) {
        if (original == null) return null;
        Drawable wrapped = DrawableCompat.wrap(original);
        DrawableCompat.setTint(wrapped, getColor());

        return wrapped;
    }

    private int getColor() {
        return ContextCompat.getColor(text.getContext(), color);
    }
}
