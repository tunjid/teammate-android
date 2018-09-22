package com.mainstreetcode.teammate.util.nav;

import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.mainstreetcode.teammate.R;

public class BottomNav {

    private final LinearLayout container;
    private final ViewHolder[] viewHolders;

    BottomNav(LinearLayout container,
              Runnable onSwipe,
              View.OnClickListener onClick,
              NavItem... navItems) {
        this.container = container;
        LayoutInflater inflater = LayoutInflater.from(container.getContext());

        int min = Math.min(navItems.length, 5);
        viewHolders = new ViewHolder[min];

        for (int i = 0; i < min; i++) {
            View itemView = inflater.inflate(R.layout.bottom_nav_item, container, false);
            viewHolders[i] = addViewHolder(new ViewHolder(itemView, onSwipe))
                    .bind(navItems[i])
                    .setOnClickListener(onClick);
        }
    }

    public static Builder builder() { return new Builder(); }

    public void highlight(@IdRes int highlighted) {
        for (ViewHolder viewHolder : viewHolders)
            viewHolder.tint(viewHolder.itemView.getId() == highlighted ? R.color.colorPrimary : R.color.dark_grey);
    }

    @Nullable
    public ViewHolder getViewHolder(@IdRes int id) {
        for (ViewHolder holder : viewHolders) if (holder.itemView.getId() == id) return holder;
        return null;
    }

    private ViewHolder addViewHolder(ViewHolder viewHolder) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        params.weight = 1;
        container.addView(viewHolder.itemView, params);
        return viewHolder;
    }

    public static class Builder {
        private LinearLayout container;
        private View.OnClickListener listener;
        private NavItem[] navItems;
        private Runnable onSwipe;

        Builder() {}

        public Builder setContainer(LinearLayout container) {
            this.container = container;
            return this;
        }

        public Builder setSwipeRunnable(Runnable onSwipe) {
            this.onSwipe = onSwipe;
            return this;
        }

        public Builder setListener(View.OnClickListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder setNavItems(NavItem... navItems) {
            this.navItems = navItems;
            return this;
        }

        public BottomNav createBottomNav() {
            return new BottomNav(container, onSwipe, listener, navItems);
        }
    }
}
