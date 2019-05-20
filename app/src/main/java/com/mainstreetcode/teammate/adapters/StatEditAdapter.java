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
import com.mainstreetcode.teammate.adapters.viewholders.TeamViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.UserViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.SpinnerTextInputStyle;
import com.mainstreetcode.teammate.adapters.viewholders.input.StatAttributeViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle;
import com.mainstreetcode.teammate.baseclasses.BaseAdapter;
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.model.enums.StatType;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;

import java.util.List;

import androidx.annotation.NonNull;

import static com.mainstreetcode.teammate.model.Item.ALL_INPUT_VALID;
import static com.mainstreetcode.teammate.model.Item.STAT_TYPE;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.ITEM;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.TEAM;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.USER;

/**
 * Adapter for {@link com.mainstreetcode.teammate.model.Tournament}
 */

public class StatEditAdapter extends BaseAdapter<BaseViewHolder, StatEditAdapter.AdapterListener> {

    private final List<Differentiable> items;
    private final TextInputStyle.InputChooser chooser;
    private final UserAdapter.AdapterListener userListener = user -> adapterListener.onUserClicked();
    private final TeamAdapter.AdapterListener teamListener = team -> adapterListener.onTeamClicked();

    public StatEditAdapter(List<Differentiable> items, AdapterListener listener) {
        super(listener);
        this.items = items;
        chooser = new Chooser(adapterListener);
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            default:
            case ITEM:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
            case STAT_TYPE:
                return new StatAttributeViewHolder(getItemView(R.layout.viewholder_stat_type, viewGroup),
                        adapterListener.getStat());
            case USER:
                return new UserViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), userListener);
            case TEAM:
                return new TeamViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), teamListener);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <S extends InteractiveAdapter.AdapterListener> S updateListener(BaseViewHolder<S> viewHolder) {
        if (viewHolder.getItemViewType() == USER) return (S) userListener;
        if (viewHolder.getItemViewType() == TEAM) return (S) teamListener;

        return (S) adapterListener;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder viewHolder, int i) {
        super.onBindViewHolder(viewHolder, i);
        Object item = items.get(i);

        if (item instanceof Item) ((InputViewHolder) viewHolder).bind(chooser.get((Item) item));
        else if (item instanceof User) ((UserViewHolder) viewHolder).bind((User) item);
        else if (item instanceof Team) ((TeamViewHolder) viewHolder).bind((Team) item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object object = items.get(position);
        return object instanceof Item ? ((Item) object).getItemType() == STAT_TYPE
                ? STAT_TYPE
                : ITEM
                : object instanceof User
                ? USER : TEAM;
    }

    public interface AdapterListener extends ImageWorkerFragment.ImagePickerListener {
        void onUserClicked();

        void onTeamClicked();

        boolean canChangeStat();

        Stat getStat();
    }

    private static class Chooser extends TextInputStyle.InputChooser {

        private AdapterListener adapterListener;

        Chooser(AdapterListener adapterListener) {
            this.adapterListener = adapterListener;
        }

        @Override public TextInputStyle apply(Item item) {
            int itemType = item.getItemType();
            switch (itemType) {
                default:
                case Item.INPUT:
                case Item.NUMBER:
                    return new TextInputStyle(
                            Item.NO_CLICK,
                            Item.NO_CLICK,
                            Item.TRUE,
                            Item.NON_EMPTY,
                            Item.NO_ICON);
                case STAT_TYPE:
                    Stat stat = adapterListener.getStat();
                    return new SpinnerTextInputStyle<>(
                            R.string.choose_stat,
                            stat.getSport().getStats(),
                            StatType::getEmojiAndName,
                            StatType::getCode,
                            ignored -> adapterListener.canChangeStat(),
                            ALL_INPUT_VALID);
            }
        }
    }
}
