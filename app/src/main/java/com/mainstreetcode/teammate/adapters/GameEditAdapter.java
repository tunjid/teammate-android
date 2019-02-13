package com.mainstreetcode.teammate.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.CompetitorViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle;
import com.mainstreetcode.teammate.baseclasses.BaseAdapter;
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;

import java.util.List;

import androidx.annotation.NonNull;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.AWAY;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.ITEM;

/**
 * Adapter for {@link com.mainstreetcode.teammate.model.Tournament}
 */

public class GameEditAdapter extends BaseAdapter<BaseViewHolder, GameEditAdapter.AdapterListener> {

    private final List<Identifiable> items;
    private final TextInputStyle.InputChooser chooser;

    public GameEditAdapter(List<Identifiable> items, AdapterListener listener) {
        super(listener);
        this.items = items;
        this.chooser = new Chooser(adapterListener);
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            default:
            case ITEM:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
            case AWAY:
                return new CompetitorViewHolder(getItemView(R.layout.viewholder_competitor, viewGroup), adapterListener::onAwayClicked)
                        .hideSubtitle().withTitle(R.string.pick_away_competitor);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <S extends InteractiveAdapter.AdapterListener> S updateListener(BaseViewHolder<S> viewHolder) {
        if (viewHolder instanceof CompetitorViewHolder)
            return (S) ((CompetitorAdapter.AdapterListener) adapterListener::onAwayClicked);
        return (S) adapterListener;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder viewHolder, int i) {
        super.onBindViewHolder(viewHolder, i);
        Object item = items.get(i);
        if (item instanceof Item) ((InputViewHolder) viewHolder).bind(chooser.get((Item) item));
        else if (item instanceof Competitor)
            ((CompetitorViewHolder) viewHolder).bind((Competitor) item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object object = items.get(position);
        return object instanceof Item ? ITEM : AWAY;
    }

    public interface AdapterListener extends InteractiveAdapter.AdapterListener {
        boolean canEditGame();

        void onAwayClicked(Competitor away);
    }

    private static class Chooser extends TextInputStyle.InputChooser {

        private AdapterListener adapterListener;

        Chooser(AdapterListener adapterListener) {
            this.adapterListener = adapterListener;
        }

        @Override public boolean enabler(Item item) {
            switch (item.getItemType()) {
                default:
                    return false;
                case Item.INPUT:
                    return adapterListener.canEditGame();
            }
        }

        @Override public TextInputStyle apply(Item item) {
            switch (item.getItemType()) {
                default:
                case Item.INPUT:
                case Item.NUMBER:
                    return new TextInputStyle(Item.NO_CLICK, Item.NO_CLICK, this::enabler, Item.NON_EMPTY, Item.NO_ICON);
            }
        }
    }
}
