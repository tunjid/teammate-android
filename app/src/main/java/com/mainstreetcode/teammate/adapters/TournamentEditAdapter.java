package com.mainstreetcode.teammate.adapters;

import android.content.res.Resources;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.CompetitorViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.SpinnerTextInputStyle;
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle;
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

import static com.mainstreetcode.teammate.model.Item.ALL_INPUT_VALID;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.TOURNAMENT;

/**
 * Adapter for {@link com.mainstreetcode.teammate.model.Tournament}
 */

public class TournamentEditAdapter extends InteractiveAdapter<InteractiveViewHolder, TournamentEditAdapter.AdapterListener> {

    private final List<Identifiable> items;
    private final TextInputStyle.InputChooser chooser;

    public TournamentEditAdapter(List<Identifiable> items, AdapterListener listener) {
        super(listener);
        this.items = items;
        chooser = new Chooser(adapterListener);
    }

    @NonNull
    @Override
    public InteractiveViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            default:
            case Item.INPUT:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
            case TOURNAMENT:
                return new CompetitorViewHolder(getItemView(R.layout.viewholder_competitor, viewGroup), viewHolder -> {});
        }
    }

    @Override
    public void onBindViewHolder(@NonNull InteractiveViewHolder viewHolder, int i) {
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
        Object thing = items.get(position);
        return thing instanceof Item ? Item.INPUT : TOURNAMENT;
    }

    public interface AdapterListener extends ImageWorkerFragment.ImagePickerListener {

        boolean canEditBeforeCreation();

        boolean canEditAfterCreation();

        Sport getSport();
    }

    private static class Chooser extends TextInputStyle.InputChooser {

        private AdapterListener adapterListener;

        Chooser(AdapterListener adapterListener) {
            this.adapterListener = adapterListener;
        }

        @Override public int iconGetter(Item item) {
            return item.getStringRes() == R.string.tournament_name && adapterListener.canEditAfterCreation()
                    ? R.drawable.ic_picture_white_24dp : 0;
        }

        @Override public boolean enabler(Item item) {
            switch (item.getItemType()) {
                default:
                case Item.ABOUT:
                    return Item.FALSE.apply(item);
                case Item.INFO:
                case Item.INPUT:
                case Item.NUMBER:
                case Item.TOURNAMENT_TYPE:
                case Item.TOURNAMENT_STYLE:
                    return adapterListener.canEditBeforeCreation();
                case Item.DESCRIPTION:
                    return adapterListener.canEditAfterCreation();
            }
        }

        @Override public CharSequence textChecker(Item item) {
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

        @Override public TextInputStyle apply(Item item) {
            int itemType = item.getItemType();
            switch (itemType) {
                default:
                case Item.INPUT:
                case Item.NUMBER:
                case Item.DESCRIPTION:
                    return new TextInputStyle(
                            Item.NO_CLICK,
                            adapterListener::onImageClick,
                            this::enabler,
                            this::textChecker,
                            this::iconGetter);
                case Item.TOURNAMENT_TYPE:
                    return new SpinnerTextInputStyle<>(
                            R.string.tournament_type,
                            Config.getTournamentTypes(adapterListener.getSport()::supportsTournamentType),
                            TournamentType::getName,
                            TournamentType::getCode,
                            this::enabler,
                            ALL_INPUT_VALID);
                case Item.TOURNAMENT_STYLE:
                    return new SpinnerTextInputStyle<>(
                            R.string.tournament_style,
                            Config.getTournamentStyles(adapterListener.getSport()::supportsTournamentStyle),
                            TournamentStyle::getName,
                            TournamentStyle::getCode,
                            this::enabler,
                            ALL_INPUT_VALID);
                case Item.INFO:
                    Resources resources = App.getInstance().getResources();
                    return new SpinnerTextInputStyle<>(
                            R.string.tournament_single_final,
                            Arrays.asList(true, false),
                            flag -> resources.getString(flag ? R.string.yes : R.string.no),
                            String::valueOf,
                            this::enabler,
                            ALL_INPUT_VALID);
            }
        }
    }
}
