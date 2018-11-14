package com.mainstreetcode.teammate.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.input.DateTextInputStyle;
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.SpinnerTextInputStyle;
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle;
import com.mainstreetcode.teammate.baseclasses.BaseAdapter;
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.EventSearchRequest;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.ITEM;

public class EventSearchRequestAdapter extends BaseAdapter<BaseViewHolder, EventSearchRequestAdapter.EventSearchAdapterListener> {

    private final EventSearchRequest request;
    private final TextInputStyle.InputChooser chooser;

    public EventSearchRequestAdapter(EventSearchRequest request,
                                     EventSearchRequestAdapter.EventSearchAdapterListener listener) {
        super(listener);
        setHasStableIds(true);
        this.request = request;
        this.chooser = new Chooser(listener);
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
    }

    @SuppressWarnings("unchecked")
    @Override protected <S extends AdapterListener> S updateListener(BaseViewHolder<S> viewHolder) {
        return (S) adapterListener;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull BaseViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);
        Item item = request.asItems().get(position);
        ((InputViewHolder) viewHolder).bind(chooser.get(item));
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
        return ITEM;
    }

    public interface EventSearchAdapterListener extends InteractiveAdapter.AdapterListener {
        void onLocationClicked();
    }

    private static class Chooser extends TextInputStyle.InputChooser {

        private final List<Sport> sports;
        private final EventSearchAdapterListener adapterListener;

        private Chooser(EventSearchAdapterListener adapterListener) {
            this.adapterListener = adapterListener;
            this.sports = new ArrayList<>(Config.getSports());
            sports.add(0, Sport.empty());
        }

        public int iconGetter(Item item) {
            switch (item.getItemType()) {
                default:
                    return 0;
                case Item.LOCATION:
                    return R.drawable.ic_location_on_white_24dp;
            }
        }

        public boolean enabler(Item item) {
            switch (item.getItemType()) {
                default:
                case Item.INFO:
                    return false;
                case Item.DATE:
                case Item.SPORT:
                case Item.LOCATION:
                    return true;
            }
        }

        public CharSequence textChecker(Item item) {
            switch (item.getItemType()) {
                default:
                case Item.DATE:
                    return Item.NON_EMPTY.apply(item);
                case Item.SPORT:
                case Item.LOCATION:
                    return Item.ALL_INPUT_VALID.apply(item);
            }
        }

        public TextInputStyle apply(Item item) {
            int itemType = item.getItemType();
            switch (itemType) {
                default:
                case Item.INFO:
                case Item.LOCATION:
                    return new TextInputStyle(
                            Item.EMPTY_CLICK,
                            itemType == Item.LOCATION
                                    ? adapterListener::onLocationClicked
                                    : Item.NO_CLICK,
                            this::enabler,
                            this::textChecker,
                            this::iconGetter);
                case Item.SPORT:
                    return new SpinnerTextInputStyle<>(
                            R.string.choose_sport,
                            sports,
                            Sport::getName,
                            Sport::getCode,
                            this::enabler,
                            this::textChecker);
                case Item.DATE:
                    return new DateTextInputStyle(this::enabler);
            }
        }
    }
}
