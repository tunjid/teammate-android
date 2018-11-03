package com.mainstreetcode.teammate.adapters;

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
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.enums.Visibility;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveViewHolder;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;

/**
 * Adapter for {@link com.mainstreetcode.teammate.model.Event  }
 */

public class EventEditAdapter extends InteractiveAdapter<InteractiveViewHolder, EventEditAdapter.EventEditAdapterListener> {

    private static final int GUEST = 18;
    private static final int TEAM = 19;

    private final List<Identifiable> identifiables;

    public EventEditAdapter(List<Identifiable> identifiables, EventEditAdapterListener listener) {
        super(listener);
        setHasStableIds(true);
        this.identifiables = identifiables;
    }

    @NonNull
    @Override
    public InteractiveViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.INPUT:
            case Item.TEXT:
            case Item.NUMBER:
            case Item.LOCATION:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup),
                        item -> adapterListener.canEditEvent(),
                        (Function<Item, CharSequence>) this::textChecker)
                        .setButtonRunnable(R.drawable.ic_location_on_white_24dp, this::onLocationClicked);
            case Item.VISIBILITY:
                return new SelectionViewHolder<>(getItemView(R.layout.viewholder_simple_input, viewGroup),
                        R.string.event_visibility_selection,
                        Config.getVisibilities(),
                        Visibility::getName,
                        Visibility::getCode,
                        item -> adapterListener.canEditEvent());
            case Item.DATE:
                return new DateViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), item -> adapterListener.canEditEvent());
            case GUEST:
                return new GuestViewHolder(getItemView(R.layout.viewholder_event_guest, viewGroup), adapterListener);
            case TEAM:
                return new TeamViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), item -> adapterListener.selectTeam());
            default:
                return new BaseItemViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull InteractiveViewHolder viewHolder, int i) {
        Object item = identifiables.get(i);

        if (item instanceof Item) ((BaseItemViewHolder) viewHolder).bind((Item) item);
        else if (item instanceof Team) ((TeamViewHolder) viewHolder).bind((Team) item);
        else if (item instanceof Guest) ((GuestViewHolder) viewHolder).bind((Guest) item);
    }

    @Override
    public int getItemCount() {
        return identifiables.size();
    }

    @Override
    public long getItemId(int position) {
        return identifiables.get(position).getId().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        Object thing = identifiables.get(position);
        return thing instanceof Item
                ? ((Item) thing).getItemType()
                : thing instanceof Team
                ? TEAM
                : GUEST;
    }

    private void onLocationClicked() {
        if (adapterListener.canEditEvent()) adapterListener.onLocationClicked();
    }

    private CharSequence textChecker(Item item) {
        switch (item.getItemType()) {
            default:
            case Item.INPUT:
            case Item.DATE:
                return Item.NON_EMPTY.apply(item);
            case Item.TEXT:
            case Item.NUMBER:
            case Item.LOCATION:
                return Item.ALL_INPUT_VALID.apply(item);
        }
    }

    public interface EventEditAdapterListener extends
            TeamAdapter.AdapterListener,
            ImageWorkerFragment.ImagePickerListener {

        void selectTeam();

        void onLocationClicked();

        void onGuestClicked(Guest guest);

        boolean canEditEvent();
    }

}
