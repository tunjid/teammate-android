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

package com.mainstreetcode.teammate.util.nav

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.RemoteImageAdapter
import com.mainstreetcode.teammate.adapters.TeamAdapter
import com.mainstreetcode.teammate.adapters.viewholders.RemoteImageViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.TeamViewHolder
import com.mainstreetcode.teammate.fragments.main.TeamMembersFragment
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.viewmodel.TeamViewModel
import com.tunjid.androidbootstrap.core.components.Navigator
import com.tunjid.androidbootstrap.core.components.activityNavigationController
import io.reactivex.disposables.CompositeDisposable

class NavDialogFragment : BottomSheetDialogFragment() {

    private val navigator: Navigator by activityNavigationController()

    private lateinit var teamViewModel: TeamViewModel
    private lateinit var disposables: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppTheme_NavDialogTheme)

        disposables = CompositeDisposable()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        teamViewModel = ViewModelProviders.of(requireActivity()).get(TeamViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_bottom_nav, container, false)
        val itemView = root.findViewById<View>(R.id.item_container)
        val teamViewHolder = TeamViewHolder(itemView, TeamAdapter.AdapterListener.asSAM(this::viewTeam))
        val navigationView = root.findViewById<NavigationView>(R.id.bottom_nav_view)

        val current = teamViewModel.defaultTeam
        teamViewHolder.bind(current)

        val list = mutableListOf<Team>()

        val scrollManager = ScrollManager.with<RemoteImageViewHolder<Team>>(root.findViewById(R.id.horizontal_list))
                .withCustomLayoutManager(LinearLayoutManager(root.context, RecyclerView.HORIZONTAL, false))
                .withAdapter(RemoteImageAdapter(list, object : RemoteImageAdapter.AdapterListener<Team> {
                    override fun onImageClicked(item: Team) {
                        teamViewModel.updateDefaultTeam(item)
                        viewTeam(item)
                    }
                }))
                .build()

        itemView.elevation = 0f
        navigationView.elevation = 0f
        navigationView.setNavigationItemSelectedListener(this::onOptionsItemSelected)

        val cornerSize = resources.getDimensionPixelSize(R.dimen.single_and_half_margin)
        val elevation = resources.getDimensionPixelSize(R.dimen.single_margin).toFloat()

        root.background = MaterialShapeDrawable.createWithElevationOverlay(itemView.context, elevation).apply {
            shapeAppearanceModel = ShapeAppearanceModel.builder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, cornerSize)
                    .setTopRightCorner(CornerFamily.ROUNDED, cornerSize)
                    .build()
        }

        root.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) = Unit

            override fun onViewDetachedFromWindow(v: View) = disposables.clear()
        })

        disposables.add(teamViewModel.nonDefaultTeams(list).subscribe(scrollManager::onDiff, ErrorHandler.EMPTY::invoke))
        disposables.add(teamViewModel.gofer(current).get().subscribe({ teamViewHolder.bind(teamViewModel.defaultTeam) }, Throwable::printStackTrace))

        return root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        dismiss()
        return requireActivity().onOptionsItemSelected(item)
    }

    private fun viewTeam(team: Team) {
        dismiss()
        navigator.show(TeamMembersFragment.newInstance(team))
    }

    companion object {

        fun newInstance(): NavDialogFragment = NavDialogFragment().apply { arguments = Bundle() }
    }
}
