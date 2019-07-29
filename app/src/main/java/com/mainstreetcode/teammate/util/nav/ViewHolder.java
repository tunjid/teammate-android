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

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import androidx.annotation.AttrRes;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GestureDetectorCompat;
import de.hdodenhof.circleimageview.CircleImageView;

public class ViewHolder {

    private boolean hasCustomImage;

    private NavItem navItem;
    final View itemView;
    private final TextView title;
    private final CircleImageView icon;
    private final ImageCallback callback;
    private final Runnable swipeRunnable;

    @SuppressLint("ClickableViewAccessibility")
    ViewHolder(View view, Runnable swipeRunnable) {
        this.swipeRunnable = swipeRunnable;
        itemView = view;
        title = view.findViewById(R.id.item_title);
        icon = view.findViewById(R.id.thumbnail);
        icon.setDisableCircularTransformation(true);
        callback = new ImageCallback(this);

        GestureDetectorCompat detector = new GestureDetectorCompat(itemView.getContext(), new NavGestureListener(this));
        itemView.setOnTouchListener((v, event) -> onItemViewTouched(detector, v, event));
    }

   public void setImageUrl(String imageUrl) { callback.loadUrl(imageUrl); }

    void click() { itemView.performClick(); }

    void onSwipedUp() { swipeRunnable.run(); }

    private boolean onItemViewTouched(GestureDetectorCompat detector, View v, MotionEvent event) {
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

    void tint(@AttrRes int colorAttr) {
        int color = ViewHolderUtil.resolveThemeColor(itemView.getContext(), colorAttr);
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

    ViewHolder bind(NavItem navItem) {
        this.navItem = navItem;
        itemView.setId(navItem.idRes);
        title.setText(navItem.titleRes);
        icon.setImageResource(navItem.drawableRes);
        tint(R.attr.bottom_nav_unselected);
        return this;
    }

    ViewHolder setOnClickListener(View.OnClickListener listener) {
        itemView.setOnClickListener(listener);
        return this;
    }

    private void onCustomImageLoaded(boolean succeeded) {
        hasCustomImage = succeeded;
        icon.setDisableCircularTransformation(!succeeded);
        icon.setBorderWidth(succeeded ? itemView.getResources().getDimensionPixelSize(R.dimen.sixteenth_margin) : 0);
        if (!hasCustomImage) bind(navItem);
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
            Picasso.get().load(imageUrl).fit().centerCrop().noFade().into(viewHolder.icon, this);
        }

        @Override
        public void onSuccess() { viewHolder.onCustomImageLoaded(loaded = true); }

        @Override
        public void onError(Exception e) { viewHolder.onCustomImageLoaded(loaded = false); }
    }
}
