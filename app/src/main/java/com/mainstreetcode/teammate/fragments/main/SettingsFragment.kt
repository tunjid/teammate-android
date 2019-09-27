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

package com.mainstreetcode.teammate.fragments.main

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.activities.signOut
import com.mainstreetcode.teammate.adapters.SettingsAdapter
import com.mainstreetcode.teammate.adapters.viewholders.SettingsViewHolder
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.model.SettingsItem
import com.mainstreetcode.teammate.util.ScrollManager

class SettingsFragment : MainActivityFragment(R.layout.fragment_settings),
        SettingsAdapter.SettingsAdapterListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultUi(
                toolbarTitle = getString(R.string.settings),
                toolBarMenu = R.menu.fragment_settings
        )
        scrollManager = ScrollManager.with<SettingsViewHolder>(view.findViewById(R.id.settings_list))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(SettingsAdapter(items, this))
                .withLinearLayoutManager()
                .build()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when {
        item.itemId != R.id.action_delete_account -> super.onOptionsItemSelected(item)
        else -> AlertDialog.Builder(requireContext()).setTitle(getString(R.string.delete_user_prompt))
                .setMessage(R.string.delete_user_prompt_body)
                .setPositiveButton(R.string.yes) { _, _ -> deleteAccount() }
                .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                .show().let { true }
    }

    private fun deleteAccount() {
        disposables.add(userViewModel.deleteAccount().subscribe({
            navigator.signOut()
        }, defaultErrorHandler::invoke))
    }

    override fun onSettingsItemClicked(item: SettingsItem) {
        when (item.stringRes) {
            R.string.sign_out -> signOut()
            R.string.settings_set_theme -> AlertDialog.Builder(requireContext())
                    .setSingleChoiceItems(prefsViewModel.themeOptions, prefsViewModel.checkedIndex) { dialog, index ->
                        prefsViewModel.onThemeSelected(index)
                        dialog.dismiss()
                    }
                    .show()
            R.string.show_on_boarding -> {
                prefsViewModel.isOnBoarded = false
                navigator.show(FeedFragment.newInstance())
            }
        }
    }

    companion object {

        private val items = listOf(
                SettingsItem(R.string.show_on_boarding, R.drawable.ic_teach_24dp),
                SettingsItem(R.string.settings_set_theme, R.drawable.ic_theme_24dp),
                SettingsItem(R.string.sign_out, R.drawable.ic_logout_white_24dp)
        )

        fun newInstance(): SettingsFragment = SettingsFragment().apply { arguments = Bundle() }
    }
}
