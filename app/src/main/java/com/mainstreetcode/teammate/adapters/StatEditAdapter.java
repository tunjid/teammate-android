package com.mainstreetcode.teammate.adapters;

import androidx.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.StatAttributeViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.TeamViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.UserViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveViewHolder;

import java.util.List;

import static com.mainstreetcode.teammate.model.Item.ALL_INPUT_VALID;
import static com.mainstreetcode.teammate.model.Item.TRUE;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.TEAM;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.USER;

/**
 * Adapter for {@link com.mainstreetcode.teammate.model.Tournament}
 */

public class StatEditAdapter extends InteractiveAdapter<InteractiveViewHolder, StatEditAdapter.AdapterListener> {

    private final List<Identifiable> items;

    public StatEditAdapter(List<Identifiable> items, AdapterListener listener) {
        super(listener);
        this.items = items;
    }

    @NonNull
    @Override
    public InteractiveViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.INPUT:
            case Item.NUMBER:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), TRUE);
            case Item.STAT_TYPE:
                return new StatAttributeViewHolder(getItemView(R.layout.viewholder_stat_type, viewGroup), R.string.choose_stat, adapterListener.getStat(), adapterListener::canChangeStat, ALL_INPUT_VALID);
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

        if (item instanceof Item) ((BaseItemViewHolder) viewHolder).bind((Item) item);
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
        return object instanceof Item ? ((Item) object).getItemType()
                : object instanceof User
                ? USER : TEAM;
    }


    public interface AdapterListener extends ImageWorkerFragment.ImagePickerListener {
        void onUserClicked();

        void onTeamClicked();

        boolean canChangeStat();

        Stat getStat();
    }
}
