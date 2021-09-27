package ru.skillbranch.skillarticles.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
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
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.google.android.material.shape.MaterialShapeDrawable
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.dpToPx
import ru.skillbranch.skillarticles.extensions.setPaddingOptionally
import ru.skillbranch.skillarticles.ui.custom.behaviors.BottombarBehavior
import kotlin.math.hypot

class Bottombar @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ViewGroup(ContextThemeWrapper(context, R.style.ArticleBarsTheme), null, 0), CoordinatorLayout.AttachedBehavior {

    //sizes
    @Px
    private val iconSize = context.dpToIntPx(56)

    @Px
    private val iconPadding = context.dpToIntPx(16)
    private val iconTint = context.getColorStateList(R.color.tint_color)

    //views
    val btnLike: CheckableImageView
    val btnBookmark: CheckableImageView
    val btnShare: AppCompatImageView
    val btnSettings: CheckableImageView

    private val searchBar: SearchBar
    val tvSearchResult
        get() = searchBar.tvSearchResult
    val btnResultUp
        get() = searchBar.btnResultUp
    val btnResultDown
        get() = searchBar.btnResultDown
    val btnSearchClose
        get() = searchBar.btnSearchClose

    var isSearchMode = false

    override fun getBehavior(): CoordinatorLayout.Behavior<Bottombar> {
        return BottombarBehavior()
    }

    init {
        id = R.id.bottombar
        layoutParams = CoordinatorLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
            insetEdge = Gravity.BOTTOM
        }
        val elev = context.dpToPx(4)

        val materialBg = MaterialShapeDrawable.createWithElevationOverlay(context)
        materialBg.elevation = elev
        elevation = elev

        materialBg.elevation = elevation
        background = materialBg

