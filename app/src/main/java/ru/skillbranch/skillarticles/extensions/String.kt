package ru.skillbranch.skillarticles.extensions

public fun String?.indexesOf(substr: String, ignoreCase: Boolean = true): List<Int>{
    var positions: MutableList<Int> = mutableListOf()
    var foundPosition: Int = this?.indexOf(substr, 0, ignoreCase) ?: -1
    while (foundPosition > 0){
        positions.add(foundPosition)
        foundPosition = this?.indexOf(substr, foundPosition + substr.length, ignoreCase) ?: -1
    }

    return positions
}