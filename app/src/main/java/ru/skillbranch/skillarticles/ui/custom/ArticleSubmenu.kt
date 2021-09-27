package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.switchmaterial.SwitchMaterial
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.dpToPx
import ru.skillbranch.skillarticles.ui.custom.behaviors.SubmenuBehavior
import kotlin.math.hypot

class ArticleSubmenu @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {
    //settings
    @Px
    private val menuWidth = context.dpToIntPx(200)
    @Px
    private val menuHeight = context.dpToIntPx(96)
    @Px
    private val btnHeight = context.dpToIntPx(40)
    @Px
    private val btnWidth = menuWidth / 2
    @Px
    private val defaultPadding = context.dpToIntPx(16)
    @Px
    private val smallPadding = context.dpToIntPx(8)
    @ColorInt
    private var lineColor: Int = context.getColor(R.color.color_divider)
    @ColorInt
    private val textColor = context.attrValue(R.attr.colorOnSurface)
    private val iconTint = context.getColorStateList(R.color.tint_color)
    @DrawableRes
    private val bg = context.attrValue(R.attr.selectableItemBackground, needRes = true)

    //views
    val btnTextDown: CheckableImageView
    val btnTextUp: CheckableImageView
    val switchMode: SwitchMaterial
    val tvLabel: TextView

    var isOpen = false

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = lineColor
        strokeWidth = 0f
    }

    init {
        id = R.id.submenu
        val elev = context.dpToPx(8)
        val marg = context.dpToIntPx(8)
        layoutParams = CoordinatorLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM or Gravity.RIGHT
            dodgeInsetEdges = Gravity.BOTTOM
            setMargins(0, 0, marg, marg)
        }

        val materialBg = MaterialShapeDrawable.createWithElevationOverlay(context)
        materialBg.elevation = elevation
        background = materialBg
        materialBg.elevation = elev
        elevation = elev
        isVisible = false

        btnTextDown = CheckableImageView(context).apply {
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_title_black_24dp))
            val pad = context.dpToIntPx(12)
            setPadding(pad)
            imageTintList = iconTint
            setBackgroundResource(bg)
        }
        addView(btnTextDown)

        btnTextUp = CheckableImageView(context).apply {
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_title_black_24dp))
            val pad = defaultPadding / 2
            setPadding(pad)
            imageTintList = iconTint
            setBackgroundResource(bg)
        }
        addView(btnTextUp)

        switchMode = SwitchMaterial(context)
        addView(switchMode)

        tvLabel = TextView(context).apply {
            text = "Темный режим"
            setTextColor(textColor)
            textSize = 14f
        }
        addView(tvLabel)
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<ArticleSubmenu> {
        return SubmenuBehavior()
    }

    fun open() {
        if (isOpen || !isAttachedToWindow) return
        isOpen = true
        animatedShow()
    }

    fun close() {
        if (!isOpen || !isAttachedToWindow) return
        isOpen = false
        animatedHide()
    }

    private fun animatedShow() {
        val endRadius = hypot(menuWidth.toFloat(), menuHeight.toFloat()).toInt()
        val va = ViewAnimationUtils.createCircularReveal(
                this,
                menuWidth,
                menuHeight,
                0f,
                endRadius.toFloat()
        )
        va.doOnStart { visibility = View.VISIBLE }
        va.start()
    }

    private fun animatedHide() {
        val endRadius = hypot(menuWidth.toFloat(), menuHeight.toFloat()).toInt()
        val va = ViewAnimationUtils.createCircularReveal(
                this,
                menuWidth,
                menuHeight,
                endRadius.toFloat(),
                0f
        )
        va.doOnEnd { visibility = View.GONE }
        va.start()
    }

    //save state
    override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.ssIsOpen = isOpen
        return savedState
    }

    //restore state
    override fun onRestoreInstanceState(state: Parcelable) {
        super.onRestoreInstanceState(state)
        if (state is SavedState) {
            isOpen = state.ssIsOpen
            isVisible = isOpen
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChild(switchMode, widthMeasureSpec, heightMeasureSpec)
        measureChild(tvLabel, widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(menuWidth, menuHeight)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onLayout(p0: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val bodyWidth = r - l - paddingLeft - paddingRight
        val left = paddingLeft
        val right = paddingLeft + bodyWidth
        var usedHeight = paddingTop

        btnTextDown.layout(left,
                usedHeight,
                btnWidth,
                btnHeight)

        btnTextUp.layout(right - btnWidth,
                usedHeight,
                right,
                btnHeight)

        usedHeight += btnHeight
        val deltaHLabel = (menuHeight - usedHeight - tvLabel.measuredHeight) / 2

        tvLabel.layout(left + defaultPadding,
                usedHeight + deltaHLabel,
                left + defaultPadding + tvLabel.measuredWidth,
                usedHeight + deltaHLabel + tvLabel.measuredHeight)

        val deltaHSwitch = (menuHeight - usedHeight - switchMode.measuredHeight) / 2
        switchMode.layout(right - defaultPadding - switchMode.measuredWidth,
                usedHeight + deltaHSwitch,
                right - defaultPadding,
                usedHeight + deltaHSwitch + switchMode.measuredHeight)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        canvas.drawLine(0f,
                btnHeight.toFloat(),
                canvas.width.toFloat(),
                btnHeight.toFloat(),
                linePaint)

        canvas.drawLine(canvas.width / 2f,
                0f,
                canvas.width / 2f,
                btnHeight.toFloat(),
                linePaint)
    }

    private class SavedState : BaseSavedState, Parcelable {
        var ssIsOpen: Boolean = false

        constructor(superState: Parcelable?) : super(superState)

        constructor(src: Parcel) : super(src) {
            ssIsOpen = src.readInt() == 1
        }

        override fun writeToParcel(dst: Parcel, flags: Int) {
            super.writeToParcel(dst, flags)
            dst.writeInt(if (ssIsOpen) 1 else 0)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel) = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }

}