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

package com.mainstreetcode.teammate.model.enums

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.ModelUtils
import com.mainstreetcode.teammate.util.ModelUtils.replaceList
import com.mainstreetcode.teammate.util.ModelUtils.replaceStringList
import com.tunjid.androidbootstrap.core.text.SpanBuilder

class Sport private constructor(code: String, name: String, private var emoji: String?) : MetaData(code, name) {
    val stats = StatTypes()
    private val tournamentTypes = mutableListOf<String>()
    private val tournamentStyles = mutableListOf<String>()

    fun supportsCompetitions(): Boolean = tournamentStyles.isNotEmpty()

    fun supportsTournamentType(type: TournamentType): Boolean = tournamentTypes.contains(type.code)

    fun supportsTournamentStyle(style: TournamentStyle): Boolean =
            tournamentStyles.contains(style.code)

    fun betweenUsers(): Boolean = User.COMPETITOR_TYPE == refType()

    fun refType(): String =
            if (tournamentTypes.isEmpty()) "" else Config.tournamentTypeFromCode(tournamentTypes[0]).refPath

    fun defaultTournamentType(): TournamentType {
        return if (tournamentTypes.isEmpty())
            TournamentType.empty()
        else
            Config.tournamentTypeFromCode(tournamentTypes[0])
    }

    fun defaultTournamentStyle(): TournamentStyle {
        return if (tournamentStyles.isEmpty())
            TournamentStyle.empty()
        else
            Config.tournamentStyleFromCode(tournamentStyles[0])
    }

    fun statTypeFromCode(code: String): StatType = stats.fromCodeOrFirst(code)

    override fun getName(): CharSequence = appendEmoji(name)

    fun getEmoji(): CharSequence = ModelUtils.processString(emoji)

    fun appendEmoji(text: CharSequence): CharSequence =
            SpanBuilder.of(getEmoji()).append("   ").append(text).build()

    fun update(updated: Sport) {
        var source = updated
        if (source.stats.isEmpty() || source.tournamentTypes.isEmpty() || source.tournamentStyles.isEmpty())
            source = Config.sportFromCode(source.code)

        if (this === source) return

        super.update(source)
        this.emoji = source.emoji
        replaceList(this.stats, source.stats)
        replaceStringList(this.tournamentTypes, source.tournamentTypes)
        replaceStringList(this.tournamentStyles, source.tournamentStyles)
    }

    class GsonAdapter : MetaData.GsonAdapter<Sport>() {

        override fun fromJson(code: String, name: String, body: JsonObject, context: JsonDeserializationContext): Sport {
            val emoji = ModelUtils.asString(EMOJI, body)

            val sport = Sport(code, name, emoji)
            ModelUtils.deserializeList(context, body.get(STAT_TYPES), sport.stats, StatType::class.java)
            ModelUtils.deserializeList(context, body.get(TOURNAMENT_TYPES), sport.tournamentTypes, String::class.java)
            ModelUtils.deserializeList(context, body.get(TOURNAMENT_STYLES), sport.tournamentStyles, String::class.java)

            return sport
        }

        override fun toJson(serialized: JsonObject, src: Sport, context: JsonSerializationContext): JsonObject {
            serialized.addProperty(EMOJI, src.emoji)
            serialized.add(STAT_TYPES, context.serialize(src.stats))
            serialized.add(TOURNAMENT_TYPES, context.serialize(src.tournamentTypes))
            serialized.add(TOURNAMENT_STYLES, context.serialize(src.tournamentStyles))
            return serialized
        }

        companion object {

            private const val EMOJI = "emoji"
            private const val STAT_TYPES = "statTypes"
            private const val TOURNAMENT_TYPES = "tournamentTypes"
            private const val TOURNAMENT_STYLES = "tournamentStyles"
        }
    }

    companion object {

        private const val THONK = "\uD83E\uDD14"

        fun empty(): Sport = Sport("", App.getInstance().getString(R.string.any_sport), THONK)
    }
}


