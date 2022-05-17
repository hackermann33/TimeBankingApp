package it.polito.timebankingapp.ui.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.message.ChatMessage
import it.polito.timebankingapp.ui.timeslots.timeslots_list.MyDiffCallback


class ChatViewAdapter(
    /*var context: Context,*/
    private var messageList: MutableList<ChatMessage>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_MESSAGE_SENT = 1
    private val VIEW_TYPE_MESSAGE_RECEIVED = 2

    /*private val mContext: Context = context*/
    private val mMessageList: List<ChatMessage> = messageList

    private class SentMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var messageText: TextView = itemView.findViewById<View>(R.id.text_gchat_message_me) as TextView
        var timeText: TextView = itemView.findViewById<View>(R.id.text_gchat_timestamp_me) as TextView

        fun bind(message: ChatMessage) {
            messageText.text = message.messageText

            // Format the stored timestamp into a readable String using method.
            //timeText.setText(Utils.formatDateTime(message.getCreatedAt()))
            timeText.text = message.time
        }
    }

    private class ReceivedMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var messageText: TextView = itemView.findViewById<View>(R.id.text_gchat_message_other) as TextView
        var timeText: TextView = itemView.findViewById<View>(R.id.text_gchat_timestamp_other) as TextView
        var nameText: TextView = itemView.findViewById<View>(R.id.text_gchat_user_other) as TextView
        var profileImage: ImageView = itemView.findViewById<View>(R.id.image_gchat_profile_other) as ImageView

        fun bind(message: ChatMessage) {
            messageText.text = message.messageText

            // Format the stored timestamp into a readable String using method.
            timeText.text = message.time/*Utils.formatDateTime(message.getCreatedAt())*/
            nameText.text = "userId.name" /*message.getSender().getNickname()*/

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
        val message: ChatMessage = mMessageList[position]

        when (holder.itemViewType) {
            VIEW_TYPE_MESSAGE_SENT -> (holder as SentMessageHolder).bind(message)
            VIEW_TYPE_MESSAGE_RECEIVED -> (holder as ReceivedMessageHolder).bind(message)
        }
    }

    override fun getItemCount(): Int = messageList.size

    // Determines the appropriate ViewType according to the sender of the message.
    override fun getItemViewType(position: Int): Int {
        val message: ChatMessage = mMessageList[position]
        return if (message.userId != "user1" /*message.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())*/) {
            // If the current user is the sender of the message
            VIEW_TYPE_MESSAGE_SENT
        } else {
            // If some other user sent the message
            VIEW_TYPE_MESSAGE_RECEIVED
        }
    }

    fun addMessage(message: ChatMessage) {
        messageList.add(message)
        notifyDataSetChanged()
    }
}

/*
class MyDiffCallback(private val old: List<TimeSlot>, private val new: List<TimeSlot>): DiffUtil.Callback() {
    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition] === new[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition] == new[newItemPosition]
    }
}*/