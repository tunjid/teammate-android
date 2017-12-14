package com.mainstreetcode.teammates.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.DateViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.EventGuestViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.HeaderedImageViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.MapInputViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.TeamViewHolder;
import com.mainstreetcode.teammates.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Item;
import com.mainstreetcode.teammates.model.User;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.getItemView;

/**
 * Adapter for {@link com.mainstreetcode.teammates.model.Event  }
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class EventEditAdapter extends BaseRecyclerViewAdapter<BaseViewHolder, EventEditAdapter.EditAdapterListener> {

    private static final int GUEST = 12;
    private static final int TEAM = 13;

    private final boolean isEditable;

    private final Event event;
    private final List<User> attendees;
    private final List<User> absentees;

    public EventEditAdapter(Event event, boolean isEditable, EditAdapterListener listener) {
        super(listener);
        this.event = event;
        attendees = event.getAttendees();
        absentees = event.getAbsentees();
        this.isEditable = isEditable;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.INPUT:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), isEditable);
            case Item.DATE:
                return new DateViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
            case Item.IMAGE:
                return new HeaderedImageViewHolder(getItemView(R.layout.viewholder_item_image, viewGroup), adapterListener);
            case Item.LOCATION:
                return new MapInputViewHolder(getItemView(R.layout.viewholder_location_input, viewGroup), adapterListener);
            case GUEST:
                return new EventGuestViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), adapterListener);
            case TEAM:
                return new TeamViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), item -> adapterListener.selectTeam());
            default:
                return new BaseItemViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder viewHolder, int i) {
        List<User> users = new ArrayList<>(attendees);
        users.addAll(event.getAbsentees());

        int guestStartIndex = event.size();
        int guestEndIndex = guestStartIndex + users.size();
        if (i > guestEndIndex) return;
        if (i == guestStartIndex) ((TeamViewHolder) viewHolder).bind(event.getTeam());
        else if (i > guestStartIndex && i <= guestEndIndex)
            ((EventGuestViewHolder) viewHolder).bind(users.get(i - guestStartIndex - 1), attendees.contains(users.get(i - guestStartIndex - 1)));
        else ((BaseItemViewHolder) viewHolder).bind(event.get(i));
    }

    @Override
    public int getItemCount() {
        return event.size() + attendees.size() + absentees.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        int guestStartIndex = event.size();
        int guestEndIndex = guestStartIndex + attendees.size() + absentees.size();
        return position == guestStartIndex
                ? TEAM
                : position <= guestEndIndex && position > guestStartIndex
                ? GUEST
                : event.get(position).getItemType();
    }

    public interface EditAdapterListener extends
            TeamAdapter.TeamAdapterListener,
            ImageWorkerFragment.ImagePickerListener {

        void selectTeam();

        void onLocationClicked();

        void rsvpToEvent(User user);
    }

}
