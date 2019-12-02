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


import com.mainstreetcode.teammate.model.Message
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.TeamHost
import com.mainstreetcode.teammate.viewmodel.events.Alert
import com.mainstreetcode.teammate.viewmodel.events.matches
import com.tunjid.androidx.recyclerview.diff.Differentiable
import java.util.*

abstract class TeamMappedViewModel<V> : MappedViewModel<Team, V>() where V : Differentiable, V : TeamHost {

    internal val modelListMap: MutableMap<String, MutableList<Differentiable>> = HashMap()

    internal val allModels: List<Differentiable>
        get() = modelListMap.values.flatten()

    override fun onModelAlert(alert: Alert<*>) {
        super.onModelAlert(alert)

        alert.matches(Alert.of(Alert.Deletion::class.java, Team::class.java) { modelListMap.remove(it.id) })
    }

    override fun getModelList(key: Team): MutableList<Differentiable> =
            modelListMap.getOrPut(key.id) { mutableListOf() }

    override fun onErrorMessage(message: Message, key: Team, invalid: Differentiable) {
        super.onErrorMessage(message, key, invalid)
        if (message.isIllegalTeamMember) pushModelAlert(Alert.deletion(key))
    }

    internal fun onError(model: V): (Throwable) -> Unit =
            { throwable -> checkForInvalidObject(throwable, model, model.team) }

    override fun onInvalidKey(key: Team) {
        super.onInvalidKey(key)
        pushModelAlert(Alert.deletion(key))
    }
}
