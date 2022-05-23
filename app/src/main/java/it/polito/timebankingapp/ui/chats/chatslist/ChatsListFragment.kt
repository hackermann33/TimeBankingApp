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