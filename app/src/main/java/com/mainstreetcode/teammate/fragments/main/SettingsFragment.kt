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
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.activities.MainActivity
import com.mainstreetcode.teammate.adapters.SettingsAdapter
import com.mainstreetcode.teammate.adapters.viewholders.SettingsViewHolder
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.model.SettingsItem
import com.mainstreetcode.teammate.util.ScrollManager

class SettingsFragment : MainActivityFragment(), SettingsAdapter.SettingsAdapterListener {

    override val toolbarMenu: Int get() = R.menu.fragment_settings

    override val toolbarTitle: CharSequence get() = getString(R.string.settings)

    override val showsFab: Boolean get() = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)

        scrollManager = ScrollManager.with<SettingsViewHolder>(rootView.findViewById(R.id.settings_list))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(SettingsAdapter(items, this))
                .withLinearLayoutManager()
                .build()

        return rootView
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
            val activity = requireActivity()
            MainActivity.startRegistrationActivity(activity)
            activity.finish()
        }, defaultErrorHandler::invoke))
    }

    override fun onSettingsItemClicked(item: SettingsItem) {
        when (item.stringRes) {
            R.string.sign_out -> signOut()
            R.string.show_on_boarding -> {
                prefsViewModel.isOnBoarded = false
                showFragment(FeedFragment.newInstance())
            }
            R.string.my_profile -> showFragment(UserEditFragment.newInstance(userViewModel.currentUser))
        }
    }

    companion object {

        private val items = listOf(
                SettingsItem(R.string.show_on_boarding, R.drawable.ic_teach_24dp),
                SettingsItem(R.string.sign_out, R.drawable.ic_logout_white_24dp)
        )

        fun newInstance(): SettingsFragment = SettingsFragment().apply { arguments = Bundle() }
    }
}
