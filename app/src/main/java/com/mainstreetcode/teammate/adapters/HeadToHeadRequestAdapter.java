package com.mainstreetcode.teammate.adapters;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.CompetitorViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.DateViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.SelectionViewHolder;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.HeadToHeadRequest;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.mainstreetcode.teammate.model.enums.TournamentType;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import static com.mainstreetcode.teammate.model.Item.ALL_INPUT_VALID;
import static com.mainstreetcode.teammate.model.Item.TRUE;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getItemView;

/**
 * Adapter for {@link Event}
 */

public class HeadToHeadRequestAdapter extends BaseRecyclerViewAdapter<BaseViewHolder, HeadToHeadRequestAdapter.EventSearchAdapterListener> {

    private final HeadToHeadRequest request;
    private final List<Sport> sports;

    public HeadToHeadRequestAdapter(HeadToHeadRequest request,
                                    HeadToHeadRequestAdapter.EventSearchAdapterListener listener) {
        super(listener);
        this.request = request;
        sports = new ArrayList<>(Config.getSports());
        sports.add(0, Sport.empty());
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.TOURNAMENT_TYPE:
                return new SelectionViewHolder<>(getItemView(R.layout.viewholder_simple_input, viewGroup), R.string.tournament_type, Config.getTournamentTypes(type -> true), TournamentType::getName, TournamentType::getCode, TRUE, ALL_INPUT_VALID);
            case Item.SPORT:
                return new SelectionViewHolder<>(getItemView(R.layout.viewholder_simple_input, viewGroup), R.string.choose_sport, sports, Sport::getName, Sport::getCode, TRUE, ALL_INPUT_VALID);
            case Item.DATE:
                return new DateViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), TRUE);
            case Item.COMPETITOR:
                return new CompetitorViewHolder(getItemView(R.layout.viewholder_competitor, viewGroup), adapterListener::onCompetitorClicked);
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
        else if (identifiable instanceof Competitor)
            ((CompetitorViewHolder) viewHolder).bind((Competitor) identifiable);
    }

    @Override
    public int getItemCount() {
        return request.getItems().size();
    }

    @Override
    public int getItemViewType(int position) {
        Identifiable identifiable = request.getItems().get(position);
        return identifiable instanceof Item ? ((Item) identifiable).getItemType() : Item.COMPETITOR;
    }

    public interface EventSearchAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onCompetitorClicked(Competitor home);
    }
}
