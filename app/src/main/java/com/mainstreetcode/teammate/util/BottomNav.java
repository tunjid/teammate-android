package com.mainstreetcode.teammate.util;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

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

    public static class ViewHolder {
        private boolean hasCustomImage;

        private Item item;
        private View itemView;
        private TextView title;
        private CircleImageView icon;
        private final ImageCallback callback;

        @SuppressLint("ClickableViewAccessibility")
        ViewHolder(View view) {
            itemView = view;
            title = view.findViewById(R.id.title);
            icon = view.findViewById(R.id.icon);
            icon.setDisableCircularTransformation(true);
            callback = new ImageCallback(this);

            GestureDetectorCompat detector = new GestureDetectorCompat(itemView.getContext(), new NavGestureListener(this));
            itemView.setOnTouchListener((v, event) -> onItemViewTouched(detector, v, event));

            tint(R.color.dark_grey);
        }

        public void setImageUrl(String imageUrl) { callback.loadUrl(imageUrl); }

        boolean onItemViewTouched(GestureDetectorCompat detector, View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    v.setPressed(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    v.setPressed(false);
                    break;
            }
            return detector.onTouchEvent(event);
        }

        void tint(@ColorRes int colorRes) {
            int color = ContextCompat.getColor(itemView.getContext(), colorRes);
            title.setTextColor(color);
            icon.setBorderColor(color);
            if (hasCustomImage) return;

            Drawable drawable = icon.getDrawable();
            if (drawable == null) return;

            drawable = drawable.mutate();
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, color);
            DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
            icon.setImageDrawable(drawable);
        }

        void onCustomImageLoaded(boolean succeeded) {
            hasCustomImage = succeeded;
            icon.setDisableCircularTransformation(!succeeded);
            icon.setBorderWidth(succeeded ? itemView.getResources().getDimensionPixelSize(R.dimen.sixteenth_margin) : 0);
            if (!hasCustomImage) bind(item);
        }

        ViewHolder bind(Item item) {
            this.item = item;
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

    private static class ImageCallback implements Callback {
        boolean loaded = false;
        String currentImage;
        final ViewHolder viewHolder;

        private ImageCallback(ViewHolder viewHolder) {this.viewHolder = viewHolder;}

        private void loadUrl(String imageUrl) {
            boolean sameImage = currentImage != null && currentImage.equals(imageUrl);
            if ((sameImage && loaded) || TextUtils.isEmpty(imageUrl)) return;

            currentImage = imageUrl;
            viewHolder.hasCustomImage = true;
            Picasso.with(viewHolder.itemView.getContext())
                    .load(imageUrl).fit().centerCrop().noFade().into(viewHolder.icon, this);
        }

        @Override
        public void onSuccess() { viewHolder.onCustomImageLoaded(loaded = true); }

        @Override
        public void onError() { viewHolder.onCustomImageLoaded(loaded = false); }
    }

    private static class NavGestureListener extends GestureListener {
        final ViewHolder viewHolder;

        private NavGestureListener(ViewHolder viewHolder) {this.viewHolder = viewHolder;}

        @Override
        public boolean onDown(MotionEvent e) { return true; }


        @Override
        public boolean onSwipe(GestureListener.Direction direction) {
            return super.onSwipe(direction);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            viewHolder.itemView.performClick();
            return true;
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
