package it.polito.timebankingapp.model.timeslot

import java.util.*

data class CompactTimeSlot(
    var id: String = "",
    var title: String = "",
    var date: Date = Date(),
    var fee: String = "",
    var nUnreadChats: Int = 0
) {
}