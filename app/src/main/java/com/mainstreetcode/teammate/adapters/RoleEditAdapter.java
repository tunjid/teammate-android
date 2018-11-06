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
import com.mainstreetcode.teammate.model.Identifiable;
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

    private final List<Identifiable> items;
    private final TextInputStyle.InputChooser chooser;

    public RoleEditAdapter(List<Identifiable> items, RoleEditAdapter.RoleEditAdapterListener listener) {
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
        super.bindViewHolder(viewHolder, i);
        Identifiable item = items.get(i);
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

        private boolean showsChangePicture(Item item) {
            return item.getStringRes() == R.string.first_name && adapterListener.canChangeRoleFields();
        }

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
                case Item.INPUT:
                case Item.ABOUT:
                case Item.NICKNAME:
                    return 0;
                case Item.ROLE:
                    return showsChangePicture(item) ? R.drawable.ic_picture_white_24dp : 0;
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
                            item.getStringRes() == R.string.first_name
                                    ? adapterListener::onImageClick
                                    : Item.NO_CLICK,
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
