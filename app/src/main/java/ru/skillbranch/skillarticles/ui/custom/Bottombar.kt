package ru.skillbranch.skillarticles.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.*
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
) : ViewGroup(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {

    //sizes
    @Px
    private val iconSize = context.dpToIntPx(56)
    @Px
    private val iconPadding = context.dpToIntPx(16)
    private val iconTint = context.getColorStateList(R.color.tint_color)
    @DrawableRes
    private val bg = context.attrValue(R.attr.selectableItemBackground, needRes = true)

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
        val materialBg = MaterialShapeDrawable.createWithElevationOverlay(context)
        materialBg.elevation = elevation
        setBackgroundColor(bg)
        background = materialBg

        btnLike = CheckableImageView(context).apply {
            setImageResource(R.drawable.like_states)
            imageTintList = iconTint
            setPadding(iconPadding)
        }
        addView(btnLike)

        btnBookmark = CheckableImageView(context).apply {
            setImageResource(R.drawable.bookmark_states)
            imageTintList = iconTint
            setPadding(iconPadding)
        }
        addView(btnBookmark)

        btnShare = AppCompatImageView(context).apply {
            setImageResource(R.drawable.ic_share_black_24dp)
            imageTintList = iconTint
            setPadding(iconPadding)
        }
        addView(btnShare)

        btnSettings = CheckableImageView(context).apply {
            setImageResource(R.drawable.ic_format_size_black_24dp)
            imageTintList = iconTint
            setPadding(iconPadding)
        }
        addView(btnSettings)

        searchBar = SearchBar().apply {
            visibility = View.INVISIBLE
        }
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
//        var usedHeight = 0
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)

        Log.d("Bottombar", "width: $width")

        measureChild(btnLike, widthMeasureSpec, heightMeasureSpec)
        measureChild(btnBookmark, widthMeasureSpec, heightMeasureSpec)
        measureChild(btnShare, widthMeasureSpec, heightMeasureSpec)
        measureChild(btnSettings, widthMeasureSpec, heightMeasureSpec)

//        usedHeight += btnLike.measuredHeight + paddingTop + paddingBottom
        setMeasuredDimension(width, iconSize)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onLayout(p0: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val usedHeight = paddingTop
        val bodyWidth = r - l - paddingLeft - paddingRight
        val left = paddingLeft
        val right = paddingLeft + bodyWidth

        btnLike.layout(
            left,
            iconSize,
            iconSize,
            0
        )

        btnBookmark.layout(
            left + 2 * iconSize,
            iconSize,
            left + 3 * iconSize,
            0
        )

        btnShare.layout(
            left + 3 * iconSize,
            iconSize,
            left + 4 * iconSize,
            0
        )

        btnSettings.layout(
            right - iconSize,
            iconSize,
            right,
            0
        )
    }

    fun setSearchState(isSearch: Boolean) {

    }

    fun setSearchInfo(searchCount: Int = 0, position: Int = 0) {

    }

    private fun animatedShowSearch() {
        this?.isVisible = true
        val endRadius = hypot(this?.width.toFloat() ?: 0f, this?.height.toFloat() ?: 0f)
        val va = ViewAnimationUtils.createCircularReveal(
            this,
            this?.width ?: 0,
            this?.height ?: 0,
            0f,
            endRadius
        )
        va.start()
    }

    private fun animateHideSearch() {
        val endRadius = hypot(this.width?.toFloat() ?: 0f, this.height?.toFloat() ?: 0f)
        val va = ViewAnimationUtils.createCircularReveal(
            this,
            this?.width ?: 0,
            this?.height ?: 0,
            endRadius,
            0f
        )
        va.doOnEnd { this?.isVisible = false }
        va.start()
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
            btnSearchClose = AppCompatImageView(context).apply {
                setImageResource(R.drawable.ic_close_black_24dp)
                imageTintList = iconTint
            }
            addView(btnSearchClose)

            tvSearchResult = TextView(context).apply {
                setTextColor(iconColor)
            }
            addView(tvSearchResult)

            btnResultDown = AppCompatImageView(context).apply {
                setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
                imageTintList = iconTint
            }
            addView(btnResultDown)

            btnResultUp = AppCompatImageView(context).apply {
                setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
                imageTintList = iconTint
            }
            addView(btnResultUp)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            var usedHeight = 0
            val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)

            measureChild(btnSearchClose, widthMeasureSpec, heightMeasureSpec)
            measureChild(tvSearchResult, widthMeasureSpec, heightMeasureSpec)
            measureChild(btnResultDown, widthMeasureSpec, heightMeasureSpec)
            measureChild(btnResultUp, widthMeasureSpec, heightMeasureSpec)

            usedHeight += btnSearchClose.measuredHeight + paddingTop + paddingBottom
            setMeasuredDimension(width, usedHeight)
        }

        override fun onLayout(p0: Boolean, l: Int, t: Int, r: Int, b: Int) {
            val usedHeight = paddingTop
            val bodyWidth = r - l - paddingLeft - paddingRight
            val left = paddingLeft
            val right = paddingLeft + bodyWidth

            btnSearchClose.layout(
                left,
                iconSize,
                iconSize,
                0
            )

            btnResultUp.layout(
                right - iconSize,
                iconSize,
                right,
                0
            )

            btnResultDown.layout(
                right - 2 * iconSize,
                iconSize,
                right - iconSize,
                0
            )

            tvSearchResult.layout(
                left + iconSize,
                iconSize,
                right - 2 * iconSize,
                0
            )
        }

    }
}