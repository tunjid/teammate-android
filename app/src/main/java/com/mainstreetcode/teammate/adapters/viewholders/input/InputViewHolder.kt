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
import android.text.TextUtils.isEmpty
import android.text.TextWatcher
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.view.doOnNextLayout
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.resolveThemeColor
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers

open class InputViewHolder<T : ImageWorkerFragment.ImagePickerListener>(itemView: View) : BaseViewHolder<T>(itemView), TextWatcher {

    private var lastLineCount = 1

    protected val hint: TextView = itemView.findViewById(R.id.hint)
    protected val text: EditText = itemView.findViewById(R.id.input)
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
        text.setOnFocusChangeListener { _, hasFocus ->
            tintHint(hasFocus)
            scaleHint(!hasFocus && isEmpty(text.text))
        }
    }

    override fun clear() {
        button.setOnClickListener(null)
        text.setOnClickListener(null)
        text.removeTextChangedListener(this)

        textInputStyle?.viewHolder = null
        textInputStyle = null

        super.clear()
    }

    override fun onDetached() {
        button.visibility = GONE
        super.onDetached()
    }

    open fun bind(inputStyle: TextInputStyle) {
        this.textInputStyle = inputStyle
        inputStyle.viewHolder = this

        val item = inputStyle.item

        val newInputType = item.inputType
        val oldInputType = text.inputType

        val isEditable = inputStyle.isEditable
        val isSelector = inputStyle.isSelector

        val isEnabled = text.isEnabled
        val isClickable = text.isClickable
        val isFocusable = text.isFocusable
        val isFocusableInTouchMode = text.isFocusableInTouchMode

        val newValue = item.getValue().toString()
        val oldValue = text.text.toString()

        val oldHint = hint.text
        val newHint = itemView.context.getString(item.stringRes)

        if (isEnabled != isEditable) text.isEnabled = isEditable
        if (isClickable != isSelector) text.isClickable = isSelector
        if (isFocusable == isSelector) text.isFocusable = !isSelector
        if (isFocusableInTouchMode == isSelector) text.isFocusableInTouchMode = !isSelector

        if (oldHint != newHint) hint.text = newHint
        if (oldValue != newValue) text.setText(newValue)
        if (oldInputType != newInputType) text.inputType = newInputType

        text.setOnClickListener(inputStyle.textClickListener())
        button.setOnClickListener(inputStyle.buttonClickListener())

        text.removeTextChangedListener(this)
        if (!isSelector) text.addTextChangedListener(this)

        updateButton()
        checkForErrors()
        setTintAlpha(text.hasFocus())
        hint.doOnNextLayout { scaleHint(isEmpty(text.text)) }
    }

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {/* Nothing */
    }

    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {/* Nothing */
    }

    override fun afterTextChanged(editable: Editable) {
        val currentLineCount = text.lineCount
        if (lastLineCount != currentLineCount) hint.doOnNextLayout { scaleHint(false) }
        lastLineCount = currentLineCount

        if (textInputStyle == null || textInputStyle!!.isSelector) return
        textInputStyle?.item?.setValue(editable.toString())
        checkForErrors()
    }

    internal open fun updateText(text: CharSequence) {
        this.text.setText(text)
        checkForErrors()
        hint.doOnNextLayout { scaleHint(isEmpty(text)) }
    }

    private fun checkForErrors() {
        if (textInputStyle == null) return

        val errorMessage = textInputStyle!!.errorText()
        if (errorMessage == text.error) return

        if (isEmpty(errorMessage))
            text.error = null
        else
            text.error = errorMessage
    }

    private fun updateButton() {
        if (textInputStyle == null) return

        val newIcon = textInputStyle!!.icon

        val oldVisibility = button.visibility
        val newVisibility = if (newIcon == 0) GONE else VISIBLE

        if (oldVisibility != newVisibility) button.visibility = newVisibility
        if (newIcon != 0)
            disposables.add(Single.fromCallable { getDrawable(text.context, newIcon) }
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
        val end = hint.context.resolveThemeColor(if (hasFocus) R.attr.colorAccent else R.attr.input_text_color)
        val animator = ValueAnimator.ofObject(ArgbEvaluator(), start, end)

        animator.duration = HINT_ANIMATION_DURATION.toLong()
        animator.addUpdateListener { animation -> hint.setTextColor(animation.animatedValue as Int) }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) = setTintAlpha(hasFocus)
        })
        animator.start()
    }

    private fun setTintAlpha(hasFocus: Boolean) {
        hint.alpha = if (textInputStyle != null && !textInputStyle!!.isEditable && !hasFocus) 0.38f else 1f
    }

    companion object {

        private const val HINT_ANIMATION_DURATION = 200
        private const val HINT_SHRINK_SCALE = 0.8f
        private const val HALF = 0.5f
    }
}
