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

package com.mainstreetcode.teammate.adapters.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Ad;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.squareup.picasso.Picasso;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import static android.text.TextUtils.isEmpty;

/**
 * Viewholder for a {@link Team}
 */
public abstract class AdViewHolder<T extends Ad> extends InteractiveViewHolder<InteractiveAdapter.AdapterListener> {

    T ad;

    TextView title;
    TextView subtitle;
    ImageView thumbnail;

    AdViewHolder(View itemView, InteractiveAdapter.AdapterListener adapterListener) {
        super(itemView, adapterListener);
        title = itemView.findViewById(R.id.item_title);
        subtitle = itemView.findViewById(R.id.item_subtitle);
        thumbnail = itemView.findViewById(R.id.thumbnail);
    }

    @Nullable
    abstract String getImageUrl();

    public void bind(T ad) {
        this.ad = ad;
        setImageAspectRatio(ad);
        ViewHolderUtil.listenForLayout(thumbnail, () -> {
            String imageUrl = getImageUrl();
            if (!isEmpty(imageUrl)) Picasso.get().load(imageUrl).fit().centerCrop().into(thumbnail);
        });
    }

    private void setImageAspectRatio(Ad ad) {
        String aspectRatio = ad.getImageAspectRatio();
        if (aspectRatio != null) {
            ((ConstraintLayout.LayoutParams) thumbnail.getLayoutParams()).dimensionRatio = aspectRatio;
        }
    }
}
