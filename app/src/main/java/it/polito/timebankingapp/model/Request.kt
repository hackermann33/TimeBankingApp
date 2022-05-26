package it.polito.timebankingapp.model

import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.model.user.User
import java.util.*

data class Request(
    var requestId: String = " ", val timeSlot: TimeSlot = TimeSlot(), val requester: User = User(), val offerer: User = User(), val lastMessageText: String = "",
    val lastMessageTime: Date = Date(), val status: Int = STATUS_INTERESTED, var users: List<String> = listOf() //needed to check or condition (request is or as a requester or as an offerer)
) {
init {
    requestId = timeSlot.id + "_" + requester.id
    users = listOf(requester.id, offerer.id)

}




    companion object {
        const val STATUS_INTERESTED = 0
    }
}
