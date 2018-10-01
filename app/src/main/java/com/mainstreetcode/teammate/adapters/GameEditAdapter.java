package com.mainstreetcode.teammate.adapters;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.CompetitorViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

import static com.mainstreetcode.teammate.model.Item.COMPETITOR;
import static com.mainstreetcode.teammate.model.Item.FALSE;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getItemView;

/**
 * Adapter for {@link com.mainstreetcode.teammate.model.Tournament}
 */

public class GameEditAdapter extends BaseRecyclerViewAdapter<BaseViewHolder, GameEditAdapter.AdapterListener> {

    private final List<Identifiable> items;

    public GameEditAdapter(List<Identifiable> items, AdapterListener listener) {
        super(listener);
        this.items = items;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.INPUT:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditGame);
            case Item.NUMBER:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), FALSE);
            case COMPETITOR:
                return new CompetitorViewHolder(getCompetitorItemView(viewGroup), competitor -> {});
            default:
                return new BaseItemViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder viewHolder, int i) {
        Object item = items.get(i);
        if (item instanceof Item) ((BaseItemViewHolder) viewHolder).bind((Item) item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object object = items.get(position);
        return object instanceof Item ? ((Item) object).getItemType()
                : COMPETITOR;
    }

    private View getCompetitorItemView(@NonNull ViewGroup viewGroup) {
        View itemView = getItemView(R.layout.viewholder_competitor, viewGroup);
        itemView.findViewById(R.id.item_subtitle).setVisibility(View.INVISIBLE);
        return itemView;
    }

    public interface AdapterListener extends ImageWorkerFragment.ImagePickerListener {
        boolean canEditGame();
    }
}
