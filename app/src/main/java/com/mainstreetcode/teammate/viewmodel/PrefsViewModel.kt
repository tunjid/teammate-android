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

package com.mainstreetcode.teammate.viewmodel

import android.content.res.Configuration
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.Prefs
import com.mainstreetcode.teammate.repository.PrefsRepo
import com.mainstreetcode.teammate.repository.RepoProvider

class PrefsViewModel : BaseViewModel() {

    private val prefsRepository = RepoProvider.forRepo(PrefsRepo::class.java)
    private val prefs: Prefs = prefsRepository.current

    private val nightModes = intArrayOf(MODE_NIGHT_NO, MODE_NIGHT_YES, if (SDK_INT >= Q) MODE_NIGHT_FOLLOW_SYSTEM else MODE_NIGHT_AUTO_BATTERY)

    val nightUiMode: Int
        get() = prefs.nightUiMode

    val checkedIndex: Int
        get() = nightModes.indexOf(nightUiMode)

    val isInDarkMode: Boolean
        get() = when (nightUiMode) {
            MODE_NIGHT_NO -> false
            MODE_NIGHT_YES -> true
            MODE_NIGHT_FOLLOW_SYSTEM -> when (App.instance.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO,
                Configuration.UI_MODE_NIGHT_UNDEFINED -> false
                else -> true
            }
            MODE_NIGHT_AUTO_BATTERY -> true
            else -> false
        }

    var isOnBoarded: Boolean
        get() = prefs.isOnBoarded
        set(value) = updatePrefs { isOnBoarded = value }

    val themeOptions: Array<CharSequence>
        get() = App.instance.run {
            nightModes.map {
                getString(when (it) {
                    MODE_NIGHT_NO -> R.string.settings_light_theme
                    MODE_NIGHT_YES -> R.string.settings_dark_theme
                    MODE_NIGHT_FOLLOW_SYSTEM -> R.string.settings_system_theme
                    MODE_NIGHT_AUTO_BATTERY -> R.string.settings_battery_saver_theme
                    else -> R.string.empty_string
                })
            }.toTypedArray()
        }

    fun onThemeSelected(index: Int) = updateNightMode(nightModes[index])

    private fun updatePrefs(updater: Prefs.() -> Unit) {
        updater.invoke(prefs)
        prefsRepository.createOrUpdate(prefs)
    }

    private fun updateNightMode(nightUiMode: Int) {
        AppCompatDelegate.setDefaultNightMode(nightUiMode)
        updatePrefs { this.nightUiMode = nightUiMode }
    }
}
