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
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.SpinnerTextInputStyle;
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle;
import com.mainstreetcode.teammate.baseclasses.BaseAdapter;
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Config;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.enums.Position;

import java.util.List;

import androidx.annotation.NonNull;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.ITEM;

/**
 * Adapter for {@link Role}
 */

public class RoleEditAdapter extends BaseAdapter<InputViewHolder, RoleEditAdapter.RoleEditAdapterListener> {

    private final List<Differentiable> items;
    private final TextInputStyle.InputChooser chooser;

    public RoleEditAdapter(List<Differentiable> items, RoleEditAdapter.RoleEditAdapterListener listener) {
        super(listener);
        this.items = items;
        chooser = new Chooser(adapterListener);
    }

    @NonNull
    @Override
    public InputViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
    }

    @SuppressWarnings("unchecked")
    @Override protected <S extends AdapterListener> S updateListener(BaseViewHolder<S> viewHolder) {
        return (S) adapterListener;
    }

    @Override
    public void onBindViewHolder(@NonNull InputViewHolder viewHolder, int i) {
        super.onBindViewHolder(viewHolder, i);
        Differentiable item = items.get(i);
        if (item instanceof Item) viewHolder.bind(chooser.get((Item) item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return ITEM;
    }

    public interface RoleEditAdapterListener extends ImageWorkerFragment.ImagePickerListener {
        boolean canChangeRolePosition();

        boolean canChangeRoleFields();
    }

    private static class Chooser extends TextInputStyle.InputChooser {

        private final RoleEditAdapterListener adapterListener;

        private Chooser(RoleEditAdapterListener adapterListener) {this.adapterListener = adapterListener;}

        @Override public boolean enabler(Item item) {
            switch (item.getItemType()) {
                default:
                case Item.INPUT:
                case Item.ABOUT:
                    return false;
                case Item.NICKNAME:
                    return adapterListener.canChangeRoleFields();
            }
        }

        @Override public int iconGetter(Item item) {
            switch (item.getItemType()) {
                default:
                case Item.ROLE:
                case Item.ABOUT:
                case Item.NICKNAME:
                    return 0;
                case Item.INPUT:
                    return item.getStringRes() == R.string.first_name && adapterListener.canChangeRolePosition()
                            ? R.drawable.ic_picture_white_24dp : 0;
            }
        }

        @Override public CharSequence textChecker(Item item) {
            switch (item.getItemType()) {
                default:
                case Item.INPUT:
                    return Item.NON_EMPTY.apply(item);
                case Item.ABOUT:
                case Item.NICKNAME:
                    return Item.ALL_INPUT_VALID.apply(item);
            }
        }

        @Override public TextInputStyle apply(Item item) {
            switch (item.getItemType()) {
                default:
                case Item.INPUT:
                case Item.ABOUT:
                case Item.NICKNAME:
                    return new TextInputStyle(
                            Item.NO_CLICK,
                            adapterListener::onImageClick,
                            this::enabler,
                            this::textChecker,
                            this::iconGetter);
                case Item.ROLE:
                    return new SpinnerTextInputStyle<>(
                            R.string.choose_role,
                            Config.getPositions(),
                            Position::getName,
                            Position::getCode,
                            current -> adapterListener.canChangeRolePosition());
            }
        }
    }
}
