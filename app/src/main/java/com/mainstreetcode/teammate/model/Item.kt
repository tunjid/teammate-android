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

import android.text.InputType
import android.text.TextUtils

import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.R
import com.tunjid.androidbootstrap.functions.Supplier
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

import java.lang.annotation.Retention

import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.arch.core.util.Function

import java.lang.annotation.RetentionPolicy.SOURCE

/**
 * Item for listing properties of a [Model]
 */
class Item<T> internal constructor(
        private val id: String?,
        val sortPosition: Int,
        val inputType: Int,
        @field:ItemType val itemType: Int,
        @field:StringRes val stringRes: Int,
        private var value: CharSequence?,
        private val changeCallBack: ValueChangeCallBack?,
        val itemizedObject: T) : Differentiable, Comparable<Item<*>> {
    private var textTransformer: Function<CharSequence, CharSequence>? = null

    val rawValue: String
        get() = value!!.toString()

    @Retention(SOURCE)
    @IntDef(INPUT, IMAGE, ROLE, DATE, CITY, LOCATION, INFO, TEXT, NUMBER, SPORT, VISIBILITY)
    internal annotation class ItemType

    fun setValue(value: CharSequence) {
        this.value = value
        changeCallBack?.onValueChanged(value.toString())
    }

    fun textTransformer(textTransformer: Function<CharSequence, CharSequence>): Item<T> {
        this.textTransformer = textTransformer
        return this
    }

    fun getValue(): CharSequence? =
            if (textTransformer == null) value else textTransformer!!.apply(value)

    override fun getId(): String? = id

    override fun compareTo(other: Item<*>): Int = sortPosition.compareTo(other.sortPosition)

    override fun areContentsTheSame(other: Differentiable): Boolean =
            if (other is Item<*>) value == other.value else id == other.id

    override fun getChangePayload(other: Differentiable?): Any? = other

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is Item<*>) return false

        val item = o as Item<*>?

        return if (id != null) id == item!!.id else item!!.id == null
    }

    override fun hashCode(): Int = id!!.hashCode()

    // Used to change the value of the Team's fields
    interface ValueChangeCallBack {
        fun onValueChanged(value: String)
    }

    companion object {

        const val INPUT = 2
        const val IMAGE = 3
        const val ROLE = 4
        const val DATE = 5

        const val CITY = 6
        const val ZIP = 8
        const val STATE = 7
        const val SPORT = 13

        const val LOCATION = 9

        const val INFO = 10
        const val TEXT = 11
        const val NUMBER = 12
        const val DESCRIPTION = 14
        const val VISIBILITY = 15
        const val ABOUT = 16
        const val NICKNAME = 17
        const val STAT_TYPE = 18
        const val TOURNAMENT_TYPE = 19
        const val TOURNAMENT_STYLE = 20
        const val COMPETITOR = 21

        val NO_CLICK: (() -> Unit)? = null
        val EMPTY_CLICK = { }

        val NO_ICON = { _: Item<*> -> 0 }

        val TRUE = { _: Item<*> -> true }
        val FALSE = { _: Item<*> -> false }

        val ALL_INPUT_VALID = { _: Item<*> -> "" }
        val NON_EMPTY = { input: Item<*> -> if (TextUtils.isEmpty(input.getValue())) App.getInstance().getString(R.string.team_invalid_empty_field) else "" }

        fun <T> ignore(ignored: T) {}

        fun <T> number(id: String, sortPosition: Int, itemType: Int, stringRes: Int,
                       supplier: Supplier<CharSequence>, changeCallBack: ValueChangeCallBack?,
                       itemizedObject: T): Item<T> =
                Item(id, sortPosition, InputType.TYPE_CLASS_NUMBER, itemType, stringRes, supplier.get(), changeCallBack, itemizedObject)

        fun <T> text(id: String, sortPosition: Int, itemType: Int, stringRes: Int,
                     supplier: Supplier<CharSequence>, changeCallBack: ValueChangeCallBack?,
                     itemizedObject: T): Item<T> =
                Item(id, sortPosition, InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE, itemType, stringRes, supplier.get(), changeCallBack, itemizedObject)

        fun <T> email(id: String, sortPosition: Int, itemType: Int, stringRes: Int,
                      supplier: Supplier<CharSequence>, changeCallBack: ValueChangeCallBack?,
                      itemizedObject: T): Item<T> =
                Item(id, sortPosition, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, itemType, stringRes, supplier.get(), changeCallBack, itemizedObject)

        fun nullToEmpty(source: CharSequence?): Supplier<CharSequence> {
            val finalSource = source ?: ""
            return Supplier { finalSource }
        }
    }
}
