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

import android.content.Context
import android.net.ConnectivityManager
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.Message
import com.mainstreetcode.teammate.model.toMessage
import java.net.UnknownHostException
import java.util.*

/**
 * A Error handler
 */

open class ErrorHandler private constructor(
        private val defaultMessage: String?,
        private val messageConsumer: ((Message) -> Unit)?,
        private val messageMap: Map<String, String>?) : (Throwable) -> Unit {

    private val actions: MutableList<() -> Unit> = mutableListOf()

    private val isOffline: Boolean
        get() {
            val app = App.instance

            val connectivityManager = app.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                    ?: return true

            val netInfo = connectivityManager.activeNetworkInfo
            return netInfo == null || !netInfo.isConnectedOrConnecting
        }

    override fun invoke(throwable: Throwable) {
        Logger.log(TAG, ERROR_HANDLER_RECEIVED_ERROR, throwable)

        val key = throwable.javaClass.name
        val map = messageMap

        val message =
                if (map!= null && map.containsKey(key)) Message(map[key])
                else if (isTeammateException(throwable)) Message(throwable.message)
                else parseMessage(throwable)

        try {
            messageConsumer?.invoke(message)
            for (action in actions) action.invoke()
        } catch (e: Exception) {
            Logger.log(TAG, ERROR_HANDLER_DISPATCH_ERROR, e)
        }

    }

    fun addAction(action: () -> Unit) {
        actions.add(action)
    }

    private fun isTeammateException(throwable: Throwable): Boolean {
        return throwable is TeammateException
    }

    private fun parseMessage(throwable: Throwable): Message {
        val message = throwable.toMessage()
        if (message != null) return message

        val app = App.instance

        return if (isOffline || throwable is UnknownHostException) Message(app.getString(R.string.error_network)) else Message(defaultMessage)

    }

    class Builder {
        private var defaultMessage: String? = null
        private var messageConsumer: ((Message) -> Unit)? = null
        private var messageMap: Map<String, String> = HashMap()

        fun defaultMessage(errorMessage: String): Builder {
            defaultMessage = errorMessage
            return this
        }

        fun add(messageConsumer: (Message) -> Unit): Builder {
            this.messageConsumer = messageConsumer
            return this
        }

        fun build(): ErrorHandler {
            if (defaultMessage == null) {
                throw IllegalArgumentException("No default message provided")
            }
            if (messageConsumer == null) {
                throw IllegalArgumentException("No Consumer provided for error message")
            }
            return ErrorHandler(defaultMessage!!, messageConsumer!!, messageMap)
        }
    }

    companion object {

        private const val TAG = "ErrorHandler"
        private const val ERROR_HANDLER_RECEIVED_ERROR = "Received Error"
        private const val ERROR_HANDLER_DISPATCH_ERROR = "Dispatch Error to callback"

        fun builder(): Builder {
            return Builder()
        }

        val EMPTY: ErrorHandler = object : ErrorHandler(null, null, null) {
            override fun invoke(throwable: Throwable) = Logger.log(TAG, ERROR_HANDLER_RECEIVED_ERROR, throwable)
        }
    }
}