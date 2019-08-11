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


import android.annotation.SuppressLint
import android.os.Parcel
import android.text.TextUtils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.mainstreetcode.teammate.util.ModelUtils

import java.lang.reflect.Type

@SuppressLint("ParcelCreator")
class Device : Model<Device> {

    var fcmToken = ""
    private var id = ""
    private val operatingSystem = "Android"

    override val isEmpty: Boolean
        get() = TextUtils.isEmpty(id)

    override val imageUrl: String
        get() = ""

    private constructor()

    constructor(id: String) {
        this.id = id
    }

    fun setFcmToken(fcmToken: String): Device {
        this.fcmToken = fcmToken
        return this
    }

    override fun update(updated: Device) {
        this.id = updated.id
        this.fcmToken = updated.fcmToken
    }

    override fun compareTo(other: Device): Int = id.compareTo(other.id)

    override fun getId(): String = id

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {}

    class GsonAdapter : JsonSerializer<Device>, JsonDeserializer<Device> {

        override fun serialize(src: Device, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val serialized = JsonObject()

            serialized.addProperty(FCM_TOKEN_KEY, src.fcmToken)
            serialized.addProperty(OPERATING_SYSTEM_KEY, src.operatingSystem)

            return serialized
        }

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Device {

            val deviceJson = json.asJsonObject

            val id = ModelUtils.asString(ID_KEY, deviceJson)
            val fcmToken = ModelUtils.asString(FCM_TOKEN_KEY, deviceJson)

            val device = Device(id)
            device.setFcmToken(fcmToken)

            return device
        }

        companion object {

            private const val ID_KEY = "_id"
            private const val FCM_TOKEN_KEY = "fcmToken"
            private const val OPERATING_SYSTEM_KEY = "operatingSystem"
        }
    }

    companion object {

        fun empty(): Device = Device()

        fun withFcmToken(fcmToken: String): Device {
            val device = Device()
            device.fcmToken = fcmToken
            return device
        }
    }
}
