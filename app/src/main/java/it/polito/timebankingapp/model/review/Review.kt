package it.polito.timebankingapp.model.review

import java.io.Serializable
import java.sql.Timestamp


data class Review(
    var reviewText: String ="",
    var numStars: Int = 5,
    var timestamp: Timestamp = Timestamp(0),
) : Serializable {

    override fun toString(): String = "reviewText:$reviewText, numStars: $numStars, timestamp: $timestamp"
}