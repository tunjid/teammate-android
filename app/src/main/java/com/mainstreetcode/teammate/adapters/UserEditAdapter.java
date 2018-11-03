package com.mainstreetcode.teammate.adapters;

import androidx.arch.core.util.Function;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;

import java.util.List;

import static com.mainstreetcode.teammate.model.Item.ALL_INPUT_VALID;

/**
 * Adapter for {@link User}
 */

public class UserEditAdapter extends InteractiveAdapter<BaseItemViewHolder, UserEditAdapter.AdapterListener> {

    private final List<Identifiable> items;

    public UserEditAdapter(List<Identifiable> items, UserEditAdapter.AdapterListener listener) {
        super(listener);
        this.items = items;
    }

    @NonNull
    @Override
    public BaseItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.viewholder_simple_input, viewGroup, false);

        switch (viewType) {
            case Item.INPUT:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEdit)
                        .setButtonRunnable((Function<Item, Boolean>) this::showsChangePicture, R.drawable.ic_picture_white_24dp, adapterListener::onImageClick);
            case Item.INFO:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEdit, ViewHolderUtil.allowsSpecialCharacters);
            case Item.ABOUT:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEdit, ALL_INPUT_VALID);
            default:
                return new BaseItemViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseItemViewHolder viewHolder, int i) {
        Identifiable item = items.get(i);
        if (item instanceof Item) viewHolder.bind((Item) item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        Identifiable item = items.get(position);
        return item instanceof Item ? ((Item) item).getItemType() : Item.INPUT;
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    private boolean showsChangePicture(Item item) {
        return adapterListener.canEdit() && item.getStringRes() == R.string.first_name;
    }

    public interface AdapterListener extends ImageWorkerFragment.ImagePickerListener {
        boolean canEdit();
    }
}
