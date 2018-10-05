package com.mainstreetcode.teammate.util;

public class AppBarListener {

    private int lastOffset;
    private final ModelUtils.Consumer<Integer> offsetDiffListener;

    private AppBarListener(ModelUtils.Consumer<Integer> offsetDiffListener) {this.offsetDiffListener = offsetDiffListener;}

    public static AppBarListener with(ModelUtils.Consumer<Integer> offsetDiffListener) {
        return new AppBarListener(offsetDiffListener);
    }

    public void onOffsetChanged(int newOffset) {
        int dy =  lastOffset - newOffset;
        lastOffset = newOffset;
        offsetDiffListener.accept(dy);
    }
}
