package it.polito.timebankingapp.ui.chats.chatslist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.chat.ChatMessage
import it.polito.timebankingapp.model.chat.ChatsListItem

class ChatsListFragment : Fragment(R.layout.fragment_chats_list_list) {

    private lateinit var rv : RecyclerView
    private lateinit var adTmp: ChatsListViewAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        rv = view.findViewById(R.id.recycler_chat_list)
        rv.layoutManager = LinearLayoutManager(context)


        val tempMessagesList = mutableListOf<ChatMessage>()

        //val res =  BitmapFactory.decodeResource(requireContext().resources,R.drawable.default_avatar)
        tempMessagesList.add(ChatMessage("1", "user1", "Ciao", "17/05/2022-11:05"))
        tempMessagesList.add(
            ChatMessage(
                "2"         ,
                "user2",
                "Ciao, questa è una prova per vedere quanto può essere lungo un messaggio",
                "17/05/2022-11:07"
            )
        )
        tempMessagesList.add(ChatMessage("3", "user1", "Arrivederci", "17/05/2022-11:09"))

        //chatsList
        val chatsListItem = ChatsListItem("timeslotID", "requesterId", tempMessagesList)
        val tempChatsList = mutableListOf<ChatsListItem>()

        tempChatsList.add(chatsListItem)
        tempChatsList.add(chatsListItem)
        tempChatsList.add(chatsListItem)

        adTmp = ChatsListViewAdapter(tempChatsList)
        rv.adapter = adTmp
    }
}