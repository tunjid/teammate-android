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
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.mainstreetcode.teammate.util.ViewHolderUtil;

import java.util.List;

import androidx.annotation.NonNull;

import static com.mainstreetcode.teammate.model.Item.ZIP;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.ITEM;

/**
 * Adapter for {@link Team}
 */

public class TeamEditAdapter extends BaseAdapter<InputViewHolder, TeamEditAdapter.TeamEditAdapterListener> {

    private final List<Differentiable> items;
    private final TextInputStyle.InputChooser chooser;

    public TeamEditAdapter(List<Differentiable> items, TeamEditAdapter.TeamEditAdapterListener listener) {
        super(listener);
        // setHasStableIds(true); DO NOT PUT THIS BACK
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

    public interface TeamEditAdapterListener extends ImageWorkerFragment.ImagePickerListener {

        void onAddressClicked();

        boolean canEditFields();
    }

    private static class Chooser extends TextInputStyle.InputChooser {

        private TeamEditAdapterListener adapterListener;

        Chooser(TeamEditAdapterListener adapterListener) {
            this.adapterListener = adapterListener;
        }

        @Override public int iconGetter(Item item) {
            return item.getStringRes() == R.string.team_name && adapterListener.canEditFields()
                    ? R.drawable.ic_picture_white_24dp : 0;
        }

        @Override public boolean enabler(Item item) {
            switch (item.getItemType()) {
                default:
                case Item.ABOUT:
                    return Item.FALSE.apply(item);
                case Item.ZIP:
                case Item.CITY:
                case Item.INFO:
                case Item.STATE:
                case Item.INPUT:
                case Item.SPORT:
                case Item.NUMBER:
                case Item.DESCRIPTION:
                    return adapterListener.canEditFields();
            }
        }

        @Override public CharSequence textChecker(Item item) {
            switch (item.getItemType()) {
                default:
                case Item.CITY:
                case Item.STATE:
                case Item.INPUT:
                case Item.NUMBER:
                    return Item.NON_EMPTY.apply(item);
                case Item.INFO:
                    return ViewHolderUtil.allowsSpecialCharacters.apply(item.getValue());
                case Item.ZIP:
                case Item.DESCRIPTION:
                    return Item.ALL_INPUT_VALID.apply(item);
            }
        }

        @Override public TextInputStyle apply(Item item) {
            int itemType = item.getItemType();
            switch (itemType) {
                default:
                case Item.ZIP:
                case Item.CITY:
                case Item.INFO:
                case Item.STATE:
                case Item.ABOUT:
                case Item.INPUT:
                case Item.NUMBER:
                case Item.DESCRIPTION:
                    return new TextInputStyle(
                            itemType == Item.CITY || itemType == Item.STATE || itemType == ZIP
                                    ? adapterListener::onAddressClicked
                                    : Item.NO_CLICK,
                            itemType == Item.INPUT
                                    ? adapterListener::onImageClick
                                    : adapterListener::onAddressClicked,
                            this::enabler,
                            this::textChecker,
                            this::iconGetter);
                case Item.SPORT:
                    return new SpinnerTextInputStyle<>(
                            R.string.choose_sport,
                            Config.getSports(),
                            Sport::getName,
                            Sport::getCode,
                            this::enabler);
            }
        }
    }
}
