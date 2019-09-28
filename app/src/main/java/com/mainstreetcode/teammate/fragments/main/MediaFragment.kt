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

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.MediaTransferIntentService
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.MediaAdapter
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.MediaViewHolder
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseFragment
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment
import com.mainstreetcode.teammate.fragments.headless.TeamPickerFragment
import com.mainstreetcode.teammate.model.Media
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.util.getTransitionName
import com.tunjid.androidx.recyclerview.InteractiveViewHolder
import com.tunjid.androidx.recyclerview.diff.Differentiable

class MediaFragment : TeammatesBaseFragment(R.layout.fragment_media),
        MediaAdapter.MediaAdapterListener,
        ImageWorkerFragment.MediaListener,
        ImageWorkerFragment.DownloadRequester {

    private lateinit var team: Team
    private lateinit var items: List<Differentiable>

    override val isFullScreen: Boolean get() = false

    override val showsFab: Boolean get() = true

    override val stableTag: String
        get() {
            val superResult = super.stableTag
            val tempTeam = arguments!!.getParcelable<Team>(ARG_TEAM)

            return if (tempTeam != null) superResult + "-" + tempTeam.hashCode()
            else superResult
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        team = arguments!!.getParcelable(ARG_TEAM)!!
        items = mediaViewModel.getModelList(team)

        ImageWorkerFragment.attach(this)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (!mediaViewModel.hasSelections(team)) {
                isEnabled = false
                activity?.onBackPressed()
                return@addCallback
            }
            mediaViewModel.clearSelections(team)
            scrollManager.notifyDataSetChanged()
            toggleContextMenu(false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultUi(
                toolbarTitle = getString(R.string.media_title, team.name),
                toolBarMenu = R.menu.fragment_media,
                altToolbarTitle = getString(R.string.multi_select, mediaViewModel.getNumSelected(team)),
                altToolBarMenu = R.menu.fragment_media_context,
                fabText = R.string.media_add,
                fabIcon = R.drawable.ic_add_white_24dp,
                fabShows = showsFab
        )

        val refreshAction = { disposables.add(mediaViewModel.refresh(team).subscribe(this::onMediaUpdated, defaultErrorHandler::invoke)).let { Unit } }

        scrollManager = ScrollManager.with<InteractiveViewHolder<*>>(view.findViewById(R.id.team_media))
                .withPlaceholder(EmptyViewHolder(view, R.drawable.ic_video_library_black_24dp, R.string.no_media))
                .withRefreshLayout(view.findViewById(R.id.refresh_layout), refreshAction)
                .withEndlessScroll { fetchMedia(false) }
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(MediaAdapter(items, this))
                .withGridLayoutManager(4)
                .build()
    }

    override fun onResume() {
        super.onResume()
        fetchMedia(true)
        toggleContextMenu(mediaViewModel.hasSelections(team))
        disposables.add(mediaViewModel.listenForUploads().subscribe(this::onMediaUpdated, emptyErrorHandler::invoke))
    }

    private fun fetchMedia(fetchLatest: Boolean) {
        if (fetchLatest) scrollManager.setRefreshing()
        else transientBarDriver.toggleProgress(true)

        disposables.add(mediaViewModel.getMany(team, fetchLatest).subscribe(this::onMediaUpdated, defaultErrorHandler::invoke))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_pick_team -> TeamPickerFragment.change(requireActivity(), R.id.request_media_team_pick).let { true }

        R.id.action_delete -> disposables.add(mediaViewModel.deleteMedia(team, localRoleViewModel.hasPrivilegedRole())
                .subscribe(this::onMediaDeleted, defaultErrorHandler::invoke)).let { true }

        R.id.action_download ->
            if (ImageWorkerFragment.requestDownload(this, team)) scrollManager.notifyDataSetChanged().let { true }
            else true

        else -> super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> ImageWorkerFragment.requestMultipleMedia(this)
        }
    }

    override fun augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment) {
        if (incomingFragment is MediaDetailFragment) {
            val media = incomingFragment.arguments!!.getParcelable<Media>(MediaDetailFragment.ARG_MEDIA)
                    ?: return

            val holder = scrollManager.findViewHolderForItemId(media.hashCode().toLong()) as? MediaViewHolder<*>
                    ?: return

            holder.bind(media) // Rebind, to make sure transition names remain.
            transaction
                    .addSharedElement(holder.itemView, media.getTransitionName(R.id.fragment_media_background))
                    .addSharedElement(holder.thumbnailView, media.getTransitionName(R.id.fragment_media_thumbnail))
        }
    }

    override fun onMediaClicked(item: Media) {
        if (mediaViewModel.hasSelections(team))
            longClickMedia(item)
        else {
            updateUi(bottomNavShows = false)
            navigator.show(MediaDetailFragment.newInstance(item))
        }
    }

    override fun onMediaLongClicked(media: Media): Boolean {
        val result = mediaViewModel.select(media)
        val hasSelections = mediaViewModel.hasSelections(team)

        toggleContextMenu(hasSelections)
        return result
    }

    override fun isSelected(media: Media): Boolean = mediaViewModel.isSelected(media)

    override fun onFilesSelected(uris: List<Uri>) =
            MediaTransferIntentService.startActionUpload(requireContext(), userViewModel.currentUser, team, uris)

    override fun requestedTeam(): Team = team

    override fun startedDownLoad(started: Boolean) {
        toggleContextMenu(!started)
        if (started) scrollManager.notifyDataSetChanged()
    }

    private fun onMediaUpdated(result: DiffUtil.DiffResult) {
        scrollManager.onDiff(result)
        transientBarDriver.toggleProgress(false)
    }

    private fun toggleContextMenu(show: Boolean) = updateUi(
            altToolBarShows = show,
            altToolbarTitle = getString(R.string.multi_select, mediaViewModel.getNumSelected(team))
    )

    private fun longClickMedia(media: Media) {
        val holder = scrollManager.findViewHolderForItemId(media.hashCode().toLong()) as? MediaViewHolder<*>
                ?: return

        holder.performLongClick()
    }

    private fun onMediaDeleted(pair: Pair<Boolean, DiffUtil.DiffResult>) {
        toggleContextMenu(false)

        val (partialDelete, diffResult) = pair

        scrollManager.onDiff(diffResult)
        if (!partialDelete) return

        scrollManager.notifyDataSetChanged()
        scrollManager.recyclerView?.postDelayed({ transientBarDriver.showSnackBar(getString(R.string.partial_delete_message)) }, MEDIA_DELETE_SNACKBAR_DELAY.toLong())
    }

    companion object {

        private const val MEDIA_DELETE_SNACKBAR_DELAY = 350
        private const val ARG_TEAM = "team"

        fun newInstance(team: Team): MediaFragment = MediaFragment().apply { arguments = bundleOf(ARG_TEAM to team) }
    }
}
