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

package com.mainstreetcode.teammate.baseclasses


import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat.setTransitionName
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.appbar.AppBarLayout
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.viewholders.HeaderedImageViewHolder
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment
import com.mainstreetcode.teammate.model.BlockedUser
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.HeaderedModel
import com.mainstreetcode.teammate.model.ListableModel
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.AppBarListener
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.getTransitionName
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer
import io.reactivex.Completable.timer
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import java.util.*
import java.util.concurrent.TimeUnit.MILLISECONDS

abstract class HeaderedFragment<T> :
        MainActivityFragment(),
        ImageWorkerFragment.CropListener,
        ImageWorkerFragment.ImagePickerListener where T : HeaderedModel<T>, T : ListableModel<T> {

    private var imageJustCropped: Boolean = false

    private var appBarLayout: AppBarLayout? = null

    protected lateinit var viewHolder: HeaderedImageViewHolder

    protected abstract val headeredModel: T

    protected abstract fun gofer(): Gofer<T>

    protected abstract fun onModelUpdated(result: DiffUtil.DiffResult)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImageWorkerFragment.attach(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val model = headeredModel

        viewHolder = HeaderedImageViewHolder(view, this)
        viewHolder.bind(model)

        setTransitionName(view, model.getTransitionName(R.id.fragment_header_background))
        setTransitionName(viewHolder.thumbnail, model.getTransitionName(R.id.fragment_header_thumbnail))

        view.findViewById<View>(R.id.header).visibility = if (canExpandAppBar()) View.VISIBLE else View.GONE
        view.findViewById<Toolbar>(R.id.header_toolbar).layoutParams.height += TeammatesBaseActivity.topInset

        appBarLayout = view.findViewById<AppBarLayout>(R.id.app_bar)
                .apply { AppBarListener(this, this@HeaderedFragment::onAppBarOffset) }
    }

    override fun onResume() {
        super.onResume()
        fetch()
    }

    override fun onImageClick() {
        viewHolder.bind(headeredModel)

        val errorMessage = gofer().getImageClickMessage(this)

        if (errorMessage == null) ImageWorkerFragment.requestCrop(this)
        else showSnackbar(errorMessage)
    }

    override fun onImageCropped(uri: Uri) {
        val item = headeredModel.headerItem
        uri.path?.apply { item.rawValue = this }
        viewHolder.bind(headeredModel)
        imageJustCropped = true
    }

    override fun onDestroyView() {
        gofer().clear()
        viewHolder.unBind()
        super.onDestroyView()
    }

    protected open fun onPrepComplete() = togglePersistentUi()

    protected open fun canExpandAppBar(): Boolean = true

    protected open fun cantGetModel(): Boolean {
        val result = !ImageWorkerFragment.isPicking(this) && !imageJustCropped
        imageJustCropped = false
        return !result
    }

    override fun onKeyBoardChanged(appeared: Boolean) {
        super.onKeyBoardChanged(appeared)
        if (!appeared) return
        if (appBarLayout != null) appBarLayout!!.setExpanded(false)
        if (showsFab && !isBottomSheetShowing)
            disposables.add(timer(FAB_DELAY.toLong(), MILLISECONDS)
                    .observeOn(mainThread())
                    .subscribe(this::togglePersistentUi, ErrorHandler.EMPTY::invoke))
    }

    protected fun fetch() {
        getData(false)
    }

    protected fun refresh() {
        getData(true)
    }

    private fun getData(refresh: Boolean) {
        checkIfChanged()

        val diffFlowable = gofer().get().doOnComplete { if (refresh) scrollManager.notifyDataSetChanged() }

        if (cantGetModel()) return

        val get = { disposables.add(diffFlowable.subscribe(this::onModelUpdated, defaultErrorHandler::invoke, this::checkIfChanged)) }

        if (refresh) get.invoke()
        else appBarLayout?.postDelayed({ if (view != null) get.invoke() }, 800)
    }

    private fun checkIfChanged() {
        disposables.add(gofer().watchForChange().subscribe({ onPrepComplete() }, ErrorHandler.EMPTY::invoke))
    }

    private fun onAppBarOffset(offsetProps: AppBarListener.OffsetProps) {
        if (!offsetProps.appBarUnmeasured()) updateFabForScrollState(offsetProps.dy)
    }

    protected fun blockUser(user: User, team: Team) {
        val reasons = Config.getBlockReasons()
        val sequences = ArrayList<CharSequence>(reasons.size)

        for (reason in reasons) sequences.add(reason.name)

        AlertDialog.Builder(requireActivity())
                .setTitle(R.string.block_user)
                .setItems(sequences.toTypedArray()) { _, index ->
                    val reason = reasons[index]
                    val request = BlockedUser.block(user, team, reason)

                    disposables.add(blockedUserViewModel.blockUser(request).subscribe(this::onUserBlocked, defaultErrorHandler::invoke))
                    toggleProgress(true)
                }
                .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun onUserBlocked(blockedUser: BlockedUser) {
        showSnackbar(getString(R.string.user_blocked, blockedUser.user.firstName))
        toggleProgress(false)
        activity?.onBackPressed()
    }

    companion object {

        private const val FAB_DELAY = 400
    }
}
