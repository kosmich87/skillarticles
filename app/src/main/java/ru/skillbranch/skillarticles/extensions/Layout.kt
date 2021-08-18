package ru.skillbranch.skillarticles.extensions

import android.text.Layout

fun Layout.getLineHeight(line: Int): Int {
    return getLineTop(line.inc()) - getLineTop(line)
}

fun Layout.getLineTopWithoutPadding(line: Int): Int{
    var lineTop = getLineTop(line)
    if (lineTop == 0){
        lineTop -= topPadding
    }
    val p = topPadding
    return lineTop
}

fun Layout.getLineBottomWithoutPadding(line: Int): Int {
    var lineBottom = getLineBottomWithoutSpacing(line)
    if (line == lineCount.dec()){
        lineBottom -= bottomPadding
    }
    return lineBottom
}

fun Layout.getLineBottomWithoutSpacing(line: Int): Int {
    val lineBottom = getLineBottom(line)
    val isLastLine = line == lineCount.dec()
    val hasLineSpacing = spacingAdd != 0f

    val nextLineIsLast = line == lineCount - 2

    val onlyWhitespaceIsAfter = if (nextLineIsLast) {
        val start = getLineStart(line + 1)
        val lastVisible = getLineVisibleEnd(line + 1)
        start == lastVisible
    } else false

    return if (!hasLineSpacing || isLastLine || onlyWhitespaceIsAfter) {
        lineBottom
    } else {
        lineBottom - spacingAdd.toInt()
    }
}
