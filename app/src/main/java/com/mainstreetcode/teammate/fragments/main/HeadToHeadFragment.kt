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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.GameAdapter
import com.mainstreetcode.teammate.adapters.HeadToHeadRequestAdapter
import com.mainstreetcode.teammate.adapters.TeamAdapter
import com.mainstreetcode.teammate.adapters.UserAdapter
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.baseclasses.BottomSheetController
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.model.Competitive
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.HeadToHead
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.ExpandingToolbar
import com.mainstreetcode.teammate.util.ScrollManager
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment
import com.tunjid.androidbootstrap.core.text.SpanBuilder
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

class HeadToHeadFragment : MainActivityFragment(), UserAdapter.AdapterListener, TeamAdapter.AdapterListener, HeadToHeadRequestAdapter.AdapterListener {

    private var isHome = true
    private lateinit var request: HeadToHead.Request

    private var expandingToolbar: ExpandingToolbar? = null
    private var searchScrollManager: ScrollManager<*>? = null

    private var wins: TextView? = null
    private var draws: TextView? = null
    private var losses: TextView? = null

    private lateinit var matchUps: List<Differentiable>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        request = HeadToHead.Request.empty()
        matchUps = gameViewModel.headToHeadMatchUps
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_head_to_head, container, false)

        wins = root.findViewById(R.id.wins)
        draws = root.findViewById(R.id.draws)
        losses = root.findViewById(R.id.losses)

        searchScrollManager = ScrollManager.with<BaseViewHolder<*>>(root.findViewById(R.id.search_options))
                .withAdapter(HeadToHeadRequestAdapter(request, this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .withLinearLayoutManager()
                .build()

        scrollManager = ScrollManager.with<InteractiveViewHolder<*>>(root.findViewById(R.id.list_layout))
                .withPlaceholder(EmptyViewHolder(root, R.drawable.ic_head_to_head_24dp, R.string.game_head_to_head_prompt))
                .withAdapter(GameAdapter(matchUps, GameAdapter.AdapterListener.asSAM { game -> showFragment(GameFragment.newInstance(game)) }))
                .withRefreshLayout(root.findViewById(R.id.refresh_layout)) { this.fetchMatchUps() }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build()

        expandingToolbar = ExpandingToolbar.create(root.findViewById(R.id.card_view_wrapper)) { this.fetchMatchUps() }
        expandingToolbar?.setTitleIcon(false)
        expandingToolbar?.setTitle(R.string.game_head_to_head_params)

        scrollManager.notifyDataSetChanged()

        updateHeadToHead(0, 0, 0)
        if (!restoredFromBackStack()) expandingToolbar?.changeVisibility(false)

        return root
    }

    override fun onDestroyView() {
        expandingToolbar = null
        searchScrollManager = null
        losses = null
        draws = null
        wins = null
        super.onDestroyView()
    }

    override fun showsToolBar(): Boolean = false

    override fun showsFab(): Boolean = false

    override fun onKeyBoardChanged(appeared: Boolean) {
        super.onKeyBoardChanged(appeared)
        if (!appeared && isBottomSheetShowing) hideBottomSheet()
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
        toggleProgress(true)
        disposables.add(gameViewModel.headToHead(request).subscribe({ summary -> updateHeadToHead(summary.wins, summary.draws, summary.losses) }, ErrorHandler.EMPTY::accept))
        disposables.add(gameViewModel.getMatchUps(request).subscribe({ diffResult ->
            toggleProgress(false)
            scrollManager.onDiff(diffResult)
        }, defaultErrorHandler::accept))
    }

    private fun updateHeadToHead(numWins: Int, numDraws: Int, numLosses: Int) {
        wins?.text = getText(R.string.game_wins, numWins)
        draws?.text = getText(R.string.game_draws, numDraws)
        losses?.text = getText(R.string.game_losses, numLosses)
    }

    private fun updateCompetitor(item: Competitive) {
        if (isHome) request.updateHome(item)
        else request.updateAway(item)
        searchScrollManager?.notifyDataSetChanged()
        hideKeyboard()
    }

    private fun findCompetitor() {
        if (request.hasInvalidType()) return showSnackbar(getString(R.string.game_select_tournament_type))

        val refPath = request.refPath
        val isBetweenUsers = User.COMPETITOR_TYPE == refPath

        var fragment: BaseFragment? = null

        if (isBetweenUsers) fragment = UserSearchFragment.newInstance()
        else if (Team.COMPETITOR_TYPE == refPath) fragment = TeamSearchFragment.newInstance(request.sport)

        fragment ?: return
        fragment.setTargetFragment(this, R.id.request_competitor_pick)

        showBottomSheet(BottomSheetController.Args.builder()
                .setMenuRes(R.menu.empty)
                .setFragment(fragment)
                .build())
    }

    private fun getText(@StringRes stringRes: Int, count: Int): CharSequence {
        return SpanBuilder.of(count.toString()).resize(1.4f).bold()
                .append(SpanBuilder.of(getString(stringRes))
                        .prependNewLine()
                        .build()).build()
    }

    companion object {

        fun newInstance(): HeadToHeadFragment {
            val fragment = HeadToHeadFragment()
            val args = Bundle()

            fragment.arguments = args
            fragment.setEnterExitTransitions()
            return fragment
        }
    }
}
