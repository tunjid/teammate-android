/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.util.nav;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.mainstreetcode.teammate.R;

public class BottomNav {

    private final LinearLayout container;
    private final ViewHolder[] viewHolders;

    private BottomNav(LinearLayout container,
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
            viewHolder.tint(viewHolder.itemView.getId() == highlighted ? R.attr.bottom_nav_selected : R.attr.bottom_nav_unselected);
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
