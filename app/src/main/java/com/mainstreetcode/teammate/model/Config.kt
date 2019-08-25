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
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.mainstreetcode.teammate.model.enums.AndroidVariant
import com.mainstreetcode.teammate.model.enums.BlockReason
import com.mainstreetcode.teammate.model.enums.MetaData
import com.mainstreetcode.teammate.model.enums.Position
import com.mainstreetcode.teammate.model.enums.Sport
import com.mainstreetcode.teammate.model.enums.StatType
import com.mainstreetcode.teammate.model.enums.TournamentStyle
import com.mainstreetcode.teammate.model.enums.TournamentType
import com.mainstreetcode.teammate.model.enums.Visibility
import com.mainstreetcode.teammate.persistence.AppDatabase
import com.mainstreetcode.teammate.repository.ConfigRepo
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.asStringOrEmpty
import com.mainstreetcode.teammate.util.deserializeList
import com.mainstreetcode.teammate.util.replaceList
import com.mainstreetcode.teammate.util.replaceStringList
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import java.lang.reflect.Type

@SuppressLint("ParcelCreator")
class Config internal constructor(
        private var defaultTeamLogo: String,
        private var defaultEventLogo: String,
        private var defaultUserAvatar: String,
        private val defaultTournamentLogo: String
) : Model<Config> {
    private val sports = mutableListOf<Sport>()
    private val privileged = mutableListOf<String>()
    private val positions = mutableListOf<Position>()
    private val statTypes = mutableListOf<StatType>()
    private val visibilities = mutableListOf<Visibility>()
    private val blockReasons = mutableListOf<BlockReason>()
    private val staticVariants = mutableListOf<AndroidVariant>()
    private val tournamentTypes = mutableListOf<TournamentType>()
    private val tournamentStyles = mutableListOf<TournamentStyle>()

    override val isEmpty: Boolean
        get() = (TextUtils.isEmpty(defaultTeamLogo) || sports.isEmpty() || positions.isEmpty()
                || visibilities.isEmpty() || blockReasons.isEmpty() || staticVariants.isEmpty())

    override val imageUrl: String
        get() = ""

    override fun update(updated: Config) {
        this.defaultTeamLogo = updated.defaultTeamLogo
        this.defaultEventLogo = updated.defaultEventLogo
        this.defaultUserAvatar = updated.defaultUserAvatar
        replaceStringList(privileged, updated.privileged)

        replaceList(sports, updated.sports)
        replaceList(positions, updated.positions)
        replaceList(statTypes, updated.statTypes)
        replaceList(visibilities, updated.visibilities)
        replaceList(blockReasons, updated.blockReasons)
        replaceList(staticVariants, updated.staticVariants)
        replaceList(staticVariants, updated.staticVariants)
        replaceList(tournamentTypes, updated.tournamentTypes)
        replaceList(tournamentStyles, updated.tournamentStyles)
    }

    override fun compareTo(other: Config): Int = 0

    override fun getId(): String = "0"

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = Unit

    class GsonAdapter : JsonSerializer<Config>, JsonDeserializer<Config> {

        override fun serialize(src: Config, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val serialized = JsonObject()

            serialized.addProperty(TEAM_LOGO_KEY, src.defaultTeamLogo)
            serialized.addProperty(EVENT_LOGO_KEY, src.defaultEventLogo)
            serialized.addProperty(USER_AVATAR_KEY, src.defaultUserAvatar)
            serialized.addProperty(TOURNAMENT_LOGO_KEY, src.defaultTournamentLogo)

            val statsArray = JsonArray()
            val sportsArray = JsonArray()
            val positionArray = JsonArray()
            val visibilityArray = JsonArray()
            val privilegedArray = JsonArray()
            val blockedReasonArray = JsonArray()
            val staticVariantsArray = JsonArray()
            val tournamentTypesArray = JsonArray()
            val tournamentStylesArray = JsonArray()

            for (item in src.sports) sportsArray.add(context.serialize(item))
            for (item in src.statTypes) statsArray.add(context.serialize(item))
            for (item in src.positions) positionArray.add(context.serialize(item))
            for (item in src.privileged) privilegedArray.add(context.serialize(item))
            for (item in src.visibilities) visibilityArray.add(context.serialize(item))
            for (item in src.blockReasons)
                blockedReasonArray.add(context.serialize(item))
            for (item in src.staticVariants)
                staticVariantsArray.add(context.serialize(item))
            for (item in src.tournamentTypes)
                tournamentTypesArray.add(context.serialize(item))
            for (item in src.tournamentStyles)
                tournamentStylesArray.add(context.serialize(item))

            serialized.add(SPORTS_KEY, sportsArray)
            serialized.add(GAME_STATS_KEY, statsArray)
            serialized.add(PRIVILEGED, privilegedArray)
            serialized.add(POSITIONS_KEY, positionArray)
            serialized.add(VISIBILITIES_KEY, visibilityArray)
            serialized.add(BLOCKED_REASONS_KEY, blockedReasonArray)
            serialized.add(STATIC_VARIANTS_KEY, staticVariantsArray)
            serialized.add(TOURNAMENT_TYPE_KEY, tournamentTypesArray)
            serialized.add(TOURNAMENT_STYLE_KEY, tournamentStylesArray)

            return serialized
        }

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Config {
            val deviceJson = json.asJsonObject

            val defaultTeamLogo = deviceJson.asStringOrEmpty(TEAM_LOGO_KEY)
            val defaultEventLogo = deviceJson.asStringOrEmpty(EVENT_LOGO_KEY)
            val defaultUserAvatar = deviceJson.asStringOrEmpty(USER_AVATAR_KEY)
            val defaultTournamentLogo = deviceJson.asStringOrEmpty(TOURNAMENT_LOGO_KEY)

            val config = Config(defaultTeamLogo, defaultEventLogo, defaultUserAvatar, defaultTournamentLogo)

            context.deserializeList(deviceJson.get(SPORTS_KEY), config.sports, Sport::class.java)
            context.deserializeList(deviceJson.get(PRIVILEGED), config.privileged, String::class.java)
            context.deserializeList(deviceJson.get(POSITIONS_KEY), config.positions, Position::class.java)
            context.deserializeList(deviceJson.get(GAME_STATS_KEY), config.statTypes, StatType::class.java)
            context.deserializeList(deviceJson.get(VISIBILITIES_KEY), config.visibilities, Visibility::class.java)
            context.deserializeList(deviceJson.get(BLOCKED_REASONS_KEY), config.blockReasons, BlockReason::class.java)
            context.deserializeList(deviceJson.get(STATIC_VARIANTS_KEY), config.staticVariants, AndroidVariant::class.java)
            context.deserializeList(deviceJson.get(TOURNAMENT_TYPE_KEY), config.tournamentTypes, TournamentType::class.java)
            context.deserializeList(deviceJson.get(TOURNAMENT_STYLE_KEY), config.tournamentStyles, TournamentStyle::class.java)

            return config
        }

        companion object {

            private const val TEAM_LOGO_KEY = "defaultTeamLogo"
            private const val EVENT_LOGO_KEY = "defaultEventLogo"
            private const val USER_AVATAR_KEY = "defaultUserAvatar"
            private const val TOURNAMENT_LOGO_KEY = "defaultTournamentLogo"
            private const val SPORTS_KEY = "sports"
            private const val POSITIONS_KEY = "roles"
            private const val PRIVILEGED = "privilegedRoles"
            private const val VISIBILITIES_KEY = "visibility"
            private const val BLOCKED_REASONS_KEY = "blockReasons"
            private const val STATIC_VARIANTS_KEY = "staticAndroidVariants"
            private const val GAME_STATS_KEY = "stats"
            private const val TOURNAMENT_TYPE_KEY = "tournamentTypes"
            private const val TOURNAMENT_STYLE_KEY = "tournamentStyles"
        }
    }

    companion object {

        private const val EMPTY_STRING = ""
        private val cached by lazy { AppDatabase.instance.configDao().current }

        fun empty(): Config = Config(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING)

        internal fun getDefaultTeamLogo(): String = currentConfig.defaultTeamLogo

        internal fun getDefaultEventLogo(): String = currentConfig.defaultEventLogo

        internal fun getDefaultUserAvatar(): String = currentConfig.defaultUserAvatar

        internal fun getDefaultTournamentLogo(): String = currentConfig.defaultTournamentLogo

        fun getSports(): List<Sport> = getList { config -> config.sports }

        fun getPrivileged(): List<String> = getList { config -> config.privileged }

        fun getPositions(): List<Position> = getList { config -> config.positions }

        fun getVisibilities(): List<Visibility> = getList { config -> config.visibilities }

        fun getBlockReasons(): List<BlockReason> = getList { config -> config.blockReasons }

        fun getTournamentTypes(predicate: (TournamentType) -> Boolean): List<TournamentType> =
                getList { config -> config.tournamentTypes }.filter(predicate)

        fun getTournamentStyles(predicate: (TournamentStyle) -> Boolean): List<TournamentStyle> =
                getList { config -> config.tournamentStyles }.filter(predicate)

        val isStaticVariant: Boolean
            get() = getList { config -> config.staticVariants }.contains(AndroidVariant.empty())

        fun sportFromCode(code: String): Sport =
                getFromCode(code, { config -> config.sports }, Sport.empty())

        fun positionFromCode(code: String): Position =
                getFromCode(code, { config -> config.positions }, Position.empty())

        fun visibilityFromCode(code: String): Visibility =
                getFromCode(code, { config -> config.visibilities }, Visibility.empty())

        fun reasonFromCode(code: String): BlockReason =
                getFromCode(code, { config -> config.blockReasons }, BlockReason.empty())

        fun statTypeFromCode(code: String): StatType =
                getFromCode(code, { config -> config.statTypes }, StatType.empty())

        fun tournamentTypeFromCode(code: String): TournamentType =
                getFromCode(code, { config -> config.tournamentTypes }, TournamentType.empty())

        fun tournamentStyleFromCode(code: String): TournamentStyle =
                getFromCode(code, { config -> config.tournamentStyles }, TournamentStyle.empty())

        private val currentConfig: Config
            get() {
                if (cached.isEmpty) fetchConfig()
                return cached
            }

        private fun <T> getList(function: (Config) -> List<T>): List<T> {
            val config = currentConfig

            return if (!config.isEmpty) function.invoke(config) else mutableListOf()
        }

        private fun <T : MetaData> getFromCode(code: String?, function: (Config) -> List<T>, defaultItem: T): T {
            val config = currentConfig
            val matcher = code ?: ""
            val list = function.invoke(config)
            return list.firstOrNull { metaData -> matcher == metaData.code } ?: defaultItem
        }

        @SuppressLint("CheckResult")
        private fun fetchConfig() {
            if (RepoProvider.initialized())
                RepoProvider.forRepo(ConfigRepo::class.java)[EMPTY_STRING]
                        .observeOn(mainThread()) // Necessary to prevent a concurrent modification exception
                        .subscribe(cached::update, ErrorHandler.EMPTY::invoke)
        }
    }
}
