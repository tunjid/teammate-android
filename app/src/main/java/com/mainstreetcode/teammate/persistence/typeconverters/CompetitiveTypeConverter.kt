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

package com.mainstreetcode.teammate.persistence.typeconverters

import androidx.room.TypeConverter
import com.mainstreetcode.teammate.model.Competitive
import com.mainstreetcode.teammate.model.EmptyCompetitor
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User

class CompetitiveTypeConverter {

    private val userTypeConverter = UserTypeConverter()
    private val teamTypeConverter = TeamTypeConverter()

    @TypeConverter
    fun toDbValue(competitive: Competitive?): String =
            if (competitive == null) "" else competitive.refType + "," + competitive.id

    @TypeConverter
    fun fromId(source: String): Competitive {
        if (source.isBlank()) return EmptyCompetitor()
        val split = source.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val type = split[0]
        val id = split[1]

        return when (type) {
            User.COMPETITOR_TYPE -> userTypeConverter.fromId(id)
            Team.COMPETITOR_TYPE -> teamTypeConverter.fromId(id)
            else -> EmptyCompetitor()
        }
    }
}
