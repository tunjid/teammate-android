package com.mainstreetcode.teammate.adapters.viewholders;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.util.ViewHolderUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

public class EmptyViewHolder {

    private final TextView text;
    private final ImageView icon;

    @ColorInt
    private int color;

    @IntDef({R.attr.empty_view_holder_tint, R.attr.alt_empty_view_holder_tint})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EmptyTint {}

    public EmptyViewHolder(View itemView, @DrawableRes int iconRes, @StringRes int stringRes) {
        text = itemView.findViewById(R.id.item_title);
        icon = itemView.findViewById(R.id.icon);
        color = ViewHolderUtil.resolveThemeColor(itemView.getContext(), R.attr.empty_view_holder_tint);

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

    public void setColor(@EmptyTint @AttrRes int attrRes) {
        this.color = ViewHolderUtil.resolveThemeColor(icon.getContext(), attrRes);
        text.setTextColor(color);
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
        Drawable mutated = original;
        if (color != R.color.white) mutated = original.mutate();

        Drawable wrapped = DrawableCompat.wrap(mutated);
        DrawableCompat.setTint(wrapped, color);

        return wrapped;
    }
}
