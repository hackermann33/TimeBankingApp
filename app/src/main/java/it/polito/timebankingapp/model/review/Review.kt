package it.polito.timebankingapp.model.review

import it.polito.timebankingapp.model.user.CompactUser
import java.io.Serializable
import java.util.Date



data class Review(
    val referredTimeSlotId: String = "",
    var referredTimeslotTitle: String = "",
    var reviewText: String ="",
    var stars: Int = 0,
    var type: Int = 0,
    var reviewer: CompactUser = CompactUser(),
    var userToReview: CompactUser = CompactUser(),
    var timestamp: Date = Date(),
    var published: Boolean = false,
) : Serializable {

    override fun toString(): String = "reviewText:$reviewText, numStars: $stars, timestamp: $timestamp"


    companion object {
        const val TO_REQUESTER_TYPE = 0
        const val TO_OFFERER_TYPE = 1
    }
}