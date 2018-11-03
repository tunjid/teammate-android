package com.mainstreetcode.teammate.adapters;

import android.content.res.Resources;
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

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;

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
            case Item.DESCRIPTION:

                return new InputViewHolder(
                        getItemView(R.layout.viewholder_simple_input, viewGroup),
                        (Function<Item, Boolean>) this::canEdit,
                        (Function<Item, CharSequence>) this::textChecker)
                        .setButtonRunnable(
                                R.drawable.ic_picture_white_24dp,
                                adapterListener::onImageClick,
                                (Function<Item, Boolean>) this::showsChangePicture);
            case Item.TOURNAMENT_TYPE:
                return new SelectionViewHolder<>(getItemView(
                        R.layout.viewholder_simple_input, viewGroup),
                        R.string.tournament_type,
                        Config.getTournamentTypes(adapterListener.getSport()::supportsTournamentType),
                        TournamentType::getName,
                        TournamentType::getCode,
                        (Function<Item, Boolean>) this::canEdit,
                        ALL_INPUT_VALID);
            case Item.TOURNAMENT_STYLE:
                return new SelectionViewHolder<>(
                        getItemView(R.layout.viewholder_simple_input, viewGroup),
                        R.string.tournament_style,
                        Config.getTournamentStyles(adapterListener.getSport()::supportsTournamentStyle),
                        TournamentStyle::getName,
                        TournamentStyle::getCode,
                        (Function<Item, Boolean>) this::canEdit,
                        ALL_INPUT_VALID);
            case Item.INFO:
                Resources resources = viewGroup.getResources();
                return new SelectionViewHolder<>(
                        getItemView(R.layout.viewholder_simple_input, viewGroup),
                        R.string.tournament_single_final,
                        Arrays.asList(true, false),
                        flag -> resources.getString(flag ? R.string.yes : R.string.no),
                        String::valueOf,
                        (Function<Item, Boolean>) this::canEdit,
                        ALL_INPUT_VALID);
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
        else if (item instanceof Competitor)
            ((CompetitorViewHolder) viewHolder).bind((Competitor) item);
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

    private boolean canEdit(Item item) {
        switch (item.getItemType()) {
            default:
            case Item.ABOUT:
                return Item.FALSE.apply(item);
            case Item.INFO:
            case Item.NUMBER:
            case Item.TOURNAMENT_TYPE:
            case Item.TOURNAMENT_STYLE:
                return adapterListener.canEditBeforeCreation();
            case Item.DESCRIPTION:
                return adapterListener.canEditAfterCreation();
        }
    }

    private CharSequence textChecker(Item item) {
        switch (item.getItemType()) {
            default:
            case Item.INPUT:
            case Item.NUMBER:
                return Item.NON_EMPTY.apply(item);
            case Item.INFO:
            case Item.DESCRIPTION:
            case Item.TOURNAMENT_TYPE:
            case Item.TOURNAMENT_STYLE:
                return Item.ALL_INPUT_VALID.apply(item);
        }
    }

    public interface AdapterListener extends ImageWorkerFragment.ImagePickerListener {

        boolean canEditBeforeCreation();

        boolean canEditAfterCreation();

        Sport getSport();
    }
}
