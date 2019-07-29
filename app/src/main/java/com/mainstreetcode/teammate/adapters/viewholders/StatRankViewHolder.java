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

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.StatRank;
import com.mainstreetcode.teammate.util.ViewHolderUtil;

public class StatRankViewHolder extends ModelCardViewHolder<StatRank, ViewHolderUtil.SimpleAdapterListener<StatRank>>
        implements View.OnClickListener {

    private TextView count;
    private ImageView inset;

    public StatRankViewHolder(View itemView, ViewHolderUtil.SimpleAdapterListener<StatRank> adapterListener) {
        super(itemView, adapterListener);
        itemView.setOnClickListener(this);
        count = itemView.findViewById(R.id.item_position);
        inset = itemView.findViewById(R.id.inset);
    }

    public void bind(StatRank model) {
        super.bind(model);

        count.setText(model.getCount());
        title.setText(model.getTitle());
        subtitle.setText(model.getSubtitle());

        String imageUrl = model.getInset();

        if (!TextUtils.isEmpty(imageUrl)) load(imageUrl, inset);
    }

    @Override
    public void onClick(View view) {
        adapterListener.onItemClicked(model);
    }

    @Override
    public boolean isThumbnail() { return false; }
}
