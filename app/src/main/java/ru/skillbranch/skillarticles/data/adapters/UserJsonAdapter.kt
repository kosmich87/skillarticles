package ru.skillbranch.skillarticles.data.adapters

import org.json.JSONObject
import ru.skillbranch.skillarticles.data.local.User
import ru.skillbranch.skillarticles.extensions.asMap

class UserJsonAdapter() : JsonAdapter<User> {
    override fun fromJson(json: String): User? {
        if (json.isNullOrEmpty()) return null
        var values: List<Any?> = json.split(",").map { it.trim() }
        return User(
            id = values[0] as String,
            name = values[1] as String,
            avatar = values[2] as String,
            rating = values[3] as Int,
            respect = values[4] as Int,
            about = values[5] as String
        )
    }

    override fun toJson(obj: User?): String {
        if (obj == null) return ""
        return "${obj.id}, ${obj.name}, ${obj.avatar}, ${obj.rating}, ${obj.respect}, ${obj.about}"
    }
}