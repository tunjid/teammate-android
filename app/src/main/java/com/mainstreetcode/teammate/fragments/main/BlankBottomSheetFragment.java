package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;

public class BlankBottomSheetFragment extends MainActivityFragment {

    public static BlankBottomSheetFragment newInstance() {
        BlankBottomSheetFragment bottomSheetFragment = new BlankBottomSheetFragment();
        bottomSheetFragment.setArguments(new Bundle());

        return bottomSheetFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_blank_bottom, container, false);
    }

    @Override
    public void togglePersistentUi() {}

    @Override
    protected void toggleSystemUI(boolean show) {}

    @Override
    protected void toggleToolbar(boolean show) {}

    @Override
    protected void toggleAltToolbar(boolean show) {}

    @Override
    protected void toggleBottombar(boolean show) {}

    @Override
    protected void toggleFab(boolean show) {}

    @Override
    protected void toggleProgress(boolean show) {}
}

