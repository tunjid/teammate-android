package com.mainstreetcode.teammates.adapters.viewholders;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;

public class EmptyViewHolder {

    private final TextView text;
    private final ImageView icon;

    public EmptyViewHolder(View itemView, @DrawableRes int iconRes, @StringRes int stringRes) {
        text = itemView.findViewById(R.id.item_title);
        icon = itemView.findViewById(R.id.icon);

        text.setText(stringRes);
        icon.setImageDrawable(getIcon(iconRes));
    }

    public void toggle(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        icon.setVisibility(visibility);
        text.setVisibility(visibility);
    }

    @Nullable
    private Drawable getIcon(@DrawableRes int iconRes) {
        Context context = icon.getContext();
        Drawable original = VectorDrawableCompat.create(context.getResources(), iconRes, context.getTheme());

        if (original == null) return null;
        Drawable wrapped = DrawableCompat.wrap(original);
        DrawableCompat.setTint(wrapped, ContextCompat.getColor(context, R.color.white));

        return wrapped;
    }
}
