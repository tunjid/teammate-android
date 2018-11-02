package com.mainstreetcode.teammate.util;


import android.app.Activity;

import androidx.arch.core.util.Function;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.google.android.material.ripple.RippleUtils;
import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.view.util.ViewUtil;

import java.util.Arrays;

import io.reactivex.Single;

public class ViewHolderUtil extends ViewUtil {

    public static final int USER = 284;
    public static final int TEAM = 285;
    public static final int CHAT = 286;
    public static final int ROLE = 287;
    public static final int GAME = 288;
    public static final int HOME = 289;
    public static final int AWAY = 290;
    public static final int STAT = 291;
    public static final int EVENT = 292;
    public static final int FEED_ITEM = 293;
    public static final int TOURNAMENT = 294;
    public static final int CONTENT_AD = 295;
    public static final int INSTALL_AD = 296;
    public static final int MEDIA_IMAGE = 297;
    public static final int MEDIA_VIDEO = 298;
    public static final int JOIN_REQUEST = 299;
    public static final int BLOCKED_USER = 300;
    public static final int THUMBNAIL_SIZE = 250;
    public static final int FULL_RES_LOAD_DELAY = 200;
    private static final int DEFAULT_STROKE_VALUE = -1;

    public static Function<CharSequence, CharSequence> allowsSpecialCharacters =
            input -> ModelUtils.hasNoSpecialChars(input) ? "" : App.getInstance().getResources().getString(R.string.no_special_characters);

    public static View getItemView(@LayoutRes int res, ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(res, parent, false);
    }

    public static String getTransitionName(Object item, @IdRes int id) {
        return item.hashCode() + "-" + id;
    }

    @Nullable
    public static Activity getActivity(RecyclerView.ViewHolder viewHolder) {
        Context context = viewHolder.itemView.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) return (Activity) context;
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public static void listenForLayout(View view, Runnable onLayout) {
        ViewTreeObserver observer = view.getViewTreeObserver();
        if (!observer.isAlive()) return;
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override public void onGlobalLayout() {
                onLayout.run();
                if (observer.isAlive()) observer.removeOnGlobalLayoutListener(this);
                ViewTreeObserver current = view.getViewTreeObserver();
                if (current.isAlive()) current.removeOnGlobalLayoutListener(this);
            }
        });
    }

    public static Bitmap loadBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public static Single<Drawable> fetchRoundedDrawable(Context context, String url, int size) {
        return Single.create(emitter -> Picasso.with(context).load(url).resize(size, size).centerCrop()
                .into(new Target() {
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
                        imageDrawable.setCircular(true);
                        imageDrawable.setCornerRadius(size);
                        emitter.onSuccess(imageDrawable);
                    }

                    public void onBitmapFailed(Drawable errorDrawable) { emitter.onError(new TeammateException("failed")); }

                    public void onPrepareLoad(Drawable placeHolderDrawable) { }
                }));
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

    public interface SimpleAdapterListener<T> extends InteractiveAdapter.AdapterListener {
        void onItemClicked(T item);
    }
}
