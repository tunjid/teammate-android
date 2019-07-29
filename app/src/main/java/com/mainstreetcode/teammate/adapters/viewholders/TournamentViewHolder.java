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

import androidx.core.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TournamentAdapter;
import com.mainstreetcode.teammate.model.Tournament;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName;


public class TournamentViewHolder extends ModelCardViewHolder<Tournament, TournamentAdapter.TournamentAdapterListener>
        implements View.OnClickListener {

    public TournamentViewHolder(View itemView, TournamentAdapter.TournamentAdapterListener adapterListener) {
        super(itemView, adapterListener);
        itemView.setOnClickListener(this);
    }

    public void bind(Tournament item) {
        super.bind(item);
        title.setText(item.getName());
        subtitle.setText(item.getDescription());

        ViewCompat.setTransitionName(itemView, getTransitionName(item, R.id.fragment_header_background));
        ViewCompat.setTransitionName(thumbnail, getTransitionName(item, R.id.fragment_header_thumbnail));
    }

    @Override
    public void onClick(View view) {
        adapterListener.onTournamentClicked(model);
    }

    public ImageView getImage() {
        return thumbnail;
    }
}
