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
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.model.enums.StatType;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;


public class StatViewHolder extends InteractiveViewHolder<ViewHolderUtil.SimpleAdapterListener<Stat>> {

    protected Stat model;

    private TextView[] statTime;
    private TextView[] statUser;
    private TextView[] statName;
    private TextView[] statEmoji;

    public StatViewHolder(View itemView, ViewHolderUtil.SimpleAdapterListener<Stat> adapterListener) {
        super(itemView, adapterListener);
        statTime = new TextView[]{itemView.findViewById(R.id.home_stat_time), itemView.findViewById(R.id.away_stat_time)};
        statUser = new TextView[]{itemView.findViewById(R.id.home_stat_user), itemView.findViewById(R.id.away_stat_user)};
        statName = new TextView[]{itemView.findViewById(R.id.home_stat_name), itemView.findViewById(R.id.away_stat_name)};
        statEmoji = new TextView[]{itemView.findViewById(R.id.home_stat_emoji), itemView.findViewById(R.id.away_stat_emoji)};

        itemView.setOnClickListener(view -> adapterListener.onItemClicked(model));
    }

    public void bind(Stat model) {
        this.model = model;
        StatType statType = model.getStatType();

        setText(statTime, String.valueOf(model.getTime()));
        setText(statUser, model.getUser().getFirstName());
        setText(statName, statType.getName());
        setText(statEmoji, statType.getEmoji());
    }

    private void setText(TextView[] textViewPair, CharSequence text) {
        boolean isHome = model.isHome();
        int home = isHome ? 0 : 1;
        int away = isHome ? 1: 0;

        textViewPair[home].setText(text);
        textViewPair[away].setText("");
    }
}
