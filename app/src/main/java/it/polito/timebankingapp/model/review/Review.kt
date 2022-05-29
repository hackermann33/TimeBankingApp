package it.polito.timebankingapp.model.review

import java.io.Serializable
import java.util.Date




data class Review(
    var reviewText: String ="",
    var stars: Int = 0,
    var role: String = "",
    var reviewer: Map<String, String> = mapOf("" to "id", "" to "fullName", "" to "profilePicUrl"),
    var timestamp: Date = Date(0),
) : Serializable {

    override fun toString(): String = "reviewText:$reviewText, numStars: $stars, timestamp: $timestamp"

    companion object {
        const val AS_OFFERER_TYPE = 0
        const val AS_REQUESTER_TYPE = 1
    }
}