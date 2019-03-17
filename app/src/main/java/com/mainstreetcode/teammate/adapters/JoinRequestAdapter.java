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
import com.mainstreetcode.teammate.model.enums.Sport;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;

import java.util.List;

import androidx.annotation.NonNull;

import static com.mainstreetcode.teammate.model.Item.ALL_INPUT_VALID;
import static com.mainstreetcode.teammate.model.Item.FALSE;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.ITEM;

/**
 * Adapter for {@link Role}
 */

public class JoinRequestAdapter extends BaseAdapter<InputViewHolder, JoinRequestAdapter.AdapterListener> {

    private final List<Differentiable> items;
    private final TextInputStyle.InputChooser chooser;

    public JoinRequestAdapter(List<Differentiable> items, AdapterListener listener) {
        super(listener);
        this.items = items;
        chooser = new Chooser(adapterListener);
    }

    @NonNull
    @Override
    public InputViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <S extends InteractiveAdapter.AdapterListener> S updateListener(BaseViewHolder<S> viewHolder) {
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

    public interface AdapterListener extends ImageWorkerFragment.ImagePickerListener {
        boolean canEditFields();

        boolean canEditRole();
    }

    private static class Chooser extends TextInputStyle.InputChooser {

        private final AdapterListener adapterListener;

        private Chooser(AdapterListener adapterListener) {this.adapterListener = adapterListener;}

        @Override public TextInputStyle apply(Item item) {
            int itemType = item.getItemType();
            switch (itemType) {
                default:
                    return new TextInputStyle(
                            Item.NO_CLICK,
                            Item.NO_CLICK,
                            itemType == Item.INPUT
                                    ? ignored -> adapterListener.canEditFields()
                                    : Item.FALSE,
                            Item.ALL_INPUT_VALID,
                            Item.NO_ICON);
                case Item.SPORT:
                    return new SpinnerTextInputStyle<>(
                            R.string.choose_sport,
                            Config.getSports(),
                            Sport::getName,
                            Sport::getCode,
                            FALSE,
                            ALL_INPUT_VALID);
                case Item.ROLE:
                    return new SpinnerTextInputStyle<>(
                            R.string.choose_role,
                            Config.getPositions(),
                            Position::getName,
                            Position::getCode,
                            ignored -> adapterListener.canEditRole(),
                            ALL_INPUT_VALID);
            }
        }
    }
}
