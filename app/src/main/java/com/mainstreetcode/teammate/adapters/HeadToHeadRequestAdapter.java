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

package com.mainstreetcode.teammate.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.CompetitorViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.DateTextInputStyle;
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.SpinnerTextInputStyle;
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle;
import com.mainstreetcode.teammate.baseclasses.BaseAdapter;
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.HeadToHead;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.mainstreetcode.teammate.model.enums.TournamentType;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

import static com.mainstreetcode.teammate.model.Item.ALL_INPUT_VALID;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.AWAY;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.HOME;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.ITEM;

/**
 * Adapter for {@link Event}
 */

public class HeadToHeadRequestAdapter extends BaseAdapter<BaseViewHolder, HeadToHeadRequestAdapter.AdapterListener> {

    private final HeadToHead.Request request;
    private final Chooser chooser;

    public HeadToHeadRequestAdapter(HeadToHead.Request request,
                                    AdapterListener listener) {
        super(listener);
        this.request = request;
        chooser = new Chooser();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            default:
            case ITEM:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
            case HOME:
                return new CompetitorViewHolder(getItemView(R.layout.viewholder_competitor, viewGroup), adapterListener::onHomeClicked)
                        .hideSubtitle().withTitle(R.string.pick_home_competitor);
            case AWAY:
                return new CompetitorViewHolder(getItemView(R.layout.viewholder_competitor, viewGroup), adapterListener::onAwayClicked)
                        .hideSubtitle().withTitle(R.string.pick_away_competitor);
        }
    }

    @SuppressWarnings("unchecked")
    @Override protected <S extends InteractiveAdapter.AdapterListener> S updateListener(BaseViewHolder<S> viewHolder) {
        if (viewHolder.getItemViewType() == HOME)
            return (S) ((CompetitorAdapter.AdapterListener) adapterListener::onHomeClicked);
        if (viewHolder.getItemViewType() == AWAY)
            return (S) ((CompetitorAdapter.AdapterListener) adapterListener::onAwayClicked);
        return (S) adapterListener;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull BaseViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);
        Differentiable identifiable = request.getItems().get(position);
        if (identifiable instanceof Item)
            ((InputViewHolder) viewHolder).bind(chooser.get((Item) identifiable));
        else if (identifiable instanceof Competitor)
            ((CompetitorViewHolder) viewHolder).bind((Competitor) identifiable);
    }

    @Override
    public int getItemCount() {
        return request.getItems().size();
    }

    @Override
    public int getItemViewType(int position) {
        Differentiable identifiable = request.getItems().get(position);
        return identifiable instanceof Item ? ITEM : position == 2 ? HOME : AWAY;
    }

    public interface AdapterListener extends InteractiveAdapter.AdapterListener {
        void onHomeClicked(Competitor home);

        void onAwayClicked(Competitor away);
    }

    private static class Chooser extends TextInputStyle.InputChooser {

        private final List<Sport> sports;

        Chooser() {
            sports = new ArrayList<>(Config.getSports());
            sports.add(0, Sport.empty());
        }

        @Override public TextInputStyle apply(Item item) {
            int itemType = item.getItemType();
            switch (itemType) {
                default:
                case Item.SPORT:
                    return new SpinnerTextInputStyle<>(
                            R.string.choose_sport,
                            sports,
                            Sport::getName,
                            Sport::getCode,
                            Item.TRUE,
                            ALL_INPUT_VALID);
                case Item.DATE:
                    return new DateTextInputStyle(Item.TRUE);
                case Item.TOURNAMENT_TYPE:
                    return new SpinnerTextInputStyle<>(
                            R.string.tournament_type,
                            Config.getTournamentTypes(type -> true),
                            TournamentType::getName,
                            TournamentType::getCode,
                            Item.TRUE,
                            ALL_INPUT_VALID);
            }
        }
    }
}
