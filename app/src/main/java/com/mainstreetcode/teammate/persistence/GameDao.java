package com.mainstreetcode.teammate.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.persistence.entity.GameEntity;

import java.util.Date;
import java.util.List;

import io.reactivex.Maybe;

/**
 * DAO for {@link Event}
 */

@Dao
public abstract class GameDao extends EntityDao<GameEntity> {

    @Override
    protected String getTableName() {
        return "games";
    }

    @Query("SELECT game_id, game_name, game_ref_path, game_score, game_match_up," +
            " game_home_entity, game_away_entity,game_winner_entity,game_created," +
            "game_sport,game_referee,game_host,game_event,game_tournament,game_home,game_away,game_winner" +
            ",game_leg,game_seed,game_round,game_home_score,game_away_score,game_ended,game_can_draw" +
            " FROM games as game" +
            " INNER JOIN competitors competitor" +
            " ON (competitor.competitor_game = game.game_id)" +
            " WHERE competitor.competitor_declined = 0" +
            " AND (:teamId = game_host AND game_ref_path = 'user')" +
            " OR :teamId = game_home_entity" +
            " OR :teamId = game_away_entity" +
            " AND game_created < :date" +
            " ORDER BY game_created DESC" +
            " LIMIT :limit")
    public abstract Maybe<List<Game>> getGames(String teamId, Date date, int limit);

    @Query("SELECT * FROM games as game" +
            " WHERE :tournamentId = game_tournament" +
            " AND game_round = :round" +
            " ORDER BY game_created DESC" +
            " LIMIT :limit")
    public abstract Maybe<List<Game>> getGames(String tournamentId, int round, int limit);

    @Query("SELECT * FROM games" +
            " WHERE :id = game_id")
    public abstract Maybe<Game> get(String id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insert(List<GameEntity> games);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void update(List<GameEntity> games);

    @Delete
    public abstract void delete(GameEntity game);
}
