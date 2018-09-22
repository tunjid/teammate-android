package com.mainstreetcode.teammate.util;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class BottomNav {

    private final LinearLayout container;
    private final ViewHolder[] viewHolders;

    BottomNav(LinearLayout container, View.OnClickListener listener, Item... items) {
        this.container = container;
        LayoutInflater inflater = LayoutInflater.from(container.getContext());

        viewHolders = new ViewHolder[items.length];

        for (int i = 0, itemsLength = items.length; i < itemsLength; i++)
            viewHolders[i] = addViewHolder(new ViewHolder(inflater.inflate(R.layout.bottom_nav_item, container, false)))
                    .bind(items[i])
                    .setOnClickListener(listener);
    }

    public static BottomNavBuilder builder() { return new BottomNavBuilder(); }

    public void highlight(@IdRes int highlighted) {
        for (ViewHolder viewHolder : viewHolders)
            viewHolder.tint(viewHolder.itemView.getId() == highlighted ? R.color.colorPrimary : R.color.dark_grey);
    }

    private ViewHolder addViewHolder(ViewHolder viewHolder) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        params.weight = 1;
        container.addView(viewHolder.itemView, params);
        return viewHolder;
    }

    public static class Item {
        @IdRes private int idRes;
        @StringRes private int titleRes;
        @DrawableRes private int drawableRes;

        private Item(int idRes, int titleRes, int drawableRes) {
            this.idRes = idRes;
            this.titleRes = titleRes;
            this.drawableRes = drawableRes;
        }

        public static Item create(@IdRes int idRes, @StringRes int titleRes, @DrawableRes int drawableRes) {
            return new Item(idRes, titleRes, drawableRes);
        }
    }

    static class ViewHolder {
        private View itemView;
        private TextView title;
        private CircleImageView icon;

        ViewHolder(View view) {
            itemView = view;
            title = view.findViewById(R.id.title);
            icon = view.findViewById(R.id.icon);
            icon.setDisableCircularTransformation(true);
            tint(R.color.dark_grey);
        }

        void tint(@ColorRes int colorRes) {
            int color = ContextCompat.getColor(itemView.getContext(), colorRes);
            title.setTextColor(color);

            Drawable drawable = icon.getDrawable();
            if (drawable == null) return;

            drawable = drawable.mutate();
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, color);
            DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
            icon.setImageDrawable(drawable);
        }

        ViewHolder bind(Item item) {
            itemView.setId(item.idRes);
            title.setText(item.titleRes);
            icon.setImageResource(item.drawableRes);
            return this;
        }

        ViewHolder setOnClickListener(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
            return this;
        }
    }

    public static class BottomNavBuilder {
        private LinearLayout container;
        private View.OnClickListener listener;
        private Item[] items;

        BottomNavBuilder() {}

        public BottomNavBuilder setContainer(LinearLayout container) {
            this.container = container;
            return this;
        }

        public BottomNavBuilder setListener(View.OnClickListener listener) {
            this.listener = listener;
            return this;
        }

        public BottomNavBuilder setItems(Item... items) {
            this.items = items;
            return this;
        }

        public BottomNav createBottomNav() {
            return new BottomNav(container, listener, items);
        }
    }
}
