package com.mainstreetcode.teammate.adapters;

import androidx.arch.core.util.Function;
import android.content.res.Resources;
import androidx.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.CompetitorViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.SelectionViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.mainstreetcode.teammate.model.enums.TournamentStyle;
import com.mainstreetcode.teammate.model.enums.TournamentType;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveViewHolder;

import java.util.Arrays;
import java.util.List;

import static com.mainstreetcode.teammate.model.Item.ALL_INPUT_VALID;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.TOURNAMENT;

/**
 * Adapter for {@link com.mainstreetcode.teammate.model.Tournament}
 */

public class TournamentEditAdapter extends InteractiveAdapter<InteractiveViewHolder, TournamentEditAdapter.AdapterListener> {

    private final List<Identifiable> items;

    public TournamentEditAdapter(List<Identifiable> items, AdapterListener listener) {
        super(listener);
        this.items = items;
    }

    @NonNull
    @Override
    public InteractiveViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.INPUT:
            case Item.NUMBER:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditBeforeCreation)
                        .setButtonRunnable((Function<Item, Boolean>) this::showsChangePicture, R.drawable.ic_picture_white_24dp, adapterListener::onImageClick);
            case Item.DESCRIPTION:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditAfterCreation, ALL_INPUT_VALID);
            case Item.TOURNAMENT_TYPE:
                return new SelectionViewHolder<>(getItemView(R.layout.viewholder_simple_input, viewGroup), R.string.tournament_type, Config.getTournamentTypes(adapterListener.getSport()::supportsTournamentType), TournamentType::getName, TournamentType::getCode, adapterListener::canEditBeforeCreation, ALL_INPUT_VALID);
            case Item.TOURNAMENT_STYLE:
                return new SelectionViewHolder<>(getItemView(R.layout.viewholder_simple_input, viewGroup), R.string.tournament_style, Config.getTournamentStyles(adapterListener.getSport()::supportsTournamentStyle), TournamentStyle::getName, TournamentStyle::getCode, adapterListener::canEditBeforeCreation, ALL_INPUT_VALID);
            case Item.INFO:
                Resources resources = viewGroup.getResources();
                return new SelectionViewHolder<>(getItemView(R.layout.viewholder_simple_input, viewGroup), R.string.tournament_single_final, Arrays.asList(true, false), flag -> resources.getString(flag ? R.string.yes : R.string.no), String::valueOf, adapterListener::canEditBeforeCreation, ALL_INPUT_VALID);
            case TOURNAMENT:
                return new CompetitorViewHolder(getItemView(R.layout.viewholder_competitor, viewGroup), viewHolder -> {});
            default:
                return new BaseItemViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull InteractiveViewHolder viewHolder, int i) {
        Object item = items.get(i);

        if (item instanceof Item) ((BaseItemViewHolder) viewHolder).bind((Item) item);
        else if (item instanceof Competitor) ((CompetitorViewHolder) viewHolder).bind((Competitor) item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object thing = items.get(position);
        return thing instanceof Item
                ? ((Item) thing).getItemType()
                : TOURNAMENT;
    }

    private boolean showsChangePicture(Item item) {
        return item.getStringRes() == R.string.tournament_name && adapterListener.canEditAfterCreation();
    }

    public interface AdapterListener extends ImageWorkerFragment.ImagePickerListener {

        boolean canEditBeforeCreation();

        boolean canEditAfterCreation();

        Sport getSport();
    }
}
