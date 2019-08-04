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

package com.mainstreetcode.teammate.persistence

import androidx.core.util.Pair
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.BuildConfig
import com.mainstreetcode.teammate.model.Chat
import com.mainstreetcode.teammate.model.Media
import com.mainstreetcode.teammate.persistence.entity.CompetitorEntity
import com.mainstreetcode.teammate.persistence.entity.EventEntity
import com.mainstreetcode.teammate.persistence.entity.GameEntity
import com.mainstreetcode.teammate.persistence.entity.GuestEntity
import com.mainstreetcode.teammate.persistence.entity.JoinRequestEntity
import com.mainstreetcode.teammate.persistence.entity.RoleEntity
import com.mainstreetcode.teammate.persistence.entity.StatEntity
import com.mainstreetcode.teammate.persistence.entity.TeamEntity
import com.mainstreetcode.teammate.persistence.entity.TournamentEntity
import com.mainstreetcode.teammate.persistence.entity.UserEntity
import com.mainstreetcode.teammate.persistence.migrations.Migration1To2
import com.mainstreetcode.teammate.persistence.migrations.Migration2To3
import com.mainstreetcode.teammate.persistence.migrations.Migration3To4
import com.mainstreetcode.teammate.persistence.typeconverters.CharSequenceConverter
import com.mainstreetcode.teammate.persistence.typeconverters.CompetitiveTypeConverter
import com.mainstreetcode.teammate.persistence.typeconverters.CompetitorTypeConverter
import com.mainstreetcode.teammate.persistence.typeconverters.DateTypeConverter
import com.mainstreetcode.teammate.persistence.typeconverters.EventTypeConverter
import com.mainstreetcode.teammate.persistence.typeconverters.GameTypeConverter
import com.mainstreetcode.teammate.persistence.typeconverters.LatLngTypeConverter
import com.mainstreetcode.teammate.persistence.typeconverters.PositionTypeConverter
import com.mainstreetcode.teammate.persistence.typeconverters.SportTypeConverter
import com.mainstreetcode.teammate.persistence.typeconverters.StatAttributesTypeConverter
import com.mainstreetcode.teammate.persistence.typeconverters.StatTypeTypeConverter
import com.mainstreetcode.teammate.persistence.typeconverters.TeamTypeConverter
import com.mainstreetcode.teammate.persistence.typeconverters.TournamentStyleTypeConverter
import com.mainstreetcode.teammate.persistence.typeconverters.TournamentTypeConverter
import com.mainstreetcode.teammate.persistence.typeconverters.TournamentTypeTypeConverter
import com.mainstreetcode.teammate.persistence.typeconverters.UserTypeConverter
import com.mainstreetcode.teammate.persistence.typeconverters.VisibilityTypeConverter
import com.mainstreetcode.teammate.util.Logger
import io.reactivex.Single

/**
 * App Database
 */

@Database(entities = [
    UserEntity::class,
    TeamEntity::class,
    EventEntity::class,
    RoleEntity::class,
    JoinRequestEntity::class,
    GuestEntity::class,
    TournamentEntity::class,
    CompetitorEntity::class,
    GameEntity::class,
    StatEntity::class,
    Chat::class,
    Media::class
],
        version = 4)
@TypeConverters(
        LatLngTypeConverter::class,
        DateTypeConverter::class,
        CharSequenceConverter::class,
        UserTypeConverter::class,
        TeamTypeConverter::class,
        EventTypeConverter::class,
        TournamentTypeConverter::class,
        CompetitorTypeConverter::class,
        GameTypeConverter::class,
        SportTypeConverter::class,
        PositionTypeConverter::class,
        VisibilityTypeConverter::class,
        TournamentTypeTypeConverter::class,
        TournamentStyleTypeConverter::class,
        StatTypeTypeConverter::class,
        CompetitiveTypeConverter::class,
        StatAttributesTypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun teamDao(): TeamDao

    abstract fun roleDao(): RoleDao

    abstract fun gameDao(): GameDao

    abstract fun statDao(): StatDao

    abstract fun eventDao(): EventDao

    abstract fun mediaDao(): MediaDao

    abstract fun guestDao(): GuestDao

    abstract fun teamChatDao(): ChatDao

    abstract fun tournamentDao(): TournamentDao

    abstract fun competitorDao(): CompetitorDao

    abstract fun joinRequestDao(): JoinRequestDao

    fun prefsDao(): PrefsDao = PrefsDao()

    fun deviceDao(): DeviceDao = DeviceDao()

    fun configDao(): ConfigDao = ConfigDao()

    fun teamMemberDao(): TeamMemberDao = TeamMemberDao()

    fun clearTables(): Single<List<Pair<String, Int>>> = Single.concat(listOf(
            clearTable(competitorDao()),
            clearTable(statDao()),
            clearTable(gameDao()),
            clearTable(tournamentDao()),
            clearTable(teamChatDao()),
            clearTable(joinRequestDao()),
            clearTable(guestDao()),
            clearTable(eventDao()),
            clearTable(mediaDao()),
            clearTable(roleDao()),
            clearTable(teamDao()),
            clearTable(userDao()),
            clearTable(deviceDao()),
            clearTable(configDao())
    )).toList()

    private fun clearTable(entityDao: EntityDao<*>): Single<Pair<String, Int>> {
        val tableName = entityDao.tableName

        return entityDao.deleteAll()
                .map { rowsDeleted -> Pair(tableName, rowsDeleted) }
                .onErrorResumeNext { throwable ->
                    Logger.log(TAG, "Error clearing table: $tableName", throwable)
                    Single.just(Pair(tableName, 0))
                }
    }

    companion object {

        private const val TAG = "AppDatabase"
        private const val PROD_DB = "database-name"
        private const val DEV_DB = "teammate-dev-db"
        private lateinit var INSTANCE: AppDatabase

        val instance: AppDatabase
            get() {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = Room.databaseBuilder(
                            App.getInstance(),
                            AppDatabase::class.java,
                            if (BuildConfig.DEV) DEV_DB else PROD_DB
                    )
                            .addMigrations(Migration1To2())
                            .addMigrations(Migration2To3())
                            .addMigrations(Migration3To4())
                            .fallbackToDestructiveMigration()
                            .build()
                }
                return INSTANCE
            }
    }
}