        btnLike = CheckableImageView(context).apply {
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.like_states))
            setPadding(iconPadding)
            imageTintList = iconTint
            setBackgroundResource(R.drawable.ripple)
        }
        addView(btnLike)

        btnBookmark = CheckableImageView(context).apply {
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bookmark_states))
            setPadding(iconPadding)
            imageTintList = iconTint
            setBackgroundResource(R.drawable.ripple)
        }
        addView(btnBookmark)

        btnShare = AppCompatImageView(context).apply {
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_share_black_24dp))
            setPadding(iconPadding)
            imageTintList = iconTint
            isClickable = true
            isFocusable = true
            setBackgroundResource(R.drawable.ripple)
        }
        addView(btnShare)

        btnSettings = CheckableImageView(context).apply {
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_format_size_black_24dp))
            imageTintList = iconTint
            setPadding(iconPadding)
            setBackgroundResource(R.drawable.ripple)
        }
        addView(btnSettings)

        btnShare.setOnClickListener {
            Log.e("Bottombar", "click share")
        }

        searchBar = SearchBar()
        addView(searchBar)
    }

    override fun onSaveInstanceState(): Parcelable {
        val saveState = SavedState(super.onSaveInstanceState())
        saveState.ssIsSearchMode = isSearchMode
        return saveState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        if (state is SavedState) {
            isSearchMode = state.ssIsSearchMode
            searchBar.isVisible = isSearchMode
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        measureChild(searchBar, widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(width, iconSize)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onLayout(p0: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val bodyWidth = r - l - paddingLeft - paddingRight
        val left = paddingLeft
        val right = paddingLeft + bodyWidth
        var usedWidth = left

        btnLike.layout(
                usedWidth,
                0,
                usedWidth + iconSize,
                iconSize
        )

        usedWidth += iconSize

        btnBookmark.layout(
                usedWidth,
                0,
                usedWidth + iconSize,
                iconSize
        )

        usedWidth += iconSize

        btnShare.layout(
                usedWidth,
                0,
                usedWidth + iconSize,
                iconSize
        )

        btnSettings.layout(
                right - iconSize,
                0,
                right,
                iconSize
        )

        searchBar.layout(
                left,
                0,
                right,
                iconSize
        )
    }

    fun setSearchState(isSearch: Boolean) {
        if (isSearch == isSearchMode || !isAttachedToWindow) return
        isSearchMode = isSearch
        if (isSearchMode) animatedShowSearch()
        else animateHideSearch()
    }

    fun setSearchInfo(searchCount: Int = 0, position: Int = 0) {
        btnResultUp.isEnabled = searchCount > 0
        btnResultDown.isEnabled = searchCount > 0

        tvSearchResult.text =
                if (searchCount == 0) "Not found" else "${position.inc()} of $searchCount"

        when (position) {
            0 -> btnResultUp.isEnabled = false
            searchCount.dec() -> btnResultDown.isEnabled = false
        }
    }

    private fun animatedShowSearch() {
        searchBar.isVisible = true
        val endRadius = hypot(width.toDouble(), height / 2.toDouble())
        ViewAnimationUtils.createCircularReveal(
                searchBar,
                width,
                height / 2,
                0f,
                endRadius.toFloat()
        ).start()
    }

    private fun animateHideSearch() {
        val endRadius = hypot(width.toDouble(), height / 2.toDouble())
        ViewAnimationUtils.createCircularReveal(
                searchBar,
                width,
                height / 2,
                endRadius.toFloat(),
                0f
        ).apply {
            doOnEnd { searchBar.isVisible = false }
            start()
        }
    }

    private class SavedState : BaseSavedState, Parcelable {
        var ssIsSearchMode: Boolean = false

        constructor(superState: Parcelable?) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            ssIsSearchMode = parcel.readByte() != 0.toByte()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeByte(if (ssIsSearchMode) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel) = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }

    }

    @SuppressLint("ViewConstructor")
    inner class SearchBar : ViewGroup(context, null, 0) {
        internal val btnSearchClose: AppCompatImageView
        internal val tvSearchResult: TextView
        internal val btnResultDown: AppCompatImageView
        internal val btnResultUp: AppCompatImageView

        @ColorInt
        private val iconColor = context.attrValue(R.attr.colorPrimary)

        init {
            isVisible = false
            setBackgroundColor(context.getColor(R.color.color_on_article_bar))

            btnSearchClose = AppCompatImageView(context).apply {
                setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_close_black_24dp))
                setPadding(iconPadding)
                imageTintList = ColorStateList.valueOf(iconColor)
                setBackgroundResource(R.drawable.ripple)
                isClickable = true
                isFocusable = true
            }
            addView(btnSearchClose)

            tvSearchResult = TextView(context).apply {
                textSize = 14f
                setTextColor(iconColor)
                setPaddingOptionally(left = iconPadding)
                text = "Not found"
            }
            addView(tvSearchResult)

            btnResultDown = AppCompatImageView(context).apply {
                setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_keyboard_arrow_down_black_24dp))
                setPadding(iconPadding)
                imageTintList = ColorStateList.valueOf(iconColor)
                setBackgroundResource(R.drawable.ripple)
                isClickable = true
                isFocusable = true
            }
            addView(btnResultDown)

            btnResultUp = AppCompatImageView(context).apply {
                setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_keyboard_arrow_up_black_24dp))
                setPadding(iconPadding)
                imageTintList = ColorStateList.valueOf(iconColor)
                setBackgroundResource(R.drawable.ripple)
                isClickable = true
                isFocusable = true
            }
            addView(btnResultUp)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
            measureChild(tvSearchResult, widthMeasureSpec, heightMeasureSpec)
            setMeasuredDimension(width, iconSize)
        }

        override fun onLayout(p0: Boolean, l: Int, t: Int, r: Int, b: Int) {
            val bodyWidth = r - l - paddingLeft - paddingRight
            val left = paddingLeft
            val right = paddingLeft + bodyWidth
            var usedWidth = left

            btnSearchClose.layout(
                    usedWidth,
                    0,
                    iconSize,
                    iconSize
            )
            usedWidth += iconSize

            tvSearchResult.layout(
                    usedWidth,
                    (iconSize - tvSearchResult.measuredHeight) / 2,
                    usedWidth + tvSearchResult.measuredWidth,
                    (iconSize + tvSearchResult.measuredHeight) / 2
            )

            btnResultUp.layout(
                    right - iconSize,
                    0,
                    right,
                    iconSize
            )

            btnResultDown.layout(
                    right - 2 * iconSize,
                    0,
                    right - iconSize,
                    iconSize
            )
        }

    }
}