package com.mainstreetcode.teammate.util;

import android.view.View;

import com.mainstreetcode.teammate.R;

import java.util.ArrayList;
import java.util.List;

public class SyncedScrollManager {

    private static final int SCROLL_HORIZONTAL = 1;
    private static final int SCROLL_VERTICAL = 2;

    private List<SyncedScrollView> clients = new ArrayList<>(4);

    private volatile boolean isSyncing = false;

    public void addScrollClient(SyncedScrollView client) {
        clients.add(client);
        client.setScrollManager(this);
    }

    public void clearClients() {
        clients.clear();
    }

    public void jog() {
        if (clients.isEmpty()) return;
        SyncedScrollView scrollView = clients.get(0);
        int current = scrollView.getScrollX();
        int amount = scrollView.getResources().getDimensionPixelSize(R.dimen.triple_and_half_margin);
        scrollView.postDelayed(() -> scrollView.smoothScrollTo(current + amount, 0), 500);
        scrollView.postDelayed(() -> scrollView.smoothScrollTo(0, 0), 1000);
    }

    public void onScrollChanged(View sender, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (isSyncing) return;

        isSyncing = true;

        int scrollType;
        if (scrollX != oldScrollX) scrollType = SCROLL_HORIZONTAL;
        else if (scrollY != oldScrollY) scrollType = SCROLL_VERTICAL;
        else {
            isSyncing = false;
            return;
        }

        // update clients
        for (SyncedScrollView client : clients) {
            if (client == sender) continue;
            if (scrollType == SCROLL_HORIZONTAL) client.scrollTo(scrollX, scrollY);
        }

        isSyncing = false;
    }

}
