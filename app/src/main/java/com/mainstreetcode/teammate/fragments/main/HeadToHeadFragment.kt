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

package com.mainstreetcode.teammate.fragments.main

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.GameAdapter
import com.mainstreetcode.teammate.adapters.HeadToHeadRequestAdapter
import com.mainstreetcode.teammate.adapters.TeamAdapter
import com.mainstreetcode.teammate.adapters.UserAdapter
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseFragment
import com.mainstreetcode.teammate.databinding.FragmentHeadToHeadBinding
import com.mainstreetcode.teammate.model.Competitive
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.HeadToHead
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.ExpandingToolbar
import com.mainstreetcode.teammate.util.ScrollManager
import com.tunjid.androidbootstrap.core.text.SpanBuilder
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

class HeadToHeadFragment : TeammatesBaseFragment(R.layout.fragment_head_to_head),
        UserAdapter.AdapterListener,
        TeamAdapter.AdapterListener,
        HeadToHeadRequestAdapter.AdapterListener {

    private var isHome = true
    private lateinit var request: HeadToHead.Request

    private var expandingToolbar: ExpandingToolbar? = null
    private var searchScrollManager: ScrollManager<*>? = null
    private var binding: FragmentHeadToHeadBinding? = null

    private lateinit var matchUps: List<Differentiable>

    override val showsFab: Boolean get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        request = HeadToHead.Request.empty()
        matchUps = gameViewModel.headToHeadMatchUps
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = FragmentHeadToHeadBinding.bind(view).run {
        super.onViewCreated(view, savedInstanceState)
        defaultUi(
                toolbarShows = false,
                fabShows = showsFab
        )

        searchScrollManager = ScrollManager.with<BaseViewHolder<*>>(view.findViewById(R.id.search_options))
                .withAdapter(HeadToHeadRequestAdapter(request, this@HeadToHeadFragment))
                .withInconsistencyHandler(this@HeadToHeadFragment::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .withLinearLayoutManager()
                .build()

        scrollManager = ScrollManager.with<InteractiveViewHolder<*>>(view.findViewById(R.id.list_layout))
                .withPlaceholder(EmptyViewHolder(view, R.drawable.ic_head_to_head_24dp, R.string.game_head_to_head_prompt))
                .withAdapter(GameAdapter(matchUps, GameAdapter.AdapterListener.asSAM { game -> navigator.show(GameFragment.newInstance(game)) }))
                .withRefreshLayout(view.findViewById(R.id.refresh_layout)) { this@HeadToHeadFragment.fetchMatchUps() }
                .withInconsistencyHandler(this@HeadToHeadFragment::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build()

        expandingToolbar = ExpandingToolbar.create(cardViewWrapper.cardViewWrapper) { this@HeadToHeadFragment.fetchMatchUps() }
        expandingToolbar?.setTitleIcon(false)
        expandingToolbar?.setTitle(R.string.game_head_to_head_params)

        scrollManager.notifyDataSetChanged()

        updateHeadToHead(0, 0, 0)
        if (!restoredFromBackStack) expandingToolbar?.changeVisibility(false)

        binding = this
    }

    override fun onDestroyView() {
        expandingToolbar = null
        searchScrollManager = null
        binding = null
        super.onDestroyView()
    }

    override fun onKeyBoardChanged(appeared: Boolean) {
        super.onKeyBoardChanged(appeared)
        if (!appeared && bottomSheetDriver.isBottomSheetShowing) bottomSheetDriver.hideBottomSheet()
    }

    override fun onUserClicked(item: User) = updateCompetitor(item)

    override fun onTeamClicked(item: Team) = updateCompetitor(item)

    override fun onHomeClicked(home: Competitor) {
        isHome = true
        findCompetitor()
    }

    override fun onAwayClicked(away: Competitor) {
        isHome = false
        findCompetitor()
    }

    private fun fetchMatchUps() {
        transientBarDriver.toggleProgress(true)
        disposables.add(gameViewModel.headToHead(request).subscribe({ summary -> updateHeadToHead(summary.wins, summary.draws, summary.losses) }, ErrorHandler.EMPTY::invoke))
        disposables.add(gameViewModel.getMatchUps(request).subscribe({ diffResult ->
            transientBarDriver.toggleProgress(false)
            scrollManager.onDiff(diffResult)
        }, defaultErrorHandler::invoke))
    }

    private fun updateHeadToHead(numWins: Int, numDraws: Int, numLosses: Int) = binding?.apply {
        wins.text = getText(R.string.game_wins, numWins)
        draws.text = getText(R.string.game_draws, numDraws)
        losses.text = getText(R.string.game_losses, numLosses)
    }

    private fun updateCompetitor(item: Competitive) {
        if (isHome) request.updateHome(item)
        else request.updateAway(item)
        searchScrollManager?.notifyDataSetChanged()
        bottomSheetDriver.hideBottomSheet()
        hideKeyboard()
    }

    private fun findCompetitor() = bottomSheetDriver.showBottomSheet {
        if (request.hasInvalidType()) return@showBottomSheet transientBarDriver.showSnackBar(getString(R.string.game_select_tournament_type))

        val refPath = request.refPath
        val isBetweenUsers = User.COMPETITOR_TYPE == refPath


        if (isBetweenUsers) fragment = UserSearchFragment.newInstance()
        else if (Team.COMPETITOR_TYPE == refPath) fragment = TeamSearchFragment.newInstance(request.sport)

        fragment?.setTargetFragment(this@HeadToHeadFragment, R.id.request_competitor_pick)
    }

    private fun getText(@StringRes stringRes: Int, count: Int): CharSequence =
            SpanBuilder.of(count.toString())
                    .resize(1.4f)
                    .bold()
                    .append(SpanBuilder.of(getString(stringRes))
                            .prependNewLine()
                            .build())
                    .build()

    companion object {

        fun newInstance(): HeadToHeadFragment = HeadToHeadFragment().apply {
            arguments = Bundle()
            setEnterExitTransitions()
        }
    }
}
