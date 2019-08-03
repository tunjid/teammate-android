/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.util;


import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.ripple.RippleUtils;
import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.GameAdapter;
import com.mainstreetcode.teammate.model.Game;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.view.util.ViewUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.arch.core.util.Function;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.palette.graphics.Palette;
import io.reactivex.Single;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static io.reactivex.Single.error;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;

public class ViewHolderUtil extends ViewUtil {

    public static final int ITEM = 283;
    public static final int USER = 284;
    public static final int TEAM = 285;
    public static final int CHAT = 286;
    public static final int ROLE = 287;
    public static final int GAME = 288;
    public static final int HOME = 289;
    public static final int AWAY = 290;
    public static final int STAT = 291;
    public static final int EVENT = 292;
    public static final int GUEST = 292;
    public static final int FEED_ITEM = 294;
    public static final int TOURNAMENT = 295;
    public static final int CONTENT_AD = 296;
    public static final int INSTALL_AD = 297;
    public static final int MEDIA_IMAGE = 298;
    public static final int MEDIA_VIDEO = 299;
    public static final int JOIN_REQUEST = 300;
    public static final int BLOCKED_USER = 301;
    public static final int THUMBNAIL_SIZE = 250;
    public static final int FULL_RES_LOAD_DELAY = 200;
    public static final int TOOLBAR_ANIM_DELAY = 200;
    private static final int DEFAULT_STROKE_VALUE = -1;

    public static Function<CharSequence, CharSequence> allowsSpecialCharacters =
            input -> ModelUtils.isValidScreenName(input) ? "" : App.getInstance().getResources().getString(R.string.no_special_characters);

    @ColorInt
    public static int resolveThemeColor(Context context, @AttrRes int colorAttr) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(colorAttr, typedValue, true);
        return typedValue.data;
    }

    public static View getItemView(@LayoutRes int res, ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(res, parent, false);
    }

    public static String getTransitionName(Object item, @IdRes int id) {
        return item.hashCode() + "-" + id;
    }

    @Nullable
    public static Activity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) return (Activity) context;
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public static void updateToolBar(Toolbar toolbar, int menu, CharSequence title) {
        int childCount = toolbar.getChildCount();

        if (toolbar.getId() == R.id.alt_toolbar || childCount <= 2) {
            toolbar.setTitle(title);
            replaceMenu(toolbar, menu);
        }
        else for (int i = 0; i < childCount; i++) {
            View child = toolbar.getChildAt(i);
            if (child instanceof ImageView) continue;

            child.animate().alpha(0).setDuration(TOOLBAR_ANIM_DELAY).withEndAction(() -> {
                if (child instanceof TextView) toolbar.setTitle(title);
                else if (child instanceof ActionMenuView) replaceMenu(toolbar, menu);
                child.animate().setDuration(TOOLBAR_ANIM_DELAY).setInterpolator(new AccelerateDecelerateInterpolator()).alpha(1).start();
            }).start();
        }
    }

    private static void replaceMenu(Toolbar toolbar, int menu) {
        toolbar.getMenu().clear();
        if (menu != 0) toolbar.inflateMenu(menu);
    }

    public static boolean isDisplayingSystemUI(View decorView) {
        return (decorView.getSystemUiVisibility() & SYSTEM_UI_FLAG_FULLSCREEN) != 0;
    }

    public static Single<Drawable> fetchRoundedDrawable(Context context, String url, int size) {
        return fetchRoundedDrawable(context, url, size, 0);
    }

    public static Single<Drawable> fetchRoundedDrawable(Context context, String url, int size, int placeholder) {
        return Single.<Bitmap>create(emitter -> Picasso.get().load(url).resize(size, size).centerCrop()
                .into(new Target() {
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) { emitter.onSuccess(bitmap); }

                    public void onBitmapFailed(Exception e, Drawable errorDrawable) { emitter.onError(e); }

                    public void onPrepareLoad(Drawable placeHolderDrawable) { }
                }))
                .onErrorResumeNext(throwable -> placeholder != 0
                        ? Single.fromCallable(() -> getBitmapFromVectorDrawable(context, placeholder))
                        : error(throwable))
                .map(bitmap -> {
                    RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
                    imageDrawable.setCircular(true);
                    imageDrawable.setCornerRadius(size);
                    return imageDrawable;
                });
    }

    public static Single<Palette> extractPalette(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();

        if (drawable == null) return error(new TeammateException("No drawable in ImageView"));
        if (!(drawable instanceof BitmapDrawable)) return error(new TeammateException("Not a BitmapDrawable"));

        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        return Single.fromCallable(() -> Palette.from(bitmap).generate()).subscribeOn(io()).observeOn(mainThread());
    }

    public static void updateForegroundDrawable(View itemView) {
        if (!(itemView instanceof MaterialCardView)) return;

        MaterialCardView materialCardView = (MaterialCardView) itemView;
        int rippleColor = getRippleColor(materialCardView.getContext());
        int strokeWidth = materialCardView.getStrokeWidth();
        int strokeColor = materialCardView.getStrokeColor();
        float radius = materialCardView.getRadius();

        GradientDrawable fgDrawable = new GradientDrawable();
        fgDrawable.setCornerRadius(radius);
        // In order to set a stroke, a size and color both need to be set. We default to a zero-width
        // width size, but won't set a default color. This prevents drawing a stroke that blends in with
        // the card but that could affect card spacing.
        if (strokeColor != DEFAULT_STROKE_VALUE) fgDrawable.setStroke(strokeWidth, strokeColor);

        if (!materialCardView.isClickable()) return;

        Drawable rippleDrawable;
        if (RippleUtils.USE_FRAMEWORK_RIPPLE) {
            //noinspection NewApi
            rippleDrawable = new RippleDrawable(ColorStateList.valueOf(rippleColor), null, createForegroundShape(radius));
        }
        else {
            rippleDrawable = new StateListDrawable();
            Drawable foregroundShape = createForegroundShape(radius);
            DrawableCompat.setTint(foregroundShape, rippleColor);
            ((StateListDrawable) rippleDrawable).addState(new int[]{android.R.attr.state_pressed}, foregroundShape);
        }
        materialCardView.setForeground(new LayerDrawable(new Drawable[]{rippleDrawable, fgDrawable}));
    }

    private static int getRippleColor(Context context) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorControlHighlight, value, true);
        return value.data;
    }

    private static Drawable createForegroundShape(float radius) {
        float[] radii = new float[8];
        Arrays.fill(radii, radius);
        RoundRectShape shape = new RoundRectShape(radii, null, null);
        return new ShapeDrawable(shape);
    }

    @Nullable
    private static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable == null) return null;

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.setTint(Color.WHITE);
        drawable.draw(canvas);

        return bitmap;
    }

    public interface SimpleAdapterListener<T> extends InteractiveAdapter.AdapterListener {
        void onItemClicked(T item);
    }
}
