package it.polito.timebankingapp.model.chat

import java.io.Serializable

data class ChatsListItem(
    var timeslotId: String = "",
    var userId: String = "",
    var chatMessages: List<ChatMessage> = listOf()
) : Serializable {

    override fun toString(): String = "timeslotId:$timeslotId, userId: $userId, chatMessages: $chatMessages"
}