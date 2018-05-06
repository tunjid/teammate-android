package com.mainstreetcode.teammate.adapters;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.ClickInputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.DateViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.SelectionViewHolder;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.EventSearchRequest;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import static com.mainstreetcode.teammate.model.Item.FALSE;
import static com.mainstreetcode.teammate.model.Item.TRUE;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getItemView;

/**
 * Adapter for {@link Event}
 */

public class EventSearchRequestAdapter extends BaseRecyclerViewAdapter<BaseViewHolder, EventSearchRequestAdapter.EventSearchAdapterListener> {

    private final EventSearchRequest request;
    private final List<Sport> sports;

    public EventSearchRequestAdapter(EventSearchRequest request,
                                     EventSearchRequestAdapter.EventSearchAdapterListener listener) {
        super(listener);
        setHasStableIds(true);
        this.request = request;
        this.sports = new ArrayList<>(Config.getSports());
        sports.add(0, Sport.empty());
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.LOCATION:
                return new ClickInputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), TRUE, adapterListener::onLocationClicked)
                        .setButtonRunnable(R.drawable.ic_location_on_white_24dp, adapterListener::onLocationClicked);
            case Item.INFO:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), FALSE);
            case Item.SPORT:
                return new SelectionViewHolder<>(getItemView(R.layout.viewholder_simple_input, viewGroup), R.string.choose_sport, sports, Sport::getName, Sport::getCode, TRUE, FALSE);
            case Item.DATE:
                return new DateViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), TRUE);
            default:
                return new BaseItemViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull BaseViewHolder viewHolder, int position) {
        Item item = request.asItems().get(position);
        ((BaseItemViewHolder) viewHolder).bind(item);
    }

    @Override
    public int getItemCount() {
        return request.asItems().size();
    }

    @Override
    public long getItemId(int position) {
        return request.asItems().get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return request.asItems().get(position).getItemType();
    }

    public interface EventSearchAdapterListener extends BaseRecyclerViewAdapter.AdapterListener{
        void onLocationClicked();
    }
}
