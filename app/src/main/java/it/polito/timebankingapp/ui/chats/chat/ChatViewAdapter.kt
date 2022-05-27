package it.polito.timebankingapp.ui.chats.chat

import android.telecom.TelecomManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.chat.ChatMessage
import java.text.SimpleDateFormat
import java.util.*


class ChatViewAdapter(
    private var messageList: MutableList<ChatMessage>,
    private var sendMessage: (ChatMessage) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_MESSAGE_SENT = 1
    private val VIEW_TYPE_MESSAGE_RECEIVED = 2

    private var mMessageList: MutableList<ChatMessage> = messageList
    private var displayedList : MutableList<ChatMessage> = mutableListOf()

    init {
        //rimuovi date dello stesso giorno assegnando il tag "skip"

        var tempMessage: ChatMessage

        /*for(i in mMessageList.indices) {
             = mMessageList[i].copy() //Debug necessario
            if (i > 0) {
                val val1 = mMessageList[i - 1].timestamp.split("-")[0]
                val val2 = mMessageList[i].timestamp.split("-")[0]
                if (val1 == val2 *//*|| val1 == "skip"*//*) {
                    tempMessage.timestamp = "skip-".plus(mMessageList[i].timestamp.split("-")[1])
                }
            }
            displayedList.add(tempMessage)
        }*/
    }

    private class SentMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var messageText: TextView = itemView.findViewById<View>(R.id.text_gchat_message_me) as TextView
        var timeText: TextView = itemView.findViewById<View>(R.id.text_gchat_timestamp_me) as TextView

        var dateText: TextView = itemView.findViewById<View>(R.id.text_gchat_date) as TextView

        fun bind(message: ChatMessage, showDate: Boolean) {
            messageText.text = message.messageText

//            dateText.text = message.timestamp /*if(message.timestamp. =split("-")[0]= "skip") "" else message.timestamp.split("-")[0]*/
//            timeText.text = message.timestamp /*.split("-")[1]*/
            val cal = message.timestamp
            val pattern = "MMM d, yyyy"
            val sdf  = SimpleDateFormat(pattern, Locale.getDefault())
            val date = sdf.format(cal.time)
            val hour = String.format("%02d:%02d", cal[Calendar.HOUR], cal[Calendar.MINUTE] )

            if(showDate)
                dateText.text = date
            else
                dateText.visibility = View.GONE
            /*if(message.timestamp.split("-")[0] == "skip") *//*"" else message.timestamp.split("-")[0]*/
            timeText.text = hour /*.split("-")[1]*//*Utils.formatDateTime(message.getCreatedAt())*/

        }
    }

    private class ReceivedMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var messageText: TextView = itemView.findViewById<View>(R.id.text_gchat_message_other) as TextView
        var timeText: TextView = itemView.findViewById<View>(R.id.text_gchat_timestamp_other) as TextView
        var nameText: TextView = itemView.findViewById<View>(R.id.text_gchat_user_other) as TextView
//        var profileImage: ImageView = itemView.findViewById<View>(R.id.image_gchat_profile_other) as ImageView
        var dateText: TextView = itemView.findViewById<View>(R.id.text_gchat_date) as TextView

        fun bind(message: ChatMessage, showDate : Boolean) {
            messageText.text = message.messageText

            val cal = message.timestamp
            val pattern = "MMM d, yyyy"
            val sdf  = SimpleDateFormat(pattern)
            val date = sdf.format(cal.time)
            val hour = String.format("%02d:%02d", cal[Calendar.HOUR], cal[Calendar.MINUTE] )

            if(showDate)
                dateText.text = date
            else
                dateText.visibility = View.GONE

            timeText.text = hour
            /*nameText.text = message.userName*/

            //profileImage.setImageBitmap(message.profilePic) //da riabilitare più avanti

        }
    }

    //inflate the item_layout-based structure inside each ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vg: View
        return if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            vg = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.fragment_chat_sender_item, parent, false)
            SentMessageHolder(vg)
        } else { // (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            vg = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.fragment_chat_receiver_item, parent, false)
            ReceivedMessageHolder(vg)
        }
    }

    //populate data for each inflated ViewHolder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var putDateText = false
        if(position == 0 || messageList[position-1].timestamp[Calendar.DATE] != messageList[position].timestamp[Calendar.DATE] )
          putDateText = true

        val message: ChatMessage = messageList[position] //text_gchat_date_me

        when (holder.itemViewType) {
            VIEW_TYPE_MESSAGE_SENT -> (holder as SentMessageHolder).bind(message, putDateText)
            VIEW_TYPE_MESSAGE_RECEIVED -> (holder as ReceivedMessageHolder).bind(message, putDateText)
        }
    }

    override fun getItemCount(): Int = messageList.size

    // Determines the appropriate ViewType according to the sender of the message.
    override fun getItemViewType(position: Int): Int {
        val message: ChatMessage = mMessageList[position]
        return if (message.userId == Firebase.auth.uid /*message.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())*/) {
            VIEW_TYPE_MESSAGE_SENT // If the current user is the sender of the message
        } else {
            VIEW_TYPE_MESSAGE_RECEIVED // If some other user sent the message
        }
    }

    fun addMessage(message: ChatMessage) { //si assegna skip a display data e si aggiunge alla lista classica con il timestamp normale
        val i = messageList.size
        val tempMessage = message.copy()
        /*if(i > 0) {
            val val1 = mMessageList[i - 1].timestamp.split("-")[0] //Da debuggare
            val val2 = message.timestamp.split("-")[0]
            if (val1 == val2)
                tempMessage.timestamp = "skip-".plus(message.timestamp.split("-")[1])
        }
        mMessageList.add(message)
        displayedList.add(tempMessage)*/
        Log.d("sizeOfMsgList", i.toString())
        sendMessage(message)
//        notifyItemInserted(i)
    }
}