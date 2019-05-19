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
import com.mainstreetcode.teammate.adapters.viewholders.GuestViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.TeamViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.DateTextInputStyle;
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.SpinnerTextInputStyle;
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle;
import com.mainstreetcode.teammate.baseclasses.BaseAdapter;
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Guest;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.enums.Visibility;

import java.util.List;

import androidx.annotation.NonNull;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.GUEST;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.ITEM;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.TEAM;

/**
 * Adapter for {@link com.mainstreetcode.teammate.model.Event  }
 */

public class EventEditAdapter extends BaseAdapter<BaseViewHolder, EventEditAdapter.EventEditAdapterListener> {

    private final List<Differentiable> identifiables;
    private final TextInputStyle.InputChooser chooser = new Chooser(adapterListener);
    private final TeamAdapter.AdapterListener teamListener = item -> adapterListener.selectTeam();

    public EventEditAdapter(List<Differentiable> identifiables, EventEditAdapterListener listener) {
        super(listener);
        setHasStableIds(true);
        this.identifiables = identifiables;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            default:
            case ITEM:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
            case GUEST:
                return new GuestViewHolder(getItemView(R.layout.viewholder_event_guest, viewGroup), adapterListener);
            case TEAM:
                return new TeamViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), teamListener);
        }
    }

    @SuppressWarnings("unchecked")
    @Override protected <S extends AdapterListener> S updateListener(BaseViewHolder<S> viewHolder) {
        if (viewHolder instanceof TeamViewHolder) return (S) teamListener;
        return (S) adapterListener;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder viewHolder, int i) {
        super.onBindViewHolder(viewHolder, i);
        Object item = identifiables.get(i);

        if (item instanceof Item) ((InputViewHolder) viewHolder).bind(chooser.get((Item) item));
        else if (item instanceof Team) ((TeamViewHolder) viewHolder).bind((Team) item);
        else if (item instanceof Guest) ((GuestViewHolder) viewHolder).bind((Guest) item);
    }

    @Override
    public int getItemCount() {
        return identifiables.size();
    }

    @Override
    public long getItemId(int position) {
        return identifiables.get(position).getId().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        Object thing = identifiables.get(position);
        return thing instanceof Item
                ? ITEM
                : thing instanceof Team
                ? TEAM
                : GUEST;
    }

    public interface EventEditAdapterListener extends
            TeamAdapter.AdapterListener,
            ImageWorkerFragment.ImagePickerListener {

        void selectTeam();

        void onLocationClicked();

        void onGuestClicked(Guest guest);

        boolean canEditEvent();
    }

    private static class Chooser extends TextInputStyle.InputChooser {

        private final EventEditAdapterListener adapterListener;

        private Chooser(EventEditAdapterListener adapterListener) {this.adapterListener = adapterListener;}

        @Override public int iconGetter(Item item) {
            return item.getItemType() == Item.LOCATION ? R.drawable.ic_location_on_white_24dp : 0;
        }

        @Override public boolean enabler(Item item) { return adapterListener.canEditEvent(); }

        @Override public CharSequence textChecker(Item item) {
            switch (item.getItemType()) {
                default:
                case Item.INPUT:
                case Item.DATE:
                    return Item.NON_EMPTY.apply(item);
                case Item.TEXT:
                case Item.NUMBER:
                case Item.LOCATION:
                    return Item.ALL_INPUT_VALID.apply(item);
            }
        }

        public TextInputStyle apply(Item item) {
            int itemType = item.getItemType();
            switch (itemType) {
                default:
                case Item.INPUT:
                case Item.TEXT:
                case Item.NUMBER:
                case Item.LOCATION:
                    return new TextInputStyle(
                            Item.NO_CLICK,
                            adapterListener::onLocationClicked,
                            input -> adapterListener.canEditEvent(),
                            this::textChecker,
                            this::iconGetter);
                case Item.VISIBILITY:
                    return new SpinnerTextInputStyle<>(
                            R.string.event_visibility_selection,
                            Config.getVisibilities(),
                            Visibility::getName,
                            Visibility::getCode,
                            current -> adapterListener.canEditEvent());
                case Item.DATE:
                    return new DateTextInputStyle(input -> adapterListener.canEditEvent());
            }
        }
    }
}
