package com.mainstreetcode.teammate.baseclasses;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.snackbar.Snackbar;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.ChoiceBar;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Message;
import com.mainstreetcode.teammate.model.UiState;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.Validator;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.functions.Consumer;
import com.tunjid.androidbootstrap.view.util.InsetFlags;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.MenuRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentTransaction;
import io.reactivex.disposables.CompositeDisposable;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.O;

/**
 * Base Fragment for this app
 */

public class TeammatesBaseFragment extends BaseFragment implements View.OnClickListener {

    protected static final InsetFlags NO_TOP = InsetFlags.NO_TOP;
    protected static final InsetFlags NONE = InsetFlags.NONE;
    protected static final int PLACE_PICKER_REQUEST = 1;
    private static final int LIGHT_NAV_BAR_COLOR = 0xFFE0E0E0; // Same as R.color.light_grey

    protected static final Validator VALIDATOR = new Validator();

    protected CompositeDisposable disposables = new CompositeDisposable();
    protected io.reactivex.functions.Consumer<Throwable> emptyErrorHandler = ErrorHandler.EMPTY;
    protected ErrorHandler defaultErrorHandler;
    private Message lastMessage;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        defaultErrorHandler = ErrorHandler.builder()
                .defaultMessage(getString(R.string.error_default))
                .add(this::handleErrorMessage)
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) togglePersistentUi();
    }

    @Override
    public void onPause() {
        disposables.clear();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        disposables.clear();
        getPersistentUiController().setFabClickListener(null);
        super.onDestroyView();
    }

    public InsetFlags insetFlags() {
        return InsetFlags.ALL;
    }

    public int[] staticViews() { return new int[]{}; }

    @StringRes
    protected int getFabStringResource() { return R.string.empty_string; }

    @DrawableRes
    protected int getFabIconResource() { return R.drawable.ic_add_white_24dp; }

    @MenuRes
    protected int getToolbarMenu() { return 0; }

    @MenuRes
    protected int getAltToolbarMenu() { return 0; }

    @ColorInt
    protected int getNavBarColor() { return SDK_INT >= O ? LIGHT_NAV_BAR_COLOR : Color.BLACK; }

    public boolean showsFab() { return false; }

    public boolean showsToolBar() { return true; }

    public boolean showsAltToolBar() { return false; }

    public boolean showsBottomNav() { return true; }

    protected boolean showsSystemUI() { return true; }

    protected boolean hasLightNavBar() { return SDK_INT >= O; }

    protected CharSequence getToolbarTitle() { return ""; }

    protected CharSequence getAltToolbarTitle() { return ""; }

    @Override
    public void onClick(View v) {}

    protected void toggleProgress(boolean show) {getPersistentUiController().toggleProgress(show);}

    @SuppressWarnings("WeakerAccess")
    protected void setFabExtended(boolean extended) {getPersistentUiController().setFabExtended(extended);}

    protected void showSnackbar(CharSequence message) {getPersistentUiController().showSnackBar(message);}

    protected void showSnackbar(Consumer<Snackbar> consumer) {getPersistentUiController().showSnackBar(consumer);}

    protected void showChoices(Consumer<ChoiceBar> consumer) {getPersistentUiController().showChoices(consumer);}

    @Nullable
    @Override
    @SuppressLint("CommitTransaction")
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        return beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out);
    }

    protected void setEnterExitTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition baseTransition = new Fade();

            setEnterTransition(baseTransition);
            setExitTransition(baseTransition);

            if (Config.isStaticVariant()) return;

            Transition baseSharedTransition = new TransitionSet()
                    .addTransition(new ChangeBounds())
                    .addTransition(new ChangeTransform())
                    .addTransition(new ChangeImageTransform())
                    .setOrdering(TransitionSet.ORDERING_TOGETHER);

            setSharedElementEnterTransition(baseSharedTransition);
            setSharedElementReturnTransition(baseSharedTransition);
        }
    }

    protected void removeEnterExitTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setEnterTransition(new Fade());
            setExitTransition(new Fade());
            setSharedElementEnterTransition(null);
            setSharedElementReturnTransition(null);
        }
    }

    protected void handleErrorMessage(Message message) {
        if (lastMessage == null || !lastMessage.equals(message)) showSnackbar(message.getMessage());
        lastMessage = message;
        toggleProgress(false);
    }

    @SuppressWarnings("unused")
    protected void updateFabOnScroll(int dx, int dy) {
        if (showsFab() && Math.abs(dy) > 3) toggleFab(dy < 0);
    }

    protected void onKeyBoardChanged(boolean appeared) {}

    public void togglePersistentUi() {
        getPersistentUiController().update(fromThis());
    }

    @SuppressLint("CommitTransaction")
    @SuppressWarnings("ConstantConditions")
    protected final FragmentTransaction beginTransaction() {
        return getFragmentManager().beginTransaction();
    }

    @SuppressWarnings("WeakerAccess")
    protected PersistentUiController getPersistentUiController() {
        Activity activity = getActivity();
        return activity == null ? PersistentUiController.DUMMY : ((PersistentUiController) activity);
    }

    protected void hideKeyboard() {
        View root = getView();
        if (root == null) return;

        InputMethodManager imm = (InputMethodManager) root.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(root.getWindowToken(), 0);
    }

    private void toggleFab(boolean show) {getPersistentUiController().toggleFab(show);}

    private UiState fromThis() {
        return new UiState(
                this.getFabIconResource(),
                this.getFabStringResource(),
                this.getToolbarMenu(),
                this.getAltToolbarMenu(),
                this.getNavBarColor(),
                this.showsFab(),
                this.showsToolBar(),
                this.showsAltToolBar(),
                this.showsBottomNav(),
                this.showsSystemUI(),
                this.hasLightNavBar(),
                this.insetFlags(),
                this.getToolbarTitle(),
                this.getAltToolbarTitle(),
                getView() == null ? null : this
        );
    }

}
