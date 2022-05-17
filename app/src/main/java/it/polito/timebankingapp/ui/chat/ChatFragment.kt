package it.polito.timebankingapp.ui.chat

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.message.ChatMessage
import java.text.SimpleDateFormat
import java.util.*


class ChatFragment : Fragment(R.layout.fragment_chat_list) {

    private lateinit var rv : RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        rv = view.findViewById(R.id.recycler_gchat)
        rv.layoutManager = LinearLayoutManager(context)

        val tempList = mutableListOf<ChatMessage>()

        //val res =  BitmapFactory.decodeResource(requireContext().resources,R.drawable.default_avatar)
        tempList.add(ChatMessage("1","user1","Ciao","17/05/2022","11:05"))
        tempList.add(ChatMessage("2","user2","Ciao","17/05/2022","11:07"))
        tempList.add(ChatMessage("3","user1","Arrivederci","17/05/2022","11:09"))

        val adTmp = ChatViewAdapter(/*requireContext(),*/tempList)
        rv.adapter = adTmp

        var sendButton = view.findViewById<Button>(R.id.button_gchat_send)
        var textMessage = view.findViewById<EditText>(R.id.edit_gchat_message)
        sendButton.setOnClickListener {
            //temporary method
            val dateCalendar = GregorianCalendar().time
            val dateFormatter = SimpleDateFormat("yyyy.MM.dd").toLocalizedPattern()
            val timeFormatter = SimpleDateFormat("HH.mm")
            val date = dateFormatter.format(dateCalendar)
            val time = timeFormatter.format(dateCalendar)

            adTmp.addMessage(ChatMessage("..", "user2", textMessage.text.toString(), date, time))
            textMessage.setText("")
        }
    }
}