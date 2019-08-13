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
import android.text.Editable
import android.text.TextUtils.isEmpty
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.viewpager.widget.ViewPager.SCROLL_STATE_DRAGGING
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.TeamChatAdapter
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.TeamChatViewHolder
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.fragments.headless.TeamPickerFragment
import com.mainstreetcode.teammate.model.Chat
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.util.Deferrer
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.ScrollManager
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import com.tunjid.androidbootstrap.view.animator.ViewHider
import com.tunjid.androidbootstrap.view.animator.ViewHider.TOP
import io.reactivex.disposables.Disposable
import kotlin.math.abs

class ChatFragment : MainActivityFragment(), TextView.OnEditorActionListener, TeamChatAdapter.ChatAdapterListener {

    private var wasScrolling: Boolean = false
    private var unreadCount: Int = 0

    private lateinit var team: Team
    private lateinit var items: MutableList<Differentiable>
    private lateinit var chatDisposable: Disposable

    private var dateView: TextView? = null
    private var newMessages: TextView? = null

    private var deferrer: Deferrer? = null
    private var dateHider: ViewHider? = null
    private var newMessageHider: ViewHider? = null

    override val toolbarMenu: Int
        get() = R.menu.fragment_chat

    override val toolbarTitle: CharSequence
        get() = getString(R.string.team_chat_title, team.name)

    private val isSubscribedToChat: Boolean
        get() = ::chatDisposable.isInitialized && !chatDisposable.isDisposed

    private val isNearBottomOfChat: Boolean
        get() {
            val lastVisibleItemPosition = scrollManager.lastVisiblePosition

            return abs(items.size - lastVisibleItemPosition) < 4
        }

    override fun getStableTag(): String {
        val superResult = super.getStableTag()
        val team = arguments?.getParcelable<Team>(ARG_TEAM)

        return if (team != null) superResult + "-" + team.hashCode()
        else superResult
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        team = arguments!!.getParcelable(ARG_TEAM)!!
        items = chatViewModel.getModelList(team)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_chat, container, false)
        val refresh = rootView.findViewById<SwipeRefreshLayout>(R.id.refresh_layout)
        val input = rootView.findViewById<EditText>(R.id.input)
        val send = rootView.findViewById<View>(R.id.send)
        dateView = rootView.findViewById(R.id.date)
        newMessages = rootView.findViewById(R.id.new_messages)

        dateHider = ViewHider.of(dateView).setDirection(TOP).build()
        newMessageHider = ViewHider.of(newMessages).setDirection(ViewHider.BOTTOM).build()
        deferrer = Deferrer(2000) { dateHider?.hide() }

        scrollManager = ScrollManager.with<TeamChatViewHolder>(rootView.findViewById(R.id.chat))
                .withPlaceholder(EmptyViewHolder(rootView, R.drawable.ic_message_black_24dp, R.string.no_chats))
                .onLayoutManager { layoutManager -> (layoutManager as LinearLayoutManager).stackFromEnd = true }
                .withAdapter(TeamChatAdapter(items, userViewModel.currentUser, this))
                .withEndlessScroll { fetchChatsBefore(false) }
                .withRefreshLayout(refresh) { refresh.isRefreshing = false }
                .addScrollListener { _, _ -> updateTopSpacerElevation() }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .addStateListener(this::onScrollStateChanged)
                .addScrollListener(this::onScroll)
                .withLinearLayoutManager()
                .build()

