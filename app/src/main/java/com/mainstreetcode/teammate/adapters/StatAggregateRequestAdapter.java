package com.mainstreetcode.teammate.adapters;

import androidx.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.DateViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.SelectionViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.TeamViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.UserViewHolder;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.StatAggregate;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveViewHolder;

import java.util.ArrayList;
import java.util.List;

import static com.mainstreetcode.teammate.model.Item.ALL_INPUT_VALID;
import static com.mainstreetcode.teammate.model.Item.TRUE;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.TEAM;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.USER;

/**
 * Adapter for {@link Event}
 */

public class StatAggregateRequestAdapter extends InteractiveAdapter<InteractiveViewHolder, StatAggregateRequestAdapter.AdapterListener> {

    private final StatAggregate.Request request;
    private final List<Sport> sports;

    public StatAggregateRequestAdapter(StatAggregate.Request request,
                                       AdapterListener listener) {
        super(listener);
        this.request = request;
        sports = new ArrayList<>(Config.getSports());
        sports.add(0, Sport.empty());
    }

    @NonNull
    @Override
    public InteractiveViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.SPORT:
                return new SelectionViewHolder<>(getItemView(R.layout.viewholder_simple_input, viewGroup), R.string.choose_sport, sports, Sport::getName, Sport::getCode, TRUE, ALL_INPUT_VALID);
            case Item.DATE:
                return new DateViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), TRUE);
            case USER:
                return new UserViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), adapterListener::onUserPicked)
                        .withTitle(R.string.pick_user);
            case TEAM:
                return new TeamViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), adapterListener::onTeamPicked)
                        .withTitle(R.string.pick_team);
            default:
                return new BaseItemViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull InteractiveViewHolder viewHolder, int position) {
        Identifiable identifiable = request.getItems().get(position);
        if (identifiable instanceof Item)
            ((BaseItemViewHolder) viewHolder).bind((Item) identifiable);
        else if (identifiable instanceof User)
            ((UserViewHolder) viewHolder).bind((User) identifiable);
        else if (identifiable instanceof Team)
            ((TeamViewHolder) viewHolder).bind((Team) identifiable);
    }

    @Override
    public int getItemCount() {
        return request.getItems().size();
    }

    @Override
    public int getItemViewType(int position) {
        Identifiable identifiable = request.getItems().get(position);
        return identifiable instanceof Item ? ((Item) identifiable).getItemType() : identifiable instanceof User ? USER : TEAM;
    }

    public interface AdapterListener extends InteractiveAdapter.AdapterListener {
        void onUserPicked(User user);

        void onTeamPicked(Team team);
    }
}
