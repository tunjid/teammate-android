package com.mainstreetcode.teammate.adapters.viewholders.input;

import android.app.Activity;
import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;

import com.mainstreetcode.teammate.baseclasses.TeammatesBaseActivity;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.util.ModelUtils;

import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.getActivity;

public class TextInputStyle {

    Item item;
    InputViewHolder viewHolder;

    @Nullable private Runnable textRunnable;
    @Nullable private Runnable buttonRunnable;

    private Function<Item, Boolean> enabler;
    private Function<Item, CharSequence> errorChecker;
    private Function<Item, Integer> iconVisibilityFunction;

    public TextInputStyle(@Nullable Runnable textRunnable,
                          @Nullable Runnable buttonRunnable,
                          Function<Item, Boolean> enabler,
                          Function<Item, CharSequence> errorChecker,
                          Function<Item, Integer> iconVisibilityFunction) {
        this.textRunnable = textRunnable;
        this.buttonRunnable = buttonRunnable;
        this.enabler = enabler;
        this.errorChecker = errorChecker;
        this.iconVisibilityFunction = iconVisibilityFunction;
    }

    public TextInputStyle(Function<Item, Boolean> enabler,
                          Function<Item, CharSequence> errorChecker,
                          Function<Item, Integer> iconVisibilityFunction) {
        this(Item.NO_CLICK, Item.NO_CLICK, enabler, errorChecker, iconVisibilityFunction);
    }

    Item getItem() { return item; }

    boolean isSelector() { return textRunnable != null; }

    boolean isEnabled() { return enabler.apply(item);}

    int getIcon() { return iconVisibilityFunction.apply(item);}

    CharSequence errorText() { return errorChecker.apply(item);}

    View.OnClickListener buttonClickListener() { return isEnabled() ? this::onButtonClicked : null; }

    View.OnClickListener textClickListener() {
        return isEnabled() ? this::onTextClicked : null;
    }

    void onTextClicked(View view) { if (textRunnable != null) textRunnable.run();}

    void onButtonClicked(View view) { if (buttonRunnable != null) buttonRunnable.run(); }

    void setViewHolder(InputViewHolder viewHolder) {
        this.viewHolder = viewHolder;
    }

    void onDialogDismissed(Context context) {
        Activity activity = getActivity(context);
        if (activity instanceof TeammatesBaseActivity)
            ((TeammatesBaseActivity) activity).onDialogDismissed();
    }

    private TextInputStyle with(Item item) {
        this.item = item;
        return this;
    }

    public static abstract class InputChooser implements Function<Item, TextInputStyle> {

        private final SparseArray<TextInputStyle> behaviors = new SparseArray<>();

        public TextInputStyle get(Item item) {
            return ModelUtils.get(
                    item.getStringRes(),
                    behaviors::get, behaviors::append,
                    () -> apply(item)).with(item);
        }

        public int iconGetter(Item item) { return 0;}

        public boolean enabler(Item item) { return false;}

        public CharSequence textChecker(Item item) { return null;}
    }
}