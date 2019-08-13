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

package com.mainstreetcode.teammate.util

import android.view.View

import com.mainstreetcode.teammate.R

import java.util.ArrayList

class SyncedScrollManager {

    private val clients = ArrayList<SyncedScrollView>(4)

    @Volatile
    private var isSyncing = false

    fun addScrollClient(client: SyncedScrollView) {
        clients.add(client)
        client.setScrollManager(this)
    }

    fun clearClients() = clients.clear()

    fun jog() {
        if (clients.isEmpty()) return
        val scrollView = clients[0]
        val current = scrollView.scrollX
        val amount = scrollView.resources.getDimensionPixelSize(R.dimen.triple_and_half_margin)
        scrollView.postDelayed({ scrollView.smoothScrollTo(current + amount, 0) }, 500)
        scrollView.postDelayed({ scrollView.smoothScrollTo(0, 0) }, 1000)
    }

    fun onScrollChanged(sender: View, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        if (isSyncing) return

        isSyncing = true

        val scrollType: Int
        when {
            scrollX != oldScrollX -> scrollType = SCROLL_HORIZONTAL
            scrollY != oldScrollY -> scrollType = SCROLL_VERTICAL
            else -> {
                isSyncing = false
                return
            }
        }

        // update clients
        for (client in clients) {
            if (client == sender) continue
            if (scrollType == SCROLL_HORIZONTAL) client.scrollTo(scrollX, scrollY)
        }

        isSyncing = false
    }

    companion object {

        private const val SCROLL_HORIZONTAL = 1
        private const val SCROLL_VERTICAL = 2
    }

}
