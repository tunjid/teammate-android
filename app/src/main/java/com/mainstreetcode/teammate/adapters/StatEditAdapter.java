package com.mainstreetcode.teammate.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.TeamViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.UserViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.BaseItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.SpinnerTextInputStyle;
import com.mainstreetcode.teammate.adapters.viewholders.input.StatAttributeViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.model.enums.StatType;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveViewHolder;

import java.util.List;

import androidx.annotation.NonNull;

import static com.mainstreetcode.teammate.model.Item.ALL_INPUT_VALID;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.TEAM;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.USER;

/**
 * Adapter for {@link com.mainstreetcode.teammate.model.Tournament}
 */

public class StatEditAdapter extends InteractiveAdapter<InteractiveViewHolder, StatEditAdapter.AdapterListener> {

    private final List<Identifiable> items;
    private final TextInputStyle.InputChooser chooser;

    public StatEditAdapter(List<Identifiable> items, AdapterListener listener) {
        super(listener);
        this.items = items;
        chooser = new Chooser(adapterListener);
    }

    @NonNull
    @Override
    public InteractiveViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.INPUT:
            case Item.NUMBER:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
            case Item.STAT_TYPE:
                return new StatAttributeViewHolder(getItemView(R.layout.viewholder_stat_type, viewGroup),
                        adapterListener.getStat());
            case USER:
                return new UserViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), user -> adapterListener.onUserClicked());
            case TEAM:
                return new TeamViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), team -> adapterListener.onTeamClicked());
            default:
                return new BaseItemViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull InteractiveViewHolder viewHolder, int i) {
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
        return object instanceof Item ? ((Item) object).getItemType() == Item.STAT_TYPE
                ? Item.STAT_TYPE
                : Item.INPUT
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
                case Item.STAT_TYPE:
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