        newMessages?.setOnClickListener { scrollManager.withRecyclerView { rv -> rv.smoothScrollToPosition(items.size - 1) } }
        dateView?.setOnClickListener { dateHider?.hide() }
        send.setOnClickListener { sendChat(input) }
        input.setOnEditorActionListener(this)
        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                wasScrolling = false
            }
        })

        newMessageHider?.hide()
        dateHider?.hide()
        return rootView
    }

    override fun onResume() {
        super.onResume()
        subscribeToChat()
        fetchChatsBefore(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_pick_team -> {
                TeamPickerFragment.change(requireActivity(), R.id.request_chat_team_pick)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        chatViewModel.updateLastSeen(team)
        newMessageHider = null
        newMessages = null
        dateHider = null
        dateView = null
        deferrer = null
    }

    override fun showsFab(): Boolean {
        return false
    }

    override fun staticViews(): IntArray {
        return EXCLUDED_VIEWS
    }

    override fun onChatClicked(chat: Chat) {
        if (!chat.isEmpty) return

        val index = items.indexOf(chat)
        if (index == -1) return

        items.removeAt(index)
        scrollManager.notifyItemRemoved(index)

        postChat(chat)
    }

    override fun onEditorAction(textView: TextView, actionId: Int, event: KeyEvent): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE || event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
            sendChat(textView)
            return true
        }
        return false
    }

    private fun fetchChatsBefore(fetchLatest: Boolean) {
        scrollManager.setRefreshing()
        disposables.add(chatViewModel.getMany(team, fetchLatest).subscribe(this::onChatsUpdated, defaultErrorHandler::accept))
    }

    private fun subscribeToChat() {
        chatDisposable = chatViewModel.listenForChat(team).subscribe({ chat ->
            items.add(chat)
            val nearBottomOfChat = isNearBottomOfChat
            unreadCount = if (nearBottomOfChat) 0 else unreadCount + 1
            notifyAndScrollToLast(nearBottomOfChat)
            updateUnreadCount()
        }, ErrorHandler.builder()
                .defaultMessage(getString(R.string.error_default))
                .add { message -> showSnackbar(message.message) }
                .build()::accept)

        disposables.add(chatDisposable)
    }

    private fun sendChat(textView: TextView) {
        val text = textView.text.toString()
        textView.text = null

        if (isEmpty(text)) return

        val chat = Chat.chat(text, userViewModel.currentUser, team)
        items.add(chat)

        wasScrolling = false
        notifyAndScrollToLast(true)
        postChat(chat)
    }

    private fun postChat(chat: Chat) {
        disposables.add(chatViewModel.post(chat).subscribe({
            chatViewModel.updateLastSeen(team)
            val index = items.indexOf(chat)

            if (index != -1) scrollManager.notifyItemChanged(index)
            if (!isSubscribedToChat) subscribeToChat()
        }, ErrorHandler.builder()
                .defaultMessage(getString(R.string.error_default))
                .add {
                    val index = items.indexOf(chat)
                    if (index != -1) scrollManager.notifyItemChanged(index)
                }
                .build()::accept))
    }

    private fun notifyAndScrollToLast(scrollToLast: Boolean) {
        val index = items.size - 1

        val recyclerView = scrollManager.recyclerView ?: return

        scrollManager.notifyItemInserted(index)
        if (scrollToLast) recyclerView.smoothScrollToPosition(index)
    }

    private fun onChatsUpdated(result: DiffUtil.DiffResult?) {
        toggleProgress(false)
        chatViewModel.updateLastSeen(team)
        if (result != null) scrollManager.onDiff(result)
    }

    private fun onScroll(dx: Int, dy: Int) {
        if (abs(dy) > 8) wasScrolling = true

        deferrer?.advanceDeadline()
        val date = chatViewModel.onScrollPositionChanged(team, scrollManager.firstVisiblePosition)

        if (date.isBlank()) dateHider?.hide()
        else dateHider?.show()

        val ref = dateView ?: return
        if (date == ref.text.toString()) return

        TransitionManager.beginDelayedTransition(
                ref.parent as ViewGroup,
                AutoTransition().addTarget(ref))

        ref.text = date
    }

    private fun onScrollStateChanged(newState: Int) {
        if (newState == SCROLL_STATE_DRAGGING) deferrer?.advanceDeadline()
        if (wasScrolling && newState == SCROLL_STATE_IDLE && isNearBottomOfChat) {
            unreadCount = 0
            updateUnreadCount()
            fetchChatsBefore(true)
        }
    }

    private fun updateUnreadCount() {
        if (unreadCount == 0)
            newMessageHider?.hide()
        else {
            newMessages?.text = if (unreadCount == 1) getString(R.string.chat_new_message) else getString(R.string.chat_new_messages, unreadCount)
            newMessageHider?.show()
        }
    }

    companion object {

        private const val ARG_TEAM = "team"
        private val EXCLUDED_VIEWS = intArrayOf(R.id.chat)

        fun newInstance(team: Team): ChatFragment = ChatFragment().apply { arguments = bundleOf(ARG_TEAM to team) }
    }
}
