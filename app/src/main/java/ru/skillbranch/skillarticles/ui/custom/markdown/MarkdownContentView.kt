package ru.skillbranch.skillarticles.ui.custom.markdown

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.util.isEmpty
import androidx.core.view.ViewCompat
import androidx.core.view.children
import ru.skillbranch.skillarticles.data.repositories.MarkdownElement
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.groupByBounds
import ru.skillbranch.skillarticles.extensions.setPaddingOptionally
import kotlin.properties.Delegates

class MarkdownContentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    private lateinit var copyListener: (String) -> Unit
    private var elements: List<MarkdownElement> = emptyList()
    private var layoutManager = LayoutManager()

    var textSize by Delegates.observable(14f) { _, old, value ->
        if (value == old) return@observable
        this.children.forEach {
            it as IMarkdownView
            it.fontSize = value
        }
    }

    var isLoading: Boolean = true
    private val padding = context.dpToIntPx(8)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var usedHeight = paddingTop
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)

        children.forEach {
            measureChild(it, widthMeasureSpec, heightMeasureSpec)
            usedHeight += it.measuredHeight
        }

        usedHeight += paddingBottom
        setMeasuredDimension(width, usedHeight)
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var usedHeight = paddingTop
        val bodyWidth = right - left - paddingLeft - paddingRight
        val left = paddingLeft
        val right = paddingLeft + bodyWidth

        children.forEach {
            if (it is MarkdownTextView) {
                it.layout(
                    left - paddingLeft / 2,
                    usedHeight,
                    r - paddingRight / 2,
                    usedHeight + it.measuredHeight
                )
            } else {
                it.layout(
                    left,
                    usedHeight,
                    right,
                    usedHeight + it.measuredHeight
                )
            }
            usedHeight += it.measuredHeight
        }
    }

    fun setContent(content: List<MarkdownElement>) {
        if (elements.isNotEmpty()) return
        elements = content
        var ind: Int = 1
        content.forEach {
            when (it) {
                is MarkdownElement.Text -> {
                    val tv = MarkdownTextView(context, textSize).apply {
                        setPaddingOptionally(
                            left = padding,
                            right = padding
                        )
                    }

                    MarkdownBuilder(context)
                        .markdownToSpan(it)
                        .run {
                            tv.setText(this, TextView.BufferType.SPANNABLE)
                        }
                    addView(tv)
                }

                is MarkdownElement.Image -> {
                    val iv = MarkdownImageView(
                        context,
                        textSize,
                        it.image.url,
                        it.image.text,
                        it.image.alt
                    )
                    addView(iv)
                    layoutManager.attachToParent(iv, ind)
                }

                is MarkdownElement.Scroll -> {
                    val sv = MarkdownCodeView(
                        context,
                        textSize,
                        it.blockCode.text
                    )
                    sv.copyListener = copyListener
                    addView(sv)
                    layoutManager.attachToParent(sv, ind)
                }
            }
            ind++
        }
    }

    fun renderSearchResult(searchResult: List<Pair<Int, Int>>) {
        children.forEach { view ->
            view as IMarkdownView
            view.clearSearchResult()
        }

        if (searchResult.isEmpty()) return

        val bounds = elements.map { it.bounds }
        val result = searchResult.groupByBounds(bounds)

        children.forEachIndexed { index, view ->
            view as IMarkdownView
            view.renderSearchResult(result[index], elements[index].offset)
        }
    }

    fun renderSearchPosition(
        searchPosition: Pair<Int, Int>?
    ) {
        searchPosition ?: return
        val bounds = elements.map { it.bounds }

        val index = bounds.indexOfFirst { (start, end) ->
            val boundRange = start..end
            val (startPos, endPos) = searchPosition
            startPos in boundRange && endPos in boundRange
        }

        if (index == -1) return
        val view = getChildAt(index)
        view as IMarkdownView
        view.renderSearchPosition(searchPosition, elements[index].offset)
    }

    fun clearSearchResult() {
        children.forEach { view ->
            view as IMarkdownView
            view.clearSearchResult()
        }
    }

    fun setCopyListener(listener: (String) -> Unit) {
        copyListener = listener
    }

    /*override fun onSaveInstanceState(): Parcelable? {
        val state = ContentSavedState(super.onSaveInstanceState())
        state.layout = layoutManager
        return state
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>?) {
        children.forEachIndexed { ind, it ->
            if (it is MarkdownTextView) it.saveHierarchyState(layoutManager.container)
        }
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>?) {
        val superState = container?.get(id) as ContentSavedState
        layoutManager = superState.layout
        children.forEachIndexed { ind, it ->
            if (it !is MarkdownTextView) layoutManager.restoreChild(it, ind)
        }
        super.dispatchRestoreInstanceState(container)
    }*/
}

private class ContentSavedState: View.BaseSavedState, Parcelable {
    var layout: LayoutManager = LayoutManager()

    constructor(superState: Parcelable?) : super(superState)

    constructor(src: Parcel): super(src){
//        layout = src.read
    }
}

private class LayoutManager() : Parcelable {
    var ids: MutableMap<Int, Int> = LinkedHashMap()
    var container: SparseArray<Parcelable> = SparseArray()

    constructor(parcel: Parcel) : this() {
        ids = parcel.readSerializable() as LinkedHashMap<Int, Int>
        container = parcel.readSparseArray<Parcelable>(this::class.java.classLoader) as SparseArray<Parcelable>
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeSerializable(ids as LinkedHashMap)
        parcel.writeSparseArray(container)
    }

    fun attachToParent(view: View, index: Int){
        if (container.isEmpty()){
            ViewCompat.generateViewId().also {
                view.id = it
                ids[index] = it
            }
        }else {
            view.id = ids[index]!!
            view.restoreHierarchyState(container)
        }
    }

    fun restoreChild(view: View, index: Int){
        view.id = ids[index]!!
        view.restoreHierarchyState(container)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LayoutManager> {
        override fun createFromParcel(parcel: Parcel): LayoutManager {
            return LayoutManager(parcel)
        }

        override fun newArray(size: Int): Array<LayoutManager?> {
            return arrayOfNulls(size)
        }
    }

}
		