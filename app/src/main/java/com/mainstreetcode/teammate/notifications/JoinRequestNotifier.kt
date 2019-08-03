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

package com.mainstreetcode.teammate.notifications


import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.repository.ModelRepo
import com.mainstreetcode.teammate.repository.RepoProvider


class JoinRequestNotifier internal constructor() : Notifier<JoinRequest>() {

    override val notifyId: String
        get() = FeedItem.JOIN_REQUEST

    override val repository: ModelRepo<JoinRequest>
        get() = RepoProvider.forModel(JoinRequest::class.java)

    override val notificationChannels: Array<NotificationChannel>?
        @TargetApi(Build.VERSION_CODES.O)
        get() = arrayOf(buildNotificationChannel(FeedItem.JOIN_REQUEST, R.string.join_requests, R.string.join_requests_notifier_description, NotificationManager.IMPORTANCE_DEFAULT))
}
