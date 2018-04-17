package com.mainstreetcode.teammate.adapters;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.DateViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.SelectionViewHolder;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.EventSearchRequest;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import static com.mainstreetcode.teammate.model.Item.FALSE;
import static com.mainstreetcode.teammate.model.Item.TRUE;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getItemView;

/**
 * Adapter for {@link Event}
 */

public class PublicEventRequestAdapter extends BaseRecyclerViewAdapter<BaseViewHolder, BaseRecyclerViewAdapter.AdapterListener> {

    private final EventSearchRequest request;

    public PublicEventRequestAdapter(EventSearchRequest request) {
        super(new BaseRecyclerViewAdapter.AdapterListener() {});
        this.request = request;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.INFO:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), FALSE);
            case Item.SPORT:
                return new SelectionViewHolder<>(getItemView(R.layout.viewholder_simple_input, viewGroup), R.string.choose_sport, Config.getSports(), Sport::getName, Sport::getCode, TRUE, FALSE);
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
}
