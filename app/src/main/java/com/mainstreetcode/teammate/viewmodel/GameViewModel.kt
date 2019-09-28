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

package com.mainstreetcode.teammate.viewmodel

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.model.Competitive
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.HeadToHead
import com.mainstreetcode.teammate.model.Message
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.repository.GameRepo
import com.mainstreetcode.teammate.repository.GameRoundRepo
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.repository.UserRepo
import com.mainstreetcode.teammate.rest.TeammateService
import com.mainstreetcode.teammate.util.FunctionalDiff
import com.mainstreetcode.teammate.util.asDifferentiables
import com.mainstreetcode.teammate.util.replaceList
import com.mainstreetcode.teammate.viewmodel.events.Alert
import com.mainstreetcode.teammate.viewmodel.events.matches
import com.mainstreetcode.teammate.viewmodel.gofers.GameGofer
import com.tunjid.androidx.recyclerview.diff.Differentiable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import java.util.*

class GameViewModel : TeamMappedViewModel<Game>() {

    val headToHeadMatchUps = mutableListOf<Differentiable>()

    private val api = TeammateService.getApiInstance()
    private val gameRoundMap = HashMap<Tournament, MutableMap<Int, MutableList<Differentiable>>>()

    private val gameRoundRepository = RepoProvider.forRepo(GameRoundRepo::class.java)
    private val gameRepository = RepoProvider.forRepo(GameRepo::class.java)

    fun gofer(game: Game): GameGofer =
            GameGofer(game, onError(game), ::getGame, ::updateGame, ::delete, ::getEligibleTeamsForGame)

    override fun valueClass(): Class<Game> = Game::class.java

    @SuppressLint("CheckResult")
    override fun onModelAlert(alert: Alert<*>) {
        super.onModelAlert(alert)

        alert.matches(
                Alert.of(Alert.Deletion::class.java, Game::class.java, this::onGameDeleted),
                Alert.of(Alert.Deletion::class.java, Tournament::class.java, this::onTournamentDeleted)
        )
    }

    override fun onErrorMessage(message: Message, key: Team, invalid: Differentiable) {
        super.onErrorMessage(message, key, invalid)
        if (message.isInvalidObject) headToHeadMatchUps.remove(invalid)
        if (invalid is Game) invalid.isEnded = false
    }

    override fun fetch(key: Team, fetchLatest: Boolean): Flowable<List<Game>> =
            gameRepository.modelsBefore(key, getQueryDate(fetchLatest, key) { it.created })
                    .map { games -> filterDeclinedGamed(key, games) }

    @SuppressLint("UseSparseArrays")
    fun getGamesForRound(tournament: Tournament, round: Int): MutableList<Differentiable> =
            gameRoundMap.getOrPut(tournament) { mutableMapOf() }
                    .getOrPut(round) { mutableListOf() }

    fun fetchGamesInRound(tournament: Tournament, round: Int): Flowable<DiffUtil.DiffResult> {
        val flowable = gameRoundRepository.modelsBefore(tournament, round).map(::asDifferentiables)
        return FunctionalDiff.of(flowable, getGamesForRound(tournament, round), this::preserveList)
    }

    fun headToHead(request: HeadToHead.Request): Single<HeadToHead.Summary> =
            api.headToHead(request).map { result -> result.getSummary(request) }.observeOn(mainThread())

    fun getMatchUps(request: HeadToHead.Request): Single<DiffUtil.DiffResult> {
        val sourceSingle = api.matchUps(request).map<List<Differentiable>> { games ->
            asDifferentiables(games.apply { sortWith(FunctionalDiff.COMPARATOR) })
        }
        return FunctionalDiff.of(sourceSingle, headToHeadMatchUps, ::replaceList).observeOn(mainThread())
    }

    private fun getGame(game: Game): Flowable<Game> = gameRoundRepository[game].observeOn(mainThread())

    fun endGame(game: Game): Single<Game> {
        game.isEnded = true
        return updateGame(game)
    }

    private fun updateGame(game: Game): Single<Game> =
            gameRoundRepository.createOrUpdate(game)
                    .doOnError(onError(game)).observeOn(mainThread())

    private fun delete(game: Game): Single<Game> =
            gameRepository.delete(game)
                    .doOnSuccess { getModelList(game.team).remove(it) }
                    .doOnSuccess { deleted -> pushModelAlert(Alert.deletion(deleted)) }

    @SuppressLint("CheckResult")
    private fun onTournamentDeleted(tournament: Tournament) {
        val tournamentMap = gameRoundMap.getOrPut(tournament) { mutableMapOf() }
        (modelListMap.values + tournamentMap.values)
                .distinct()
                .filterIsInstance(Game::class.java)
                .filter { game -> tournament == game.tournament }
                .forEach { game -> pushModelAlert(Alert.deletion(game)) }
    }

    private fun onGameDeleted(game: Game) {
        headToHeadMatchUps.remove(game)
        getModelList(game.host).remove(game)

        val home = game.home.entity
        val away = game.away.entity
        if (home is Team) getModelList(home).remove(game)
        if (away is Team) getModelList(away).remove(game)

        gameRepository.queueForLocalDeletion(game)
    }

    private fun filterDeclinedGamed(key: Team, games: List<Game>): List<Game> = games.filter { game ->
        val entity: Competitive = if (game.betweenUsers()) RepoProvider.forRepo(UserRepo::class.java).currentUser
        else key
        val isAway = entity == game.away.entity
        (isAway && game.away.isDeclined).not()
    }

    companion object {

        internal fun getEligibleTeamsForGame(game: Game): Flowable<Team> = when {
            game.betweenUsers() -> Flowable.just(game.host)
            else -> Flowable.fromIterable(RoleViewModel.roles)
                    .filter { identifiable -> identifiable is Role }.cast(Role::class.java)
                    .filter(Role::isPrivilegedRole).map(Role::team)
                    .filter { team -> isParticipant(game, team) }
        }

        private fun isParticipant(game: Game, team: Team): Boolean =
                game.home.entity == team || game.away.entity == team
    }
}
