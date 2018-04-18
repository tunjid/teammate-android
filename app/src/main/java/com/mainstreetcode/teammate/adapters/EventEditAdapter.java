package com.mainstreetcode.teammate.adapters;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.DateViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.GuestViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.SelectionViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.TeamViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.enums.Visibility;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.getItemView;

/**
 * Adapter for {@link com.mainstreetcode.teammate.model.Event  }
 */

public class EventEditAdapter extends BaseRecyclerViewAdapter<BaseViewHolder, EventEditAdapter.EventEditAdapterListener> {

    private static final int GUEST = 12;
    private static final int TEAM = 13;

    private final Event event;

    public EventEditAdapter(Event event, EventEditAdapterListener listener) {
        super(listener);
        setHasStableIds(true);
        this.event = event;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.INPUT:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditEvent);
            case Item.TEXT:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditEvent, () -> false);
            case Item.DATE:
                return new DateViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditEvent);
            case Item.LOCATION:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditEvent, () -> false)
                        .setButtonRunnable(R.drawable.ic_location_on_white_24dp, this::onLocationClicked);
            case Item.VISIBILITY:
                return new SelectionViewHolder<>(getItemView(R.layout.viewholder_simple_input, viewGroup), R.string.event_visibility_selection, Config.getVisibilities(), Visibility::getName, Visibility::getCode, adapterListener::canEditEvent);
            case GUEST:
                return new GuestViewHolder(getItemView(R.layout.viewholder_event_guest, viewGroup), adapterListener);
            case TEAM:
                return new TeamViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), item -> adapterListener.selectTeam());
            default:
                return new BaseItemViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder viewHolder, int i) {
        Object item = event.asIdentifiables().get(i);

        if (item instanceof Item) ((BaseItemViewHolder) viewHolder).bind((Item) item);
        else if (item instanceof Team) ((TeamViewHolder) viewHolder).bind((Team) item);
        else if (item instanceof Guest) ((GuestViewHolder) viewHolder).bind((Guest) item);
    }

    @Override
    public int getItemCount() {
        return event.asIdentifiables().size();
    }

    @Override
    public long getItemId(int position) {
        return event.asIdentifiables().get(position).getId().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        Object thing = event.asIdentifiables().get(position);
        return thing instanceof Item
                ? ((Item) thing).getItemType()
                : thing instanceof Team
                ? TEAM
                : GUEST;
    }

    private void onLocationClicked() {
        if (adapterListener.canEditEvent()) adapterListener.onLocationClicked();
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
