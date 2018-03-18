package com.mainstreetcode.teammate.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.DateViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.GuestViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.TeamViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.Team;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.getItemView;

/**
 * Adapter for {@link com.mainstreetcode.teammate.model.Event  }
 */

public class EventEditAdapter extends BaseRecyclerViewAdapter<BaseViewHolder, EventEditAdapter.EventEditAdapterListener> {

    private static final int GUEST = 12;
    private static final int TEAM = 13;

    private final List<Identifiable> items;

    public EventEditAdapter(List<Identifiable> items, EventEditAdapterListener listener) {
        super(listener);
        setHasStableIds(true);
        this.items = items;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.INPUT:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditEvent);
            case Item.TEXT:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditEvent, () -> false);
            case Item.DATE:
                return new DateViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditEvent);
            case Item.LOCATION:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditEvent);
            case GUEST:
                return new GuestViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), adapterListener);
            case TEAM:
                return new TeamViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), item -> adapterListener.selectTeam());
            default:
                return new BaseItemViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder viewHolder, int i) {
        Object item = items.get(i);

        if (item instanceof Item) ((BaseItemViewHolder) viewHolder).bind((Item) item);
        else if (item instanceof Team) ((TeamViewHolder) viewHolder).bind((Team) item);
        else if (item instanceof Guest) ((GuestViewHolder) viewHolder).bind((Guest) item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        Object thing = items.get(position);
        return thing instanceof Item
                ? ((Item) thing).getItemType()
                : thing instanceof Team
                ? TEAM
                : GUEST;
    }

    public interface EventEditAdapterListener extends
            TeamAdapter.TeamAdapterListener,
            ImageWorkerFragment.ImagePickerListener {

        void selectTeam();

        void onLocationClicked();

        void onGuestClicked(Guest guest);

        boolean canEditEvent();
    }

}
