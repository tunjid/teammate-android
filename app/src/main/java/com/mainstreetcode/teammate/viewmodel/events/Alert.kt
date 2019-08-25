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

package com.mainstreetcode.teammate.viewmodel.events

import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.model.Model

abstract class Alert<T : Model<T>> private constructor(val model: T) {

    class Creation<T : Model<T>> internal constructor(model: T) : Alert<T>(model)

    class Deletion<T : Model<T>> internal constructor(model: T) : Alert<T>(model)

    class EventAbsentee internal constructor(model: Event) : Alert<Event>(model)

    class JoinRequestProcessed internal constructor(model: JoinRequest) : Alert<JoinRequest>(model)

    class Capsule<A : Alert<*>, T : Model<in T>> internal constructor(
            internal var alertClass: Class<in A>,
            internal var modelClass: Class<in T>,
            internal var consumer: (T) -> Unit
    )

    companion object {

        fun <T : Model<T>> creation(model: T): Creation<T> = Creation(model)

        fun <T : Model<T>> deletion(model: T): Deletion<T> = Deletion(model)

        fun eventAbsentee(event: Event): Alert<Event> = EventAbsentee(event)

        fun requestProcessed(joinRequest: JoinRequest): Alert<JoinRequest> =
                JoinRequestProcessed(joinRequest)

        fun <A : Alert<*>, T : Model<T>> of(
                alertClass: Class<in A>,
                modelClass: Class<in T>,
                consumer: (T) -> Unit
        ): Capsule<A, T> = Capsule(alertClass, modelClass, consumer)

    }
}

fun Alert<*>.matches(vararg testers: Alert.Capsule<*, *>) {
    for (capsule in testers) {
        if (javaClass != capsule.alertClass) continue

        val model = model

        @Suppress("UNCHECKED_CAST")
        if (model.javaClass == capsule.modelClass) return (capsule.consumer as (Any) -> Unit).invoke(model)
    }
}