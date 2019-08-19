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

package com.mainstreetcode.teammate.persistence.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration4To5 : Migration(4, 5) {

    companion object {
        const val TEXT = "TEXT"
        const val INTEGER = "INTEGER"
        const val REAL = "REAL"

        const val NEVER_NULLABLE = 0
        const val NOW_NON_NULL = 1
        const val ALWAYS_NULLABLE = 2
    }

    sealed class ColumnDesc(
            open val name: String,
            open val type: String,
            open val change: Int
    ) {

        class PrimaryKey(
                override val name: String
        ) : ColumnDesc(name, "STRING", NEVER_NULLABLE) {
            override fun toString(): String = " PRIMARY KEY(`$name`)"
        }

        class ForeignKey(
                override val name: String,
                val referenceTable: String,
                val referenceKey: String
        ) : ColumnDesc(name, "STRING", NEVER_NULLABLE) {
            override fun toString(): String {
                return " FOREIGN KEY(`$name`) REFERENCES `$referenceTable`(`$referenceKey`) ON UPDATE NO ACTION ON DELETE CASCADE "
            }
        }

        class Regular(
                override val name: String,
                override val type: String,
                override val change: Int
        ) : ColumnDesc(name = name, type = type, change = change) {
            val default: String
                get() = if (type == TEXT) "''" else "0"

            override fun toString(): String = when (change) {
                ALWAYS_NULLABLE -> " `$name` $type"
                else -> " `$name` $type NOT NULL"
            }
        }

    }

    override fun migrate(database: SupportSQLiteDatabase) {

        database.migrateTable(
                "users",
                ColumnDesc.Regular("user_id", TEXT, NEVER_NULLABLE),
                ColumnDesc.Regular("user_image_url", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("user_screen_name", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("user_primary_email", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("user_first_name", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("user_last_name", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("user_about", TEXT, NOW_NON_NULL),
                ColumnDesc.PrimaryKey("user_id")
        )

        database.migrateTable(
                "teams",
                ColumnDesc.Regular("team_id", TEXT, NEVER_NULLABLE),
                ColumnDesc.Regular("team_image_url", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("team_screen_name", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("team_city", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("team_state", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("team_zip", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("team_name", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("team_description", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("team_sport", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("team_created", INTEGER, NOW_NON_NULL),
                ColumnDesc.Regular("team_location", TEXT, ALWAYS_NULLABLE),
                ColumnDesc.Regular("team_storage_used", INTEGER, NEVER_NULLABLE),
                ColumnDesc.Regular("team_max_storage", INTEGER, NEVER_NULLABLE),
                ColumnDesc.Regular("team_min_age", INTEGER, NEVER_NULLABLE),
                ColumnDesc.Regular("team_max_age", INTEGER, NEVER_NULLABLE),
                ColumnDesc.PrimaryKey("team_id")
        )

        database.migrateTable(
                "events",
                ColumnDesc.Regular("event_id", TEXT, NEVER_NULLABLE),
                ColumnDesc.Regular("event_game_id", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("event_image_url", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("event_name", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("event_notes", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("event_location_name", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("event_team", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("event_start_date", INTEGER, NOW_NON_NULL),
                ColumnDesc.Regular("event_end_date", INTEGER, NOW_NON_NULL),
                ColumnDesc.Regular("event_location", TEXT, ALWAYS_NULLABLE),
                ColumnDesc.Regular("event_visibility", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("event_spots", INTEGER, NEVER_NULLABLE),
                ColumnDesc.PrimaryKey("event_id"),
                ColumnDesc.ForeignKey("event_team", referenceTable = "teams", referenceKey = "team_id")
        )

        database.migrateTable(
                "roles",
                ColumnDesc.Regular("role_id", TEXT, NEVER_NULLABLE),
                ColumnDesc.Regular("role_image_url", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("role_nickname", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("role_name", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("role_team", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("role_user", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("role_created", INTEGER, NOW_NON_NULL),
                ColumnDesc.PrimaryKey("role_id"),
                ColumnDesc.ForeignKey("role_user", referenceTable = "users", referenceKey = "user_id"),
                ColumnDesc.ForeignKey("role_team", referenceTable = "teams", referenceKey = "team_id")
        )

        database.migrateTable(
                "join_requests",
                ColumnDesc.Regular("join_request_id", TEXT, NEVER_NULLABLE),
                ColumnDesc.Regular("join_request_team_approved", INTEGER, NEVER_NULLABLE),
                ColumnDesc.Regular("join_request_team_userApproved", INTEGER, NEVER_NULLABLE),
                ColumnDesc.Regular("join_request_role_name", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("join_request_team", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("join_request_user", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("join_request_created", INTEGER, NOW_NON_NULL),
                ColumnDesc.PrimaryKey("join_request_id"),
                ColumnDesc.ForeignKey("join_request_user", referenceTable = "users", referenceKey = "user_id"),
                ColumnDesc.ForeignKey("join_request_team", referenceTable = "teams", referenceKey = "team_id")
        )

        database.migrateTable(
                "guests",
                ColumnDesc.Regular("guest_id", TEXT, NEVER_NULLABLE),
                ColumnDesc.Regular("guest_user", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("guest_event", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("guest_created", INTEGER, NOW_NON_NULL),
                ColumnDesc.Regular("guest_attending", INTEGER, NEVER_NULLABLE),
                ColumnDesc.PrimaryKey("guest_id"),
                ColumnDesc.ForeignKey("guest_user", referenceTable = "users", referenceKey = "user_id"),
                ColumnDesc.ForeignKey("guest_event", referenceTable = "events", referenceKey = "event_id")
        )

        database.migrateTable(
                "tournaments",
                ColumnDesc.Regular("tournament_id", TEXT, NEVER_NULLABLE),
                ColumnDesc.Regular("tournament_image_url", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("tournament_ref_path", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("tournament_name", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("tournament_description", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("tournament_host", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("tournament_created", INTEGER, NOW_NON_NULL),
                ColumnDesc.Regular("tournament_sport", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("tournament_type", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("tournament_style", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("tournament_winner", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("tournament_num_legs", INTEGER, NEVER_NULLABLE),
                ColumnDesc.Regular("tournament_num_rounds", INTEGER, NEVER_NULLABLE),
                ColumnDesc.Regular("tournament_current_round", INTEGER, NEVER_NULLABLE),
                ColumnDesc.Regular("tournament_num_competitors", INTEGER, NEVER_NULLABLE),
                ColumnDesc.Regular("tournament_single_final", INTEGER, NEVER_NULLABLE),
                ColumnDesc.PrimaryKey("tournament_id"),
                ColumnDesc.ForeignKey("tournament_host", referenceTable = "teams", referenceKey = "team_id")
        )

        database.migrateTable(
                "competitors",
                ColumnDesc.Regular("competitor_id", TEXT, NEVER_NULLABLE),
                ColumnDesc.Regular("competitor_ref_path", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("competitor_tournament", TEXT, ALWAYS_NULLABLE),
                ColumnDesc.Regular("competitor_game", TEXT, ALWAYS_NULLABLE),
                ColumnDesc.Regular("competitor_entity", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("competitor_created", INTEGER, NOW_NON_NULL),
                ColumnDesc.Regular("competitor_seed", INTEGER, NEVER_NULLABLE),
                ColumnDesc.Regular("competitor_accepted", INTEGER, NEVER_NULLABLE),
                ColumnDesc.Regular("competitor_declined", INTEGER, NEVER_NULLABLE),
                ColumnDesc.PrimaryKey("competitor_id"),
                ColumnDesc.ForeignKey("competitor_tournament", referenceTable = "tournaments", referenceKey = "tournament_id"),
                ColumnDesc.ForeignKey("competitor_game", referenceTable = "games", referenceKey = "game_id")
        )

        database.migrateTable(
                "games",
                ColumnDesc.Regular("game_id", TEXT, NEVER_NULLABLE),
                ColumnDesc.Regular("game_name", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("game_ref_path", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("game_score", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("game_match_up", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("game_home_entity", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("game_away_entity", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("game_winner_entity", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("game_created", INTEGER, NOW_NON_NULL),
                ColumnDesc.Regular("game_sport", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("game_referee", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("game_host", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("game_event", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("game_tournament", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("game_home", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("game_away", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("game_winner", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("game_leg", INTEGER, NEVER_NULLABLE),
                ColumnDesc.Regular("game_seed", INTEGER, NEVER_NULLABLE),
                ColumnDesc.Regular("game_round", INTEGER, NEVER_NULLABLE),
                ColumnDesc.Regular("game_home_score", INTEGER, NEVER_NULLABLE),
                ColumnDesc.Regular("game_away_score", INTEGER, NEVER_NULLABLE),
                ColumnDesc.Regular("game_ended", INTEGER, NEVER_NULLABLE),
                ColumnDesc.Regular("game_can_draw", INTEGER, NEVER_NULLABLE),
                ColumnDesc.PrimaryKey("game_id"),
                ColumnDesc.ForeignKey("game_tournament", referenceTable = "tournaments", referenceKey = "tournament_id")
        )

        database.migrateTable(
                "stats",
                ColumnDesc.Regular("stat_id", TEXT, NEVER_NULLABLE),
                ColumnDesc.Regular("stat_created", INTEGER, NOW_NON_NULL),
                ColumnDesc.Regular("stat_type", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("stat_sport", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("stat_user", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("stat_team", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("stat_game", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("stat_attributes", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("stat_value", INTEGER, NEVER_NULLABLE),
                ColumnDesc.Regular("stat_time", REAL, NEVER_NULLABLE),
                ColumnDesc.PrimaryKey("stat_id"),
                ColumnDesc.ForeignKey("stat_user", referenceTable = "users", referenceKey = "user_id"),
                ColumnDesc.ForeignKey("stat_team", referenceTable = "teams", referenceKey = "team_id"),
                ColumnDesc.ForeignKey("stat_game", referenceTable = "games", referenceKey = "game_id")
        )

        database.migrateTable(
                "team_chats",
                ColumnDesc.Regular("team_chat_id", TEXT, NEVER_NULLABLE),
                ColumnDesc.Regular("team_chat_kind", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("team_chat_content", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("team_chat_user", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("team_chat_team", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("team_chat_created", INTEGER, NOW_NON_NULL),
                ColumnDesc.PrimaryKey("team_chat_id"),
                ColumnDesc.ForeignKey("team_chat_user", referenceTable = "users", referenceKey = "user_id"),
                ColumnDesc.ForeignKey("team_chat_team", referenceTable = "teams", referenceKey = "team_id")
        )

        database.migrateTable(
                "team_media",
                ColumnDesc.Regular("media_id", TEXT, NEVER_NULLABLE),
                ColumnDesc.Regular("media_url", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("media_mime_type", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("media_thumbnail", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("media_user", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("media_team", TEXT, NOW_NON_NULL),
                ColumnDesc.Regular("media_created", INTEGER, NOW_NON_NULL),
                ColumnDesc.Regular("media_flagged", INTEGER, NOW_NON_NULL),
                ColumnDesc.PrimaryKey("media_id"),
                ColumnDesc.ForeignKey("media_user", referenceTable = "users", referenceKey = "user_id"),
                ColumnDesc.ForeignKey("media_team", referenceTable = "teams", referenceKey = "team_id")
        )

    }

    private fun SupportSQLiteDatabase.migrateTable(tableName: String, vararg columns: ColumnDesc) {
        execSQL(columns.createSQL(tableName))
        execSQL(columns.copySQL(tableName))
        execSQL(dropSql(tableName))
        execSQL(renameSql(tableName))
    }

    private fun tempName(tableName: String) = "${tableName}_new"

    private fun Array<out ColumnDesc>.createSQL(tableName: String): String {
        val tempName = tempName(tableName)
        val result = StringBuilder()
        val last = size - 1

        result.append("CREATE TABLE $tempName (")

        forEachIndexed { index, item ->
            result.append(item.toString())
            if (index != last) result.append(",")
        }

        result.append(")")

        return result.toString()
    }

    private fun Array<out ColumnDesc>.copySQL(tableName: String): String {
        val toCopy = filterIsInstance(ColumnDesc.Regular::class.java)
        val tempName = tempName(tableName)
        val destBuilder = StringBuilder()
        val srcBuilder = StringBuilder()
        val result = StringBuilder()
        val last = toCopy.size - 1

        toCopy.forEachIndexed { index, item ->
            destBuilder.append(item.name)
            srcBuilder.append(
                    if (item.change == NOW_NON_NULL) "coalesce(${item.name}, ${item.default}) ${item.name}"
                    else item.name
            )

            if (index != last) {
                srcBuilder.append(",")
                destBuilder.append(",")
            }
        }

        val src = srcBuilder.toString()
        val dest = destBuilder.toString()

        result.append("INSERT INTO $tempName ($dest) SELECT $src FROM $tableName")


        return result.toString()
    }

    private fun dropSql(tableName: String): String = "DROP TABLE $tableName"

    private fun renameSql(tableName: String): String = "ALTER TABLE ${tempName(tableName)} RENAME TO $tableName"

}
