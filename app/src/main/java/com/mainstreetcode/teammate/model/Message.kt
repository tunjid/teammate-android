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

package com.mainstreetcode.teammate.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.rest.TeammateService
import com.mainstreetcode.teammate.util.Logger
import com.mainstreetcode.teammate.util.asStringOrEmpty
import io.reactivex.exceptions.CompositeException
import retrofit2.HttpException
import java.lang.reflect.Type
import java.nio.charset.Charset
import java.util.*

/**
 * Messages from the [com.mainstreetcode.teammate.rest.TeammateApi]
 */

class Message {

    val message: String
    private val errorCode: String

    val isValidModel: Boolean
        get() = !isIllegalTeamMember && !isInvalidObject

    val isInvalidObject: Boolean
        get() = INVALID_OBJECT_REFERENCE_ERROR_CODE == errorCode

    val isIllegalTeamMember: Boolean
        get() = ILLEGAL_TEAM_MEMBER_ERROR_CODE == errorCode

    val isUnauthorizedUser: Boolean
        get() = UNAUTHENTICATED_USER_ERROR_CODE == errorCode

    val isAtMaxStorage: Boolean
        get() = MAX_STORAGE_ERROR_CODE == errorCode

    constructor(message: String?) {
        this.message = message ?: ""
        this.errorCode = UNKNOWN_ERROR_CODE
    }

    private constructor(message: String, errorCode: String) {
        this.message = message
        this.errorCode = errorCode
    }

    constructor(exception: HttpException) {
        val parsed = getMessage(exception)
        this.message = parsed.message
        this.errorCode = parsed.errorCode
    }

    private fun getMessage(throwable: HttpException): Message {
        try {
            val errorBody = throwable.response()!!.errorBody()
            if (errorBody != null) {
                val source = errorBody.source()
                source.request(java.lang.Long.MAX_VALUE) // request the entire body.
                val buffer = source.buffer()
                // clone buffer before reading from it
                val json = buffer.clone().readString(Charset.forName("UTF-8"))
                return TeammateService.getGson().fromJson(json, Message::class.java)
            }
        } catch (e: Exception) {
            Logger.log("ApiMessage", "Unable to read API error message", e)
        }

        return Message(App.instance.getString(R.string.error_default))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val casted = other as Message?
        return message == casted?.message && errorCode == casted.errorCode
    }

    override fun hashCode(): Int {
        return Objects.hash(message, errorCode)
    }

    class GsonAdapter : com.google.gson.JsonDeserializer<Message> {

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Message {
            val messageJson = json.asJsonObject
            val message = if (messageJson.has(MESSAGE_KEY)) messageJson.get(MESSAGE_KEY).asString else "Sorry, an error occurred"
            val errorCode = messageJson.asStringOrEmpty(ERROR_CODE_KEY)

            return Message(message, errorCode)
        }

        companion object {

            private const val MESSAGE_KEY = "message"
            private const val ERROR_CODE_KEY = "errorCode"
        }

    }

    companion object {

        private const val UNKNOWN_ERROR_CODE = "unknown.error"
        private const val MAX_STORAGE_ERROR_CODE = "maximum.storage.error"
        private const val ILLEGAL_TEAM_MEMBER_ERROR_CODE = "illegal.team.member.error"
        private const val UNAUTHENTICATED_USER_ERROR_CODE = "unauthenticated.user.error"
        private const val INVALID_OBJECT_REFERENCE_ERROR_CODE = "invalid.object.reference.error"
    }
}

fun Throwable.toMessage(): Message? {
    if (this is HttpException) {
        return Message(this)
    } else if (this is CompositeException) {
        val httpException = exceptions.filterIsInstance(HttpException::class.java).firstOrNull()
        if (httpException != null) return Message(httpException)
    }
    return null
}