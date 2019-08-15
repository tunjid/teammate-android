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

package com.mainstreetcode.teammate.adapters.viewholders

import android.graphics.Typeface
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.GameAdapter
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.util.AppBarListener
import com.mainstreetcode.teammate.util.ViewHolderUtil
import com.mainstreetcode.teammate.util.ViewHolderUtil.Companion.THUMBNAIL_SIZE
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.math.min


class GameViewHolder(
        itemView: View,
        adapterListener: GameAdapter.AdapterListener
) : BaseViewHolder<GameAdapter.AdapterListener>(itemView, adapterListener) {


    private lateinit var model: Game

    private val animationPadding: Int = itemView.resources.getDimensionPixelSize(R.dimen.quarter_margin)

    private val highlight: View = itemView.findViewById(R.id.highlight)
    private val ended: TextView = itemView.findViewById(R.id.ended)
    private val score: TextView = itemView.findViewById(R.id.score)
    private val date: TextView = itemView.findViewById(R.id.date)
    private val homeText: TextView = itemView.findViewById(R.id.home)
    private val awayText: TextView = itemView.findViewById(R.id.away)
    private val homeThumbnail: CircleImageView = itemView.findViewById(R.id.home_thumbnail)
    private val awayThumbnail: CircleImageView = itemView.findViewById(R.id.away_thumbnail)

    init {
        itemView.setOnClickListener { adapterListener.onGameClicked(model) }
        ViewHolderUtil.updateForegroundDrawable(itemView)
    }

    fun bind(model: Game) {
        this.model = model
        val home = model.home
        val away = model.away
        val winner = model.winner

        date.text = model.date
        score.text = model.score
        homeText.text = home.name
        awayText.text = away.name
        ended.visibility = if (model.isEnded) View.VISIBLE else View.INVISIBLE
        highlight.visibility = if (model.isEnded) View.INVISIBLE else View.VISIBLE
        homeText.setTypeface(homeText.typeface, if (home == winner) Typeface.BOLD else Typeface.NORMAL)
        awayText.setTypeface(awayText.typeface, if (away == winner) Typeface.BOLD else Typeface.NORMAL)

        tintScore()
        val homeUrl = home.imageUrl
        val awayUrl = away.imageUrl

        if (TextUtils.isEmpty(homeUrl))
            homeThumbnail.setImageResource(R.color.dark_grey)
        else
            Picasso.get().load(homeUrl)
                    .resize(THUMBNAIL_SIZE, THUMBNAIL_SIZE).centerInside().into(homeThumbnail)

        if (TextUtils.isEmpty(awayUrl))
            awayThumbnail.setImageResource(R.color.dark_grey)
        else
            Picasso.get().load(awayUrl).resize(THUMBNAIL_SIZE, THUMBNAIL_SIZE).centerInside().into(awayThumbnail)
    }

    fun animate(props: AppBarListener.OffsetProps) {
        if (props.appBarUnmeasured()) return

        val offset = props.offset
        val fraction = props.fraction
        val scale = ONE_F - fraction * 1.8f
        val drop = min(offset, (homeText.y - homeThumbnail.y).toInt() - animationPadding)

        if (scale < 0 || scale > 1) return

        homeText.alpha = scale
        awayText.alpha = scale
        homeThumbnail.alpha = scale
        awayThumbnail.alpha = scale
        homeThumbnail.scaleX = scale
        awayThumbnail.scaleX = scale
        homeThumbnail.scaleY = scale
        awayThumbnail.scaleY = scale
        homeThumbnail.translationY = drop.toFloat()
        awayThumbnail.translationY = drop.toFloat()
    }

    private fun tintScore() {
        val noTournament = model.tournament.isEmpty
        val borderWidth = if (noTournament) 0 else itemView.resources.getDimensionPixelSize(R.dimen.sixteenth_margin)
        homeThumbnail.borderWidth = borderWidth
        awayThumbnail.borderWidth = borderWidth
    }

    companion object {

        private const val ONE_F = 1f
    }
}
