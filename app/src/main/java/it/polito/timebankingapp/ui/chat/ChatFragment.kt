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
        tempList.add(ChatMessage("1","user1","Ciao","17/05/2022-11:05"))
        tempList.add(ChatMessage("2","user2","Ciao, questa è una prova per vedere quanto può essere lungo un messaggio","17/05/2022-11:07"))
        tempList.add(ChatMessage("3","user1","Arrivederci","17/05/2022-11:09"))

        //rimuovi date dello stesso giorno
        val tempList2 = mutableListOf<ChatMessage>()
        for(i in 0 until tempList.size) {
            if (i > 0) {
                val val1 = tempList[i - 1].timestamp.split("-")[0]
                val val2 = tempList[i].timestamp.split("-")[0]
                if (val1 == val2 || val1 == "skip")
                    tempList[i].timestamp = "skip-".plus(tempList[i].timestamp.split("-")[1])
            }
            tempList2.add(tempList[i])
        }

        val adTmp = ChatViewAdapter(tempList)
        rv.adapter = adTmp

        val layoutManager = rv.layoutManager as LinearLayoutManager?
        val sendButton = view.findViewById<Button>(R.id.button_gchat_send)
        val textMessage = view.findViewById<EditText>(R.id.edit_gchat_message)

        sendButton.setOnClickListener {
            val dateCalendar = GregorianCalendar().time
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy-HH:mm")
            val timestamp = dateFormatter.format(dateCalendar)

            if (textMessage.length() > 0) {
                //temporary method
                adTmp.addMessage(ChatMessage("..", "user2", textMessage.text.toString(), timestamp))
                textMessage.text.clear();
                layoutManager!!.scrollToPosition(adTmp.itemCount - 1);
            }
        }
    }
}