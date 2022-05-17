package it.polito.timebankingapp.ui.chat

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.message.ChatMessage


class ChatFragment : Fragment(R.layout.fragment_chat_list) {

    private lateinit var rv : RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        rv = view.findViewById(R.id.recycler_gchat)
        rv.layoutManager = LinearLayoutManager(context)

        val tempList = mutableListOf<ChatMessage>()

        val res =  BitmapFactory.decodeResource(requireContext().resources,R.drawable.default_avatar)
        tempList.add(ChatMessage("1","user1","Nome Cognome","Ciao","17/05/2022","11:05",res))
        tempList.add(ChatMessage("2","user2","Name Surname","Ciao","17/05/2022","11:07",res))
        tempList.add(ChatMessage("3","user1","Nome Cognome","Arrivederci","17/05/2022","11:09",res))

        val adTmp = ChatViewAdapter(requireContext(),tempList)
        rv.adapter = adTmp
    }
}