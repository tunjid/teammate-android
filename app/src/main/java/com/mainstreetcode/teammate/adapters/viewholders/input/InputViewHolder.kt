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

package com.mainstreetcode.teammate.adapters.viewholders.input

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.RecyclerView
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.resolveThemeColor
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

open class InputViewHolder(
        itemView: View
) : RecyclerView.ViewHolder(itemView), TextWatcher {

    private val disposables = CompositeDisposable()

    private var lastLineCount = 1

    protected val hint: TextView = itemView.findViewById(R.id.hint)
    private val textField: EditText = itemView.findViewById(R.id.input)
    private val button: ImageButton = itemView.findViewById(R.id.button)

    internal var textInputStyle: TextInputStyle? = null

    protected open val hintLateralTranslation: Float
        get() {
            val width = hint.width
            return -((width - HINT_SHRINK_SCALE * width) * HALF)
        }

    protected open val hintLongitudinalTranslation: Float
        get() = -((itemView.height - hint.height) * HALF)

    init {
        textField.setOnFocusChangeListener { _, hasFocus ->
            tintHint(hasFocus)
            scaleHint(!hasFocus && textField.text.isBlank())
        }
    }

    fun clear() {
        button.setOnClickListener(null)
        textField.setOnClickListener(null)
        textField.removeTextChangedListener(this)

        textInputStyle?.viewHolder = null
        textInputStyle = null

        disposables.clear()
    }

    fun onDetached() {
        button.visibility = GONE
    }

    open fun bind(inputStyle: TextInputStyle) {
        this.textInputStyle = inputStyle
        inputStyle.viewHolder = this

        val item = inputStyle.item

        val newInputType = item.inputType
        val oldInputType = textField.inputType

        val isEditable = inputStyle.isEditable
        val isSelector = inputStyle.isSelector

        val isEnabled = textField.isEnabled
        val isClickable = textField.isClickable
        val isFocusable = textField.isFocusable
        val isFocusableInTouchMode = textField.isFocusableInTouchMode

        val newValue = item.formattedValue.toString()
        val oldValue = textField.text.toString()

        val oldHint = hint.text
        val newHint = itemView.context.getString(item.stringRes)

        if (isEnabled != isEditable) textField.isEnabled = isEditable
        if (isClickable != isSelector) textField.isClickable = isSelector
        if (isFocusable == isSelector) textField.isFocusable = !isSelector
        if (isFocusableInTouchMode == isSelector) textField.isFocusableInTouchMode = !isSelector

        if (oldHint != newHint) hint.text = newHint
        if (oldValue != newValue) textField.setText(newValue)
        if (oldInputType != newInputType) textField.inputType = newInputType

        textField.setOnClickListener(inputStyle.textClickListener())
        button.setOnClickListener(inputStyle.buttonClickListener())

        textField.removeTextChangedListener(this)
        if (!isSelector) textField.addTextChangedListener(this)

        updateButton()
        checkForErrors()
        setTintAlpha(textField.hasFocus())
        hint.doOnLayout { scaleHint(textField.text.isBlank()) }
    }

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit /* Nothing */

    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit /* Nothing */

    override fun afterTextChanged(editable: Editable) {
        val currentLineCount = textField.lineCount
        if (lastLineCount != currentLineCount) hint.doOnLayout { scaleHint(false) }
        lastLineCount = currentLineCount

        if (textInputStyle == null || textInputStyle!!.isSelector) return
        textInputStyle?.item?.rawValue = editable.toString()
        checkForErrors()
    }

    internal open fun updateText(text: CharSequence) {
        this.textField.setText(text)
        checkForErrors()
        hint.doOnLayout { scaleHint(text.isBlank()) }
    }

    private fun checkForErrors() {
        val errorMessage = textInputStyle?.errorText()
        if (errorMessage == textField.error) return

        textField.error = if (errorMessage.isNullOrBlank()) null else errorMessage
    }

    private fun updateButton() {
        if (textInputStyle == null) return

        val newIcon = textInputStyle!!.icon

        val oldVisibility = button.visibility
        val newVisibility = if (newIcon == 0) GONE else VISIBLE

        if (oldVisibility != newVisibility) button.visibility = newVisibility
        if (newIcon != 0)
            disposables.add(Single.fromCallable { getDrawable(textField.context, newIcon) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(mainThread())
                    .subscribe(button::setImageDrawable, ErrorHandler.EMPTY::invoke))
    }

    private fun scaleHint(grow: Boolean) {
        val scale = if (grow) 1F else HINT_SHRINK_SCALE
        val translationX = if (grow) 0F else hintLateralTranslation
        val translationY = if (grow) 0F else hintLongitudinalTranslation

        hint.animate()
                .scaleX(scale)
                .scaleY(scale)
                .translationX(translationX)
                .translationY(translationY)
                .setDuration(HINT_ANIMATION_DURATION.toLong())
                .start()
    }

    private fun tintHint(hasFocus: Boolean) {
        val start = hint.currentTextColor
        val end = hint.context.resolveThemeColor(if (hasFocus) R.attr.colorSecondary else R.attr.main_text_color)

        ValueAnimator.ofObject(ArgbEvaluator(), start, end).apply {
            duration = HINT_ANIMATION_DURATION.toLong()
            addUpdateListener { animation -> hint.setTextColor(animation.animatedValue as Int) }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) = setTintAlpha(hasFocus)
            })
            start()
        }
    }

    private fun setTintAlpha(hasFocus: Boolean) {
        val notEditable = textInputStyle?.isEditable?.not() ?: false
        hint.alpha = if (notEditable && !hasFocus) 0.38f else 1f
    }

    companion object {

        private const val HINT_ANIMATION_DURATION = 200
        private const val HINT_SHRINK_SCALE = 0.8f
        private const val HALF = 0.5f
    }
}
