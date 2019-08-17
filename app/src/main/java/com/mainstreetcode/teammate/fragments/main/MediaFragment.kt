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

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.MediaTransferIntentService
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.MediaAdapter
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.MediaViewHolder
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment
import com.mainstreetcode.teammate.model.Media
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.util.getTransitionName
import com.mainstreetcode.teammate.util.yes
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import java.util.concurrent.atomic.AtomicBoolean

class MediaFragment : MainActivityFragment(), MediaAdapter.MediaAdapterListener, ImageWorkerFragment.MediaListener, ImageWorkerFragment.DownloadRequester {

    private lateinit var team: Team
    private lateinit var items: List<Differentiable>
    private val bottomBarState: AtomicBoolean = AtomicBoolean()
    private val altToolBarState: AtomicBoolean = AtomicBoolean()

    override val fabStringResource: Int
        @StringRes
        get() = R.string.media_add

    override val fabIconResource: Int
        @DrawableRes
        get() = R.drawable.ic_add_white_24dp

    override val altToolbarMenu: Int
        get() = R.menu.fragment_media_context

    override val toolbarTitle: CharSequence
        get() = getString(R.string.media_title, team.name)

    override val altToolbarTitle: CharSequence
        get() = getString(R.string.multi_select, mediaViewModel.getNumSelected(team))

    override val toolbarMenu: Int
        get() = R.menu.fragment_media

    override val isFullScreen: Boolean
        get() = false

    override fun getStableTag(): String {
        val superResult = super.getStableTag()
        val tempTeam = arguments!!.getParcelable<Team>(ARG_TEAM)

        return if (tempTeam != null) superResult + "-" + tempTeam.hashCode()
        else superResult
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        team = arguments!!.getParcelable(ARG_TEAM)!!
        items = mediaViewModel.getModelList(team)

        ImageWorkerFragment.attach(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_media, container, false)

        val refreshAction = Runnable { disposables.add(mediaViewModel.refresh(team).subscribe(this::onMediaUpdated, defaultErrorHandler::invoke)) }

        scrollManager = ScrollManager.with<InteractiveViewHolder<*>>(rootView.findViewById(R.id.team_media))
                .withPlaceholder(EmptyViewHolder(rootView, R.drawable.ic_video_library_black_24dp, R.string.no_media))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), refreshAction)
                .withEndlessScroll { fetchMedia(false) }
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(MediaAdapter(items, this))
                .withGridLayoutManager(4)
                .build()

        bottomBarState.set(true)

        return rootView
    }

    override fun onResume() {
        super.onResume()
        fetchMedia(true)
        toggleContextMenu(mediaViewModel.hasSelections(team))
        disposables.add(mediaViewModel.listenForUploads().subscribe(this::onMediaUpdated, emptyErrorHandler::invoke))
    }

    private fun fetchMedia(fetchLatest: Boolean) {
        if (fetchLatest)
            scrollManager.setRefreshing()
        else
            toggleProgress(true)

        disposables.add(mediaViewModel.getMany(team, fetchLatest).subscribe(this::onMediaUpdated, defaultErrorHandler::invoke))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_pick_team -> yes

        R.id.action_delete -> disposables.add(mediaViewModel.deleteMedia(team, localRoleViewModel.hasPrivilegedRole())
                .subscribe(this::onMediaDeleted, defaultErrorHandler::invoke))

        R.id.action_download -> {
            if (ImageWorkerFragment.requestDownload(this, team)) scrollManager.notifyDataSetChanged()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun handledBackPress(): Boolean {
        if (!mediaViewModel.hasSelections(team)) return false
        mediaViewModel.clearSelections(team)
        scrollManager.notifyDataSetChanged()
        toggleContextMenu(false)
        return true
    }

    override fun showsFab(): Boolean = true

    override fun showsAltToolBar(): Boolean = altToolBarState.get()

    override fun showsBottomNav(): Boolean = bottomBarState.get()

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> ImageWorkerFragment.requestMultipleMedia(this)
        }
    }

    @SuppressLint("CommitTransaction")
    override fun provideFragmentTransaction(fragmentTo: BaseFragment): FragmentTransaction? {
        if (fragmentTo.stableTag.contains(MediaDetailFragment::class.java.simpleName)) {
            val media = fragmentTo.arguments!!.getParcelable<Media>(MediaDetailFragment.ARG_MEDIA)
                    ?: return null

            val holder = scrollManager.findViewHolderForItemId(media.hashCode().toLong()) as? MediaViewHolder<*>
                    ?: return null

            holder.bind(media) // Rebind, to make sure transition names remain.
            return beginTransaction()
                    .addSharedElement(holder.itemView, media.getTransitionName(R.id.fragment_media_background))
                    .addSharedElement(holder.thumbnailView, media.getTransitionName(R.id.fragment_media_thumbnail))
        }
        return null
    }

    override fun onMediaClicked(item: Media) {
        if (mediaViewModel.hasSelections(team))
            longClickMedia(item)
        else {
            bottomBarState.set(false)
            togglePersistentUi()
            showFragment(MediaDetailFragment.newInstance(item))
        }
    }

    override fun onMediaLongClicked(media: Media): Boolean {
        val result = mediaViewModel.select(media)
        val hasSelections = mediaViewModel.hasSelections(team)

        toggleContextMenu(hasSelections)
        return result
    }

    override fun isSelected(media: Media): Boolean = mediaViewModel.isSelected(media)

    override fun onFilesSelected(uris: List<Uri>) {
        MediaTransferIntentService.startActionUpload(requireContext(), userViewModel.currentUser, team, uris)
    }

    override fun requestedTeam(): Team = team

    override fun startedDownLoad(started: Boolean) {
        toggleContextMenu(!started)
        if (started) scrollManager.notifyDataSetChanged()
    }

    private fun onMediaUpdated(result: DiffUtil.DiffResult) {
        scrollManager.onDiff(result)
        toggleProgress(false)
    }

    private fun toggleContextMenu(show: Boolean) {
        altToolBarState.set(show)
        togglePersistentUi()
    }

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
        scrollManager.recyclerView.postDelayed({ showSnackbar(getString(R.string.partial_delete_message)) }, MEDIA_DELETE_SNACKBAR_DELAY.toLong())
    }

    companion object {

        private const val MEDIA_DELETE_SNACKBAR_DELAY = 350
        private const val ARG_TEAM = "team"

        fun newInstance(team: Team): MediaFragment = MediaFragment().apply { arguments = bundleOf(ARG_TEAM to team) }
    }
}
