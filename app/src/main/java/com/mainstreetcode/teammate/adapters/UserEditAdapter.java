package com.mainstreetcode.teammate.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle;
import com.mainstreetcode.teammate.baseclasses.BaseAdapter;
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;

import java.util.List;

import androidx.annotation.NonNull;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.ITEM;

/**
 * Adapter for {@link User}
 */

public class UserEditAdapter extends BaseAdapter<InputViewHolder, UserEditAdapter.AdapterListener> {

    private final List<Identifiable> items;
    private final TextInputStyle.InputChooser chooser;

    public UserEditAdapter(List<Identifiable> items, UserEditAdapter.AdapterListener listener) {
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

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    public interface AdapterListener extends ImageWorkerFragment.ImagePickerListener {
        boolean canEdit();
    }

    private static class Chooser extends TextInputStyle.InputChooser {

        private AdapterListener adapterListener;

        Chooser(AdapterListener adapterListener) {
            this.adapterListener = adapterListener;
        }

        @Override public int iconGetter(Item item) {
            return adapterListener.canEdit() && item.getStringRes() == R.string.first_name ? R.drawable.ic_picture_white_24dp : 0;
        }

        @Override public CharSequence textChecker(Item item) {
            switch (item.getItemType()) {
                default:
                case Item.INPUT:
                    return Item.NON_EMPTY.apply(item);
                case Item.INFO:
                    return ViewHolderUtil.allowsSpecialCharacters.apply(item.getValue());
                case Item.ABOUT:
                    return Item.ALL_INPUT_VALID.apply(item);
            }
        }

        @Override public TextInputStyle apply(Item item) {
            switch (item.getItemType()) {
                default:
                case Item.INFO:
                case Item.INPUT:
                case Item.ABOUT:
                    return new TextInputStyle(
                            Item.NO_CLICK,
                            adapterListener::onImageClick,
                            input -> adapterListener.canEdit(),
                            this::textChecker,
                            this::iconGetter);
            }
        }
    }
}
