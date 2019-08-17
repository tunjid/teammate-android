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

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.MediaAdapter
import com.mainstreetcode.teammate.adapters.viewholders.ImageMediaViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.MediaViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.VideoMediaViewHolder
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.model.Media
import com.mainstreetcode.teammate.util.isDisplayingSystemUI
import com.tunjid.androidbootstrap.view.util.InsetFlags
import java.util.concurrent.atomic.AtomicBoolean


class MediaDetailFragment : MainActivityFragment(), MediaAdapter.MediaAdapterListener {

    private lateinit var media: Media
    private var mediaViewHolder: MediaViewHolder<*>? = null
    private val systemUiStatus: AtomicBoolean = AtomicBoolean()

    override val toolbarMenu: Int
        get() = R.menu.fragment_media_detail

    override val navBarColor: Int
        get() = Color.TRANSPARENT

    override val isFullScreen: Boolean
        get() = true

    override fun getStableTag(): String =
            super.getStableTag() + "-" + arguments!!.getParcelable(ARG_MEDIA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        media = arguments!!.getParcelable(ARG_MEDIA)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val isImage = media.isImage
        val resource = if (isImage) R.layout.viewholder_image else R.layout.viewholder_video

        return inflater.inflate(resource, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        disposables.add(mediaViewModel.getMedia(media).subscribe(this::checkMediaFlagged, defaultErrorHandler::invoke))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId != R.id.action_flag_media) return super.onOptionsItemSelected(item)

        val activity = activity ?: return true

        AlertDialog.Builder(activity)
                .setTitle(R.string.flag_media)
                .setMessage(R.string.flag_media_message)
                .setPositiveButton(R.string.yes) { _, _ -> disposables.add(mediaViewModel.flagMedia(media).subscribe(this::checkMediaFlagged, defaultErrorHandler::invoke)) }
                .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                .show()

        return true
    }

    override fun onResume() {
        super.onResume()
        val root = view
        if (root != null) bindViewHolder(root, media, media.isImage)
    }

    override fun onPause() {
        mediaViewHolder?.unBind()
        super.onPause()
    }

    override fun onDestroyView() {
        mediaViewHolder = null
        super.onDestroyView()
    }

    override fun insetFlags(): InsetFlags = NONE

    override fun showsFab(): Boolean = false

    override fun showsBottomNav(): Boolean = false

    override fun showsToolBar(): Boolean = true

    override fun showsSystemUI(): Boolean = systemUiStatus.get()

    override fun hasLightNavBar(): Boolean = false

    override fun onMediaClicked(item: Media) {
        val activity = activity ?: return

        systemUiStatus.set(activity.window.decorView.isDisplayingSystemUI())
        togglePersistentUi()
    }

    override fun onMediaLongClicked(media: Media): Boolean = false

    override fun isSelected(media: Media): Boolean = false

    override fun onFillLoaded() {
        togglePersistentUi()
    }

    private fun checkMediaFlagged(media: Media) {
        if (!media.isFlagged) return
        showSnackbar(getString(R.string.media_flagged))

        val activity = activity
        activity?.onBackPressed()
    }

    private fun bindViewHolder(root: View, media: Media, isImage: Boolean) {
        mediaViewHolder = if (isImage) ImageMediaViewHolder(root, this)
        else VideoMediaViewHolder(root, this)

        mediaViewHolder?.fullBind(media)
        onMediaClicked(media)
    }

    companion object {

        internal const val ARG_MEDIA = "media"

        fun newInstance(media: Media): MediaDetailFragment = MediaDetailFragment().apply {
            arguments = bundleOf(ARG_MEDIA to media)
            setEnterExitTransitions()
        }
    }
}
