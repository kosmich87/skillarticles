package ru.skillbranch.skillarticles.extensions

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
