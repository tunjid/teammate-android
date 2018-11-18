package com.mainstreetcode.teammate.adapters.viewholders.input;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.util.ErrorHandler;

import java.util.Objects;

import androidx.annotation.Nullable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static android.text.TextUtils.isEmpty;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.core.content.ContextCompat.getColor;
import static androidx.core.content.ContextCompat.getDrawable;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.listenForLayout;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public class InputViewHolder<T extends ImageWorkerFragment.ImagePickerListener> extends BaseViewHolder<T>
        implements
        TextWatcher {

    private static final int HINT_ANIMATION_DURATION = 200;
    private static final float HINT_SHRINK_SCALE = 0.8F;
    private static final float HALF = 0.5F;

    private int lastLineCount = 1;

    protected final TextView hint;
    protected final EditText text;
    private final ImageButton button;

    @Nullable
    TextInputStyle textInputStyle;

    public InputViewHolder(View itemView) {
        super(itemView);
        hint = itemView.findViewById(R.id.hint);
        text = itemView.findViewById(R.id.input);
        button = itemView.findViewById(R.id.button);
        text.setOnFocusChangeListener((v, hasFocus) -> {
            tintHint(hasFocus);
            scaleHint(!hasFocus && isEmpty(text.getText()));
        });
    }

    @Override protected void clear() {
        button.setOnClickListener(null);
        text.setOnClickListener(null);
        text.removeTextChangedListener(this);

        if (textInputStyle != null) textInputStyle.setViewHolder(null);
        textInputStyle = null;

        super.clear();
    }

    @Override protected void onDetached() {
        button.setVisibility(GONE);
        super.onDetached();
    }

    protected float getHintLateralTranslation() {
        int width = hint.getWidth();
        return -((width - (HINT_SHRINK_SCALE * width)) * HALF);
    }

    protected float getHintLongitudinalTranslation() {
        return -((itemView.getHeight() - hint.getHeight()) * HALF);
    }

    public void bind(TextInputStyle inputStyle) {
        this.textInputStyle = inputStyle;
        inputStyle.setViewHolder(this);

        Item item = inputStyle.getItem();

        int newInputType = item.getInputType();
        int oldInputType = text.getInputType();

        boolean isEditable = inputStyle.isEditable();
        boolean isSelector = inputStyle.isSelector();

        boolean isEnabled = text.isEnabled();
        boolean isClickable = text.isClickable();
        boolean isFocusable = text.isFocusable();
        boolean isFocusableInTouchMode = text.isFocusableInTouchMode();

        CharSequence newValue = item.getValue().toString();
        CharSequence oldValue = text.getText().toString();

        CharSequence oldHint = hint.getText();
        CharSequence newHint = itemView.getContext().getString(item.getStringRes());

        if (isEnabled != isEditable) text.setEnabled(isEditable);
        if (isClickable != isSelector) text.setClickable(isSelector);
        if (isFocusable == isSelector) text.setFocusable(!isSelector);
        if (isFocusableInTouchMode == isSelector) text.setFocusableInTouchMode(!isSelector);

        if (!Objects.equals(oldHint, newHint)) hint.setText(newHint);
        if (!Objects.equals(oldValue, newValue)) text.setText(newValue);
        if (oldInputType != newInputType) text.setInputType(newInputType);

        text.setOnClickListener(inputStyle.textClickListener());
        button.setOnClickListener(inputStyle.buttonClickListener());

        text.removeTextChangedListener(this);
        if (!isSelector) text.addTextChangedListener(this);

        updateButton();
        checkForErrors();
        setTintAlpha(text.hasFocus());
        listenForLayout(hint, () -> scaleHint(isEmpty(text.getText())));
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {/* Nothing */}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {/* Nothing */}

    @Override
    public void afterTextChanged(Editable editable) {
        int currentLineCount = text.getLineCount();
        if (lastLineCount != currentLineCount) listenForLayout(hint, () -> scaleHint(false));
        lastLineCount = currentLineCount;

        if (textInputStyle == null || textInputStyle.isSelector()) return;
        textInputStyle.getItem().setValue(editable.toString());
        checkForErrors();
    }

    void updateText(CharSequence text) {
        this.text.setText(text);
        checkForErrors();
        listenForLayout(hint, () -> scaleHint(isEmpty(text)));
    }

    private void checkForErrors() {
        if (textInputStyle == null) return;

        CharSequence errorMessage = textInputStyle.errorText();
        if (Objects.equals(errorMessage, text.getError())) return;

        if (isEmpty(errorMessage)) text.setError(null);
        else text.setError(errorMessage);
    }

    private void updateButton() {
        if (textInputStyle == null) return;

        int newIcon = textInputStyle.getIcon();

        int oldVisibility = button.getVisibility();
        int newVisibility = newIcon == 0 ? GONE : VISIBLE;

        if (oldVisibility != newVisibility) button.setVisibility(newVisibility);
        if (newIcon != 0)
            disposables.add(Single.fromCallable(() -> getDrawable(text.getContext(), newIcon))
                    .subscribeOn(Schedulers.io())
                    .observeOn(mainThread())
                    .subscribe(button::setImageDrawable, ErrorHandler.EMPTY));
    }

    private void scaleHint(boolean grow) {
        float scale = grow ? 1F : HINT_SHRINK_SCALE;
        float translationX = grow ? 0 : getHintLateralTranslation();
        float translationY = grow ? 0 : getHintLongitudinalTranslation();

        hint.animate()
                .scaleX(scale)
                .scaleY(scale)
                .translationX(translationX)
                .translationY(translationY)
                .setDuration(HINT_ANIMATION_DURATION)
                .start();
    }

    private void tintHint(boolean hasFocus) {
        int start = hint.getCurrentTextColor();
        int end = getColor(hint.getContext(), hasFocus ? R.color.colorAccent : R.color.dark_grey);

        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), start, end);
        animator.setDuration(HINT_ANIMATION_DURATION);
        animator.addUpdateListener(animation -> hint.setTextColor((int) animation.getAnimatedValue()));
        animator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) { setTintAlpha(hasFocus); }
        });
        animator.start();
    }

    private void setTintAlpha(boolean hasFocus) {
        hint.setAlpha(textInputStyle != null && !textInputStyle.isEditable() && !hasFocus ? 0.38F : 1F);
    }
}
