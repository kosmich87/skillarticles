package ru.skillbranch.skillarticles.extensions

import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop

fun View.setMarginOptionally(left: Int = marginLeft, top: Int = marginTop, right: Int = marginRight, bottom: Int = marginBottom){
    val param = this.layoutParams as ViewGroup.MarginLayoutParams
    param.setMargins(left, top, right, bottom)
    this.layoutParams = param
}

fun View.setPaddingOptionally(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0){
    this.setPadding(left, top, right, bottom)
}

fun List<Pair<Int, Int>>.groupByBounds(bounds: List<Pair<Int, Int>>): List<MutableList<Pair<Int, Int>>>{
    val results: MutableList<MutableList<Pair<Int, Int>>> = mutableListOf()
    bounds.forEach { bound ->
        var group = mutableListOf<Pair<Int, Int>>()
        this.forEach {
            if (it.first >= bound.first && it.second <= bound.second){
                group.add(it)
            }
        }
        results.add(group)
    }
    return results
}