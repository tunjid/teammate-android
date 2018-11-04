package com.mainstreetcode.teammate.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.GuestViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.TeamViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.BaseItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.DateTextInputStyle;
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.SpinnerTextInputStyle;
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle;
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

/**
 * Adapter for {@link com.mainstreetcode.teammate.model.Event  }
 */

public class EventEditAdapter extends InteractiveAdapter<InteractiveViewHolder, EventEditAdapter.EventEditAdapterListener> {

    private static final int GUEST = 18;
    private static final int TEAM = 19;

    private final List<Identifiable> identifiables;
    private final TextInputStyle.InputChooser chooser = new Chooser(adapterListener);

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
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
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

        if (item instanceof Item) ((InputViewHolder) viewHolder).bind(chooser.get((Item) item));
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
                ? Item.INPUT
                : thing instanceof Team
                ? TEAM
                : GUEST;
    }

    public interface EventEditAdapterListener extends
            TeamAdapter.AdapterListener,
            ImageWorkerFragment.ImagePickerListener {

        void selectTeam();

        void onLocationClicked();

        void onGuestClicked(Guest guest);

        boolean canEditEvent();
    }

    private static class Chooser extends TextInputStyle.InputChooser {

        private final EventEditAdapterListener adapterListener;

        private Chooser(EventEditAdapterListener adapterListener) {this.adapterListener = adapterListener;}

        @Override public int iconGetter(Item item) {
            return item.getItemType() == Item.LOCATION ? R.drawable.ic_location_on_white_24dp : 0;
        }

        @Override public boolean enabler(Item item) { return adapterListener.canEditEvent(); }

        @Override public CharSequence textChecker(Item item) {
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

        public TextInputStyle apply(Item item) {
            int itemType = item.getItemType();
            switch (itemType) {
                default:
                case Item.INPUT:
                case Item.TEXT:
                case Item.NUMBER:
                case Item.LOCATION:
                    return new TextInputStyle(
                            Item.NO_CLICK,
                            adapterListener::onLocationClicked,
                            input -> adapterListener.canEditEvent(),
                            this::textChecker,
                            this::iconGetter);
                case Item.VISIBILITY:
                    return new SpinnerTextInputStyle<>(
                            R.string.event_visibility_selection,
                            Config.getVisibilities(),
                            Visibility::getName,
                            Visibility::getCode,
                            current -> adapterListener.canEditEvent());
                case Item.DATE:
                    return new DateTextInputStyle(input -> adapterListener.canEditEvent());
            }
        }
    }
}
