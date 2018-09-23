package com.mainstreetcode.teammate.persistence.migrations;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;

public class Migration3To4 extends Migration {

    public Migration3To4() {
        super(3, 4);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {

        database.execSQL("ALTER TABLE events ADD COLUMN event_game_id TEXT DEFAULT ''");

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
                "`game_ref_path` TEXT, " +
                "`game_score` TEXT, " +
                "`game_host_id` TEXT, " +
                "`game_home_entity_id` TEXT, " +
                "`game_away_entity_id` TEXT, " +
                "`game_created` INTEGER, " +
                "`game_sport` TEXT, " +
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
                "`competitor_tournament_id` TEXT, " +
                "`competitor_entity_id` TEXT, " +
                "`competitor_created` INTEGER, " +
                "`competitor_seed` INTEGER, " +
                "PRIMARY KEY(`competitor_id`), " +
                "FOREIGN KEY(`competitor_tournament_id`) " +
                "REFERENCES `tournaments`(`tournament_id`) " +
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
                "ON UPDATE NO ACTION ON DELETE CASCADE )"
        );
    }
}
