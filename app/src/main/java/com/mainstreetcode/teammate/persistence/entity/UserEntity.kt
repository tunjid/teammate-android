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

package com.mainstreetcode.teammate.persistence.entity


import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.processEmoji

@Entity(tableName = "users")
open class UserEntity : Parcelable {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    private var id: String

    @ColumnInfo(name = "user_image_url")
    var imageUrl: String

    @ColumnInfo(name = "user_screen_name")
    var screenName: String

    @ColumnInfo(name = "user_primary_email")
    var primaryEmail: String

    @ColumnInfo(name = "user_first_name")
    var firstName: CharSequence
        get() = field.processEmoji()

    @ColumnInfo(name = "user_last_name")
    var lastName: CharSequence
        get() = field.processEmoji()

    @ColumnInfo(name = "user_about")
    var about: CharSequence
        get() = field.processEmoji()

    constructor(id: String, imageUrl: String, screenName: String, primaryEmail: String,
                firstName: CharSequence, lastName: CharSequence, about: CharSequence) {
        this.id = id
        this.imageUrl = imageUrl
        this.screenName = screenName
        this.primaryEmail = primaryEmail
        this.firstName = firstName
        this.lastName = lastName
        this.about = about
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false

        val user = other as User?

        return id == user?.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String =
            "com.mainstreetcode.teammates.model.User(id=" + this.id + ", firstName=" + this.firstName + ", lastName=" + this.lastName + ", primaryEmail=" + this.primaryEmail + ")"

    fun setFirstName(firstName: String) {
        this.firstName = firstName
    }

    fun setLastName(lastName: String) {
        this.lastName = lastName
    }

    fun setAbout(about: String) {
        this.about = about
    }

    protected constructor(`in`: Parcel) {
        id = `in`.readString()!!
        imageUrl = `in`.readString()!!
        screenName = `in`.readString()!!
        primaryEmail = `in`.readString()!!
        firstName = `in`.readString()!!
        lastName = `in`.readString()!!
        about = `in`.readString()!!
    }

    fun getId(): String = id

    protected fun setId(id: String) {
        this.id = id
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(imageUrl)
        dest.writeString(screenName)
        dest.writeString(primaryEmail)
        dest.writeString(firstName.toString())
        dest.writeString(lastName.toString())
        dest.writeString(about.toString())
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<UserEntity> = object : Parcelable.Creator<UserEntity> {
            override fun createFromParcel(`in`: Parcel): UserEntity = UserEntity(`in`)

            override fun newArray(size: Int): Array<UserEntity?> = arrayOfNulls(size)
        }
    }
}
