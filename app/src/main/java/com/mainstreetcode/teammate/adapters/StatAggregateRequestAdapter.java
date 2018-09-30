package com.mainstreetcode.teammate.adapters;

import android.support.annotation.NonNull;
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
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import static com.mainstreetcode.teammate.model.Item.ALL_INPUT_VALID;
import static com.mainstreetcode.teammate.model.Item.TRUE;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.TEAM;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.USER;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getItemView;

/**
 * Adapter for {@link Event}
 */

public class StatAggregateRequestAdapter extends BaseRecyclerViewAdapter<BaseViewHolder, StatAggregateRequestAdapter.AdapterListener> {

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
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.SPORT:
                return new SelectionViewHolder<>(getItemView(R.layout.viewholder_simple_input, viewGroup), R.string.choose_sport, sports, Sport::getName, Sport::getCode, TRUE, ALL_INPUT_VALID);
            case Item.DATE:
                return new DateViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), TRUE);
            case USER:
                return new UserViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), adapterListener::onUserPicked);
            case TEAM:
                return new TeamViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), adapterListener::onTeamPicked);
            default:
                return new BaseItemViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull BaseViewHolder viewHolder, int position) {
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

    public interface AdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onUserPicked(User user);

        void onTeamPicked(Team team);
    }
}
