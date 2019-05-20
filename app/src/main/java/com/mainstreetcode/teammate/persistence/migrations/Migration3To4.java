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

package com.mainstreetcode.teammate.persistence.migrations;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.migration.Migration;
import androidx.annotation.NonNull;

public class Migration3To4 extends Migration {

    public Migration3To4() {
        super(3, 4);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {

        database.execSQL("ALTER TABLE users ADD COLUMN user_screen_name TEXT DEFAULT null");

        database.execSQL("ALTER TABLE teams ADD COLUMN team_screen_name TEXT DEFAULT null");

        database.execSQL("ALTER TABLE events ADD COLUMN event_game_id TEXT DEFAULT null");

        database.execSQL("CREATE TABLE IF NOT EXISTS `tournaments` (" +
                "`tournament_id` TEXT NOT NULL, " +
                "`tournament_image_url` TEXT, " +
                "`tournament_ref_path` TEXT, " +
                "`tournament_name` TEXT, " +
                "`tournament_description` TEXT, " +
                "`tournament_host` TEXT, " +
                "`tournament_created` INTEGER, " +
                "`tournament_sport` TEXT, " +
                "`tournament_type` TEXT, " +
                "`tournament_style` TEXT, " +
                "`tournament_winner` TEXT, " +
                "`tournament_num_legs` INTEGER NOT NULL, " +
                "`tournament_num_rounds` INTEGER NOT NULL, " +
                "`tournament_current_round` INTEGER NOT NULL, " +
                "`tournament_num_competitors` INTEGER NOT NULL, " +
                "`tournament_single_final` INTEGER NOT NULL, " +
                "PRIMARY KEY(`tournament_id`), " +
                "FOREIGN KEY(`tournament_host`) " +
                "REFERENCES `teams`(`team_id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )");

        database.execSQL("CREATE TABLE IF NOT EXISTS `games` " +
                "(`game_id` TEXT NOT NULL, " +
                "`game_name` TEXT, " +
                "`game_ref_path` TEXT, " +
                "`game_score` TEXT, " +
                "`game_home_entity` TEXT, " +
                "`game_away_entity` TEXT, " +
                "`game_winner_entity` TEXT, " +
                "`game_match_up` TEXT, " +
                "`game_created` INTEGER, " +
                "`game_sport` TEXT, " +
                "`game_referee` TEXT, " +
                "`game_host` TEXT, " +
                "`game_event` TEXT, " +
                "`game_tournament` TEXT, " +
                "`game_home` TEXT, " +
                "`game_away` TEXT, " +
                "`game_winner` TEXT, " +
                "`game_leg` INTEGER NOT NULL, " +
                "`game_seed` INTEGER NOT NULL, " +
                "`game_round` INTEGER NOT NULL, " +
                "`game_home_score` INTEGER NOT NULL, " +
                "`game_away_score` INTEGER NOT NULL, " +
                "`game_ended` INTEGER NOT NULL, " +
                "`game_can_draw` INTEGER NOT NULL, " +
                "PRIMARY KEY(`game_id`), " +
                "FOREIGN KEY(`game_tournament`) " +
                "REFERENCES `tournaments`(`tournament_id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )");

        database.execSQL("CREATE TABLE IF NOT EXISTS `competitors` " +
                "(`competitor_id` TEXT NOT NULL, " +
                "`competitor_ref_path` TEXT, " +
                "`competitor_tournament` TEXT, " +
                "`competitor_game` TEXT, " +
                "`competitor_entity` TEXT, " +
                "`competitor_created` INTEGER, " +
                "`competitor_seed` INTEGER NOT NULL, " +
                "`competitor_accepted` INTEGER NOT NULL, " +
                "`competitor_declined` INTEGER NOT NULL, " +
                "PRIMARY KEY(`competitor_id`), " +
                "FOREIGN KEY(`competitor_tournament`) " +
                "REFERENCES `tournaments`(`tournament_id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE , " +
                "FOREIGN KEY(`competitor_game`) " +
                "REFERENCES `games`(`game_id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )");

        database.execSQL("CREATE TABLE IF NOT EXISTS `stats` (" +
                "`stat_id` TEXT NOT NULL, " +
                "`stat_created` INTEGER, " +
                "`stat_type` TEXT, " +
                "`stat_sport` TEXT, " +
                "`stat_user` TEXT, " +
                "`stat_team` TEXT, " +
                "`stat_game` TEXT, " +
                "`stat_attributes` TEXT, " +
                "`stat_value` INTEGER NOT NULL, " +
                "`stat_time` REAL NOT NULL, " +
                "PRIMARY KEY(`stat_id`), " +
                "FOREIGN KEY(`stat_game`) " +
                "REFERENCES `games`(`game_id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE , " +
                "FOREIGN KEY(`stat_team`) " +
                "REFERENCES `teams`(`team_id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE , " +
                "FOREIGN KEY(`stat_user`) " +
                "REFERENCES `users`(`user_id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )");
    }
}
