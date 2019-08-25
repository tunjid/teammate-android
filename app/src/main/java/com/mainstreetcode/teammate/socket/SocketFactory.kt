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

package com.mainstreetcode.teammate.socket

import android.content.Context.MODE_PRIVATE
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.rest.TeammateService.API_BASE_URL
import com.mainstreetcode.teammate.rest.TeammateService.SESSION_COOKIE
import com.mainstreetcode.teammate.rest.TeammateService.SESSION_PREFS
import com.mainstreetcode.teammate.rest.TeammateService.getHttpClient
import com.mainstreetcode.teammate.util.Logger
import com.mainstreetcode.teammate.util.TeammateException
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import io.socket.client.IO
import io.socket.client.Manager.EVENT_OPEN
import io.socket.client.Manager.EVENT_TRANSPORT
import io.socket.client.Socket
import io.socket.client.Socket.EVENT_ERROR
import io.socket.client.Socket.EVENT_RECONNECT_ERROR
import io.socket.engineio.client.Transport
import io.socket.engineio.client.Transport.EVENT_REQUEST_HEADERS
import io.socket.engineio.client.transports.WebSocket
import java.net.URISyntaxException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference


class SocketFactory private constructor() {

    private val app = App.instance
    private val teamChatSocketRef = AtomicReference<Socket>()

    val teamChatSocket: Single<Socket>
        get() {
            val current = teamChatSocketRef.get()

            if (isConnected(current)) return Single.just(current)

            if (current != null) return Single.timer(1200, TimeUnit.MILLISECONDS).map {
                val delayed = teamChatSocketRef.get()
                val isConnected = isConnected(delayed)

                if (!isConnected) teamChatSocketRef.set(null)

                if (isConnected) return@map delayed
                else throw TeammateException(app.getString(R.string.error_socket))
            }

            val pending = buildTeamChatSocket()
                    ?: return Single.error(TeammateException(app.getString(R.string.error_socket)))

            val processor = PublishProcessor.create<Socket>()

            pending.io().once(EVENT_OPEN) {
                teamChatSocketRef.set(pending)
                processor.onNext(pending)
                processor.onComplete()
            }

            pending.once(EVENT_ERROR, this::onError)
            pending.connect()

            return processor.firstOrError()
        }

    private fun buildBaseSocket(): Socket? {
        var socket: Socket? = null
        val client = getHttpClient()

        IO.setDefaultOkHttpWebSocketFactory(client)
        IO.setDefaultOkHttpCallFactory(client)

        val options = IO.Options()
        options.secure = true
        options.forceNew = true
        options.reconnection = false
        options.transports = arrayOf(WebSocket.NAME)
        options.reconnectionAttempts = RECONNECTION_ATTEMPTS

        try {
            socket = IO.socket(API_BASE_URL, options)
        } catch (e: URISyntaxException) {
            Logger.log(TAG, "Unable to build Socket", e)
        }

        return socket
    }

    private fun buildTeamChatSocket(): Socket? {
        var socket = buildBaseSocket()

        if (socket != null) {
            socket = socket.io().socket(TEAM_CHAT_NAMESPACE)

            val manager = socket.io()
            manager.on(EVENT_TRANSPORT, this::routeTransportEvent)
            //            manager.on(EVENT_CLOSE, i -> onDisconnection());

            socket.on(EVENT_RECONNECT_ERROR, this::onReconnectionError)
            //socket.on(EVENT_RECONNECT_ATTEMPT, this::onReconnectionAttempt);
        }
        return socket
    }

    private fun routeTransportEvent(vararg transportArgs: Any) {
        val transport = transportArgs[0] as Transport
        transport.on(EVENT_REQUEST_HEADERS, this::routeHeaderRequestEvent)
    }

    private fun routeHeaderRequestEvent(vararg headerArgs: Any) {
        val preferences = app.getSharedPreferences(SESSION_PREFS, MODE_PRIVATE)
        val serializedCookie = preferences.getString(SESSION_COOKIE, "")

        if (serializedCookie.isNullOrBlank()) return

        @Suppress("UNCHECKED_CAST")
        val headers = headerArgs[0] as MutableMap<String, List<String>>

        // modify request headers
        headers[COOKIE] = listOf(serializedCookie)
    }

    //    private void onReconnectionAttempt(Object... args) {}

    private fun onReconnectionError(vararg args: Any) =
            Logger.log(TAG, "Reconnection Error", args[0] as Exception)

    private fun onError(vararg args: Any) {
        Logger.log(TAG, "Socket Error", args[0] as Exception)

        val socket = teamChatSocketRef.get()

        if (socket != null) {
            socket.off(EVENT_NEW_MESSAGE)
            socket.close()
        }

        teamChatSocketRef.set(null)
    }

    private fun isConnected(socket: Socket?): Boolean = socket != null && socket.connected()

    companion object {

        private const val RECONNECTION_ATTEMPTS = 3
        private const val TAG = "Socket.Factory"
        private const val COOKIE = "Cookie"
        private const val TEAM_CHAT_NAMESPACE = "/team-chat"
        const val EVENT_NEW_MESSAGE = "newMessage"
        const val EVENT_JOIN = "join"

        private lateinit var INSTANCE: SocketFactory

        val instance: SocketFactory
            get() {
                if (!::INSTANCE.isInitialized) INSTANCE = SocketFactory()
                return INSTANCE
            }
    }
}